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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl.oss;

import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.MinioService;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.ObjectStorageService;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectStorageServiceMinioImpl implements ObjectStorageService {
    private static final Logger log = LoggerFactory.getLogger(ObjectStorageServiceMinioImpl.class);
    private final MinioService minioService;

    public ObjectStorageServiceMinioImpl(MinioService minioService) {
        this.minioService = minioService;
    }

    @Override
    public boolean bucketExists(String bucketName) {
        log.info("minioImpl#bucketExists: {}", bucketName);
        return this.minioService.bucketExists(bucketName);
    }

    @Override
    public boolean makeBucket(String bucketName) {
        log.info("minioImpl#makeBucket: {}", bucketName);
        return this.minioService.makeBucket(bucketName);
    }

    @Override
    public boolean makeBucketAccessibleWithAnonymous(String bucketName) {
        log.info("minioImpl#makeBucketAccessibleWithAnonymous: {}", bucketName);
        return this.minioService.makeBucketAccessibleWithAnonymous(bucketName);
    }

    @Override
    public void uploadFile(String bucketName, String localFilePath, @Nullable String objectName, @Nullable String contentType) {
        log.info("minioImpl#uploadFile: {}, {}, {}, {}", bucketName, localFilePath, objectName, contentType);
        this.minioService.uploadFile(bucketName, localFilePath, objectName, contentType);
    }

}
