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

import org.junit.jupiter.api.Test;
import reactor.netty.tcp.TcpServer;
import io.netty.channel.ChannelOption;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class LoadtestPropertiesTest {
    @Test
    void invalidConfigurationShouldFailEarly() {
        assertThrows(IllegalArgumentException.class, () -> new LoadtestProperties("", 2048, Duration.ofSeconds(30)));
        assertThrows(IllegalArgumentException.class, () -> new LoadtestProperties("中文", 2048, Duration.ofSeconds(30)));
        assertThrows(IllegalArgumentException.class, () -> new LoadtestProperties("a".repeat(256), 2048, Duration.ofSeconds(30)));
        assertThrows(IllegalArgumentException.class, () -> new LoadtestProperties("valid", 0, Duration.ofSeconds(30)));
        assertThrows(IllegalArgumentException.class, () -> new LoadtestProperties("valid", 2048, Duration.ZERO));
    }

    @Test
    void customBacklogShouldReachNettyConfiguration() {
        final var properties = new LoadtestProperties("test-auth", 4096, Duration.ofSeconds(30));
        final var server = new LoadtestConfiguration().loadtestTcpCustomizer(properties, mock(LoadtestMetrics.class))
                .customize(TcpServer.create());
        assertEquals(4096, server.configuration().options().get(ChannelOption.SO_BACKLOG));
    }
}
