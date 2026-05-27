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

package io.github.hylexus.xtream.codec.ext.jt808.processor;

import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistryCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author hylexus
 */
public class BuiltinJt808ServerCustomSchedulerRegistrar
        implements PriorityOrdered,
        BeanDefinitionRegistryPostProcessor,
        EnvironmentAware {

    private static final Logger log = LoggerFactory.getLogger(BuiltinJt808ServerCustomSchedulerRegistrar.class);
    @SuppressWarnings({"NullAway"})
    private Environment environment;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        final Map<String, XtreamJt808ServerProperties.NamedXtreamServerSchedulerProperties> customSchedulers = getCustomSchedulerConfig();
        if (CollectionUtils.isEmpty(customSchedulers)) {
            return;
        }
        for (final Map.Entry<String, XtreamJt808ServerProperties.NamedXtreamServerSchedulerProperties> entry : customSchedulers.entrySet()) {
            final String name = entry.getKey();
            final XtreamJt808ServerProperties.NamedXtreamServerSchedulerProperties config = entry.getValue();
            if (!StringUtils.hasText(config.getName())) {
                config.setName(name);
            }
            log.info("Registering custom scheduler: {}", config.getName());
            this.registerCustomScheduler(registry, config);
        }
    }

    private void registerCustomScheduler(BeanDefinitionRegistry registry, XtreamJt808ServerProperties.NamedXtreamServerSchedulerProperties config) {
        final XtreamSchedulerRegistryCustomizer schedulerCustomizer = new NamedDefaultJt808XtreamScheduleRegistryCustomizer(config);
        final BeanDefinition beanDefinition = BeanDefinitionBuilder
                .rootBeanDefinition(XtreamSchedulerRegistryCustomizer.class, () -> schedulerCustomizer)
                .setPrimary(false)
                .getBeanDefinition();
        registry.registerBeanDefinition("xtreamSchedulerCustomizer_" + config.getName(), beanDefinition);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public int getOrder() {
        return -1024;
    }

    private Map<String, XtreamJt808ServerProperties.NamedXtreamServerSchedulerProperties> getCustomSchedulerConfig() {
        final XtreamJt808ServerProperties.SchedulerProperties schedulerProperties = Binder.get(this.environment)
                // Map<String, CustomXtreamServerSchedulerProperties> 不方便绑定
                // 直接绑定到上一级
                .bind("jt808-server.schedulers", XtreamJt808ServerProperties.SchedulerProperties.class).get();
        return schedulerProperties.getCustomSchedulers();
    }
}
