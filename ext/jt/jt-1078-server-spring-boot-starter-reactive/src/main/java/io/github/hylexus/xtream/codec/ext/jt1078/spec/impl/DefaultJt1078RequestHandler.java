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

package io.github.hylexus.xtream.codec.ext.jt1078.spec.impl;

import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078RequestPublisher;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078DataType;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078Request;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class DefaultJt1078RequestHandler implements Jt1078RequestHandler {
    private static final Logger log = LoggerFactory.getLogger(DefaultJt1078RequestHandler.class);
    private final Jt1078RequestPublisher requestPublisher;

    public DefaultJt1078RequestHandler(Jt1078RequestPublisher requestPublisher) {
        this.requestPublisher = requestPublisher;
    }

    @Override
    public Mono<Void> handleRequest(Jt1078Request request) {
        final Jt1078DataType dataType = request.dataType();
        return switch (dataType) {
            case VIDEO_I, VIDEO_P, VIDEO_B, AUDIO -> requestPublisher.publish(request);
            case null, default -> {
                log.error("Invalid data type: {}", dataType);
                yield Mono.empty();
            }
        };
    }

}
