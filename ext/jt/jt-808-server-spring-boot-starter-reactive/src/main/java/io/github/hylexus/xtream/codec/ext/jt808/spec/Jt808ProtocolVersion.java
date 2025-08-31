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

public enum Jt808ProtocolVersion {
    /**
     * 自动检测2019版||2013版
     */
    AUTO_DETECTION("ALL", (byte) -1, (byte) -2),
    /**
     * 2011 版
     */
    VERSION_2011("2011", (byte) 0, (byte) -1),
    /**
     * 2013 版
     */
    VERSION_2013("2013", (byte) 0, (byte) 0),
    /**
     * 2019 版
     */
    VERSION_2019("2019", (byte) 1, (byte) 1),
    ;

    /**
     * 消息体属性中 第14位
     */
    private final byte versionBit;
    /**
     * 字符串描述
     */
    private final String shortDesc;

    /**
     * 自定义属性，版本对应的数字标识。
     * <p>
     * 协议中规定的 {@link #versionBit} 无法区分 2011 和 2013 两个版本。所以才有这个自定义属性。
     */
    private final byte versionIdentifier;

    Jt808ProtocolVersion(String shortDesc, byte versionBit, byte versionIdentifier) {
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

    public byte versionIdentifier() {
        return versionIdentifier;
    }
}
