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

package io.github.hylexus.xtream.codec.ext.jt808.boot.configuration;

import io.github.hylexus.xtream.codec.ext.jt808.boot.actuator.BuiltinJt808ServerEndpoint;
import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * @author hylexus
 */
@ConditionalOnClass(name = {
        "org.springframework.http.HttpStatus",
        "org.springframework.boot.actuate.endpoint.annotation.Endpoint"
})
@ConditionalOnAvailableEndpoint(endpoint = BuiltinJt808ServerEndpoint.class)
public class BuiltinJt808ServerActuatorConfiguration {

    @Bean
    BuiltinJt808ServerEndpoint builtinJt808ServerEndpoint(XtreamJt808ServerProperties serverProperties) {
        return new BuiltinJt808ServerEndpoint(serverProperties);
    }

}
