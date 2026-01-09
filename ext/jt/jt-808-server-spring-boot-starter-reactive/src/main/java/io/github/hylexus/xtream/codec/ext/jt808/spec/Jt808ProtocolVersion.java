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

package io.github.hylexus.xtream.codec.ext.jt808.spec;

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;

public enum Jt808ProtocolVersion {
    /**
     * 自动检测2019版||2013版
     */
    AUTO_DETECTION((byte) -1, (byte) -2, -2, "ALL"),
    /**
     * 2011 版
     */
    VERSION_2011((byte) 0, (byte) -1, 2011, "2011"),
    /**
     * 2013 版
     */
    VERSION_2013((byte) 0, (byte) 0, 2013, "2013"),
    /**
     * 2019 版
     */
    VERSION_2019((byte) 1, (byte) 1, 2019, "2019"),
    ;

    /**
     * 消息体属性中 第14位
     */
    private final byte versionBit;

    /**
     * 自定义属性，版本对应的数字标识。
     * <p>
     * 协议中规定的 {@link #versionBit} 无法区分 2011 和 2013 两个版本。所以才有这个自定义属性。
     */
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    private final byte versionIdentifier;

    /**
     * @see XtreamField#version()
     */
    private final int versionValue;

    /**
     * 字符串描述
     */
    private final String shortDesc;

    Jt808ProtocolVersion(byte versionBit, byte versionIdentifier, int versionValue, String shortDesc) {
        this.versionValue = versionValue;
        this.shortDesc = shortDesc;
        this.versionBit = versionBit;
        this.versionIdentifier = versionIdentifier;
    }

    // getters
    public byte versionBit() {
        return versionBit;
    }

    public String shortDesc() {
        return shortDesc;
    }

    /**
     * @deprecated Use {@link #versionValue()} instead.
     */
    @Deprecated(forRemoval = true, since = "0.1.0")
    public byte versionIdentifier() {
        return versionIdentifier;
    }

    public int versionValue() {
        return versionValue;
    }

}
