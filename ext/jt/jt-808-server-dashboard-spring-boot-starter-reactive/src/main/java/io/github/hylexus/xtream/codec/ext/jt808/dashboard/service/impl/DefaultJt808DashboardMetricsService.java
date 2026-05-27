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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.impl;

import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamServerSchedulerProperties;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.actuate.request.Jt808DashboardErrorStore;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.Jt808ServerSimpleMetricsHolder;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.SimpleTypes;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.XtreamServerErrorInfo;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.vo.SimpleMetricsVo;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardMetricsService;
import io.github.hylexus.xtream.codec.server.reactive.spec.event.XtreamEventPublisher;
import org.jspecify.annotations.Nullable;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

public class DefaultJt808DashboardMetricsService implements Jt808DashboardMetricsService {
    private final XtreamEventPublisher eventPublisher;
    private final Jt808ServerSimpleMetricsHolder metricsHolder;
    private final Map<String, String> threadGroupMapping;
    private final Jt808DashboardErrorStore errorStore;

    public DefaultJt808DashboardMetricsService(XtreamJt808ServerProperties serverProperties, XtreamEventPublisher eventPublisher, Jt808ServerSimpleMetricsHolder metricsHolder, Jt808DashboardErrorStore errorStore) {
        this.eventPublisher = eventPublisher;
        this.metricsHolder = metricsHolder;
        this.threadGroupMapping = this.initThreadGroupMapping(serverProperties);
        this.errorStore = errorStore;
    }

    @Override
    public Flux<ServerSentEvent<Object>> getBasicMetrics(Duration duration) {

        final Supplier<SimpleTypes.TimeSeries<SimpleMetricsVo>> supplier = () -> {
            final LocalDateTime now = LocalDateTime.now();
            final SimpleMetricsVo.ErrorMetrics errorMetrics = this.errorMetrics();
            return new SimpleTypes.TimeSeries<>(now, new SimpleMetricsVo(metricsHolder, this.eventPublisher.subscriberCount(), this.simpleJvmThreadMetrics(), errorMetrics));
        };

        return Flux.just(supplier.get())
                .concatWith(Flux.interval(duration).map(ignore -> supplier.get()))
                .map(metricsHolder -> ServerSentEvent.<Object>builder(metricsHolder).build());
    }

    private SimpleMetricsVo.ErrorMetrics errorMetrics() {
        final Map<String, XtreamServerErrorInfo> errorInfoMap = this.errorStore.asMapView();
        return new SimpleMetricsVo.ErrorMetrics(
                errorInfoMap.values().stream().map(XtreamServerErrorInfo::getCount).mapToLong(LongAdder::longValue).sum(),
                errorInfoMap.values()
        );
    }

    @Override
    public Flux<ServerSentEvent<Object>> getThreadDumpMetrics(Duration duration) {
        return this.threadDumpMetricsFlux()
                .concatWith(Flux.interval(duration)
                        .flatMap(ignored -> threadDumpMetricsFlux())
                );
    }


    private Flux<ServerSentEvent<Object>> threadDumpMetricsFlux() {
        final LocalDateTime now = LocalDateTime.now();
        final ThreadInfo[] threadInfos = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
        return Flux.fromStream(Arrays.stream(threadInfos).map(it -> {
            final String groupName = this.detectThreadGroup(it);
            final ThreadDumpInfo dumpInfo = new ThreadDumpInfo(groupName, it);
            return ServerSentEvent.<Object>builder(SimpleTypes.TimeSeries.of(now, dumpInfo)).event("dumpInfo").build();
        })).concatWith(Mono.defer(() -> {
            final SimpleTypes.SimpleJvmThreadMetrics simpleJvmThreadMetrics = this.simpleJvmThreadMetrics();
            return Mono.just(ServerSentEvent.<Object>builder(SimpleTypes.TimeSeries.of(now, simpleJvmThreadMetrics)).event("stateInfo").build());
        }));
    }

    private SimpleTypes.SimpleJvmThreadMetrics simpleJvmThreadMetrics() {
        final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        final Map<String, Long> states = new HashMap<>();
        Arrays.stream(Thread.State.values()).forEach(state -> {
            final long count = getThreadStateCount(threadBean, state);
            states.put(state.name(), count);
        });
        return new SimpleTypes.SimpleJvmThreadMetrics(
                threadBean.getPeakThreadCount(),
                threadBean.getDaemonThreadCount(),
                threadBean.getThreadCount(),
                threadBean.getTotalStartedThreadCount(),
                states
        );
    }

    // Copied from `io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics#getThreadStateCount()`
    static long getThreadStateCount(ThreadMXBean threadBean, Thread.State state) {
        return Arrays.stream(threadBean.getThreadInfo(threadBean.getAllThreadIds()))
                .filter(threadInfo -> threadInfo != null && threadInfo.getThreadState() == state)
                .count();
    }

    private String detectThreadGroup(ThreadInfo threadInfo) {
        return threadGroupMapping.entrySet().stream()
                .filter(entry -> threadInfo.getThreadName().startsWith(entry.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(ThreadGroup.OTHERS.group);
    }

    @SuppressWarnings("deprecation")
    private @Nullable String getThreadNamePrefix(XtreamServerSchedulerProperties schedulerProperties) {
        return switch (schedulerProperties.getType()) {
            case PARALLEL -> schedulerProperties.getParallel().getThreadNamePrefix();
            case BOUNDED_ELASTIC -> schedulerProperties.getBoundedElastic().getThreadNamePrefix();
            case SINGLE -> schedulerProperties.getSingle().getThreadNamePrefix();
            default -> null;
        };
    }

    private Map<String, String> initThreadGroupMapping(XtreamJt808ServerProperties serverProperties) {
        final Map<String, String> mapping = new HashMap<>();

        final XtreamJt808ServerProperties.SchedulerProperties schedulers = serverProperties.getSchedulers();
        String threadNamePrefix = this.getThreadNamePrefix(schedulers.getBlockingHandler());

        if (threadNamePrefix != null) {
            mapping.put(threadNamePrefix, ThreadGroup.XTREAM_HANDLER_BLOCKING.group);
        }

        threadNamePrefix = this.getThreadNamePrefix(schedulers.getNonBlockingHandler());
        if (threadNamePrefix != null) {
            mapping.put(threadNamePrefix, ThreadGroup.XTREAM_HANDLER_NON_BLOCKING.group);
        }

        threadNamePrefix = this.getThreadNamePrefix(schedulers.getRequestDispatcher());
        if (threadNamePrefix != null) {
            mapping.put(threadNamePrefix, ThreadGroup.XTREAM_DISPATCHER.group);
        }

        threadNamePrefix = this.getThreadNamePrefix(schedulers.getEventPublisher());
        if (threadNamePrefix != null) {
            mapping.put(threadNamePrefix, ThreadGroup.XTREAM_EVENT_PUBLISHER.group);
        }

        Optional.of(serverProperties.getInstructionServer())
                .map(XtreamJt808ServerProperties.InstructionServerProps::getTcpServer)
                .map(XtreamJt808ServerProperties.TcpServerProps::getLoopResources)
                .map(XtreamJt808ServerProperties.TcpLoopResourcesProperty::getThreadNamePrefix)
                .ifPresent(prefix -> mapping.put(prefix, ThreadGroup.REACTOR_LOOP_RESOURCES_INSTRUCTION_TCP.group));

        Optional.of(serverProperties.getInstructionServer())
                .map(XtreamJt808ServerProperties.InstructionServerProps::getUdpServer)
                .map(XtreamJt808ServerProperties.UdpServerProps::getLoopResources)
                .map(XtreamJt808ServerProperties.UdpLoopResourcesProperty::getThreadNamePrefix)
                .ifPresent(prefix -> mapping.put(prefix, ThreadGroup.REACTOR_LOOP_RESOURCES_INSTRUCTION_UDP.group));

        Optional.of(serverProperties.getAttachmentServer())
                .map(XtreamJt808ServerProperties.AttachmentServerProps::getTcpServer)
                .map(XtreamJt808ServerProperties.TcpAttachmentServerProps::getLoopResources)
                .map(XtreamJt808ServerProperties.TcpLoopResourcesProperty::getThreadNamePrefix)
                .ifPresent(prefix -> mapping.put(prefix, ThreadGroup.REACTOR_LOOP_RESOURCES_ATTACHMENT_TCP.group));

        Optional.of(serverProperties.getAttachmentServer())
                .map(XtreamJt808ServerProperties.AttachmentServerProps::getUdpServer)
                .map(XtreamJt808ServerProperties.UdpAttachmentServerProps::getLoopResources)
                .map(XtreamJt808ServerProperties.UdpLoopResourcesProperty::getThreadNamePrefix)
                .ifPresent(prefix -> mapping.put(prefix, ThreadGroup.REACTOR_LOOP_RESOURCES_ATTACHMENT_UDP.group));

        schedulers.getCustomSchedulers().forEach((name, props) -> {
            final String prefix = this.getThreadNamePrefix(props);
            if (prefix != null) {
                mapping.put(prefix, ThreadGroup.XTREAM_CUSTOM_SCHEDULER.group);
            }
        });

        return mapping;
    }

    public record ThreadDumpInfo(String group, ThreadInfo dumpInfo) {
    }

    public enum ThreadGroup {
        REACTOR_LOOP_RESOURCES_INSTRUCTION_TCP("reactor_loop_resources_instruction_tcp"),
        REACTOR_LOOP_RESOURCES_ATTACHMENT_TCP("reactor_loop_resources_attachment_tcp"),
        REACTOR_LOOP_RESOURCES_INSTRUCTION_UDP("reactor_loop_resources_instruction_udp"),
        REACTOR_LOOP_RESOURCES_ATTACHMENT_UDP("reactor_loop_resources_attachment_udp"),
        XTREAM_HANDLER_NON_BLOCKING("xtream_handler_non_blocking"),
        XTREAM_HANDLER_BLOCKING("xtream_handler_blocking"),
        XTREAM_DISPATCHER("xtream_dispatcher"),
        XTREAM_EVENT_PUBLISHER("xtream_event_publisher"),
        XTREAM_CUSTOM_SCHEDULER("xtream_custom_scheduler"),
        OTHERS("others"),
        ;
        private final String group;

        ThreadGroup(String group) {
            this.group = group;
        }
    }

}
