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

package io.github.hylexus.xtream.quickstart.custom.annotation.handler;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamUtils;
import io.github.hylexus.xtream.codec.core.utils.BeanUtils;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.AbstractSimpleXtreamRequestMappingHandlerMapping;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.DefaultXtreamBlockingHandlerMethodPredicate;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.DefaultXtreamSchedulerRegistry;
import io.github.hylexus.xtream.quickstart.custom.annotation.annotation.DemoMessageMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Function;

/// X-IoT Demo 协议的 HandlerMapping。
///
/// 继承 `AbstractSimpleXtreamRequestMappingHandlerMapping` 后，会自动扫描
/// `@DemoMessageHandler`（即 `@XtreamRequestHandler`）标记的类，并将其中的
/// `@DemoMessageMapping`（即 `@XtreamRequestHandlerMapping`）标记的方法注册为处理器。
///
/// `getHandler(XtreamExchange)` 方法根据报文中的 `msgType` 字段分发到对应处理器方法。
public class DemoMessageHandlerMapping extends AbstractSimpleXtreamRequestMappingHandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(DemoMessageHandlerMapping.class);

    public DemoMessageHandlerMapping() {
        this(new String[]{XtreamUtils.detectMainClassPackageName()}, cls -> BeanUtils.createNewInstance(cls, new Object[0]));
    }

    public DemoMessageHandlerMapping(String[] basePackages, Function<Class<?>, Object> instanceFactory) {
        super(
                new DefaultXtreamSchedulerRegistry(Schedulers.parallel(), Schedulers.boundedElastic(), Schedulers.boundedElastic()),
                new DefaultXtreamBlockingHandlerMethodPredicate(),
                basePackages, instanceFactory
        );
    }

    public DemoMessageHandlerMapping(String[] basePackages, Function<Class<?>, Object> instanceFactory, XtreamSchedulerRegistry schedulerRegistry) {
        super(schedulerRegistry, new DefaultXtreamBlockingHandlerMethodPredicate(), basePackages, instanceFactory);
    }

    @Override
    public Mono<Object> getHandler(XtreamExchange exchange) {
        // msgType 在报文头的第 5 字节（偏移 4），无符号
        final int msgType = exchange.request().payload().getByte(4) & 0xFF;
        log.info("Dispatching request with msgType={}(0x{})", msgType, FormatUtils.toHexString(msgType, 2));

        // FIXME 这里可以缓存到 Map 里，不用每次都遍历（为演示方便，这里直接遍历）
        for (final var handlerMethod : handlerMethods) {
            final DemoMessageMapping mapping = handlerMethod.getMethod().getAnnotation(DemoMessageMapping.class);
            if (mapping != null) {
                for (final int type : mapping.msgType()) {
                    if (type == msgType) {
                        return Mono.just(handlerMethod);
                    }
                }
            }
        }

        log.warn("No handler found for msgType={}(0x{})", msgType, FormatUtils.toHexString(msgType, 2));
        return Mono.empty();
    }
}
