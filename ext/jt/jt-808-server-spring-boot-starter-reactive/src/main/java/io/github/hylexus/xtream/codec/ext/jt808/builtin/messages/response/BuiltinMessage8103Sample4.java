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

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.codec.CustomParameterListFieldCodec;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.codec.ParameterItem;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.List;
import java.util.StringJoiner;

@Jt808ResponseBody(messageId = 0x8103)
public class BuiltinMessage8103Sample4 {

    @Preset.JtStyle.Byte
    private short parameterCount;

    @XtreamField(fieldCodec = CustomParameterListFieldCodec.class)
    private List<ParameterItem> parameterItemList;

    public short getParameterCount() {
        return parameterCount;
    }

    public BuiltinMessage8103Sample4 setParameterCount(short parameterCount) {
        this.parameterCount = parameterCount;
        return this;
    }

    public List<ParameterItem> getParameterItemList() {
        return parameterItemList;
    }

    public BuiltinMessage8103Sample4 setParameterItemList(List<ParameterItem> parameterItemList) {
        this.parameterItemList = parameterItemList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8103Sample4.class.getSimpleName() + "[", "]")
                .add("parameterCount=" + parameterCount)
                .add("parameterItemList=" + parameterItemList)
                .toString();
    }
}
