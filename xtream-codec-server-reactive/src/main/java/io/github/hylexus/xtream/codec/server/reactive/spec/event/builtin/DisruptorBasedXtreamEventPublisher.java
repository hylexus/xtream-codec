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

import java.util.concurrent.ThreadFactory;

/**
 * 基于 Disruptor 的实现
 *
 * @author hylexus
 * @see <a href="https://github.com/LMAX-Exchange/disruptor">https://github.com/LMAX-Exchange/disruptor</a>
 */
public class DisruptorBasedXtreamEventPublisher extends AbstractXtreamEventPublisher {

    private final Disruptor<XtreamEventDisruptorHolder> disruptor;

    public DisruptorBasedXtreamEventPublisher(XtreamSchedulerRegistry schedulerRegistry, int bufferSize, ThreadFactory threadFactory) {
        super(schedulerRegistry);
        this.disruptor = initDisruptor(bufferSize, threadFactory);
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
    public void publish(XtreamEvent event) {
        final RingBuffer<XtreamEventDisruptorHolder> ringBuffer = this.disruptor.getRingBuffer();
        final long seq = ringBuffer.next();
        try {
            final XtreamEventDisruptorHolder holder = ringBuffer.get(seq);
            holder.setEvent(event);
        } finally {
            ringBuffer.publish(seq);
        }
    }

    @Override
    public void shutdown() {
        this.disruptor.shutdown();
        super.shutdown();
    }

}
