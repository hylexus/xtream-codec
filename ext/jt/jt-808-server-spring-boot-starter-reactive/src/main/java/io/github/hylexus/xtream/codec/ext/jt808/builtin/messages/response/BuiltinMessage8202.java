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

import io.github.hylexus.xtream.codec.core.annotation.Expression;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.StringJoiner;

/**
 * 临时位置跟踪控制 0x8202
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8202, desc = "临时位置跟踪控制")
public class BuiltinMessage8202 {

    /**
     * 时间间隔;
     * <p>
     * 单位为秒(s)，0则停止跟踪。停止跟踪无需带后继字段
     */
    @Preset.JtStyle.Word(desc = "时间间隔")
    private int timeDurationInSeconds;

    /**
     * 位置跟踪有效期
     * <p>
     * 单位为秒(s) ，终端在接收到位置跟踪控制消息后，
     * 在有效期截止时间之前，依据消息中的时间间隔发
     * 送位置汇报
     */
    // todo 这个 condition 待确认
    // @Preset.JtStyle.Dword(condition = "getTimeDurationInSeconds() > 0", desc = "位置跟踪有效期")
    @Preset.JtStyle.Dword(conditions = @Expression(spel = "getTimeDurationInSeconds() > 0", mvel = "self.getTimeDurationInSeconds() > 0", aviator = "self.timeDurationInSeconds > 0"), desc = "位置跟踪有效期")
    private long traceValidityDurationInSeconds;

    @SuppressWarnings("lombok")
    public int getTimeDurationInSeconds() {
        return timeDurationInSeconds;
    }

    public BuiltinMessage8202 setTimeDurationInSeconds(int timeDurationInSeconds) {
        this.timeDurationInSeconds = timeDurationInSeconds;
        return this;
    }

    public long getTraceValidityDurationInSeconds() {
        return traceValidityDurationInSeconds;
    }

    public BuiltinMessage8202 setTraceValidityDurationInSeconds(long traceValidityDurationInSeconds) {
        this.traceValidityDurationInSeconds = traceValidityDurationInSeconds;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8202.class.getSimpleName() + "[", "]")
                .add("timeDurationInSeconds=" + timeDurationInSeconds)
                .add("traceValidityDurationInSeconds=" + traceValidityDurationInSeconds)
                .toString();
    }
}
