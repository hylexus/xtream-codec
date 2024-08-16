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

package io.github.hylexus.xtream.debug.codec.server.reactive.udp.handlermapping;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.handler.XtreamHandlerMapping;
import io.github.hylexus.xtream.debug.codec.server.reactive.udp.handler.DemoUdpXtreamHandler;
import reactor.core.publisher.Mono;

public class DemoUdpXtreamHandlerMapping implements XtreamHandlerMapping {

    @Override
    public Mono<Object> getHandler(XtreamExchange exchange) {
        return Mono.just(new DemoUdpXtreamHandler());
    }

}