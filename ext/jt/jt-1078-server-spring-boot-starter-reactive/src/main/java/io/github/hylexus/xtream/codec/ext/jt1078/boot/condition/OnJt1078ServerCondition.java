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

package io.github.hylexus.xtream.codec.ext.jt1078.boot.condition;

import jakarta.annotation.Nonnull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

class OnJt1078ServerCondition implements Condition {

    @Override
    public boolean matches(@Nonnull ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
        final Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(ConditionalOnJt1078Server.class.getName());
        if (annotationAttributes == null || annotationAttributes.isEmpty()) {
            return false;
        }

        final ConditionalOnJt1078Server.ProtocolType protocolType = (ConditionalOnJt1078Server.ProtocolType) annotationAttributes.get("protocolType");
        final Environment environment = context.getEnvironment();
        final boolean tcpServerEnabled = environment.getProperty("jt1078-server.tcp-server.enabled", Boolean.class, true);
        final boolean udpServerEnabled = environment.getProperty("jt1078-server.udp-server.enabled", Boolean.class, true);
        return switch (protocolType) {
            case TCP -> tcpServerEnabled;
            case UDP -> udpServerEnabled;
            case ANY -> tcpServerEnabled || udpServerEnabled;
            case NONE -> !tcpServerEnabled && !udpServerEnabled;
            // 不应该执行到这里
            case null -> throw new IllegalStateException();
        };
    }

}
