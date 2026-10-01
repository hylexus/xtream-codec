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

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLength;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtension;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtensionRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 已注册格式扩展和配置校验测试。
 * 演示宿主注册的双向转换，并确保类名和未注册标识不能绕过注册表。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class DynamicExtensionTest {
    private static final DynamicExtensionRegistry REGISTRY = new DynamicExtensionRegistry(Map.of("date", new DynamicExtension() {
        @Override
        public void validate(Map<String, Object> config) {
            if (!"iso".equals(config.get("format"))) {
                throw new IllegalArgumentException("只接受 iso 格式");
            }
        }

        @Override
        public byte[] encode(Object value, Map<String, Object> config) {
            return LocalDate.parse(value.toString()).toString().getBytes(StandardCharsets.US_ASCII);
        }

        @Override
        public Object decode(byte[] bytes, Map<String, Object> config) {
            return LocalDate.parse(new String(bytes, StandardCharsets.US_ASCII)).toString();
        }
    }));

    @Test
    void shouldRoundTripRegisteredFormat() {
        final DynamicSchema schema = DynamicSchema.of(DynamicField.field("date", "extension")
                .length(DynamicLength.fixed(10, "byte")).options(Map.of("extensionId", "date", "format", "iso")).build());
        final DynamicPlan plan = DynamicCompiler.compile(schema, REGISTRY);
        final ByteBuf buffer = Unpooled.buffer();
        try {
            plan.encode(DynamicValues.of(Map.of("date", "2026-09-26")), buffer);
            assertEquals("2026-09-26", plan.decode(buffer).values().fields().get("date"));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldRejectUnregisteredClassNameAndInvalidConfiguration() {
        final DynamicField unknown = DynamicField.field("date", "extension").length(DynamicLength.fixed(10, "byte"))
                .options(Map.of("extensionId", "java.lang.Runtime")).build();
        assertThrows(DynamicCodecException.class, () -> DynamicCompiler.compile(DynamicSchema.of(unknown), REGISTRY));
        final DynamicField invalid = DynamicField.field("date", "extension").length(DynamicLength.fixed(10, "byte"))
                .options(Map.of("extensionId", "date", "format", "invalid")).build();
        final DynamicCodecException error = assertThrows(DynamicCodecException.class,
                () -> DynamicCompiler.compile(DynamicSchema.of(invalid), REGISTRY));
        assertEquals("$.date", error.fieldPath());
    }
}
