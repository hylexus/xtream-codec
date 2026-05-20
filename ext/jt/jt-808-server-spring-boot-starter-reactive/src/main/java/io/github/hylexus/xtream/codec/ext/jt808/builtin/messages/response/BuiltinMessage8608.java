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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.core.type.wrapper.DwordWrapper;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.List;
import java.util.StringJoiner;

/**
 * 查询区域或线路数据
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8608, desc = "查询区域或线路数据")
public class BuiltinMessage8608 {

    /**
     * 查询类型
     * <li>1 -- 查询圆形区域数据</li>
     * <li>2 -- 查询矩形区域数据</li>
     * <li>3 -- 查询多边形区域数据</li>
     * <li>4 -- 查询线路数据</li>
     */
    @Preset.JtStyle.Byte(desc = "查询类型")
    private short type;

    /**
     * 要查询的区域或线路的ID数量
     */
    @Preset.JtStyle.Dword(desc = "要查询的区域或线路的ID数量")
    private long count;

    @Preset.JtStyle.List(desc = "ID 列表")
    private List<DwordWrapper> idList;

    public short getType() {
        return type;
    }

    public BuiltinMessage8608 setType(short type) {
        this.type = type;
        return this;
    }

    public long getCount() {
        return count;
    }

    public BuiltinMessage8608 setCount(long count) {
        this.count = count;
        return this;
    }

    public List<DwordWrapper> getIdList() {
        return idList;
    }

    public BuiltinMessage8608 setIdList(List<DwordWrapper> idList) {
        this.idList = idList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8608.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("count=" + count)
                .add("idList=" + idList)
                .toString();
    }
}
