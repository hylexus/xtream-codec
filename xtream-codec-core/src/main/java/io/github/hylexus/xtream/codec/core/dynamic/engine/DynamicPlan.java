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

package io.github.hylexus.xtream.codec.core.dynamic.engine;

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 编译后的不可变结构计划。字段路径、引用目标和分支在编译时固定，
 * 因而同一个计划可以由多个线程并发复用。游标、已解析值、资源计数和 Trace 节点
 * 均由每次调用新建的 {@link DynamicRuntime} 持有，不写入计划。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public final class DynamicPlan {
    private final DynamicSchema schema;
    private final int version;
    private final DynamicLayout layout;

    DynamicPlan(DynamicSchema schema, int version, DynamicLayout layout) {
        this.layout = layout;
        this.schema = schema;
        this.version = version;
    }

    public DynamicSchema schema() {
        return schema;
    }

    /**
     * 编译时选定的协议版本；执行时无需再次传入或逐字段匹配。
     */
    public int version() {
        return version;
    }

    /**
     * 按字段定义顺序将值写入调用方缓冲区，返回实际写入的位数。
     * 从 writerIndex 写入；失败可能保留部分输出，调用方应丢弃该结果。
     * 缓冲区的分配与释放仍由调用方负责。
     */
    public long encode(DynamicValues values, ByteBuf output) {
        return encode(values, output, DynamicLimits.DEFAULT);
    }

    public long encode(DynamicValues values, ByteBuf output, DynamicLimits limits) {
        return encode(values, output, limits, null);
    }

    public long encode(DynamicValues values, ByteBuf output, @Nullable CodecTracker tracker) {
        return encode(values, output, DynamicLimits.DEFAULT, tracker);
    }

    /**
     * 使用指定资源上限执行编码；仅在提供 tracker 时创建字段级跟踪节点。
     * 编码与跟踪共用同一条执行路径，便于比较调试和生产结果。
     */
    public long encode(DynamicValues values, ByteBuf output, DynamicLimits limits, @Nullable CodecTracker tracker) {
        return new DynamicWriteExecution(layout, output, limits, tracker).encode(values.fields());
    }

    /**
     * 从当前 readerIndex 开始解码，返回值及消费量；失败不提交 readerIndex。
     * 校验和不匹配默认仅记录在结果中，不中断解码；严格核对需显式传入解码选项。
     * 缓冲区所有权及引用计数仍属于调用方。
     */
    public DynamicDecodeResult decode(ByteBuf input) {
        return decode(input, DynamicDecodeOptions.DEFAULT);
    }

    public DynamicDecodeResult decode(ByteBuf input, DynamicLimits limits) {
        return decode(input, limits, null);
    }

    public DynamicDecodeResult decode(ByteBuf input, @Nullable CodecTracker tracker) {
        return decode(input, DynamicLimits.DEFAULT, tracker);
    }

    /**
     * 使用指定资源上限解码。结构结束后的字节保持未消费，便于上层继续解析后续数据。
     */
    public DynamicDecodeResult decode(ByteBuf input, DynamicLimits limits, @Nullable CodecTracker tracker) {
        return decode(input, DynamicDecodeOptions.DEFAULT.withLimits(limits), tracker);
    }

    /**
     * 按单次调用选项解码；使用 {@link DynamicDecodeOptions#STRICT} 拒绝校验和不匹配的报文。
     */
    public DynamicDecodeResult decode(ByteBuf input, DynamicDecodeOptions options) {
        return decode(input, options, null);
    }

    /**
     * 按单次调用选项解码，并可选记录跟踪信息。默认模式的校验不匹配记录为警告，
     * 严格模式及结构错误仍抛出异常且不推进调用方的 readerIndex。
     */
    public DynamicDecodeResult decode(ByteBuf input, DynamicDecodeOptions options, @Nullable CodecTracker tracker) {
        return new DynamicReadExecution(layout, input, options, tracker).decode();
    }

    List<DynamicLayout.Node> fields() {
        return layout.nodes();
    }
}
