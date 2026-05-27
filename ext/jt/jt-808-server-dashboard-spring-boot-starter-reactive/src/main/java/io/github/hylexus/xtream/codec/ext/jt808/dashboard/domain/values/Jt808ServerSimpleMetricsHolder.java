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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values;

@SuppressWarnings("all")
public class Jt808ServerSimpleMetricsHolder {
    private final SimpleTypes.SessionInfo tcpInstructionSession;
    private final SimpleTypes.SessionInfo tcpAttachmentSession;
    private final SimpleTypes.SessionInfo udpInstructionSession;
    private final SimpleTypes.SessionInfo udpAttachmentSession;
    private final SimpleTypes.RequestInfo tcpInstructionRequest;
    private final SimpleTypes.RequestInfo tcpAttachmentRequest;
    private final SimpleTypes.RequestInfo udpInstructionRequest;
    private final SimpleTypes.RequestInfo udpAttachmentRequest;

    public Jt808ServerSimpleMetricsHolder(
            SimpleTypes.SessionInfo tcpInstructionSession,
            SimpleTypes.SessionInfo tcpAttachmentSession,
            SimpleTypes.SessionInfo udpInstructionSession,
            SimpleTypes.SessionInfo udpAttachmentSession,
            SimpleTypes.RequestInfo tcpInstructionRequest,
            SimpleTypes.RequestInfo tcpAttachmentRequest,
            SimpleTypes.RequestInfo udpInstructionRequest,
            SimpleTypes.RequestInfo udpAttachmentRequest) {
        this.tcpInstructionSession = tcpInstructionSession;
        this.tcpAttachmentSession = tcpAttachmentSession;
        this.udpInstructionSession = udpInstructionSession;
        this.udpAttachmentSession = udpAttachmentSession;
        this.tcpInstructionRequest = tcpInstructionRequest;
        this.tcpAttachmentRequest = tcpAttachmentRequest;
        this.udpInstructionRequest = udpInstructionRequest;
        this.udpAttachmentRequest = udpAttachmentRequest;
    }

    public SimpleTypes.SessionInfo getTcpInstructionSession() {
        return tcpInstructionSession;
    }

    public SimpleTypes.SessionInfo getTcpAttachmentSession() {
        return tcpAttachmentSession;
    }

    public SimpleTypes.SessionInfo getUdpInstructionSession() {
        return udpInstructionSession;
    }

    public SimpleTypes.SessionInfo getUdpAttachmentSession() {
        return udpAttachmentSession;
    }

    public SimpleTypes.RequestInfo getTcpInstructionRequest() {
        return tcpInstructionRequest;
    }

    public SimpleTypes.RequestInfo getTcpAttachmentRequest() {
        return tcpAttachmentRequest;
    }

    public SimpleTypes.RequestInfo getUdpInstructionRequest() {
        return udpInstructionRequest;
    }

    public SimpleTypes.RequestInfo getUdpAttachmentRequest() {
        return udpAttachmentRequest;
    }


    @Override
    public String toString() {
        return "Jt808ServerSimpleMetricsHolder[" +
               "tcpInstructionSessions=" + tcpInstructionSession + ", " +
               "tcpAttachmentSessions=" + tcpAttachmentSession + ", " +
               "udpInstructionSessions=" + udpInstructionSession + ", " +
               "udpAttachmentSessions=" + udpAttachmentSession + ", " +
               "tcpInstructionRequests=" + tcpInstructionRequest + ", " +
               "tcpAttachmentRequests=" + tcpAttachmentRequest + ", " +
               "udpInstructionRequests=" + udpInstructionRequest + ", " +
               "udpAttachmentRequests=" + udpAttachmentRequest + ']';
    }


}
