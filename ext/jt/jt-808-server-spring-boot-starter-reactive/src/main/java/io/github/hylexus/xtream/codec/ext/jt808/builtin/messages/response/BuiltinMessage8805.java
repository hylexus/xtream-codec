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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

/**
 * 单条存储多媒体数据检索上传命令
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8805, desc = "单条存储多媒体数据检索上传命令")
public class BuiltinMessage8805 {

    @Preset.JtStyle.Dword(desc = "多媒体ID")
    private long multimediaId;

    @Preset.JtStyle.Byte(desc = "删除标志 0：保留；1：删除；")
    private short deleteFlag;

    public long getMultimediaId() {
        return multimediaId;
    }

    public BuiltinMessage8805 setMultimediaId(long multimediaId) {
        this.multimediaId = multimediaId;
        return this;
    }

    public short getDeleteFlag() {
        return deleteFlag;
    }

    public BuiltinMessage8805 setDeleteFlag(short deleteFlag) {
        this.deleteFlag = deleteFlag;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8805.class.getSimpleName() + "[", "]")
                .add("multimediaId=" + multimediaId)
                .add("deleteFlag=" + deleteFlag)
                .toString();
    }
}
