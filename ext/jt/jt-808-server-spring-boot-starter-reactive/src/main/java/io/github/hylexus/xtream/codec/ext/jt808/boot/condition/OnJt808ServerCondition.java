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

package io.github.hylexus.xtream.codec.ext.jt808.boot.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

class OnJt808ServerCondition implements Condition {

    @Override
    @SuppressWarnings("checkstyle:indentation")
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(ConditionalOnJt808Server.class.getName());
        if (annotationAttributes == null || annotationAttributes.isEmpty()) {
            return false;
        }

        final ConditionalOnJt808Server.ServerType serverType = (ConditionalOnJt808Server.ServerType) annotationAttributes.get("serverType");
        final ConditionalOnJt808Server.ProtocolType protocolType = (ConditionalOnJt808Server.ProtocolType) annotationAttributes.get("protocolType");

        final Environment environment = context.getEnvironment();
        final boolean tcpInstructionServerEnabled = environment.getProperty("jt808-server.instruction-server.tcp-server.enabled", Boolean.class, true);
        final boolean udpInstructionServerEnabled = environment.getProperty("jt808-server.instruction-server.udp-server.enabled", Boolean.class, true);
        final boolean tcpAttachmentServerEnabled = environment.getProperty("jt808-server.attachment-server.tcp-server.enabled", Boolean.class, false);
        final boolean udpAttachmentServerEnabled = environment.getProperty("jt808-server.attachment-server.udp-server.enabled", Boolean.class, false);


        return switch (protocolType) {
            // tcp
            case TCP -> switch (serverType) {
                // tcp_instruction
                case INSTRUCTION_SERVER -> tcpInstructionServerEnabled;
                // tcp_attachment
                case ATTACHMENT_SERVER -> tcpAttachmentServerEnabled;
                // tcp_instruction || tcp_attachment
                case ANY -> tcpInstructionServerEnabled || tcpAttachmentServerEnabled;
                case NONE -> false;
            };
            // udp
            case UDP -> switch (serverType) {
                // udp_instruction
                case INSTRUCTION_SERVER -> udpInstructionServerEnabled;
                // udp_attachment
                case ATTACHMENT_SERVER -> udpAttachmentServerEnabled;
                // udp_instruction || udp_attachment
                case ANY -> udpInstructionServerEnabled || udpAttachmentServerEnabled;
                case NONE -> false;
            };
            // tcp || udp
            case ANY -> {
                final boolean instructionServerEnabled = tcpInstructionServerEnabled || udpInstructionServerEnabled;
                final boolean attachmentServerEnabled = tcpAttachmentServerEnabled || udpAttachmentServerEnabled;

                yield switch (serverType) {
                    // tcp_instruction || udp_instruction
                    case INSTRUCTION_SERVER -> instructionServerEnabled;
                    // tcp_attachment || udp_attachment
                    case ATTACHMENT_SERVER -> attachmentServerEnabled;
                    // tcp_instruction || tcp_attachment || udp_instruction || udp_attachment
                    case ANY -> attachmentServerEnabled || instructionServerEnabled;
                    // (tcp || udp) && (!instruction && !attachment)
                    case NONE -> false;
                };
            }
            // !tcp && !udp
            case NONE -> false;
        };
    }

}
