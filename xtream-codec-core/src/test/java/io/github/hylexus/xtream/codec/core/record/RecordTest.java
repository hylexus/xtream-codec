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

package io.github.hylexus.xtream.codec.core.record;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.tracker.CodecTracker;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RecordTest {

    public record Entity1(
            @Preset.JtStyle.Str(desc = "姓名", prependLengthFieldType = PrependLengthFieldType.u8) String name,
            Integer x,
            @Preset.JtStyle.Word(desc = "年龄") int age,
            @Preset.JtStyle.TransientRecordComponent(nulls = XtreamField.Nulls.AS_NULL) String address,
            @Preset.RustStyle.transient_record_component(nulls = XtreamField.Nulls.AS_EMPTY)
            List<String> add,
            @Preset.JtStyle.Object NestedRecord1 nestedRecord1,
            @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8) String desc) {
        public Entity1(String name, Integer x, int age, String address) {
            this(name, x, age, address, List.of("111"), new NestedRecord1("张三", 100, new NestedRecord2("张三1", 1100, "desc2"), "desc1"), "...");
        }
    }

    public record NestedRecord1(
            @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8)
            String name1,
            @Preset.JtStyle.Word int age1,
            @Preset.JtStyle.Object NestedRecord2 nestedRecord2,
            @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8) String desc1) {
    }

    public record NestedRecord2(
            @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8)
            String name2,
            @Preset.JtStyle.Word int age2,
            @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8) String des2) {
    }

    @Test
    void test1() {
        EntityCodec entityCodec = EntityCodec.DEFAULT;
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        final Entity1 sourceEntity = new Entity1("张三李四王五", 0, 18, "t");
        final CodecTracker tracker = new CodecTracker();
        entityCodec.encode(sourceEntity, buffer, tracker);
        tracker.visit();
        System.out.println(FormatUtils.toHexString(buffer));
        final CodecTracker decodeTracker = new CodecTracker();
        final Entity1 decode = entityCodec.decode(Entity1.class, buffer,decodeTracker);
        System.out.println(sourceEntity);
        System.out.println(decode);
        decodeTracker.visit();
    }

}
