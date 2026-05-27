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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class DemoNetUtils {

    public static List<String> getLocalIpAddressesOrEmpty() {
        try {
            return getLocalIpAddresses();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    public static List<String> getLocalIpAddresses() throws Exception {
        final Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();

        final List<String> validIpList = new ArrayList<>();
        while (allNetInterfaces.hasMoreElements()) {
            final NetworkInterface netInterface = allNetInterfaces.nextElement();
            if (netInterface.isLoopback() || !netInterface.isUp() || netInterface.isVirtual()) {
                continue;
            }

            final Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                final InetAddress ip = addresses.nextElement();
                // 过滤掉IPv6地址，只保留有效的IPv4地址
                if (ip instanceof java.net.Inet4Address) {
                    validIpList.add(ip.getHostAddress());
                }
            }
        }

        return validIpList;
    }

}
