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

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLengthUnit;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNodeKind;
import io.github.hylexus.xtream.codec.core.tracker.CodecTraceDiagnostic;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicLayout.*;

/**
 * 读取编译布局并构造返回值；只有完整执行成功才提交输入游标。
 * 每次解码创建独立的读取器和运行状态，从调用方缓冲区当前 readerIndex 开始读取。
 * 字段按布局顺序进入节点入口，解析后的值保存在活动帧中，供条件、长度、派生值和分支引用。
 * 容器按声明长度约束读取范围；数组元素经过节点入口，条目按需设置子边界。
 * 解码失败不移动调用方的 readerIndex，缓冲区仍由调用方持有。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
final class DynamicReadExecution {
    // 本次解码的位游标；读取过程中不直接推进缓冲区的 readerIndex。
    private final DynamicBitReader reader;
    // 本次解码的活动帧、容器边界、集合计数和可选跟踪状态。
    private final DynamicRuntime runtime;
    private final DynamicDecodeOptions options;
    // 未配置或未执行校验字段时不分配列表。
    private @Nullable List<DynamicChecksumResult> checksums;

    /**
     * 为一次解码绑定可复用布局、输入缓冲区和资源限制。
     *
     * @param layout  编译后的执行布局
     * @param input   调用方持有的输入缓冲区
     * @param options 资源限制和校验和处理策略
     * @param tracker 可选的跟踪器，为空时不创建跟踪节点
     */
    DynamicReadExecution(DynamicLayout layout, ByteBuf input, DynamicDecodeOptions options, @Nullable CodecTracker tracker) {
        this.options = options;
        reader = new DynamicBitReader(input, options.limits());
        runtime = new DynamicRuntime(layout, reader, input, options.limits(), tracker);
    }

    /**
     * 按根节点顺序解码，要求最终位置字节对齐，成功后一次性提交读取位置。
     * 结果同时报告实际消费的位数、字节数和缓冲区的剩余字节数；跟踪模式在成功或失败时结算记录。
     *
     * @return 本次解码的字段值与消费量
     */
    DynamicDecodeResult decode() {
        if (runtime.tracker != null) {
            runtime.tracker.beginDecode(reader.start, null);
        }
        boolean success = false;
        try {
            final Map<String, Object> values = fields(runtime.layout.nodes(), 0);
            reader.checkAligned("$");
            reader.commit();
            success = true;
            return new DynamicDecodeResult(DynamicValues.of(values), reader.position(), reader.consumedBytes(),
                    runtime.buffer.readableBytes(), checksums == null ? List.of() : checksums);
        } catch (DynamicCodecException error) {
            runtime.failure(null, error);
            throw error;
        } finally {
            runtime.finish(success, runtime.buffer.writerIndex() - reader.start);
        }
    }

    /**
     * 读取同一结构中的字段，跳过条件不成立的节点，并按布局顺序收集可见结果。
     * 节点入口会将解析值保存到当前帧；返回对象中的大整数经过 JSON 安全转换。
     *
     * @param nodes 按报文顺序排列的字段节点
     * @param depth 当前嵌套深度
     * @return 字段标识到解码值的映射
     */
    private Map<String, Object> fields(List<Node> nodes, int depth) {
        final Map<String, Object> result = new LinkedHashMap<>();
        for (final Node node : nodes) {
            for (final Backfill action : node.starts()) {
                if (action.checksum() != null) {
                    runtime.frame.beginRegion(action.target(), reader.position());
                }
            }
            final Object value = read(node, depth, CodecTraceNodeKind.FIELD, node.id());
            if (value != DynamicFrame.MISSING) {
                result.put(node.id(), DynamicRuntime.json(value));
            }
            for (final Backfill action : node.ends()) {
                if (action.checksum() != null && (!action.deferred() || runtime.frame.get(action.target()) != DynamicFrame.MISSING)) {
                    runtime.frame.checksum(action.target(), runtime.checksum(action));
                    verifyChecksum(action, runtime.frame.get(action.target()), runtime.frame.trace(action.target()));
                }
            }
        }
        return result;
    }

    /**
     * 节点统一入口：进入作用域、检查条件、读取内容，并在结束时恢复外层帧和跟踪父节点。
     * 条件跳过时记录缺失标记；读取成功后将值存入帧，供后续节点引用。
     *
     * @param node  当前执行节点
     * @param depth 当前嵌套深度，用于资源限制检查
     * @param kind  跟踪节点类型
     * @param name  跟踪名称，例如字段标识、元素下标或条目键
     * @return 节点结果；条件跳过时返回缺失标记，无业务值时可为空
     */
    private @Nullable Object read(Node node, int depth, CodecTraceNodeKind kind, String name) {
        final DynamicFrame previousFrame = runtime.enter(node);
        final CodecTraceNode previousParent = runtime.parent;
        final long start = reader.position();
        CodecTraceNode trace = null;
        Object value = DynamicFrame.MISSING;
        boolean success = false;
        try {
            if (!runtime.present(node, depth)) {
                runtime.frame.put(node.address(), DynamicFrame.MISSING);
                return DynamicFrame.MISSING;
            }
            trace = runtime.begin(node, start, kind, name);

            value = this.content(node, depth);

            runtime.frame.put(node.address(), value);
            if (node.operation() instanceof IntegerOp number && number.backfill() != null && number.backfill().checksum() != null) {
                verifyChecksum(number.backfill(), value, trace);
            }
            success = true;
            return value;
        } catch (RuntimeException exception) {
            final var error = runtime.wrap(node, exception);
            runtime.failure(trace, error);
            throw error;
        } finally {
            runtime.end(node, trace, start, value == DynamicFrame.MISSING ? null : value, success);
            runtime.parent = previousParent;
            runtime.frame = previousFrame;
        }
    }

    /**
     * 前置校验字段在覆盖区间结束后核对，后置字段在读取实际值后核对；每个字段只记录一次。
     * 默认返回结果并可选附加跟踪警告，严格模式在提交输入游标前抛出异常。
     */
    private void verifyChecksum(Backfill action, @Nullable Object value, @Nullable CodecTraceNode trace) {
        final Long expected = runtime.frame.checksum(action.target());
        if (expected == null || value == DynamicFrame.MISSING) {
            return;
        }
        final long actual = DynamicNumbers.requireInteger(value, action.path(), reader.position()).longValueExact();
        final var result = new DynamicChecksumResult(action.path(), Objects.requireNonNull(action.checksum()), actual, expected);
        if (checksums == null) {
            checksums = new ArrayList<>();
        }
        checksums.add(result);
        if (result.matched()) {
            return;
        }
        final String message = "校验和不匹配，实际值: " + actual + "，计算值: " + expected;
        if (options.strictChecksums()) {
            throw new DynamicCodecException(action.path(), reader.position(), message);
        }
        if (runtime.tracker != null) {
            final CodecTraceNode target = trace == null ? runtime.tracker.getTrace().getRoot() : trace;
            final long offset = target.getBitStart() == null ? reader.position() : target.getBitStart();
            final var diagnostic = new CodecTraceDiagnostic("WARNING", action.path() + ": " + message,
                    target.getId(), (int) (offset / 8), null, offset);
            target.addDiagnostic(diagnostic);
            runtime.tracker.getTrace().addDiagnostic(diagnostic);
        }
    }

    /**
     * 根据互斥操作读取节点内容，条件和作用域已由外层入口处理。
     * 整数字段按符号和位宽解释；字节字段按配置解码；对齐位必须匹配填充值。
     * choice 选中的分支仍经过节点入口，派生值只求值而不消费输入。
     */
    private @Nullable Object content(Node node, int depth) {
        return switch (node.operation()) {
            case IntegerOp number -> {
                if (number.bits() % 8 == 0) {
                    this.reader.checkAligned(node.path());
                }
                yield DynamicRuntime.integer(this.reader.readLong(number.bits(), number.little(), number.lsb(), runtime.boundary, node.path()), number);
            }
            case FloatOp number -> {
                this.reader.checkAligned(node.path());
                final long raw = this.reader.readLong(number.bits(), number.little(), number.lsb(), runtime.boundary, node.path());
                final double value = number.bits() == 32 ? Float.intBitsToFloat((int) raw) : Double.longBitsToDouble(raw);
                if (!Double.isFinite(value)) {
                    throw runtime.error(node, "浮点数必须为有限数值");
                }
                yield value;
            }
            case BytesOp bytes -> {
                this.reader.checkAligned(node.path());
                final long size = DynamicRuntime.bits(bytes.length(), units(bytes.length(), node));
                if (size % 8 != 0 || size / 8 > Integer.MAX_VALUE) {
                    throw runtime.error(node, "字节字段长度必须为完整字节");
                }
                final byte[] input = this.reader.readBytes((int) (size / 8), runtime.boundary, node.path());
                yield bytes.format().extension() == null ? DynamicByteFormat.decode(bytes.format(), input)
                        : bytes.format().extension().decode(input, bytes.format().config());
            }
            case BitsOp bits -> {
                this.reader.checkBoundary(bits.bits(), runtime.boundary, node.path());
                final StringBuilder result = new StringBuilder(bits.bits());
                for (int index = 0; index < bits.bits(); index++) {
                    result.append(reader.readLong(1, false, bits.lsb(), runtime.boundary, node.path()));
                }
                yield result.toString();
            }
            case AlignOp align -> {
                final int count = (int) ((align.alignment() - this.reader.position() % align.alignment()) % align.alignment());
                for (int index = 0; index < count; index++) {
                    if (this.reader.readLong(1, false, align.lsb(), runtime.boundary, node.path()) != align.fill()) {
                        throw new DynamicCodecException(node.path(), reader.position() - 1, "对齐填充值不匹配");
                    }
                }
                yield null;
            }
            case StructOp structure -> this.structure(node, structure, depth);
            case ArrayOp array -> this.array(node, array, depth);
            case EntriesOp entries -> this.entries(node, entries, depth);
            case ChoiceOp choice -> {
                final Node selected = runtime.choose(choice, node);
                yield this.read(selected, depth + 1, CodecTraceNodeKind.FIELD, selected.id());
            }
            case DerivedOp derived -> runtime.derived(derived, node);
        };
    }

    /**
     * 在声明边界内读取结构字段，并在退出时恢复父容器边界。
     * 对非剩余量长度，子字段必须恰好消费整个结构内容。
     */
    private Map<String, Object> structure(Node node, StructOp structure, int depth) {
        final long parent = runtime.boundary;
        runtime.boundary = boundary(structure.length(), node);
        try {
            final var result = fields(structure.fields(), depth + 1);
            exact(structure.length(), node);
            return result;
        } finally {
            runtime.boundary = parent;
        }
    }

    /**
     * 按 item 单位的声明次数，或按 bit、byte 单位的内容边界读取数组。
     * 每个元素经过节点入口且必须推进游标，避免条件跳过造成循环停滞。
     *
     * @return 保留元素顺序的不可修改列表
     */
    private List<Object> array(Node node, ArrayOp array, int depth) {
        final Length length = array.length();
        final long count = length.unit() == DynamicLengthUnit.ITEM ? units(length, node) : -1;
        runtime.requireItems(count, node);
        final long parent = runtime.boundary;
        if (count < 0) {
            runtime.boundary = boundary(length, node);
        }
        final List<Object> result = new ArrayList<>();
        try {
            for (int index = 0; count >= 0 ? index < count : reader.position() < runtime.boundary; index++) {
                runtime.item(node);
                final long start = reader.position();
                final Object value = read(array.item(), depth + 1, CodecTraceNodeKind.COLLECTION_ITEM, "[" + index + "]");
                if (reader.position() == start) {
                    throw runtime.error(node, "集合元素未推进游标");
                }
                result.add(DynamicRuntime.json(value));
            }
            if (count < 0) {
                exact(length, node);
            }
            return Collections.unmodifiableList(result);
        } finally {
            runtime.boundary = parent;
        }
    }

    /**
     * 按计数或容器边界读取 TLV、map 和 pair 条目，保留重复键及输入顺序。
     * 非 pair 条目的字节长度形成临时子边界；已知键由分支解析，未知键保留为 rawHex。
     * 条目值必须恰好消费其声明长度，离开条目及集合时分别恢复上层边界。
     *
     * @return 保留条目顺序的不可修改列表
     */
    private List<Object> entries(Node node, EntriesOp entries, int depth) {
        final Length length = entries.length();
        final long count = length.unit() == DynamicLengthUnit.ITEM ? units(length, node) : -1;
        runtime.requireItems(count, node);
        final long parent = runtime.boundary;
        if (count < 0) {
            runtime.boundary = boundary(length, node);
        }
        final List<Object> result = new ArrayList<>();
        try {
            for (int index = 0; count >= 0 ? index < count : reader.position() < runtime.boundary; index++) {
                runtime.item(node);
                final long key = reader.readLong(entries.keyBits(), false, false, runtime.boundary, node.path());
                final Node variant = entries.variants().get(Long.toString(key));
                if (entries.pair() && variant == null) {
                    throw runtime.error(node, "pair 的未知键没有可确定的值边界");
                }
                final long lengthUnits = entries.pair() ? -1
                        : reader.readLong(entries.lengthBits(), false, false, runtime.boundary, node.path());
                final long entryParent = runtime.boundary;
                if (lengthUnits >= 0) {
                    reader.checkBoundary(Math.multiplyExact(lengthUnits, 8), runtime.boundary, node.path());
                    runtime.boundary = reader.position() + lengthUnits * 8;
                }
                try {
                    final Object value = variant == null
                            ? HexFormat.of().formatHex(reader.readBytes(Math.toIntExact(lengthUnits), runtime.boundary, node.path()))
                            : read(variant, depth + 1, CodecTraceNodeKind.MAP_ENTRY, "[" + key + "]");
                    if (!entries.pair() && reader.position() != runtime.boundary) {
                        throw runtime.error(node, "条目值未恰好消费声明长度");
                    }
                    final Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("key", key);
                    if (value != DynamicFrame.MISSING) {
                        entry.put(variant == null ? "rawHex" : "value", DynamicRuntime.json(value));
                    }
                    result.add(Collections.unmodifiableMap(entry));
                } finally {
                    runtime.boundary = entryParent;
                }
            }
            if (count < 0) {
                exact(length, node);
            }
            return Collections.unmodifiableList(result);
        } finally {
            runtime.boundary = parent;
        }
    }

    /**
     * 按声明单位确定长度：前缀直接从输入读取，剩余量取当前边界内的可读部分。
     * 固定值、字段引用和表达式由共享运行状态求值。
     *
     * @return 本次使用的长度或集合项数
     */
    private long units(Length length, Node node) {
        if (length.source() instanceof Prefix(int bits)) {
            reader.checkAligned(node.path());
            return reader.readLong(bits, false, false, runtime.boundary, node.path());
        }
        if (length.source() instanceof Remaining) {
            return (runtime.boundary - reader.position()) / (length.unit() == DynamicLengthUnit.BYTE ? 8 : 1);
        }
        return runtime.units(length, node);
    }

    /**
     * 将声明长度换算为 bit 并确定容器终点，同时检查其未越过父边界。
     * 未声明长度时沿用当前边界。
     *
     * @return 相对本次解码起点的容器终止位位置
     */
    private long boundary(@Nullable Length length, Node node) {
        if (length == null) {
            return runtime.boundary;
        }
        final long bits = DynamicRuntime.bits(length, units(length, node));
        reader.checkBoundary(bits, runtime.boundary, node.path());
        return reader.position() + bits;
    }

    /**
     * 检查容器是否恰好消费声明长度；剩余量模式不要求恰好到达边界。
     */
    private void exact(@Nullable Length length, Node node) {
        if (length != null && !(length.source() instanceof Remaining) && reader.position() != runtime.boundary) {
            throw runtime.error(node, "容器未恰好消费声明的长度");
        }
    }
}
