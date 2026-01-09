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

package io.github.hylexus.xtream.codec.ext.jt1078.spec;


import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.netty.buffer.ByteBuf;

/**
 * @author hylexus
 */
public interface Jt1078Request extends XtreamRequest {

    Jt1078RequestHeader header();

    /**
     * 请求体
     */
    default ByteBuf body() {
        return this.payload();
    }

    @Override
    default void release() {
        XtreamBytes.releaseBuf(this.body());
    }

    /**
     * @deprecated JT/T 1078 中没必要实现这个方法
     */
    @Override
    @Deprecated
    default Jt1078RequestBuilder mutate() {
        throw new UnsupportedOperationException();
    }

    // short-cut methods
    default String sim() {
        return header().convertedSim();
    }

    default short channelNumber() {
        return header().channelNumber();
    }

    default int messageBodyLength() {
        return header().msgBodyLength();
    }

    default Jt1078PayloadType payloadType() {
        return header().payloadType();
    }

    default Jt1078DataType dataType() {
        return header().dataType();
    }

    interface Jt1078RequestBuilder extends XtreamRequest.XtreamRequestBuilder {

        Jt1078RequestBuilder header(Jt1078RequestHeader header);

        @Override
        default Jt1078RequestBuilder payload(ByteBuf payload) {
            return this.payload(payload, true);
        }

        @Override
        Jt1078RequestBuilder payload(ByteBuf payload, boolean autoRelease);

        default Jt1078RequestBuilder body(ByteBuf body, boolean autoRelease) {
            return this.payload(body, autoRelease);
        }

        default Jt1078RequestBuilder body(ByteBuf body) {
            return this.payload(body);
        }

        @Override
        Jt1078Request build();

    }
}
