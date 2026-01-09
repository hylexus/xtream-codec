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

package io.github.hylexus.xtream.codec.ext.jt1078.boot.properties;

import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.TcpSessionIdleStateCheckerProps;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.UdpSessionIdleStateCheckerProps;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import reactor.netty.resources.LoopResources;

import java.time.Duration;

@Getter
@Setter
@ToString
@SuppressWarnings("NullAway")
@ConfigurationProperties(prefix = "jt1078-server")
public class XtreamJt1078ServerProperties {

    private boolean enabled = true;

    // @NestedConfigurationProperty
    // private Features features = new Features();

    @NestedConfigurationProperty
    private TcpServerProps tcpServer = new TcpServerProps();

    @NestedConfigurationProperty
    private UdpServerProps udpServer = new UdpServerProps();

    /**
     * 音视频流配置
     */
    private AudioVideoStreamProperties audioVideoStream = new AudioVideoStreamProperties();

    @Getter
    @Setter
    @ToString
    public static class AudioVideoStreamProperties {
        /**
         * 音视频流 编解码配置
         */
        @NestedConfigurationProperty
        private AvStreamCodecProperties codec = new AvStreamCodecProperties();

        /**
         * 音视频流 订阅者配置
         */
        @NestedConfigurationProperty
        private AvStreamSubscriberProperties subscriber = new AvStreamSubscriberProperties();
    }

    @Getter
    @Setter
    @ToString
    public static class AvStreamCodecProperties {
        @NestedConfigurationProperty
        private XtreamServerSchedulerProperties scheduler = new XtreamServerSchedulerProperties();
    }

    @Getter
    @Setter
    @ToString
    public static class AvStreamSubscriberProperties {
        @NestedConfigurationProperty
        private XtreamServerSchedulerProperties scheduler = new XtreamServerSchedulerProperties();
    }

    // @Getter
    // @Setter
    // @ToString
    // public static class Features {
    //
    //     // @NestedConfigurationProperty
    //     // private RequestLoggerFeature requestLogger = new RequestLoggerFeature();
    //
    //     @NestedConfigurationProperty
    //     private RequestCombinerFeature requestCombiner = new RequestCombinerFeature();
    // }

    // @Getter
    // @Setter
    // @ToString
    // public static class RequestCombinerFeature {
    //     private boolean enabled = true;
    //
    //     @NestedConfigurationProperty
    //     private RequestSubPacketStorage subPackageStorage = new RequestSubPacketStorage();
    // }

    // @Getter
    // @Setter
    // @ToString
    // public static class RequestSubPacketStorage {
    //     /**
    //      * 缓存最大大小
    //      */
    //     private int maximumSize = 1024;
    //     /**
    //      * 缓存条目的存活时间
    //      */
    //     private Duration ttl = Duration.ofSeconds(60);
    // }

    @Getter
    @Setter
    @ToString
    public static class TcpServerProps extends BaseServerProps {

        @NestedConfigurationProperty
        private TcpSessionIdleStateCheckerProps sessionIdleStateChecker = new TcpSessionIdleStateCheckerProps();

        @NestedConfigurationProperty
        private TcpLoopResourcesProperty loopResources = new TcpLoopResourcesProperty();

        private int maxFrameLength = 8192;
    }

    @Getter
    @Setter
    @ToString
    public static class UdpServerProps extends BaseServerProps {

        @NestedConfigurationProperty
        private UdpSessionIdleStateCheckerProps sessionIdleStateChecker = new UdpSessionIdleStateCheckerProps();

        @NestedConfigurationProperty
        private UdpLoopResourcesProperty loopResources = new UdpLoopResourcesProperty();

    }

    @Getter
    @Setter
    @ToString
    public static class TcpLoopResourcesProperty {
        private String threadNamePrefix = "x78-tcp";
        private int selectCount = LoopResources.DEFAULT_IO_SELECT_COUNT;
        private int workerCount = LoopResources.DEFAULT_IO_WORKER_COUNT;
        private boolean daemon = true;
        private boolean colocate = true;
        private boolean preferNative = LoopResources.DEFAULT_NATIVE;
    }

    @Getter
    @Setter
    @ToString
    public static class UdpLoopResourcesProperty {
        private String threadNamePrefix = "x78-udp";
        private int selectCount = LoopResources.DEFAULT_IO_SELECT_COUNT;
        private int workerCount = LoopResources.DEFAULT_IO_WORKER_COUNT;
        private boolean daemon = true;
        private boolean colocate = true;
        private boolean preferNative = LoopResources.DEFAULT_NATIVE;
    }

    @Getter
    @Setter
    @ToString
    public static class BaseServerProps {
        private boolean enabled = true;
        private String host;
        private int port;
    }
}
