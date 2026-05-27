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

package io.github.hylexus.xtream.codec.core.impl.codec.tlv;

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.ext.LengthFieldType;
import io.github.hylexus.xtream.codec.core.type.TLV;
import io.github.hylexus.xtream.codec.core.type.simple.DataField;
import io.github.hylexus.xtream.codec.core.type.wrapper.DwordWrapper;
import io.github.hylexus.xtream.codec.core.type.wrapper.StringWrapperBcd;
import io.github.hylexus.xtream.codec.core.type.wrapper.U32Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.hylexus.xtream.codec.core.type.simple.DataFields.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TLVSequenceCodecsTest extends BaseEntityCodecTest {
    TLVDebugEntity01 sourceEntity;

    @BeforeEach
    void setUp() {
        sourceEntity = new TLVDebugEntity01()
                .setId(1L)
                .setTlvFields(List.of(
                        new TLV(u16(1), LengthFieldType.u8, gbkString("你是谁")),
                        new TLV(u16(2), LengthFieldType.u8, utf8String("你是谁😂")),
                        new TLV(u16(3), LengthFieldType.u8, new DwordWrapper(222L)),
                        new TLV(u16(4), LengthFieldType.u8, new StringWrapperBcd("1122")),
                        new TLV(u16(4), LengthFieldType.u8, new StringWrapperBcd("3344")),
                        new TLV(u16(5), LengthFieldType.u8, new TLVDebugEntity01.Item01(100L, "张三😄")),
                        new TLV(u16(6), LengthFieldType.u8, hexString("332211"))
                ))
                .setDesc("......😁");
    }

    @Test
    void testDefaultVersion() {
        doCodecTest(XtreamField.ALL_VERSION, sourceEntity, (source, hexString, decoded) -> {
            assertEquals(source.getId(), decoded.getId());
            final List<TLV> sourceTlvFields = source.getTlvFields();
            final List<TLV> targetTlvFields = decoded.getTlvFields();
            assertEquals(((DataField.GbkString) sourceTlvFields.get(0).value()).value(), targetTlvFields.get(0).value());
            assertEquals(((DataField.Utf8String) sourceTlvFields.get(1).value()).value(), targetTlvFields.get(1).value());
            assertEquals(((DwordWrapper) sourceTlvFields.get(2).value()).asU32(), targetTlvFields.get(2).value());
            assertEquals(((StringWrapperBcd) sourceTlvFields.get(3).value()).asBcd(), targetTlvFields.get(3).value());
            assertEquals(((StringWrapperBcd) sourceTlvFields.get(4).value()).asBcd(), targetTlvFields.get(4).value());
            assertEquals(sourceTlvFields.get(5).value(), targetTlvFields.get(5).value());
            assertEquals(((DataField.HexString) sourceTlvFields.get(6).value()).value(), targetTlvFields.get(6).value());

            assertEquals(source.getDesc(), decoded.getDesc());
        }, true);
    }


    @Test
    void testVersion1() {
        doCodecTest(1, sourceEntity, (source, hexString, decoded) -> {
            assertEquals(source.getId(), decoded.getId());
            final List<TLV> sourceTlvFields = source.getTlvFields();
            final List<TLV> targetTlvFields = decoded.getTlvFields();
            assertEquals(((DataField.GbkString) sourceTlvFields.get(0).value()).value(), targetTlvFields.get(0).value());
            assertEquals(((DataField.Utf8String) sourceTlvFields.get(1).value()).value(), targetTlvFields.get(1).value());
            assertEquals(((DwordWrapper) sourceTlvFields.get(2).value()).asU32(), ((U32Wrapper) targetTlvFields.get(2).value()).asU32());
            assertEquals(((StringWrapperBcd) sourceTlvFields.get(3).value()).asBcd(), ((StringWrapperBcd) targetTlvFields.get(3).value()).asBcd());
            assertEquals(((StringWrapperBcd) sourceTlvFields.get(4).value()).asBcd(), ((StringWrapperBcd) targetTlvFields.get(4).value()).asBcd());
            assertEquals(sourceTlvFields.get(5).value(), targetTlvFields.get(5).value());
            assertEquals(((DataField.HexString) sourceTlvFields.get(6).value()).value(), FormatUtils.toHexString((byte[]) targetTlvFields.get(6).value()));
            assertEquals(source.getDesc(), decoded.getDesc());
        }, true);
    }
}
