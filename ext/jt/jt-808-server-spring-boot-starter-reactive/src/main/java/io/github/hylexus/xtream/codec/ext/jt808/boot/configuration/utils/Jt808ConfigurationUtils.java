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

package io.github.hylexus.xtream.codec.ext.jt808.boot.configuration.utils;

import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.TcpXtreamFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.UdpXtreamFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.LoggingXtreamFilter;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.RequestDispatcherSchedulerFilter;

public final class Jt808ConfigurationUtils {

    private Jt808ConfigurationUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * JT/T 808(TCP) 服务启动时，会自动收集容器中的 {@link XtreamFilter} 实现类。
     */
    public static boolean jt808RequestFilterPredicateTcp(XtreamFilter xtreamFilter) {
        if (xtreamFilter instanceof UdpXtreamFilter) {
            return false;
        }
        return isJt808RequestFilter(xtreamFilter);
    }

    /**
     * JT/T 808(UDP) 服务启动时，会自动收集容器中的 {@link XtreamFilter} 实现类。
     */
    public static boolean jt808RequestFilterPredicateUdp(XtreamFilter xtreamFilter) {
        if (xtreamFilter instanceof TcpXtreamFilter) {
            return false;
        }
        return isJt808RequestFilter(xtreamFilter);
    }

    private static boolean isJt808RequestFilter(XtreamFilter xtreamFilter) {
        if (xtreamFilter instanceof Jt808RequestFilter) {
            return true;
        }
        if (xtreamFilter instanceof RequestDispatcherSchedulerFilter) {
            return true;
        }
        return xtreamFilter instanceof LoggingXtreamFilter;
    }

}
