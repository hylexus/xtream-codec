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

package io.github.hylexus.xtream.codec.base.web.proxy;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * @see <a href="https://github.com/codecentric/spring-boot-admin">https://github.com/codecentric/spring-boot-admin</a>
 * @see <a href="https://github.com/codecentric/spring-boot-admin/blob/1433ca5b8343247075ff775d558c9a82341e2ac6/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/InstanceWebProxy.java#L58">de.codecentric.boot.admin.server.web.InstanceWebProxy</a>
 * @see <a href="https://github.com/codecentric/spring-boot-admin/blob/1433ca5b8343247075ff775d558c9a82341e2ac6/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/client/InstanceWebClient.java#L31">de.codecentric.boot.admin.server.web.client.InstanceWebClient</a>
 */
@NullMarked
public final class XtreamWebProxyBuiltinFilters {
    private static final String ATTR_KEY_BACKEND_SERVER = "__BACKEND_SERVER_INSTANCE__";
    private static final Logger log = LoggerFactory.getLogger(XtreamWebProxyBuiltinFilters.class);

    private XtreamWebProxyBuiltinFilters() {
        throw new UnsupportedOperationException();
    }


    /**
     * @see <a href="https://github.com/codecentric/spring-boot-admin/blob/1433ca5b8343247075ff775d558c9a82341e2ac6/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/client/InstanceWebClient.java#L57">de.codecentric.boot.admin.server.web.client.InstanceWebClient.setInstance</a>
     */
    public static class XtreamBackendServerSetterAsync implements ExchangeFilterFunction {
        private final Mono<XtreamWebProxyBackend> backendMono;

        public XtreamBackendServerSetterAsync(Mono<XtreamWebProxyBackend> backendMono) {
            this.backendMono = backendMono;
        }

        @Override
        public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
            return this.backendMono
                    .switchIfEmpty(Mono.error(() -> new IllegalStateException("Can not found BackendServer")))
                    .flatMap((backend) -> {
                        final ClientRequest newRequest = XtreamBackendServerSetterSync.setBackend(request, backend);
                        return next.exchange(newRequest);
                    });
        }
    }

    /**
     * @see <a href="https://github.com/codecentric/spring-boot-admin/blob/1433ca5b8343247075ff775d558c9a82341e2ac6/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/client/InstanceWebClient.java#L57">de.codecentric.boot.admin.server.web.client.InstanceWebClient.setInstance</a>
     */
    public static class XtreamBackendServerSetterSync implements ExchangeFilterFunction {
        private final XtreamWebProxyBackend backend;

        public XtreamBackendServerSetterSync(XtreamWebProxyBackend backend) {
            this.backend = backend;
        }

        @Override
        public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
            final ClientRequest newRequest = setBackend(request, this.backend);
            return next.exchange(newRequest);
        }

        static ClientRequest setBackend(ClientRequest request, XtreamWebProxyBackend webBackend) {
            final URI newUrl = UriComponentsBuilder.fromHttpUrl(webBackend.getBaseUrl())
                    .path(request.url().getPath())
                    .query(request.url().getQuery())
                    .build(true)
                    .toUri();

            return ClientRequest.from(request)
                    .attribute(ATTR_KEY_BACKEND_SERVER, webBackend)
                    .url(newUrl)
                    .build();
        }
    }

    /**
     * @see <a href="https://github.com/codecentric/spring-boot-admin/blob/1433ca5b8343247075ff775d558c9a82341e2ac6/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/client/InstanceWebClient.java#L64">de.codecentric.boot.admin.server.web.client.InstanceWebClient.toExchangeFilterFunction</a>
     */
    public static class XtreamBackendServerExchangeFilterFunctionWrapper implements ExchangeFilterFunction {

        private final XtreamWebProxyExchangeFilter delegateFilter;

        public XtreamBackendServerExchangeFilterFunctionWrapper(XtreamWebProxyExchangeFilter delegateFilter) {
            this.delegateFilter = delegateFilter;
        }

        @Override
        public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
            return request.attribute(ATTR_KEY_BACKEND_SERVER)
                    .map(XtreamWebProxyBackend.class::cast)
                    .map((instance) -> delegateFilter.filter(instance, request, next))
                    .orElseGet(() -> next.exchange(request));
        }

    }

}
