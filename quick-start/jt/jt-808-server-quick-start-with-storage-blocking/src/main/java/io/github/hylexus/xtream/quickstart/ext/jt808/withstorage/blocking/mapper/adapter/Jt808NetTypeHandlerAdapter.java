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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.mapper.adapter;


import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.values.Jt808NetType;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author hylexus
 */
public class Jt808NetTypeHandlerAdapter implements TypeHandler<Jt808NetType> {

    @Override
    public void setParameter(PreparedStatement ps, int i, Jt808NetType parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public Jt808NetType getResult(ResultSet rs, String columnName) throws SQLException {
        Integer result = rs.getObject(columnName, Integer.class);
        return result == null ? null : Jt808NetType.fromValue(result);
    }

    @Override
    public Jt808NetType getResult(ResultSet rs, int columnIndex) throws SQLException {
        Integer result = rs.getObject(columnIndex, Integer.class);
        return result == null ? null : Jt808NetType.fromValue(result);
    }

    @Override
    public Jt808NetType getResult(CallableStatement cs, int columnIndex) throws SQLException {
        Integer result = cs.getObject(columnIndex, Integer.class);
        return result == null ? null : Jt808NetType.fromValue(result);
    }

}
