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

import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.type.ByteArrayContainer;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.wrapper.DataWrapper;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Jt808ResponseBody(messageId = 0x8103)
public class BuiltinMessage8103Sample3 {

    @Preset.JtStyle.Byte
    private short parameterCount;

    @Preset.JtStyle.List
    private List<ParameterItem> parameterItemList;

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class ParameterItem {

        @Preset.JtStyle.Dword
        private long parameterId;

        @Preset.JtStyle.RuntimeType(prependLengthFieldType = PrependLengthFieldType.u8)
        private Object parameterValue;

        public ParameterItem() {
        }

        public ParameterItem(long parameterId, Object parameterValue) {
            this.parameterId = parameterId;
            this.parameterValue = parameterValue;
        }

        public ParameterItem(long parameterId, DataWrapper<?> parameterValue) {
            this.parameterId = parameterId;
            this.parameterValue = parameterValue;
        }

        public ParameterItem(long parameterId, ByteArrayContainer container) {
            this.parameterId = parameterId;
            this.parameterValue = container;
        }
    }
}
