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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.converter;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage1210;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.entity.Jt808AlarmAttachmentInfoEntity;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.entity.Jt808RequestTraceLogEntity;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.entity.Jt808ResponseTraceLogEntity;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.event.Jt808EventPayloads;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.values.Jt808NetType;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.values.Jt808Version;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.vo.Jt808AlarmAttachmentInfoVo;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.vo.Jt808TraceLogVo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * @author hylexus
 */
public class Jt808EntityConverter {

    public static Jt808RequestTraceLogEntity toRequestLogEntity(Jt808EventPayloads.Jt808ReceiveEvent event) {
        return new Jt808RequestTraceLogEntity()
                .setRequestId(event.requestId())
                .setId(UUID.randomUUID().toString())
                .setReceivedAt(event.receiveTime())
                .setNetType(Jt808NetType.fromName(event.netType().name()))
                .setTraceId(event.traceId())
                .setTerminalId(event.terminalId())
                .setMessageId(event.messageId())
                .setVersion(Jt808Version.fromValue(event.version().versionValue()))
                .setFlowId(event.flowId())
                .setMessageBodyLength(event.messageBodyLength())
                .setMessageBodyProperty(event.messageBodyProperty())
                .setTotalPackage(event.totalPackage())
                .setCurrentPackageNo(event.currentPackage())
                .setSubpackage(event.isSubPackage())
                .setRawHex(event.rawHexString())
                .setEscapedHex(event.rawHexString())
                .setCreatedAt(LocalDateTime.now());
    }

    public static Jt808ResponseTraceLogEntity toResponseLogEntity(Jt808EventPayloads.Jt808SendEvent event) {
        return new Jt808ResponseTraceLogEntity()
                .setRequestId(event.requestId())
                .setId(UUID.randomUUID().toString())
                .setSentAt(event.sentAt())
                .setNetType(Jt808NetType.fromName(event.netType().name()))
                .setTraceId(event.traceId())
                .setTerminalId(event.terminalId())
                .setEscapedHex(event.hexString())
                .setCreatedAt(LocalDateTime.now());
    }

    public static Jt808AlarmAttachmentInfoEntity toAlarmInfoEntity(String terminalId, String filePath, BuiltinMessage1210.AttachmentItem attachmentItem) {
        final BuiltinMessage1210 group = attachmentItem.getGroup();
        return new Jt808AlarmAttachmentInfoEntity()
                .setId(UUID.randomUUID().toString())
                .setTerminalId(terminalId)
                .setAlarmNo(group.getAlarmNo())
                .setAlarmTime(group.getAlarmIdentifier().getTime())
                .setAlarmSequence(group.getAlarmIdentifier().getSequence())
                .setAttachmentCount(group.getAttachmentCount())
                .setClientId(group.getAlarmIdentifier().getTerminalId())
                .setFileName(attachmentItem.getFileName())
                .setFileType(attachmentItem.getFileType())
                .setFileSize(attachmentItem.getFileSize())
                .setFilePath(filePath)
                .setCreatedAt(LocalDateTime.now())
                ;
    }

    public static Jt808AlarmAttachmentInfoVo armInfoEntityToVo(Jt808AlarmAttachmentInfoEntity entity) {
        final Jt808AlarmAttachmentInfoVo vo = new Jt808AlarmAttachmentInfoVo();
        vo.setId(entity.getId());
        vo.setTerminalId(entity.getTerminalId());
        vo.setAlarmNo(entity.getAlarmNo());
        vo.setAlarmTime(entity.getAlarmTime());
        vo.setAlarmSequence(entity.getAlarmSequence());
        vo.setAttachmentCount(entity.getAttachmentCount());
        vo.setClientId(entity.getClientId());
        vo.setFileName(entity.getFileName());
        vo.setFileType(entity.getFileType());
        vo.setFileSize(entity.getFileSize());
        vo.setFilePath(entity.getFilePath());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static void setMessageDescription(List<Jt808TraceLogVo> voList, Function<Integer, String> descriptionProvider, String def) {
        for (final Jt808TraceLogVo vo : voList) {
            final String desc = descriptionProvider.apply(vo.getMessageId());
            vo.setMessageDesc(desc == null ? def : desc);
        }
    }
}
