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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl;

import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.configuration.props.MinioProps;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.configuration.props.QuickStartAppProps;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.MinioService;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.utils.ContentTypeDetector;
import io.minio.*;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author hylexus
 */
public class MinioServiceImpl implements MinioService {
    private static final Logger log = LoggerFactory.getLogger(MinioServiceImpl.class);
    private final MinioAsyncClient minioAsyncClient;

    public MinioServiceImpl(QuickStartAppProps quickStartAppProps) {
        final MinioProps minioProps = quickStartAppProps.getMinio();
        this.minioAsyncClient = MinioAsyncClient.builder()
                .endpoint(minioProps.getEndpoint())
                .credentials(minioProps.getAccessKey(), minioProps.getSecretKey())
                .build();
    }

    @Override
    public boolean bucketExists(String bucketName) {
        final BucketExistsArgs args = BucketExistsArgs.builder().bucket(bucketName).build();
        try {
            return this.minioAsyncClient.bucketExists(args).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean setBucketPolicy(String bucketName, String config) {
        try {
            final SetBucketPolicyArgs args = SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(config)
                    .build();
            this.minioAsyncClient.setBucketPolicy(args).get();
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean makeBucket(String bucketName) {
        final MakeBucketArgs args = MakeBucketArgs.builder().bucket(bucketName).build();
        try {
            this.minioAsyncClient.makeBucket(args).get();
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void uploadFile(String bucketName, String localFilePath, @Nullable String objectName, @Nullable String contentType) {
        final String objectNameToUse;
        final Path path = Paths.get(localFilePath);
        if (StringUtils.isEmpty(objectName)) {
            objectNameToUse = path.getFileName().toString();
        } else {
            objectNameToUse = objectName;
        }
        try {
            final long fileSize = Files.size(path);
            final PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectNameToUse)
                    .contentType(StringUtils.isEmpty(contentType) ? ContentTypeDetector.detectContentType(localFilePath) : contentType)
                    .stream(Files.newInputStream(path), fileSize, 10485760)
                    .build();

            this.minioAsyncClient.putObject(args).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
