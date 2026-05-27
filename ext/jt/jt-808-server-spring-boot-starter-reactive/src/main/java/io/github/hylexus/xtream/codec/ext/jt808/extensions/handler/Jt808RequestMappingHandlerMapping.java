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

package io.github.hylexus.xtream.codec.ext.jt808.extensions.handler;

import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.ReactiveXtreamHandlerMethod;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamHandlerMethod;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamBlockingHandlerMethodPredicate;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.AbstractXtreamRequestMappingHandlerMapping;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * @author hylexus
 */
public class Jt808RequestMappingHandlerMapping extends AbstractXtreamRequestMappingHandlerMapping implements ApplicationContextAware, InitializingBean {
    // private static final Logger log = LoggerFactory.getLogger(Jt808RequestMappingHandlerMapping.class);
    // <messageId,<version,handler>>
    private final Map<Integer, Map<Jt808ProtocolVersion, XtreamHandlerMethod>> mappings = new HashMap<>();
    @SuppressWarnings("NullAway")
    protected ApplicationContext applicationContext;

    public Jt808RequestMappingHandlerMapping(XtreamSchedulerRegistry schedulerRegistry, XtreamBlockingHandlerMethodPredicate blockingHandlerMethodPredicate) {
        super(schedulerRegistry, blockingHandlerMethodPredicate);
    }

    @Override
    public Mono<Object> getHandler(XtreamExchange exchange) {
        if (exchange.request() instanceof Jt808Request jt808Request) {
            final Map<Jt808ProtocolVersion, XtreamHandlerMethod> multiVersionHandlers = mappings.get(jt808Request.header().messageId());
            if (multiVersionHandlers == null || multiVersionHandlers.isEmpty()) {
                return Mono.empty();
            }
            final XtreamHandlerMethod handler = multiVersionHandlers.get(jt808Request.header().version());
            if (handler == null) {
                return Mono.justOrEmpty(multiVersionHandlers.get(Jt808ProtocolVersion.AUTO_DETECTION));
            }
            return Mono.just(handler);
        }
        return Mono.empty();
    }

    protected void initHandlerMethods() {
        final Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(Jt808RequestHandler.class);

        for (final Map.Entry<String, Object> entry : beans.entrySet()) {
            final Object instance = entry.getValue();
            final Class<?> cls = instance.getClass();
            final Jt808RequestHandler classLevelAnnotation = requireNonNull(AnnotatedElementUtils.getMergedAnnotation(cls, Jt808RequestHandler.class));
            ReflectionUtils.doWithMethods(
                    cls,
                    method -> {
                        final Jt808RequestHandlerMapping annotation = Objects.requireNonNull(AnnotatedElementUtils.getMergedAnnotation(method, Jt808RequestHandlerMapping.class));

                        final XtreamHandlerMethod handlerMethod = new ReactiveXtreamHandlerMethod(
                                cls,
                                instance,
                                method,
                                annotation.desc(),
                                (XtreamHandlerMethod ha) -> determineSchedulerInfo(ha, annotation.scheduler(),
                                        classLevelAnnotation.blockingScheduler(),
                                        classLevelAnnotation.nonBlockingScheduler()
                                )
                        );

                        final int[] messageIds = annotation.messageIds();
                        for (final int messageId : messageIds) {
                            final Map<Jt808ProtocolVersion, XtreamHandlerMethod> map = this.mappings.computeIfAbsent(messageId, k -> new HashMap<>());
                            for (final Jt808ProtocolVersion version : annotation.versions()) {
                                map.put(version, handlerMethod);
                            }
                        }
                    },
                    // 被 @Jt808RequestHandlerMapping 注解的方法才会被注册为处理器
                    method -> AnnotatedElementUtils.hasAnnotation(method, Jt808RequestHandlerMapping.class)
            );
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.initHandlerMethods();
    }

    @Override
    public int order() {
        // 0
        return super.order();
    }

    public Map<Integer, Map<Jt808ProtocolVersion, XtreamHandlerMethod>> getMappings() {
        return mappings;
    }
}
