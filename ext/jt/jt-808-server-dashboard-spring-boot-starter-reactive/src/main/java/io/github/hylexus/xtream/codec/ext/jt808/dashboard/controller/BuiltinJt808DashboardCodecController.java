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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.controller;

import io.github.hylexus.xtream.codec.base.web.domain.vo.PageableVo;
import io.github.hylexus.xtream.codec.base.web.exception.XtreamHttpException;
import io.github.hylexus.xtream.codec.common.bean.BeanDescriptor;
import io.github.hylexus.xtream.codec.core.BeanMetadataRegistry;
import io.github.hylexus.xtream.codec.core.FieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.annotation.NumberEndian;
import io.github.hylexus.xtream.codec.core.annotation.NumberSignedness;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto.BeanMetadataDto;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto.CodecMetadataDto;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto.DecodeMessageDto;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto.EncodeMessageDto;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.values.SimpleTypes;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.vo.DecodedMessageVo;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.service.Jt808DashboardCodecService;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageDescriber;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@RestController
@RequestMapping("/dashboard-api/jt808/v1/codec")
public class BuiltinJt808DashboardCodecController {

    private final Jt808DashboardCodecService codecService;
    private final BeanMetadataRegistry beanMetadataRegistry;

    public BuiltinJt808DashboardCodecController(Jt808DashboardCodecService codecService, BeanMetadataRegistry beanMetadataRegistry) {
        this.codecService = codecService;
        this.beanMetadataRegistry = beanMetadataRegistry;
    }

    @GetMapping("/codec-options")
    public SimpleTypes.CodecDebugOptions codecOptions() {
        return this.codecService.getCodecOptions();
    }

    @PostMapping("/encode-with-entity")
    public List<Jt808MessageDescriber.Tracker> encode(@Validated @RequestBody EncodeMessageDto dto) {
        dto.setTerminalId(this.convertTerminalId(dto.getTerminalId(), dto.getVersion()));
        return this.codecService.encodeWithTracker(dto);
    }

    @GetMapping("/codec-metadata")
    public PageableVo<FieldCodecRegistry.CodecDescriptor> codecDescriptors(@Validated CodecMetadataDto dto) {
        final FieldCodecRegistry codecRegistry = this.beanMetadataRegistry.getFieldCodecRegistry();

        final Predicate<FieldCodecRegistry.CodecDescriptor> filter = createFilter(dto);
        final long total = codecRegistry.descriptors().filter(filter).count();
        if (total <= 0) {
            return PageableVo.empty();
        }

        final List<FieldCodecRegistry.CodecDescriptor> data = codecRegistry.descriptors()
                .filter(filter)
                .skip((dto.getOffset()))
                .limit(dto.getPageSize())
                .toList();
        return PageableVo.of(total, data);
    }

    static Predicate<FieldCodecRegistry.CodecDescriptor> createFilter(CodecMetadataDto dto) {
        Predicate<FieldCodecRegistry.CodecDescriptor> filter = x -> true;
        final String key = dto.getKey();
        if (StringUtils.hasText(key)) {
            filter = filter.and(x -> x.key().toLowerCase().contains(key.toLowerCase().trim()));
        }
        final String className = dto.getClassName();
        if (StringUtils.hasText(className)) {
            filter = filter.and(x -> x.rawClassName().toLowerCase().contains(className.toLowerCase().trim()));
        }
        final String charset = dto.getCharset();
        if (StringUtils.hasText(charset)) {
            filter = filter.and(x -> x.charset().toLowerCase().contains(charset.toLowerCase().trim()));
        }
        final NumberSignedness signedness = dto.getSignedness();
        if (signedness != null) {
            filter = filter.and(x -> x.signedness() == signedness);
        }
        final NumberEndian endian = dto.getEndian();
        if (endian != null) {
            filter = filter.and(x -> x.endian() == endian);
        }
        final Boolean builtin = dto.getBuiltin();
        if (builtin != null) {
            filter = filter.and(x -> x.isBuiltin() == builtin);
        }
        return filter;
    }

    @GetMapping("/bean-metadata")
    public PageableVo<BeanDescriptor> beanMetadata(@Validated BeanMetadataDto dto) {
        final Predicate<BeanDescriptor> filter = createBeanFilter(dto);
        final long total = this.beanMetadataRegistry.beanDescriptors().filter(filter).count();
        if (total <= 0) {
            return PageableVo.empty();
        }
        final List<BeanDescriptor> data = this.beanMetadataRegistry.beanDescriptors()
                .filter(filter)
                .skip((dto.getOffset()))
                .limit(dto.getPageSize())
                .toList();
        return PageableVo.of(total, data);
    }

    static Predicate<BeanDescriptor> createBeanFilter(BeanMetadataDto dto) {
        Predicate<BeanDescriptor> filter = x -> true;
        final String className = dto.getClassName();
        if (StringUtils.hasText(className)) {
            filter = filter.and(x -> x.rawClass().toLowerCase().contains(className.toLowerCase().trim()));
        }
        final Integer version = dto.getVersion();
        if (version != null) {
            filter = filter.and(x -> x.properties().stream().anyMatch(p -> p.getVersionValue() == version));
        }
        final String dataType = dto.getDataType();
        if (StringUtils.hasText(dataType)) {
            filter = filter.and(x -> x.properties().stream().anyMatch(p -> p.getDataTypeName().equalsIgnoreCase(dataType.trim())));
        }
        return filter;
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
    public DecodedMessageVo decode(@Validated @RequestBody DecodeMessageDto dto) {
        final List<String> list = dto.getHexString().stream()
                .filter(Objects::nonNull)
                .map(s -> s.replace(" ", ""))
                .filter(StringUtils::hasText)
                .toList();
        if (list.isEmpty()) {
            throw XtreamHttpException.badRequest("hexString is invalid");
        }
        dto.setHexString(list);

        return this.codecService.decodeWithTracker(dto);
    }

}
