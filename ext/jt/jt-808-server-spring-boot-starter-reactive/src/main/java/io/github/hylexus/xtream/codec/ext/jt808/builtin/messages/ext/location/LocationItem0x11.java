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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location;

import io.github.hylexus.xtream.codec.core.annotation.Expression;
import io.github.hylexus.xtream.codec.core.type.Preset;
import org.jspecify.annotations.Nullable;

/**
 * @param locationType 位置类型
 * @param locationId   区域或路段ID; 若位置类型为0，无该字段
 */
public record LocationItem0x11(
        @Preset.JtStyle.Byte short locationType,
        // @JtStyle.Dword(condition = "#locationType != 0") @Nullable Long locationId,
        @Preset.JtStyle.Dword(conditions = @Expression(spel = "locationType != 0", mvel = "self.locationType != 0", aviator = "self.locationType != 0"))
        @Nullable Long locationId) {

    // for Aviator
    @SuppressWarnings("unused")
    public short getLocationType() {
        return locationType;
    }
}
