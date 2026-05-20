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

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.StringJoiner;

/**
 * 查询服务器时间应答 0x8004
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8004, desc = "查询服务器时间应答")
public class BuiltinMessage8004 {

    @Preset.JtStyle.BcdDateTime
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime serverSideDateTime;

    public LocalDateTime getServerSideDateTime() {
        return serverSideDateTime;
    }

    public BuiltinMessage8004 setServerSideDateTime(LocalDateTime serverSideDateTime) {
        this.serverSideDateTime = serverSideDateTime;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8004.class.getSimpleName() + "[", "]")
                .add("serverSideDateTime=" + serverSideDateTime)
                .toString();
    }
}
