/*
 * Copyright 2024 the original author or authors.
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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.actuate.mapping;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestMappingHandlerMapping;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageDescriptionRegistry;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamHandlerMethod;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 当前类是从 `org.springframework.boot.actuate.web.mappings.reactive.DispatcherHandlersMappingDescriptionProvider` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `org.springframework.boot.actuate.web.mappings.reactive.DispatcherHandlersMappingDescriptionProvider`.
 *
 * @author hylexus
 */
public class DispatcherHandlerXtreamMappingDescriptionProvider implements XtreamMappingDescriptionProvider {
    private final List<XtreamHandlerMapping> handlerMappingList;
    private final Jt808MessageDescriptionRegistry jt808MessageDescriptionRegistry;

    public DispatcherHandlerXtreamMappingDescriptionProvider(List<XtreamHandlerMapping> handlerMappingList, Jt808MessageDescriptionRegistry jt808MessageDescriptionRegistry) {
        this.handlerMappingList = handlerMappingList;
        this.jt808MessageDescriptionRegistry = jt808MessageDescriptionRegistry;
    }

    @Override
    public String name() {
        return "dispatcherXtreamHandler";
    }

    @Override
    public List<Jt808RequestHandlerMappingDescription> description() {
        return this.handlerMappingList.stream().flatMap(handlerMapping -> {
            if (handlerMapping instanceof Jt808RequestMappingHandlerMapping jt808RequestMappingHandlerMapping) {
                return this.describe(jt808RequestMappingHandlerMapping)
                        .sorted(Comparator.comparing(Jt808RequestHandlerMappingDescription::messageId).thenComparing(Jt808RequestHandlerMappingDescription::version));
            } else {
                return null;
            }
        }).filter(Objects::nonNull).toList();
    }

    private Stream<Jt808RequestHandlerMappingDescription> describe(Jt808RequestMappingHandlerMapping jt808RequestMappingHandlerMapping) {
        return jt808RequestMappingHandlerMapping.getMappings().entrySet().stream().flatMap(entry -> {
            final Integer messageId = entry.getKey();
            final Map<Jt808ProtocolVersion, XtreamHandlerMethod> value = entry.getValue();
            return this.describe(value, messageId);
        });
    }

    private Stream<Jt808RequestHandlerMappingDescription> describe(Map<Jt808ProtocolVersion, XtreamHandlerMethod> value, Integer messageId) {
        return value.entrySet().stream().map(entry -> {
            final Jt808ProtocolVersion version = entry.getKey();
            final XtreamHandlerMethod handlerMethod = entry.getValue();
            return new Jt808RequestHandlerMappingDescription(
                    handlerMethod,
                    messageId,
                    jt808MessageDescriptionRegistry.getDescription(messageId, "Unknown"),
                    version
            );
        });
    }

    public record Jt808RequestHandlerMappingDescription(
            String handler,
            boolean nonBlocking,
            String handlerDesc,
            int messageId,
            String messageIdAsHexString,
            String messageIdDesc,
            Jt808ProtocolVersion version,
            String scheduler,
            boolean rejectBlockingTask,
            boolean virtualThread
    ) {

        public Jt808RequestHandlerMappingDescription(XtreamHandlerMethod handlerMethod, int messageId, String messageIdDesc, Jt808ProtocolVersion version) {
            this(
                    handlerMethod.getContainerClass().getName() + "#" + handlerMethod.getMethod().getName(),
                    handlerMethod.isNonBlocking(),
                    handlerMethod.getDesc(),
                    messageId,
                    "0x" + FormatUtils.toHexString(messageId, 4),
                    messageIdDesc,
                    version,
                    handlerMethod.getSchedulerName(),
                    handlerMethod.isRejectBlockingTask(),
                    handlerMethod.isVirtualThread()
            );
        }
    }
}
