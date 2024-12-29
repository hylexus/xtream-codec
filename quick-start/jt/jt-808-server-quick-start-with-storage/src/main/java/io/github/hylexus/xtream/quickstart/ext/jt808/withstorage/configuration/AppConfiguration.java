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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.configuration;

import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.pubsub.Jt808TraceLogSubscriber;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.service.AttachmentInfoService;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.service.ObjectStorageService;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.service.TraceLogService;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.service.impl.composite.AttachmentInfoServiceComposite;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.service.impl.composite.ObjectStorageServiceComposite;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.service.impl.composite.TraceLogServiceComposite;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * @author hylexus
 */
@Configuration
public class AppConfiguration {

    @Bean
    @Primary
    TraceLogService traceLogService(List<TraceLogService> traceLogServiceList) {
        return new TraceLogServiceComposite(traceLogServiceList);
    }

    @Bean
    @Primary
    AttachmentInfoService attachmentInfoService(List<AttachmentInfoService> attachmentInfoServices) {
        return new AttachmentInfoServiceComposite(attachmentInfoServices);
    }

    @Bean
    @Primary
    ObjectStorageService objectStorageService(List<ObjectStorageService> objectStorageServices) {
        return new ObjectStorageServiceComposite(objectStorageServices);
    }

    @Bean
    public CommandLineRunner jt808TraceLogSubscriberCommandLineRunner(Jt808TraceLogSubscriber subscriber) {
        return args -> {
            // traceLog 订阅
            subscriber.subscribe();
        };
    }

}
