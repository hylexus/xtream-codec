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

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

/**
 * 摄像头立即拍摄命令
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8801, desc = "摄像头立即拍摄命令")
public class BuiltinMessage8801 {
    /**
     * 通道ID > 0
     */
    @Preset.JtStyle.Byte(desc = "通道ID")
    private short chanelId;

    /**
     * 拍摄命令
     * <p>
     * 0 表示停止拍摄；
     * 0xFFFF 表示录像；
     * 其它表示拍照张数
     */
    @Preset.JtStyle.Word(desc = "拍摄命令")
    private int command;

    /**
     * 拍照间隔/录像时间
     * <p>
     * 秒，0表示按最小间隔拍照或一直录像
     */
    @Preset.JtStyle.Word(desc = "拍照间隔/录像时间")
    private int duration;

    /**
     * 保存标志
     * <p>
     * 1：保存；
     * 0：实时上传
     */
    @Preset.JtStyle.Byte(desc = "保存标志")
    private short saveFlag;

    /**
     * 分辨率
     */
    @Preset.JtStyle.Byte(desc = "分辨率")
    private short resolution;

    /**
     * 图像/视频质量
     * <p>
     * 1-10，1代表质量损失最小，10表示压缩比最大
     */
    @Preset.JtStyle.Byte(desc = "图像/视频质量")
    private short quality;

    @Preset.JtStyle.Byte(desc = "亮度")
    private short brightness;

    @Preset.JtStyle.Byte(desc = "对比度")
    private short contrastRate;

    @Preset.JtStyle.Byte(desc = "饱和度")
    private short saturation;

    @Preset.JtStyle.Byte(desc = "色度")
    private short chroma;

    public short getChanelId() {
        return chanelId;
    }

    public BuiltinMessage8801 setChanelId(short chanelId) {
        this.chanelId = chanelId;
        return this;
    }

    public int getCommand() {
        return command;
    }

    public BuiltinMessage8801 setCommand(int command) {
        this.command = command;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public BuiltinMessage8801 setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public short getSaveFlag() {
        return saveFlag;
    }

    public BuiltinMessage8801 setSaveFlag(short saveFlag) {
        this.saveFlag = saveFlag;
        return this;
    }

    public short getResolution() {
        return resolution;
    }

    public BuiltinMessage8801 setResolution(short resolution) {
        this.resolution = resolution;
        return this;
    }

    public short getQuality() {
        return quality;
    }

    public BuiltinMessage8801 setQuality(short quality) {
        this.quality = quality;
        return this;
    }

    public short getBrightness() {
        return brightness;
    }

    public BuiltinMessage8801 setBrightness(short brightness) {
        this.brightness = brightness;
        return this;
    }

    public short getContrastRate() {
        return contrastRate;
    }

    public BuiltinMessage8801 setContrastRate(short contrastRate) {
        this.contrastRate = contrastRate;
        return this;
    }

    public short getSaturation() {
        return saturation;
    }

    public BuiltinMessage8801 setSaturation(short saturation) {
        this.saturation = saturation;
        return this;
    }

    public short getChroma() {
        return chroma;
    }

    public BuiltinMessage8801 setChroma(short chroma) {
        this.chroma = chroma;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8801.class.getSimpleName() + "[", "]")
                .add("chanelId=" + chanelId)
                .add("command=" + command)
                .add("duration=" + duration)
                .add("saveFlag=" + saveFlag)
                .add("resolution=" + resolution)
                .add("quality=" + quality)
                .add("brightness=" + brightness)
                .add("contrastRate=" + contrastRate)
                .add("saturation=" + saturation)
                .add("chroma=" + chroma)
                .toString();
    }
}
