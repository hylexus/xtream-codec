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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request;

import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.List;
import java.util.StringJoiner;

/**
 * 定位数据批量上传
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x0704, desc = "定位数据批量上传")
public class BuiltinMessage0704 {

    /**
     * 数据项个数
     */
    @Preset.JtStyle.Word(desc = "数据项个数")
    private int count;

    /**
     * 位置数据类型
     * <li>0：正常位置批量汇报</li>
     * <li>1：盲区补报</li>
     */
    @Preset.JtStyle.Byte(desc = "位置数据类型")
    private short type;

    @Preset.JtStyle.List(desc = "数据项列表")
    private List<Item> itemList;

    public int getCount() {
        return count;
    }

    public BuiltinMessage0704 setCount(int count) {
        this.count = count;
        return this;
    }

    public short getType() {
        return type;
    }

    public BuiltinMessage0704 setType(short type) {
        this.type = type;
        return this;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public BuiltinMessage0704 setItemList(List<Item> itemList) {
        this.itemList = itemList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage0704.class.getSimpleName() + "[", "]")
                .add("count=" + count)
                .add("type=" + type)
                .add("itemList=" + itemList)
                .toString();
    }

    public static class Item {
        // /**
        //  * 位置汇报数据体长度
        //  * <li>0：正常位置批量汇报</li>
        //  * <li>1：盲区补报</li>
        //  */
        // // @Preset.JtStyle.Word
        // private int locationDataLength;

        /**
         * 位置汇报数据体
         */
        // @Preset.JtStyle.Object(lengthExpression = "getLocationDataLength()")
        // prependLengthFieldType: 前置一个 u16 类型的字段表示当前字段的长度
        @Preset.JtStyle.Object(prependLengthFieldType = PrependLengthFieldType.u16, desc = "位置汇报数据体")
        private BuiltinMessage0200 locationData;

        public BuiltinMessage0200 getLocationData() {
            return locationData;
        }

        public Item setLocationData(BuiltinMessage0200 locationData) {
            this.locationData = locationData;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Item.class.getSimpleName() + "[", "]")
                    .add("locationData=" + locationData)
                    .toString();
        }
    }

}
