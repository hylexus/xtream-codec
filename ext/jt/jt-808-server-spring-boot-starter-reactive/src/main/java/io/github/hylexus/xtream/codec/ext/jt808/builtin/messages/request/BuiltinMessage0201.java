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

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

/**
 * 位置信息查询应答 0x0201
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x0201, desc = "位置信息查询应答")
public class BuiltinMessage0201 {

    /**
     * 应答流水号
     */
    @Preset.JtStyle.Word(desc = "应答流水号")
    private int flowId;

    /**
     * 位置汇报信息: 和 0x0200 消息格式一致
     */
    @Preset.JtStyle.Object(desc = "位置汇报信息")
    private BuiltinMessage0200 locationMessage;

    public int getFlowId() {
        return flowId;
    }

    public BuiltinMessage0201 setFlowId(int flowId) {
        this.flowId = flowId;
        return this;
    }

    public BuiltinMessage0200 getLocationMessage() {
        return locationMessage;
    }

    public BuiltinMessage0201 setLocationMessage(BuiltinMessage0200 locationMessage) {
        this.locationMessage = locationMessage;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage0201.class.getSimpleName() + "[", "]")
                .add("flowId=" + flowId)
                .add("locationMessage=" + locationMessage)
                .toString();
    }
}
