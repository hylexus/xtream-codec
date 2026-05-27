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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service;

import jakarta.annotation.Nullable;
import reactor.core.publisher.Mono;

/**
 * @author hylexus
 */
public interface MinioService {

    Mono<Boolean> bucketExists(String bucketName);

    Mono<Boolean> makeBucket(String bucketName);

    Mono<Void> uploadFile(String bucketName, String localFilePath, @Nullable String objectName, @Nullable String contentType);

    Mono<Boolean> setBucketPolicy(String bucketName, String config);

    default Mono<Boolean> makeBucketAccessibleWithAnonymous(String bucketName) {
        return this.setBucketPolicy(
                bucketName,
                """
                        {
                            "Version": "2012-10-17",
                            "Statement": [
                                {
                                    "Effect": "Allow",
                                    "Principal": {
                                        "AWS": [
                                            "*"
                                        ]
                                    },
                                    "Action": [
                                        "s3:GetObject"
                                    ],
                                    "Resource": [
                                        "arn:aws:s3:::%s/*"
                                    ]
                                }
                            ]
                        }""".formatted(bucketName)
        );
    }
}
