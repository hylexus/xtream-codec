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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext;

import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.AlarmIdentifier;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

/**
 * 报警附件上传指令(苏标)
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x9208, desc = "苏标-报警附件上传指令(表-4-21)")
public class BuiltinMessage9208 {

    // STRING
    // prependLengthFieldType: 前置一个 u8 类型的字段，表示附件服务器IP地址长度
    @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "附件服务器IP地址")
    private String attachmentServerIp;

    // WORD
    @Preset.JtStyle.Word(desc = "附件服务器端口（TCP）")
    private int attachmentServerPortTcp;

    // WORD
    @Preset.JtStyle.Word(desc = "附件服务器端口（UDP）")
    private int attachmentServerPortUdp;

    // BYTE[16]
    @Preset.JtStyle.Object(desc = "报警标识号(表4 16)")
    private AlarmIdentifier alarmIdentifier;

    // BYTE[32]
    @Preset.JtStyle.Bytes(desc = "报警编号:平台给报警分配的唯一编号", length = 32)
    private String alarmNo;

    // BYTE[16]
    @Preset.JtStyle.Bytes(desc = "预留")
    private String reservedByte16 = "0000000000000000";

    public String getAttachmentServerIp() {
        return attachmentServerIp;
    }

    public BuiltinMessage9208 setAttachmentServerIp(String attachmentServerIp) {
        this.attachmentServerIp = attachmentServerIp;
        return this;
    }

    public int getAttachmentServerPortTcp() {
        return attachmentServerPortTcp;
    }

    public BuiltinMessage9208 setAttachmentServerPortTcp(int attachmentServerPortTcp) {
        this.attachmentServerPortTcp = attachmentServerPortTcp;
        return this;
    }

    public int getAttachmentServerPortUdp() {
        return attachmentServerPortUdp;
    }

    public BuiltinMessage9208 setAttachmentServerPortUdp(int attachmentServerPortUdp) {
        this.attachmentServerPortUdp = attachmentServerPortUdp;
        return this;
    }

    public AlarmIdentifier getAlarmIdentifier() {
        return alarmIdentifier;
    }

    public BuiltinMessage9208 setAlarmIdentifier(AlarmIdentifier alarmIdentifier) {
        this.alarmIdentifier = alarmIdentifier;
        return this;
    }

    public String getAlarmNo() {
        return alarmNo;
    }

    public BuiltinMessage9208 setAlarmNo(String alarmNo) {
        this.alarmNo = alarmNo;
        return this;
    }

    public String getReservedByte16() {
        return reservedByte16;
    }

    public BuiltinMessage9208 setReservedByte16(String reservedByte16) {
        this.reservedByte16 = reservedByte16;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage9208.class.getSimpleName() + "[", "]")
                .add("attachmentServerIp='" + attachmentServerIp + "'")
                .add("attachmentServerPortTcp=" + attachmentServerPortTcp)
                .add("attachmentServerPortUdp=" + attachmentServerPortUdp)
                .add("alarmIdentifier=" + alarmIdentifier)
                .add("alarmNo='" + alarmNo + "'")
                .add("reservedByte16='" + reservedByte16 + "'")
                .toString();
    }
}
