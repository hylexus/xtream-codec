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

package io.github.hylexus.xtream.codec.core.utils;

import io.github.hylexus.xtream.codec.common.bean.BeanMetadata;
import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.FieldTransformer;
import io.github.hylexus.xtream.codec.core.annotation.DerivedField;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * @author hylexus
 * @author opencode (AI)
 */
public final class XtreamFieldUtils {

    private XtreamFieldUtils() {
        throw new UnsupportedOperationException();
    }

    // 缓存：Key = AnnotatedElement, Value = List<XtreamField>
    private static final Map<AnnotatedElement, List<XtreamField>> CACHE = new ConcurrentHashMap<>();

    /**
     * 获取 XtreamField 注解列表，如果没有注解则返回包含默认代理实例的列表
     * <p>
     * 注意：这个方法是 {@link Record} 类型专用的
     */
    public static List<XtreamField> getOrDefault(AnnotatedElement element) {
        return CACHE.computeIfAbsent(element, e -> {
            final List<XtreamField> fields = resolveAnnotations(e);
            if (fields.isEmpty()) {
                // 如果只有 @DerivedField 没有显式 @XtreamField，返回空让 DerivedField 逻辑处理
                if (MergedAnnotations.from(e).isPresent(DerivedField.class)) {
                    return List.of();
                }
                return List.of(generateTransientFieldProxyInstance(element));
            }

            final boolean hasDefaultVersion = fields.stream().anyMatch(annotation -> {
                for (final int version : annotation.version()) {
                    if (version == XtreamField.ALL_VERSION) {
                        return true;
                    }
                }
                return false;
            });

            if (hasDefaultVersion) {
                return fields;
            }
            // 没有默认版本 ==> 添加一个默认版本
            final ArrayList<XtreamField> newList = new ArrayList<>(fields);
            newList.add(generateTransientFieldProxyInstance(element));
            return Collections.unmodifiableList(newList);
        });
    }

    /**
     * 获取 XtreamField 注解列表，如果没有注解返回空列表（不返回 null）
     */
    public static List<XtreamField> getOrEmpty(AnnotatedElement element) {
        final List<XtreamField> cached = CACHE.get(element);
        if (cached != null) {
            return cached;
        }
        final List<XtreamField> fields = resolveAnnotations(element);
        CACHE.put(element, fields);
        return fields;
    }

    private static List<XtreamField> resolveAnnotations(AnnotatedElement element) {
        final MergedAnnotations annotations = MergedAnnotations.from(element);

        if (annotations.isPresent(XtreamField.class)) {
            final List<XtreamField> resultList = new ArrayList<>();
            final List<MergedAnnotation<XtreamField>> list = annotations.stream(XtreamField.class).toList();
            for (MergedAnnotation<XtreamField> ann : list) {
                resultList.add(ann.synthesize());
            }
            return Collections.unmodifiableList(resultList);
        }
        return Collections.emptyList();
    }

    private static XtreamField generateTransientFieldProxyInstance(AnnotatedElement element) {
        final Class<?> type;
        if (element instanceof Field field) {
            type = field.getType();
        } else if (element instanceof RecordComponent recordComponent) {
            type = recordComponent.getType();
        } else {
            type = Object.class;
        }

        final InvocationHandler handler = new DefaultAnnotationInvocationHandler<>(XtreamField.class, type);
        final Object newProxyInstance = Proxy.newProxyInstance(
                XtreamField.class.getClassLoader(),
                new Class[]{XtreamField.class, XtreamTransientFieldProxy.class},
                handler
        );
        return (XtreamField) newProxyInstance;
    }

    public interface XtreamTransientFieldProxy {

        @Nullable
        Object defaultValueForNulls();

        Class<?> targetType();

    }


    private static final class DefaultAnnotationInvocationHandler<A extends Annotation>
            implements InvocationHandler, XtreamTransientFieldProxy {

        /**
         * @see XtreamField#nulls()
         */
        private static final String ATTRIBUTE_NAME_NULLS = "nulls";

        /**
         * @see XtreamTransientFieldProxy#defaultValueForNulls()
         */
        private static final String METHOD_NAME_DEFAULT_VALUE_FOR_NULLS = "defaultValueForNulls";

        /**
         * @see XtreamTransientFieldProxy#targetType()
         */
        private static final String METHOD_NAME_TARGET_TYPE = "targetType";

        private final Class<A> annotationType;
        private final Map<String, Object> defaultValues;
        private final Class<?> targetType;
        private @Nullable Object calculatedNullsValue;

        DefaultAnnotationInvocationHandler(Class<A> annotationType, Class<?> targetType) {
            this.annotationType = annotationType;
            this.targetType = targetType;

            final Map<String, Object> defaults = new LinkedHashMap<>();
            for (Method m : annotationType.getDeclaredMethods()) {
                final Object dv = m.getDefaultValue();
                // nulls
                if (ATTRIBUTE_NAME_NULLS.equals(m.getName()) && m.getReturnType().isEnum()) {
                    this.calculatedNullsValue = createDefaultValueForNulls((XtreamField.Nulls) dv, this.targetType);
                }
                defaults.put(m.getName(), dv);
            }
            // 属性读取、比较、哈希和展示统一使用代理实际生效的瞬态策略。
            defaults.put("codecStrategy", XtreamField.CodecStrategy.TRANSIENT);
            this.defaultValues = Collections.unmodifiableMap(defaults);
        }

        @Override
        @Nullable
        public Object defaultValueForNulls() {
            return this.calculatedNullsValue;
        }

        @Override
        public Class<?> targetType() {
            return this.targetType;
        }

        @Override
        @Nullable
        public Object invoke(Object proxy, Method method, Object[] args) {
            final String name = method.getName();
            return switch (name) {
                case "equals" -> this.equalsImpl(proxy, args[0]);
                case "hashCode" -> this.hashCodeImpl();
                case "toString" -> this.toStringImpl();
                case "annotationType" -> this.annotationType;
                // defaultValueForNulls
                case METHOD_NAME_DEFAULT_VALUE_FOR_NULLS -> this.defaultValueForNulls();
                // targetType
                case METHOD_NAME_TARGET_TYPE -> this.targetType();
                default -> defaultValues.get(name);
            };
        }

        private boolean equalsImpl(Object proxy, Object other) {
            if (proxy == other) {
                return true;
            }
            if (!annotationType.isInstance(other)) {
                return false;
            }

            for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
                try {
                    final Method m = annotationType.getDeclaredMethod(entry.getKey());
                    final Object otherVal = m.invoke(other);
                    if (!Objects.deepEquals(entry.getValue(), otherVal)) {
                        return false;
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to compare annotation property", e);
                }
            }
            return true;
        }

        private int hashCodeImpl() {
            int result = 0;
            for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
                final String name = entry.getKey();
                final Object value = entry.getValue();
                final int valueHash = value instanceof int[] array ? Arrays.hashCode(array) : Objects.hashCode(value);
                result += (127 * name.hashCode()) ^ valueHash;
            }
            return result;
        }

        private String toStringImpl() {
            final StringBuilder sb = new StringBuilder();
            sb.append('@').append(annotationType.getName()).append('(');
            final Iterator<Map.Entry<String, Object>> it = defaultValues.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<String, Object> e = it.next();
                sb.append(e.getKey()).append('=').append(e.getValue());
                if (it.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(')');
            return sb.toString();
        }
    }

    @Nullable
    public static Object createDefaultValueForNulls(XtreamField.Nulls nulls, Class<?> targetType) {
        if (targetType.isPrimitive()) {
            return defaultValueForPrimitive(targetType);
        }

        return switch (nulls) {
            case AS_NULL -> null;
            case AS_EMPTY -> {
                if (CharSequence.class.isAssignableFrom(targetType)) {
                    yield "";
                }
                if (Collection.class.isAssignableFrom(targetType)) {
                    if (Set.class.isAssignableFrom(targetType)) {
                        yield new HashSet<>();
                    }
                    if (List.class.isAssignableFrom(targetType)) {
                        yield new ArrayList<>();
                    }
                    yield new ArrayList<>();
                }
                if (Map.class.isAssignableFrom(targetType)) {
                    yield new HashMap<>();
                }
                yield null;
            }
            // null? (不应该执行到这里，写出来是为了分支完整性)
            case null -> throw new IllegalStateException("nulls strategy is missing");
        };
    }

    private static @Nullable Object defaultValueForPrimitive(Class<?> targetType) {
        if (targetType == boolean.class) {
            return false;
        } else if (targetType == byte.class) {
            return (byte) 0;
        } else if (targetType == short.class) {
            return (short) 0;
        } else if (targetType == int.class) {
            return 0;
        } else if (targetType == long.class) {
            return 0L;
        } else if (targetType == float.class) {
            return 0f;
        } else if (targetType == double.class) {
            return 0d;
        } else if (targetType == char.class) {
            return '\0';
        } else {
            return null;
        }
    }

    public static @Nullable XtreamField matchVersion(int targetVersion, List<XtreamField> xtreamFieldAnnotations) {
        XtreamField defaultVersion = null;

        for (final XtreamField annotation : xtreamFieldAnnotations) {
            for (final int version : annotation.version()) {
                if (version == targetVersion) {
                    // 一旦发现目标版本，立即成功
                    return annotation;
                } else if (version == XtreamField.ALL_VERSION) {
                    // 记录遇到的 "第一个" 默认版本
                    if (defaultVersion == null) {
                        defaultVersion = annotation;
                    }
                }
            }
        }
        // 无目标版本时，依赖默认版本兜底
        return defaultVersion;
    }

    /**
     * 从 {@link AnnotatedElement} 中解析所有 {@link DerivedField @DerivedField} 注解（含 {@code @Repeatable}）。
     */
    public static List<DerivedField> resolveDerivedFieldAnnotations(AnnotatedElement element) {
        final MergedAnnotations annotations = MergedAnnotations.from(element);
        if (annotations.isPresent(DerivedField.class)) {
            return annotations.stream(DerivedField.class)
                    .map(MergedAnnotation::synthesize)
                    .toList();
        }
        return List.of();
    }

    /**
     * 从多个 {@link DerivedField @DerivedField} 中匹配目标版本。
     * <p>
     * 匹配逻辑与 {@link #matchVersion(int, List)} 一致：
     * <ol>
     *   <li>精确匹配目标版本 → 立即返回</li>
     *   <li>无精确匹配，有 {@link DerivedField#ALL_VERSION} → 返回遇到的第一个默认版本</li>
     *   <li>无精确匹配也无默认版本 → 返回 {@code null}</li>
     * </ol>
     */
    public static @Nullable DerivedField matchDerivedFieldVersion(int targetVersion, List<DerivedField> annotations) {
        DerivedField fallback = null;
        for (final DerivedField annotation : annotations) {
            for (final int version : annotation.version()) {
                if (version == targetVersion) {
                    return annotation;
                } else if (version == DerivedField.ALL_VERSION && fallback == null) {
                    fallback = annotation;
                }
            }
        }
        return fallback;
    }

    /**
     * Creates a synthetic {@link XtreamField} proxy with {@link XtreamField.CodecStrategy#TRANSIENT}
     * for fields that don't have a real {@link XtreamField} annotation (e.g., {@code @DerivedField} fields).
     *
     * @since 0.6.0
     */
    public static XtreamField createTransientFieldProxy(Field field) {
        return generateTransientFieldProxyInstance(field);
    }

    private static final Logger log = LoggerFactory.getLogger(XtreamFieldUtils.class);

    /**
     * 内联派生字段求值：源字段解码后立即查询 {@link BeanMetadata#getDerivedBySource()} 并计算派生值。
     *
     * @param sourceValue  源字段解码值
     * @param sourceName   源字段名
     * @param beanMetadata 当前 Bean 元数据
     * @param consumer     接收 (派生字段元数据, 派生值) 的回调，由调用方决定如何消费派生值
     */
    public static void applyDerivedFieldsInline(
            @Nullable Object sourceValue,
            String sourceName,
            BeanMetadata beanMetadata,
            BiConsumer<BeanPropertyMetadata, @Nullable Object> consumer) {

        if (sourceValue == null) {
            return;
        }
        final List<BeanPropertyMetadata> derivedFields = beanMetadata.getDerivedBySource().get(sourceName);
        if (derivedFields == null) {
            return;
        }
        for (final BeanPropertyMetadata derived : derivedFields) {
            final FieldTransformer<?, ?> transformer = derived.derivedTransformer();
            if (transformer == null) {
                continue;
            }
            try {
                final Object derivedValue = readUnchecked(transformer, sourceValue);
                consumer.accept(derived, derivedValue);
            } catch (Exception e) {
                log.warn("Failed to transform derived field [{}] from source [{}]", derived.name(), sourceName, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <S, T> @Nullable T readUnchecked(FieldTransformer<S, T> transformer, @Nullable Object sourceValue) {
        return transformer.read((S) sourceValue);
    }

    /**
     * 解析编码值：如有 reverseSource 派生字段，从派生字段取值并逆变换后返回；
     * 否则返回源字段的原始 Getter 值。
     *
     * @param sourceProperty 当前正在编码的源字段元数据
     * @param instance       实体实例
     * @param beanMetadata   当前 Bean 元数据
     * @return 实际应写入 ByteBuf 的值
     */
    public static @Nullable Object resolveEncodingValue(
            BeanPropertyMetadata sourceProperty,
            Object instance,
            BeanMetadata beanMetadata) {

        // 快速路径 大多数情况没有衍生字段
        if (!beanMetadata.hasDerivedFields()) {
            return sourceProperty.getProperty(instance);
        }
        final BeanPropertyMetadata reverseDerived = beanMetadata.getReverseDerivedBySource().get(sourceProperty.name());
        if (reverseDerived != null) {
            final Object derivedValue = reverseDerived.getProperty(instance);
            if (derivedValue != null) {
                final FieldTransformer<?, ?> transformer = reverseDerived.derivedTransformer();
                if (transformer != null) {
                    try {
                        return writeUnchecked(transformer, derivedValue);
                    } catch (UnsupportedOperationException e) {
                        log.debug("write() not supported for derived field [{}], using source value", reverseDerived.name());
                    }
                }
            }
        }
        return sourceProperty.getProperty(instance);
    }

    @SuppressWarnings("unchecked")
    private static <S, T> @Nullable S writeUnchecked(FieldTransformer<S, T> transformer, @Nullable Object derivedValue) {
        return transformer.write((T) derivedValue);
    }

    public static boolean isVersionMatched(int targetVersion, int[] versionCandidates) {
        boolean foundDefault = false;

        for (int v : versionCandidates) {
            if (v == targetVersion) {
                // 一旦发现目标版本，立即成功
                return true;
            } else if (v == XtreamField.ALL_VERSION) {
                // 仅记录默认版本存在
                foundDefault = true;
            }
        }

        // 无目标版本时，依赖默认版本兜底
        return foundDefault;
    }

}
