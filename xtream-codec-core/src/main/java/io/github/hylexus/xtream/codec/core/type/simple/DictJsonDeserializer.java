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

package io.github.hylexus.xtream.codec.core.type.simple;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DictJsonDeserializer extends JsonDeserializer<SimpleField.Dict<? extends SimpleField.DictKey>> {

    private static final Map<String, Class<? extends SimpleField.DictKey>> NAME_TO_CLASS = Map.of(
            "i8", SimpleField.I8.class,
            "u8", SimpleField.U8.class,
            "i16", SimpleField.I16.class,
            "u16", SimpleField.U16.class,
            "i32", SimpleField.I32.class,
            "u32", SimpleField.U32.class,
            "i64", SimpleField.I64.class
    );

    private final ObjectMapper objectMapper;

    public DictJsonDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public SimpleField.Dict<? extends SimpleField.DictKey> deserialize(JsonParser p, DeserializationContext context) throws IOException {
        final JsonNode node = p.getCodec().readTree(p);

        // 1. keyType
        final JsonNode keyTypeNode = node.get("keyType");
        if (keyTypeNode == null || keyTypeNode.isNull()) {
            throw new JsonParseException(p, "keyType is required");
        }
        final String keyTypeString = keyTypeNode.asText();
        final Class<? extends SimpleField.DictKey> keyType = NAME_TO_CLASS.get(keyTypeString);
        if (keyType == null) {
            throw new JsonParseException(p, "Invalid keyType: " + keyTypeString + ". Supported: " + NAME_TO_CLASS.keySet());
        }

        // 2. 读取其他字段
        final JsonNode valueLengthTypeNode = node.get("valueLengthType");
        if (valueLengthTypeNode == null || valueLengthTypeNode.isNull()) {
            throw new JsonParseException(p, "valueLengthType is required");
        }
        final String valueLengthTypeStr = valueLengthTypeNode.asText().toLowerCase();
        final SimpleField.KeyLengthType valueLengthType = SimpleField.KeyLengthType.valueOf(valueLengthTypeStr);

        final PrependLengthFieldType prependLengthFieldType = node.has("prependLengthFieldType")
                ? PrependLengthFieldType.valueOf(node.get("prependLengthFieldType").asText())
                : PrependLengthFieldType.none;

        // 3. 手动反序列化 value map
        final JsonNode valueNode = node.get("value");
        final Map<SimpleField.DictKey, SimpleField> valueMap = new LinkedHashMap<>();

        final Set<Map.Entry<String, JsonNode>> properties = valueNode.properties();
        for (Map.Entry<String, JsonNode> entry : properties) {
            final String keyValueStr = entry.getKey();

            // 将字符串 key 转为 DictKey 实例
            final SimpleField.DictKey dictKey = SimpleField.DictKey.from(keyType, keyValueStr);

            // 反序列化 value
            final SimpleField fieldValue = this.objectMapper.treeToValue(entry.getValue(), SimpleField.class);

            valueMap.put(dictKey, fieldValue);
        }

        // 4. 构造 Dict
        return createDict(
                keyType,
                valueLengthType,
                valueMap,
                prependLengthFieldType
        );

    }

    private <K extends SimpleField.DictKey> SimpleField.Dict<K> createDict(
            Class<K> keyType,
            SimpleField.KeyLengthType valueLengthType,
            Map<SimpleField.DictKey, SimpleField> rawValueMap,
            PrependLengthFieldType prependLengthFieldType) {
        @SuppressWarnings("unchecked") final Map<K, SimpleField> typedMap = (Map<K, SimpleField>) rawValueMap;
        return new SimpleField.Dict<>(null, prependLengthFieldType, keyType, valueLengthType, typedMap);
    }
}
