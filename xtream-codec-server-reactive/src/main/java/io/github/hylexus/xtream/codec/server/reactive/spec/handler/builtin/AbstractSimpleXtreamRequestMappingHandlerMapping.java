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

package io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin;

import io.github.hylexus.xtream.codec.common.utils.DefaultXtreamClassScanner;
import io.github.hylexus.xtream.codec.common.utils.XtreamClassScanner;
import io.github.hylexus.xtream.codec.common.utils.XtreamUtils;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.ReactiveXtreamHandlerMethod;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamHandlerMethod;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamRequestHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamRequestHandlerMapping;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamBlockingHandlerMethodPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * 用于在非 Spring 环境下快速初始化 {@link io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerMapping}
 *
 * @author hylexus
 */
public abstract class AbstractSimpleXtreamRequestMappingHandlerMapping extends AbstractXtreamRequestMappingHandlerMapping {
    private static final Logger log = LoggerFactory.getLogger(AbstractSimpleXtreamRequestMappingHandlerMapping.class);
    protected List<XtreamHandlerMethod> handlerMethods = new ArrayList<>();

    public AbstractSimpleXtreamRequestMappingHandlerMapping(
            XtreamSchedulerRegistry schedulerRegistry, XtreamBlockingHandlerMethodPredicate blockingHandlerMethodPredicate,
            String[] basePackages, Function<Class<?>, Object> instanceFactory) {
        super(schedulerRegistry, blockingHandlerMethodPredicate);
        this.initHandlerMethods(basePackages, instanceFactory);
    }

    protected Set<Class<?>> doScan(String[] basePackages) {
        return new DefaultXtreamClassScanner().scan(
                basePackages,
                Set.of(XtreamClassScanner.ScanMode.CLASS_ANNOTATION),
                XtreamRequestHandler.class,
                classInfo -> !classInfo.isAnnotation()
        );
    }

    protected void initHandlerMethods(String[] basePackages, Function<Class<?>, Object> instanceFactory) {
        // 启动类所在包名
        if (basePackages == null || basePackages.length == 0) {
            final String packageName = XtreamUtils.detectMainClassPackageName();
            log.info("Use [{}] as [@{}] scan packages.", packageName, XtreamRequestHandler.class.getSimpleName());
            basePackages = new String[]{packageName};
        }

        final Set<Class<?>> classes = this.doScan(basePackages);

        for (final Class<?> cls : classes) {
            final XtreamRequestHandler classLevelAnnotation = requireNonNull(AnnotatedElementUtils.getMergedAnnotation(cls, XtreamRequestHandler.class));
            ReflectionUtils.doWithMethods(
                    cls,
                    method -> {
                        log.info(method.getName());
                        final XtreamRequestHandlerMapping mappingAnnotation = requireNonNull(AnnotatedElementUtils.getMergedAnnotation(method, XtreamRequestHandlerMapping.class));
                        final Object containerInstance = instanceFactory.apply(cls);

                        final XtreamHandlerMethod handlerMethod = new ReactiveXtreamHandlerMethod(
                                cls,
                                containerInstance,
                                method,
                                "desc",
                                (XtreamHandlerMethod ha) -> this.determineSchedulerInfo(
                                        ha,
                                        mappingAnnotation.scheduler(),
                                        classLevelAnnotation.blockingScheduler(),
                                        classLevelAnnotation.nonBlockingScheduler()
                                )
                        );

                        handlerMethods.add(handlerMethod);
                    },
                    // 被 @XtreamRequestHandlerMapping 注解的方法才会被注册为处理器
                    method -> AnnotatedElementUtils.hasAnnotation(method, XtreamRequestHandlerMapping.class)
            );
        }
    }

    @Override
    public int order() {
        return -1;
    }
}
