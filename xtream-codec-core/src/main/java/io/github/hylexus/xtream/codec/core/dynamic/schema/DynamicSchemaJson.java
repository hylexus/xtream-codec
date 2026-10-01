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

package io.github.hylexus.xtream.codec.core.dynamic.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 结构定义的 JSON 交换入口。解析时拒绝未知属性，避免静默忽略拼写错误。
 * 此处只负责格式转换；字段之间的引用、长度单位及类型组合仍由编译器校验。
 * JSON 读写需要宿主提供 Jackson 2 databind；读取 JSON Schema 资源不需要 Jackson 实现。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public final class DynamicSchemaJson {

    private DynamicSchemaJson() {
    }

    /**
     * 读取随核心模块发布的 Draft 2020-12 JSON Schema，供编辑器与外部结构校验工具使用。
     * 跨字段引用、版本覆盖和实际字节边界仍由动态编译器与执行器检查。
     */
    public static String jsonSchema() {
        try (InputStream input = DynamicSchemaJson.class.getResourceAsStream("dynamic-codec-schema.json")) {
            if (input == null) {
                throw new IllegalStateException("动态结构 JSON Schema 资源缺失");
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("无法读取动态结构 JSON Schema", exception);
        }
    }

    public static DynamicSchema read(String json) {
        return JsonSupport.read(json);
    }

    public static String write(DynamicSchema schema) {
        return JsonSupport.write(schema);
    }

    // 首次 JSON 读写时才加载 Jackson 类型，避免读取 Schema 资源触发可选实现的类加载。
    private static final class JsonSupport {
        private static final ObjectMapper MAPPER = new ObjectMapper()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        private static DynamicSchema read(String json) {
            try {
                return MAPPER.readValue(json, DynamicSchema.class);
            } catch (JsonProcessingException exception) {
                throw new IllegalArgumentException("无效的动态结构 JSON", exception);
            }
        }

        private static String write(DynamicSchema schema) {
            try {
                return MAPPER.writeValueAsString(schema);
            } catch (JsonProcessingException exception) {
                throw new IllegalArgumentException("无法序列化动态结构", exception);
            }
        }
    }
}
