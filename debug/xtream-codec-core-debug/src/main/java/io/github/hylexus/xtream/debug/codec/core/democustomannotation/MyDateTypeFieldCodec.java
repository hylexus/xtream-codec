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

package io.github.hylexus.xtream.debug.codec.core.democustomannotation;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MyDateTypeFieldCodec implements FieldCodec<LocalDate> {

    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public LocalDate deserialize(BeanPropertyMetadata propertyMetadata, DeserializeContext context, ByteBuf input, int length) {
        // yyyyMMdd
        final CharSequence dateString = input.readCharSequence(8, StandardCharsets.UTF_8);
        return LocalDate.parse(dateString, formatter);
    }

    @Override
    public void serialize(BeanPropertyMetadata propertyMetadata, SerializeContext context, ByteBuf output, LocalDate value) {
        final String dateString = value.format(formatter);
        output.writeCharSequence(dateString, StandardCharsets.UTF_8);
    }
}
