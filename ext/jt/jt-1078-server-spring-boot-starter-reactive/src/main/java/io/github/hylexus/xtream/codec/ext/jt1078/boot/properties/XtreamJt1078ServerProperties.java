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

package io.github.hylexus.xtream.codec.ext.jt1078.boot.properties;

import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.TcpSessionIdleStateCheckerProps;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.UdpSessionIdleStateCheckerProps;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import reactor.netty.resources.LoopResources;

import java.util.StringJoiner;

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

    public boolean isEnabled() {
        return enabled;
    }

    public XtreamJt1078ServerProperties setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public TcpServerProps getTcpServer() {
        return tcpServer;
    }

    public XtreamJt1078ServerProperties setTcpServer(TcpServerProps tcpServer) {
        this.tcpServer = tcpServer;
        return this;
    }

    public UdpServerProps getUdpServer() {
        return udpServer;
    }

    public XtreamJt1078ServerProperties setUdpServer(UdpServerProps udpServer) {
        this.udpServer = udpServer;
        return this;
    }

    public AudioVideoStreamProperties getAudioVideoStream() {
        return audioVideoStream;
    }

    public XtreamJt1078ServerProperties setAudioVideoStream(AudioVideoStreamProperties audioVideoStream) {
        this.audioVideoStream = audioVideoStream;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", XtreamJt1078ServerProperties.class.getSimpleName() + "[", "]")
                .add("enabled=" + enabled)
                .add("tcpServer=" + tcpServer)
                .add("udpServer=" + udpServer)
                .add("audioVideoStream=" + audioVideoStream)
                .toString();
    }

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

        public AvStreamCodecProperties getCodec() {
            return codec;
        }

        public AudioVideoStreamProperties setCodec(AvStreamCodecProperties codec) {
            this.codec = codec;
            return this;
        }

        public AvStreamSubscriberProperties getSubscriber() {
            return subscriber;
        }

        public AudioVideoStreamProperties setSubscriber(AvStreamSubscriberProperties subscriber) {
            this.subscriber = subscriber;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", AudioVideoStreamProperties.class.getSimpleName() + "[", "]")
                    .add("codec=" + codec)
                    .add("subscriber=" + subscriber)
                    .toString();
        }
    }

    public static class AvStreamCodecProperties {
        @NestedConfigurationProperty
        private XtreamServerSchedulerProperties scheduler = new XtreamServerSchedulerProperties();

        public XtreamServerSchedulerProperties getScheduler() {
            return scheduler;
        }

        public AvStreamCodecProperties setScheduler(XtreamServerSchedulerProperties scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", AvStreamCodecProperties.class.getSimpleName() + "[", "]")
                    .add("scheduler=" + scheduler)
                    .toString();
        }
    }

    public static class AvStreamSubscriberProperties {
        @NestedConfigurationProperty
        private XtreamServerSchedulerProperties scheduler = new XtreamServerSchedulerProperties();

        public XtreamServerSchedulerProperties getScheduler() {
            return scheduler;
        }

        public AvStreamSubscriberProperties setScheduler(XtreamServerSchedulerProperties scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", AvStreamSubscriberProperties.class.getSimpleName() + "[", "]")
                    .add("scheduler=" + scheduler)
                    .toString();
        }
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

    public static class TcpServerProps extends BaseServerProps {

        @NestedConfigurationProperty
        private TcpSessionIdleStateCheckerProps sessionIdleStateChecker = new TcpSessionIdleStateCheckerProps();

        @NestedConfigurationProperty
        private TcpLoopResourcesProperty loopResources = new TcpLoopResourcesProperty();

        private int maxFrameLength = 8192;

        public TcpSessionIdleStateCheckerProps getSessionIdleStateChecker() {
            return sessionIdleStateChecker;
        }

        public TcpServerProps setSessionIdleStateChecker(TcpSessionIdleStateCheckerProps sessionIdleStateChecker) {
            this.sessionIdleStateChecker = sessionIdleStateChecker;
            return this;
        }

        public TcpLoopResourcesProperty getLoopResources() {
            return loopResources;
        }

        public TcpServerProps setLoopResources(TcpLoopResourcesProperty loopResources) {
            this.loopResources = loopResources;
            return this;
        }

        public int getMaxFrameLength() {
            return maxFrameLength;
        }

        public TcpServerProps setMaxFrameLength(int maxFrameLength) {
            this.maxFrameLength = maxFrameLength;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", TcpServerProps.class.getSimpleName() + "[", "]")
                    .add("sessionIdleStateChecker=" + sessionIdleStateChecker)
                    .add("loopResources=" + loopResources)
                    .add("maxFrameLength=" + maxFrameLength)
                    .toString();
        }
    }

    public static class UdpServerProps extends BaseServerProps {

        @NestedConfigurationProperty
        private UdpSessionIdleStateCheckerProps sessionIdleStateChecker = new UdpSessionIdleStateCheckerProps();

        @NestedConfigurationProperty
        private UdpLoopResourcesProperty loopResources = new UdpLoopResourcesProperty();

        public UdpSessionIdleStateCheckerProps getSessionIdleStateChecker() {
            return sessionIdleStateChecker;
        }

        public UdpServerProps setSessionIdleStateChecker(UdpSessionIdleStateCheckerProps sessionIdleStateChecker) {
            this.sessionIdleStateChecker = sessionIdleStateChecker;
            return this;
        }

        public UdpLoopResourcesProperty getLoopResources() {
            return loopResources;
        }

        public UdpServerProps setLoopResources(UdpLoopResourcesProperty loopResources) {
            this.loopResources = loopResources;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", UdpServerProps.class.getSimpleName() + "[", "]")
                    .add("sessionIdleStateChecker=" + sessionIdleStateChecker)
                    .add("loopResources=" + loopResources)
                    .toString();
        }
    }

    public static class TcpLoopResourcesProperty {
        private String threadNamePrefix = "x78-tcp";
        private int selectCount = LoopResources.DEFAULT_IO_SELECT_COUNT;
        private int workerCount = LoopResources.DEFAULT_IO_WORKER_COUNT;
        private boolean daemon = true;
        private boolean colocate = true;
        private boolean preferNative = LoopResources.DEFAULT_NATIVE;

        public String getThreadNamePrefix() {
            return threadNamePrefix;
        }

        public TcpLoopResourcesProperty setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
            return this;
        }

        public int getSelectCount() {
            return selectCount;
        }

        public TcpLoopResourcesProperty setSelectCount(int selectCount) {
            this.selectCount = selectCount;
            return this;
        }

        public int getWorkerCount() {
            return workerCount;
        }

        public TcpLoopResourcesProperty setWorkerCount(int workerCount) {
            this.workerCount = workerCount;
            return this;
        }

        public boolean isDaemon() {
            return daemon;
        }

        public TcpLoopResourcesProperty setDaemon(boolean daemon) {
            this.daemon = daemon;
            return this;
        }

        public boolean isColocate() {
            return colocate;
        }

        public TcpLoopResourcesProperty setColocate(boolean colocate) {
            this.colocate = colocate;
            return this;
        }

        public boolean isPreferNative() {
            return preferNative;
        }

        public TcpLoopResourcesProperty setPreferNative(boolean preferNative) {
            this.preferNative = preferNative;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", TcpLoopResourcesProperty.class.getSimpleName() + "[", "]")
                    .add("threadNamePrefix='" + threadNamePrefix + "'")
                    .add("selectCount=" + selectCount)
                    .add("workerCount=" + workerCount)
                    .add("daemon=" + daemon)
                    .add("colocate=" + colocate)
                    .add("preferNative=" + preferNative)
                    .toString();
        }
    }

    public static class UdpLoopResourcesProperty {
        private String threadNamePrefix = "x78-udp";
        private int selectCount = LoopResources.DEFAULT_IO_SELECT_COUNT;
        private int workerCount = LoopResources.DEFAULT_IO_WORKER_COUNT;
        private boolean daemon = true;
        private boolean colocate = true;
        private boolean preferNative = LoopResources.DEFAULT_NATIVE;

        public String getThreadNamePrefix() {
            return threadNamePrefix;
        }

        public UdpLoopResourcesProperty setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
            return this;
        }

        public int getSelectCount() {
            return selectCount;
        }

        public UdpLoopResourcesProperty setSelectCount(int selectCount) {
            this.selectCount = selectCount;
            return this;
        }

        public int getWorkerCount() {
            return workerCount;
        }

        public UdpLoopResourcesProperty setWorkerCount(int workerCount) {
            this.workerCount = workerCount;
            return this;
        }

        public boolean isDaemon() {
            return daemon;
        }

        public UdpLoopResourcesProperty setDaemon(boolean daemon) {
            this.daemon = daemon;
            return this;
        }

        public boolean isColocate() {
            return colocate;
        }

        public UdpLoopResourcesProperty setColocate(boolean colocate) {
            this.colocate = colocate;
            return this;
        }

        public boolean isPreferNative() {
            return preferNative;
        }

        public UdpLoopResourcesProperty setPreferNative(boolean preferNative) {
            this.preferNative = preferNative;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", UdpLoopResourcesProperty.class.getSimpleName() + "[", "]")
                    .add("threadNamePrefix='" + threadNamePrefix + "'")
                    .add("selectCount=" + selectCount)
                    .add("workerCount=" + workerCount)
                    .add("daemon=" + daemon)
                    .add("colocate=" + colocate)
                    .add("preferNative=" + preferNative)
                    .toString();
        }
    }

    public static class BaseServerProps {
        private boolean enabled = true;
        private String host;
        private int port;

        public boolean isEnabled() {
            return enabled;
        }

        public BaseServerProps setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getHost() {
            return host;
        }

        public BaseServerProps setHost(String host) {
            this.host = host;
            return this;
        }

        public int getPort() {
            return port;
        }

        public BaseServerProps setPort(int port) {
            this.port = port;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", BaseServerProps.class.getSimpleName() + "[", "]")
                    .add("enabled=" + enabled)
                    .add("host='" + host + "'")
                    .add("port=" + port)
                    .toString();
        }
    }
}
