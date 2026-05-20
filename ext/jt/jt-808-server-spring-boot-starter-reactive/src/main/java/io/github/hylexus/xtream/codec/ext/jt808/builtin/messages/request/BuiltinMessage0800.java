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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

/**
 * 多媒体事件信息上传
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x0800, desc = "多媒体事件信息上传")
public class BuiltinMessage0800 {

    /**
     * 多媒体数据ID >0
     * <p>
     * 包含的CAN总线数据项个数，>0
     */
    @Preset.JtStyle.Dword(desc = "多媒体数据ID")
    private long multimediaDataID;

    /**
     * 多媒体类型
     * <p>
     * 0：图像；1：音频；2：视频；
     */
    @Preset.JtStyle.Byte(desc = "多媒体类型。 0：图像；1：音频；2：视频；")
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
    @Preset.JtStyle.Byte(desc = "多媒体格式编码。0：JPEG; 1: TIF; 2: MP3; 3: WAV; 4: WMV")
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

    public long getMultimediaDataID() {
        return multimediaDataID;
    }

    public BuiltinMessage0800 setMultimediaDataID(long multimediaDataID) {
        this.multimediaDataID = multimediaDataID;
        return this;
    }

    public short getMultimediaType() {
        return multimediaType;
    }

    public BuiltinMessage0800 setMultimediaType(short multimediaType) {
        this.multimediaType = multimediaType;
        return this;
    }

    public short getMultimediaFormatCode() {
        return multimediaFormatCode;
    }

    public BuiltinMessage0800 setMultimediaFormatCode(short multimediaFormatCode) {
        this.multimediaFormatCode = multimediaFormatCode;
        return this;
    }

    public short getEventItemCode() {
        return eventItemCode;
    }

    public BuiltinMessage0800 setEventItemCode(short eventItemCode) {
        this.eventItemCode = eventItemCode;
        return this;
    }

    public short getChannelId() {
        return channelId;
    }

    public BuiltinMessage0800 setChannelId(short channelId) {
        this.channelId = channelId;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage0800.class.getSimpleName() + "[", "]")
                .add("multimediaDataID=" + multimediaDataID)
                .add("multimediaType=" + multimediaType)
                .add("multimediaFormatCode=" + multimediaFormatCode)
                .add("eventItemCode=" + eventItemCode)
                .add("channelId=" + channelId)
                .toString();
    }
}
