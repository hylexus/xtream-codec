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

package io.github.hylexus.xtream.codec.debugger.app;

import io.github.hylexus.xtream.codec.core.type.Preset;

/**
 * 独立调试工具内置的协议无关实体示例。
 * 仅用两个顺序字段演示实体目录、初始值模板和双向编解码，
 * 不引入 JT/T 808 等协议的消息头或帧规则。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public class SampleMessage {
    @Preset.RustStyle.u16
    private int sequence;

    @Preset.RustStyle.u8
    private short flags;

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public short getFlags() {
        return flags;
    }

    public void setFlags(short flags) {
        this.flags = flags;
    }
}
