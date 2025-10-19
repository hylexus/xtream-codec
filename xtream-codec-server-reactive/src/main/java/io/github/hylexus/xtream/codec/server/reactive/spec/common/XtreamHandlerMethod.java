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
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResult;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.lang.reflect.Method;

/**
 * 当前类是从 `org.springframework.web.method.HandlerMethod` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `org.springframework.web.method.HandlerMethod`.
 *
 * @author hylexus
 */
// todo: NullAway 重构
@SuppressWarnings("NullAway")
public abstract class XtreamHandlerMethod {

    protected final Class<?> containerClass;
    protected Object containerInstance;
    protected final Method method;
    protected final XtreamMethodParameter[] parameters;
    protected Scheduler scheduler;
    protected String schedulerName;
    protected String desc;
    protected boolean nonBlocking;
    protected boolean rejectBlockingTask;
    protected boolean virtualThread;

    public XtreamHandlerMethod(Class<?> containerClass, Method method) {
        this.containerClass = containerClass;
        this.method = method;
        this.parameters = this.initMethodParameters(method);
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

    public void setContainerInstance(Object containerInstance) {
        this.containerInstance = containerInstance;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public XtreamHandlerMethod setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
        return this;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public String getDesc() {
        return desc;
    }

    public XtreamHandlerMethod setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public XtreamHandlerMethod setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    public XtreamHandlerMethod setNonBlocking(boolean nonBlocking) {
        this.nonBlocking = nonBlocking;
        return this;
    }

    public boolean isNonBlocking() {
        return nonBlocking;
    }

    public boolean isVirtualThread() {
        return virtualThread;
    }

    public XtreamHandlerMethod setVirtualThread(boolean virtualThread) {
        this.virtualThread = virtualThread;
        return this;
    }

    public boolean isRejectBlockingTask() {
        return rejectBlockingTask;
    }

    public XtreamHandlerMethod setRejectBlockingTask(boolean rejectBlockingTask) {
        this.rejectBlockingTask = rejectBlockingTask;
        return this;
    }
}
