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
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

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

    public SchedulerType getType() {
        return type;
    }

    public XtreamServerSchedulerProperties setType(SchedulerType type) {
        this.type = type;
        return this;
    }

    public Map<String, Serializable> getMetadata() {
        return metadata;
    }

    public XtreamServerSchedulerProperties setMetadata(Map<String, Serializable> metadata) {
        this.metadata = metadata;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public XtreamServerSchedulerProperties setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public MetricsProps getMetrics() {
        return metrics;
    }

    public XtreamServerSchedulerProperties setMetrics(MetricsProps metrics) {
        this.metrics = metrics;
        return this;
    }

    public BoundedElasticProperties getBoundedElastic() {
        return boundedElastic;
    }

    public XtreamServerSchedulerProperties setBoundedElastic(BoundedElasticProperties boundedElastic) {
        this.boundedElastic = boundedElastic;
        return this;
    }

    public ParallelProperties getParallel() {
        return parallel;
    }

    public XtreamServerSchedulerProperties setParallel(ParallelProperties parallel) {
        this.parallel = parallel;
        return this;
    }

    public SingleProperties getSingle() {
        return single;
    }

    public XtreamServerSchedulerProperties setSingle(SingleProperties single) {
        this.single = single;
        return this;
    }

    public VirtualThreadProperties getVirtual() {
        return virtual;
    }

    public XtreamServerSchedulerProperties setVirtual(VirtualThreadProperties virtual) {
        this.virtual = virtual;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", XtreamServerSchedulerProperties.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("metadata=" + metadata)
                .add("remark='" + remark + "'")
                .add("metrics=" + metrics)
                .add("boundedElastic=" + boundedElastic)
                .add("parallel=" + parallel)
                .add("single=" + single)
                .add("virtual=" + virtual)
                .toString();
    }

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

    public static class MetricsProps {
        private boolean enabled = false;
        // todo prefix 格式验证
        private String prefix;

        public boolean isEnabled() {
            return enabled;
        }

        public MetricsProps setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getPrefix() {
            return prefix;
        }

        public MetricsProps setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", MetricsProps.class.getSimpleName() + "[", "]")
                    .add("enabled=" + enabled)
                    .add("prefix='" + prefix + "'")
                    .toString();
        }
    }

}
