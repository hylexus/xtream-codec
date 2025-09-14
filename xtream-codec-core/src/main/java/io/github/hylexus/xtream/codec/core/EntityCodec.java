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

package io.github.hylexus.xtream.codec.core;

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.impl.DefaultFieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.impl.SimpleBeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

public class EntityCodec {
    private final EntityEncoder entityEncoder;
    private final EntityDecoder entityDecoder;
    public static EntityCodec DEFAULT = new EntityCodec(
            new SimpleBeanMetadataRegistry(
                    new DefaultFieldCodecRegistry(),
                    new XtreamCacheableClassPredicate.Default()
            )
    );

    public EntityCodec(BeanMetadataRegistry beanMetadataRegistry) {
        this.entityEncoder = new EntityEncoder(beanMetadataRegistry);
        this.entityDecoder = new EntityDecoder(beanMetadataRegistry);
    }

    public void encode(Object instance, ByteBuf target) {
        this.encode(XtreamField.DEFAULT_VERSION, instance, target);
    }

    public void encode(int version, Object instance, ByteBuf target) {
        this.entityEncoder.encode(version, instance, target);
    }

    public void encode(Object instance, ByteBuf target, CodecTracker tracker) {
        this.encode(XtreamField.DEFAULT_VERSION, instance, target, tracker);
    }

    public void encode(int version, Object instance, ByteBuf target, @Nullable CodecTracker tracker) {
        if (tracker == null) {
            this.entityEncoder.encode(version, instance, target);
        } else {
            this.entityEncoder.encodeWithTracker(version, instance, target, tracker);
        }
    }

    public <T> T decode(Class<T> entityClass, ByteBuf source) {
        return this.decode(XtreamField.DEFAULT_VERSION, entityClass, source);
    }

    public <T> T decode(int version, Class<T> entityClass, ByteBuf source) {
        return entityDecoder.decode(version, entityClass, source);
    }

    public <T> T decode(Class<T> entityClass, ByteBuf source, CodecTracker tracker) {
        return this.decode(XtreamField.DEFAULT_VERSION, entityClass, source, tracker);
    }

    public <T> T decode(int version, Class<T> entityClass, ByteBuf source, CodecTracker tracker) {
        if (tracker == null) {
            return entityDecoder.decode(version, entityClass, source);
        } else {
            return entityDecoder.decodeWithTracker(version, entityClass, source, tracker);
        }
    }

    public <T> T decode(T instance, ByteBuf source) {
        return this.decode(XtreamField.DEFAULT_VERSION, instance, source);
    }

    public <T> T decode(int version, T instance, ByteBuf source) {
        return entityDecoder.decode(version, source, instance);
    }

    public <T> T decode(T instance, ByteBuf source, CodecTracker tracker) {
        return this.decode(XtreamField.DEFAULT_VERSION, instance, source, tracker);
    }

    public <T> T decode(int version, T instance, ByteBuf source, CodecTracker tracker) {
        if (tracker == null) {
            return entityDecoder.decode(version, source, instance);
        } else {
            return entityDecoder.decodeWithTracker(version, source, instance, tracker);
        }
    }

    @SuppressWarnings("unused")
    public EntityDecoder entityDecoder() {
        return entityDecoder;
    }

    @SuppressWarnings("unused")
    public EntityEncoder entityEncoder() {
        return entityEncoder;
    }

}
