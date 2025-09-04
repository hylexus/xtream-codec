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

package io.github.hylexus.xtream.codec;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;

import java.lang.annotation.*;
import java.lang.reflect.Field;

public class AnnotationTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface CodecFieldContainer {
        CodecField[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface CodecFieldRustContainer {
        CodecFieldRust[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface CodecFieldJtContainer {
        CodecFieldJt[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface CodecFieldJt11Container {
        CodecFieldJt11[] value();
    }

    @Repeatable(CodecFieldContainer.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    public @interface CodecField {
        String type();

        int length();

        int[] version() default {0};
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @Repeatable(CodecFieldRustContainer.class)
    // 必须添加这行！作为元注解
    @CodecField(type = "", length = 0)
    public @interface CodecFieldRust {
        @AliasFor(annotation = CodecField.class, attribute = "type")
        String type();

        @AliasFor(annotation = CodecField.class, attribute = "length")
        int length();

        @AliasFor(annotation = CodecField.class, attribute = "version")
        int[] version() default {0};
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @Repeatable(CodecFieldJtContainer.class)
    // 必须添加这行！作为元注解
    @CodecField(type = "", length = 0)
    public @interface CodecFieldJt {
        @AliasFor(annotation = CodecField.class, attribute = "type")
        String type();

        @AliasFor(annotation = CodecField.class, attribute = "length")
        int length();

        @AliasFor(annotation = CodecField.class, attribute = "version")
        int[] version() default {2222};
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @Repeatable(CodecFieldJt11Container.class)
    // 必须添加这行！作为元注解
    @CodecFieldJt(type = "", length = 0)
    public @interface CodecFieldJt11 {
        @AliasFor(annotation = CodecFieldJt.class, attribute = "type")
        String type();

        @AliasFor(annotation = CodecFieldJt.class, attribute = "length")
        int length();

        @AliasFor(annotation = CodecFieldJt.class, attribute = "version")
        int[] version() default {666666};
    }

    public static class Person {
        private String field0;

        @CodecField(type = "type_original_111", length = 111111)
        @CodecField(type = "type_original_222", length = 111222, version = {2, 3, 4})
        private String field1;

        @CodecFieldRust(type = "type_rust_111", length = 222111, version = 1)
        @CodecFieldRust(type = "type_rust_222", length = 222222, version = 2)
        @CodecFieldRust(type = "type_rust_333", length = 222333, version = 3)
        private String field2;

        @CodecFieldJt(type = "type_jt_111", length = 333111, version = 1)
        @CodecFieldJt(type = "type_jt_222", length = 333222, version = 2)
        @CodecFieldJt(type = "type_jt_333", length = 333333, version = 3)
        @CodecFieldJt(type = "type_jt_333", length = 333444)
        private String field3;

        @CodecFieldJt11(type = "type_jt11_111", length = 444111, version = 1)
        @CodecFieldJt11(type = "type_jt11_222", length = 444222, version = 2)
        @CodecFieldJt11(type = "type_jt11_333", length = 444333, version = 3)
        @CodecFieldJt11(type = "type_jt11_333", length = 444444)
        private String field4;
    }

    @Test
    void test() throws Exception {

        for (final Field field : Person.class.getDeclaredFields()) {
            System.out.println(field);
            final MergedAnnotations mergedAnnotations = MergedAnnotations.from(field);
            for (final MergedAnnotation<Annotation> mergedAnnotation : mergedAnnotations) {
                final Class<Annotation> type = mergedAnnotation.getType();
                // System.out.println("\t" + type);
                // System.out.println("\t" + type.equals(CodecField.class));
                if (type.equals(CodecField.class)) {
                    final Annotation annotation = mergedAnnotation.synthesize();
                    final CodecField codecField = (CodecField) annotation;
                    System.out.println("\t" + codecField);
                }
            }
        }
    }

}
