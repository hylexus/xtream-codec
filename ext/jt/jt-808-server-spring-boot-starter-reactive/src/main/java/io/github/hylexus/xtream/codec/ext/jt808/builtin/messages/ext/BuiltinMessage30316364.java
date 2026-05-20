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

import io.github.hylexus.xtream.codec.core.type.Preset;

/**
 * TJSATL12—2017道路运输车辆主动安全智能防控系统 $4.6.4--文件数据上传
 * <p>
 * 该消息不需要回复客户端
 *
 * @author hylexus
 */
public class BuiltinMessage30316364 {

    // BYTE[50]  文件名称
    @Preset.JtStyle.Bytes(length = 50)
    private String fileName;

    // DWORD 数据偏移量
    @Preset.JtStyle.Dword
    private long dataOffset;

    // DWORD 数据长度
    @Preset.JtStyle.Dword
    private long dataLength;

    // BYTE[n] 数据体 默认长度64K，文件小于64K则为实际长度
    @Preset.JtStyle.Bytes(lengthExpression = "getDataLength()")
    private byte[] data;

    public String getFileName() {
        return fileName;
    }

    public BuiltinMessage30316364 setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public long getDataOffset() {
        return dataOffset;
    }

    public BuiltinMessage30316364 setDataOffset(long dataOffset) {
        this.dataOffset = dataOffset;
        return this;
    }

    public long getDataLength() {
        return dataLength;
    }

    public BuiltinMessage30316364 setDataLength(long dataLength) {
        this.dataLength = dataLength;
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public BuiltinMessage30316364 setData(byte[] data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "BuiltinMsg30316364Alias{"
               + "fileName='" + fileName + '\''
               + ", dataOffset=" + dataOffset
               + ", dataLength=" + dataLength
               + '}';
    }
}
