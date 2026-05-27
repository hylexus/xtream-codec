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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.configuration.oss.minio;

import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.configuration.props.QuickStartAppProps;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.MinioService;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.ObjectStorageService;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.impl.MinioServiceImpl;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.impl.oss.ObjectStorageServiceMinioImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "demo-app-with-storage.feature-control.oss.minio", name = "enabled", havingValue = "true")
public class DemoMinioOssConfiguration {

    @Bean
    MinioService minioService(QuickStartAppProps quickStartAppProps) {
        return new MinioServiceImpl(quickStartAppProps);
    }

    @Bean
    ObjectStorageService objectStorageServiceMinioImpl(MinioService minioService) {
        return new ObjectStorageServiceMinioImpl(minioService);
    }

    @Bean
    DemoMinioInitializer minioInitializer(QuickStartAppProps quickStartAppProps, MinioService minioService) {
        return new DemoMinioInitializer(quickStartAppProps, minioService);
    }
}
