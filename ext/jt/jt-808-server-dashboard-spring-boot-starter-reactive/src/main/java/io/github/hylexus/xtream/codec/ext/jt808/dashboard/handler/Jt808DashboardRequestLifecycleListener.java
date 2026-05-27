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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.handler;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.events.Jt808DashboardEventPayloads;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.events.Jt808DashboardEventType;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageDescriptionRegistry;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestHeader;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEvent;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventPublisher;
import io.netty.buffer.ByteBuf;
import reactor.netty.NettyInbound;

import java.time.Instant;

/**
 * @author hylexus
 */
public class Jt808DashboardRequestLifecycleListener implements Jt808RequestLifecycleListener {
    private final XtreamEventPublisher eventPublisher;
    private final Jt808MessageDescriptionRegistry descriptionRegistry;

    public Jt808DashboardRequestLifecycleListener(XtreamEventPublisher eventPublisher, Jt808MessageDescriptionRegistry descriptionRegistry) {
        this.eventPublisher = eventPublisher;
        this.descriptionRegistry = descriptionRegistry;
    }

    @Override
    public void afterRequestDecoded(NettyInbound nettyInbound, ByteBuf rawPayload, Jt808Request request) {
        // 请求被解码之后发送事件
        // 发送事件然后立即返回；不要有阻塞的操作
        final Instant now = Instant.now();
        this.eventPublisher.publishIfNecessary(
                XtreamEvent.XtreamEventType.AFTER_REQUEST_RECEIVED,
                () -> {
                    final Jt808RequestHeader header = request.header();
                    return new Jt808DashboardEventPayloads.ReceivedRequestInfo(
                            request.requestId(),
                            request.traceId(),
                            request.terminalId(),
                            header.version().shortDesc(),
                            header.messageBodyProps().hasSubPackage(),
                            header.messageId(),
                            this.getMessageIdDesc(header.messageId()),
                            FormatUtils.toHexString(rawPayload),
                            now
                    );
                }
        );
    }

    @Override
    public void afterSubPackageMerged(XtreamExchange exchange, Jt808Request mergedRequest) {
        // 分包合并之后发送事件
        // 发送事件然后立即返回；不要有阻塞的操作
        final Instant now = Instant.now();
        this.eventPublisher.publishIfNecessary(
                Jt808DashboardEventType.AFTER_SUB_REQUEST_MERGED,
                () -> {
                    final Jt808RequestHeader header = mergedRequest.header();
                    return new Jt808DashboardEventPayloads.MergedRequestInfo(
                            mergedRequest.requestId(),
                            mergedRequest.traceId(),
                            mergedRequest.terminalId(),
                            header.version().shortDesc(),
                            header.messageBodyProps().hasSubPackage(),
                            header.messageId(),
                            this.getMessageIdDesc(header.messageId()),
                            FormatUtils.toHexString(mergedRequest.payload()),
                            now
                    );
                }
        );
    }

    @Override
    public void beforeResponseSend(Jt808Request request, ByteBuf response) {
        final Instant now = Instant.now();
        this.eventPublisher.publishIfNecessary(
                XtreamEvent.XtreamEventType.BEFORE_RESPONSE_SEND,
                () -> {
                    // ...
                    return new Jt808DashboardEventPayloads.ResponseInfo(
                            request.requestId(),
                            request.traceId(),
                            request.terminalId(),
                            request.messageId(),
                            this.getMessageIdDesc(request.messageId()) + " - 回复",
                            FormatUtils.toHexString(response),
                            now
                    );
                }
        );
    }

    @Override
    public void beforeCommandSend(Jt808Session session, ByteBuf command) {
        final Instant now = Instant.now();
        this.eventPublisher.publishIfNecessary(
                XtreamEvent.XtreamEventType.BEFORE_COMMAND_SEND,
                () -> {
                    // ...
                    return new Jt808DashboardEventPayloads.CommandInfo(
                            session.id(),
                            session.terminalId(),
                            session.protocolVersion().shortDesc(),
                            FormatUtils.toHexString(command),
                            now
                    );
                }
        );
    }

    private String getMessageIdDesc(int messageId) {
        return this.descriptionRegistry.getDescription(messageId, "未知消息");
    }
}
