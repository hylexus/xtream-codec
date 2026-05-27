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

package io.github.hylexus.xtream.codec.server.reactive.spec.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry.SCHEDULER_NAME_BLOCKING;
import static io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry.SCHEDULER_NAME_NON_BLOCKING;
import static org.junit.jupiter.api.Assertions.*;

class DefaultXtreamSchedulerRegistryTest {

    private DefaultXtreamSchedulerRegistry registry;
    private Scheduler defaultBlockingScheduler;
    private Scheduler defaultNonBlockingScheduler;
    private Scheduler eventPublisherScheduler;

    @BeforeEach
    void setUp() {
        defaultBlockingScheduler = Schedulers.parallel();
        defaultNonBlockingScheduler = Schedulers.single();
        eventPublisherScheduler = Schedulers.single();
        registry = new DefaultXtreamSchedulerRegistry(defaultNonBlockingScheduler, defaultBlockingScheduler, eventPublisherScheduler);
    }

    @Test
    void testGetSchedulerShouldReturnDefaultNonBlockingScheduler() {
        assertSame(defaultNonBlockingScheduler, registry.defaultNonBlockingScheduler());
        assertEquals(3, registry.asMapView().size());
    }

    @Test
    void testGetSchedulerShouldReturnDefaultBlockingScheduler() {
        assertSame(defaultBlockingScheduler, registry.defaultBlockingScheduler());
        assertEquals(3, registry.asMapView().size());
    }

    @Test
    void testGetSchedulerShouldReturnEventPublisherScheduler() {
        assertSame(eventPublisherScheduler, registry.eventPublisherScheduler());
        assertEquals(3, registry.asMapView().size());
    }

    @Test
    void testGetSchedulerNonRegisteredSchedulerShouldReturnEmpty() {
        assertEquals(Optional.empty(), registry.getScheduler("nonExistentScheduler"));
        assertEquals(Optional.empty(), registry.getScheduler("nonExistentScheduler2"));
        assertEquals(Optional.empty(), registry.getScheduler("nonExistentScheduler3"));
        assertEquals(3, registry.asMapView().size());
    }

    @Test
    void testRegisterSchedulerShouldRegisterSchedulerSuccessfully() {
        final String schedulerName = "testScheduler";
        final Scheduler scheduler = Schedulers.parallel();
        final Scheduler scheduler2 = Schedulers.single();
        assertTrue(registry.registerScheduler(schedulerName, scheduler));
        assertFalse(registry.registerScheduler(schedulerName, scheduler2));
        assertEquals(Optional.of(scheduler), registry.getScheduler(schedulerName));
        assertEquals(4, registry.asMapView().size());
    }

    @Test
    void testAsMapViewShouldReturnUnmodifiableMapView() {
        Map<String, Scheduler> view = registry.asMapView();
        assertThrows(UnsupportedOperationException.class, () -> view.put("test", Schedulers.parallel()));
        assertEquals(3, view.size());
    }


    @Test
    void testRegisteredSchedulersShouldBePresentInMapView() {
        assertTrue(registry.registerScheduler("testScheduler1", Schedulers.parallel()));
        assertFalse(registry.registerScheduler("testScheduler1", Schedulers.parallel()));
        assertTrue(registry.registerScheduler("testScheduler2", Schedulers.single()));
        assertFalse(registry.registerScheduler("testScheduler2", Schedulers.single()));
        Map<String, Scheduler> view = registry.asMapView();
        assertEquals(5, view.size());
        assertTrue(view.containsKey("testScheduler1"));
        assertTrue(view.containsKey("testScheduler2"));
    }


    @Test
    void testReactiveStyleExecutionOnNonBlockingScheduler() {
        AtomicBoolean executed = new AtomicBoolean(false);
        registry.defaultNonBlockingScheduler().schedule(() -> executed.set(true));
        assertEventuallyTrue(executed);
    }

    @Test
    void testReactiveStyleExecutionOnBlockingScheduler() {
        AtomicBoolean executed = new AtomicBoolean(false);
        registry.defaultBlockingScheduler().schedule(() -> executed.set(true));
        assertEventuallyTrue(executed);
    }

    @Test
    void testSchedulerShouldNotBeRemovedOnceRegistered() {
        final String schedulerName = "testScheduler";
        final Scheduler scheduler = Schedulers.parallel();

        registry.registerScheduler(schedulerName, scheduler);
        assertTrue(registry.getScheduler(schedulerName).isPresent());

        // Direct removal from map
        registry.schedulerMap.remove(schedulerName);
        assertTrue(registry.getScheduler(schedulerName).isEmpty());
    }

    @Test
    void testRemoveScheduler() {
        assertTrue(registry.registerScheduler("testScheduler", Schedulers.parallel()));
        assertEquals(4, registry.asMapView().size());

        assertTrue(registry.removeScheduler("testScheduler"));
        assertEquals(3, registry.asMapView().size());

        assertFalse(registry.removeScheduler("testScheduler"));
        assertEquals(3, registry.asMapView().size());

        assertFalse(registry.getScheduler("testScheduler").isPresent());

        assertThrows(UnsupportedOperationException.class, () -> registry.removeScheduler(SCHEDULER_NAME_BLOCKING));
        assertThrows(UnsupportedOperationException.class, () -> registry.removeScheduler(SCHEDULER_NAME_NON_BLOCKING));
        assertEquals(3, registry.asMapView().size());
    }

    private void assertEventuallyTrue(AtomicBoolean executed) {
        final long start = System.currentTimeMillis();
        while (!executed.get()) {
            if (System.currentTimeMillis() - start > 1000) {
                fail("Condition was not met within timeout");
            }
        }
    }
}
