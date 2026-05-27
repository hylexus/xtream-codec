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

package io.github.hylexus.xtream.debug.codec.server.reactive.tcp.handlermapping;

import io.github.hylexus.xtream.codec.common.utils.XtreamUtils;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.AbstractSimpleXtreamRequestMappingHandlerMapping;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.DefaultXtreamBlockingHandlerMethodPredicate;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.DefaultXtreamSchedulerRegistry;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Function;

/**
 * @author hylexus
 */
public class DemoTcpXtreamHandlerMapping2 extends AbstractSimpleXtreamRequestMappingHandlerMapping {

    public DemoTcpXtreamHandlerMapping2() {
        this(new String[]{XtreamUtils.detectMainClassPackageName()}, cls -> BeanUtils.createNewInstance(cls, new Object[0]));
    }

    public DemoTcpXtreamHandlerMapping2(String[] basePackages, Function<Class<?>, Object> instanceFactory) {
        super(
                new DefaultXtreamSchedulerRegistry(Schedulers.parallel(), Schedulers.boundedElastic(), Schedulers.boundedElastic()),
                new DefaultXtreamBlockingHandlerMethodPredicate(),
                basePackages, instanceFactory
        );
    }

    @Override
    public Mono<Object> getHandler(XtreamExchange exchange) {
        // 这里示例性地返回第一个处理器，你可以自定义逻辑来进行分发
        return Mono.just(handlerMethods.getFirst());
    }

}
