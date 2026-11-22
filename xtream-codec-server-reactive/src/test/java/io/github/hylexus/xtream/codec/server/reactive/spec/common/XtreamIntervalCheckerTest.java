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


package io.github.hylexus.xtream.codec.server.reactive.spec.common;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class XtreamIntervalCheckerTest {
    private static final Duration INTERVAL = Duration.ofMillis(20);
    private static final Duration BACKOFF = Duration.ofSeconds(1);

    @Test
    void retriesFailedCheckAndRunsTheNextCheck() throws Exception {
        final var calls = new AtomicInteger();
        final var failedAt = new CompletableFuture<Long>();
        final var retriedAt = new CompletableFuture<Long>();
        final var nextTick = new CompletableFuture<Long>();
        final var checker = new XtreamIntervalChecker("retry-check", INTERVAL, BACKOFF, (name, tick) -> {
            // ...
            return switch (calls.incrementAndGet()) {
                case 1 -> {
                    failedAt.complete(System.nanoTime());
                    yield Mono.error(new IllegalStateException("本轮检查失败"));
                }
                case 2 -> {
                    retriedAt.complete(System.nanoTime());
                    yield Mono.empty();
                }
                default -> {
                    nextTick.complete(tick);
                    yield Mono.empty();
                }
            };
        });
        checker.start();
        try {
            final long delay = retriedAt.get(4, TimeUnit.SECONDS) - failedAt.get(1, TimeUnit.SECONDS);
            assertTrue(delay >= BACKOFF.toNanos());
            assertTrue(nextTick.get(2, TimeUnit.SECONDS) > 0);
        } finally {
            checker.stop();
        }
    }

    @Test
    void repeatedStartDoesNotLeaveAnotherCheckRunningAfterStopAndRestart() throws Exception {
        final var entered = new CountDownLatch(1);
        final var firstCancelled = new CountDownLatch(1);
        final var restartEntered = new CompletableFuture<Thread>();
        final var restartCancelled = new CountDownLatch(1);
        final var firstWorker = new CompletableFuture<Thread>();
        final var stage = new AtomicInteger(0);
        final var firstCheck = Sinks.<Void>one();
        final var checker = new XtreamIntervalChecker("restart-check", INTERVAL, BACKOFF, (name, tick) -> {
            if (stage.get() == 0) {
                firstWorker.complete(Thread.currentThread());
                return firstCheck.asMono().doOnSubscribe(ignored -> entered.countDown()).doOnCancel(firstCancelled::countDown);
            }
            restartEntered.complete(Thread.currentThread());
            return Mono.<Void>never().doOnCancel(restartCancelled::countDown);
        });
        checker.start();
        try {
            assertTrue(entered.await(2, TimeUnit.SECONDS));
            checker.start();
            checker.stop();
            assertTrue(firstCancelled.await(1, TimeUnit.SECONDS), "第一次启动的运行中任务应被取消");
            assertTrue(firstWorker.get(1, TimeUnit.SECONDS).join(Duration.ofSeconds(2)));

            stage.set(1);
            checker.start();
            final var restarted = restartEntered.get(2, TimeUnit.SECONDS);
            checker.stop();
            assertTrue(restartCancelled.await(1, TimeUnit.SECONDS));
            assertTrue(restarted.join(Duration.ofSeconds(2)));
        } finally {
            checker.stop();
            firstCheck.tryEmitEmpty();
        }
    }
}
