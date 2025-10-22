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

package io.github.hylexus.xtream.codec.server.reactive.spec.resources;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.NonBlocking;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 * 当前类是从 `reactor.core.scheduler.ReactorThreadFactory` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `reactor.core.scheduler.ReactorThreadFactory`.
 *
 * @author hylexus
 */
public class XtreamReactorThreadFactory implements ThreadFactory,
        Thread.UncaughtExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(XtreamReactorThreadFactory.class);
    private final String name;
    private final AtomicLong counterReference = new AtomicLong();
    private final boolean daemon;
    private final boolean rejectBlocking;

    private final @Nullable BiConsumer<Thread, Throwable> uncaughtExceptionHandler;

    public XtreamReactorThreadFactory(
            String name,
            boolean daemon,
            boolean rejectBlocking) {

        this(name, daemon, rejectBlocking, null);
    }

    public XtreamReactorThreadFactory(
            String name,
            boolean daemon,
            boolean rejectBlocking,
            @Nullable BiConsumer<Thread, Throwable> uncaughtExceptionHandler) {
        this.name = name;
        this.daemon = daemon;
        this.rejectBlocking = rejectBlocking;
        this.uncaughtExceptionHandler = Objects.requireNonNullElseGet(
                uncaughtExceptionHandler,
                () -> (thread, throwable) -> {
                    // ...
                    log.error("Scheduler worker in group {} failed with an uncaught exception", thread.getThreadGroup().getName(), throwable);
                }
        );
    }

    @Override
    public final Thread newThread(Runnable runnable) {
        String newThreadName = name + "-" + counterReference.incrementAndGet();
        Thread t = rejectBlocking
                ? new NonBlockingThread(runnable, newThreadName)
                : new Thread(runnable, newThreadName);
        if (daemon) {
            t.setDaemon(true);
        }
        if (uncaughtExceptionHandler != null) {
            t.setUncaughtExceptionHandler(this);
        }
        return t;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (uncaughtExceptionHandler == null) {
            return;
        }

        uncaughtExceptionHandler.accept(t, e);
    }

    static final class NonBlockingThread extends Thread implements NonBlocking {

        public NonBlockingThread(Runnable target, String name) {
            super(target, name);
        }

    }
}
