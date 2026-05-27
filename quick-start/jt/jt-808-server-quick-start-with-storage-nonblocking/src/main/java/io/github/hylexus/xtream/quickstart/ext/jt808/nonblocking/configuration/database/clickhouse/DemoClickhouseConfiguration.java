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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.configuration.database.clickhouse;


import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageDescriptionRegistry;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.configuration.props.QuickStartAppProps;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.mapper.clickhouse.Jt808AlarmAttachmentInfoMapperClickhouse;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.mapper.clickhouse.Jt808RequestTraceLogMapperClickhouse;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.mapper.clickhouse.Jt808ResponseTraceLogMapperClickhouse;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.AttachmentInfoService;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.TraceLogService;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.impl.clickhouse.AttachmentInfoServiceClickhouseImpl;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.impl.clickhouse.TraceLogServiceClickhouseImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "demo-app-with-storage.feature-control.database.clickhouse", name = "enabled", havingValue = "true")
public class DemoClickhouseConfiguration {

    @Bean
    TraceLogService traceLogServiceClickhouseImpl(
            Jt808MessageDescriptionRegistry descriptionRegistry,
            Jt808RequestTraceLogMapperClickhouse requestTraceLogMapper,
            Jt808ResponseTraceLogMapperClickhouse responseTraceLogMapper) {
        return new TraceLogServiceClickhouseImpl(descriptionRegistry, requestTraceLogMapper, responseTraceLogMapper);
    }

    @Bean
    AttachmentInfoService attachmentInfoServiceClickhouseImpl(
            Jt808AlarmAttachmentInfoMapperClickhouse mapper,
            QuickStartAppProps appProps) {
        return new AttachmentInfoServiceClickhouseImpl(mapper, appProps.getAttachmentBasePreviewUrl());
    }

}
