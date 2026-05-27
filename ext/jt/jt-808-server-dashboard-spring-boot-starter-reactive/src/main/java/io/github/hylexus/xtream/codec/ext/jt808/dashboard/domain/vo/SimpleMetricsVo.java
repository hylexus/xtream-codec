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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.vo;

import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.Jt808ServerSimpleMetricsHolder;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.SimpleTypes;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.XtreamServerErrorInfo;

import java.util.Collection;

public record SimpleMetricsVo(
        SimpleTypes.SessionInfo tcpInstructionSession,
        SimpleTypes.SessionInfo tcpAttachmentSession,
        SimpleTypes.SessionInfo udpInstructionSession,
        SimpleTypes.SessionInfo udpAttachmentSession,
        SimpleTypes.RequestInfo tcpInstructionRequest,
        SimpleTypes.RequestInfo tcpAttachmentRequest,
        SimpleTypes.RequestInfo udpInstructionRequest,
        SimpleTypes.RequestInfo udpAttachmentRequest,
        EventPublisherMetrics eventPublisher,
        SimpleTypes.SimpleJvmThreadMetrics threads,
        SimpleMetricsVo.ErrorMetrics error) {

    public SimpleMetricsVo(
            Jt808ServerSimpleMetricsHolder simpleMetricsHolder,
            long subscriberCount,
            SimpleTypes.SimpleJvmThreadMetrics threads,
            SimpleMetricsVo.ErrorMetrics error) {
        this(
                simpleMetricsHolder.getTcpInstructionSession(),
                simpleMetricsHolder.getTcpAttachmentSession(),
                simpleMetricsHolder.getUdpInstructionSession(),
                simpleMetricsHolder.getUdpAttachmentSession(),
                simpleMetricsHolder.getTcpInstructionRequest(),
                simpleMetricsHolder.getTcpAttachmentRequest(),
                simpleMetricsHolder.getUdpInstructionRequest(),
                simpleMetricsHolder.getUdpAttachmentRequest(),
                new EventPublisherMetrics(new EventSubscriberMetrics(subscriberCount)),
                threads,
                error
        );
    }

    public record EventPublisherMetrics(EventSubscriberMetrics subscriber) {
    }

    public record EventSubscriberMetrics(long total) {
    }

    public record ErrorMetrics(long total, Collection<XtreamServerErrorInfo> details) {
    }
}
