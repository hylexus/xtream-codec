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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ServerType;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamInbound;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.Instant;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class Jt808SessionVo {
    private String id;
    private String terminalId;
    private Jt808ServerType serverType;
    private Jt808ProtocolVersion protocolVersion;
    private XtreamInbound.Type protocolType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Instant creationTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Instant lastCommunicateTime;
}
