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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.boot.configuration;

import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardMetricsServiceWithMicroMeter;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.impl.DefaultJt808DashboardMetricsServiceWithMicroMeterImpl;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author hylexus
 */
@ConditionalOnClass(name = {
        "io.micrometer.core.instrument.MeterRegistry",
        "reactor.core.observability.micrometer.Micrometer"
})
public class BuiltinJt808DashboardConfigurationWithMicroMeter {

    @Bean
    @ConditionalOnMissingBean
    MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    Jt808DashboardMetricsServiceWithMicroMeter jt808DashboardMetricsServiceWithMicroMeter(
            XtreamSchedulerRegistry schedulerRegistry,
            MeterRegistry meterRegistry) {
        return new DefaultJt808DashboardMetricsServiceWithMicroMeterImpl(schedulerRegistry, meterRegistry);
    }

}
