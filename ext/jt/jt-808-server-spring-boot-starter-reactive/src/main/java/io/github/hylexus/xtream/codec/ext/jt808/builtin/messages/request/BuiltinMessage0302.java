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
 * 提问应答 0x0302
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x0302, desc = "提问应答")
public class BuiltinMessage0302 {

    @Preset.JtStyle.Word(desc = "应答流水号")
    private int flowId;

    @Preset.JtStyle.Byte(desc = "答案ID")
    private short answerId;

    public int getFlowId() {
        return flowId;
    }

    public BuiltinMessage0302 setFlowId(int flowId) {
        this.flowId = flowId;
        return this;
    }

    public short getAnswerId() {
        return answerId;
    }

    public BuiltinMessage0302 setAnswerId(short answerId) {
        this.answerId = answerId;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage0302.class.getSimpleName() + "[", "]")
                .add("flowId=" + flowId)
                .add("answerId=" + answerId)
                .toString();
    }
}
