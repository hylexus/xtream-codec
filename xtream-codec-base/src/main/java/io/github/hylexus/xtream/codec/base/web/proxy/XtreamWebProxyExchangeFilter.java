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

package io.github.hylexus.xtream.codec.base.web.proxy;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * 当前类是从 `de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunction` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunction`.
 *
 * @see <a href="https://github.com/codecentric/spring-boot-admin">https://github.com/codecentric/spring-boot-admin</a>
 * @see <a href="https://github.com/codecentric/spring-boot-admin/blob/1433ca5b8343247075ff775d558c9a82341e2ac6/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/web/client/InstanceExchangeFilterFunction.java#L33">de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunction</a>
 */
@FunctionalInterface
public interface XtreamWebProxyExchangeFilter {

    Mono<ClientResponse> filter(XtreamWebProxyBackend backend, ClientRequest request, ExchangeFunction next);

}
