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
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

import java.math.BigInteger;
import java.util.*;

import static io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicLayout.*;

/**
 * 写入编译布局；只在后续依赖或跟踪需要时保留规范化值。
 * 每次编码创建独立的写入器和运行状态，从调用方缓冲区当前 writerIndex 开始写入。
 * 容器需要保留值时才收集其子节点结果；字节字段需要规范化时以实际写出的字节计算值。
 * 数组元素和分支统一经过节点入口，处理条件、作用域、错误定位及可选跟踪。
 * 编码失败可能保留部分输出，缓冲区的丢弃和释放由调用方负责。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
final class DynamicWriteExecution {
    // 本次编码的位游标，负责边界检查、字节写入和预留位段的原地回填。
    private final DynamicBitWriter writer;
    // 本次编码的活动帧、容器边界、集合计数和可选跟踪状态。
    private final DynamicRuntime runtime;

    /**
     * 为一次编码绑定可复用布局、输出缓冲区和资源限制。
     *
     * @param layout  编译后的执行布局
     * @param output  调用方持有的输出缓冲区
     * @param limits  嵌套深度、集合项数和输出大小限制
     * @param tracker 可选的跟踪器，为空时不创建跟踪节点
     */
    DynamicWriteExecution(DynamicLayout layout, ByteBuf output, DynamicLimits limits, @Nullable CodecTracker tracker) {
        writer = new DynamicBitWriter(output, limits);
        runtime = new DynamicRuntime(layout, writer, output, limits, tracker);
    }

    /**
     * 按根节点顺序写入输入值，成功结束时要求最终位置字节对齐。
     * 根调用不收集完整结果树；开启跟踪时，成功或失败都会结算本次已写范围。
     *
     * @param input 根字段标识到业务值的映射
     * @return 相对本次写入起点的 bit 数
     */
    long encode(Map<String, Object> input) {
        if (runtime.tracker != null) {
            runtime.tracker.beginEncode(writer.start, null);
        }
        boolean success = false;
        try {
            fields(runtime.layout.nodes(), input, 0, false);
            writer.checkAligned("$");
            success = true;
            return writer.position();
        } catch (DynamicCodecException error) {
            runtime.failure(null, error);
            throw error;
        } finally {
            runtime.finish(success, writer.consumedBytes());
        }
    }

    /**
     * 执行同一结构中的字段序列，在各字段入口前后执行回填区间的起止动作。
     * 动作围绕条件判断执行，因此区间端点被跳过时仍按实际写入量结算。
     * 收集结果时，序列结束后用帧中的最终长度替换先前收集的回填占位值。
     *
     * @param nodes   按报文顺序排列的字段节点
     * @param input   当前结构的业务值，也供缺省源整数读取同级派生位段
     * @param depth   当前嵌套深度
     * @param collect 是否收集当前结构的规范化值对象
     * @return 收集到的字段值；不需要收集时为空
     */
    private @Nullable Map<String, Object> fields(List<Node> nodes, Map<String, Object> input, int depth, boolean collect) {
        final Map<String, Object> result = collect ? new LinkedHashMap<>() : null;
        for (final Node node : nodes) {
            for (final Backfill action : node.starts()) {
                if (action.checksum() != null || runtime.frame.get(action.target()) == DynamicFrame.PENDING) {
                    runtime.frame.beginRegion(action.target(), writer.position());
                }
            }
            final Object value = write(node, input.get(node.id()), depth, CodecTraceNodeKind.FIELD, node.id(), collect, input);
            if (result != null && value != DynamicFrame.MISSING) {
                result.put(node.id(), DynamicRuntime.json(value));
            }
            for (final Backfill action : node.ends()) {
                finish(action, node);
            }
        }
        if (result != null) {
            for (final Node node : nodes) {
                if (node.operation() instanceof IntegerOp number && number.backfill() != null
                        && runtime.frame.get(node.address()) != DynamicFrame.MISSING) {
                    result.put(node.id(), DynamicRuntime.json(runtime.frame.get(node.address())));
                }
            }
        }
        return result;
    }

    /**
     * 源整数输入缺省时，将已提供的同级派生位段组合成源值。
     * 每个位段必须为其位宽内的非负整数，重叠位必须一致，未提供的位保持为零。
     *
     * @return 合成的整数；没有提供任何派生位段时为空
     */
    private @Nullable Object reverse(IntegerOp number, Map<String, Object> input, Node node) {
        BigInteger result = BigInteger.ZERO;
        BigInteger used = BigInteger.ZERO;
        boolean found = false;
        for (final BitView view : number.reverse()) {
            final Object supplied = input.get(view.id());
            if (supplied == null) {
                continue;
            }
            final BigInteger part = DynamicNumbers.requireInteger(supplied, view.path(), writer.position());
            if (part.signum() < 0 || part.bitLength() > view.width()) {
                throw runtime.error(node, "派生位段值超出位宽");
            }
            // (2^width - 1) 是 width 个低位 1，再左移 offset 将掩码定位到源整数中的位段。
            final BigInteger mask = BigInteger.ONE.shiftLeft(view.width()).subtract(BigInteger.ONE).shiftLeft(view.offset());
            // 派生值原本从低位起算，左移后与源整数的对应位段对齐；例如 0b101 左移 2 位为 0b10100。
            final BigInteger shifted = part.shiftLeft(view.offset());
            // used & mask 只留下此前已赋值的重叠位；两侧分别保留这些位，检查新旧输入是否一致。
            if (!result.and(used.and(mask)).equals(shifted.and(used.and(mask)))) {
                throw runtime.error(node, "派生位段输入相互冲突");
            }
            // OR 合并位段值；used 记录整段已被赋值，包括值为 0 的位，以便后续检查重叠冲突。
            result = result.or(shifted);
            used = used.or(mask);
            found = true;
        }
        return found ? result : null;
    }

    /**
     * 数组元素和分支的节点入口，输入中没有用于反向合成源整数的同级字段对象。
     */
    private @Nullable Object write(
            Node node, @Nullable Object supplied, int depth,
            CodecTraceNodeKind kind, String name, boolean collect) {
        return write(node, supplied, depth, kind, name, collect, null);
    }

    /**
     * 节点统一入口：进入作用域、检查条件、执行内容，并在结束时恢复外层帧和跟踪父节点。
     * 回填整数字段写入后标为待回填；其他字段仅在需要时把规范化值保存到帧中。
     *
     * @param node     当前执行节点
     * @param supplied 调用方提供的值，缺省时为空
     * @param depth    当前嵌套深度，用于资源限制检查
     * @param kind     跟踪节点类型
     * @param name     跟踪名称，例如字段标识、元素下标或条目键
     * @param collect  调用方是否需要当前节点的规范化结果
     * @param siblings 可选的同级输入字段，供缺省源整数反向合成
     * @return 节点结果；条件跳过时返回缺失标记，无业务值或无需规范化时可为空
     */
    private @Nullable Object write(
            Node node, @Nullable Object supplied, int depth,
            CodecTraceNodeKind kind, String name, boolean collect, @Nullable Map<String, Object> siblings) {
        final DynamicFrame previousFrame = runtime.enter(node);
        final CodecTraceNode previousParent = runtime.parent;
        final long start = writer.position();
        CodecTraceNode trace = null;
        Object value = DynamicFrame.MISSING;
        boolean success = false;
        try {
            if (!runtime.present(node, depth)) {
                runtime.frame.put(node.address(), DynamicFrame.MISSING);
                return DynamicFrame.MISSING;
            }
            trace = runtime.begin(node, start, kind, name);
            if (node.generated() instanceof GeneratedExpression generated) {
                supplied = generated.expression().evaluate(runtime.frame, start);
            } else if (node.generated() instanceof GeneratedCount generated) {
                if (siblings == null || !(siblings.get(generated.id()) instanceof List<?> items)) {
                    throw runtime.error(node, "countOf 目标必须提供集合值: " + generated.id());
                }
                supplied = items.size();
            }
            if (supplied == null && siblings != null
                    && node.operation() instanceof IntegerOp number && number.backfill() == null) {
                supplied = reverse(number, siblings, node);
            }
            // 收集容器结果、后续字段引用和跟踪展示都需要实际写出值的规范化结果。
            final boolean needed = collect || node.retained() || trace != null;
            value = content(node, supplied, depth, needed);
            if (node.operation() instanceof IntegerOp number && number.backfill() != null && number.backfill().deferred()) {
                runtime.frame.reserve(node.address(), start);
            } else if (needed || value == DynamicFrame.MISSING) {
                runtime.frame.put(node.address(), value);
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
     * 根据互斥操作写入节点内容，条件和作用域已由外层入口处理。
     * choice 选中的分支仍经过节点入口，派生值只求值并检查位段输入，不占用报文空间。
     *
     * @param needed 是否需要生成规范化值，传递给字节字段和容器以控制额外转换及收集
     */
    private @Nullable Object content(Node node, @Nullable Object value, int depth, boolean needed) {
        return switch (node.operation()) {
            case IntegerOp number -> integer(node, number, value);
            case FloatOp number -> {
                writer.checkAligned(node.path());
                if (!(value instanceof Number input) || !Double.isFinite(input.doubleValue())
                        || (number.bits() == 32 && !Float.isFinite(input.floatValue()))) {
                    throw runtime.error(node, "浮点数必须是有限数值");
                }
                final long raw = number.bits() == 32
                        ? Integer.toUnsignedLong(Float.floatToRawIntBits(input.floatValue()))
                        : Double.doubleToRawLongBits(input.doubleValue());
                writer.writeLong(raw, number.bits(), number.little(), number.lsb(), runtime.boundary, node.path());
                yield number.bits() == 32
                        ? input.floatValue()
                        : input.doubleValue();
            }
            case BytesOp bytes -> bytes(node, bytes, value, needed);
            case BitsOp bits -> {
                if (!(value instanceof String text) || text.length() != bits.bits()) {
                    throw runtime.error(node, "bits 值必须为指定长度的 0/1 字符串");
                }
                writer.checkBoundary(bits.bits(), runtime.boundary, node.path());
                for (int index = 0; index < text.length(); index++) {
                    final char bit = text.charAt(index);
                    if (bit != '0' && bit != '1') {
                        throw runtime.error(node, "bits 包含非 0/1 字符");
                    }
                    writer.writeLong(bit - '0', 1, false, bits.lsb(), runtime.boundary, node.path());
                }
                yield text;
            }
            case AlignOp align -> {
                final int count = (int) ((align.alignment() - writer.position() % align.alignment()) % align.alignment());
                for (int index = 0; index < count; index++) {
                    writer.writeLong(align.fill(), 1, false, align.lsb(), runtime.boundary, node.path());
                }
                yield null;
            }
            case StructOp structure -> structure(node, structure, value, depth, needed);
            case ArrayOp array -> array(node, array, value, depth, needed);
            case EntriesOp entries -> entries(node, entries, value, depth, needed);
            case ChoiceOp choice -> {
                final Node selected = runtime.choose(choice, node);
                yield write(selected, value, depth + 1, CodecTraceNodeKind.FIELD, selected.id(), needed);
            }
            case DerivedOp derived -> {
                final Object result = runtime.derived(derived, node);
                if (derived.view() != null
                        && value != null
                        && !DynamicNumbers.requireInteger(result, node.path(), writer.position()).equals(DynamicNumbers.requireInteger(value, node.path(), writer.position()))
                ) {
                    throw runtime.error(node, "派生位段与源整数不一致");
                }
                yield result;
            }
        };
    }

    /**
     * 检查有符号或无符号范围后，以 long 位模式写入整数。
     * 常用整型直接检查范围，其他表示先转换为精确整数；回填字段先写入零占位。
     * 返回按字段符号和位宽解释的值，u64 高位通过大整数保留精度。
     */
    private Object integer(Node node, IntegerOp number, @Nullable Object value) {
        if (number.bits() % 8 == 0) {
            writer.checkAligned(node.path());
        }
        final long raw;
        if (number.backfill() != null && number.backfill().deferred()) {
            raw = 0;
        } else if (number.backfill() != null) {
            final Long checksum = runtime.frame.checksum(node.address());
            if (checksum == null) {
                throw runtime.error(node, "校验和区间尚未完成");
            }
            raw = checksum;
        } else if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            raw = ((Number) value).longValue();
            // n 位有符号范围为 [-2^(n-1), 2^(n-1))，无符号范围为 [0, 2^n)。
            // 有符号 64 位涵盖全部 long；无符号 n>=63 时所有非负 long 都合法，避免 1L << 63 变成负数。
            final boolean valid = number.signed()
                    ? number.bits() == 64 || (raw >= -(1L << (number.bits() - 1)) && raw < (1L << (number.bits() - 1)))
                    : raw >= 0 && (number.bits() >= 63 || raw < (1L << number.bits()));
            if (!valid) {
                throw runtime.error(node, "整数超出 " + number.bits() + " bit 范围");
            }
        } else {
            final BigInteger input = DynamicNumbers.requireInteger(value, node.path(), writer.position());
            final int magnitude = number.signed() ? number.bits() - 1 : number.bits();
            // bitLength 不计符号扩展位；负数按其补码所需位数计算，例如 -128 的 bitLength 为 7，可容纳于 i8。
            if (input.bitLength() > magnitude || (!number.signed() && input.signum() < 0)) {
                throw runtime.error(node, "整数超出 " + number.bits() + " bit 范围");
            }
            raw = input.longValue();
        }
        writer.writeLong(raw, number.bits(), number.little(), number.lsb(), runtime.boundary, node.path());
        return DynamicRuntime.integer(raw, number);
    }

    /**
     * 将业务值转换为字节，确定内容长度、补齐填充后写出。
     * 内置转换在分配前使用声明边界、长度前缀容量和输出上限约束大小；扩展返回字节后再检查边界。
     * 仅在需要规范化值时，对已填充的字节调用对应解码入口，以便后续引用观察实际写出的值。
     */
    private @Nullable Object bytes(Node node, BytesOp operation, @Nullable Object value, boolean needed) {
        writer.checkAligned(node.path());
        final Length length = operation.length();
        long maximum = (Math.min(runtime.boundary, writer.limit()) - writer.position()) / 8;
        if (length.source() instanceof Prefix(int bits)) {
            // 无符号前缀最多表示 2^bits - 1；先扣除前缀字节，再将 bit 单位容量向下换算为完整字节。
            maximum = Math.min(
                    maximum - bits / 8,
                    ((1L << bits) - 1) / (length.unit() == DynamicLengthUnit.BIT ? 8 : 1)
            );
        } else if (!(length.source() instanceof Remaining) && !deferred(length)) {
            final long bits = DynamicRuntime.bits(length, runtime.units(length, node));
            if (bits % 8 != 0) {
                throw runtime.error(node, "字节字段长度必须为完整字节");
            }
            maximum = Math.min(maximum, bits / 8);
        }
        if (maximum < 0) {
            throw runtime.error(node, "输出超过边界或资源上限");
        }
        final ByteFormat format = operation.format();
        final byte[] input;
        if (format.extension() == null) {
            input = DynamicByteFormat.encode(format, value, (int) Math.min(Integer.MAX_VALUE, maximum));
        } else {
            if (value == null) {
                throw runtime.error(node, "缺少必需字段值");
            }
            input = format.extension().encode(value, format.config());
        }
        final long natural = length.unit() == DynamicLengthUnit.BIT ? input.length * 8L : input.length;
        final long units = units(length, node, natural);
        final long bits = DynamicRuntime.bits(length, units);
        writer.checkBoundary(bits, runtime.boundary, node.path());
        if (bits % 8 != 0 || bits / 8 > Integer.MAX_VALUE) {
            throw runtime.error(node, "字节字段长度必须为完整字节");
        }
        final byte[] bytes = DynamicByteFormat.pad(format, input, (int) (bits / 8));
        writer.writeBytes(bytes, runtime.boundary, node.path());
        return !needed ? null
                : ((format.extension() == null) ? DynamicByteFormat.decode(format, bytes) : format.extension().decode(bytes, format.config()));
    }

    /**
     * 在结构边界内顺序写入子字段，必要时收集结构值并结算长度前缀。
     * 普通结构沿用当前作用域，退出时恢复父容器的边界。
     */
    private @Nullable Object structure(Node node, StructOp structure, @Nullable Object value, int depth, boolean needed) {
        if (!(value instanceof Map<?, ?> input)) {
            throw runtime.error(node, "struct 值必须为对象");
        }
        final long parent = runtime.boundary;
        final long prefix = prefix(structure.length(), node);
        runtime.boundary = boundary(structure.length(), node, prefix);
        final long start = writer.position();
        try {
            @SuppressWarnings("unchecked") final var supplied = (Map<String, Object>) input;
            final var result = fields(structure.fields(), supplied, depth + 1, needed);
            complete(structure.length(), node, prefix, start);
            return result;
        } finally {
            runtime.boundary = parent;
        }
    }

    /**
     * 按输入列表重复写入元素，各元素进入自己的局部帧。
     * item 单位校验元素个数，bit 或 byte 单位校验整个内容边界；每个元素都必须推进游标。
     * 仅在需要数组规范化值时收集结果，允许无业务值元素返回空值。
     */
    private @Nullable Object array(Node node, ArrayOp array, @Nullable Object value, int depth, boolean needed) {
        if (!(value instanceof List<?> items)) {
            throw runtime.error(node, "array 值必须为数组");
        }
        final Length length = array.length();
        runtime.requireItems(items.size(), node);
        final boolean counted = length.unit() == DynamicLengthUnit.ITEM;
        final long count = counted ? units(length, node, items.size()) : -1;
        runtime.requireItems(count, node);
        final long parent = runtime.boundary;
        final long prefix = counted ? -1 : prefix(length, node);
        if (!counted) {
            runtime.boundary = boundary(length, node, prefix);
        }
        final long start = writer.position();
        final List<Object> result = needed ? new ArrayList<>(items.size()) : null;
        try {
            for (int index = 0; index < items.size(); index++) {
                runtime.item(node);
                final long before = writer.position();
                final Object item = write(array.item(), items.get(index), depth + 1,
                        CodecTraceNodeKind.COLLECTION_ITEM, "[" + index + "]", needed);
                if (writer.position() == before) {
                    throw runtime.error(node, "集合元素未推进游标");
                }
                if (result != null) {
                    result.add(DynamicRuntime.json(item));
                }
            }
            if (counted && count != items.size()) {
                throw runtime.error(node, "集合长度与边界不匹配");
            }
            if (!counted) {
                complete(length, node, prefix, start);
            }
            return result == null ? null : Collections.unmodifiableList(result);
        } finally {
            runtime.boundary = parent;
        }
    }

    /**
     * 按输入顺序写入 TLV、map 或 pair 条目，列表保留重复键及顺序。
     * 已知键写入匹配分支，未知键要求提供 rawHex；pair 无条目长度，不能写入未知键。
     * 条目字节长度在内容写完后原地回填，条目集合的计数或整体边界由外层长度声明校验。
     */
    private @Nullable Object entries(Node node, EntriesOp entries, @Nullable Object value, int depth, boolean needed) {
        if (!(value instanceof List<?> items)) {
            throw runtime.error(node, "条目值必须为有序数组");
        }
        final Length length = entries.length();
        runtime.requireItems(items.size(), node);
        final boolean counted = length.unit() == DynamicLengthUnit.ITEM;
        final long count = counted ? units(length, node, items.size()) : -1;
        runtime.requireItems(count, node);
        final long parent = runtime.boundary;
        final long prefix = counted ? -1 : prefix(length, node);
        if (!counted) {
            runtime.boundary = boundary(length, node, prefix);
        }
        final long start = writer.position();
        final List<Object> result = needed ? new ArrayList<>(items.size()) : null;
        try {
            for (final Object item : items) {
                runtime.item(node);
                if (!(item instanceof Map<?, ?> input)) {
                    throw runtime.error(node, "条目必须为对象");
                }
                final long key = DynamicNumbers.requireInteger(input.get("key"), node.path(), writer.position()).longValueExact();
                // 无符号 keyBits 位的键要求 0 <= key < 2^keyBits。
                if (key < 0 || key >= (1L << entries.keyBits())) {
                    throw runtime.error(node, "条目键超出位宽");
                }
                writer.writeLong(key, entries.keyBits(), false, false, runtime.boundary, node.path());
                final Node variant = entries.variants().get(Long.toString(key));
                if (entries.pair() && variant == null) {
                    throw runtime.error(node, "pair 的未知键没有可确定的值边界");
                }
                final long lengthAt = writer.position();
                if (!entries.pair()) {
                    writer.writeLong(0, entries.lengthBits(), false, false, runtime.boundary, node.path());
                }
                final long contentStart = writer.position();
                final Object content;
                if (variant == null) {
                    if (!(input.get("rawHex") instanceof String hex)) {
                        throw runtime.error(node, "未知条目需要 rawHex 字符串");
                    }
                    // 条目长度是无符号字节数，前缀可表示的最大值为 2^lengthBits - 1。
                    final long maximum = Math.min((runtime.boundary - writer.position()) / 8, (1L << entries.lengthBits()) - 1);
                    if (hex.length() / 2L > maximum) {
                        throw runtime.error(node, "条目原始值超过边界或资源上限");
                    }
                    writer.writeBytes(HexFormat.of().parseHex(hex), runtime.boundary, node.path());
                    content = needed ? hex.toLowerCase(Locale.ROOT) : null;
                } else {
                    content = write(variant, input.get("value"), depth + 1, CodecTraceNodeKind.MAP_ENTRY, "[" + key + "]", needed);
                }
                if (!entries.pair()) {
                    final long bits = writer.position() - contentStart;
                    // 字节数必须小于 2^lengthBits，即能完整放入无符号长度前缀。
                    if (bits % 8 != 0 || bits / 8 >= (1L << entries.lengthBits())) {
                        throw runtime.error(node, "条目值长度超出完整字节或前缀范围");
                    }
                    writer.patchInteger(lengthAt, BigInteger.valueOf(bits / 8), entries.lengthBits(), false, false, node.path());
                }
                if (result != null) {
                    final Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("key", key);
                    if (content != DynamicFrame.MISSING) {
                        entry.put(variant == null ? "rawHex" : "value", DynamicRuntime.json(content));
                    }
                    result.add(Collections.unmodifiableMap(entry));
                }
            }
            if (counted && count != items.size()) {
                throw runtime.error(node, "条目数与声明不符");
            }
            if (!counted) {
                complete(length, node, prefix, start);
            }
            return result == null ? null : Collections.unmodifiableList(result);
        } finally {
            runtime.boundary = parent;
        }
    }

    /**
     * 只有长度引用被标记为可延迟且目标仍待回填时，才允许按当前内容确定长度。
     */
    private boolean deferred(Length length) {
        return length.source() instanceof Referenced(Address target, boolean deferred)
                && deferred
                && runtime.frame.get(target) == DynamicFrame.PENDING;
    }

    /**
     * 为字节字段或按项计数的集合确定长度，已知自然长度时可直接写入前缀。
     * 剩余量及待回填引用采用自然长度，其他来源通过已编译配置求值。
     *
     * @param natural 按声明单位计量的内容长度或元素个数
     * @return 本次使用的长度或个数
     */
    private long units(Length length, Node node, long natural) {
        if (length.source() instanceof Prefix(int bits)) {
            writer.checkAligned(node.path());
            // n 位无符号前缀可表示 0..2^n-1，因此用 2^n 作为不包含的上界。
            if (natural < 0 || natural >= (1L << bits)) {
                throw runtime.error(node, "长度超出前缀范围");
            }
            writer.writeLong(natural, bits, false, false, runtime.boundary, node.path());
            return natural;
        }
        return length.source() instanceof Remaining || deferred(length) ? natural : runtime.units(length, node);
    }

    /**
     * 为按 bit 或 byte 限定的容器预留长度前缀，内容写完后再结算实际长度。
     *
     * @return 前缀相对本次编码起点的 bit 位置，没有前缀时返回 -1
     */
    private long prefix(@Nullable Length length, Node node) {
        if (length == null || !(length.source() instanceof Prefix(int bits))) {
            return -1;
        }
        writer.checkAligned(node.path());
        final long at = writer.position();
        writer.writeLong(0, bits, false, false, runtime.boundary, node.path());
        return at;
    }

    /**
     * 计算容器内容的终止位置，固定值、已就绪引用和表达式可以在写入前确定边界。
     * 未声明长度、预留前缀、剩余量或待回填引用先沿用父边界，长度确定后再结算。
     */
    private long boundary(@Nullable Length length, Node node, long prefix) {
        if (length == null || prefix >= 0 || length.source() instanceof Remaining || deferred(length)) {
            return runtime.boundary;
        }
        final long bits = DynamicRuntime.bits(length, runtime.units(length, node));
        writer.checkBoundary(bits, runtime.boundary, node.path());
        return writer.position() + bits;
    }

    /**
     * 容器内容结束后回填预留前缀，或检查是否恰好写满预先确定的边界。
     * 前缀长度只计内容，不含前缀本身；byte 单位同时要求内容长度为完整字节。
     * 剩余量和待回填引用由实际内容决定，无需写满当前父边界。
     *
     * @param prefix 预留前缀的 bit 位置，没有前缀时为 -1
     * @param start  容器内容起点，位于可选前缀之后
     */
    private void complete(@Nullable Length length, Node node, long prefix, long start) {
        if (length == null) {
            return;
        }
        if (prefix >= 0) {
            final long bits = writer.position() - start;
            final long units = length.unit() == DynamicLengthUnit.BYTE ? bits / 8 : bits;
            final int width = ((Prefix) length.source()).bits();
            // 1L << width 是前缀容量的开区间上界 2^width；长度前缀位宽已限制为 8、16 或 32。
            if ((length.unit() == DynamicLengthUnit.BYTE && bits % 8 != 0) || units >= (1L << width)) {
                throw runtime.error(node, "容器长度超出前缀范围");
            }
            writer.patchInteger(prefix, BigInteger.valueOf(units), width, false, false, node.path());
        } else if (!(length.source() instanceof Remaining) && !deferred(length) && writer.position() != runtime.boundary) {
            throw runtime.error(node, "容器未恰好消费声明的长度");
        }
    }

    /**
     * 在 computedLengthOf 区间终点执行回填动作，以实际写入量覆盖长度字段的占位位段。
     * 目标被条件跳过时不执行；成功后更新帧值及对应跟踪节点，后续引用即可读取最终长度。
     * 写入器在原地回填后恢复当前游标和位序，不影响后续字段的写入位置。
     */
    private void finish(Backfill action, Node node) {
        if (action.checksum() == null && runtime.frame.get(action.target()) != DynamicFrame.PENDING) {
            return;
        }
        if (action.checksum() != null && !action.deferred()) {
            runtime.frame.checksum(action.target(), runtime.checksum(action));
            return;
        }
        if (runtime.frame.get(action.target()) != DynamicFrame.PENDING) {
            return;
        }
        final long bits = writer.position() - runtime.frame.regionStart(action.target());
        final long units = action.checksum() != null ? runtime.checksum(action)
                : action.unit() == DynamicLengthUnit.BYTE ? bits / 8 : bits;
        // 小于 63 位时检查 units < 2^n；非负 long 已能放入 u63/u64，无需计算无法表示正上界的 2^63/2^64。
        // Java 对 long 的移位距离只取低 6 位，1L << 63 为负数，1L << 64 会绕回为 1。
        if (((action.unit() == DynamicLengthUnit.BYTE) && ((bits % 8) != 0))
                || ((action.bits() < 63) && (units >= (1L << action.bits())))) {
            throw runtime.error(node, "回填长度超出整数位宽或完整字节");
        }
        writer.patchInteger(
                runtime.frame.position(action.target()),
                BigInteger.valueOf(units),
                action.bits(), action.little(), action.lsb(), node.path()
        );
        runtime.frame.put(action.target(), units);
        final CodecTraceNode trace = runtime.frame.trace(action.target());
        if (trace != null) {
            trace.setValue(units);
        }
    }
}
