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

package io.github.hylexus.xtream.codec.core.debugger;

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicCompileOptions;
import io.github.hylexus.xtream.codec.core.dynamic.engine.DynamicLimits;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicField;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchema;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchemaJson;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 由 testWithoutJackson 任务执行，验证运行时没有 Jackson 实现的依赖边界。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class JacksonOptionalDependencyTest {
    @Test
    void shouldReadJsonSchemaWithoutJacksonImplementation() throws Exception {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("com.fasterxml.jackson.databind.ObjectMapper"));
        assertThrows(ClassNotFoundException.class, () -> Class.forName("com.fasterxml.jackson.core.JsonFactory"));
        assertNotNull(Class.forName("com.fasterxml.jackson.annotation.JsonValue"));
        assertTrue(DynamicSchemaJson.jsonSchema().contains("https://json-schema.org/draft/2020-12/schema"));
    }

    @Test
    void shouldRoundTripEntityWithoutJacksonImplementation() {
        final var sample = new Sample();
        sample.setNumber(0x1234);
        final var buffer = Unpooled.buffer();
        try {
            EntityCodec.DEFAULT.encode(sample, buffer);
            assertEquals("1234", ByteBufUtil.hexDump(buffer));
            assertEquals(sample.getNumber(), EntityCodec.DEFAULT.decode(Sample.class, buffer).getNumber());
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldExecuteBothModesWithCustomValueMapperWithoutJacksonImplementation() {
        final DebugEntityValueMapper valueMapper = new DebugEntityValueMapper() {
            @Override
            public <T> T toEntity(Map<String, Object> values, Class<T> entityType) {
                final var sample = new Sample();
                sample.setNumber(assertInstanceOf(Number.class, values.get("number")).intValue());
                return entityType.cast(sample);
            }

            @Override
            public Map<String, Object> toValues(Object entity) {
                return Map.of("number", assertInstanceOf(Sample.class, entity).getNumber());
            }
        };
        final var service = new CodecDebuggerService(new DebugEntityCatalog(Map.of("sample", Sample.class)),
                valueMapper, DynamicCompileOptions.DEFAULT, DynamicLimits.DEFAULT);
        assertEquals(Map.of("number", 0), service.entityTemplate("sample"));
        final var schema = DynamicSchema.of(DynamicField.field("number", "u16").build());
        for (final var mode : DebugCodecMode.values()) {
            final var encoded = service.execute(new DebugCodecRequest(mode, DebugCodecDirection.ENCODE,
                    "sample", null, schema, Map.of("number", 0x1234), null));
            assertTrue(encoded.success(), String.valueOf(encoded.error()));
            assertEquals("1234", encoded.hex());
            final var decoded = service.execute(new DebugCodecRequest(mode, DebugCodecDirection.DECODE,
                    "sample", null, schema, null, "123455"));
            assertTrue(decoded.success(), String.valueOf(decoded.error()));
            assertEquals(encoded.values(), decoded.values());
            assertEquals(16, decoded.consumedBits());
            assertEquals("55", decoded.remainingHex());
            assertFalse(decoded.trace().nodes().isEmpty());
        }
    }

    public static class Sample {
        @Preset.RustStyle.u16(order = 0)
        private int number;

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }
}
