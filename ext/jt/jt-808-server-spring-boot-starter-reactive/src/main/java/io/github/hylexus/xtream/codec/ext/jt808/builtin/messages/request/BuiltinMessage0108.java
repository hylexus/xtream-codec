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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

/**
 * 查询终端参数应答 0x0108
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x0108, desc = "查询终端参数应答")
public class BuiltinMessage0108 {

    /**
     * 升级类型
     * <p>
     * 0：终端，12：道路运输证 IC 卡读卡器，52：北斗卫星定位模块
     */
    @Preset.JtStyle.Byte(desc = "升级类型")
    private short type;

    @Preset.JtStyle.Byte(desc = "升级结果 0：成功，1：失败，2：取消")
    private short result;

    public short getType() {
        return type;
    }

    public BuiltinMessage0108 setType(short type) {
        this.type = type;
        return this;
    }

    public short getResult() {
        return result;
    }

    public BuiltinMessage0108 setResult(short result) {
        this.result = result;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage0108.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("result=" + result)
                .toString();
    }
}
