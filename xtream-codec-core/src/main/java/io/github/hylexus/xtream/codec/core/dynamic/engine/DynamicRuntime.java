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

import io.github.hylexus.xtream.codec.core.tracker.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicChecksumAlgorithm;
import org.jspecify.annotations.Nullable;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.CRC32C;
import java.util.zip.Checksum;

import static io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicLayout.*;

/**
 * 执行器共享的边界、作用域及可选跟踪状态，不定义编码或解码主流程。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
final class DynamicRuntime {
    final DynamicLayout layout;
    final DynamicBitCursor cursor;
    final DynamicLimits limits;
    final ByteBuf buffer;
    final @Nullable CodecTracker tracker;
    @Nullable CodecTraceNode parent;
    DynamicFrame frame;
    long boundary;
    int items;

    DynamicRuntime(DynamicLayout layout, DynamicBitCursor cursor, ByteBuf buffer, DynamicLimits limits,
                   @Nullable CodecTracker tracker) {
        this.layout = layout;
        this.cursor = cursor;
        this.buffer = buffer;
        this.limits = limits;
        this.tracker = tracker;
        boundary = cursor.limit();
        frame = new DynamicFrame(0, layout.scopeSizes()[0], null, tracker != null);
        if (tracker != null) {
            parent = tracker.getTrace().getRoot();
            parent.getChildren().clear();
            parent.getDiagnostics().clear();
            tracker.getTrace().getDiagnostics().clear();
            tracker.getTrace().setEntityClass(null);
        }
    }

    DynamicFrame enter(Node node) {
        final DynamicFrame previous = frame;
        if (frame.scope != node.address().scope()) {
            frame = new DynamicFrame(node.address().scope(), layout.scopeSizes()[node.address().scope()], frame, tracker != null);
        }
        return previous;
    }

    boolean present(Node node, int depth) {
        if (depth > limits.maxDepth()) {
            throw error(node, "嵌套深度超过资源上限");
        }
        return DynamicExpressionValues.bool(node.condition().evaluate(frame, cursor.position()), node.path(), cursor.position());
    }

    void requireItems(long count, Node node) {
        if (count > (long) limits.maxItems() - items) {
            throw error(node, "集合项数超过资源上限");
        }
    }

    void item(Node node) {
        if (++items > limits.maxItems()) {
            throw error(node, "集合项数超过资源上限");
        }
    }

    DynamicCodecException error(Node node, String message) {
        return new DynamicCodecException(node.path(), cursor.position(), message);
    }

    DynamicCodecException wrap(Node node, RuntimeException exception) {
        return exception instanceof DynamicCodecException error && error.bitOffset() >= 0 ? error
                : new DynamicCodecException(node.path(), cursor.position(), String.valueOf(exception.getMessage()), exception);
    }

    @Nullable CodecTraceNode begin(Node node, long start, CodecTraceNodeKind kind, String name) {
        if (tracker == null) {
            return null;
        }
        final var result = new CodecTraceNode(kind, name, parent)
                .setBitRange(start, null).setByteRange((int) (start / 8), null)
                .putAttribute("displayName", node.name()).putAttribute("bitOrder", lsb(node.operation()) ? "lsb0" : "msb0");
        Objects.requireNonNull(parent).addChild(result);
        parent = result;
        return result;
    }

    void end(Node node, @Nullable CodecTraceNode trace, long start, @Nullable Object value, boolean success) {
        if (trace == null) {
            return;
        }
        long from = start;
        long to = cursor.position();
        if (node.operation() instanceof DerivedOp derived && derived.view() != null) {
            final BitView view = derived.view();
            final long sourceStart = frame.start(view.source());
            final long sourceEnd = frame.end(view.source());
            if (view.little()) {
                // 小端低字节在前：lsb0 或覆盖完整字节时可直接加数值偏移；单字节内的 msb0 则反向定位。
                // offset / 8 * 8 定位字节，8 - offset % 8 - width 定位该字节内的物理段起点。
                from = view.lsb() || (view.width() % 8 == 0 && view.offset() % 8 == 0)
                        ? sourceStart + view.offset()
                        : sourceStart + (view.offset() / 8L) * 8 + 8 - view.offset() % 8 - view.width();
            } else {
                // lsb0 从源范围起点累加低位偏移；msb0 从源范围终点扣除低位偏移和段宽度。
                from = view.lsb() ? sourceStart + view.offset() : sourceEnd - view.offset() - view.width();
            }
            to = from + view.width();
        }
        trace.setBitRange(from, to).setByteRange((int) (from / 8), (int) ((to + 7) / 8))
                .setStatus(success ? CodecTraceStatus.SUCCESS : CodecTraceStatus.ERROR);
        if (success) {
            trace.setValue(json(value));
        }
        frame.recordTrace(node.address(), start, cursor.position(), trace);
    }

    void failure(@Nullable CodecTraceNode trace, DynamicCodecException error) {
        if (tracker == null || tracker.getTrace().getDiagnostics().stream().anyMatch(diagnostic -> "ERROR".equals(diagnostic.level()))) {
            return;
        }
        final var target = trace == null ? tracker.getTrace().getRoot() : trace;
        target.setStatus(CodecTraceStatus.ERROR);
        final var diagnostic = new CodecTraceDiagnostic("ERROR", Objects.requireNonNull(error.getMessage()), target.getId(),
                (int) (error.bitOffset() / 8), error.getClass().getName(), error.bitOffset());
        target.addDiagnostic(diagnostic);
        tracker.getTrace().addDiagnostic(diagnostic);
    }

    void finish(boolean success, int payloadSize) {
        if (tracker != null) {
            tracker.finishTrace(ByteBufUtil.hexDump(buffer, cursor.start, payloadSize), cursor.start + cursor.consumedBytes());
            tracker.getTrace().getRoot().setBitRange(0L, cursor.position())
                    .setStatus(success ? CodecTraceStatus.SUCCESS : CodecTraceStatus.ERROR);
        }
    }

    @Nullable Object derived(DerivedOp operation, Node node) {
        if (operation.expression() != null) {
            return DynamicExpressionValues.json(operation.expression().evaluate(frame, cursor.position()));
        }
        final BitView view = Objects.requireNonNull(operation.view());
        // offset 从源数值的最低位起算；先右移到低位，再用 (2^width - 1) 只保留 width 个位。
        // 例如 0b11010110 的 offset=2、width=3 得到 0b101；掩码也会去掉负数右移补出的符号位。
        // 不能改为 (1 << width) - 1：Java 的 int 移位距离只取低 5 位，width=32 时会按移 0 位计算。
        // 1L << width 也只取低 6 位，width=64 时同样会按移 0 位计算；BigInteger 可正确生成 64 位掩码。
        return DynamicNumbers
                .requireInteger(frame.require(view.source(), node.path(), cursor.position()), node.path(), cursor.position())
                .shiftRight(view.offset())
                .and(BigInteger.ONE.shiftLeft(view.width()).subtract(BigInteger.ONE));
    }

    Node choose(ChoiceOp operation, Node node) {
        final BigInteger key = DynamicNumbers.requireInteger(frame.require(operation.discriminator(), node.path(), cursor.position()),
                node.path(), cursor.position());
        final Node result = operation.variants().getOrDefault(key.toString(), operation.defaultVariant());
        if (result == null) {
            throw error(node, "没有匹配的判别分支: " + key);
        }
        return result;
    }

    /**
     * 对最终线上字节求值，偏移相对本次读写起点。CRC 使用零拷贝的 NIO 分段视图。
     */
    long checksum(Backfill action) {
        final long start = frame.regionStart(action.target());
        final long end = cursor.position();
        if (start % 8 != 0 || end % 8 != 0) {
            throw new DynamicCodecException(action.path(), end, "校验和区间必须完整字节对齐");
        }
        final int index = Math.addExact(cursor.start, Math.toIntExact(start / 8));
        final int size = Math.toIntExact((end - start) / 8);
        final DynamicChecksumAlgorithm algorithm = Objects.requireNonNull(action.checksum());
        return switch (algorithm) {
            case XOR8 -> {
                int result = 0;
                for (int offset = index; offset < index + size; offset++) {
                    result ^= buffer.getUnsignedByte(offset);
                }
                yield result;
            }
            case SUM8 -> {
                int result = 0;
                for (int offset = index; offset < index + size; offset++) {
                    result += buffer.getUnsignedByte(offset);
                }
                // 只保留低八位，使字节累加结果按模 256 取值。
                yield result & 0xff;
            }
            case CRC32, CRC32C -> {
                final Checksum checksum = algorithm == DynamicChecksumAlgorithm.CRC32 ? new CRC32() : new CRC32C();
                for (final ByteBuffer bytes : buffer.nioBuffers(index, size)) {
                    checksum.update(bytes);
                }
                yield checksum.getValue();
            }
        };
    }

    long units(Length length, Node node) {
        final Object value = switch (length.source()) {
            case Fixed fixed -> fixed.units();
            case Referenced reference -> frame.require(reference.target(), node.path(), cursor.position());
            case Evaluated expression -> expression.expression().evaluate(frame, cursor.position());
            default -> throw new IllegalStateException("当前长度需要方向专属处理");
        };
        try {
            final long units = DynamicNumbers.requireInteger(value, node.path(), cursor.position()).longValueExact();
            if (units < 0) {
                throw error(node, "长度不能为负数");
            }
            return units;
        } catch (IllegalArgumentException | ArithmeticException error) {
            if (length.source() instanceof Evaluated) {
                throw new DynamicCodecException(node.path(), cursor.position(), "length.expression: " + error.getMessage(), error);
            }
            throw error;
        }
    }

    static long bits(Length length, long units) {
        return length.unit() == io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLengthUnit.BYTE
                ? Math.multiplyExact(units, 8)
                : units;
    }

    static @Nullable Object json(@Nullable Object value) {
        if (value instanceof BigInteger number) {
            return DynamicNumbers.toJsonValue(number);
        }
        if (value instanceof Long number && (number > 9007199254740991L || number < -9007199254740991L)) {
            return number.toString();
        }
        return value;
    }

    static boolean lsb(Operation operation) {
        return switch (operation) {
            case IntegerOp number -> number.lsb();
            case FloatOp number -> number.lsb();
            case BitsOp bits -> bits.lsb();
            case AlignOp align -> align.lsb();
            default -> false;
        };
    }

    static Object integer(long raw, IntegerOp operation) {
        if (operation.signed()) {
            // 符号扩展：左移丢弃字段位宽外的高位，并将字段符号位移到 long 的第 63 位。
            // 随后的 >> 是算术右移，负数在高位补 1；不能替换为始终补 0 的 >>>。
            // 例如 8 位的 0x80：左移 56 位得到 0x8000000000000000，再右移得到 -128。
            // 64 位输入已经是完整的 long 位模式，无需再扩展。
            return operation.bits() == 64
                    ? raw
                    : (raw << (64 - operation.bits())) >> (64 - operation.bits());
        }
        // 无符号 64 位的最高位为 1 时，raw 表现为负 long；先清除第 63 位得到非负低 63 位，
        // 再在 BigInteger 中恢复该位，得到 2^63..2^64-1 范围内的精确正整数。
        return operation.bits() == 64 && raw < 0
                ? BigInteger.valueOf(raw & Long.MAX_VALUE).setBit(63)
                : raw;
    }
}
