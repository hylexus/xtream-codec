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

package io.github.hylexus.xtream.codec.ext.jt1078.codec;


import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078Request;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078RequestHeader;
import jakarta.annotation.Nullable;
import reactor.netty.NettyInbound;

import java.util.UUID;
import java.util.function.Consumer;

public interface Jt1078RequestCombiner {

    /**
     * 尝试合并子包(完整包不应该调用这个方法)
     *
     * @param request 解码后的 JT1078 请求(子包)
     * @return 如果能合并(所有子包都已到达)，则返回合并后的新请求，否则返回 {@code null}
     */
    @Nullable
    Jt1078Request tryMergeSubPackage(Jt1078Request request);

    /**
     * 为当请求生成 {@link Jt1078Request#traceId()}，确保同一个消息的所有子包的 {@link Jt1078Request#traceId()} 是相同的。
     * <p>
     * 如果你不在意子包的 {@link Jt1078Request#traceId()} 是否相同，返回一个随机值即可。
     *
     * @param nettyInbound inbound; 可以获取连接信息
     * @param header       JT1078 消息头
     * @return traceId
     * @see NettyInbound#withConnection(Consumer)
     */
    default String getTraceId(NettyInbound nettyInbound, Jt1078RequestHeader header) {
        return randomTraceId();
    }

    static String randomTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
