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

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnJt1078ServerCondition.class)
public @interface ConditionalOnJt1078Server {

    ProtocolType protocolType();

    enum ProtocolType {
        /**
         * {@code jt1078-server.tcp-server.enabled == true}
         */
        TCP,
        /**
         * {@code jt1078-server.udp-server.enabled == true}
         */
        UDP,
        /**
         * {@code jt1078-server.tcp-server.enabled == true || jt1078-server.udp-server.enabled == true}
         */
        ANY,
        /**
         * {@code jt1078-server.tcp-server.enabled == false && jt1078-server.udp-server.enabled == false}
         */
        NONE,
    }
}
