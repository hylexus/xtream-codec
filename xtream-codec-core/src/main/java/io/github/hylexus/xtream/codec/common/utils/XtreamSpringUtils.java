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

package io.github.hylexus.xtream.codec.common.utils;


import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;

/**
 * @author hylexus
 */
@NullMarked
public final class XtreamSpringUtils {

    private XtreamSpringUtils() {
        throw new UnsupportedOperationException("No instance");
    }

    private static final List<PropertyAccessor> STANDARD_EVALUATION_CONTEXT_PROPERTY_ACCESSORS = List.of(
            // 以访问普通对象的风格访问 Map(mapObj.key.attr1.att2)
            new MapAccessor(),
            new ReflectivePropertyAccessor()
    );

    public static EvaluationContext createEvaluationContext(@Nullable Object rootObject) {
        // 1. rootObject
        final StandardEvaluationContext context = new StandardEvaluationContext(rootObject);
        context.setPropertyAccessors(STANDARD_EVALUATION_CONTEXT_PROPERTY_ACCESSORS);
        // 2. #self
        // todo 命名待定 #this, #self, 命名冲突的情况?
        context.setVariable("self", rootObject);
        return context;
    }

}
