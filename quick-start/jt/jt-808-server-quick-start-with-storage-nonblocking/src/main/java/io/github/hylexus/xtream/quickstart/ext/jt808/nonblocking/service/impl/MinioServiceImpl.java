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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.impl;

import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.configuration.props.QuickStartAppProps;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.configuration.props.MinioProps;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.MinioService;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.utils.ContentTypeDetector;
import io.minio.*;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
    public Mono<Boolean> bucketExists(String bucketName) {
        return Mono.fromFuture(() -> {
            try {
                final BucketExistsArgs args = BucketExistsArgs.builder().bucket(bucketName).build();
                return this.minioAsyncClient.bucketExists(args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Mono<Boolean> setBucketPolicy(String bucketName, String config) {
        return Mono.fromFuture(() -> {
            try {
                final SetBucketPolicyArgs args = SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(config)
                        .build();
                return this.minioAsyncClient.setBucketPolicy(args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).then(Mono.just(true));
    }

    @Override
    public Mono<Boolean> makeBucket(String bucketName) {
        return Mono.fromFuture(() -> {
            try {
                final MakeBucketArgs args = MakeBucketArgs.builder().bucket(bucketName).build();
                return this.minioAsyncClient.makeBucket(args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).then(Mono.just(true));
    }

    @Override
    public Mono<Void> uploadFile(String bucketName, String localFilePath, @Nullable String objectName, @Nullable String contentType) {
        return Mono.fromCallable(
                        () -> {
                            final String objectNameToUse;
                            final Path path = Paths.get(localFilePath);
                            if (StringUtils.isEmpty(objectName)) {
                                objectNameToUse = path.getFileName().toString();
                            } else {
                                objectNameToUse = objectName;
                            }
                            // 阻塞操作：获取文件路径和大小
                            final long fileSize = Files.size(path);
                            final PutObjectArgs args = PutObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(objectNameToUse)
                                    .contentType(StringUtils.isEmpty(contentType) ? ContentTypeDetector.detectContentType(localFilePath) : contentType)
                                    .stream(Files.newInputStream(path), fileSize, 10485760)
                                    .build();

                            // CompletableFuture
                            return this.minioAsyncClient.putObject(args);
                        })
                // 在 boundedElastic 调度器上执行
                .subscribeOn(Schedulers.boundedElastic())
                // 将 CompletableFuture 转换为 Mono
                .flatMap(Mono::fromFuture)
                // 完成后返回空值
                .then();
    }

}
