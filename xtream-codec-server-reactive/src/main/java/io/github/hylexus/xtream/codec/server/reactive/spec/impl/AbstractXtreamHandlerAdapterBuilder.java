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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl;

import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.annotation.OrderedComponent;
import io.github.hylexus.xtream.codec.server.reactive.spec.*;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.UdpSessionIdleStateCheckerProps;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.*;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.builtin.DelegateXtreamHandlerMethodArgumentResolver;
import io.netty.buffer.ByteBufAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author hylexus
 */
public abstract class AbstractXtreamHandlerAdapterBuilder<C extends AbstractXtreamHandlerAdapterBuilder<C>> {
    private static final Logger log = LoggerFactory.getLogger(AbstractXtreamHandlerAdapterBuilder.class);
    protected final ByteBufAllocator byteBufAllocator;
    private final List<XtreamHandlerMapping> handlerMappings;

    private final List<XtreamHandlerAdapter> handlerAdapters;

    private final List<XtreamHandlerResultHandler> resultHandlers;

    private final List<XtreamFilter> xtreamFilters;
    private final List<XtreamRequestExceptionHandler> exceptionHandlers;

    protected XtreamSessionManager<? extends XtreamSession> sessionManager;

    public AbstractXtreamHandlerAdapterBuilder(ByteBufAllocator allocator) {
        this.handlerMappings = new ArrayList<>();
        this.handlerAdapters = new ArrayList<>();
        this.resultHandlers = new ArrayList<>();
        this.xtreamFilters = new ArrayList<>();
        this.exceptionHandlers = new ArrayList<>();
        this.byteBufAllocator = allocator;
    }

    @SuppressWarnings("unchecked")
    private C self() {
        return (C) this;
    }

    public C addHandlerMappings(XtreamHandlerMapping... handlerMappings) {
        if (handlerMappings != null) {
            this.handlerMappings.addAll(Arrays.asList(handlerMappings));
        }
        return self();
    }

    public C addHandlerMappings(Collection<XtreamHandlerMapping> handlerMappings) {
        this.handlerMappings.addAll(handlerMappings);
        return self();
    }

    public C addHandlerMapping(XtreamHandlerMapping handlerMapping) {
        this.handlerMappings.add(handlerMapping);
        return self();
    }

    public C removeHandlerMapping(Class<? extends XtreamHandlerMapping> cls) {
        this.handlerMappings.removeIf(m -> m.getClass().equals(cls));
        return self();
    }

    public C enableBuiltinHandlerAdapters(EntityCodec entityCodec) {
        return this.enableBuiltinHandlerAdapters(DelegateXtreamHandlerMethodArgumentResolver.createDefault(entityCodec));
    }

    public C enableBuiltinHandlerAdapters(XtreamHandlerMethodArgumentResolver argumentResolver) {
        // todo 优化
        this.addHandlerAdapter(new XtreamHandlerMethodHandlerAdapter(argumentResolver));

        this.addHandlerAdapter(new SimpleXtreamRequestHandlerHandlerAdapter());
        return self();
    }

    public C disableBuiltinHandlerAdapters() {
        this.removeHandlerAdapter(XtreamHandlerMethodHandlerAdapter.class);
        this.removeHandlerAdapter(SimpleXtreamRequestHandlerHandlerAdapter.class);
        return self();
    }

    public C enableBuiltinHandlerResultHandlers(EntityCodec codec) {
        this.addHandlerResultHandler(new XtreamResponseBodyHandlerResultHandler(codec));
        this.addHandlerResultHandler(new LoggingXtreamHandlerResultHandler());
        return self();
    }

    public C disableBuiltinHandlerResultHandlers() {
        this.removeResultHandler(LoggingXtreamHandlerResultHandler.class);
        this.removeResultHandler(XtreamResponseBodyHandlerResultHandler.class);
        return self();
    }

    public C addHandlerAdapters(Collection<XtreamHandlerAdapter> handlerAdapters) {
        this.handlerAdapters.addAll(handlerAdapters);
        return self();
    }

    public C addHandlerAdapter(XtreamHandlerAdapter handlerAdapter) {
        this.handlerAdapters.add(handlerAdapter);
        return self();
    }

    public C removeHandlerAdapter(Class<? extends XtreamHandlerAdapter> cls) {
        this.handlerAdapters.removeIf(m -> m.getClass().equals(cls));
        return self();
    }

    public C addHandlerResultHandlers(Collection<XtreamHandlerResultHandler> resultHandlers) {
        this.resultHandlers.addAll(resultHandlers);
        return self();
    }

    public C addHandlerResultHandler(XtreamHandlerResultHandler resultHandler) {
        this.resultHandlers.add(resultHandler);
        return self();
    }

    public C removeResultHandler(Class<? extends XtreamHandlerResultHandler> cls) {
        this.resultHandlers.removeIf(m -> m.getClass().equals(cls));
        return self();
    }

    public C addFilter(XtreamFilter filter) {
        this.xtreamFilters.add(filter);
        return self();
    }

    public C addFilters(Collection<XtreamFilter> filters) {
        this.xtreamFilters.addAll(filters);
        return self();
    }

    public C removeFilter(Class<? extends XtreamFilter> cls) {
        this.xtreamFilters.removeIf(m -> m.getClass().equals(cls));
        return self();
    }

    public C addExceptionHandlers(Collection<XtreamRequestExceptionHandler> exceptionHandlers) {
        this.exceptionHandlers.addAll(exceptionHandlers);
        return self();
    }

    public C addExceptionHandler(XtreamRequestExceptionHandler exceptionHandler) {
        this.exceptionHandlers.add(exceptionHandler);
        return self();
    }

    public C removeExceptionHandler(Class<? extends XtreamRequestExceptionHandler> cls) {
        this.exceptionHandlers.removeIf(m -> m.getClass().equals(cls));
        return self();
    }

    public C sessionManager(XtreamSessionManager<? extends XtreamSession> sessionManager) {
        this.sessionManager = sessionManager;
        return self();
    }

    public abstract XtreamNettyHandlerAdapter build();

    protected XtreamHandler createRequestHandler() {
        if (this.sessionManager == null) {
            // todo 优化
            this.sessionManager = new DefaultXtreamSessionManager(true, new UdpSessionIdleStateCheckerProps(), new XtreamSessionIdGenerator.DefalutXtreamSessionIdGenerator());
        }
        if (this.handlerMappings.isEmpty()) {
            throw new IllegalStateException("No [" + XtreamHandlerMapping.class.getSimpleName() + "] instance configured.");
        }

        if (this.handlerAdapters.isEmpty()) {
            throw new IllegalStateException("No [" + XtreamHandlerAdapter.class.getSimpleName() + "] instance configured.");
        }

        if (this.resultHandlers.isEmpty()) {
            log.info("No [{}] instance configured. Add [{}] for debugging purpose.", XtreamHandlerResultHandler.class.getSimpleName(), LoggingXtreamHandlerResultHandler.class.getSimpleName());
            this.resultHandlers.add(new LoggingXtreamHandlerResultHandler());
        }

        final DispatcherXtreamHandler dispatcherHandler = new DispatcherXtreamHandler(
                OrderedComponent.sort(this.handlerMappings),
                OrderedComponent.sort(this.handlerAdapters),
                OrderedComponent.sort(this.resultHandlers)
        );

        final FilteringXtreamHandler filteringHandler = new FilteringXtreamHandler(
                dispatcherHandler,
                OrderedComponent.sort(this.xtreamFilters)
        );

        return new ExceptionHandlingXtreamHandler(
                filteringHandler,
                OrderedComponent.sort(this.exceptionHandlers)
        );
    }
}
