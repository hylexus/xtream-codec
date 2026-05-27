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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext;

import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

/**
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x1212, desc = "苏标-文件上传完成消息(表-4-27)")
public class BuiltinMessage1212 {

    // @Preset.JtStyle.Str(lengthExpression = "getFileNameLength()")
    // 前置一个 u8 类型的字段，表示文件名称的长度
    @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "文件名称")
    private String fileName;

    /**
     * 文件类型
     * <li>0x00：图片</li>
     * <li>0x01：音频</li>
     * <li>0x02：视频</li>
     * <li>0x03：文本</li>
     * <li>0x04：其它</li>
     */
    @Preset.JtStyle.Byte(desc = "文件类型")
    private short fileType;

    @Preset.JtStyle.Dword(desc = "文件大小")
    private long fileSize;

    public String getFileName() {
        return fileName;
    }

    public BuiltinMessage1212 setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public short getFileType() {
        return fileType;
    }

    public BuiltinMessage1212 setFileType(short fileType) {
        this.fileType = fileType;
        return this;
    }

    public long getFileSize() {
        return fileSize;
    }

    public BuiltinMessage1212 setFileSize(long fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage1212.class.getSimpleName() + "[", "]")
                .add("fileName='" + fileName + "'")
                .add("fileType=" + fileType)
                .add("fileSize=" + fileSize)
                .toString();
    }
}
