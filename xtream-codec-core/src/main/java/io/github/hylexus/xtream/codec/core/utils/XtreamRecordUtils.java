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

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

/**
 * @author hylexus
 */
public final class XtreamRecordUtils {

    private XtreamRecordUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 查找 Record 类型的 canonical constructor（标准构造器）。
     *
     * @param recordClass 必须是 Record 类型
     * @return canonical constructor（标准构造器）
     * @throws IllegalArgumentException 如果传入的类不是 Record
     * @throws IllegalStateException    如果未能找到标准构造器
     */
    public static Constructor<?> findCanonicalRecordConstructor(Class<?> recordClass) {
        if (!recordClass.isRecord()) {
            throw new IllegalArgumentException(recordClass + " is not a Record");
        }

        final RecordComponent[] components = recordClass.getRecordComponents();
        // 构造器参数的类型、顺序、个数都必须与 RecordComponent 一致
        final Class<?>[] paramTypes = Arrays.stream(components)
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);

        try {
            return recordClass.getDeclaredConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Cannot find canonical constructor for record " + recordClass.getName(), e
            );
        }
    }
}
