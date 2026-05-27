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

package io.github.hylexus.xtream.codec.server.reactive.spec;


import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * @author hylexus
 */
public interface XtreamSessionIdGenerator {

    default String generateSessionId(XtreamExchange exchange) {
        final XtreamRequest request = exchange.request();
        return switch (request.type()) {
            case UDP -> this.generateUdpSessionId(request.remoteAddress());
            case TCP -> this.generateTcpSessionId(request.underlyingChannel());
        };
    }

    default String generateUdpSessionId(InetSocketAddress remoteAddress) {
        return remoteAddress.toString();
    }

    String generateTcpSessionId(Channel channel);

    class DefalutXtreamSessionIdGenerator implements XtreamSessionIdGenerator {
        private static final char[] DIGIT_MAP = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'};

        @Override
        public String generateTcpSessionId(Channel channel) {
            return channel.id().asLongText();
        }

        @Override
        public String generateUdpSessionId(InetSocketAddress remoteAddress) {
            final StringBuilder result = new StringBuilder();
            final String address = remoteAddress.getHostString();
            final int port = remoteAddress.getPort();

            // IP地址部分
            for (int i = 0; i < address.length(); i++) {
                final char c = address.charAt(i);
                if (c == '.') {
                    // 小数点映射为 'X'
                    result.append('X');
                } else if (Character.isDigit(c)) {
                    final int digit = Character.digit(c, 10);
                    // 数字映射为字母
                    result.append(DIGIT_MAP[digit]);
                } else {
                    // 非数字和小数点的字符，直接追加
                    result.append(c);
                }
            }

            // 分隔符
            result.append("S");

            // 端口号部分
            for (char c : String.valueOf(port).toCharArray()) {
                if (Character.isDigit(c)) {
                    final int digit = Character.digit(c, 10);
                    // 数字映射为字母
                    result.append(DIGIT_MAP[digit]);
                }
            }

            return result.toString();
        }
    }

}
