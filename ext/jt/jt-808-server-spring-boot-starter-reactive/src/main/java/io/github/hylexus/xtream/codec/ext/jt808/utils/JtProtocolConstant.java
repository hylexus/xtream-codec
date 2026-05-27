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

package io.github.hylexus.xtream.codec.ext.jt808.utils;


import java.time.format.DateTimeFormatter;

/**
 * @author hylexus
 **/
public final class JtProtocolConstant {
    private JtProtocolConstant() {
    }

    public static final int PACKAGE_DELIMITER = 0x7E;
    public static final int DEFAULT_MAX_INSTRUCTION_FRAME_LENGTH = 1024;
    public static final int DEFAULT_MAX_STREAM_FRAME_LENGTH = 1024 * 65;


    public static final String DEFAULT_DATE_TIME_FORMAT = "yyMMddHHmmss";
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(JtProtocolConstant.DEFAULT_DATE_TIME_FORMAT);

    public static final String BEAN_NAME_CHANNEL_INBOUND_HANDLER_ADAPTER = "Jt808DelimiterBasedFrameDecoder";
    public static final String BEAN_NAME_CHANNEL_INBOUND_IDLE_STATE_HANDLER = "xtreamTcpIdleStateHandler";
    public static final String BEAN_NAME_CHANNEL_INBOUND_IDLE_STATE_HANDLER_CALLBACK = "xtreamTcpIdleStateHandlerCallback";

    public static final String BEAN_NAME_JT_808_TCP_XTREAM_NETTY_HANDLER_ADAPTER_INSTRUCTION_SERVER = "jt808TcpXtreamNettyHandlerAdapterInstructionServer";
    public static final String BEAN_NAME_JT_808_TCP_XTREAM_NETTY_HANDLER_ADAPTER_ATTACHMENT_SERVER = "jt808TcpXtreamNettyHandlerAdapterAttachmentServer";
    public static final String BEAN_NAME_JT_808_UDP_XTREAM_NETTY_HANDLER_ADAPTER_INSTRUCTION_SERVER = "jt808UdpXtreamNettyHandlerAdapterInstructionServer";
    public static final String BEAN_NAME_JT_808_UDP_XTREAM_NETTY_HANDLER_ADAPTER_ATTACHMENT_SERVER = "jt808UdpXtreamNettyHandlerAdapterAttachmentServer";

    public static final String BEAN_NAME_JT_808_TCP_XTREAM_NETTY_RESOURCE_FACTORY_INSTRUCTION_SERVER = "jt808TcpXtreamNettyResourceFactoryInstructionServer";
    public static final String BEAN_NAME_JT_808_TCP_XTREAM_NETTY_RESOURCE_FACTORY_ATTACHMENT_SERVER = "jt808TcpXtreamNettyResourceFactoryAttachmentServer";
    public static final String BEAN_NAME_JT_808_UDP_XTREAM_NETTY_RESOURCE_FACTORY_INSTRUCTION_SERVER = "jt808UdpXtreamNettyResourceFactoryInstructionServer";
    public static final String BEAN_NAME_JT_808_UDP_XTREAM_NETTY_RESOURCE_FACTORY_ATTACHMENT_SERVER = "jt808UdpXtreamNettyResourceFactoryAttachmentServer";

    public static final String BEAN_NAME_JT_808_TCP_XTREAM_SERVER_INSTRUCTION_SERVER = "jt808TcpXtreamServerInstructionServer";
    public static final String BEAN_NAME_JT_808_TCP_XTREAM_SERVER_ATTACHMENT_SERVER = "jt808TcpXtreamServerAttachmentServer";
    public static final String BEAN_NAME_JT_808_UDP_XTREAM_SERVER_INSTRUCTION_SERVER = "jt808UdpXtreamServerInstructionServer";
    public static final String BEAN_NAME_JT_808_UDP_XTREAM_SERVER_ATTACHMENT_SERVER = "jt808UdpXtreamServerAttachmentServer";

    // private static final AttributeKey<Jt808Session> NETTY_ATTR_KEY_TCP_ATTACHMENT_SESSION = AttributeKey.newInstance("jt808/attachment/tcp/" + Jt808Session.class.getName());

    // public static AttributeKey<Jt808Session> udpSessionKey(InetSocketAddress remoteAddress) {
    //     return AttributeKey.valueOf("jt808/attachment/udp/" + remoteAddress.toString());
    // }

    // public static AttributeKey<Jt808Session> tcpSessionKey() {
    //     return NETTY_ATTR_KEY_TCP_ATTACHMENT_SESSION;
    // }

}
