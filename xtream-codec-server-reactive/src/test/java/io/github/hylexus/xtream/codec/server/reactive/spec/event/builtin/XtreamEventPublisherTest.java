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

import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEvent;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventSubscriberInfo;
import io.github.hylexus.xtream.codec.server.reactive.spec.resources.DefaultXtreamSchedulerRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEvent.XtreamEventType.AFTER_REQUEST_RECEIVED;
import static io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEvent.XtreamEventType.BEFORE_RESPONSE_SEND;
import static org.junit.jupiter.api.Assertions.*;

class XtreamEventPublisherTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    @Test
    void interestStartsAtSubscriptionAndStopsAtCancellation() {
        final var publisher = publisher();
        final var listener = XtreamEventSubscriberInfo.of("request", Set.of(AFTER_REQUEST_RECEIVED), Map.of());
        final Flux<XtreamEvent> events = publisher.subscribe(listener);

        assertEquals(0, publisher.subscriberCount());
        assertFalse(publisher.eventPredicate().test(AFTER_REQUEST_RECEIVED));
        final var first = events.subscribe();
        try {
            assertEquals(List.of(listener), publisher.subscriberView().toList());
            assertTrue(publisher.eventPredicate().test(AFTER_REQUEST_RECEIVED));
        } finally {
            first.dispose();
        }
        assertEquals(0, publisher.subscriberCount());
        assertFalse(publisher.eventPredicate().test(AFTER_REQUEST_RECEIVED));
    }

    @Test
    void duplicateIdIsRejectedWhileActiveAndCanBeReusedAfterCancellation() {
        final var publisher = publisher();
        final var listener = XtreamEventSubscriberInfo.of("request", Set.of(AFTER_REQUEST_RECEIVED), Map.of());
        final var events = publisher.subscribe(listener);
        final var first = events.subscribe();
        try {
            assertThrows(IllegalStateException.class, () -> events.blockFirst(TIMEOUT));
            assertEquals(1, publisher.subscriberCount());
        } finally {
            first.dispose();
        }
        final var second = events.subscribe();
        try {
            assertTrue(publisher.eventPredicate().test(AFTER_REQUEST_RECEIVED));
            assertEquals(List.of(listener), publisher.subscriberView().toList());
        } finally {
            second.dispose();
        }
        assertEquals(0, publisher.subscriberCount());
    }

    @Test
    void skipsPayloadCreationWithoutInterestAndFiltersEventsByType() {
        final var publisher = publisher();
        final var payloads = new AtomicInteger();
        final List<Object> specific = new ArrayList<>();
        final List<Object> all = new ArrayList<>();
        final var request = publisher.subscribe(XtreamEventSubscriberInfo.of(Set.of(AFTER_REQUEST_RECEIVED), Map.of())).subscribe(e -> specific.add(e.payload()));
        try {
            publisher.publishIfNecessary(BEFORE_RESPONSE_SEND, payloads::incrementAndGet);
            assertEquals(0, payloads.get());
            final var wildcard = publisher.subscribeAll(Map.of("source", "test")).subscribe(e -> all.add(e.payload()));
            try {
                assertTrue(publisher.eventPredicate().test(BEFORE_RESPONSE_SEND));
                publisher.publishIfNecessary(AFTER_REQUEST_RECEIVED, payloads::incrementAndGet);
                publisher.publishIfNecessary(BEFORE_RESPONSE_SEND, payloads::incrementAndGet);
                assertEquals(List.of(1), specific);
                assertEquals(List.of(1, 2), all);
            } finally {
                wildcard.dispose();
            }
            assertFalse(publisher.eventPredicate().test(BEFORE_RESPONSE_SEND));
            assertTrue(publisher.eventPredicate().test(AFTER_REQUEST_RECEIVED));
        } finally {
            request.dispose();
        }
    }

    @Test
    void oneOfTwoSubscribersCancellingDoesNotRemoveTheSharedInterest() {
        final var publisher = publisher();
        final var first = publisher.subscribe(XtreamEventSubscriberInfo.of(Set.of(AFTER_REQUEST_RECEIVED), Map.of())).subscribe();
        final var second = publisher.subscribe(XtreamEventSubscriberInfo.of(Set.of(AFTER_REQUEST_RECEIVED), Map.of())).subscribe();
        try {
            first.dispose();
            assertEquals(1, publisher.subscriberCount());
            assertTrue(publisher.eventPredicate().test(AFTER_REQUEST_RECEIVED));
        } finally {
            first.dispose();
            second.dispose();
        }
        assertFalse(publisher.eventPredicate().test(AFTER_REQUEST_RECEIVED));
    }

    @Test
    void rejectsEmptyInterestAndClearsActiveSubscriptionsOnShutdown() throws Exception {
        final var publisher = publisher();

        assertThrows(IllegalArgumentException.class,
                () -> publisher.subscribe(XtreamEventSubscriberInfo.of(Set.of(), Map.of())).blockFirst(TIMEOUT));
        final var done = publisher.subscribeAll(Map.of()).then().toFuture();
        assertEquals(1, publisher.subscriberCount());
        publisher.shutdown();
        done.get(2, TimeUnit.SECONDS);
        assertEquals(0, publisher.subscriberCount());
        assertFalse(publisher.eventPredicate().test(AFTER_REQUEST_RECEIVED));
    }

    @Test
    void defaultPublisherStopsItsPublishingThreadOnShutdown() throws Exception {
        final var publisher = new DefaultXtreamEventPublisher(registry(), "event-publisher-test");
        final CompletableFuture<Thread> publishedBy = new CompletableFuture<>();
        final var subscription = publisher.subscribeAll(Map.of()).subscribe(event -> publishedBy.complete(Thread.currentThread()));
        final Thread worker;
        try {
            publisher.publishIfNecessary(AFTER_REQUEST_RECEIVED, () -> "测试事件");
            worker = publishedBy.get(2, TimeUnit.SECONDS);
            assertNotSame(Thread.currentThread(), worker);
        } finally {
            publisher.shutdown();
            subscription.dispose();
        }
        assertTrue(worker.join(TIMEOUT), "关闭后发布线程应退出");
        assertDoesNotThrow(() -> publisher.publishIfNecessary(AFTER_REQUEST_RECEIVED, () -> fail("关闭后无需生产事件")));
    }

    private static AbstractXtreamEventPublisher publisher() {
        return new AbstractXtreamEventPublisher(registry()) {
        };
    }

    @Test
    void defaultPublisherCompletesSubscriptionsWhenClosedDuringDelivery() throws Exception {
        final var publisher = new DefaultXtreamEventPublisher(registry(), "event-close-race-test");
        final var release = new CountDownLatch(1);
        final var worker = new CompletableFuture<Thread>();
        final var finished = publisher.subscribeAll(Map.of())
                .doOnNext(event -> {
                    worker.complete(Thread.currentThread());
                    awaitRelease(release);
                })
                .then().toFuture();
        try {
            publisher.publishIfNecessary(AFTER_REQUEST_RECEIVED, () -> "测试事件");
            final Thread publishing = worker.get(2, TimeUnit.SECONDS);
            publisher.shutdown();
            release.countDown();
            finished.get(2, TimeUnit.SECONDS);
            assertTrue(publishing.join(TIMEOUT));
            assertEquals(0, publisher.subscriberCount());
        } finally {
            release.countDown();
            finished.cancel(false);
            publisher.shutdown();
        }
    }

    @Test
    void disruptorDrainsQueuedEventsAndTerminatesAllSubscribers() throws Exception {
        final var publisher = new DisruptorBasedXtreamEventPublisher(registry(), 16,
                Thread.ofPlatform().daemon().name("disruptor-close-test").factory());
        final var release = new CountDownLatch(1);
        final var worker = new CompletableFuture<Thread>();
        final var closing = new CountDownLatch(1);
        final var finished = publisher.subscribeAll(Map.of())
                .doOnNext(event -> {
                    if ("first".equals(event.payload())) {
                        worker.complete(Thread.currentThread());
                        awaitRelease(release);
                    }
                })
                .map(XtreamEvent::payload).collectList().toFuture();
        final var shutdown = new CompletableFuture<Void>();
        try {
            publisher.publishIfNecessary(AFTER_REQUEST_RECEIVED, () -> "first");
            final Thread publishing = worker.get(2, TimeUnit.SECONDS);
            publisher.publishIfNecessary(AFTER_REQUEST_RECEIVED, () -> "second");
            Thread.ofVirtual().start(() -> {
                closing.countDown();
                try {
                    publisher.shutdown();
                    shutdown.complete(null);
                } catch (Throwable error) {
                    shutdown.completeExceptionally(error);
                }
            });
            assertTrue(closing.await(2, TimeUnit.SECONDS));
            release.countDown();
            shutdown.get(2, TimeUnit.SECONDS);
            assertEquals(List.of("first", "second"), finished.get(2, TimeUnit.SECONDS));
            assertTrue(publishing.join(TIMEOUT));
            assertEquals(0, publisher.subscriberCount());
        } finally {
            release.countDown();
            finished.cancel(false);
            publisher.shutdown();
        }
    }

    @Test
    void disruptorCanShutDownFromItsSubscriberCallback() throws Exception {
        final var publisher = new DisruptorBasedXtreamEventPublisher(registry(), 2,
                Thread.ofPlatform().daemon().name("disruptor-callback-close-test").factory());
        final var requestShutdown = new CountDownLatch(1);
        final var dispatcher = new CompletableFuture<Thread>();
        final var shutdownReturned = new CompletableFuture<Void>();
        final var received = publisher.subscribeAll(Map.of())
                .doOnNext(event -> {
                    if ("first".equals(event.payload())) {
                        dispatcher.complete(Thread.currentThread());
                        awaitRelease(requestShutdown);
                        publisher.shutdown();
                        publisher.publish(DefaultXtreamEvent.of(AFTER_REQUEST_RECEIVED, "late"));
                        shutdownReturned.complete(null);
                    }
                })
                .map(XtreamEvent::payload).collectList().toFuture();
        try {
            publisher.publish(DefaultXtreamEvent.of(AFTER_REQUEST_RECEIVED, "first"));
            final var worker = dispatcher.get(2, TimeUnit.SECONDS);
            assertEquals("disruptor-callback-close-test", worker.getName());
            publisher.publish(DefaultXtreamEvent.of(AFTER_REQUEST_RECEIVED, "second"));
            requestShutdown.countDown();

            shutdownReturned.get(2, TimeUnit.SECONDS);
            assertEquals(List.of("first", "second"), received.get(2, TimeUnit.SECONDS));
            assertTrue(worker.join(TIMEOUT), "回调发起关闭后消费线程应退出");
        } finally {
            requestShutdown.countDown();
            received.cancel(false);
            publisher.shutdown();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void disruptorWaitingProducerResumesOnShutdownOrAvailableCapacity(boolean stopWithFullQueue) throws Exception {
        final var publisher = new DisruptorBasedXtreamEventPublisher(registry(), 2,
                Thread.ofPlatform().daemon().name("disruptor-full-test").factory());
        final var release = new CountDownLatch(1);
        final var worker = new CompletableFuture<Thread>();
        final var received = publisher.subscribeAll(Map.of()).doOnNext(event -> {
            if ("first".equals(event.payload())) {
                worker.complete(Thread.currentThread());
                awaitRelease(release);
            }
        }).map(XtreamEvent::payload).collectList().toFuture();
        final var started = new CountDownLatch(1);
        final var returned = new CompletableFuture<Boolean>();
        final var stopped = new CompletableFuture<Void>();
        final var stopping = new CountDownLatch(1);
        final Runnable stop = () -> {
            stopping.countDown();
            try {
                publisher.shutdown();
                stopped.complete(null);
            } catch (Throwable error) {
                stopped.completeExceptionally(error);
            }
        };
        try {
            publisher.publish(DefaultXtreamEvent.of(AFTER_REQUEST_RECEIVED, "first"));
            final var dispatcher = worker.get(2, TimeUnit.SECONDS);
            publisher.publish(DefaultXtreamEvent.of(AFTER_REQUEST_RECEIVED, "second"));
            final var producer = Thread.ofPlatform().daemon().start(() -> {
                started.countDown();
                try {
                    publisher.publish(DefaultXtreamEvent.of(AFTER_REQUEST_RECEIVED, "third"));
                    returned.complete(Thread.currentThread().isInterrupted());
                } catch (Throwable error) {
                    returned.completeExceptionally(error);
                }
            });
            assertTrue(started.await(2, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> returned.get(100, TimeUnit.MILLISECONDS));

            if (stopWithFullQueue) {
                Thread.ofPlatform().daemon().start(stop);
                assertTrue(stopping.await(2, TimeUnit.SECONDS));
                assertFalse(returned.get(2, TimeUnit.SECONDS));
            } else {
                producer.interrupt();
                assertThrows(TimeoutException.class, () -> returned.get(100, TimeUnit.MILLISECONDS));
            }
            release.countDown();
            assertEquals(!stopWithFullQueue, returned.get(2, TimeUnit.SECONDS));
            if (!stopWithFullQueue) {
                Thread.ofPlatform().daemon().start(stop);
            }
            stopped.get(2, TimeUnit.SECONDS);
            assertEquals(stopWithFullQueue ? List.of("first", "second") : List.of("first", "second", "third"),
                    received.get(2, TimeUnit.SECONDS));
            assertTrue(dispatcher.join(TIMEOUT));
            assertEquals(0, publisher.subscriberCount());
        } finally {
            release.countDown();
            if (stopping.getCount() > 0) {
                Thread.ofPlatform().daemon().start(stop);
            }
            received.cancel(false);
        }
    }

    private static void awaitRelease(CountDownLatch latch) {
        try {
            assertTrue(latch.await(4, TimeUnit.SECONDS));
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        }
    }

    private static DefaultXtreamSchedulerRegistry registry() {
        final var immediate = Schedulers.immediate();
        return new DefaultXtreamSchedulerRegistry(immediate, immediate, immediate);
    }
}
