/*
 * Copyright 2024 the original author or authors.
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

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hylexus
 */
public final class XtreamFieldUtils {

    private XtreamFieldUtils() {
        throw new UnsupportedOperationException();
    }

    // 缓存：Key = AnnotatedElement, Value = List<XtreamField>
    private static final Map<AnnotatedElement, List<XtreamField>> CACHE = new ConcurrentHashMap<>();

    /**
     * 获取 XtreamField 注解列表，如果没有注解则返回包含默认代理实例的列表
     */
    public static List<XtreamField> getOrDefault(AnnotatedElement element) {
        return CACHE.computeIfAbsent(element, e -> {
            final List<XtreamField> fields = resolveAnnotations(e);
            if (fields.isEmpty()) {
                return List.of(generateTransientFieldProxyInstance(element));
            }
            return fields;
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
            final List<MergedAnnotation<XtreamField>> list =
                    annotations.stream(XtreamField.class).toList();
            for (MergedAnnotation<XtreamField> ann : list) {
                resultList.add(ann.synthesize());
            }
            return Collections.unmodifiableList(resultList);
        }
        return Collections.emptyList();
    }

    private static XtreamField generateTransientFieldProxyInstance(AnnotatedElement element) {
        final Class<?> type;
        if (element instanceof Field) {
            type = ((Field) element).getType();
        } else if (element instanceof RecordComponent) {
            type = ((RecordComponent) element).getType();
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
                // 注意这里: 返回值代表不参与序列化 和 反序列化
                case "codecStrategy" -> XtreamField.CodecStrategy.TRANSIENT;
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
                result += (127 * name.hashCode()) ^ Objects.hashCode(value);
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
}
