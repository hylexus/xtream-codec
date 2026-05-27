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

package io.github.hylexus.xtream.debug.ext.jt808.config;

import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventPublisher;
import io.github.hylexus.xtream.debug.ext.jt808.domain.properties.DemoAppConfigProperties;
import io.github.hylexus.xtream.debug.ext.jt808.handler.DemoJt808RequestLifecycleListener;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author hylexus
 */
@NullMarked
@Configuration
@EnableConfigurationProperties({
        DemoAppConfigProperties.class,
})
public class DemoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DemoConfiguration.class);

    // @Bean
    Jt808RequestLifecycleListener demoJt808RequestLifecycleListener(XtreamEventPublisher eventPublisher) {
        return new DemoJt808RequestLifecycleListener(eventPublisher);
    }

    // @Bean
    public CommandLineRunner commandLineRunner(XtreamEventPublisher publisher) {
        return args -> {
            publisher.subscribeAll(Map.of("desc", "demo")).subscribe(event -> {
                log.info("Event ::: {}", event);
            });
        };
    }

    // @Bean(name = XtreamServerConstants.BEAN_NAME_HANDLER_ADAPTER_NON_BLOCKING_SCHEDULER)
    // Scheduler scheduler(){
    //     return Schedulers.newParallel("jt");
    // }
}
