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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service;

import jakarta.annotation.Nullable;

public interface ObjectStorageService {

    boolean bucketExists(String bucketName);

    boolean makeBucket(String bucketName);

    boolean makeBucketAccessibleWithAnonymous(String bucketName);

    void uploadFile(String bucketName, String localFilePath, @Nullable String objectName, @Nullable String contentType);

}
