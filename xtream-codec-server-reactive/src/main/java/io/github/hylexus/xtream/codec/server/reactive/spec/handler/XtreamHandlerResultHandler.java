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

package io.github.hylexus.xtream.codec.server.reactive.spec.handler;

import io.github.hylexus.xtream.codec.core.annotation.OrderedComponent;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import reactor.core.publisher.Mono;

/**
 * 当前类是从 `org.springframework.web.reactive.HandlerResultHandler` 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on `org.springframework.web.reactive.HandlerResultHandler`.
 *
 * @author hylexus
 */
public interface XtreamHandlerResultHandler extends OrderedComponent {

    boolean supports(XtreamHandlerResult result);

    Mono<Void> handleResult(XtreamExchange exchange, XtreamHandlerResult result);

}
