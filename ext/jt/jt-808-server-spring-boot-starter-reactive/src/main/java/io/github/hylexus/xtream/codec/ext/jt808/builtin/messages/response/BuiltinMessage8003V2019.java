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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.wrapper.WordWrapper;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.List;
import java.util.StringJoiner;

/**
 * 服务器补传分包请求 0x8003
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8003, desc = "服务器补传分包请求(2019)")
public class BuiltinMessage8003V2019 {

    @Preset.JtStyle.Word(desc = "原始消息流水号")
    private int originalMessageFlowId;

    @Preset.JtStyle.Word(desc = "重传包总数")
    private int packageCount;

    @Preset.JtStyle.List(desc = "重传包 ID 列表")
    private List<WordWrapper> packageIdList;

    public int getOriginalMessageFlowId() {
        return originalMessageFlowId;
    }

    public BuiltinMessage8003V2019 setOriginalMessageFlowId(int originalMessageFlowId) {
        this.originalMessageFlowId = originalMessageFlowId;
        return this;
    }

    public int getPackageCount() {
        return packageCount;
    }

    public BuiltinMessage8003V2019 setPackageCount(int packageCount) {
        this.packageCount = packageCount;
        return this;
    }

    public List<WordWrapper> getPackageIdList() {
        return packageIdList;
    }

    public BuiltinMessage8003V2019 setPackageIdList(List<WordWrapper> packageIdList) {
        this.packageIdList = packageIdList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8003V2019.class.getSimpleName() + "[", "]")
                .add("originalMessageFlowId=" + originalMessageFlowId)
                .add("packageCount=" + packageCount)
                .add("packageIdList=" + packageIdList)
                .toString();
    }
}
