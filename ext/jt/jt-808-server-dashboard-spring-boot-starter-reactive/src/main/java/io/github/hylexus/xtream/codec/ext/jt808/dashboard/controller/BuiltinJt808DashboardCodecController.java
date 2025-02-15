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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.controller;

import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto.DecodeMessageDto;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto.EncodeMessageDto;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.SimpleTypes;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.vo.DecodedMessageVo;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.vo.EncodedMessageVo;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardCodecService;
import io.github.hylexus.xtream.codec.ext.jt808.exception.BadRequestException;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/dashboard-api/v1/codec")
public class BuiltinJt808DashboardCodecController {

    private final Jt808DashboardCodecService codecService;

    public BuiltinJt808DashboardCodecController(Jt808DashboardCodecService codecService) {
        this.codecService = codecService;
    }

    @GetMapping("/codec-options")
    public SimpleTypes.CodecDebugOptions codecOptions() {
        return this.codecService.getCodecOptions();
    }

    @PostMapping("/encode-with-entity")
    public List<DecodedMessageVo> encode(@Validated @RequestBody EncodeMessageDto dto) {
        dto.setTerminalId(this.convertTerminalId(dto.getTerminalId(), dto.getVersion()));
        return this.codecService.encodeWithTracker(dto);
    }

    private String convertTerminalId(String original, Jt808ProtocolVersion version) {
        if (version == Jt808ProtocolVersion.VERSION_2019) {
            if (original.length() == 20) {
                return original;
            } else if (original.length() < 20) {
                return original + "0".repeat(20 - original.length());
            } else {
                return original.substring(original.length() - 20);
            }
        }
        if (original.length() == 12) {
            return original;
        } else if (original.length() < 12) {
            return original + "0".repeat(12 - original.length());
        } else {
            return original.substring(original.length() - 12);
        }
    }

    @PostMapping("/decode-with-entity")
    public EncodedMessageVo decode(@Validated @RequestBody DecodeMessageDto dto) {
        final List<String> list = dto.getHexString().stream()
                .filter(Objects::nonNull)
                .map(s -> s.replace(" ", ""))
                .filter(StringUtils::hasText)
                .toList();
        if (list.isEmpty()) {
            throw new BadRequestException("hexString is invalid");
        }
        dto.setHexString(list);

        return this.codecService.decodeWithTracker(dto);
    }

}
