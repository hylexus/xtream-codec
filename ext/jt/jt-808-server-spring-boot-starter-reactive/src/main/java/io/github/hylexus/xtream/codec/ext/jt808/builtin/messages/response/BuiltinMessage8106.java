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
 * 查询终端参数
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8106, desc = "查询终端参数")
public class BuiltinMessage8106 {

    @Preset.JtStyle.Byte(desc = "参数总数")
    private short parameterCount;

    @Preset.JtStyle.List(desc = "参数 ID 列表")
    private List<DwordWrapper> parameterIdList;

    public short getParameterCount() {
        return parameterCount;
    }

    public BuiltinMessage8106 setParameterCount(short parameterCount) {
        this.parameterCount = parameterCount;
        return this;
    }

    public List<DwordWrapper> getParameterIdList() {
        return parameterIdList;
    }

    public BuiltinMessage8106 setParameterIdList(List<DwordWrapper> parameterIdList) {
        this.parameterIdList = parameterIdList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8106.class.getSimpleName() + "[", "]")
                .add("parameterCount=" + parameterCount)
                .add("parameterIdList=" + parameterIdList)
                .toString();
    }
}
