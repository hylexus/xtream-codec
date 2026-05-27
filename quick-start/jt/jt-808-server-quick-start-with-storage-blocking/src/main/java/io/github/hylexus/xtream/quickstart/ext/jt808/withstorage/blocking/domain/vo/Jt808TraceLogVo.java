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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class Jt808TraceLogVo {
    private String requestId;
    private String traceId;
    private String terminalId;
    private Jt808NetType netType;
    private int messageId;
    private Jt808Version version;
    private int flowId;
    private int messageBodyLength;
    private int messageBodyProperty;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private LocalDateTime receivedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private LocalDateTime sentAt;
    private boolean isSubpackage;
    private int currentPackageNo;
    private int totalPackage;
    private String requestHex;
    private String requestHexEscaped;

    private String responseHex;

    private String messageDesc;
}
