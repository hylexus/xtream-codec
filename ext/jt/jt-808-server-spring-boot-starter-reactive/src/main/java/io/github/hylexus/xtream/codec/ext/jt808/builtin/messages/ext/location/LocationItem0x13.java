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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location;

import io.github.hylexus.xtream.codec.core.type.Preset;

import java.util.StringJoiner;

/**
 * 0x13 路线行驶时间不足/过长报警附加信息消息体数据格式
 */
public class LocationItem0x13 {
    @Preset.JtStyle.Dword(desc = "路段ID")
    private long lineId;

    @Preset.JtStyle.Word(desc = "路段行驶时间")
    private int lineDrivenTime;

    @Preset.JtStyle.Byte(desc = "结果; 0：不足；1：过长")
    private short result;

    public long getLineId() {
        return lineId;
    }

    public LocationItem0x13 setLineId(long lineId) {
        this.lineId = lineId;
        return this;
    }

    public int getLineDrivenTime() {
        return lineDrivenTime;
    }

    public LocationItem0x13 setLineDrivenTime(int lineDrivenTime) {
        this.lineDrivenTime = lineDrivenTime;
        return this;
    }

    public short getResult() {
        return result;
    }

    public LocationItem0x13 setResult(short result) {
        this.result = result;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LocationItem0x13.class.getSimpleName() + "[", "]")
                .add("lineId=" + lineId)
                .add("lineDrivenTime=" + lineDrivenTime)
                .add("result=" + result)
                .toString();
    }
}
