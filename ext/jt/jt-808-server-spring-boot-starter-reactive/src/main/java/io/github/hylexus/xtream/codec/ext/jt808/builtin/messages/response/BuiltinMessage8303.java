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

import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.List;
import java.util.StringJoiner;

/**
 * 信息点播菜单设置
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8303, desc = "信息点播菜单设置")
public class BuiltinMessage8303 {
    /**
     * 设置类型
     * <li>0--删除终端全部信息项</li>
     * <li>1--更新菜单</li>
     * <li>2--追加菜单</li>
     * <li>3--修改菜单</li>
     */
    @Preset.JtStyle.Byte(desc = "设置类型")
    private short type;

    @Preset.JtStyle.Byte(desc = "信息项总数")
    private short itemCount;

    @Preset.JtStyle.List(desc = "信息项列表")
    private List<Item> itemList;

    public short getType() {
        return type;
    }

    public BuiltinMessage8303 setType(short type) {
        this.type = type;
        return this;
    }

    public short getItemCount() {
        return itemCount;
    }

    public BuiltinMessage8303 setItemCount(short itemCount) {
        this.itemCount = itemCount;
        return this;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public BuiltinMessage8303 setItemList(List<Item> itemList) {
        this.itemList = itemList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8303.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("itemCount=" + itemCount)
                .add("itemList=" + itemList)
                .toString();
    }

    public static class Item {

        @Preset.JtStyle.Byte(desc = "信息类型")
        private short type;

        // prependLengthFieldType: 前置一个 u16(Word)类型的字段 表示 信息名称长度
        @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u16, desc = "信息名称")
        private String content;

        public Item() {
        }

        public Item(short type, String content) {
            this.type = type;
            this.content = content;
        }

        public short getType() {
            return type;
        }

        public Item setType(short type) {
            this.type = type;
            return this;
        }

        public String getContent() {
            return content;
        }

        public Item setContent(String content) {
            this.content = content;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Item.class.getSimpleName() + "[", "]")
                    .add("type=" + type)
                    .add("content='" + content + "'")
                    .toString();
        }
    }
}
