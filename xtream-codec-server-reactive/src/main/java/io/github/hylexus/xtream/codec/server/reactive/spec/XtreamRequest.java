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

package io.github.hylexus.xtream.codec.server.reactive.spec;

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.netty.buffer.ByteBuf;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * @author hylexus
 */
public interface XtreamRequest extends XtreamInbound {

    int version();

    Map<String, Object> attributes();

    default void release() {
        XtreamBytes.releaseBuf(this.payload());
    }

    XtreamRequestBuilder mutate();

    @SuppressWarnings("unchecked")
    default <T extends XtreamRequest> T castAs(Class<T> ignored) {
        return (T) this;
    }

    @Override
    String toString();

    interface XtreamRequestBuilder {

        default XtreamRequestBuilder payload(ByteBuf payload) {
            return this.payload(payload, true);
        }

        XtreamRequestBuilder version(int version);

        XtreamRequestBuilder payload(ByteBuf payload, boolean autoRelease);

        XtreamRequestBuilder remoteAddress(InetSocketAddress remoteAddress);

        XtreamRequest build();
    }
}
