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

package io.github.hylexus.xtream.codec.ext.jt808.boot.properties;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSchedulerRegistry;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.scheduler.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@SuppressWarnings("NullAway")
public class XtreamServerSchedulerProperties {

    protected SchedulerType type = SchedulerType.BOUNDED_ELASTIC;

    protected Map<String, Serializable> metadata = new LinkedHashMap<>();
    protected String remark;
    /**
     * @see <a href="https://projectreactor.io/docs/core/milestone/reference/metrics.html">https://projectreactor.io/docs/core/milestone/reference/metrics.html</a>
     */
    protected MetricsProps metrics = new MetricsProps();

    @NestedConfigurationProperty
    protected BoundedElasticProperties boundedElastic = new BoundedElasticProperties();

    @NestedConfigurationProperty
    protected ParallelProperties parallel = new ParallelProperties();

    @NestedConfigurationProperty
    protected SingleProperties single = new SingleProperties();

    @NestedConfigurationProperty
    protected VirtualThreadProperties virtual = new VirtualThreadProperties();

    @SuppressWarnings("deprecation")
    public XtreamSchedulerRegistry.SchedulerConfig toSchedulerConfig(String name) {
        final XtreamSchedulerRegistry.SchedulerConfigBuilder builder = XtreamSchedulerRegistry.SchedulerConfig.newBuilder()
                .name(name)
                .metricsEnabled(this.getMetrics().isEnabled())
                .metricsPrefix(this.getMetrics().getPrefix())
                .metadata(this.getMetadata())
                .remark(this.getRemark());
        switch (this.type) {
            case VIRTUAL -> builder.virtualThread(true);
            case PARALLEL -> builder.rejectBlocking(this.getParallel().isRejectBlockingTask());
            case BOUNDED_ELASTIC -> builder.rejectBlocking(this.getBoundedElastic().isRejectBlockingTask());
            case SINGLE -> builder.rejectBlocking(this.getSingle().isRejectBlockingTask());
            case null, default -> builder.virtualThread(false).rejectBlocking(true);
        }
        return builder.build();
    }

    @Getter
    @Setter
    @ToString
    public static class MetricsProps {
        private boolean enabled = false;
        // todo prefix 格式验证
        private String prefix;
    }

}
