/*
 * Copyright (c) 2024 xtream-codec
 * xtream-codec is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package io.github.hylexus.xtream.codec.server.reactive.spec;

import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 当前类是从 `org.springframework.web.server.ServerWebExchange` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `org.springframework.web.server.ServerWebExchange`.
 *
 * @author hylexus
 */
public interface XtreamExchange {

    default ByteBufAllocator bufferFactory() {
        return response().bufferFactory();
    }

    XtreamRequest request();

    XtreamResponse response();

    Mono<XtreamSession> session();

    XtreamExchangeBuilder mutate();

    default Map<String, Object> attributes() {
        return request().attributes();
    }

    interface XtreamExchangeBuilder {
        XtreamExchangeBuilder request(XtreamRequest request);

        XtreamExchangeBuilder request(Consumer<XtreamRequest.XtreamRequestBuilder> requestBuilder);

        XtreamExchangeBuilder response(XtreamResponse response);

        XtreamExchange build();
    }
}