/*
 * Copyright 2024-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.hylexus.xtream.codec.core.debugger;

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicCodecException;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicCompiler;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicCompileOptions;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicDecodeResult;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicChecksumResult;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicLimits;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicPlan;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicValues;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtensionRegistry;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.debugger.DebugCodecResponse.Error;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.jspecify.annotations.Nullable;

import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 可直接嵌入任意容器的调试业务服务，不引用 HTTP 类型或具体协议模块。
 * entity 模式复用现有注解实体编解码；dynamic 模式编译结构定义并执行通用计划。
 * 两种模式返回同一响应合同，供独立页面或现有 dashboard 的适配层调用。
 * 实体与字段映射之间的转换由 {@link DebugEntityValueMapper} 完成，服务 API 不暴露具体 JSON 库类型。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public final class CodecDebuggerService {
    private final DebugEntityCatalog catalog;
    private final DebugEntityValueMapper valueMapper;
    private final DynamicExtensionRegistry registry;
    private final DynamicLimits limits;
    private final DynamicCompileOptions compileOptions;

    /**
     * 使用默认的 Jackson 2 实体值转换器、动态编译配置及资源上限。
     * 宿主需提供 Jackson 2 databind；使用其他转换器时选择完整配置构造器。
     *
     * @param catalog 宿主允许调试的实体目录
     */
    public CodecDebuggerService(DebugEntityCatalog catalog) {
        this(catalog, new Jackson2DebugEntityValueMapper(), DynamicCompileOptions.DEFAULT, DynamicLimits.DEFAULT);
    }

    /**
     * 使用宿主的实体值转换器、动态编译配置及资源上限。
     *
     * @param catalog        宿主允许调试的实体目录
     * @param valueMapper    可并发复用的实体值转换器
     * @param compileOptions 包含扩展注册表和表达式引擎的动态编译配置
     * @param limits         单次编解码的资源上限
     */
    public CodecDebuggerService(
            DebugEntityCatalog catalog, DebugEntityValueMapper valueMapper,
            DynamicCompileOptions compileOptions, DynamicLimits limits) {
        this.compileOptions = compileOptions;
        this.catalog = catalog;
        this.valueMapper = valueMapper;
        this.registry = compileOptions.extensions();
        this.limits = limits;
    }

    /**
     * 返回宿主允许调试的实体目录，供页面搜索和选择。
     */
    public List<DebugEntityCatalog.Entry> entities() {
        return catalog.entries();
    }

    /**
     * 返回已注册的动态扩展标识，供结构编辑器展示可用能力。
     */
    public List<String> extensions() {
        return registry.extensions().keySet().stream().sorted().toList();
    }

    /**
     * 生成实体的初始字段值模板，类型必须来自目录白名单。
     */
    public Map<String, Object> entityTemplate(String id) {
        // 模板只从白名单中的类型创建，页面无需知道 Java 构造方法或字段反射细节。
        try {
            return valueMapper.toValues(catalog.resolve(id).getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalArgumentException("实体必须具有可访问的无参构造方法", exception);
        }
    }

    /**
     * 执行一次实体或动态编解码，统一返回原始 HEX、值、消费量和 Trace。
     * 业务异常会转为包含部分跟踪信息的失败响应。
     */
    public DebugCodecResponse execute(DebugCodecRequest request) {
        // 每次请求独立分配 tracker 与缓冲区，服务实例可以并发复用。
        final boolean encoding = request.direction() == DebugCodecDirection.ENCODE;
        final CodecTracker tracker = new CodecTracker();
        final int capacity = encoding ? limits.maxOutputBytes() : limits.maxInputBytes();
        final ByteBuf buffer = Unpooled.buffer(Math.min(256, capacity), capacity);
        try {
            try {
                if (!encoding) {
                    readHex(request, buffer);
                }
                final Outcome outcome = switch (request.mode()) {
                    case DYNAMIC -> dynamic(request, buffer, tracker, encoding);
                    case ENTITY -> entity(request, buffer, tracker, encoding);
                };
                return response(outcome.values(), outcome.consumedBits(), null, buffer, tracker, encoding, outcome.checksums());
            } catch (RuntimeException exception) {
                // 统一转为可展示的响应，同时保留已完成字段的部分 Trace。
                final DebugCodecResponse.Error error = error(exception);
                final Long completed = tracker.getTrace().getRoot().getBitEnd();
                final long consumedBits = completed == null
                        ? Math.max(buffer.readerIndex(), encoding ? buffer.writerIndex() : 0) * 8L
                        : completed;
                return response(null, consumedBits, error, buffer, tracker, encoding, List.of());
            }
        } finally {
            buffer.release();
        }
    }

    private void readHex(DebugCodecRequest request, ByteBuf buffer) {
        // 在 HEX 解析前限制长度，避免异常大输入先分配大量临时内存。
        final String hex = Objects.requireNonNull(request.hex(), "解码需要 hex").replaceAll("\\s+", "");
        if (hex.length() > 2L * limits.maxInputBytes()) {
            throw new IllegalArgumentException("输入超过资源上限");
        }
        buffer.writeBytes(HexFormat.of().parseHex(hex));
    }

    private Outcome dynamic(DebugCodecRequest request, ByteBuf buffer, CodecTracker tracker, boolean encoding) {
        final int version = request.version() == null ? DynamicSchema.ALL_VERSION : request.version();
        final DynamicPlan plan = DynamicCompiler.compile(Objects.requireNonNull(request.schema(), "动态模式需要 schema"), compileOptions.withVersion(version));
        if (encoding) {
            final Map<String, Object> input = Objects.requireNonNull(request.values(), "编码需要 values");
            final long consumedBits = plan.encode(DynamicValues.of(input), buffer, limits, tracker);
            // 再用同一个计划解码已编码字节，响应给出实际线上值与回填字段。
            final DynamicDecodeResult result = plan.decode(buffer.duplicate(), limits);
            return new Outcome(result.values().fields(), consumedBits, result.checksums());
        }
        final DynamicDecodeResult result = plan.decode(buffer, limits, tracker);
        return new Outcome(result.values().fields(), result.consumedBits(), result.checksums());
    }

    private Outcome entity(DebugCodecRequest request, ByteBuf buffer, CodecTracker tracker, boolean encoding) {
        // 实体类型来自受限目录；HTTP 请求中的类名不能被直接加载。
        final Class<?> type = catalog.resolve(Objects.requireNonNull(request.entityId(), "实体模式需要 entityId"));
        final int version = request.version() == null ? XtreamField.ALL_VERSION : request.version();
        if (encoding) {
            final Object values = valueMapper.toEntity(Objects.requireNonNull(request.values(), "编码需要 values"), type);
            EntityCodec.DEFAULT.encode(version, values, buffer, tracker);
            if (buffer.writerIndex() > limits.maxOutputBytes()) {
                throw new IllegalArgumentException("输出超过资源上限");
            }
            return new Outcome(valueMapper.toValues(values), buffer.writerIndex() * 8L, List.of());
        }
        return new Outcome(valueMapper.toValues(EntityCodec.DEFAULT.decode(version, type, buffer, tracker)),
                buffer.readerIndex() * 8L, List.of());
    }

    private DebugCodecResponse response(@Nullable Map<String, Object> values, long consumedBits, @Nullable Error error,
                                        ByteBuf buffer, CodecTracker tracker, boolean encoding, List<DynamicChecksumResult> checksums) {
        // HEX 始终是原始报文字节，不添加特定协议的帧头、转义或校验。
        final int consumedBytes = (int) ((consumedBits + 7) / 8);
        final String hex = ByteBufUtil.hexDump(buffer, 0, buffer.writerIndex());
        final int remainingStart = Math.min(consumedBytes, buffer.writerIndex());
        final String remaining = encoding ? "" : ByteBufUtil.hexDump(buffer, remainingStart, buffer.writerIndex() - remainingStart);
        return new DebugCodecResponse(error == null, values, hex,
                consumedBits, consumedBytes, remaining, tracker.toTraceView(), error, checksums);
    }

    private static DebugCodecResponse.Error error(RuntimeException exception) {
        final String detail = exception.getMessage();
        final String message = detail == null ? exception.getClass().getSimpleName() : detail;
        if (exception instanceof DynamicCodecException codecError) {
            return new DebugCodecResponse.Error(message, codecError.fieldPath(), codecError.bitOffset());
        }
        return new DebugCodecResponse.Error(message, null, null);
    }

    private record Outcome(@Nullable Map<String, Object> values, long consumedBits, List<DynamicChecksumResult> checksums) {
    }
}
