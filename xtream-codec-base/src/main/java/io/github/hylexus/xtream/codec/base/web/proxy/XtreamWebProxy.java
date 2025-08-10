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

import io.github.hylexus.xtream.codec.base.web.exception.XtreamHttpCallException;
import io.github.hylexus.xtream.codec.base.web.exception.XtreamHttpErrorDetails;
import io.netty.handler.timeout.ReadTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.springframework.http.HttpMethod.*;

/**
 * 当前类是从 `de.codecentric.boot.admin.server.web.InstanceWebProxy` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `de.codecentric.boot.admin.server.web.InstanceWebProxy`.
 *
 * @see <a href="https://github.com/codecentric/spring-boot-admin">https://github.com/codecentric/spring-boot-admin</a>
 * @see <a href="https://github.com/codecentric/spring-boot-admin/blob/1433ca5b8343247075ff775d558c9a82341e2ac6/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/InstanceWebProxy.java#L58">de.codecentric.boot.admin.server.web.InstanceWebProxy</a>
 * @see <a href="https://github.com/codecentric/spring-boot-admin/blob/1433ca5b8343247075ff775d558c9a82341e2ac6/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/client/InstanceWebClient.java#L31">de.codecentric.boot.admin.server.web.client.InstanceWebClient</a>
 */
public class XtreamWebProxy {

    private static final List<HttpMethod> METHODS_HAS_BODY = List.of(PUT, POST, PATCH);
    private static final Logger log = LoggerFactory.getLogger(XtreamWebProxy.class);
    private final DataBufferFactory bufferFactory;
    private final WebClient webClient;
    private final XtreamWebProxyHttpHeaderFilter xtreamWebProxyHttpHeaderFilter;
    private final List<XtreamWebProxyExchangeFilter> filterFunctions;
    private final ExchangeStrategies strategies = ExchangeStrategies.withDefaults();

    public static Builder newBuilder() {
        return new Builder();
    }

    private XtreamWebProxy(DataBufferFactory bufferFactory, WebClient webClient, XtreamWebProxyHttpHeaderFilter xtreamWebProxyHttpHeaderFilter, List<XtreamWebProxyExchangeFilter> filterFunctions) {
        this.webClient = webClient;
        this.xtreamWebProxyHttpHeaderFilter = xtreamWebProxyHttpHeaderFilter;
        this.filterFunctions = filterFunctions;
        this.bufferFactory = bufferFactory;
    }

    /**
     * @see <a href="https://github.com/codecentric/spring-boot-admin/blob/1433ca5b8343247075ff775d558c9a82341e2ac6/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/InstanceWebProxy.java#L98">de.codecentric.boot.admin.server.web.InstanceWebProxy#forward()</a>
     */
    public <V> Mono<V> proxy(XtreamWebProxyBackend backend, ForwardRequest request, Function<ClientResponse, ? extends Mono<V>> responseHandler) {
        final HttpHeaders httpHeaders = this.xtreamWebProxyHttpHeaderFilter.filterHeaders(request.headers);
        final WebClient.RequestBodySpec bodySpec = this.webClient.mutate()
                .filters(filters -> filters.addFirst(new XtreamWebProxyBuiltinFilters.XtreamBackendServerSetterSync(backend)))
                .filters(filters -> {
                    for (final XtreamWebProxyExchangeFilter filterFunction : this.filterFunctions) {
                        filters.add(new XtreamWebProxyBuiltinFilters.XtreamBackendServerExchangeFilterFunctionWrapper(filterFunction));
                    }
                })
                .build()
                .method(request.method)
                .uri(request.uri)
                .headers(headers -> headers.addAll(httpHeaders));

        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec;
        if (hashRequestBody(request.method)) {
            headersSpec = bodySpec.body(request.body);
        }

        return headersSpec.exchangeToMono(responseHandler).onErrorResume(throwable -> {
            Throwable cause = throwable;
            if (throwable instanceof WebClientRequestException requestException) {
                cause = requestException.getCause();
            }
            if (cause instanceof ReadTimeoutException || cause instanceof TimeoutException) {
                log.error("Timeout for Proxy-Request for BACKEND {} with URL '{}'", backend, request.uri);
                return responseHandler.apply(ClientResponse.create(HttpStatus.GATEWAY_TIMEOUT, this.strategies).build());
            }
            if (cause instanceof IOException) {
                log.error("Proxy-Request for BACKEND {} with URL '{}' errored", backend, request.uri, cause);
                final XtreamHttpErrorDetails.ProxyErrorDetails errorDetails = XtreamHttpErrorDetails.ProxyErrorDetails.of(request.uri.toString(), backend.getBaseUrl(), cause.getMessage());
                final XtreamHttpCallException error = new XtreamHttpCallException(cause, HttpStatus.BAD_GATEWAY, "001:502", "Failed to proxy request to downstream service " + backend.getBaseUrl(), List.of(errorDetails));
                return Mono.error(error);
            }
            return Mono.error(throwable);
        });
    }

    public DataBufferFactory bufferFactory() {
        return bufferFactory;
    }

    public XtreamWebProxyHttpHeaderFilter httpHeaderFilter() {
        return xtreamWebProxyHttpHeaderFilter;
    }

    public static final class ForwardRequest {

        private final URI uri;

        private final HttpMethod method;

        private final HttpHeaders headers;

        private final BodyInserter<?, ? super ClientHttpRequest> body;

        public ForwardRequest(URI uri, HttpMethod method, HttpHeaders headers, BodyInserter<?, ? super ClientHttpRequest> body) {
            this.uri = uri;
            this.method = method;
            this.headers = headers;
            this.body = body;
        }
    }

    private boolean hashRequestBody(HttpMethod method) {
        return METHODS_HAS_BODY.contains(method);
    }

    public static final class Builder {
        private DataBufferFactory bufferFactory;
        private WebClient webClient;
        private XtreamWebProxyHttpHeaderFilter xtreamWebProxyHttpHeaderFilter;
        private final List<XtreamWebProxyExchangeFilter> filterFunctions = new ArrayList<>();

        public Builder bufferFactory(DataBufferFactory bufferFactory) {
            this.bufferFactory = bufferFactory;
            return this;
        }

        public Builder webClient(WebClient webClient) {
            this.webClient = webClient;
            return this;
        }

        public Builder httpHeaderFilter(XtreamWebProxyHttpHeaderFilter xtreamWebProxyHttpHeaderFilter) {
            this.xtreamWebProxyHttpHeaderFilter = xtreamWebProxyHttpHeaderFilter;
            return this;
        }

        public Builder filterFunctions(Consumer<List<XtreamWebProxyExchangeFilter>> builder) {
            builder.accept(this.filterFunctions);
            return this;
        }

        public Builder filterFunction(XtreamWebProxyExchangeFilter filterFunction) {
            this.filterFunctions.add(filterFunction);
            return this;
        }

        public XtreamWebProxy build() {
            return new XtreamWebProxy(
                    this.bufferFactory == null ? new DefaultDataBufferFactory() : this.bufferFactory,
                    Objects.requireNonNull(this.webClient, "webClient is null"),
                    this.xtreamWebProxyHttpHeaderFilter == null ? new XtreamWebProxyHttpHeaderFilter(Collections.emptySet()) : this.xtreamWebProxyHttpHeaderFilter,
                    filterFunctions
            );
        }

    }

}
