/*
 * Copyright 2024 the original author or authors.
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

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.logging.Level;

/**
 * 当前类是从 `de.codecentric.boot.admin.server.services.IntervalCheck` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `de.codecentric.boot.admin.server.services.IntervalCheck`.
 *
 * @author hylexus
 */
public class XtreamIntervalChecker {

    private static final Logger log = LoggerFactory.getLogger(XtreamIntervalChecker.class);

    private final String name;

    private Duration interval;
    private Duration maxBackoff;
    private @Nullable Disposable subscription;

    private @Nullable Scheduler scheduler;
    private final BiFunction<String, Long, Mono<Void>> checkFn;

    public XtreamIntervalChecker(String name, Duration interval, Duration maxBackoff, BiFunction<String, Long, Mono<Void>> checkFn) {
        this.name = name;
        this.checkFn = checkFn;
        this.interval = interval;
        this.maxBackoff = maxBackoff;
    }

    public void start() {
        this.scheduler = Schedulers.newSingle(this.name);
        this.subscription = Flux.interval(this.interval)
                .doOnSubscribe((s) -> log.debug("Scheduled {}-check every {}", this.name, this.interval))
                .log(log.getName(), Level.FINEST)
                .publishOn(this.scheduler)
                .concatMap(this::doCheck)
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
                        .maxBackoff(maxBackoff)
                        .doBeforeRetry((s) -> log.warn("Unexpected error in {}-check", this.name, s.failure()))
                )
                .subscribe();
    }

    protected Mono<Void> doCheck(Long i) {
        log.debug("check {} for all instances", this.name);
        return this.checkFn.apply(this.name, i).then();
    }

    public void stop() {
        if (this.subscription != null) {
            this.subscription.dispose();
            this.subscription = null;
        }
        if (this.scheduler != null) {
            this.scheduler.dispose();
            this.scheduler = null;
        }
    }

    @SuppressWarnings("lombok")
    public void setInterval(Duration interval) {
        this.interval = interval;
    }

    @SuppressWarnings("lombok")
    public XtreamIntervalChecker setMaxBackoff(Duration maxBackoff) {
        this.maxBackoff = maxBackoff;
        return this;
    }

}
