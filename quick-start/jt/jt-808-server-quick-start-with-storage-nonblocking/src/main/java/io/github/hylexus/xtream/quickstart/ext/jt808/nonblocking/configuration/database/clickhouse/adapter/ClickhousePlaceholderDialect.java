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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.configuration.database.clickhouse.adapter;

import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.configuration.r2dbc.DemoR2dbcConfiguration;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.placeholder.dialect.NamePlaceholderDialect;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * 这个数据库方言实现来自 <a href="https://github.com/chenggangpro/reactive-mybatis-support/issues/165">https://github.com/chenggangpro/reactive-mybatis-support/issues/165</a>
 *
 * @author <a href="https://github.com/chenggangpro/reactive-mybatis-support/issues/165#issuecomment-2563816570">chenggangpro</a>
 * @see <a href="https://github.com/chenggangpro/reactive-mybatis-support/issues/165">https://github.com/chenggangpro/reactive-mybatis-support/issues/165</a>
 * @see DemoR2dbcConfiguration#r2dbcMybatisConfigurationCustomizer()
 */
public class ClickhousePlaceholderDialect implements NamePlaceholderDialect {
    private final AtomicLong counter = new AtomicLong(0);
    /**
     * The dialect name.
     */
    public static final String DIALECT_NAME = "Clickhouse";

    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\.|[^@:;/$\\d\\w_]");

    @Override
    public String name() {
        return DIALECT_NAME;
    }

    @Override
    public String getMarker() {
        // return ":Ch_";
        return ":Ch_" + this.mask(this.counter.incrementAndGet()) + "_";
    }

    // 移除之后去掉符号位，再次从零开始
    long mask(long value) {
        return value & Integer.MAX_VALUE;
    }

    @Override
    public String propertyNamePostProcess(String propertyName) {
        if (Objects.isNull(propertyName) || propertyName.isEmpty()) {
            return propertyName;
        }
        return PROPERTY_PATTERN.matcher(propertyName).replaceAll("_");
    }

}
