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

package io.github.hylexus.xtream.codec.core.impl.codec;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.exception.XtreamWrappedRuntimeException;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamDateTimeField;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class XtreamDateTimeFieldCodecTest {

    private final XtreamDateTimeFieldCodec codec = new XtreamDateTimeFieldCodec();
    private final BeanPropertyMetadata metadata = mock(BeanPropertyMetadata.class);
    private final FieldCodec.SerializeContext serializeContext = mock(FieldCodec.SerializeContext.class);
    private final FieldCodec.DeserializeContext deserializeContext = mock(FieldCodec.DeserializeContext.class);

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldRoundTripSupportedTemporalTypesWithExactBcdAndAsciiBytes(boolean tracked) throws Exception {
        final TemporalMessage source = new TemporalMessage(
                LocalDateTime.of(2024, 10, 25, 20, 30, 40),
                LocalDate.of(2024, 10, 25),
                LocalTime.of(20, 30, 40),
                new SimpleDateFormat("yyyyMMddHHmmss").parse("20241025203040"),
                "20241025");
        final ByteBuf output = Unpooled.buffer();
        try {
            final EntityCodec entityCodec = EntityCodec.DEFAULT;
            entityCodec.encode(-1, source, output, tracked ? new CodecTracker() : null);
            assertEquals("2024102520304020241025203040202410252030403230323431303235", ByteBufUtil.hexDump(output));
            final TemporalMessage decoded = entityCodec.decode(-1, TemporalMessage.class, output, tracked ? new CodecTracker() : null);
            assertEquals(source, decoded);
            assertFalse(output.isReadable());
        } finally {
            output.release();
        }
    }

    @Test
    void shouldRejectUnsupportedValueAndTargetTypes() throws Exception {
        configureMetadata(Integer.class);
        final ByteBuf buffer = Unpooled.buffer();
        try {
            assertThrows(IllegalArgumentException.class, () -> codec.serialize(metadata, serializeContext, buffer, 42));
            assertEquals(0, buffer.writerIndex());
            buffer.writeBytes("20241025".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            assertThrows(IllegalArgumentException.class, () -> codec.deserialize(metadata, deserializeContext, buffer, 8));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldWrapLegacyDateParseFailureWithOriginalCause() throws Exception {
        configureMetadata(Date.class);
        final ByteBuf buffer = Unpooled.wrappedBuffer("invalid!".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        try {
            final XtreamWrappedRuntimeException failure = assertThrows(XtreamWrappedRuntimeException.class,
                    () -> codec.deserialize(metadata, deserializeContext, buffer, 8));
            assertInstanceOf(ParseException.class, failure.getCause());
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldRejectMalformedLocalDate() throws Exception {
        configureMetadata(LocalDate.class);
        final ByteBuf buffer = Unpooled.wrappedBuffer("20241325".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        try {
            assertThrows(java.time.format.DateTimeParseException.class,
                    () -> codec.deserialize(metadata, deserializeContext, buffer, 8));
        } finally {
            buffer.release();
        }
    }

    @Test
    void shouldSkipNullValues() {
        final ByteBuf buffer = Unpooled.buffer().writeByte(7);
        try {
            codec.serialize(metadata, serializeContext, buffer, null);
            assertEquals("07", ByteBufUtil.hexDump(buffer));
            verifyNoInteractions(metadata);
        } finally {
            buffer.release();
        }
    }

    private void configureMetadata(Class<?> targetType) throws Exception {
        when(metadata.findAnnotation(XtreamDateTimeField.class)).thenReturn(Optional.of(
                TemporalMessage.class.getDeclaredField("text").getAnnotation(XtreamDateTimeField.class)));
        doReturn(targetType).when(metadata).rawClass();
    }

    public record TemporalMessage(
            @XtreamDateTimeField(order = 0, charset = "BCD_8421", pattern = "yyyyMMddHHmmss", length = 7)
            LocalDateTime timestamp,
            @XtreamDateTimeField(order = 1, charset = "BCD_8421", pattern = "yyyyMMdd", length = 4)
            LocalDate date,
            @XtreamDateTimeField(order = 2, charset = "BCD_8421", pattern = "HHmmss", length = 3)
            LocalTime time,
            @XtreamDateTimeField(order = 3, charset = "BCD_8421", pattern = "yyyyMMddHHmmss", length = 7)
            Date legacyDate,
            @XtreamDateTimeField(order = 4, charset = "US-ASCII", pattern = "yyyyMMdd", length = 8)
            String text) {
    }
}
