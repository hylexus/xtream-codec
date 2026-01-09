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

package io.github.hylexus.xtream.codec.ext.jt808.spec;

import io.github.hylexus.xtream.codec.core.annotation.OrderedComponent;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;
import reactor.netty.NettyInbound;

/**
 * @author hylexus
 * @see io.github.hylexus.xtream.codec.ext.jt808.extensions.listener.Jt808RequestLoggerListener#afterRequestDecoded(NettyInbound, ByteBuf, Jt808Request)
 * @see "io.github.hylexus.xtream.codec.ext.jt808.dashboard.handler.RequestInfoCollector#afterRequestDecoded(NettyInbound, ByteBuf, Jt808Request)"
 */
public interface Jt808MessageDescriptionRegistry {

    @Nullable String getDescription(int messageId);

    default String getDescription(int messageId, String def) {
        final String description = this.getDescription(messageId);
        return description == null ? def : description;
    }

    void registerDescription(int messageId, String description);

    void unregisterDescription(int messageId);

    interface Jt808MessageDescriptionRegistryCustomizer extends OrderedComponent {

        void customize(Jt808MessageDescriptionRegistry registry);

    }

}
