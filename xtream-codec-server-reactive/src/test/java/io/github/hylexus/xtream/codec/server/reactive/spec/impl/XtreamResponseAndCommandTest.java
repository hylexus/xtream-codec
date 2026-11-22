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


package io.github.hylexus.xtream.codec.server.reactive.spec.impl;

import io.github.hylexus.xtream.codec.common.bean.XtreamMethodParameter;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.XtreamCacheableClassPredicate;
import io.github.hylexus.xtream.codec.core.annotation.XtreamResponseBody;
import io.github.hylexus.xtream.codec.core.impl.DefaultFieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.impl.SimpleBeanMetadataRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSession;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerResult;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static io.github.hylexus.xtream.codec.server.reactive.spec.impl.ReactiveTestFixtures.ADDRESS;
import static org.junit.jupiter.api.Assertions.*;

@NullMarked
class XtreamResponseAndCommandTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    @Test
    void responseAnnotationCanBeResolvedOnTheHandlerMethodOrReturnType() throws Exception {
        final var handler = new XtreamResponseBodyHandlerResultHandler(EntityCodec.DEFAULT);

        assertTrue(handler.supports(result(AnnotatedHandler.class, "handle", "数据")));
        assertTrue(handler.supports(result(Handler.class, "annotatedMethod", "数据")));
        assertTrue(handler.supports(result(Handler.class, "annotatedReturnType", null)));
        assertTrue(handler.supports(result(Handler.class, "annotatedMonoType", null)));
        assertTrue(handler.supports(result(Handler.class, "annotatedFluxType", null)));
        assertFalse(handler.supports(result(Handler.class, "unannotated", null)));
    }

    @Test
    void synchronousResponseAllocatesOnlyWhenSubscribedAndCanBeEncodedAgain() {
        try (var codec = new RecordingCodec()) {
            final var handler = new XtreamResponseBodyHandlerResultHandler(codec);
            final Flux<ByteBuf> bytes = handler.encode(ByteBufAllocator.DEFAULT, 7, 42);

            assertTrue(codec.buffers.isEmpty());
            bytes.doOnNext(buffer -> verifyAndRelease(buffer, 7, 42)).blockLast(TIMEOUT);
            bytes.doOnNext(buffer -> verifyAndRelease(buffer, 7, 42)).blockLast(TIMEOUT);
            assertEquals(2, codec.buffers.size());
        }
    }

    @Test
    void writesMonoAndFluxResultsUsingTheRequestVersion() throws Exception {
        try (var codec = new RecordingCodec()) {
            final var handler = new XtreamResponseBodyHandlerResultHandler(codec);
            final List<Integer> values = new ArrayList<>();
            final var response = new Response(body -> Flux.from(body).doOnNext(buffer -> {
                try {
                    assertEquals(7, buffer.readUnsignedByte());
                    values.add((int) buffer.readUnsignedByte());
                } finally {
                    buffer.release();
                }
            }).then());
            final var exchange = ReactiveTestFixtures.exchange(response);

            handler.handleResult(exchange, result(Handler.class, "annotatedMethod", Mono.just(19))).block(TIMEOUT);
            handler.handleResult(exchange, result(Handler.class, "annotatedMethod", Flux.just(20, 21))).block(TIMEOUT);
            handler.handleResult(exchange, result(Handler.class, "annotatedMethod", Flux.empty())).block(TIMEOUT);
            assertEquals(List.of(19, 20, 21), values);
            assertTrue(codec.buffers.stream().allMatch(buffer -> buffer.refCnt() == 0));
        }
    }

    @Test
    void releasesTheAllocatedBufferWhenEncodingThrows() {
        try (var codec = new RecordingCodec()) {
            final var handler = new XtreamResponseBodyHandlerResultHandler(codec);
            final var failure = assertThrows(RuntimeException.class,
                    () -> handler.encode(ByteBufAllocator.DEFAULT, 7, Mono.just(-1)).blockLast(TIMEOUT));

            assertInstanceOf(IllegalArgumentException.class, failure.getCause());
            assertEquals(1, codec.buffers.size());
            assertEquals(0, codec.buffers.getFirst().refCnt());
            assertThrows(IllegalArgumentException.class, () -> handler.encode(ByteBufAllocator.DEFAULT, 7, null).blockLast(TIMEOUT));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void responseDoesNotAllocateWhenOutboundRejectsOrCancelsBeforeSubscription(boolean rejection) throws Exception {
        try (var codec = new RecordingCodec()) {
            final var failure = new IllegalStateException("发送被拒绝");
            final var response = new Response(ignored -> rejection ? Mono.error(failure) : Mono.never());
            final var handler = new XtreamResponseBodyHandlerResultHandler(codec);
            final var send = handler.handleResult(ReactiveTestFixtures.exchange(response), result(Handler.class, "annotatedMethod", 42));

            if (rejection) {
                assertSame(failure, assertThrows(IllegalStateException.class, () -> send.block(TIMEOUT)));
            } else {
                send.subscribe().dispose();
            }
            assertTrue(codec.buffers.isEmpty());
        }
    }

    @Test
    void commandSenderReportsMissingSessionWithoutSubscribingToTheBody() {
        final var sessionManager = new DefaultXtreamSessionManager(false, null, ignored -> "missing");
        try (var codec = new RecordingCodec()) {
            final var sender = new DefaultXtreamCommandSender(ByteBufAllocator.DEFAULT, sessionManager, codec);
            final AtomicBoolean subscribed = new AtomicBoolean();
            final Mono<Object> data = Mono.fromSupplier(() -> {
                subscribed.set(true);
                return 42;
            });

            final var error = assertThrows(IllegalArgumentException.class, () -> sender.sendObject(7, "missing", data).block(TIMEOUT));
            assertTrue(error.getMessage().contains("missing"));
            assertFalse(subscribed.get());
        }
    }

    @Test
    void commandsSendRawDataUnchangedAndEncodeObjectsWithTheRequestedVersion() {
        final ByteBuf raw = Unpooled.buffer(1).writeByte(5);
        try (var codec = new RecordingCodec()) {
            final List<Integer> data = new ArrayList<>();
            final var sender = sender(codec, body -> Flux.from(body).doOnNext(buffer -> {
                try {
                    if (buffer.readableBytes() == 1) {
                        assertSame(raw, buffer);
                    }
                    while (buffer.isReadable()) {
                        data.add((int) buffer.readUnsignedByte());
                    }
                } finally {
                    buffer.release();
                }
            }).then());

            sender.sendByteBuf("session", Mono.just(raw)).block(TIMEOUT);
            sender.sendObject(7, "session", Flux.just(42, 43)).block(TIMEOUT);
            assertEquals(List.of(5, 7, 42, 7, 43), data);
            assertEquals(0, raw.refCnt());
            assertEquals(2, codec.buffers.size());
            assertTrue(codec.buffers.stream().allMatch(buffer -> buffer.refCnt() == 0));
        } finally {
            if (raw.refCnt() > 0) {
                raw.release();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void commandDoesNotAllocateWhenOutboundRejectsOrCancelsBeforeSubscription(boolean rejection) {
        try (var codec = new RecordingCodec()) {
            final var failure = new IllegalStateException("发送被拒绝");
            final var sender = sender(codec, ignored -> rejection ? Mono.error(failure) : Mono.never());
            final var send = sender.sendObject(7, "session", Mono.just(42));

            if (rejection) {
                assertSame(failure, assertThrows(IllegalStateException.class, () -> send.block(TIMEOUT)));
            } else {
                send.subscribe().dispose();
            }
            assertTrue(codec.buffers.isEmpty());
        }
    }

    @Test
    void commandEncodingFailureReleasesTheAllocatedBuffer() {
        try (var codec = new RecordingCodec()) {
            final var sender = sender(codec, body -> Flux.from(body).then());

            final var failure = assertThrows(RuntimeException.class, () -> sender.sendObject(7, "session", Mono.just(-1)).block(TIMEOUT));
            assertInstanceOf(IllegalArgumentException.class, failure.getCause());
            assertEquals(1, codec.buffers.size());
            assertEquals(0, codec.buffers.getFirst().refCnt());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void releasesRawCommandEmittedBeforeAnOutboundRejectionOrCancellation(boolean rejection) {
        final ByteBuf raw = Unpooled.buffer(1).writeByte(42);
        final var failure = new IllegalStateException("出站拒绝");
        try (var codec = new RecordingCodec()) {
            final var handedToSender = new AtomicBoolean();
            final var sender = sender(codec, ignored -> rejection ? Mono.error(failure) : Mono.never());
            final var send = sender.sendObject(7, "session", Mono.fromSupplier(() -> {
                handedToSender.set(true);
                return raw;
            }));

            if (rejection) {
                assertSame(failure, assertThrows(IllegalStateException.class, () -> send.block(TIMEOUT)));
            } else {
                send.subscribe().dispose();
            }
            assertTrue(handedToSender.get());
            assertEquals(0, raw.refCnt());
            assertTrue(codec.buffers.isEmpty());
        } finally {
            releaseRemaining(raw);
        }
    }

    @Test
    void rawCommandIsOwnedByOutboundAfterConsumptionEvenIfWriteFails() {
        final ByteBuf raw = Unpooled.buffer(1).writeByte(42).retain();
        final var failure = new IllegalStateException("处理后发送失败");
        try (var codec = new RecordingCodec()) {
            final var sender = sender(codec, body -> Mono.from(body).doOnNext(buffer -> {
                assertSame(raw, buffer);
                assertFalse(buffer.release());
            }).then(Mono.error(failure)));

            assertSame(failure, assertThrows(IllegalStateException.class,
                    () -> sender.sendObject(7, "session", Mono.just(raw)).block(TIMEOUT)));
            assertEquals(1, raw.refCnt());
        } finally {
            if (raw.refCnt() > 0) {
                raw.release(raw.refCnt());
            }
        }
    }

    @Test
    void rawCommandUsesOverriddenEncoder() {
        final ByteBuf raw = Unpooled.buffer(1).writeByte(42);
        final var manager = sessionManager(body -> Flux.from(body).doOnNext(buffer -> {
            try {
                assertSame(raw, buffer);
                assertEquals(7, buffer.readUnsignedByte());
            } finally {
                buffer.release();
            }
        }).then());
        final var sender = new DefaultXtreamCommandSender(ByteBufAllocator.DEFAULT, manager, EntityCodec.DEFAULT) {
            @Override
            protected ByteBuf doEncode(int version, Object message) {
                assertSame(raw, message);
                return raw.setByte(0, version);
            }
        };
        try {
            sender.sendObject(7, "session", Mono.just(raw)).block(TIMEOUT);
            assertEquals(0, raw.refCnt());
        } finally {
            releaseRemaining(raw);
        }
    }

    @Test
    void rawResponseUsesOverriddenEncoderWithOriginalValue() throws Exception {
        final ByteBuf raw = Unpooled.buffer(1).writeByte(42);
        final var handler = new XtreamResponseBodyHandlerResultHandler(EntityCodec.DEFAULT) {
            @Override
            public Flux<ByteBuf> encode(ByteBufAllocator allocator, int version, @Nullable Object data) {
                assertSame(raw, data);
                return super.encode(allocator, version, data).map(buffer -> buffer.setByte(0, version));
            }
        };
        final var response = new Response(body -> Flux.from(body).doOnNext(buffer -> {
            try {
                assertSame(raw, buffer);
                assertEquals(7, buffer.readUnsignedByte());
            } finally {
                buffer.release();
            }
        }).then());
        try {
            handler.handleResult(ReactiveTestFixtures.exchange(response), result(Handler.class, "annotatedMethod", raw)).block(TIMEOUT);
            assertEquals(0, raw.refCnt());
        } finally {
            releaseRemaining(raw);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void releasesRawResponseWhenOutboundDoesNotAcquireIt(boolean rejection) throws Exception {
        final ByteBuf raw = Unpooled.buffer(1).writeByte(42);
        final var failure = new IllegalStateException("出站拒绝");
        try (var codec = new RecordingCodec()) {
            final var handler = new XtreamResponseBodyHandlerResultHandler(codec);
            final var response = new Response(ignored -> rejection ? Mono.error(failure) : Mono.never());
            final var send = handler.handleResult(ReactiveTestFixtures.exchange(response), result(Handler.class, "annotatedMethod", raw));

            if (rejection) {
                assertSame(failure, assertThrows(IllegalStateException.class, () -> send.block(TIMEOUT)));
            } else {
                send.subscribe().dispose();
            }
            assertEquals(0, raw.refCnt());
        } finally {
            releaseRemaining(raw);
        }
    }

    private static void releaseRemaining(ByteBuf buffer) {
        if (buffer.refCnt() > 0) {
            buffer.release();
        }
    }

    private static DefaultXtreamCommandSender sender(RecordingCodec codec, Function<Publisher<? extends ByteBuf>, Mono<Void>> write) {
        return new DefaultXtreamCommandSender(ByteBufAllocator.DEFAULT, sessionManager(write), codec);
    }

    private static DefaultXtreamSessionManager sessionManager(Function<Publisher<? extends ByteBuf>, Mono<Void>> write) {
        return new DefaultXtreamSessionManager(false, null, ignored -> "session") {
            @Override
            public Mono<XtreamSession> getSessionById(String id) {
                assertEquals("session", id);
                return Mono.just(new DefaultXtreamSession(id, XtreamInbound.Type.TCP, null, ADDRESS, this) {
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends ByteBuf> body) {
                        return write.apply(body);
                    }
                });
            }
        };
    }

    private static void verifyAndRelease(ByteBuf buffer, int version, int value) {
        try {
            assertEquals(version, buffer.readUnsignedByte());
            assertEquals(value, buffer.readUnsignedByte());
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    private static XtreamHandlerResult result(Class<?> owner, String method, @Nullable Object value) throws NoSuchMethodException {
        return new XtreamHandlerResult(owner, value, new XtreamMethodParameter(-1, owner.getMethod(method)));
    }

    private static final class Response extends DefaultXtreamResponse {
        private final Function<Publisher<? extends ByteBuf>, Mono<Void>> write;

        private Response(Function<Publisher<? extends ByteBuf>, Mono<Void>> write) {
            super(ByteBufAllocator.DEFAULT, null, XtreamInbound.Type.TCP, ADDRESS);
            this.write = write;
        }

        @Override
        public Mono<Void> writeWith(Publisher<? extends ByteBuf> body) {
            return this.write.apply(body);
        }
    }

    private static final class RecordingCodec extends EntityCodec implements AutoCloseable {
        private final List<ByteBuf> buffers = new ArrayList<>();

        private RecordingCodec() {
            super(new SimpleBeanMetadataRegistry(new DefaultFieldCodecRegistry(), new XtreamCacheableClassPredicate.Default()));
        }

        @Override
        public void encode(int version, Object instance, ByteBuf target) {
            this.buffers.add(target);
            final int value = (Integer) instance;
            target.writeByte(version);
            if (value < 0) {
                throw new IllegalArgumentException("测试编码失败");
            }
            target.writeByte(value);
        }

        @Override
        public void close() {
            for (ByteBuf buffer : this.buffers) {
                if (buffer.refCnt() > 0) {
                    buffer.release();
                }
            }
        }
    }

    @XtreamResponseBody
    public static final class AnnotatedHandler {
        public String handle() {
            return "";
        }
    }

    @XtreamResponseBody
    public static final class AnnotatedPayload {
    }

    public static final class Handler {
        @XtreamResponseBody
        public String annotatedMethod() {
            return "";
        }

        public AnnotatedPayload annotatedReturnType() {
            return new AnnotatedPayload();
        }

        public Mono<AnnotatedPayload> annotatedMonoType() {
            return Mono.empty();
        }

        public Flux<AnnotatedPayload> annotatedFluxType() {
            return Flux.empty();
        }

        public String unannotated() {
            return "";
        }
    }
}
