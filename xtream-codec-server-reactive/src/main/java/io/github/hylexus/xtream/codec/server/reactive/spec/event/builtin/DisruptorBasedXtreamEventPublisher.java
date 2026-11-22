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

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEvent;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.builtin.disruptor.XtreamEventDisruptorHolder;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ThreadFactory;

/**
 * 基于 Disruptor 的实现
 *
 * @author hylexus
 * @see <a href="https://github.com/LMAX-Exchange/disruptor">https://github.com/LMAX-Exchange/disruptor</a>
 */
public class DisruptorBasedXtreamEventPublisher extends AbstractXtreamEventPublisher {

    private final Disruptor<XtreamEventDisruptorHolder> disruptor;
    // 标记正在执行本实例消费任务的线程，避免 shutdown() 在消费线程上等待队列排空。
    // 子类可通过 initDisruptor() 注册多个消费者，各线程的标记独立，避免共享线程引用被覆盖。
    private final ThreadLocal<@Nullable Boolean> onConsumerThread = new ThreadLocal<>();
    private boolean shuttingDown;

    public DisruptorBasedXtreamEventPublisher(XtreamSchedulerRegistry schedulerRegistry, int bufferSize, ThreadFactory threadFactory) {
        super(schedulerRegistry);
        this.disruptor = initDisruptor(bufferSize, task -> threadFactory.newThread(() -> {
            this.onConsumerThread.set(true);
            try {
                task.run();
            } finally {
                this.onConsumerThread.remove();
            }
        }));
        this.disruptor.start();
    }

    protected Disruptor<XtreamEventDisruptorHolder> initDisruptor(int bufferSize, ThreadFactory threadFactory) {
        final EventFactory<XtreamEventDisruptorHolder> factory = XtreamEventDisruptorHolder::new;
        final Disruptor<XtreamEventDisruptorHolder> disruptor = new Disruptor<>(
                factory,
                bufferSize,
                threadFactory,
                ProducerType.MULTI,
                new BlockingWaitStrategy()
        );

        disruptor.handleEventsWith((event, sequence, endOfBatch) -> {
            if (event.getEvent() != null) {
                super.publish(event.getEvent());
            }
        });
        return disruptor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void publish(XtreamEvent event) {
        final RingBuffer<XtreamEventDisruptorHolder> ringBuffer = this.disruptor.getRingBuffer();
        // 队列满时先等待容量；只有实际入队的事件才计入关闭时的排空范围。
        // TODO 后续评估满队列时的等待上限与超载策略，避免消费回调重入发布时等待自身。
        boolean interrupted = false;
        try {
            while (!this.shuttingDown && !ringBuffer.tryPublishEvent((holder, sequence, value) -> holder.setEvent(value), event)) {
                try {
                    this.wait(1L);
                } catch (InterruptedException ignored) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 从消费线程调用时，停止接纳新事件后返回，由另一个线程等待队列排空。
     */
    @Override
    public void shutdown() {
        synchronized (this) {
            if (this.shuttingDown) {
                return;
            }
            this.shuttingDown = true;
            this.notifyAll();
        }
        if (Boolean.TRUE.equals(this.onConsumerThread.get())) {
            Thread.ofPlatform().daemon().name("xtream-disruptor-shutdown").start(this::finishShutdown);
        } else {
            this.finishShutdown();
        }
    }

    private void finishShutdown() {
        this.disruptor.shutdown();
        super.shutdown();
    }

}
