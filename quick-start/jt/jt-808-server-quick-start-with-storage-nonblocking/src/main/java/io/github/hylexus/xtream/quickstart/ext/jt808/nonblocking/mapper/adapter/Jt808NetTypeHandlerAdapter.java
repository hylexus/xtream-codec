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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.mapper.adapter;

import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.configuration.r2dbc.DemoR2dbcConfiguration;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.values.Jt808NetType;
import io.r2dbc.spi.Readable;
import io.r2dbc.spi.ReadableMetadata;
import io.r2dbc.spi.Statement;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.parameter.ParameterHandlerContext;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.executor.type.R2dbcTypeHandlerAdapter;

/**
 * @author hylexus
 * @see DemoR2dbcConfiguration#r2dbcMybatisConfigurationCustomizer()
 * @see <a href="https://github.com/chenggangpro/reactive-mybatis-support/wiki/TypeHandlerAssociations">https://github.com/chenggangpro/reactive-mybatis-support/wiki/TypeHandlerAssociations</a>
 */
public class Jt808NetTypeHandlerAdapter implements R2dbcTypeHandlerAdapter<Jt808NetType> {

    @Override
    public Class<Jt808NetType> adaptClazz() {
        return Jt808NetType.class;
    }

    @Override
    public void setParameter(Statement statement,
                             ParameterHandlerContext parameterHandlerContext,
                             Jt808NetType parameter) {
        statement.bind(parameterHandlerContext.getIndex(), parameter.getValue());
    }

    @Override
    public Jt808NetType getResult(Readable readable, ReadableMetadata readableMetadata, String columnName) {
        Integer result = readable.get(columnName, Integer.class);
        return result == null ? null : Jt808NetType.fromValue(result);
    }

    @Override
    public Jt808NetType getResult(Readable readable, ReadableMetadata readableMetadata, int columnIndex) {
        Integer result = readable.get(columnIndex, Integer.class);
        return result == null ? null : Jt808NetType.fromValue(result);
    }

}
