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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.pubsub;

import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventPublisher;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.event.Jt808EventPayloads;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.values.Jt808EventType;
import io.netty.buffer.ByteBuf;
import org.springframework.stereotype.Component;
import reactor.netty.NettyInbound;

import java.time.LocalDateTime;

/**
 * @author hylexus
 */
@Component
public class EventBasedJt808RequestLifecycleListener implements Jt808RequestLifecycleListener {
    private final XtreamEventPublisher eventPublisher;

    public EventBasedJt808RequestLifecycleListener(XtreamEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void afterRequestDecoded(NettyInbound nettyInbound, ByteBuf rawPayload, Jt808Request request) {
        final LocalDateTime receiveTime = LocalDateTime.now();
        this.eventPublisher.publishIfNecessary(
                Jt808EventType.RECEIVE_PACKAGE,
                () -> Jt808EventPayloads.Jt808ReceiveEvent.fromRequest(rawPayload, request, receiveTime)
        );
    }

    @Override
    public void beforeResponseSend(Jt808Request request, ByteBuf response) {
        final LocalDateTime sentAt = LocalDateTime.now();
        this.eventPublisher.publishIfNecessary(
                Jt808EventType.SEND_PACKAGE,
                () -> Jt808EventPayloads.Jt808SendEvent.of(request, response, sentAt)
        );
    }
}
