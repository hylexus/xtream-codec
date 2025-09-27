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

package io.github.hylexus.xtream.codec.core.tracker;

import io.github.hylexus.xtream.codec.common.bean.BeanPropertyMetadata;
import io.github.hylexus.xtream.codec.core.FieldCodec;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.function.BiConsumer;

public class CodecTracker {
    private final RootSpan root;
    private BaseSpan current;
    private MapEntryItemSpan.Type tempMapItemType;

    public CodecTracker() {
        final RootSpan rootSpan = new RootSpan();
        this.current = rootSpan;
        this.root = rootSpan;
    }

    public RootSpan getRootSpan() {
        return this.root;
    }

    public void updateTrackerHints(MapEntryItemSpan.Type type) {
        this.tempMapItemType = type;
    }

    public NestedFieldSpan startNewNestedFieldSpan(BeanPropertyMetadata metadata, FieldCodec<?> fieldCodec, String fieldType) {
        final String fieldCodecString = fieldCodec == null
                ? null
                : fieldCodec.getClass().getSimpleName();
        return this.startNewNestedFieldSpan(metadata, fieldCodecString, fieldType);
    }

    public NestedFieldSpan startNewNestedFieldSpan(BeanPropertyMetadata metadata, String fieldCodec, String fieldType) {
        final NestedFieldSpan span = new NestedFieldSpan(
                this.current,
                metadata.name(), metadata.xtreamFieldAnnotation().desc(),
                metadata.field().getType().getTypeName(),
                fieldCodec);
        if (fieldType != null) {
            span.setFieldType(fieldType);
        } else if (this.current instanceof CollectionItemSpan collectionItemSpan) {
            final String fieldName = collectionItemSpan.getFieldName() + "(" + collectionItemSpan.getOffset() + ")";
            span.setFieldName(fieldName);
            span.setFieldType(collectionItemSpan.getFieldType());
        }
        return this.addSpan(span);
    }

    public CollectionFieldSpan startNewCollectionFieldSpan(BeanPropertyMetadata metadata) {
        final CollectionFieldSpan span = new CollectionFieldSpan(this.current, metadata.name(), this.getFieldFirstGenericTypeName(metadata.field()), metadata.xtreamFieldAnnotation().desc());
        return this.addSpan(span);
    }

    public CollectionItemSpan startNewCollectionItemSpan(BaseSpan parent, String fieldName, int sequence) {
        final String fieldType = this.current instanceof CollectionFieldSpan collectionFieldSpan
                ? collectionFieldSpan.getFieldType()
                : null;
        final CollectionItemSpan span = new CollectionItemSpan(parent, fieldName, sequence, fieldType);
        return this.addSpan(span);
    }

    public MapFieldSpan startNewMapFieldSpan(BeanPropertyMetadata metadata, String fieldCodec) {
        final MapFieldSpan span = new MapFieldSpan(this.current, metadata.name(), metadata.xtreamFieldAnnotation().desc(), fieldCodec);
        return this.addSpan(span);
    }

    public MapEntrySpan startNewMapEntrySpan(BaseSpan parent, String fieldName, int sequence) {
        final MapEntrySpan mapEntrySpan = new MapEntrySpan(parent, fieldName, sequence);
        return this.addSpan(mapEntrySpan);
    }

    public void finishCurrentSpan() {
        this.current = this.current.getParent();
    }

    public PrependLengthFieldSpan addPrependLengthFieldSpan(BaseSpan parent, String fieldName, Object value, String hexString, String fieldCodec, String fieldDesc) {
        final PrependLengthFieldSpan span = new PrependLengthFieldSpan(parent, fieldName, fieldCodec, value, hexString, fieldDesc);
        this.current.addChild(span);
        this.current = parent;
        return span;
    }

    public MapEntryItemSpan startNewMapEntryItemSpan(BaseSpan parent, MapEntryItemSpan.Type type, FieldCodec<?> fieldCodec) {
        final MapEntryItemSpan span = new MapEntryItemSpan(parent, type);
        span.setFieldCodec(fieldCodec.getClass().getSimpleName());
        this.current.addChild(span);
        this.current = span;
        return span;
    }

    public void addFieldSpan(BaseSpan parent, String fieldName, Object value, String hexString, FieldCodec<?> fieldCodec, String fieldDesc) {
        final BaseSpan trackerItem;
        if (parent instanceof MapEntrySpan) {
            trackerItem = new MapEntryItemSpan(parent, this.tempMapItemType)
                    .setFieldCodec(fieldCodec.getClass().getSimpleName())
                    .setValue(value)
                    .setHexString(hexString);
            this.tempMapItemType = null;
        } else {
            trackerItem = new BasicFieldSpan(parent, fieldName, fieldDesc)
                    .setFieldCodec(fieldCodec.getClass().getSimpleName())
                    .setValue(value)
                    .setHexString(hexString);
        }
        this.current.addChild(trackerItem);
        this.current = parent;
    }

    public BaseSpan getCurrentSpan() {
        return this.current;
    }

    protected <T extends BaseSpan> T addSpan(T span) {
        this.current.addChild(span);
        this.current = span;
        return span;
    }

    public void visit() {
        this.visit((level, span) -> {
            // ...
            System.out.println(("\t".repeat(level)) + "[==> " + level + "] " + span);
        });
    }

    public void visit(BiConsumer<Integer, BaseSpan> consumer) {
        visitTracker(0, this.root, consumer);
    }

    public static void visitTracker(int level, BaseSpan item, BiConsumer<Integer, BaseSpan> consumer) {
        consumer.accept(level, item);
        for (final BaseSpan child : item.getChildren()) {
            visitTracker(level + 1, child, consumer);
        }
    }

    private String getFieldFirstGenericTypeName(Field field) {
        if (field.getGenericType() instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getActualTypeArguments().length > 0) {
                return parameterizedType.getActualTypeArguments()[0].getTypeName();
            }
        }
        return "unknown";
    }
}
