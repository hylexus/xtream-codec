/*
 * Copyright (c) 2024 xtream-codec
 * xtream-codec is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package io.github.hylexus.xtream.codec.common.bean.impl;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.FieldCodec;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SequenceBeanPropertyMetadata extends BasicBeanPropertyMetadata {
    private final NestedBeanPropertyMetadata nestedBeanPropertyMetadata;
    private final BeanPropertyMetadata delegate;

    public SequenceBeanPropertyMetadata(BeanPropertyMetadata delegate, NestedBeanPropertyMetadata metadata) {
        super(delegate.name(), delegate.rawClass(), delegate.field(), delegate.propertyGetter(), delegate.propertySetter());
        this.nestedBeanPropertyMetadata = metadata;
        this.delegate = delegate;
    }

    @Override
    public void encodePropertyValue(FieldCodec.SerializeContext context, ByteBuf output, Object value) {
        @SuppressWarnings("unchecked") final Collection<Object> collection = (Collection<Object>) value;
        for (final Object object : collection) {
            nestedBeanPropertyMetadata.encodePropertyValue(context, output, object);
        }
    }

    @Override
    public Object decodePropertyValue(FieldCodec.DeserializeContext context, ByteBuf input) {
        final int length = delegate.fieldLengthExtractor().extractFieldLength(context, context.evaluationContext());

        final ByteBuf slice = length < 0
                ? input // all remaining
                : input.readSlice(length);

        final List<Object> list = new ArrayList<>();
        while (slice.isReadable()) {
            final Object value = nestedBeanPropertyMetadata.decodePropertyValue(context, slice);
            list.add(value);
        }
        return list;
    }
}
