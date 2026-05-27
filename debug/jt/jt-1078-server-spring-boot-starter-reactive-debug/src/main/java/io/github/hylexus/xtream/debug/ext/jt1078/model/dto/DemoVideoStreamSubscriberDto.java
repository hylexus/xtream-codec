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

package io.github.hylexus.xtream.debug.ext.jt1078.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class DemoVideoStreamSubscriberDto {

    @NotNull(message = "终端手机号不能为空")
    @NotEmpty(message = "终端手机号不能为空")
    private String sim;

    // 逻辑通道号
    private short channel;

    // 超时时间(秒)
    // 超过 timeout 秒之后依然没有收到终端的数据 ==> 自动关闭当前订阅(websocket)
    private int timeout = 10;

    // 将 byte[] 以 base64 编码后再发送给客户端(jackson内置默认逻辑)?
    private boolean byteArrayAsBase64 = false;

    private int naluDecoderRingBufferSize = 1 << 18;

    @JsonProperty("hasAudio")
    private boolean hasAudio = true;

    @JsonProperty("hasVideo")
    private boolean hasVideo = true;

}
