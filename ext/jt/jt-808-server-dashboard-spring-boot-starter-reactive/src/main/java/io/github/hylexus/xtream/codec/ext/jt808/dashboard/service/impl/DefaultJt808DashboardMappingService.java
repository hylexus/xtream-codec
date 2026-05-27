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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.impl;

import io.github.hylexus.xtream.codec.ext.jt808.dashboard.actuate.mapping.XtreamMappingDescriptionProvider;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardMappingService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultJt808DashboardMappingService implements Jt808DashboardMappingService {
    private final List<XtreamMappingDescriptionProvider> mappingDescriptionProviderList;

    public DefaultJt808DashboardMappingService(List<XtreamMappingDescriptionProvider> mappingDescriptionProviderList) {
        this.mappingDescriptionProviderList = mappingDescriptionProviderList;
    }

    @Override
    public Map<String, Object> getJt808HandlerMappings() {
        final Map<String, Object> map = new HashMap<>();
        for (final XtreamMappingDescriptionProvider provider : this.mappingDescriptionProviderList) {
            final String name = provider.name();
            final Object description = provider.description();
            map.put(name, description);
        }
        return map;
    }
}
