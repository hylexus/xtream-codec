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

import io.github.hylexus.xtream.codec.core.annotation.XtreamEntityCreator;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;

import java.util.StringJoiner;

/**
 * @author hylexus
 * @see io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808MessageArgumentResolver
 */
public abstract class AbstractJt808Message {
    protected final XtreamInbound.Type protocolType;
    protected final String requestId;
    protected final String traceId;
    protected final Jt808ServerType serverType;
    protected final Jt808RequestHeader header;
    protected final int calculatedCheckSum;
    protected final int originalCheckSum;

    @XtreamEntityCreator
    public AbstractJt808Message(Jt808Request request) {
        this.protocolType = request.type();
        this.requestId = request.requestId();
        this.traceId = request.traceId();
        this.serverType = request.serverType();
        this.header = request.header();
        this.calculatedCheckSum = request.calculatedCheckSum();
        this.originalCheckSum = request.originalCheckSum();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AbstractJt808Message.class.getSimpleName() + "[", "]")
                .add("protocolType=" + protocolType)
                .add("requestId='" + requestId + "'")
                .add("traceId='" + traceId + "'")
                .add("serverType=" + serverType)
                .add("header=" + header)
                .add("calculatedCheckSum=" + calculatedCheckSum)
                .add("originalCheckSum=" + originalCheckSum)
                .toString();
    }
}
