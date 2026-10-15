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

import io.github.hylexus.xtream.codec.base.expression.XtreamEvaluationContext;
import io.github.hylexus.xtream.codec.base.expression.XtreamExpression;
import io.github.hylexus.xtream.codec.base.expression.XtreamExpressionEngine;
import io.github.hylexus.xtream.codec.core.dynamic.schema.*;
import org.jspecify.annotations.Nullable;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicLayout.*;
import static java.util.Objects.requireNonNull;

/**
 * 将经过校验的字段树降为独立布局，解析地址、常量、回填动作及表达式。
 * {@link DynamicCompiler} 已完成版本筛选及跨字段引用校验；此类按顺序分配作用域与槽位、
 * 标记执行时需要保留的值、布置回填区间，再生成供编码和解码共用的 {@link DynamicLayout}。
 * 路径映射及编译期字段树只在构建布局时使用，单次执行的值和位置由 {@link DynamicFrame} 保存。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
final class DynamicLayoutCompiler {
    // 字段路径只在编译期间用来定位已分配的地址和经过校验的中间节点。
    private final Map<String, Address> addresses = new HashMap<>();
    private final Map<String, DynamicCompiler.Field> fields = new LinkedHashMap<>();
    // 后续条件、表达式、引用或回填仍需读取的字段，编码时保留其规范化值。
    private final Set<String> retained = new HashSet<>();
    // 下标是作用域编号，值是该作用域需要的槽位数。
    private final List<Integer> sizes = new ArrayList<>();
    // 回填字段对应一个动作，并在区间两端的节点上挂接同一动作。
    private final Map<String, Backfill> backfills = new HashMap<>();
    private final Map<String, List<Backfill>> starts = new HashMap<>();
    private final Map<String, List<Backfill>> ends = new HashMap<>();
    private final DynamicCompileOptions options;

    /**
     * 为本次布局编译绑定版本、扩展及可选表达式引擎配置。
     */
    DynamicLayoutCompiler(DynamicCompileOptions options) {
        this.options = options;
    }

    /**
     * 按地址分配、依赖收集、回填校验和节点构建的顺序生成布局。
     * 根作用域编号为 0；每次执行使用独立帧，可复用同一布局。
     */
    DynamicLayout compile(List<DynamicCompiler.Field> roots) {
        this.sizes.add(0);

        for (DynamicCompiler.Field field : roots) {
            this.allocate(field, 0);
        }

        for (DynamicCompiler.Field field : this.fields.values()) {
            this.dependencies(field);
        }

        for (DynamicCompiler.Field field : this.fields.values()) {
            this.backfill(field);
        }

        this.orderBackfills();

        this.validatePending(roots, new HashSet<>());

        final List<Node> nodes = roots.stream().map(this::node).toList();
        return new DynamicLayout(nodes, this.sizes.stream().mapToInt(Integer::intValue).toArray());
    }

    /**
     * 拒绝条件和表达式提前读取尚未完成的回填值。
     * 数组元素及各分支使用独立的待回填集合；length.ref 可延迟确定边界，
     * 但不能将目标的待回填状态当作已解析整数读取。
     */
    private void validatePending(List<DynamicCompiler.Field> nodes, Set<String> pending) {
        for (final DynamicCompiler.Field field : nodes) {
            if (pending.contains(field.conditionPath())
                    || field.expressionReferences().values().stream().anyMatch(pending::contains)
                    || (pending.contains(field.referencePath()) && (field.definition().length() == null
                    || requireNonNull(fields.get(field.referencePath())).computedLength() == null))) {
                throw new DynamicCodecException(field.path(), -1, "引用的生成字段尚未完成回填");
            }
            if (backfills.containsKey(field.path()) && backfills.get(field.path()).deferred()) {
                pending.add(field.path());
            }

            this.validatePending(field.children(), pending);

            if (field.item() != null) {
                this.validatePending(List.of(field.item()), new HashSet<>(pending));
            }

            for (DynamicCompiler.Field variant : field.variants().values()) {
                this.validatePending(List.of(variant), new HashSet<>(pending));
            }
            if (field.defaultVariant() != null) {
                this.validatePending(List.of(field.defaultVariant()), new HashSet<>(pending));
            }

            for (final Backfill action : ends.getOrDefault(field.path(), List.of())) {
                pending.remove(action.path());
            }
        }
    }

    /**
     * 为字段分配稳定地址：普通结构子字段沿用作用域，数组元素与每个分支使用独立作用域。
     * 相同布局的重复元素在执行时分别创建帧，不在编译时重复分配槽位。
     */
    private void allocate(DynamicCompiler.Field field, int scope) {
        this.addresses.put(field.path(), new Address(scope, this.sizes.get(scope)));
        this.sizes.set(scope, this.sizes.get(scope) + 1);
        this.fields.put(field.path(), field);

        for (DynamicCompiler.Field child : field.children()) {
            this.allocate(child, scope);
        }

        if (field.item() != null) {
            final int next = this.sizes.size();
            this.sizes.add(0);
            this.allocate(field.item(), next);
        }

        for (DynamicCompiler.Field child : field.variants().values()) {
            final int next = this.sizes.size();
            this.sizes.add(0);
            this.allocate(child, next);
        }
        if (field.defaultVariant() != null) {
            final int next = this.sizes.size();
            this.sizes.add(0);
            this.allocate(field.defaultVariant(), next);
        }
    }

    /**
     * 收集已有引用目标，避免编码器丢弃后续求值需要的规范化值。
     */
    private void dependencies(DynamicCompiler.Field field) {
        if (field.referencePath() != null) {
            this.retained.add(field.referencePath());
        }
        if (field.conditionPath() != null) {
            this.retained.add(field.conditionPath());
        }
        this.retained.addAll(field.expressionReferences().values());
    }

    /**
     * 将长度回填转换为目标整数配置及区间起止动作，执行帧负责记录实际位置。
     * 目标字段也必须保留回填后的值，供后续引用使用。
     */
    private void backfill(DynamicCompiler.Field field) {
        if (field.computedLength() == null && field.checksumRange() == null) {
            return;
        }
        final var length = field.computedLength();
        final var checksum = field.checksumRange();
        final String start = length != null ? length.startPath() : requireNonNull(checksum).startPath();
        final String end = length != null ? length.endPath() : requireNonNull(checksum).endPath();
        final List<String> paths = new ArrayList<>(fields.keySet());
        final var target = new Backfill(
                address(field.path()), field.path(),
                requireNonNull(field.bitWidth()),
                field.definition().endian() == DynamicEndian.LITTLE,
                field.definition().bitOrder() == DynamicBitOrder.LSB0,
                length != null ? length.unit() : DynamicLengthUnit.BYTE,
                checksum == null ? null : checksum.algorithm(),
                length != null || paths.indexOf(field.path()) < paths.indexOf(start)
        );
        this.backfills.put(field.path(), target);
        this.starts.computeIfAbsent(start, ignored -> new ArrayList<>()).add(target);
        this.ends.computeIfAbsent(end, ignored -> new ArrayList<>()).add(target);
        this.retained.add(field.path());
    }

    /**
     * 校验和必须看到最终字节。覆盖区间中的占位字段先完成回填，同一终点按依赖顺序结算。
     * 若被覆盖字段在区间结束后才回填，或两个校验和相互覆盖占位字段，则拒绝编译。
     */
    private void orderBackfills() {
        final List<String> paths = new ArrayList<>(fields.keySet());
        final Map<String, Integer> finish = new HashMap<>();
        ends.forEach((path, actions) -> {
            final int last = subtreeEnd(path, paths);
            actions.forEach(action -> finish.put(action.path(), last));
        });
        final Map<String, Set<String>> dependencies = new LinkedHashMap<>();
        backfills.forEach((path, action) -> {
            final Set<String> required = new LinkedHashSet<>();
            final var range = requireNonNull(fields.get(path)).checksumRange();
            if (range != null) {
                final int start = paths.indexOf(range.startPath());
                final int end = subtreeEnd(range.endPath(), paths);
                backfills.forEach((otherPath, other) -> {
                    final int position = paths.indexOf(otherPath);
                    if (other.deferred() && position >= start && position <= end) {
                        if (requireNonNull(finish.get(otherPath)) > end) {
                            throw new DynamicCodecException(path, -1, "校验和区间含有尚未完成的回填字段: " + otherPath);
                        }
                        required.add(otherPath);
                    }
                });
            }
            dependencies.put(path, required);
        });
        final Map<String, Integer> ranks = backfillRanks(dependencies);
        ends.values().forEach(actions -> actions.sort(Comparator.comparingInt(action -> requireNonNull(ranks.get(action.path())))));
    }

    private static Map<String, Integer> backfillRanks(Map<String, Set<String>> dependencies) {
        final Map<String, Integer> ordered = new LinkedHashMap<>();
        while (ordered.size() < dependencies.size()) {
            boolean advanced = false;
            for (final var entry : dependencies.entrySet()) {
                if (!ordered.containsKey(entry.getKey()) && ordered.keySet().containsAll(entry.getValue())) {
                    ordered.put(entry.getKey(), ordered.size());
                    advanced = true;
                }
            }
            if (!advanced) {
                throw new DynamicCodecException("$", -1, "校验和回填存在循环依赖");
            }
        }
        return ordered;
    }

    private static int subtreeEnd(String path, List<String> paths) {
        int last = paths.indexOf(path);
        for (int index = last + 1; index < paths.size(); index++) {
            final String candidate = paths.get(index);
            if (!candidate.startsWith(path + ".") && !candidate.startsWith(path + "[")) {
                break;
            }
            last = index;
        }
        return last;
    }

    /**
     * 按字段类型构造互斥的操作，并附上已解析的地址、条件、值保留标志及回填动作。
     * 此后执行器只读取布局配置，不再解释编译期字段树。
     */
    private Node node(DynamicCompiler.Field field) {
        final DynamicField definition = field.definition();
        final boolean little = definition.endian() == DynamicEndian.LITTLE;
        final boolean lsb = definition.bitOrder() == DynamicBitOrder.LSB0;
        final Operation operation = switch (field.executionType()) {
            case UINT, INT -> {
                final boolean signed = field.executionType() == DynamicCompiler.Type.INT;
                final Integer bits = requireNonNull(field.bitWidth());
                final List<BitView> viewList = this.fields.values().stream()
                        .filter(candidate -> candidate.executionType() == DynamicCompiler.Type.DERIVED
                                && candidate.definition().expression() == null
                                && field.path().equals(candidate.referencePath())
                                && parentPath(field.path()).equals(parentPath(candidate.path()))
                        )
                        .map(this::view)
                        .toList();
                yield new IntegerOp(bits, signed, little, lsb, this.backfills.get(field.path()), viewList);
            }
            case FLOAT -> new FloatOp(requireNonNull(definition.bits()), little, lsb);
            case BITS -> new BitsOp(requireNonNull(definition.bits()), lsb);
            case ALIGN -> new AlignOp(requireNonNull(definition.alignTo()), definition.fillBit() == null ? 0 : definition.fillBit(), lsb);
            case STRING, BCD, HEX, BYTES, PADDING, EXTENSION -> {
                final ByteFormat format = this.format(field);
                final Length length = this.length(field);
                yield new BytesOp(format, length);
            }
            case STRUCT -> {
                final List<Node> nodeList = field.children().stream().map(this::node).toList();
                final Length length = definition.length() == null ? null : length(field);
                yield new StructOp(nodeList, length);
            }
            case ARRAY -> new ArrayOp(node(requireNonNull(field.item())), length(field));
            case TLV, MAP, PAIR -> {
                final DynamicCompiler.EntryLayout entryLayout = requireNonNull(field.entryLayout());
                final boolean pair = field.executionType() == DynamicCompiler.Type.PAIR;
                final Map<String, Node> variants = variants(field);
                final Length length = length(field);
                yield new EntriesOp(
                        variants, length,
                        entryLayout.keyBits(),
                        entryLayout.lengthBits(),
                        pair
                );
            }
            case CHOICE -> {
                final Address discriminator = this.address(field.referencePath());
                final Map<String, Node> variants = variants(field);
                yield new ChoiceOp(discriminator, variants, field.defaultVariant() == null ? null : node(field.defaultVariant()));
            }
            case DERIVED -> {
                final ValueExpression expression = definition.expression() == null ? null : expressionFor(definition.expression(), field, "derived.expression");
                final BitView bitView = definition.expression() == null ? this.view(field) : null;
                yield new DerivedOp(expression, bitView);
            }
        };
        final String id = definition.id();
        final String name = definition.name() == null ? definition.id() : definition.name();
        final Address address = this.address(field.path());
        final ValueExpression condition = this.condition(field);

        return new Node(
                id, name, definition.type(),
                field.path(),
                address,
                retained.contains(field.path()),
                condition,
                generation(field),
                operation,
                starts.getOrDefault(field.path(), List.of()),
                ends.getOrDefault(field.path(), List.of())
        );
    }

    private @Nullable Generation generation(DynamicCompiler.Field field) {
        final DynamicGeneratedValue generated = field.definition().generated();
        if (generated == null || generated.checksum() != null) {
            return null;
        }
        if (generated.countOf() != null) {
            return new GeneratedCount(generated.countOf());
        }
        if (generated.expression() != null) {
            return new GeneratedExpression(expressionFor(generated.expression(), field, "generated.expression"));
        }
        final Object constant = DynamicExpressionValues.freeze(generated.constant(), field.path(), -1);
        return new GeneratedExpression((frame, offset) -> constant);
    }

    // 字段引用已经在前一编译阶段验证；这里只接受已分配的布局地址。
    private Address address(@Nullable String path) {
        return requireNonNull(this.addresses.get(requireNonNull(path)));
    }

    /**
     * 获取同级派生位段与源整数共有的父路径，仅在构建反向合成列表时使用。
     */
    private static String parentPath(String path) {
        return path.substring(0, path.lastIndexOf('.'));
    }

    /**
     * 将已筛选的条目或选择分支递归编译为执行节点。
     */
    private Map<String, Node> variants(DynamicCompiler.Field field) {
        final Map<String, Node> result = new LinkedHashMap<>();
        field.variants().forEach((key, value) -> result.put(key, node(value)));
        return result;
    }

    /**
     * 预解析字节类型的字符集、填充设置和扩展实例。
     * 已冻结的选项仍传给扩展，由其按自身合同解释专属配置。
     */
    private ByteFormat format(DynamicCompiler.Field field) {
        final DynamicField definition = field.definition();
        final Map<String, Object> config = definition.options() == null ? Map.of() : definition.options();
        final boolean paddingAllowed = config.containsKey("padByte") || "padding".equals(definition.type());
        return new ByteFormat(
                definition.type(),
                definition.charset() == null ? StandardCharsets.UTF_8 : Charset.forName(definition.charset()),
                ((Number) config.getOrDefault("padByte", 0)).intValue(),
                paddingAllowed,
                "left".equals(config.get("padSide")),
                Boolean.TRUE.equals(config.get("stripPadding")),
                field.extension(),
                config
        );
    }

    /**
     * 将派生位段的源字段引用和偏移配置固化为整数视图。
     */
    private BitView view(DynamicCompiler.Field field) {
        final DynamicField definition = field.definition();
        final DynamicCompiler.Field source = requireNonNull(fields.get(field.referencePath()));
        final Map<String, Object> config = requireNonNull(definition.options());
        return new BitView(
                definition.id(), field.path(),
                address(field.referencePath()),
                ((Number) requireNonNull(config.get("bitOffset"))).intValue(),
                ((Number) requireNonNull(config.get("bitWidth"))).intValue(),
                requireNonNull(source.bitWidth()),
                source.definition().endian() == DynamicEndian.LITTLE,
                source.definition().bitOrder() == DynamicBitOrder.LSB0
        );
    }

    /**
     * 将互斥的长度来源编译为固定值、槽位引用、前缀、表达式或剩余边界。
     * 对回填字段的引用标记为可延迟，执行时仅在目标仍待回填时使用实际内容长度。
     */
    private Length length(DynamicCompiler.Field field) {
        final var length = requireNonNull(field.definition().length());
        final LengthSource source;
        if (length.fixed() != null) {
            source = new Fixed(length.fixed());
        } else if (length.ref() != null) {
            source = new Referenced(address(field.referencePath()), requireNonNull(fields.get(field.referencePath())).computedLength() != null);
        } else if (length.prefixBits() != null) {
            source = new Prefix(length.prefixBits());
        } else if (length.expression() != null) {
            source = new Evaluated(expressionFor(length.expression(), field, "length.expression"));
        } else {
            source = new Remaining();
        }
        return new Length(length.unit(), source);
    }

    /**
     * 编译字段出现条件。表达式结果必须为 Boolean；旧式引用条件在源值缺失或为空时返回 false。
     * 待回填值不能作为旧式条件输入，未声明条件的字段恒为出现。
     */
    private ValueExpression condition(DynamicCompiler.Field field) {
        final String path = field.path();
        final DynamicCondition condition = field.definition().when();
        if (condition == null) {
            return (frame, offset) -> true;
        }
        if (condition.expression() != null) {
            final ValueExpression compiled = this.expressionFor(condition.expression(), field, "when.expression");

            return (frame, offset) -> {
                final Object value = compiled.evaluate(frame, offset);
                if (!(value instanceof Boolean)) {
                    throw new DynamicCodecException(path, offset, "when.expression 必须返回 Boolean");
                }
                return value;
            };
        }

        final Address address = address(field.conditionPath());
        final DynamicConditionOperator operator = requireNonNull(condition.op());
        final String expected = requireNonNull(condition.value());
        final BigInteger numeric = operator == DynamicConditionOperator.EQ || operator == DynamicConditionOperator.NE
                ? null
                : new BigInteger(expected);

        return (frame, offset) -> {
            final Object value = frame.get(address);
            if (value == DynamicFrame.MISSING || value == null) {
                return false;
            }

            if (value == DynamicFrame.PENDING) {
                throw new DynamicCodecException(path, offset, "条件引用待回填字段");
            }

            if (operator == DynamicConditionOperator.EQ || operator == DynamicConditionOperator.NE) {
                final boolean equal = String.valueOf(value).equals(expected);
                return (operator == DynamicConditionOperator.EQ) == equal;
            }

            final BigInteger actual = DynamicNumbers.requireInteger(value, path, offset);
            return switch (operator) {
                case GT -> actual.compareTo(numeric) > 0;
                case GTE -> actual.compareTo(numeric) >= 0;
                case LT -> actual.compareTo(numeric) < 0;
                case LTE -> actual.compareTo(numeric) <= 0;
                // numeric 是位掩码：AND 后仍等于掩码，表示掩码指定的所有位都已置 1。
                case BIT_SET -> actual.and(numeric).equals(numeric);
                // AND 后为 0，表示掩码指定的所有位都为 0；不要求实际值的其他位为 0。
                case BIT_CLEAR -> actual.and(numeric).signum() == 0;
                default -> throw new IllegalStateException("未处理条件");
            };
        };
    }

    /**
     * 按表达式用途包装编译与求值错误，保留所属字段路径和可用的 bit 位置。
     */
    private ValueExpression expressionFor(DynamicExpression definition, DynamicCompiler.Field field, String purpose) {
        final String path = field.path();
        final ValueExpression compiled;
        try {
            compiled = expression(definition, field);
        } catch (RuntimeException error) {
            throw new DynamicCodecException(path, -1, purpose + ": " + error.getMessage(), error);
        }
        return (frame, offset) -> {
            try {
                return compiled.evaluate(frame, offset);
            } catch (RuntimeException error) {
                throw new DynamicCodecException(path, offset, purpose + ": " + error.getMessage(), error);
            }
        };
    }

    /**
     * 将受控表达式树编译为帧求值函数；引用直接使用地址，常量只冻结一次。
     * 引擎表达式在此绑定宿主实例并预创建表达式，每次求值新建上下文，
     * 只注入已声明且当前可用的字段绑定。布尔 and/or 在求值函数中保持短路。
     */
    private ValueExpression expression(DynamicExpression definition, DynamicCompiler.Field field) {
        final String path = field.path();
        final String engineId = definition.engine();
        if (engineId != null) {
            if (options.expressionEngines() == null) {
                throw new DynamicCodecException(path, -1, "未启用表达式引擎: " + engineId);
            }
            try {
                final XtreamExpressionEngine engine = options.expressionEngines().getEngine((XtreamExpressionEngine.CustomXtreamExpressionEngineId) () -> engineId);
                final XtreamExpression compiled = engine.createExpression(requireNonNull(definition.source()));
                final Map<String, Address> bindings = new LinkedHashMap<>();
                for (Map.Entry<String, String> entry : requireNonNull(definition.bindings()).entrySet()) {
                    final String name = entry.getKey();
                    final String target = entry.getValue();
                    bindings.put(name, address(field.expressionReferences().get(target)));
                }

                return (frame, offset) -> {
                    try {
                        final XtreamEvaluationContext context = engine.createEvaluationContext(null);
                        for (final var binding : bindings.entrySet()) {
                            final Object value = frame.get(binding.getValue());
                            if (value == DynamicFrame.MISSING || value == DynamicFrame.PENDING) {
                                throw new DynamicCodecException(path, offset, "表达式绑定不可用: " + binding.getKey());
                            }
                            context.setVariable(binding.getKey(), DynamicExpressionValues.freeze(value, path, offset));
                        }
                        return DynamicExpressionValues.freeze(compiled.evaluate(context, null), path, offset);
                    } catch (DynamicCodecException error) {
                        throw error;
                    } catch (RuntimeException error) {
                        throw new DynamicCodecException(path, offset, "表达式求值失败 (" + engineId + ")", error);
                    }
                };
            } catch (RuntimeException error) {
                throw new DynamicCodecException(path, -1, "表达式编译失败 (" + engineId + ")", error);
            }
        }

        if (definition.ref() != null) {
            final Address address = address(field.expressionReferences().get(definition.ref()));
            return (frame, offset) -> frame.require(address, path, offset);
        }

        if (definition.value() != null) {
            final Object value = DynamicExpressionValues.freeze(definition.value(), path, -1);
            return (frame, offset) -> value;
        }

        final DynamicExpressionOperator operator = requireNonNull(definition.op());
        final List<ValueExpression> arguments = requireNonNull(definition.args()).stream().map(arg -> expression(arg, field)).toList();
        return (frame, offset) -> {
            final Object left = arguments.getFirst().evaluate(frame, offset);
            return switch (operator) {
                case NOT -> !DynamicExpressionValues.bool(left, path, offset);
                case BIT_NOT -> DynamicExpressionValues.bitNot(left, path, offset);
                case AND -> {
                    // yield DynamicExpressionValues.bool(left, path, offset) && DynamicExpressionValues.bool(arguments.get(1).evaluate(frame, offset), path, offset);
                    if (!DynamicExpressionValues.bool(left, path, offset)) {
                        yield false;
                    }
                    final Object right = arguments.get(1).evaluate(frame, offset);
                    yield DynamicExpressionValues.bool(right, path, offset);
                }
                case OR -> {
                    // yield DynamicExpressionValues.bool(left, path, offset) || DynamicExpressionValues.bool(arguments.get(1).evaluate(frame, offset), path, offset);
                    if (DynamicExpressionValues.bool(left, path, offset)) {
                        yield true;
                    }
                    final Object right = arguments.get(1).evaluate(frame, offset);
                    yield DynamicExpressionValues.bool(right, path, offset);
                }
                default -> {
                    final Object right = arguments.get(1).evaluate(frame, offset);
                    yield DynamicExpressionValues.binary(operator, left, right, path, offset);
                }
            };
        };
    }
}
