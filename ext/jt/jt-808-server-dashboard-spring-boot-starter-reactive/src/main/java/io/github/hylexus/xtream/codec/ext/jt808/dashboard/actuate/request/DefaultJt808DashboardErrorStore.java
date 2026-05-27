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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.actuate.request;

import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.XtreamServerErrorInfo;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.HasErrorDetails;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultJt808DashboardErrorStore implements Jt808DashboardErrorStore {

    public DefaultJt808DashboardErrorStore() {
    }

    private final ConcurrentMap<String, XtreamServerErrorInfo> errorStore = new ConcurrentHashMap<>();

    @Override
    public void save(Throwable throwable) {
        final String key = throwable.getClass().getName();
        final Instant now = Instant.now();
        final XtreamServerErrorInfo errorInfo = this.errorStore.computeIfAbsent(key, (k) -> new XtreamServerErrorInfo());
        errorInfo.incrementCount();
        errorInfo.setLastErrorTime(now);
        errorInfo.setLastErrorMessage(throwable.getMessage());
        if (throwable instanceof HasErrorDetails hasErrorDetails) {
            errorInfo.setDetails(hasErrorDetails.getErrorDetails());
        }
    }

    @Override
    public Map<String, XtreamServerErrorInfo> asMapView() {
        return Collections.unmodifiableMap(this.errorStore);
    }

}
