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
import io.github.hylexus.xtream.codec.core.type.wrapper.DwordWrapper;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.List;
import java.util.StringJoiner;

/**
 * 删除圆形区域
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8601, desc = "删除圆形区域")
public class BuiltinMessage8601 {

    @Preset.JtStyle.Byte(desc = "区域数")
    private short areaCount;

    @Preset.JtStyle.List(desc = "区域 ID 列表")
    private List<DwordWrapper> areaIdList;

    public short getAreaCount() {
        return areaCount;
    }

    public BuiltinMessage8601 setAreaCount(short areaCount) {
        this.areaCount = areaCount;
        return this;
    }

    public List<DwordWrapper> getAreaIdList() {
        return areaIdList;
    }

    public BuiltinMessage8601 setAreaIdList(List<DwordWrapper> areaIdList) {
        this.areaIdList = areaIdList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8601.class.getSimpleName() + "[", "]")
                .add("areaCount=" + areaCount)
                .add("areaIdList=" + areaIdList)
                .toString();
    }
}
