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


import io.github.hylexus.xtream.codec.core.dynamic.schema.*;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtension;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtensionRegistry;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * 在执行前校验结构并解析引用。字段引用允许访问同级已定义字段及外层已定义字段，
 * 但只能指向当前字段之前已经可用的值。编译结果记录绝对路径与子结构，
 * 执行时不再重新判断字段定义是否合法。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public final class DynamicCompiler {
    private DynamicCompiler() {
    }

    /**
     * 使用空扩展注册表编译结构；含 extension 字段的定义需要显式传入注册表。
     */
    public static DynamicPlan compile(DynamicSchema schema) {
        return compile(schema, DynamicExtensionRegistry.EMPTY);
    }

    /**
     * 校验结构并生成可并发复用的计划。定义错误在此阶段抛出带字段路径的异常，
     * 后续每次编解码只处理报文值和边界。
     */
    public static DynamicPlan compile(DynamicSchema schema, DynamicExtensionRegistry registry) {
        return compile(schema, DynamicSchema.ALL_VERSION, registry);
    }

    /**
     * 按目标协议版本选择字段，并生成可重复执行的计划。调用方持有计划即可复用，core 不创建全局缓存。
     */
    public static DynamicPlan compile(DynamicSchema schema, int version) {
        return compile(schema, version, DynamicExtensionRegistry.EMPTY);
    }

    /**
     * 协议版本选择先于引用解析；未入选字段不参与布局，引用它们的活动字段会在编译时失败。
     * 嵌套结构、数组元素和条目分支均沿用同一个目标版本。
     */
    public static DynamicPlan compile(DynamicSchema schema, int version, DynamicExtensionRegistry registry) {
        return compile(schema, new DynamicCompileOptions(version, registry, null));
    }

    public static DynamicPlan compile(DynamicSchema schema, DynamicCompileOptions options) {
        final List<Field> fields = compileFields(schema.fields(), "$", Map.of(), 0, options.version(), options.extensions());
        return new DynamicPlan(schema, options.version(), new DynamicLayoutCompiler(options).compile(fields));
    }

    private static @Nullable DynamicExtension resolveExtension(DynamicField dynamicField, String path, DynamicExtensionRegistry registry) {
        if (!"extension".equals(dynamicField.type())) {
            return null;
        }
        // 扩展在编译时绑定，执行计划不再持有注册表或按标识重复查找。
        final Object rawId = option(dynamicField, "extensionId", "");
        if (!(rawId instanceof String id) || id.isBlank()) {
            throw error(path, "扩展必须声明非空 extensionId");
        }
        final DynamicExtension extension = registry.extensions().get(id);
        if (extension == null) {
            throw error(path, "未注册的扩展: " + id);
        }
        try {
            extension.validate(dynamicField.options() == null ? Map.of() : dynamicField.options());
        } catch (IllegalArgumentException exception) {
            throw new DynamicCodecException(path, -1, "扩展配置无效: " + exception.getMessage(), exception);
        }
        return extension;
    }

    private static List<Field> compileFields(
            List<DynamicField> definitions, String parent,
            Map<String, Field> outer, int depth, int version,
            DynamicExtensionRegistry registry) {

        if (depth > 64) {
            throw error(parent, "结构嵌套超过 64 层");
        }

        // 第一步：选定协议版本，使后续引用与边界检查只看到本版本的实际布局。
        definitions = DynamicFieldSelection.select(definitions, parent, version);
        // <id, field>
        final Map<String, DynamicField> siblings = new LinkedHashMap<>();
        // 先收集同级标识，既能发现重复，也能对未执行字段形成的引用环给出明确错误。
        for (final DynamicField field : definitions) {
            if (siblings.putIfAbsent(field.id(), field) != null) {
                throw error(parent + "." + field.id(), "重复的字段 id");
            }
        }

        // 第二步：先检查同级依赖环，避免把循环仅报告为普通前向引用。
        checkCycles(siblings, parent);

        // 第三步：逐字段编译并记录已可用值；available 随声明顺序递增，拒绝前向引用。
        final Map<String, Field> available = new LinkedHashMap<>(outer);
        final List<Field> compiled = new ArrayList<>();
        for (int index = 0; index < definitions.size(); index++) {
            final DynamicField dynamicField = definitions.get(index);
            final String path = parent + "." + dynamicField.id();
            validateComputedLength(dynamicField, path, siblings, index);
            validateGeneratedTarget(dynamicField, path, siblings, index);

            final Field field = compileField(dynamicField, path, available, depth, version, registry);

            if (index != definitions.size() - 1 && consumesEnclosingBoundary(field)) {
                throw error(path, "字段可能消费外层剩余空间，只能位于容器末尾");
            }
            compiled.add(field);
            available.put(dynamicField.id(), field);

            // struct 的子字段可使用限定名访问，避免外层和内层同名时引用歧义。
            registerQualifiedChildren(available, dynamicField.id(), field);
        }
        return compiled;
    }

    /**
     * 判断字段是否可能将 remaining 的读取范围传播到所属容器的边界。
     * 用于限制字段只能位于容器末尾，并检查按项计数的数组元素能否重复执行。
     * 非 remaining 的 bit/byte 长度提供独立的内容边界，子字段的 remaining 只作用于该范围；item 计数仅约束项数。
     * 对没有独立边界的结构、选择分支和按项数组递归判断，不根据运行时条件排除可能的消费路径。
     *
     * @param field 已编译子节点及引用的字段
     * @return 按当前编译规则，字段可能消费所属容器的剩余空间时为 true
     */
    private static boolean consumesEnclosingBoundary(Field field) {
        if (field.definition().length() != null) {
            // remaining 直接以当前外层边界为终点，不为后续同级字段预留空间。
            if (Boolean.TRUE.equals(field.definition().length().remaining())) {
                return true;
            }
            if (field.definition().length().unit() == DynamicLengthUnit.ITEM) {
                // 按项数组和 Pair 都没有逐项字节边界；TLV/Map 的条目长度头则提供独立边界。
                // 固定 0 项不会执行元素；引用、前缀或表达式计数在此无法确定，按可能存在元素处理。
                final boolean consumes = switch (field.executionType()) {
                    case ARRAY -> field.item() != null && consumesEnclosingBoundary(field.item());
                    case PAIR -> field.variants().values().stream().anyMatch(DynamicCompiler::consumesEnclosingBoundary);
                    default -> false;
                };
                return consumes
                        && (field.definition().length().fixed() == null || field.definition().length().fixed() > 0);
            }
            // 其余已声明长度的字段在本规则中不继续传播子节点的剩余空间属性。
            return false;
        }
        return switch (field.executionType()) {
            // 无 length 的结构沿用外层边界；子字段已经通过末尾位置校验，只需检查最后一个子字段。
            case STRUCT -> !field.children().isEmpty() && consumesEnclosingBoundary(field.children().getLast());
            // 分支由运行时判别值选择；任一分支可能消费剩余空间，整个 choice 就具有这种可能性。
            case CHOICE -> field.variants().values().stream().anyMatch(DynamicCompiler::consumesEnclosingBoundary)
                    || (field.defaultVariant() != null && consumesEnclosingBoundary(field.defaultVariant()));
            // 其他无 length 的类型不会通过上述容器或分支规则传播剩余空间属性。
            default -> false;
        };
    }

    /**
     * 编译一个已入选字段：解析引用，递归编译子结构，最后组装不可变计划节点。
     * available 只包含前序字段；当前字段要等本方法成功后才登记到作用域。
     */
    private static Field compileField(
            DynamicField dynamicField, String path,
            Map<String, Field> available, int depth, int version,
            DynamicExtensionRegistry registry) {

        final Field referenceTarget = referenceTarget(dynamicField, path, available);
        validateDerivedRange(dynamicField, referenceTarget, path);

        final String conditionPath = conditionPath(dynamicField, path, available);
        final List<Field> children = dynamicField.fields() == null
                ? List.of()
                : compileFields(dynamicField.fields(), path, available, depth + 1, version, registry);

        final Field item = compileItem(dynamicField, path, available, depth, version, registry);
        final Map<String, Field> variants = compileVariants(dynamicField, path, available, depth, version, registry);
        final List<Field> defaults = dynamicField.defaultVariant() == null ? List.of()
                : compileFields(List.of(dynamicField.defaultVariant()), path + "[default]", available, depth + 1, version, registry);
        final Field defaultVariant = defaults.isEmpty() ? null : defaults.getFirst();
        final Map<String, String> expressionReferences = expressionReferences(dynamicField, path, available);
        final DynamicExtension extension = resolveExtension(dynamicField, path, registry);

        final DynamicLength length = dynamicField.length();
        final boolean repeatedRemaining = switch (dynamicField.type()) {
            case "array" -> item != null && consumesEnclosingBoundary(item);
            case "pair" -> variants.values().stream().anyMatch(DynamicCompiler::consumesEnclosingBoundary);
            default -> false;
        };
        if (repeatedRemaining && length != null
                && length.unit() == DynamicLengthUnit.ITEM
                && (length.fixed() == null || length.fixed() > 1)) {
            throw error(path, "多个集合元素不能共用剩余空间，需要为每项声明独立边界");
        }

        return new Field(
                dynamicField, path,
                referenceTarget == null ? null : referenceTarget.path(),
                conditionPath, children, item, variants, defaultVariant, expressionReferences, extension
        );
    }

    private static @Nullable Field compileItem(
            DynamicField dynamicField, String path,
            Map<String, Field> available, int depth, int version,
            DynamicExtensionRegistry registry) {

        if (dynamicField.item() == null) {
            return null;
        }
        final List<Field> selected = compileFields(List.of(dynamicField.item()), path + "[]", available,
                depth + 1, version, registry);
        if (selected.isEmpty()) {
            throw error(path, "当前版本的数组元素定义不存在；请对整个数组声明版本");
        }
        return selected.getFirst();
    }

    private static Map<String, Field> compileVariants(
            DynamicField dynamicField, String path,
            Map<String, Field> available, int depth, int version,
            DynamicExtensionRegistry registry) {

        final Map<String, Field> variants = new LinkedHashMap<>();
        if (dynamicField.variants() == null) {
            return variants;
        }
        dynamicField.variants().forEach((key, variant) -> {
            final List<Field> selected = compileFields(List.of(variant), path + "[" + key + "]", available,
                    depth + 1, version, registry);
            if (!selected.isEmpty()) {
                variants.put(key, selected.getFirst());
            }
        });
        return variants;
    }

    private static Map<String, String> expressionReferences(DynamicField dynamicField, String path, Map<String, Field> available) {
        final Map<String, String> references = new LinkedHashMap<>();
        if (dynamicField.expression() != null) {
            validateExpression(dynamicField.expression(), path, available, references);
        }
        if (dynamicField.generated() != null && dynamicField.generated().expression() != null) {
            validateExpression(dynamicField.generated().expression(), path, available, references);
        }
        if (dynamicField.when() != null && dynamicField.when().expression() != null) {
            validateExpression(dynamicField.when().expression(), path, available, references);
        }
        if (dynamicField.length() != null && dynamicField.length().expression() != null) {
            validateExpression(dynamicField.length().expression(), path, available, references);
        }
        return references;
    }

    private static @Nullable Field referenceTarget(DynamicField field, String path, Map<String, Field> available) {
        final String reference = reference(field);
        if (reference == null) {
            return null;
        }
        final Field target = available.get(reference);
        if (target == null) {
            throw error(path, "引用必须指向已经可用的字段: " + reference);
        }
        if (!Set.of(Type.UINT, Type.INT, Type.DERIVED).contains(target.executionType())) {
            throw error(path, "引用目标必须为整数: " + reference);
        }
        return target;
    }

    private static void validateDerivedRange(DynamicField dynamicField, @Nullable Field target, String path) {
        if (!"derived".equals(dynamicField.type()) || dynamicField.expression() != null) {
            return;
        }
        // 位段视图不占用新的报文空间；偏移必须落在已经编译的源整数内部。
        final int offset = dynamicField.options() != null && dynamicField.options().get("bitOffset") instanceof Number number
                ? number.intValue() : -1;
        final int width = dynamicField.options() != null && dynamicField.options().get("bitWidth") instanceof Number number
                ? number.intValue() : -1;
        if (target == null || target.bitWidth() == null || offset < 0 || width < 1
                || (long) offset + width > target.bitWidth()) {
            throw error(path, "派生位段必须声明有效的 bitOffset 和 bitWidth，且位于源整数范围内");
        }
        if (target.definition().endian() == DynamicEndian.LITTLE
                && target.definition().bitOrder() != DynamicBitOrder.LSB0
                && offset / 8 != (offset + width - 1) / 8
                && (offset % 8 != 0 || width % 8 != 0)) {
            throw error(path, "小端 msb0 位段跨字节时必须覆盖完整字节，才能表示精确 Trace 范围");
        }
    }

    private static @Nullable String conditionPath(DynamicField dynamicField, String path, Map<String, Field> available) {
        if (dynamicField.when() == null || dynamicField.when().expression() != null) {
            return null;
        }
        final Field source = available.get(dynamicField.when().ref());
        if (source == null) {
            throw error(path, "条件引用必须指向已经可用的字段: " + dynamicField.when().ref());
        }
        return source.path();
    }

    private static void validateComputedLength(DynamicField dynamicField, String path, Map<String, DynamicField> siblings, int index) {
        if (dynamicField.computedLengthOf() == null) {
            return;
        }
        // 回填长度只能覆盖当前字段之后的连续字段区间，以保持单次顺序写入。
        final String[] range = dynamicField.computedLengthOf().split("\\.\\.", -1);
        final List<String> ids = List.copyOf(siblings.keySet());
        final int start = ids.indexOf(range[0]);
        final int end = range.length == 1 ? start : ids.indexOf(range[range.length - 1]);
        if (range.length > 2 || start <= index || end < start) {
            throw error(path, "长度区间必须指向当前字段之后的有序字段");
        }
    }

    private static void validateGeneratedTarget(DynamicField field, String path, Map<String, DynamicField> siblings, int index) {
        if (field.generated() == null) {
            return;
        }
        if (field.generated().countOf() != null) {
            final DynamicField target = siblings.get(field.generated().countOf());
            if (target == null || field.id().equals(target.id()) || !Set.of("array", "tlv", "map", "pair").contains(target.type())) {
                throw error(path, "countOf 必须指向同级集合字段");
            }
        }
        if (field.generated().checksum() != null) {
            final String[] range = field.generated().checksum().range().split("\\.\\.", -1);
            final List<String> ids = List.copyOf(siblings.keySet());
            final int start = ids.indexOf(range[0]);
            final int end = ids.indexOf(range[range.length - 1]);
            if (start < 0 || end < start || (index >= start && index <= end)) {
                throw error(path, "校验和区间必须为同级有序字段，且不能包含自身");
            }
        }
    }

    private static void registerQualifiedChildren(Map<String, Field> available, String prefix, Field parent) {
        for (final Field child : parent.children()) {
            final String qualified = prefix + "." + child.definition().id();
            available.put(qualified, child);
            registerQualifiedChildren(available, qualified, child);
        }
    }

    private static @Nullable String reference(DynamicField dynamicField) {
        return dynamicField.length() != null && dynamicField.length().ref() != null
                ? dynamicField.length().ref()
                : dynamicField.ref();
    }

    private static void validateExpression(
            DynamicExpression expression, String path,
            Map<String, Field> available, Map<String, String> references) {
        if (expression.bindings() != null) {
            expression.bindings().values().forEach(target -> {
                final var source = available.get(target);
                if (source == null) {
                    throw error(path, "表达式绑定必须指向已可用字段: " + target);
                }
                references.put(target, source.path());
            });
        }
        if (expression.ref() != null) {
            final Field source = available.get(expression.ref());
            if (source == null) {
                throw error(path, "表达式引用必须指向已可用字段: " + expression.ref());
            }
            references.put(expression.ref(), source.path());
        }
        if (expression.op() != null) {
            Objects.requireNonNull(expression.args()).forEach(arg -> validateExpression(arg, path, available, references));
        }
    }

    /**
     * 沿同级字段的直接引用链检查循环，在逐字段解析前优先报告循环依赖。
     * 每个字段仅跟随 {@link #reference(DynamicField)} 选出的一个引用：优先 length.ref，否则取字段 ref。
     * 使用迭代遍历及两组访问状态，无需递归；条件和表达式中的引用由后续编译流程校验。
     *
     * @param fields 当前容器选定协议版本后的同级字段，键为字段标识
     * @param parent 当前容器的绝对路径，用于定位循环中的字段
     */
    private static void checkCycles(Map<String, DynamicField> fields, String parent) {
        // 已确认引用链无环的字段，跨起点复用；多个字段指向同一条无环链不构成循环。
        final Set<String> finished = new HashSet<>();
        for (final String id : fields.keySet()) {
            // 仅记录本次从 id 出发的链；再次遇到链中的字段才表示闭环，例如 a -> b -> a。
            final Set<String> visiting = new HashSet<>();
            String current = id;
            // 无引用、引用目标不在当前同级映射内或进入已检查的链时停止；目标的可用性由后续解析校验。
            while (current != null && fields.containsKey(current) && !finished.contains(current)) {
                if (!visiting.add(current)) {
                    throw error(parent + "." + current, "字段引用存在循环依赖");
                }
                current = reference(fields.get(current));
            }
            // 链已正常结束，将本次经过的所有字段标为完成，避免其他起点重复遍历公共链尾。
            finished.addAll(visiting);
        }
    }

    private static Object option(DynamicField field, String key, Object fallback) {
        return field.options() == null ? fallback : field.options().getOrDefault(key, fallback);
    }

    private static DynamicCodecException error(String path, String reason) {
        return new DynamicCodecException(path, -1, reason);
    }

    // 原始定义与执行布局分开保存；计划解析路径和整数位宽，运行时不改写 DSL 或重复解析类型名。
    record Field(
            DynamicField definition,
            Type executionType,
            @Nullable Integer bitWidth,
            String path,
            @Nullable String referencePath,
            @Nullable String conditionPath,
            List<Field> children,
            @Nullable Field item,
            Map<String, Field> variants,
            @Nullable Field defaultVariant,
            Map<String, String> expressionReferences,
            @Nullable ComputedLength computedLength,
            @Nullable ChecksumRange checksumRange,
            @Nullable EntryLayout entryLayout,
            @Nullable DynamicExtension extension) {

        Field(
                DynamicField definition, String path, @Nullable String referencePath, @Nullable String conditionPath,
                List<Field> children, @Nullable Field item, Map<String, Field> variants, @Nullable Field defaultVariant,
                Map<String, String> expressionReferences, @Nullable DynamicExtension extension
        ) {
            this(definition, executionType(definition), bitWidth(definition), path, referencePath, conditionPath,
                    children, item, variants, defaultVariant, expressionReferences, computedLength(definition, path),
                    checksumRange(definition, path), entryLayout(definition), extension);
        }

        Field {
            children = List.copyOf(children);
            variants = Map.copyOf(variants);
            expressionReferences = Map.copyOf(expressionReferences);
        }

        private static Type executionType(DynamicField definition) {
            final String type = definition.type();
            // 字段构造时已验证 uN/iN；计划只提取一次执行原语，不改变 definition.type()。
            if (type.matches("u[1-9][0-9]?")) {
                return Type.UINT;
            }
            if (type.matches("i[1-9][0-9]?")) {
                return Type.INT;
            }
            return Type.valueOf(type.toUpperCase(Locale.ROOT));
        }

        private static @Nullable Integer bitWidth(DynamicField definition) {
            final String type = definition.type();
            if (type.matches("[ui][1-9][0-9]?")) {
                return Integer.parseInt(type.substring(1));
            }
            return definition.bits();
        }

        private static @Nullable ComputedLength computedLength(DynamicField definition, String path) {
            if (definition.computedLengthOf() == null) {
                return null;
            }
            final String[] range = definition.computedLengthOf().split("\\.\\.", -1);
            final String scope = path.substring(0, path.lastIndexOf('.') + 1);
            final Object unit = option(definition, "lengthUnit", "byte");
            return new ComputedLength(scope + range[0], scope + range[range.length - 1],
                    DynamicLengthUnit.from((String) unit));
        }

        private static @Nullable EntryLayout entryLayout(DynamicField definition) {
            if (!"tlv".equals(definition.type())
                    && !"map".equals(definition.type())
                    && !"pair".equals(definition.type())) {
                return null;
            }
            return new EntryLayout(
                    ((Number) option(definition, "keyBits", 8)).intValue(),
                    ((Number) option(definition, "lengthBits", 8)).intValue()
            );
        }

        private static @Nullable ChecksumRange checksumRange(DynamicField definition, String path) {
            if (definition.generated() == null || definition.generated().checksum() == null) {
                return null;
            }
            final DynamicChecksum checksum = definition.generated().checksum();
            final String[] range = checksum.range().split("\\.\\.", -1);
            final String scope = path.substring(0, path.lastIndexOf('.') + 1);
            return new ChecksumRange(scope + range[0], scope + range[range.length - 1], checksum.algorithm());
        }
    }

    enum Type {
        UINT, INT, FLOAT, STRING, BCD, HEX, BYTES, BITS, PADDING, ALIGN,
        STRUCT, ARRAY, DERIVED, TLV, MAP, PAIR, CHOICE, EXTENSION
    }

    record ComputedLength(String startPath, String endPath, DynamicLengthUnit unit) {
    }

    record EntryLayout(int keyBits, int lengthBits) {
    }

    record ChecksumRange(String startPath, String endPath, DynamicChecksumAlgorithm algorithm) {
    }
}
