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

package io.github.hylexus.xtream.codec.common.bean.impl;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.bean.PropertySetters;
import org.jspecify.annotations.Nullable;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.StringJoiner;

/**
 * @deprecated Use {@link PropertySetters.ReflectionMethodPropertySetter} instead.
 */
@Deprecated(since = "0.2.0", forRemoval = true)
public class MethodPropertySetter implements BeanPropertyMetadata.PropertySetter {
    private final Method method;

    public MethodPropertySetter(Method method) {
        this.method = method;
    }

    @Override
    public void setProperty(BeanPropertyMetadata metadata, Object instance, @Nullable Object value) {
        ReflectionUtils.invokeMethod(method, instance, value);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MethodPropertySetter.class.getSimpleName() + "[", "]")
                .add("method=" + method)
                .toString();
    }

}
