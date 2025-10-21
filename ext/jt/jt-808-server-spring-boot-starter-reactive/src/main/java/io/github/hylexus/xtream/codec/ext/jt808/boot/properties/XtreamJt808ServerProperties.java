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

package io.github.hylexus.xtream.codec.ext.jt808.boot.properties;

import io.github.hylexus.xtream.codec.ext.jt808.utils.JtProtocolConstant;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.TcpSessionIdleStateCheckerProps;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.UdpSessionIdleStateCheckerProps;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import reactor.netty.resources.LoopResources;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "jt808-server")
@SuppressWarnings("NullAway")
public class XtreamJt808ServerProperties {
    private boolean enabled = true;

    @NestedConfigurationProperty
    private Features features = new Features();

    @NestedConfigurationProperty
    private InstructionServerProps instructionServer = new InstructionServerProps();

    @NestedConfigurationProperty
    private AttachmentServerProps attachmentServer = new AttachmentServerProps();

    @NestedConfigurationProperty
    private XtreamEventPublisherProps eventPublisher = new XtreamEventPublisherProps();

    @NestedConfigurationProperty
    private SchedulerProperties schedulers = new SchedulerProperties();

    @Getter
    @Setter
    @ToString
    public static class XtreamEventPublisherProps {
        public PublisherType publisherType = PublisherType.REACTOR;

        @NestedConfigurationProperty
        private EventPublisherReactorProps reactor = new EventPublisherReactorProps();

        @NestedConfigurationProperty
        private EventPublisherDisruptorProps disruptor = new EventPublisherDisruptorProps();

        public enum PublisherType {
            REACTOR,
            DISRUPTOR,
        }
    }

    @Getter
    @Setter
    @ToString
    public static class EventPublisherDisruptorProps {
        private String threadName = "xep-disruptor";
        private boolean daemon = false;
        private int ringBufferSize = 1024 * 8;
    }

    @Getter
    @Setter
    @ToString
    public static class EventPublisherReactorProps {
        private String threadName = "xtream-event-publisher";
    }

    @Getter
    @Setter
    @ToString
    public static class InstructionServerProps {

        @NestedConfigurationProperty
        private TcpServerProps tcpServer = new TcpServerProps();

        @NestedConfigurationProperty
        private UdpServerProps udpServer = new UdpServerProps();
    }

    @Getter
    @Setter
    @ToString
    public static class AttachmentServerProps {

        @NestedConfigurationProperty
        private TcpAttachmentServerProps tcpServer = new TcpAttachmentServerProps();

        @NestedConfigurationProperty
        private UdpAttachmentServerProps udpServer = new UdpAttachmentServerProps();
    }

    @Getter
    @Setter
    @ToString
    public static class BaseServerProps {
        private boolean enabled = true;
        private String host;
        private int port;
    }

    @Getter
    @Setter
    @ToString
    public static class TcpServerProps extends BaseServerProps {

        @NestedConfigurationProperty
        private TcpSessionIdleStateCheckerProps sessionIdleStateChecker = new TcpSessionIdleStateCheckerProps();

        @NestedConfigurationProperty
        private TcpLoopResourcesProperty loopResources = new TcpLoopResourcesProperty();
        /**
         * 指令服务器: 指令报文长度限制
         */
        private int maxInstructionFrameLength = JtProtocolConstant.DEFAULT_MAX_INSTRUCTION_FRAME_LENGTH;
    }

    @Getter
    @Setter
    @ToString
    public static class TcpAttachmentServerProps extends BaseServerProps {

        @NestedConfigurationProperty
        private TcpSessionIdleStateCheckerProps sessionIdleStateChecker = new TcpSessionIdleStateCheckerProps();

        @NestedConfigurationProperty
        private TcpLoopResourcesProperty loopResources = new TcpLoopResourcesProperty();

        /**
         * 附件服务器: 指令报文长度限制
         */
        private int maxInstructionFrameLength = JtProtocolConstant.DEFAULT_MAX_INSTRUCTION_FRAME_LENGTH;

        /**
         * 附件服务器: 苏标扩展消息 0x30316364 的报文长度限制
         */
        private int maxStreamFrameLength = JtProtocolConstant.DEFAULT_MAX_STREAM_FRAME_LENGTH;
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
    public static class UdpAttachmentServerProps extends BaseServerProps {

        @NestedConfigurationProperty
        private UdpSessionIdleStateCheckerProps sessionIdleStateChecker = new UdpSessionIdleStateCheckerProps();

        @NestedConfigurationProperty
        private UdpLoopResourcesProperty loopResources = new UdpLoopResourcesProperty();

    }

    @Getter
    @Setter
    @ToString
    public static class SchedulerProperties {
        @NestedConfigurationProperty
        private XtreamServerSchedulerProperties requestDispatcher = new XtreamServerSchedulerProperties();

        @NestedConfigurationProperty
        private XtreamServerSchedulerProperties nonBlockingHandler = new XtreamServerSchedulerProperties();

        @NestedConfigurationProperty
        private XtreamServerSchedulerProperties blockingHandler = new XtreamServerSchedulerProperties();

        @NestedConfigurationProperty
        private XtreamServerSchedulerProperties eventPublisher = new XtreamServerSchedulerProperties();

        private Map<String, NamedXtreamServerSchedulerProperties> customSchedulers = new LinkedHashMap<>();
    }

    @Getter
    @Setter
    @ToString
    public static class NamedXtreamServerSchedulerProperties extends XtreamServerSchedulerProperties {
        private String name;
    }

    @Getter
    @Setter
    @ToString
    public static class TcpLoopResourcesProperty {
        private String threadNamePrefix = "xtream-tcp";
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
        private String threadNamePrefix = "xtream-udp";
        private int selectCount = LoopResources.DEFAULT_IO_SELECT_COUNT;
        private int workerCount = LoopResources.DEFAULT_IO_WORKER_COUNT;
        private boolean daemon = true;
        private boolean colocate = true;
        private boolean preferNative = LoopResources.DEFAULT_NATIVE;
    }

    @Getter
    @Setter
    @ToString
    public static class Features {

        @NestedConfigurationProperty
        private RequestDispatcherFeature requestDispatcherScheduler = new RequestDispatcherFeature();

        @NestedConfigurationProperty
        private RequestLoggerFeature requestLogger = new RequestLoggerFeature();

        @NestedConfigurationProperty
        private RequestCombinerFeature requestCombiner = new RequestCombinerFeature();
    }

    @Getter
    @Setter
    @ToString
    public static class RequestDispatcherFeature {
        private boolean enabled = true;
    }

    @Getter
    @Setter
    @ToString
    public static class RequestLoggerFeature {
        private boolean enabled = true;
    }

    @Getter
    @Setter
    @ToString
    public static class RequestCombinerFeature {
        private boolean enabled = true;

        @NestedConfigurationProperty
        private RequestSubPacketStorage subPackageStorage = new RequestSubPacketStorage();
    }

    @Getter
    @Setter
    @ToString
    public static class RequestSubPacketStorage {
        /**
         * 缓存最大大小
         */
        private int maximumSize = 1024;
        /**
         * 缓存条目的存活时间
         */
        private Duration ttl = Duration.ofSeconds(60);
    }

}
