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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hylexus.xtream.codec.common.XtreamVersion;
import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.SimpleTypes;
import io.netty.buffer.ByteBuf;
import io.netty.util.Version;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;

import java.time.Instant;
import java.util.List;

/**
 * @author hylexus
 */
public record ServerInfoVo(
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8") Instant serverStartupTime,
        SimpleTypes.JavaInfo java,
        SimpleTypes.OsInfo os,
        XtreamJt808ServerProperties jt808ServerConfig,
        Dependencies dependencies) {

    public ServerInfoVo(Instant serverStartupTime, XtreamJt808ServerProperties configuration) {
        this(serverStartupTime, SimpleTypes.JavaInfo.INSTANCE, SimpleTypes.OsInfo.INSTANCE, configuration, Dependencies.INSTANCE);
    }

    public record Dependencies(
            SimpleTypes.DependencyInfo spring,
            SimpleTypes.DependencyInfo springBoot,
            SimpleTypes.DependencyInfo xtreamCodec,
            NettyDependencyInfo netty) {


        public static final Dependencies INSTANCE = new Dependencies(
                new SimpleTypes.DependencyInfo("spring", SpringVersion.getVersion() == null ? "Unknown" : SpringVersion.getVersion()),
                new SimpleTypes.DependencyInfo("spring-boot", SpringBootVersion.getVersion()),
                new SimpleTypes.DependencyInfo("xtream-codec", XtreamVersion.getVersion("Unknown"))
        );

        public Dependencies(SimpleTypes.DependencyInfo spring, SimpleTypes.DependencyInfo springBoot, SimpleTypes.DependencyInfo xtreamCodec) {
            this(spring, springBoot, xtreamCodec, NettyDependencyInfo.INSTANCE);
        }

        public record NettyDependencyInfo(
                SimpleTypes.DependencyInfo nettyBuffer,
                List<SimpleTypes.DependencyInfo> components) {
            private static final List<SimpleTypes.DependencyInfo> NETTY_COMPONENT_VERSIONS = detectNettyVersions();
            public static final NettyDependencyInfo INSTANCE = new NettyDependencyInfo(
                    new SimpleTypes.DependencyInfo("netty-buffer", ByteBuf.class.getPackage().getImplementationVersion()),
                    NETTY_COMPONENT_VERSIONS
            );
        }

        static List<SimpleTypes.DependencyInfo> detectNettyVersions() {
            return Version.identify().entrySet().stream()
                    .map(it -> new SimpleTypes.DependencyInfo(it.getKey(), it.getValue().toString()))
                    .toList();
        }
    }
}
