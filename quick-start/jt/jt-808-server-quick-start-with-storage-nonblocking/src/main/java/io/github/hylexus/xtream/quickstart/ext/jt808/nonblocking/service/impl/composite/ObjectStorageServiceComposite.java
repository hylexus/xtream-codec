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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.impl.composite;

import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.ObjectStorageService;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

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
    public Mono<Boolean> bucketExists(String bucketName) {
        return this.doDelegate(service -> service.bucketExists(bucketName), () -> Mono.just(true), () -> Mono.just(true));
    }

    @Override
    public Mono<Boolean> makeBucket(String bucketName) {
        return this.doDelegate(service -> service.makeBucket(bucketName), () -> Mono.just(true), () -> Mono.just(true));
    }

    @Override
    public Mono<Boolean> makeBucketAccessibleWithAnonymous(String bucketName) {
        return this.doDelegate(service -> service.makeBucketAccessibleWithAnonymous(bucketName), () -> Mono.just(true), () -> Mono.just(true));
    }

    @Override
    public Mono<Void> uploadFile(String bucketName, String localFilePath, @Nullable String objectName, @Nullable String contentType) {
        return this.doDelegate(service -> service.uploadFile(bucketName, localFilePath, objectName, contentType), Mono::empty, Mono::empty);
    }

    private <T> Mono<T> doDelegate(Function<ObjectStorageService, Mono<T>> function, Supplier<Mono<T>> onError, Supplier<Mono<T>> last) {
        return Flux.fromIterable(this.delegates)
                .flatMap(function)
                .onErrorResume(Throwable.class, throwable -> {
                    log.error("error occurred while do delegate", throwable);
                    return onError.get();
                })
                .then(Mono.defer(last));
    }
}
