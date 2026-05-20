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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.StringJoiner;

/**
 * 存储多媒体数据上传命令
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8803, desc = "存储多媒体数据上传命令")
public class BuiltinMessage8803 {

    @Preset.JtStyle.Byte(desc = "多媒体类型 0：图像；1：音频；2：视频；")
    private short multimediaType;

    @Preset.JtStyle.Byte(desc = "通道ID 0表示检索该媒体类型的所有通道")
    private short channelId;

    @Preset.JtStyle.Byte(desc = "事件项编码")
    private short eventItemCode;

    @Preset.JtStyle.BcdDateTime(desc = "起始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Preset.JtStyle.BcdDateTime(desc = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Preset.JtStyle.Byte(desc = "删除标志 0：保留；1：删除；")
    private short deleteFlag;

    public short getMultimediaType() {
        return multimediaType;
    }

    public BuiltinMessage8803 setMultimediaType(short multimediaType) {
        this.multimediaType = multimediaType;
        return this;
    }

    public short getChannelId() {
        return channelId;
    }

    public BuiltinMessage8803 setChannelId(short channelId) {
        this.channelId = channelId;
        return this;
    }

    public short getEventItemCode() {
        return eventItemCode;
    }

    public BuiltinMessage8803 setEventItemCode(short eventItemCode) {
        this.eventItemCode = eventItemCode;
        return this;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public BuiltinMessage8803 setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public BuiltinMessage8803 setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public short getDeleteFlag() {
        return deleteFlag;
    }

    public BuiltinMessage8803 setDeleteFlag(short deleteFlag) {
        this.deleteFlag = deleteFlag;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8803.class.getSimpleName() + "[", "]")
                .add("multimediaType=" + multimediaType)
                .add("channelId=" + channelId)
                .add("eventItemCode=" + eventItemCode)
                .add("startTime=" + startTime)
                .add("endTime=" + endTime)
                .add("deleteFlag=" + deleteFlag)
                .toString();
    }
}
