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

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;

import java.util.StringJoiner;

/**
 * @param <T> 消息体类型
 * @see io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestEntityArgumentResolver
 */
public class Jt808RequestEntity<T> {
    protected final XtreamInbound.Type protocolType;
    protected final String requestId;
    protected final String traceId;
    protected final Jt808ServerType serverType;
    protected final Jt808RequestHeader header;
    protected final T body;
    protected final int calculatedCheckSum;
    protected final int originalCheckSum;

    public Jt808RequestEntity(Jt808Request request, T body) {
        this.protocolType = request.type();
        this.requestId = request.requestId();
        this.traceId = request.traceId();
        this.serverType = request.serverType();
        this.header = request.header();
        this.body = body;
        this.calculatedCheckSum = request.calculatedCheckSum();
        this.originalCheckSum = request.originalCheckSum();
    }

    public String getRequestId() {
        return requestId;
    }

    public String getTraceId() {
        return traceId;
    }

    public Jt808ServerType getServerType() {
        return serverType;
    }

    public Jt808RequestHeader getHeader() {
        return header;
    }

    public XtreamInbound.Type getProtocolType() {
        return protocolType;
    }

    public T getBody() {
        return body;
    }

    public int getCalculatedCheckSum() {
        return calculatedCheckSum;
    }

    public int getOriginalCheckSum() {
        return originalCheckSum;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Jt808RequestEntity.class.getSimpleName() + "[", "]")
                .add("protocolType=" + protocolType)
                .add("requestId='" + requestId + "'")
                .add("traceId='" + traceId + "'")
                .add("serverType=" + serverType)
                .add("header=" + header)
                .add("body=" + body)
                .add("calculatedCheckSum=" + calculatedCheckSum)
                .add("originalCheckSum=" + originalCheckSum)
                .toString();
    }
}
