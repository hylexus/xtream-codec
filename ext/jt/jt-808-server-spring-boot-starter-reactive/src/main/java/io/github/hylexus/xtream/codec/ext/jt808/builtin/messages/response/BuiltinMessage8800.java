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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.hylexus.xtream.codec.core.annotation.Expression;
import io.github.hylexus.xtream.codec.core.jackson.XtreamCodecDebugJsonSerializer;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * 多媒体数据上传应答
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8800, desc = "多媒体数据上传应答")
public class BuiltinMessage8800 {
    /**
     * 多媒体 ID
     * <p>
     * > 0，如收到全部数据包则没有后续字段
     */
    @Preset.JtStyle.Dword(desc = "多媒体 ID")
    private long multimediaId;

    @Preset.JtStyle.Byte(desc = "重传包总数")
    private short retransmittedPackageCount;

    /**
     * 重传包 ID 列表
     * <p>
     * 重传包序号顺序排列，如“包 ID1 包 ID2......包IDn”。
     */
    // @Preset.JtStyle.Bytes(lengthExpression = "2 * getRetransmittedPackageCount()", desc = "重传包 ID 列表")
    @Preset.JtStyle.Bytes(lengthExpressions = @Expression(spel = "2 * getRetransmittedPackageCount()", mvel = "2 * self.getRetransmittedPackageCount()", aviator = "2 * self.retransmittedPackageCount"), desc = "重传包 ID 列表")
    @JsonSerialize(using = XtreamCodecDebugJsonSerializer.class)
    private byte[] retransmittedPackageIdList;

    @SuppressWarnings("lombok")
    public short getRetransmittedPackageCount() {
        return retransmittedPackageCount;
    }

    public long getMultimediaId() {
        return multimediaId;
    }

    public BuiltinMessage8800 setMultimediaId(long multimediaId) {
        this.multimediaId = multimediaId;
        return this;
    }

    public BuiltinMessage8800 setRetransmittedPackageCount(short retransmittedPackageCount) {
        this.retransmittedPackageCount = retransmittedPackageCount;
        return this;
    }

    public byte[] getRetransmittedPackageIdList() {
        return retransmittedPackageIdList;
    }

    public BuiltinMessage8800 setRetransmittedPackageIdList(byte[] retransmittedPackageIdList) {
        this.retransmittedPackageIdList = retransmittedPackageIdList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8800.class.getSimpleName() + "[", "]")
                .add("multimediaId=" + multimediaId)
                .add("retransmittedPackageCount=" + retransmittedPackageCount)
                .add("retransmittedPackageIdList=" + Arrays.toString(retransmittedPackageIdList))
                .toString();
    }
}
