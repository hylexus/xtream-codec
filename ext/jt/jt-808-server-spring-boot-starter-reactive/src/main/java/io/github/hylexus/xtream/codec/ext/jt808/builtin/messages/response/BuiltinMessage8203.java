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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

/**
 * 人工确认报警消息 0x8203
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8203, desc = "人工确认报警消息")
public class BuiltinMessage8203 {

    /**
     * 报警消息流水号;
     * <p>
     * 需人工确认的报警消息流水号，0 表示该报警类型所有消息
     */
    @Preset.JtStyle.Word(desc = "报警消息流水号")
    private int flowId;

    /**
     * 人工确认报警类型
     * <li>bi[0] - 1：确认紧急报警</li>
     * <li>bi[1~2] - 保留</li>
     * <li>bi[3] - 1：确认危险预警</li>
     * <li>bi[4~19] - 保留</li>
     * <li>bi[20] - 1：确认进出区域报警</li>
     * <li>bi[21] - 1：确认进出路线报警</li>
     * <li>bi[22] - 1：确认路段行驶时间不足/过长报警</li>
     * <li>bi[23~26] - 保留</li>
     * <li>bi[27] - 1：确认车辆非法点火报警</li>
     * <li>bi[28] - 1：确认车辆非法位移报警；</li>
     * <li>bi[29~31] - 保留</li>
     */
    @Preset.JtStyle.Dword(desc = "人工确认报警类型")
    private long alarmType;

    public int getFlowId() {
        return flowId;
    }

    public BuiltinMessage8203 setFlowId(int flowId) {
        this.flowId = flowId;
        return this;
    }

    public long getAlarmType() {
        return alarmType;
    }

    public BuiltinMessage8203 setAlarmType(long alarmType) {
        this.alarmType = alarmType;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8203.class.getSimpleName() + "[", "]")
                .add("flowId=" + flowId)
                .add("alarmType=" + alarmType)
                .toString();
    }
}
