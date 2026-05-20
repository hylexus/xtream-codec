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

import io.github.hylexus.xtream.codec.core.type.ByteArrayContainer;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.List;
import java.util.StringJoiner;

@Jt808ResponseBody(messageId = 0x8103)
public class BuiltinMessage8103Sample1 {

    @Preset.JtStyle.Byte
    private short parameterCount;

    @Preset.JtStyle.List
    private List<ParameterItem> parameterItemList;

    public short getParameterCount() {
        return parameterCount;
    }

    public BuiltinMessage8103Sample1 setParameterCount(short parameterCount) {
        this.parameterCount = parameterCount;
        return this;
    }

    public List<ParameterItem> getParameterItemList() {
        return parameterItemList;
    }

    public BuiltinMessage8103Sample1 setParameterItemList(List<ParameterItem> parameterItemList) {
        this.parameterItemList = parameterItemList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8103Sample1.class.getSimpleName() + "[", "]")
                .add("parameterCount=" + parameterCount)
                .add("parameterItemList=" + parameterItemList)
                .toString();
    }

    public static class ParameterItem {

        @Preset.JtStyle.Dword
        private long parameterId;

        @Preset.JtStyle.Byte
        private short parameterLength;

        @Preset.JtStyle.Bytes
        private ByteArrayContainer parameterValue;

        public ParameterItem() {
        }

        public ParameterItem(long parameterId, ByteArrayContainer container) {
            this.parameterId = parameterId;
            this.parameterLength = (short) container.length();
            this.parameterValue = container;
        }

        public long getParameterId() {
            return parameterId;
        }

        public ParameterItem setParameterId(long parameterId) {
            this.parameterId = parameterId;
            return this;
        }

        public short getParameterLength() {
            return parameterLength;
        }

        public ParameterItem setParameterLength(short parameterLength) {
            this.parameterLength = parameterLength;
            return this;
        }

        public ByteArrayContainer getParameterValue() {
            return parameterValue;
        }

        public ParameterItem setParameterValue(ByteArrayContainer parameterValue) {
            this.parameterValue = parameterValue;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ParameterItem.class.getSimpleName() + "[", "]")
                    .add("parameterId=" + parameterId)
                    .add("parameterLength=" + parameterLength)
                    .add("parameterValue=" + parameterValue)
                    .toString();
        }
    }
}
