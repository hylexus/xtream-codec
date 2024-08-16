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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp;

import io.github.hylexus.xtream.codec.server.reactive.spec.UdpXtreamNettyHandlerAdapter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamHandler;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.AbstractXtreamHandlerAdapterBuilder;
import io.netty.buffer.ByteBufAllocator;

/**
 * @author hylexus
 */
public class UdpXtreamHandlerAdapterBuilder extends AbstractXtreamHandlerAdapterBuilder<UdpXtreamHandlerAdapterBuilder> {

    public UdpXtreamHandlerAdapterBuilder(ByteBufAllocator allocator) {
        super(allocator);
    }

    @Override
    public UdpXtreamNettyHandlerAdapter build() {
        final XtreamHandler exceptionHandlingHandler = createRequestHandler();
        return new DefaultUdpXtreamNettyHandlerAdapter(super.byteBufAllocator, super.xtreamExchangeCreator, exceptionHandlingHandler);
    }

}