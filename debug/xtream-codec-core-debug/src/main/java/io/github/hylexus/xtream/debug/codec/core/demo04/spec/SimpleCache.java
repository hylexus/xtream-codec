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

package io.github.hylexus.xtream.debug.codec.core.demo04.spec;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

public class SimpleCache {
    private static final Logger log = LoggerFactory.getLogger(SimpleCache.class);
    protected final Cache<String, Map<Integer, BaseJt808Msg>> cache;
    public static final String LOG_PREFIX = "<SubPackage>";

    public SimpleCache() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(1024)
                .expireAfterWrite(Duration.ofSeconds(45))
                .removalListener((RemovalListener<String, Map<Integer, BaseJt808Msg>>) (key, value, cause) -> {
                    if (value == null) {
                        return;
                    }
                    final boolean isExpired = cause == RemovalCause.EXPIRED
                            || cause == RemovalCause.SIZE
                            || cause == RemovalCause.COLLECTED;
                    if (isExpired) {
                        value.forEach((currentPackageNo, subPackage) -> {
                            XtreamBytes.releaseBuf(subPackage.getInternalTemporarySubPackageBodyByteBuf());
                            log.info("{} {} has been released. reason = {}", LOG_PREFIX, subPackage, cause);
                        });
                    }

                }).build();
    }

    public Map<Integer, BaseJt808Msg> get(String key, Function<String, Map<Integer, BaseJt808Msg>> loader) {
        return this.cache.get(key, loader);
    }

    public void invalidate(String key) {
        this.cache.invalidate(key);
    }
}
