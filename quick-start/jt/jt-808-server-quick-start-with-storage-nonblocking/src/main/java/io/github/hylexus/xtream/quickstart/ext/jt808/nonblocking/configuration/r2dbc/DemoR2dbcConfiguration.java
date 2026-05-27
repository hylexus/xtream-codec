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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.configuration.r2dbc;

import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.configuration.database.clickhouse.adapter.ClickhousePlaceholderDialect;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.mapper.adapter.Jt808NetTypeHandlerAdapter;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.mapper.adapter.Jt808VersionHandlerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapterRegistry;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.support.R2dbcMybatisConfigurationCustomizer;

/**
 * @author hylexus
 */
@Configuration
public class DemoR2dbcConfiguration {

    @Bean
    public R2dbcMybatisConfigurationCustomizer r2dbcMybatisConfigurationCustomizer() {
        return r2dbcMybatisConfiguration -> {

            final R2dbcTypeHandlerAdapterRegistry typeHandlerAdapterRegistry = r2dbcMybatisConfiguration.getR2dbcTypeHandlerAdapterRegistry();
            typeHandlerAdapterRegistry.register(Jt808VersionHandlerAdapter.class);
            typeHandlerAdapterRegistry.register(Jt808NetTypeHandlerAdapter.class);

            r2dbcMybatisConfiguration.getPlaceholderDialectRegistry().register(new ClickhousePlaceholderDialect());
        };
    }

}
