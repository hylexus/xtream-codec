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

package io.github.hylexus.xtream.codec.core.tracker;

import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 存储多媒体数据检索
 *
 * @author hylexus
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class CodecDebugEntity01 {

    /**
     * 多媒体类型
     * <p>
     * 0：图像；1：音频；2：视频；
     */
    @Preset.JtStyle.Byte(desc = "多媒体类型")
    private short multimediaType;
    /**
     * 通道ID
     * <p>
     * 0表示检索该媒体类型的所有通道；
     */
    @Preset.JtStyle.Byte(desc = "通道ID")
    private short channelId;

    @Preset.JtStyle.Byte(desc = "事件项编码")
    private short eventItemCode;

    @Preset.JtStyle.BcdDateTime(desc = "起始时间")
    private LocalDateTime startTime;

    @Preset.JtStyle.BcdDateTime(desc = "结束时间")
    private LocalDateTime endTime;

}
