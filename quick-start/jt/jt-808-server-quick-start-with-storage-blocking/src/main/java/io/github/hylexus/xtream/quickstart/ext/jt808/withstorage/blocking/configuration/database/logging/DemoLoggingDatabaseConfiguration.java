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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.configuration.database.logging;


import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.AttachmentInfoService;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.TraceLogService;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl.logging.AttachmentInfoServiceLoggingImpl;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl.logging.TraceLogServiceLoggingImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "demo-app-with-storage.feature-control.database.mysql", name = "enabled", havingValue = "true")
public class DemoLoggingDatabaseConfiguration {

    @Bean
    TraceLogService traceLogServiceLoggingImpl() {
        return new TraceLogServiceLoggingImpl();
    }

    @Bean
    AttachmentInfoService attachmentInfoServiceLoggingImpl() {
        return new AttachmentInfoServiceLoggingImpl();
    }

}
