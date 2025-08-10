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
import io.github.hylexus.xtream.codec.base.web.exception.XtreamHttpTransparentException;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Function;

/**
 * 当前类是从 `de.codecentric.boot.admin.server.web.reactive.InstancesProxyController` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `de.codecentric.boot.admin.server.web.reactive.InstancesProxyController`.
 *
 * @see <a href="https://github.com/codecentric/spring-boot-admin">https://github.com/codecentric/spring-boot-admin</a>
 * @see <a href="https://github.com/codecentric/spring-boot-admin/blob/1433ca5b8343247075ff775d558c9a82341e2ac6/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/InstanceWebProxy.java#L58">de.codecentric.boot.admin.server.web.InstanceWebProxy</a>
 * @see <a href="https://github.com/codecentric/spring-boot-admin/blob/83db63e82e916357d36b1e6b4d552e1b6506ecc9/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/reactive/InstancesProxyController.java#L51">de.codecentric.boot.admin.server.web.reactive.InstancesProxyController</a>
 * @see <a href="https://github.com/codecentric/spring-boot-admin/blob/83db63e82e916357d36b1e6b4d552e1b6506ecc9/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/servlet/InstancesProxyController.java#L58">de.codecentric.boot.admin.server.web.servlet.InstancesProxyController</a>
 */
public final class XtreamWebProxyUtils {

    private XtreamWebProxyUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see <a href="https://github.com/codecentric/spring-boot-admin/blob/1433ca5b8343247075ff775d558c9a82341e2ac6/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/reactive/InstancesProxyController.java#L79">de.codecentric.boot.admin.server.web.reactive.InstancesProxyController#endpointProxy()</a>
     */
    public static Mono<Void> proxyReactiveRequest(XtreamWebProxy proxy, XtreamWebProxyBackend backend, ServerWebExchange exchange) {
        final ServerHttpResponse response = exchange.getResponse();
        return proxyReactiveRequest(proxy, backend, exchange.getRequest(), clientResponse -> {
            final HttpStatusCode httpStatusCode = clientResponse.statusCode();
            if (!httpStatusCode.is2xxSuccessful()) {
                return clientResponse.bodyToMono(Object.class)
                        .switchIfEmpty(Mono.defer(() -> {
                            final XtreamHttpErrorDetails.ProxyErrorDetails errorDetails = XtreamHttpErrorDetails.ProxyErrorDetails.of(exchange.getRequest().getURI().toString(), backend.getBaseUrl(), "Failed to proxy request to [" + backend.getBaseUrl() + "]");
                            final XtreamHttpCallException error = new XtreamHttpCallException(httpStatusCode, "001:" + httpStatusCode.value(), "Failed to proxy request to downstream service " + backend.getBaseUrl(), List.of(errorDetails));
                            return Mono.error(error);
                        })).flatMap(error -> {
                            final XtreamHttpTransparentException transparentException = new XtreamHttpTransparentException(httpStatusCode.value(), error);
                            return Mono.error(transparentException);
                        });
            }
            response.setStatusCode(httpStatusCode);
            response.getHeaders().addAll(proxy.httpHeaderFilter().filterHeaders(clientResponse.headers().asHttpHeaders()));
            return response.writeAndFlushWith(clientResponse.body(BodyExtractors.toDataBuffers()).window(1));
        });
    }

    public static <V> Mono<V> proxyReactiveRequest(XtreamWebProxy proxy, XtreamWebProxyBackend backend, ServerHttpRequest request, Function<ClientResponse, ? extends Mono<V>> responseHandler) {
        return proxy.proxy(
                backend,
                new XtreamWebProxy.ForwardRequest(
                        request.getURI(),
                        request.getMethod(),
                        request.getHeaders(),
                        BodyInserters.fromDataBuffers(request.getBody())
                ),
                responseHandler
        );
    }

    /**
     * @see <a href="https://github.com/codecentric/spring-boot-admin/blob/1433ca5b8343247075ff775d558c9a82341e2ac6/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/servlet/InstancesProxyController.java#L87">de.codecentric.boot.admin.server.web.servlet.InstancesProxyController#instanceProxy()</a>
     */
    public static void proxyServletRequest(XtreamWebProxy proxy, XtreamWebProxyBackend backend, HttpServletRequest request) {
        AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(-1); // no timeout because XtreamWebProxy will handle it for us

        try {
            proxyServletRequest(proxy, backend, request, clientResponse -> {
                try (ServletServerHttpResponse response = new ServletServerHttpResponse((HttpServletResponse) asyncContext.getResponse())) {
                    response.setStatusCode(clientResponse.statusCode());
                    response.getHeaders().addAll(proxy.httpHeaderFilter().filterHeaders(clientResponse.headers().asHttpHeaders()));
                    try {
                        final OutputStream responseBody = response.getBody();
                        response.flush();
                        return clientResponse.body(BodyExtractors.toDataBuffers())
                                .window(1)
                                .concatMap(body -> writeAndFlush(body, responseBody))
                                .then();
                    } catch (IOException ex) {
                        return Mono.error(ex);
                    }
                }
            }).block();
        } finally {
            asyncContext.complete();
        }
    }

    public static <V> Mono<V> proxyServletRequest(XtreamWebProxy proxy, XtreamWebProxyBackend backend, HttpServletRequest request, Function<ClientResponse, ? extends Mono<V>> responseHandler) {
        final ServletServerHttpRequest httpRequest = new ServletServerHttpRequest(request);
        final Flux<DataBuffer> requestBody = DataBufferUtils.readInputStream(httpRequest::getBody, proxy.bufferFactory(), 4096);
        return proxy.proxy(
                backend,
                new XtreamWebProxy.ForwardRequest(
                        httpRequest.getURI(),
                        httpRequest.getMethod(),
                        httpRequest.getHeaders(),
                        BodyInserters.fromDataBuffers(requestBody)
                ),
                responseHandler
        );
    }

    static Mono<Void> writeAndFlush(Flux<DataBuffer> body, OutputStream responseBody) {
        return DataBufferUtils.write(body, responseBody)
                .map(DataBufferUtils::release)
                .then(Mono.create((sink) -> {
                    try {
                        responseBody.flush();
                        sink.success();
                    } catch (IOException ex) {
                        sink.error(ex);
                    }
                }));
    }

}
