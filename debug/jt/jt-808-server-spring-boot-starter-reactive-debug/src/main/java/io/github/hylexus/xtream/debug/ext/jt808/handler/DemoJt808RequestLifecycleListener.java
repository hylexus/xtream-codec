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

package io.github.hylexus.xtream.debug.ext.jt808.handler;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestHeader;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventPublisher;
import io.github.hylexus.xtream.debug.ext.jt808.domain.values.DemoJt808EventPayloads;
import io.github.hylexus.xtream.debug.ext.jt808.domain.values.DemoJt808EventType;
import io.netty.buffer.ByteBuf;
import reactor.netty.NettyInbound;

/**
 * @author hylexus
 */
public class DemoJt808RequestLifecycleListener implements Jt808RequestLifecycleListener {
    private final XtreamEventPublisher eventPublisher;

    public DemoJt808RequestLifecycleListener(XtreamEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void afterRequestDecoded(NettyInbound nettyInbound, ByteBuf rawPayload, Jt808Request request) {
        // 请求被解码之后发送事件
        // 发送事件然后立即返回；不要有阻塞的操作
        this.eventPublisher.publishIfNecessary(
                DemoJt808EventType.RECEIVE_PACKAGE,
                () -> {
                    final Jt808RequestHeader header = request.header();
                    return new DemoJt808EventPayloads.ReceiveRequest(
                            request.requestId(),
                            request.traceId(),
                            header.version().shortDesc(),
                            header.messageBodyProps().hasSubPackage(),
                            header.messageId(),
                            FormatUtils.toHexString(rawPayload)
                    );
                }
        );
    }

    @Override
    public void afterSubPackageMerged(XtreamExchange exchange, Jt808Request mergedRequest) {
        // 分包合并之后发送事件
        // 发送事件然后立即返回；不要有阻塞的操作
        this.eventPublisher.publishIfNecessary(
                DemoJt808EventType.MERGE_PACKAGE,
                () -> {
                    final Jt808RequestHeader header = mergedRequest.header();
                    return new DemoJt808EventPayloads.MergeRequest(
                            mergedRequest.requestId(),
                            mergedRequest.traceId(),
                            header.version().shortDesc(),
                            header.messageBodyProps().hasSubPackage(),
                            header.messageId(),
                            FormatUtils.toHexString(mergedRequest.payload())
                    );
                }
        );
    }

    @Override
    public void beforeResponseSend(Jt808Request request, ByteBuf response) {
        this.eventPublisher.publishIfNecessary(
                DemoJt808EventType.SEND_PACKAGE,
                () -> {
                    // ...
                    return new DemoJt808EventPayloads.SendResponse(
                            request.requestId(),
                            request.traceId(),
                            0,
                            FormatUtils.toHexString(response)
                    );
                }
        );
    }
}
