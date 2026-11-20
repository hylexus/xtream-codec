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

package io.github.hylexus.xtream.loadtest.jt808;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("loadtest")
public record LoadtestProperties(
        @DefaultValue("jt808-loadtest") String authCode,
        @DefaultValue("2048") int tcpBacklog,
        @DefaultValue("30s") Duration summaryInterval) {

    public LoadtestProperties {
        if (authCode == null || authCode.isEmpty() || authCode.length() > 255
                || authCode.chars().anyMatch(value -> value < 0x21 || value > 0x7e)) {
            throw new IllegalArgumentException("测试鉴权码须为 1 到 255 字节的可见 ASCII 字符");
        }
        if (tcpBacklog <= 0) {
            throw new IllegalArgumentException("TCP backlog 须大于零");
        }
        if (summaryInterval == null || summaryInterval.toMillis() <= 0) {
            throw new IllegalArgumentException("汇总周期须至少为 1ms");
        }
    }
}
