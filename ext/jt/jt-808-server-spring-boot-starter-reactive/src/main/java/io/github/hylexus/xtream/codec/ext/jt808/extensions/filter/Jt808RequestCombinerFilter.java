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

package io.github.hylexus.xtream.codec.ext.jt808.extensions.filter;

import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestCombiner;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestLifecycleListener;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamFilterChain;
import reactor.core.publisher.Mono;

/**
 * @author hylexus
 */
public class Jt808RequestCombinerFilter implements Jt808RequestFilter {
    public static final int ORDER = -100;
    protected final Jt808RequestCombiner requestCombiner;
    protected final Jt808RequestLifecycleListener lifecycleListener;

    public Jt808RequestCombinerFilter(Jt808RequestCombiner requestCombiner, Jt808RequestLifecycleListener lifecycleListener) {
        this.requestCombiner = requestCombiner;
        this.lifecycleListener = lifecycleListener;
    }

    @Override
    public Mono<Void> filter(XtreamExchange exchange, XtreamFilterChain chain) {
        final Jt808Request jt808Request = (Jt808Request) exchange.request();

        // 不是子包
        if (!jt808Request.header().messageBodyProps().hasSubPackage()) {
            return chain.filter(exchange);
        }

        // 合并子包
        final Jt808Request mergedRequest = this.requestCombiner.tryMergeSubPackage(jt808Request);
        // 如果还有子包没到达，终止后续流程，直至所有子包都到达
        if (mergedRequest == null) {
            return Mono.empty();
        }

        // 所有子包都已经到达，继续执行后续流程
        final XtreamExchange mutatedExchange = this.mutatedExchange(exchange, mergedRequest);

        this.lifecycleListener.afterSubPackageMerged(mutatedExchange, mergedRequest);

        return chain.filter(mutatedExchange).doFinally(signalType -> {
            // ...
            mergedRequest.release();
        });
    }

    protected XtreamExchange mutatedExchange(XtreamExchange exchange, Jt808Request request) {
        return exchange.mutate().request(request).build();
    }

    @Override
    public int order() {
        return ORDER;
    }
}
