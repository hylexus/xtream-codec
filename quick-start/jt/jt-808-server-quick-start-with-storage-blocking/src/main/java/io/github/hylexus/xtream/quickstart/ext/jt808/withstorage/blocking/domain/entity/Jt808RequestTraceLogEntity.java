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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.entity;

import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.values.Jt808NetType;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.values.Jt808Version;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class Jt808RequestTraceLogEntity {

    /**
     * UUID string
     */
    private String id;

    private String requestId;

    /**
     * 报文接收时间
     */
    private LocalDateTime receivedAt;

    /**
     * 网络类型: TCP/UDP
     */
    // @TypeHandler(Jt808NetTypeHandlerAdapter.class)
    private Jt808NetType netType;

    /**
     * @see Jt808Request#traceId()
     */
    private String traceId;

    /**
     * 终端ID
     */
    private String terminalId;

    /**
     * 消息ID
     */
    private int messageId;

    /**
     * 版本号 2013/2019
     */
    // @TypeHandler(Jt808VersionHandlerAdapter.class)
    private Jt808Version version;

    /**
     * 流水号
     */
    private int flowId;

    /**
     * 消息体长度
     */
    private int messageBodyLength;

    /**
     * 消息体属性
     */
    private int messageBodyProperty;

    /**
     * 总包数
     */
    private int totalPackage;

    /**
     * 当前包序号
     */
    private int currentPackageNo;

    /**
     * 是否是子包
     */
    private boolean isSubpackage;

    /**
     * 原始报文对应的十六进制字符串
     */
    private String rawHex;

    /**
     * 转义之后的报文对应的十六进制字符串
     */
    private String escapedHex;

    /**
     * 数据写入时间
     */
    private LocalDateTime createdAt;

}
