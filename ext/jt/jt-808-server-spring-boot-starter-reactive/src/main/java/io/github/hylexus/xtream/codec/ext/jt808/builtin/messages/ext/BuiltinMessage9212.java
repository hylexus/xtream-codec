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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext;

import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.List;
import java.util.StringJoiner;

/**
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x9212, desc = "苏标-文件上传完成消息应答(表-4-28)")
public class BuiltinMessage9212 {

    // @Preset.JtStyle.Str
    // prependLengthFieldType: 前置一个 u8 类型的字段，表示文件名称长度
    @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "文件名称")
    private String fileName;

    // 0x00：图片
    // 0x01：音频
    // 0x02：视频
    // 0x03：文本
    // 0x04：其它
    @Preset.JtStyle.Byte(desc = "文件类型")
    private short fileType;

    // 0x00：完成
    // 0x01：需要补传
    @Preset.JtStyle.Byte(desc = "上传结果")
    private short uploadResult;

    // 补传数据包数量 需要补传的数据包数量，无补传时该值为0
    @Preset.JtStyle.Byte(desc = "补传数据包数量")
    private short packageCountToReTransmit;

    @Preset.JtStyle.List(desc = "补传数据包列表")
    private List<RetransmitItem> retransmitItemList;

    public String getFileName() {
        return fileName;
    }

    public BuiltinMessage9212 setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public short getFileType() {
        return fileType;
    }

    public BuiltinMessage9212 setFileType(short fileType) {
        this.fileType = fileType;
        return this;
    }

    public short getUploadResult() {
        return uploadResult;
    }

    public BuiltinMessage9212 setUploadResult(short uploadResult) {
        this.uploadResult = uploadResult;
        return this;
    }

    public short getPackageCountToReTransmit() {
        return packageCountToReTransmit;
    }

    public BuiltinMessage9212 setPackageCountToReTransmit(short packageCountToReTransmit) {
        this.packageCountToReTransmit = packageCountToReTransmit;
        return this;
    }

    public List<RetransmitItem> getRetransmitItemList() {
        return retransmitItemList;
    }

    public BuiltinMessage9212 setRetransmitItemList(List<RetransmitItem> retransmitItemList) {
        this.retransmitItemList = retransmitItemList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage9212.class.getSimpleName() + "[", "]")
                .add("fileName='" + fileName + "'")
                .add("fileType=" + fileType)
                .add("uploadResult=" + uploadResult)
                .add("packageCountToReTransmit=" + packageCountToReTransmit)
                .add("retransmitItemList=" + retransmitItemList)
                .toString();
    }

    public static class RetransmitItem {
        @Preset.JtStyle.Dword(desc = "数据偏移量")
        private long dataOffset;

        @Preset.JtStyle.Dword(desc = "数据长度")
        private long dataLength;

        public long getDataOffset() {
            return dataOffset;
        }

        public RetransmitItem setDataOffset(long dataOffset) {
            this.dataOffset = dataOffset;
            return this;
        }

        public long getDataLength() {
            return dataLength;
        }

        public RetransmitItem setDataLength(long dataLength) {
            this.dataLength = dataLength;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", RetransmitItem.class.getSimpleName() + "[", "]")
                    .add("dataOffset=" + dataOffset)
                    .add("dataLength=" + dataLength)
                    .toString();
        }
    }
}
