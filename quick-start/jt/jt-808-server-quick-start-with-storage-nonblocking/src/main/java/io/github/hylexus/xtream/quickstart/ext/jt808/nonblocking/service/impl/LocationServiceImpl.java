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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.impl;

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.ext.jt808.boot.condition.ConditionalOnJt808Server;
import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage9208;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.*;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request.BuiltinMessage0200;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.Jt808CommandSender;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.configuration.props.QuickStartAppProps;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Service
@ConditionalOnJt808Server(serverType = ConditionalOnJt808Server.ServerType.INSTRUCTION_SERVER,protocolType = ConditionalOnJt808Server.ProtocolType.ANY)
public class LocationServiceImpl implements LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationServiceImpl.class);
    private final XtreamJt808ServerProperties serverProperties;
    private final Jt808CommandSender commandSender;
    private final QuickStartAppProps demoAppConfigProperties;

    public LocationServiceImpl(XtreamJt808ServerProperties serverProperties, Jt808CommandSender commandSender, QuickStartAppProps demoAppConfigProperties) {
        this.serverProperties = serverProperties;
        this.commandSender = commandSender;
        this.demoAppConfigProperties = demoAppConfigProperties;
    }

    @Override
    public Mono<Byte> processLocationMessage(Jt808Session session, BuiltinMessage0200 body) {
        // 处理其他消息属性
        // ...

        // 处理附加项列表
        final Map<Short, Object> extraItems = body.getExtraItems();
        if (!CollectionUtils.isEmpty(extraItems)) {
            this.processExtraItemList(session, extraItems);
        }
        // 成功
        return Mono.just((byte) 0);
    }

    private void processExtraItemList(Jt808Session session, Map<Short, Object> groupById) {
        // 苏标: 高级驾驶辅助系统报警信息，定义见表 4-15
        // ADAS模块视频通道
        this.process0x64IfNecessary(session, (LocationItem0x64) groupById.get((short) 0x64));

        // 苏标: 驾驶员状态监测系统报警信息，定义见表 4-17
        // DSM模块视频通道
        this.process0x65IfNecessary(session, (LocationItem0x65) groupById.get((short) 0x65));

        // 苏标: 胎压监测系统报警信息，定义见表 4-18
        // TPMS 轮胎气压监测系统
        this.process0x66IfNecessary(session, (LocationItem0x66) groupById.get((short) 0x66));

        // 苏标: 盲区监测系统报警信息，定义见表 4-20
        // BSD 盲点监测系统
        this.process0x67IfNecessary(session, (LocationItem0x67) groupById.get((short) 0x67));
    }

    private void process0x67IfNecessary(Jt808Session session, LocationItem0x67 message67) {
        if (message67 == null) {
            return;
        }
        log.info("ExtraItem-67==>AlarmIdentifier: {}", message67.getAlarmIdentifier());
        this.doSendMsg9208(session, message67.getAlarmIdentifier());
    }

    private void process0x66IfNecessary(Jt808Session session, LocationItem0x66 message66) {
        if (message66 == null) {
            return;
        }
        log.info("ExtraItem-66==>AlarmIdentifier: {}", message66.getAlarmIdentifier());
        this.doSendMsg9208(session, message66.getAlarmIdentifier());
    }

    private void process0x65IfNecessary(Jt808Session session, LocationItem0x65 message65) {
        if (message65 == null) {
            return;
        }
        log.info("ExtraItem-65==>AlarmIdentifier: {}", message65.getAlarmIdentifier());
        this.doSendMsg9208(session, message65.getAlarmIdentifier());
    }

    private void process0x64IfNecessary(Jt808Session session, LocationItem0x64 message64) {
        if (message64 == null) {
            return;
        }
        log.info("ExtraItem-64==>AlarmIdentifier: {}", message64.getAlarmIdentifier());
        this.doSendMsg9208(session, message64.getAlarmIdentifier());
    }

    private void doSendMsg9208(Jt808Session session, AlarmIdentifier alarmIdentifier) {
        final BuiltinMessage9208 msg9208 = new BuiltinMessage9208();

        final String serverIp = demoAppConfigProperties.getAttachmentServer().getServerIp();
        msg9208.setAttachmentServerIp(serverIp);
        msg9208.setAttachmentServerPortTcp(serverProperties.getAttachmentServer().getTcpServer().getPort());
        msg9208.setAttachmentServerPortUdp(serverProperties.getAttachmentServer().getUdpServer().getPort());
        msg9208.setAlarmIdentifier(alarmIdentifier);
        msg9208.setAlarmNo(XtreamBytes.randomString(32));
        msg9208.setReservedByte16("0000000000000000");

        this.commandSender.sendObject(session.id(), Mono.just(msg9208))
                .publishOn(Schedulers.boundedElastic())
                .subscribe();
    }

}
