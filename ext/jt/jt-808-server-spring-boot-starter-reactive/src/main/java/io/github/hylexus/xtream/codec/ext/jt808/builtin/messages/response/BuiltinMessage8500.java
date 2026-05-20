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
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

/**
 * 车辆控制
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8500, desc = "车辆控制")
public class BuiltinMessage8500 {

    /**
     * 控制标志
     * <li>bit[0] -- 0：车门解锁；1：车门加锁</li>
     * <li>bit[1~7] -- 保留</li>
     */
    @Preset.JtStyle.Byte(desc = "控制标志")
    private short type;

    public short getType() {
        return type;
    }

    public BuiltinMessage8500 setType(short type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8500.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .toString();
    }
}
