/*
 * Copyright 2024 the original author or authors.
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

package io.github.hylexus.xtream.codec.ext.jt808.boot.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

@Endpoint(id = "jt808SimpleMetrics")
public class Jt808ServerSimpleMetricsEndpoint {
    private final Jt808ServerSimpleMetricsHolder collector;

    public Jt808ServerSimpleMetricsEndpoint(Jt808ServerSimpleMetricsHolder collector) {
        this.collector = collector;
    }

    @ReadOperation
    public Jt808ServerSimpleMetricsHolder all() {
        return collector;
    }
}
