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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.configuration.database.mysql;


import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageDescriptionRegistry;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.configuration.props.QuickStartAppProps;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.mapper.mysql.Jt808AlarmAttachmentInfoMapperMysql;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.mapper.mysql.Jt808RequestTraceLogMapperMysql;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.mapper.mysql.Jt808ResponseTraceLogMapperMysql;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.AttachmentInfoService;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.TraceLogService;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl.mysql.AttachmentInfoServiceMysqlImpl;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl.mysql.TraceLogServiceMysqlImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "demo-app-with-storage.feature-control.database.mysql", name = "enabled", havingValue = "true")
public class DemoMysqlConfiguration {

    @Bean
    TraceLogService traceLogServiceMysqlImpl(
            Jt808MessageDescriptionRegistry descriptionRegistry,
            Jt808RequestTraceLogMapperMysql requestTraceLogMapper,
            Jt808ResponseTraceLogMapperMysql responseTraceLogMapper) {
        return new TraceLogServiceMysqlImpl(descriptionRegistry, requestTraceLogMapper, responseTraceLogMapper);
    }

    @Bean
    AttachmentInfoService attachmentInfoServiceMysqlImpl(Jt808AlarmAttachmentInfoMapperMysql mapper, QuickStartAppProps appProps) {
        return new AttachmentInfoServiceMysqlImpl(mapper, appProps.getAttachmentBasePreviewUrl());
    }
}
