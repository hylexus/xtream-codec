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

package io.github.hylexus.xtream.codec.server.reactive.spec.event.builtin;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEvent;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * 默认实现
 * <p>
 * 通过 {@link Schedulers#newSingle(String)} 将实际事件投递串行化
 *
 * @author hylexus
 */
public class DefaultXtreamEventPublisher extends AbstractXtreamEventPublisher {

    private final Scheduler publishScheduler;
    private boolean shuttingDown;

    public DefaultXtreamEventPublisher(XtreamSchedulerRegistry schedulerRegistry, String name) {
        super(schedulerRegistry);
        this.publishScheduler = Schedulers.newSingle(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void publish(XtreamEvent event) {
        if (!this.shuttingDown) {
            this.publishScheduler.schedule(() -> super.publish(event));
        }
    }

    @Override
    public synchronized void shutdown() {
        if (this.shuttingDown) {
            return;
        }
        this.shuttingDown = true;
        // 与事件投递在同一线程上终止，使关闭信号跟在已提交的事件之后。
        this.publishScheduler.schedule(() -> {
            try {
                super.shutdown();
            } finally {
                this.publishScheduler.dispose();
            }
        });
    }

}
