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

package io.github.hylexus.xtream.loadtest.jt808;

import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.filter.Jt808RequestCombinerFilter;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestFilter;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionManager;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamFilterChain;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

@Component
@NullMarked
public class LoadtestMetrics implements Jt808RequestLifecycleListener, Jt808RequestFilter {
    private static final Logger log = LoggerFactory.getLogger(LoadtestMetrics.class);
    private final ObjectProvider<Jt808SessionManager> sessionManager;
    private final LoadtestTcpAdmissionMetrics tcpAdmission = new LoadtestTcpAdmissionMetrics();
    private final Map<MessageType, Counters> messages = new EnumMap<>(MessageType.class);
    private final LongAdder preparedResponses = new LongAdder();
    private final LongAdder processingErrors = new LongAdder();
    private final LongAdder invalidChecksums = new LongAdder();
    private final LongAdder locationRecords = new LongAdder();

    public LoadtestMetrics(ObjectProvider<Jt808SessionManager> sessionManager) {
        this.sessionManager = sessionManager;
        for (MessageType type : MessageType.values()) {
            this.messages.put(type, new Counters());
        }
    }

    void onTcpConnection(Connection connection) {
        tcpAdmission.onConnection(connection);
    }

    @Override
    public void afterRequestDecoded(NettyInbound inbound, ByteBuf rawPayload, Jt808Request request) {
        messages.get(MessageType.of(request.messageId())).received.increment();
        if (request.type() == XtreamInbound.Type.TCP) {
            tcpAdmission.onFrameDecoded(request.underlyingChannel());
        }
    }

    @Override
    public void beforeResponseSend(Jt808Request request, ByteBuf response) {
        preparedResponses.increment();
    }

    @Override
    public Mono<Void> filter(XtreamExchange exchange, XtreamFilterChain chain) {
        return Mono.defer(() -> {
            final Jt808Request request = (Jt808Request) exchange.request();
            if (request.originalCheckSum() != request.calculatedCheckSum()) {
                invalidChecksums.increment();
                return Mono.empty();
            }
            return chain.filter(exchange);
        }).doOnError(error -> processingErrors.increment());
    }

    @Override
    public int order() {
        return Jt808RequestCombinerFilter.ORDER - 1;
    }

    void accepted(int messageId, int records) {
        messages.get(MessageType.of(messageId)).accepted.increment();
        locationRecords.add(records);
    }

    void refused(int messageId) {
        messages.get(MessageType.of(messageId)).refused.increment();
    }

    public Snapshot snapshot() {
        final Map<MessageType, MessageCounts> result = new EnumMap<>(MessageType.class);
        messages.forEach((type, counts) -> result.put(type, new MessageCounts(counts.received.sum(), counts.accepted.sum(), counts.refused.sum())));
        return new Snapshot(
                Map.copyOf(result),
                preparedResponses.sum(), processingErrors.sum(), invalidChecksums.sum(),
                locationRecords.sum(), tcpAdmission.snapshot()
        );
    }

    @Scheduled(fixedDelayString = "${loadtest.summary-interval:30s}", initialDelayString = "${loadtest.summary-interval:30s}")
    public void logSummary() {
        final Jt808SessionManager manager = sessionManager.getObject();
        log.info("压测汇总: sessions={}, tcp={}, udp={}, statistics={}", manager.count(), manager.countTcp(), manager.countUdp(), snapshot());
    }

    public enum MessageType {
        REGISTER, AUTH, HEARTBEAT, LOCATION, BATCH_LOCATION, OTHER;

        static MessageType of(int messageId) {
            return switch (messageId) {
                case 0x0100 -> REGISTER;
                case 0x0102 -> AUTH;
                case 0x0002 -> HEARTBEAT;
                case 0x0200 -> LOCATION;
                case 0x0704 -> BATCH_LOCATION;
                default -> OTHER;
            };
        }
    }

    public record MessageCounts(long received, long accepted, long refused) {
    }

    public record Snapshot(
            Map<MessageType, MessageCounts> messages, long preparedResponses, long processingErrors,
            long invalidChecksums, long locationRecords, TcpAdmissionSnapshot tcpAdmission) {
    }

    public record TcpAdmissionSnapshot(
            long readyConnections, long firstDecodedFrames, long firstResponsesWritten,
            long readyToFirstResponseP95Micros, long readyToFirstResponseP99Micros) {
    }

    private static final class Counters {
        private final LongAdder received = new LongAdder();
        private final LongAdder accepted = new LongAdder();
        private final LongAdder refused = new LongAdder();
    }
}
