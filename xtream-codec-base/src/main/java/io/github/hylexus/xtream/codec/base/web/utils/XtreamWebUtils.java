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

package io.github.hylexus.xtream.codec.base.web.utils;


import io.micrometer.common.util.StringUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * @author hylexus
 * @see <a href="https://stackoverflow.com/questions/22877350/how-to-extract-ip-address-in-spring-mvc-controller-get-call">https://stackoverflow.com/questions/22877350/how-to-extract-ip-address-in-spring-mvc-controller-get-call</a>
 */
public final class XtreamWebUtils {
    private XtreamWebUtils() {
    }

    public interface HttpRequestHeaderProvider {
        String get(String name);
    }

    /**
     * @see <a href="https://stackoverflow.com/questions/22877350/how-to-extract-ip-address-in-spring-mvc-controller-get-call">https://stackoverflow.com/questions/22877350/how-to-extract-ip-address-in-spring-mvc-controller-get-call</a>
     */
    private static final String[] IP_HEADER_NAMES = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public static Optional<String> getClientIp(HttpRequestHeaderProvider headerProvider) {
        for (String headerName : IP_HEADER_NAMES) {
            String ip = headerProvider.get(headerName);
            if (StringUtils.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
                int index = ip.indexOf(',');
                if (index != -1) {
                    ip = ip.substring(0, index);
                }
                return Optional.of(ip);
            }
        }
        return Optional.empty();
    }

    public static Optional<String> getClientIp(HttpRequestHeaderProvider headerProvider, InetSocketAddress remoteAddress) {
        return getClientIp(headerProvider)
                .or(() -> Optional.ofNullable(remoteAddress)
                        .map(InetSocketAddress::getAddress)
                        .map(InetAddress::getHostAddress)
                );
    }
}
