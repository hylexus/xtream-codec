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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location;

import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 0x12
 * <p>
 * 进出区域/路线报警附加信息见表29
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class LocationItem0x12 {
    /**
     * <li>1：圆形区域</li>
     * <li>2：矩形区域</li>
     * <li>3：多边形区域</li>
     * <li>4：路线</li>
     */
    @Preset.JtStyle.Byte(desc = "位置类型")
    private short locationType;

    @Preset.JtStyle.Dword(desc = "区域或路段ID")
    private long areaId;

    @Preset.JtStyle.Byte(desc = "方向；0:进; 1:出")
    private short direction;

}
