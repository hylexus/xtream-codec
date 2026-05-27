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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.entity;

import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.values.Jt808NetType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class Jt808ResponseTraceLogEntity {

    /**
     * UUID string
     */
    private String id;

    private String requestId;

    /**
     * 报文发送时间
     */
    private LocalDateTime sentAt;

    /**
     * 网络类型: TCP/UDP
     */
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
     * 转义之后的报文对应的十六进制字符串
     */
    private String escapedHex;

    /**
     * 数据写入时间
     */
    private LocalDateTime createdAt;

}
