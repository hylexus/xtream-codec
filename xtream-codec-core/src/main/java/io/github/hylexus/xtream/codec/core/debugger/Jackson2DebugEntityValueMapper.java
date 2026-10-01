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

package io.github.hylexus.xtream.codec.core.debugger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Objects;

/**
 * 基于 Jackson 2 的实体值转换适配器，将具体 Jackson 类型限制在此边界。
 * 可以直接复用宿主的命名策略、MixIn 和自定义模块；传入的 mapper 不复制、不修改。
 * 宿主必须在构建适配器之前完成配置，服务使用期间不再修改 mapper，以支持并发调用。
 * 使用此适配器时，宿主需显式提供 Jackson 2 databind 依赖。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public final class Jackson2DebugEntityValueMapper implements DebugEntityValueMapper {

    private static final TypeReference<Map<String, Object>> VALUES_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper mapper;

    /**
     * 使用 Jackson 2 的默认配置，与调试服务原有的默认实体转换行为一致。
     */
    public Jackson2DebugEntityValueMapper() {
        this(new ObjectMapper());
    }

    /**
     * 使用宿主已完成配置的 mapper。
     *
     * @param mapper 实体值转换所用的 Jackson 2 mapper
     */
    public Jackson2DebugEntityValueMapper(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public <T> T toEntity(Map<String, Object> values, Class<T> entityType) {
        return Objects.requireNonNull(mapper.convertValue(values, entityType), "实体转换结果不能为空");
    }

    @Override
    public Map<String, Object> toValues(Object entity) {
        return Objects.requireNonNull(mapper.convertValue(entity, VALUES_TYPE), "字段映射转换结果不能为空");
    }
}
