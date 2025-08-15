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

package io.github.hylexus.xtream.codec.ext.jt808.spec.impl;

import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageDescriptionRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hylexus
 */
public class DefaultJt808MessageDescriptionRegistry implements Jt808MessageDescriptionRegistry {

    private final Map<Integer, String> mappings = new HashMap<>();

    public DefaultJt808MessageDescriptionRegistry() {
    }

    @Override
    public String getDescription(int messageId) {
        return mappings.get(messageId);
    }

    @Override
    public synchronized void registerDescription(int messageId, String description) {
        mappings.put(messageId, description);
    }

    @Override
    public synchronized void unregisterDescription(int messageId) {
        mappings.remove(messageId);
    }

    public static class BuiltinJt808MessageDescriptionRegistry implements Jt808MessageDescriptionRegistryCustomizer {

        @Override
        public void customize(Jt808MessageDescriptionRegistry registry) {
            registry.registerDescription(0x0003, "终端注销");
            registry.registerDescription(0x0002, "终端心跳");
            registry.registerDescription(0x0001, "终端通用应答");
            registry.registerDescription(0x8001, "平台通用应答");
            registry.registerDescription(0x8003, "服务器端补传分包请求");
            registry.registerDescription(0x0005, "终端补传分包请求");
            registry.registerDescription(0x0100, "终端注册");
            registry.registerDescription(0x8100, "终端注册应答");
            registry.registerDescription(0x0102, "终端鉴权");
            registry.registerDescription(0x0200, "定位数据上报");
            registry.registerDescription(0x0702, "驾驶员身份信息采集上报");
            registry.registerDescription(0x0704, "定位数据批量上报");
            registry.registerDescription(0x0104, "终端参数查询结果");
            registry.registerDescription(0x0800, "多媒体事件信息上传");
            registry.registerDescription(0x8600, "设置圆形区域");
            registry.registerDescription(0x8601, "删除圆形区域");
            registry.registerDescription(0x8602, "设置矩形区域");
            registry.registerDescription(0x8603, "删除矩形区域");
            registry.registerDescription(0x8604, "设置多边形区域");
            registry.registerDescription(0x8605, "删除多边形区域");
            registry.registerDescription(0x8300, "文本信息下发");
            registry.registerDescription(0x8103, "设置终端参数");
            registry.registerDescription(0x9101, "实时音视频传输请求");
            registry.registerDescription(0x9102, "实时音视频传输控制");

            registry.registerDescription(0x1210, "报警附件信息消息");
            registry.registerDescription(0x1211, "文件信息上传");
            registry.registerDescription(0x1212, "文件上传完成消息");
            registry.registerDescription(0x30316364, "附件上传消息");

            registry.registerDescription(0x9208, "报警附件上传指令");
            registry.registerDescription(0x9212, "文件上传完成消息应答");
        }

        @Override
        public int order() {
            // 最高优先级: 内置的先执行，用户自定义的可以覆盖内置的
            return HIGHEST_PRECEDENCE;
        }

    }
}
