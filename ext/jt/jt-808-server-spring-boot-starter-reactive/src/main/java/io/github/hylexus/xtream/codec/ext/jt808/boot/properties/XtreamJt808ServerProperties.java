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

package io.github.hylexus.xtream.codec.ext.jt808.boot.properties;

import io.github.hylexus.xtream.codec.ext.jt808.utils.JtProtocolConstant;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.TcpSessionIdleStateCheckerProps;
import io.github.hylexus.xtream.codec.server.reactive.spec.domain.values.UdpSessionIdleStateCheckerProps;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import reactor.netty.resources.LoopResources;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

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

    public boolean isEnabled() {
        return enabled;
    }

    public XtreamJt808ServerProperties setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Features getFeatures() {
        return features;
    }

    public XtreamJt808ServerProperties setFeatures(Features features) {
        this.features = features;
        return this;
    }

    public InstructionServerProps getInstructionServer() {
        return instructionServer;
    }

    public XtreamJt808ServerProperties setInstructionServer(InstructionServerProps instructionServer) {
        this.instructionServer = instructionServer;
        return this;
    }

    public AttachmentServerProps getAttachmentServer() {
        return attachmentServer;
    }

    public XtreamJt808ServerProperties setAttachmentServer(AttachmentServerProps attachmentServer) {
        this.attachmentServer = attachmentServer;
        return this;
    }

    public XtreamEventPublisherProps getEventPublisher() {
        return eventPublisher;
    }

    public XtreamJt808ServerProperties setEventPublisher(XtreamEventPublisherProps eventPublisher) {
        this.eventPublisher = eventPublisher;
        return this;
    }

    public SchedulerProperties getSchedulers() {
        return schedulers;
    }

    public XtreamJt808ServerProperties setSchedulers(SchedulerProperties schedulers) {
        this.schedulers = schedulers;
        return this;
    }

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

        public PublisherType getPublisherType() {
            return publisherType;
        }

        public XtreamEventPublisherProps setPublisherType(PublisherType publisherType) {
            this.publisherType = publisherType;
            return this;
        }

        public EventPublisherReactorProps getReactor() {
            return reactor;
        }

        public XtreamEventPublisherProps setReactor(EventPublisherReactorProps reactor) {
            this.reactor = reactor;
            return this;
        }

        public EventPublisherDisruptorProps getDisruptor() {
            return disruptor;
        }

        public XtreamEventPublisherProps setDisruptor(EventPublisherDisruptorProps disruptor) {
            this.disruptor = disruptor;
            return this;
        }
    }

    public static class EventPublisherDisruptorProps {
        private String threadName = "xep-disruptor";
        private boolean daemon = false;
        private int ringBufferSize = 1024 * 8;

        public String getThreadName() {
            return threadName;
        }

        public EventPublisherDisruptorProps setThreadName(String threadName) {
            this.threadName = threadName;
            return this;
        }

        public boolean isDaemon() {
            return daemon;
        }

        public EventPublisherDisruptorProps setDaemon(boolean daemon) {
            this.daemon = daemon;
            return this;
        }

        public int getRingBufferSize() {
            return ringBufferSize;
        }

        public EventPublisherDisruptorProps setRingBufferSize(int ringBufferSize) {
            this.ringBufferSize = ringBufferSize;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", EventPublisherDisruptorProps.class.getSimpleName() + "[", "]")
                    .add("threadName='" + threadName + "'")
                    .add("daemon=" + daemon)
                    .add("ringBufferSize=" + ringBufferSize)
                    .toString();
        }
    }

    public static class EventPublisherReactorProps {
        private String threadName = "xtream-event-publisher";

        public String getThreadName() {
            return threadName;
        }

        public EventPublisherReactorProps setThreadName(String threadName) {
            this.threadName = threadName;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", EventPublisherReactorProps.class.getSimpleName() + "[", "]")
                    .add("threadName='" + threadName + "'")
                    .toString();
        }
    }

    public static class InstructionServerProps {

        @NestedConfigurationProperty
        private TcpServerProps tcpServer = new TcpServerProps();

        @NestedConfigurationProperty
        private UdpServerProps udpServer = new UdpServerProps();

        public TcpServerProps getTcpServer() {
            return tcpServer;
        }

        public InstructionServerProps setTcpServer(TcpServerProps tcpServer) {
            this.tcpServer = tcpServer;
            return this;
        }

        public UdpServerProps getUdpServer() {
            return udpServer;
        }

        public InstructionServerProps setUdpServer(UdpServerProps udpServer) {
            this.udpServer = udpServer;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", InstructionServerProps.class.getSimpleName() + "[", "]")
                    .add("tcpServer=" + tcpServer)
                    .add("udpServer=" + udpServer)
                    .toString();
        }
    }

    public static class AttachmentServerProps {

        @NestedConfigurationProperty
        private TcpAttachmentServerProps tcpServer = new TcpAttachmentServerProps();

        @NestedConfigurationProperty
        private UdpAttachmentServerProps udpServer = new UdpAttachmentServerProps();

        public TcpAttachmentServerProps getTcpServer() {
            return tcpServer;
        }

        public AttachmentServerProps setTcpServer(TcpAttachmentServerProps tcpServer) {
            this.tcpServer = tcpServer;
            return this;
        }

        public UdpAttachmentServerProps getUdpServer() {
            return udpServer;
        }

        public AttachmentServerProps setUdpServer(UdpAttachmentServerProps udpServer) {
            this.udpServer = udpServer;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", AttachmentServerProps.class.getSimpleName() + "[", "]")
                    .add("tcpServer=" + tcpServer)
                    .add("udpServer=" + udpServer)
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

    public static class TcpServerProps extends BaseServerProps {

        @NestedConfigurationProperty
        private TcpSessionIdleStateCheckerProps sessionIdleStateChecker = new TcpSessionIdleStateCheckerProps();

        @NestedConfigurationProperty
        private TcpLoopResourcesProperty loopResources = new TcpLoopResourcesProperty();
        /**
         * 指令服务器: 指令报文长度限制
         */
        private int maxInstructionFrameLength = JtProtocolConstant.DEFAULT_MAX_INSTRUCTION_FRAME_LENGTH;

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

        public int getMaxInstructionFrameLength() {
            return maxInstructionFrameLength;
        }

        public TcpServerProps setMaxInstructionFrameLength(int maxInstructionFrameLength) {
            this.maxInstructionFrameLength = maxInstructionFrameLength;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", TcpServerProps.class.getSimpleName() + "[", "]")
                    .add("sessionIdleStateChecker=" + sessionIdleStateChecker)
                    .add("loopResources=" + loopResources)
                    .add("maxInstructionFrameLength=" + maxInstructionFrameLength)
                    .toString();
        }
    }

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

        public TcpSessionIdleStateCheckerProps getSessionIdleStateChecker() {
            return sessionIdleStateChecker;
        }

        public TcpAttachmentServerProps setSessionIdleStateChecker(TcpSessionIdleStateCheckerProps sessionIdleStateChecker) {
            this.sessionIdleStateChecker = sessionIdleStateChecker;
            return this;
        }

        public TcpLoopResourcesProperty getLoopResources() {
            return loopResources;
        }

        public TcpAttachmentServerProps setLoopResources(TcpLoopResourcesProperty loopResources) {
            this.loopResources = loopResources;
            return this;
        }

        public int getMaxInstructionFrameLength() {
            return maxInstructionFrameLength;
        }

        public TcpAttachmentServerProps setMaxInstructionFrameLength(int maxInstructionFrameLength) {
            this.maxInstructionFrameLength = maxInstructionFrameLength;
            return this;
        }

        public int getMaxStreamFrameLength() {
            return maxStreamFrameLength;
        }

        public TcpAttachmentServerProps setMaxStreamFrameLength(int maxStreamFrameLength) {
            this.maxStreamFrameLength = maxStreamFrameLength;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", TcpAttachmentServerProps.class.getSimpleName() + "[", "]")
                    .add("sessionIdleStateChecker=" + sessionIdleStateChecker)
                    .add("loopResources=" + loopResources)
                    .add("maxInstructionFrameLength=" + maxInstructionFrameLength)
                    .add("maxStreamFrameLength=" + maxStreamFrameLength)
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

    public static class UdpAttachmentServerProps extends BaseServerProps {

        @NestedConfigurationProperty
        private UdpSessionIdleStateCheckerProps sessionIdleStateChecker = new UdpSessionIdleStateCheckerProps();

        @NestedConfigurationProperty
        private UdpLoopResourcesProperty loopResources = new UdpLoopResourcesProperty();

        public UdpSessionIdleStateCheckerProps getSessionIdleStateChecker() {
            return sessionIdleStateChecker;
        }

        public UdpAttachmentServerProps setSessionIdleStateChecker(UdpSessionIdleStateCheckerProps sessionIdleStateChecker) {
            this.sessionIdleStateChecker = sessionIdleStateChecker;
            return this;
        }

        public UdpLoopResourcesProperty getLoopResources() {
            return loopResources;
        }

        public UdpAttachmentServerProps setLoopResources(UdpLoopResourcesProperty loopResources) {
            this.loopResources = loopResources;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", UdpAttachmentServerProps.class.getSimpleName() + "[", "]")
                    .add("sessionIdleStateChecker=" + sessionIdleStateChecker)
                    .add("loopResources=" + loopResources)
                    .toString();
        }
    }

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

        public XtreamServerSchedulerProperties getRequestDispatcher() {
            return requestDispatcher;
        }

        public SchedulerProperties setRequestDispatcher(XtreamServerSchedulerProperties requestDispatcher) {
            this.requestDispatcher = requestDispatcher;
            return this;
        }

        public XtreamServerSchedulerProperties getNonBlockingHandler() {
            return nonBlockingHandler;
        }

        public SchedulerProperties setNonBlockingHandler(XtreamServerSchedulerProperties nonBlockingHandler) {
            this.nonBlockingHandler = nonBlockingHandler;
            return this;
        }

        public XtreamServerSchedulerProperties getBlockingHandler() {
            return blockingHandler;
        }

        public SchedulerProperties setBlockingHandler(XtreamServerSchedulerProperties blockingHandler) {
            this.blockingHandler = blockingHandler;
            return this;
        }

        public XtreamServerSchedulerProperties getEventPublisher() {
            return eventPublisher;
        }

        public SchedulerProperties setEventPublisher(XtreamServerSchedulerProperties eventPublisher) {
            this.eventPublisher = eventPublisher;
            return this;
        }

        public Map<String, NamedXtreamServerSchedulerProperties> getCustomSchedulers() {
            return customSchedulers;
        }

        public SchedulerProperties setCustomSchedulers(Map<String, NamedXtreamServerSchedulerProperties> customSchedulers) {
            this.customSchedulers = customSchedulers;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", SchedulerProperties.class.getSimpleName() + "[", "]")
                    .add("requestDispatcher=" + requestDispatcher)
                    .add("nonBlockingHandler=" + nonBlockingHandler)
                    .add("blockingHandler=" + blockingHandler)
                    .add("eventPublisher=" + eventPublisher)
                    .add("customSchedulers=" + customSchedulers)
                    .toString();
        }
    }

    public static class NamedXtreamServerSchedulerProperties extends XtreamServerSchedulerProperties {
        private String name;

        public String getName() {
            return name;
        }

        public NamedXtreamServerSchedulerProperties setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", NamedXtreamServerSchedulerProperties.class.getSimpleName() + "[", "]")
                    .add("name='" + name + "'")
                    .toString();
        }
    }

    public static class TcpLoopResourcesProperty {
        private String threadNamePrefix = "xtream-tcp";
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
        private String threadNamePrefix = "xtream-udp";
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

    public static class Features {

        @NestedConfigurationProperty
        private RequestDispatcherFeature requestDispatcherScheduler = new RequestDispatcherFeature();

        @NestedConfigurationProperty
        private RequestLoggerFeature requestLogger = new RequestLoggerFeature();

        @NestedConfigurationProperty
        private RequestCombinerFeature requestCombiner = new RequestCombinerFeature();

        public RequestDispatcherFeature getRequestDispatcherScheduler() {
            return requestDispatcherScheduler;
        }

        public Features setRequestDispatcherScheduler(RequestDispatcherFeature requestDispatcherScheduler) {
            this.requestDispatcherScheduler = requestDispatcherScheduler;
            return this;
        }

        public RequestLoggerFeature getRequestLogger() {
            return requestLogger;
        }

        public Features setRequestLogger(RequestLoggerFeature requestLogger) {
            this.requestLogger = requestLogger;
            return this;
        }

        public RequestCombinerFeature getRequestCombiner() {
            return requestCombiner;
        }

        public Features setRequestCombiner(RequestCombinerFeature requestCombiner) {
            this.requestCombiner = requestCombiner;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Features.class.getSimpleName() + "[", "]")
                    .add("requestDispatcherScheduler=" + requestDispatcherScheduler)
                    .add("requestLogger=" + requestLogger)
                    .add("requestCombiner=" + requestCombiner)
                    .toString();
        }
    }

    public static class RequestDispatcherFeature {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public RequestDispatcherFeature setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", RequestDispatcherFeature.class.getSimpleName() + "[", "]")
                    .add("enabled=" + enabled)
                    .toString();
        }
    }

    public static class RequestLoggerFeature {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public RequestLoggerFeature setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", RequestLoggerFeature.class.getSimpleName() + "[", "]")
                    .add("enabled=" + enabled)
                    .toString();
        }
    }

    public static class RequestCombinerFeature {
        private boolean enabled = true;

        @NestedConfigurationProperty
        private RequestSubPacketStorage subPackageStorage = new RequestSubPacketStorage();

        public boolean isEnabled() {
            return enabled;
        }

        public RequestCombinerFeature setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public RequestSubPacketStorage getSubPackageStorage() {
            return subPackageStorage;
        }

        public RequestCombinerFeature setSubPackageStorage(RequestSubPacketStorage subPackageStorage) {
            this.subPackageStorage = subPackageStorage;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", RequestCombinerFeature.class.getSimpleName() + "[", "]")
                    .add("enabled=" + enabled)
                    .add("subPackageStorage=" + subPackageStorage)
                    .toString();
        }
    }

    public static class RequestSubPacketStorage {
        /**
         * 缓存最大大小
         */
        private int maximumSize = 1024;
        /**
         * 缓存条目的存活时间
         */
        private Duration ttl = Duration.ofSeconds(60);

        public int getMaximumSize() {
            return maximumSize;
        }

        public RequestSubPacketStorage setMaximumSize(int maximumSize) {
            this.maximumSize = maximumSize;
            return this;
        }

        public Duration getTtl() {
            return ttl;
        }

        public RequestSubPacketStorage setTtl(Duration ttl) {
            this.ttl = ttl;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", RequestSubPacketStorage.class.getSimpleName() + "[", "]")
                    .add("maximumSize=" + maximumSize)
                    .add("ttl=" + ttl)
                    .toString();
        }
    }

}
