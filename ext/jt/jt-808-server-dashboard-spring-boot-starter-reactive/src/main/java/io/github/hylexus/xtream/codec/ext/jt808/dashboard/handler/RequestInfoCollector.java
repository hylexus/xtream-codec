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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.handler;

import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageDescriptionRegistry;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.Jt808ServerSimpleMetricsHolder;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.SimpleTypes;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.netty.buffer.ByteBuf;
import reactor.netty.NettyInbound;

import java.util.concurrent.atomic.LongAdder;

public class RequestInfoCollector implements Jt808RequestLifecycleListener {
    private final Jt808ServerSimpleMetricsHolder metricsHolder;
    private final Jt808MessageDescriptionRegistry descriptionRegistry;

    public RequestInfoCollector(Jt808ServerSimpleMetricsHolder metricsHolder, Jt808MessageDescriptionRegistry descriptionRegistry) {
        this.metricsHolder = metricsHolder;
        this.descriptionRegistry = descriptionRegistry;
    }

    @Override
    public void afterRequestDecoded(NettyInbound nettyInbound, ByteBuf rawPayload, Jt808Request request) {

        final SimpleTypes.RequestInfo requestInfo = switch (request.type()) {
            case TCP -> switch (request.serverType()) {
                case INSTRUCTION_SERVER -> this.metricsHolder.getTcpInstructionRequest();
                case ATTACHMENT_SERVER -> this.metricsHolder.getTcpAttachmentRequest();
            };
            case UDP -> switch (request.serverType()) {
                case INSTRUCTION_SERVER -> this.metricsHolder.getUdpInstructionRequest();
                case ATTACHMENT_SERVER -> this.metricsHolder.getUdpAttachmentRequest();
            };
        };

        final int messageId = request.header().messageId();
        requestInfo.incrementTotal();
        requestInfo
                .details()
                .computeIfAbsent("message_" + messageId, k -> {
                    final String description = this.descriptionRegistry.getDescription(messageId);
                    return new SimpleTypes.RequestDetail(messageId, new LongAdder(), description == null ? "Unknown" : description);
                })
                .incrementCount();
    }

}
