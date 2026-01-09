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

package io.github.hylexus.xtream.debug.codec.core.demo04;

import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.XtreamCacheableClassPredicate;
import io.github.hylexus.xtream.codec.core.impl.DefaultFieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.impl.SimpleBeanMetadataRegistry;
import io.netty.buffer.ByteBuf;

public class SimpleSubPackageEntityCodec extends EntityCodec {

    final SimpleSubPackageEntityDecoder decoder;

    public SimpleSubPackageEntityCodec() {
        this(new SimpleBeanMetadataRegistry(
                new DefaultFieldCodecRegistry(),
                new XtreamCacheableClassPredicate.Default()
        ));
    }

    public SimpleSubPackageEntityCodec(BeanMetadataRegistry beanMetadataRegistry) {
        super(beanMetadataRegistry);
        this.decoder = new SimpleSubPackageEntityDecoder(beanMetadataRegistry);
    }

    @Override
    public <T> T decode(int version, T instance, ByteBuf source) {
        return this.decoder.decode(version, source, instance);
    }

    @Override
    public <T> T decode(Class<T> entityClass, ByteBuf source) {
        return this.decoder.decode(entityClass, source);
    }

    @Override
    public void encode(int version, Object instance, ByteBuf target) {
        // todo 实现支持分包的编码逻辑...
        super.encode(version, instance, target);
    }
}
