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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl.composite;

import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.ObjectStorageService;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

/**
 * 示例演示而已，实现比较简单粗暴
 *
 * @author hylexus
 */
public class ObjectStorageServiceComposite implements ObjectStorageService {
    private static final Logger log = LoggerFactory.getLogger(ObjectStorageServiceComposite.class);
    private final List<ObjectStorageService> delegates;

    public ObjectStorageServiceComposite(List<ObjectStorageService> delegates) {
        this.delegates = delegates;
    }

    @Override
    public boolean bucketExists(String bucketName) {
        this.doDelegate(service -> service.bucketExists(bucketName));
        return true;
    }

    @Override
    public boolean makeBucket(String bucketName) {
        this.doDelegate(service -> service.makeBucket(bucketName));
        return true;
    }

    @Override
    public boolean makeBucketAccessibleWithAnonymous(String bucketName) {
        this.doDelegate(service -> service.makeBucketAccessibleWithAnonymous(bucketName));
        return true;
    }

    @Override
    public void uploadFile(String bucketName, String localFilePath, @Nullable String objectName, @Nullable String contentType) {
        this.doDelegate(service -> {
            service.uploadFile(bucketName, localFilePath, objectName, contentType);
            return null;
        });
    }

    private void doDelegate(Function<ObjectStorageService, Object> function) {
        for (final ObjectStorageService delegate : this.delegates) {
            try {
                function.apply(delegate);
            } catch (Throwable e) {
                log.error("error occurred while do delegate", e);
            }
        }
    }
}
