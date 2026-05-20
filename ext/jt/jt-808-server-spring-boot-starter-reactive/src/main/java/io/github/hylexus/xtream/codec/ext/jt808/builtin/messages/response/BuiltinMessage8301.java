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
 * 事件设置
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8301, desc = "事件设置")
public class BuiltinMessage8301 {
    /**
     * 事件类型
     * <li>0 -- 删除终端现有所有事件，该命令后不带后继字节</li>
     * <li>1 -- 更新事件</li>
     * <li>2 -- 追加事件</li>
     * <li>3 -- 修改事件</li>
     * <li>4 -- 删除特定几项事件，之后事件项中无需带事件内容</li>
     */
    @Preset.JtStyle.Byte(desc = "事件类型")
    private short eventType;

    @Preset.JtStyle.Byte(desc = "设置总数")
    private short eventCount;

    @Preset.JtStyle.List(desc = "事件项列表")
    private List<EventItem> eventItemList;

    public short getEventType() {
        return eventType;
    }

    public BuiltinMessage8301 setEventType(short eventType) {
        this.eventType = eventType;
        return this;
    }

    public short getEventCount() {
        return eventCount;
    }

    public BuiltinMessage8301 setEventCount(short eventCount) {
        this.eventCount = eventCount;
        return this;
    }

    public List<EventItem> getEventItemList() {
        return eventItemList;
    }

    public BuiltinMessage8301 setEventItemList(List<EventItem> eventItemList) {
        this.eventItemList = eventItemList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8301.class.getSimpleName() + "[", "]")
                .add("eventType=" + eventType)
                .add("eventCount=" + eventCount)
                .add("eventItemList=" + eventItemList)
                .toString();
    }

    public static class EventItem {

        @Preset.JtStyle.Byte(desc = "事件 ID")
        private short eventId;

        // prependLengthFieldType: 前置一个 u8类型的字段 表示 事件内容长度
        @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "事件内容(GBK)")
        private String eventContent;

        public EventItem() {
        }

        public EventItem(short eventId, String eventContent) {
            this.eventId = eventId;
            this.eventContent = eventContent;
        }

        public short getEventId() {
            return eventId;
        }

        public EventItem setEventId(short eventId) {
            this.eventId = eventId;
            return this;
        }

        public String getEventContent() {
            return eventContent;
        }

        public EventItem setEventContent(String eventContent) {
            this.eventContent = eventContent;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", EventItem.class.getSimpleName() + "[", "]")
                    .add("eventId=" + eventId)
                    .add("eventContent='" + eventContent + "'")
                    .toString();
        }
    }

}
