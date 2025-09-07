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

package io.github.hylexus.xtream.codec.core.impl;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.NumberSignedness;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultFieldCodecRegistryTest {

    @InjectMocks
    private DefaultFieldCodecRegistry registry;

    @Mock
    private BeanPropertyMetadata metadata;

    @BeforeEach
    void setUp() {
        registry = new DefaultFieldCodecRegistry(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testInitWithEmptyRegistryShouldPopulateWithDefaultCodecs() {
        try (MockedStatic<BeanUtils> mockedBeanUtils = Mockito.mockStatic(BeanUtils.class)) {
            mockedBeanUtils.when(() -> BeanUtils.createNewInstance(any(Class.class), any())).thenReturn(mock(FieldCodec.class));
            mockedBeanUtils.when(() -> BeanUtils.createNewInstance(any(Constructor.class), any())).thenReturn(mock(FieldCodec.class));

            DefaultFieldCodecRegistry.init(registry);

            Optional<FieldCodec<?>> i8FieldCodec = registry.getFieldCodec(XtreamDataType.i8.sizeInBytes(), NumberSignedness.SIGNED, "", false, byte.class);
            assertTrue(i8FieldCodec.isPresent(), "I8FieldCodec should be present in the registry");

            Optional<FieldCodec<?>> stringFieldCodec = registry.getFieldCodec(-1, NumberSignedness.NONE, StandardCharsets.UTF_8.name(), false, String.class);
            assertTrue(stringFieldCodec.isPresent(), "StringFieldCodec should be present in the registry");
        }
    }

    @Test
    void testRegisterFieldCodec() {
        FieldCodec<?> fieldCodec = mock(FieldCodec.class);
        when(fieldCodec.signedness()).thenReturn(NumberSignedness.SIGNED);
        Class<?> targetType = Integer.class;
        int sizeInBytes = 4;
        String charset = "UTF-8";
        boolean littleEndian = false;

        registry.register(fieldCodec, targetType, sizeInBytes, charset, littleEndian);

        Optional<FieldCodec<?>> registeredCodec = registry.getFieldCodec(sizeInBytes, NumberSignedness.SIGNED, charset, littleEndian, targetType);
        assertTrue(registeredCodec.isPresent(), "FieldCodec should be registered");
        assertEquals(fieldCodec, registeredCodec.get(), "Registered codec should match the provided codec");
    }

    @Test
    void testGenerateKey() {
        Class<?> targetType = Integer.class;
        int sizeInBytes = 4;
        String charset = "UTF-8";
        boolean littleEndian = false;

        String key = registry.generateKey(NumberSignedness.UNSIGNED, targetType, sizeInBytes, charset, littleEndian);
        assertEquals("byte[4] <--> java.lang.Integer(BE)[Unsigned]", key, "Generated key should match expected format");
    }

    @Test
    void testGetFieldCodec() {
        Class<?> targetType = Integer.class;
        int sizeInBytes = 2;
        String charset = "UTF-8";
        boolean littleEndian = false;

        Optional<FieldCodec<?>> fieldCodec = registry.getFieldCodec(sizeInBytes, NumberSignedness.UNSIGNED, charset, littleEndian, targetType);
        assertTrue(fieldCodec.isPresent(), "FieldCodec should be present");
        assertNotNull(fieldCodec.get(), "FieldCodec should not be null");
    }

    @Test
    void testGetDefaultSizeInBytes() {
        XtreamField xtreamField = mock(XtreamField.class);
        when(xtreamField.length()).thenReturn(4);
        Class<?> rawClassType = Integer.class;

        int size = registry.getDefaultSizeInBytes(xtreamField, rawClassType);
        assertEquals(4, size, "Default size should match expected value");
    }

    @Test
    void testKey() {
        Class<?> targetType = Integer.class;
        int length = 4;
        String charset = "UTF-8";
        boolean littleEndian = true;

        String key = registry.key(NumberSignedness.UNSIGNED, targetType, length, charset, littleEndian);
        assertEquals("byte[4] (utf-8) <--> java.lang.Integer(LE)[Unsigned]", key, "Key should match expected format");
    }

}
