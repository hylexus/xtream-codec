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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.utils;

import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.routing.context.R2dbcMybatisDatabaseRoutingOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author hylexus
 * @see <a href="https://github.com/chenggangpro/reactive-mybatis-support/wiki/DynamicDatabaseRouting">https://github.com/chenggangpro/reactive-mybatis-support/wiki/DynamicDatabaseRouting</a>
 */
public enum DatabaseRouter {
    TRACE_LOG_MYSQL("trace-log-mysql"),
    TRACE_LOG_POSTGRES("trace-log-postgres"),
    TRACE_LOG_CLICKHOUSE("trace-log-clickhouse"),
    ;

    /**
     * 对应配置文件中 spring.r2dbc.mybatis.routing.definitions[x].name
     */
    private final String key;

    DatabaseRouter(String key) {
        this.key = key;
    }

    public <T> Mono<T> executeMono(Mono<T> mono) {
        return R2dbcMybatisDatabaseRoutingOperator.executeMono(this.key, mono);
    }

    public <T> Flux<T> executeFlux(Flux<T> flux) {
        return R2dbcMybatisDatabaseRoutingOperator.executeFlux(this.key, flux);
    }
}
