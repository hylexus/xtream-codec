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

package io.github.hylexus.xtream.codec.server.reactive.spec.common;

import io.github.hylexus.xtream.codec.common.bean.XtreamMethodParameter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResult;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * 当前类是从 `org.springframework.web.method.HandlerMethod` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `org.springframework.web.method.HandlerMethod`.
 *
 * @author hylexus
 */
@SuppressWarnings("LombokGetterMayBeUsed")
public abstract class XtreamHandlerMethod {

    protected final Class<?> containerClass;
    protected final Object containerInstance;
    protected final Method method;
    protected final XtreamMethodParameter[] parameters;
    protected final String desc;

    /**
     * @see io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamBlockingHandlerMethodPredicate#isBlockingHandlerMethod(XtreamHandlerMethod)
     */
    protected final boolean nonBlocking;

    protected final Scheduler scheduler;
    protected final String schedulerName;
    protected final boolean rejectBlockingTask;
    protected final boolean virtualThread;

    public XtreamHandlerMethod(Class<?> containerClass, Object containerInstance, Method method, String desc, Function<XtreamHandlerMethod, HandlerMethodSchedulerInfo> schedulerInfoFunc) {
        this.containerClass = containerClass;
        this.containerInstance = containerInstance;
        this.desc = desc;
        this.method = method;
        this.parameters = this.initMethodParameters(method);

        final HandlerMethodSchedulerInfo schedulerInfo = schedulerInfoFunc.apply(this);
        this.nonBlocking = !schedulerInfo.isBlockingHandler();
        this.scheduler = schedulerInfo.scheduler();
        this.schedulerName = schedulerInfo.config().name();
        this.rejectBlockingTask = schedulerInfo.config().rejectBlocking();
        this.virtualThread = schedulerInfo.config().virtualThread();
    }

    public Mono<XtreamHandlerResult> invoke(Object containerInstance, Object[] args) {
        try {
            final Object result = this.method.invoke(containerInstance, args);
            final XtreamMethodParameter returnType = new XtreamMethodParameter(-1, this.method);
            return Mono.just(new XtreamHandlerResult(this, result, returnType));
        } catch (Throwable e) {
            return Mono.error(e);
        }
    }

    private XtreamMethodParameter[] initMethodParameters(Method method) {
        final int paramCount = method.getGenericParameterTypes().length;
        final XtreamMethodParameter[] methodParameters = new XtreamMethodParameter[paramCount];
        for (int i = 0; i < paramCount; i++) {
            final XtreamMethodParameter parameter = new XtreamMethodParameter(i, method);
            methodParameters[i] = parameter;
        }
        return methodParameters;
    }

    public XtreamMethodParameter[] getParameters() {
        return parameters;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getContainerClass() {
        return containerClass;
    }

    public Object getContainerInstance() {
        return containerInstance;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isNonBlocking() {
        return nonBlocking;
    }

    public boolean isVirtualThread() {
        return virtualThread;
    }

    public boolean isRejectBlockingTask() {
        return rejectBlockingTask;
    }

    public record HandlerMethodSchedulerInfo(boolean isBlockingHandler, Scheduler scheduler, XtreamSchedulerRegistry.SchedulerConfig config) {
    }

}
