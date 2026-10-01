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

import java.util.Map;

/**
 * 调试服务的实体值转换边界，不依赖具体 JSON 库或版本。
 * 实体模板、编码输入和两个方向的响应值使用同一个转换器；动态模式不调用此接口。
 * 实现必须支持并发调用，返回非空结果，并用运行时异常报告转换失败。
 * 嵌套对象和集合使用 Map/List，标量及空值保持请求与响应的字段值合同。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public interface DebugEntityValueMapper {

    /**
     * 将编码输入转换为目录中已注册的实体类型。
     *
     * @param values     输入字段映射
     * @param entityType 已通过目录解析的实体类型
     * @param <T>        实体类型
     * @return 待编码的实体实例
     */
    <T> T toEntity(Map<String, Object> values, Class<T> entityType);

    /**
     * 将实体转换为响应字段映射；新建实体也通过此入口生成初始值模板。
     *
     * @param entity 实体实例
     * @return 包含嵌套 Map/List 的字段映射
     */
    Map<String, Object> toValues(Object entity);
}
