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

package io.github.hylexus.xtream.codec.ext.jt808.spec;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.netty.buffer.ByteBuf;

public interface Jt808Request extends XtreamRequest {

    /**
     * 同一次请求和响应 该值应该确保一致（包括分包消息）。
     *
     * @see #requestId()
     */
    String traceId();

    Jt808ServerType serverType();

    Jt808RequestHeader header();

    default int messageId() {
        return this.header().messageId();
    }

    default String terminalId() {
        return this.header().terminalId();
    }

    default ByteBuf body() {
        return this.payload();
    }

    int calculatedCheckSum();

    int originalCheckSum();

    @Override
    Jt808RequestBuilder mutate();

    interface Jt808RequestBuilder extends XtreamRequestBuilder {

        Jt808RequestBuilder traceId(String traceId);

        Jt808RequestBuilder header(Jt808RequestHeader header);

        @Override
        default Jt808RequestBuilder payload(ByteBuf payload) {
            return this.payload(payload, true);
        }

        @Override
        Jt808RequestBuilder payload(ByteBuf payload, boolean autoRelease);

        default Jt808RequestBuilder body(ByteBuf body, boolean autoRelease) {
            return this.payload(body, autoRelease);
        }

        default Jt808RequestBuilder body(ByteBuf body) {
            return this.payload(body);
        }

        Jt808RequestBuilder calculatedCheckSum(Integer calculatedCheckSum);

        Jt808RequestBuilder originalCheckSum(Integer originalCheckSum);

        @Override
        Jt808Request build();
    }
}
