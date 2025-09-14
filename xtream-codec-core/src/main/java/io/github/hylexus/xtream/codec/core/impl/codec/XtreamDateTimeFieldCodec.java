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

package io.github.hylexus.xtream.codec.core.impl.codec;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.common.exception.XtreamWrappedRuntimeException;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.github.hylexus.xtream.codec.core.annotation.XtreamDateTimeField;
import io.netty.buffer.ByteBuf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class XtreamDateTimeFieldCodec extends AbstractFieldCodec<Object> {

    @Override
    protected void doSerialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, Object value) {
        final XtreamDateTimeField annotation = propertyMetadata.findAnnotation(XtreamDateTimeField.class).orElseThrow();
        final String bcdString = switch (value) {
            case LocalDateTime localDateTime -> DateTimeFormatter.ofPattern(annotation.pattern()).format(localDateTime);
            case LocalDate localDate -> DateTimeFormatter.ofPattern(annotation.pattern()).format(localDate);
            case LocalTime localTime -> DateTimeFormatter.ofPattern(annotation.pattern()).format(localTime);
            case Date date -> new SimpleDateFormat(annotation.pattern()).format(date);
            case String string -> string;
            default -> throw new IllegalArgumentException("Unsupported value type: " + value.getClass());
        };
        final FieldCodec<String> stringFieldCodec = StringFieldCodecs.createStringCodec(annotation.charset());
        stringFieldCodec.serialize(propertyMetadata, context, output, bcdString);
    }

    @Override
    public Object deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        final XtreamDateTimeField annotation = propertyMetadata.findAnnotation(XtreamDateTimeField.class).orElseThrow();

        final FieldCodec<String> stringFieldCodec = StringFieldCodecs.createStringCodec(annotation.charset());
        final String dateString = stringFieldCodec.deserialize(propertyMetadata, context, input, length);

        final Class<?> targetType = propertyMetadata.rawClass();
        if (targetType.equals(LocalDateTime.class)) {
            return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(annotation.pattern()));
        } else if (targetType.equals(LocalDate.class)) {
            return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(annotation.pattern()));
        } else if (targetType.equals(LocalTime.class)) {
            return LocalTime.parse(dateString, DateTimeFormatter.ofPattern(annotation.pattern()));
        } else if (targetType.equals(Date.class)) {
            try {
                return new SimpleDateFormat(annotation.pattern()).parse(dateString);
            } catch (ParseException e) {
                throw new XtreamWrappedRuntimeException(e);
            }
        } else if (targetType.equals(String.class)) {
            return dateString;
        } else {
            throw new IllegalArgumentException("Unsupported value type: " + targetType);
        }
    }
}
