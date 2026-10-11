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

package io.github.hylexus.xtream.codec.studio.app;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class CodecStudioConfigurationTest {

    @Test
    void defaultsAndEnvironmentShouldConfigureNetworkAndShutdown() {
        assertEquals(new CodecStudioConfiguration("127.0.0.1", 8765, 5),
                CodecStudioConfiguration.from(new String[0], new Properties(), Map.of()));
        assertEquals(new CodecStudioConfiguration("0.0.0.0", 8080, 12),
                CodecStudioConfiguration.from(new String[0], new Properties(), Map.of(
                        "XTREAM_STUDIO_HOST", "0.0.0.0", "XTREAM_STUDIO_PORT", "8080",
                        "XTREAM_STUDIO_SHUTDOWN_GRACE_SECONDS", "12")));
    }

    @Test
    void propertiesAndLegacyPortShouldOverrideEnvironment() {
        final Properties properties = new Properties();
        properties.setProperty("xtream.studio.host", "localhost");
        properties.setProperty("xtream.studio.port", "9000");
        properties.setProperty("xtream.studio.shutdown-grace-seconds", "0");
        final Map<String, String> environment = Map.of("XTREAM_STUDIO_HOST", "0.0.0.0",
                "XTREAM_STUDIO_PORT", "8080", "XTREAM_STUDIO_SHUTDOWN_GRACE_SECONDS", "12");
        assertEquals(new CodecStudioConfiguration("localhost", 9000, 0),
                CodecStudioConfiguration.from(new String[0], properties, environment));
        assertEquals(new CodecStudioConfiguration("localhost", 0, 0),
                CodecStudioConfiguration.from(new String[]{"0", "example.entities"}, properties, environment));
    }

    @Test
    void invalidNumericSettingsShouldFailBeforeStartup() {
        for (final String port : new String[]{"-1", "65536", "not-a-port"}) {
            final var exception = assertThrows(IllegalArgumentException.class,
                    () -> CodecStudioConfiguration.from(new String[0], new Properties(), Map.of("XTREAM_STUDIO_PORT", port)));
            assertTrue(exception.getMessage().contains("xtream.studio.port"));
        }
        for (final String grace : new String[]{"-1", "forever"}) {
            final var exception = assertThrows(IllegalArgumentException.class, () -> CodecStudioConfiguration.from(
                    new String[0], new Properties(), Map.of("XTREAM_STUDIO_SHUTDOWN_GRACE_SECONDS", grace)));
            assertTrue(exception.getMessage().contains("xtream.studio.shutdown-grace-seconds"));
        }
    }
}
