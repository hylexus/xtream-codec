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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.hylexus.xtream.codec.core.jackson.XtreamCodecDebugJsonSerializer;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * 多媒体数据上传
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x0801, desc = "多媒体数据上传")
public class BuiltinMessage0801 {

    /**
     * 多媒体数据ID > 0
     */
    @Preset.JtStyle.Dword(desc = "多媒体数据ID")
    private long multimediaDataID;

    /**
     * 多媒体类型
     * <p>
     * 0：图像；1：音频；2：视频；
     */
    @Preset.JtStyle.Byte(desc = "多媒体类型。0：图像；1：音频；2：视频；")
    private short multimediaType;

    /**
     * 多媒体格式编码
     * <li>0：JPEG</li>
     * <li>1：TIF</li>
     * <li>2：MP3</li>
     * <li>3：WAV</li>
     * <li>4：WMV</li>
     * 其他保留
     */
    @Preset.JtStyle.Byte(desc = "多媒体格式编码")
    private short multimediaFormatCode;

    /**
     * 事件项编码
     * <li>0：平台下发指令</li>
     * <li>1：定时动作</li>
     * <li>2：抢劫报警触发</li>
     * <li>3：碰撞侧翻报警触发</li>
     * <li>4：门开拍照</li>
     * <li>5：门关拍照</li>
     * <li>6：车门由开变关，时速从＜20公里到超过20公里</li>
     * <li>7：定距拍照</li>
     * 其他保留
     */
    @Preset.JtStyle.Byte(desc = "事件项编码")
    private short eventItemCode;

    /**
     * 通道ID
     */
    @Preset.JtStyle.Byte(desc = "通道ID")
    private short channelId;

    /**
     * 位置信息汇报(0x0200)消息体  BYTE[28]
     * <p>
     * 表示多媒体数据的位置基本信息数据
     */
    @Preset.JtStyle.Object(length = 28, desc = "位置信息汇报")
    private BuiltinMessage0200 location;

    /**
     * 多媒体数据包
     */
    @Preset.JtStyle.Bytes(desc = "多媒体数据包")
    @JsonSerialize(using = XtreamCodecDebugJsonSerializer.class)
    private byte[] mediaData;

    public long getMultimediaDataID() {
        return multimediaDataID;
    }

    public BuiltinMessage0801 setMultimediaDataID(long multimediaDataID) {
        this.multimediaDataID = multimediaDataID;
        return this;
    }

    public short getMultimediaType() {
        return multimediaType;
    }

    public BuiltinMessage0801 setMultimediaType(short multimediaType) {
        this.multimediaType = multimediaType;
        return this;
    }

    public short getMultimediaFormatCode() {
        return multimediaFormatCode;
    }

    public BuiltinMessage0801 setMultimediaFormatCode(short multimediaFormatCode) {
        this.multimediaFormatCode = multimediaFormatCode;
        return this;
    }

    public short getEventItemCode() {
        return eventItemCode;
    }

    public BuiltinMessage0801 setEventItemCode(short eventItemCode) {
        this.eventItemCode = eventItemCode;
        return this;
    }

    public short getChannelId() {
        return channelId;
    }

    public BuiltinMessage0801 setChannelId(short channelId) {
        this.channelId = channelId;
        return this;
    }

    public BuiltinMessage0200 getLocation() {
        return location;
    }

    public BuiltinMessage0801 setLocation(BuiltinMessage0200 location) {
        this.location = location;
        return this;
    }

    public byte[] getMediaData() {
        return mediaData;
    }

    public BuiltinMessage0801 setMediaData(byte[] mediaData) {
        this.mediaData = mediaData;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage0801.class.getSimpleName() + "[", "]")
                .add("multimediaDataID=" + multimediaDataID)
                .add("multimediaType=" + multimediaType)
                .add("multimediaFormatCode=" + multimediaFormatCode)
                .add("eventItemCode=" + eventItemCode)
                .add("channelId=" + channelId)
                .add("location=" + location)
                .add("mediaData=" + Arrays.toString(mediaData))
                .toString();
    }
}
