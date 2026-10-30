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
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicSchemaJson;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtension;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtensionRegistry;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 通过扩展字段验证动态配置的深层快照和只读约定。
 *
 * @author Codex (AI)
 */
class DynamicConfigurationSnapshotTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void originalNestedMutationsDoNotChangeCompiledExtension(boolean traced) {
        final Map<String, Object> rule = new LinkedHashMap<>(Map.of("offset", 1));
        final List<Object> steps = new ArrayList<>(List.of(rule));
        final Map<String, Object> options = new LinkedHashMap<>();
        options.put("extensionId", "offset");
        options.put("steps", steps);
        final DynamicField field = extensionField(options);

        rule.put("offset", 10);
        steps.add(Map.of("offset", 20));
        options.put("extensionId", "unregistered");
        final DynamicExtension extension = new DynamicExtension() {
            @Override
            public void validate(Map<String, Object> config) {
                assertEquals(1, offset(config));
                assertEquals(1, ((List<?>) config.get("steps")).size());
            }

            @Override
            public byte[] encode(Object value, Map<String, Object> config) {
                return new byte[]{(byte) (((Number) value).intValue() + offset(config))};
            }

            @Override
            public Object decode(byte[] bytes, Map<String, Object> config) {
                return (long) Byte.toUnsignedInt(bytes[0]) - offset(config);
            }
        };
        final DynamicPlan plan = DynamicCompiler.compile(DynamicSchema.of(field),
                new DynamicExtensionRegistry(Map.of("offset", extension)));
        rule.clear();
        steps.clear();
        options.clear();

        final ByteBuf buffer = Unpooled.buffer();
        try {
            buffer.writeByte(0x55);
            final DynamicValues values = DynamicValues.of(Map.of("value", 40));
            assertEquals(8, traced ? plan.encode(values, buffer, new CodecTracker()) : plan.encode(values, buffer));
            assertEquals("5529", ByteBufUtil.hexDump(buffer));
            buffer.readerIndex(1);
            final DynamicDecodeResult result = traced ? plan.decode(buffer, new CodecTracker()) : plan.decode(buffer);
            assertEquals(Map.of("value", 40L), result.values().fields());
            assertEquals(8, result.consumedBits());
            assertEquals(2, buffer.readerIndex());
        } finally {
            buffer.release();
        }
    }

    private static int offset(Map<String, Object> config) {
        final List<?> steps = (List<?>) config.get("steps");
        final Map<?, ?> rule = (Map<?, ?>) steps.getFirst();
        return ((Number) rule.get("offset")).intValue();
    }

    @Test
    void nestedSnapshotPreservesOrderNullsAndRejectsViewMutations() {
        final Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("last", 7);
        entry.put("first", null);
        final List<String> labels = new ArrayList<>(List.of("x"));
        final List<Object> items = new ArrayList<>(Arrays.asList(entry, null, labels));
        final Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("items", items);
        final Map<String, Object> source = new LinkedHashMap<>();
        source.put("extensionId", "offset");
        source.put("nested", nested);
        source.put("empty", null);
        final Map<String, Object> frozen = Objects.requireNonNull(extensionField(source).options());
        final Map<?, ?> frozenNested = (Map<?, ?>) frozen.get("nested");
        final List<?> frozenItems = (List<?>) frozenNested.get("items");
        final Map<?, ?> frozenEntry = (Map<?, ?>) frozenItems.getFirst();
        final List<?> frozenList = (List<?>) frozenItems.get(2);

        assertNotSame(source, frozen);
        assertNotSame(nested, frozenNested);
        assertNotSame(items, frozenItems);
        assertNotSame(entry, frozenEntry);
        assertNotSame(labels, frozenList);
        assertEquals(List.of("extensionId", "nested", "empty"), new ArrayList<>(frozen.keySet()));
        assertEquals(List.of("last", "first"), new ArrayList<>(frozenEntry.keySet()));
        assertTrue(frozen.containsKey("empty"));
        assertNull(frozen.get("empty"));
        assertTrue(frozenEntry.containsKey("first"));
        assertNull(frozenEntry.get("first"));
        assertNull(frozenItems.get(1));
        for (final Map<?, ?> map : List.of(frozen, frozenNested, frozenEntry)) {
            assertThrows(UnsupportedOperationException.class, map::clear);
            assertThrows(UnsupportedOperationException.class, () -> map.entrySet().iterator().next().setValue(null));
            assertThrows(UnsupportedOperationException.class, () -> map.keySet().remove(map.keySet().iterator().next()));
            assertThrows(UnsupportedOperationException.class, () -> map.values().remove(map.values().iterator().next()));
        }
        for (final List<?> list : List.of(frozenItems, frozenList)) {
            assertThrows(UnsupportedOperationException.class, () -> list.add(null));
            assertThrows(UnsupportedOperationException.class, () -> list.set(0, null));
            final Iterator<?> iterator = list.iterator();
            iterator.next();
            assertThrows(UnsupportedOperationException.class, iterator::remove);
        }

        entry.clear();
        labels.clear();
        items.clear();
        nested.clear();
        source.clear();
        assertEquals(7, frozenEntry.get("last"));
        assertEquals(3, frozenItems.size());
        assertEquals(List.of("x"), frozenList);
        final DynamicSchema schema = DynamicSchema.of(extensionField(frozen));
        assertEquals(schema, DynamicSchemaJson.read(DynamicSchemaJson.write(schema)));
    }

    @ParameterizedTest
    @MethodSource("jsonScalars")
    void preservesScalarValuesAndNumericTypes(@Nullable Object value) {
        final Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("value", value);
        final Map<String, Object> options = new LinkedHashMap<>();
        options.put("extensionId", "offset");
        options.put("value", value);
        options.put("nested", nested);
        options.put("items", Arrays.asList(value));
        final Map<String, Object> frozen = Objects.requireNonNull(extensionField(options).options());
        final Map<?, ?> frozenNested = (Map<?, ?>) frozen.get("nested");
        final List<?> frozenItems = (List<?>) frozen.get("items");
        assertEquals(value, frozen.get("value"));
        assertEquals(value, frozenNested.get("value"));
        assertEquals(value, frozenItems.getFirst());
        if (value != null) {
            assertEquals(value.getClass(), Objects.requireNonNull(frozenItems.getFirst()).getClass());
        }
    }

    static Stream<Arguments> jsonScalars() {
        return Stream.of(
                Arguments.of((Object) null), Arguments.of("中文"), Arguments.of(true),
                Arguments.of(123), Arguments.of(Long.MAX_VALUE), Arguments.of(1.25d), Arguments.of(1.5f),
                Arguments.of((short) 32767), Arguments.of((byte) -128),
                Arguments.of(new BigInteger("18446744073709551616")),
                Arguments.of(new BigDecimal("12345678901234567890.12345678901234567890")));
    }

    @ParameterizedTest
    @MethodSource("nonJsonOptions")
    void rejectsNonJsonObjectsAtAnyNestedPosition(Map<String, Object> options, String rejectedType) {
        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> extensionField(options));
        assertEquals("结构配置只接受 JSON 值: " + rejectedType, error.getMessage());
    }

    static Stream<Arguments> nonJsonOptions() {
        return Stream.of(new Object(), new byte[]{1}, new Object[]{"x"}, Set.of("x"))
                .flatMap(value -> Stream.of(value, Map.of("nested", value), List.of(value))
                        .map(config -> Arguments.of(Map.of("extensionId", "offset", "config", config), value.getClass().getName())));
    }

    private static DynamicField extensionField(Map<String, Object> options) {
        return DynamicField.field("value", "extension").length(DynamicLength.fixed(1, "byte")).options(options).build();
    }
}
