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

package io.github.hylexus.xtream.codec.core.el;

import io.github.hylexus.xtream.codec.BaseEntityCodecTest;
import io.github.hylexus.xtream.codec.core.annotation.Expression;
import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.type.Preset;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExpressionTest extends BaseEntityCodecTest {

    @Test
    void testVersion1() {
        doCodecTest(1, createSourceEntity(1), (sourceEntity, encodedHexString, decodedEntity) -> {
            doCompareCommonProperties(sourceEntity, decodedEntity);
            assertTrue(encodedHexString.contains(hexStringForGbk(sourceEntity.getAddress1().city())));
            assertTrue(encodedHexString.contains(hexStringForGbk(sourceEntity.getAddress2().getCity())));
            assertEquals(sourceEntity.getAge(), decodedEntity.getAge());
            assertEquals(sourceEntity.getAddress1().city(), decodedEntity.getAddress1().city());
            assertEquals(sourceEntity.getAddress2().getCity(), decodedEntity.getAddress2().getCity());
        }, false);

        doCodecTest(1, createSourceEntity(2), (sourceEntity, encodedHexString, decodedEntity) -> {
            doCompareCommonProperties(sourceEntity, decodedEntity);

            assertNotNull(sourceEntity.getAge());
            assertNull(decodedEntity.getAge());

            assertNotNull(sourceEntity.getAddress1().city());
            assertNull(decodedEntity.getAddress1().city());

            assertNotNull(sourceEntity.getAddress2().getCity());
            assertNull(decodedEntity.getAddress2().getCity());
        }, false);
    }

    @Test
    void tetVersion2() {
        doCodecTest(2, createSourceEntity(1), (sourceEntity, encodedHexString, decodedEntity) -> {
            doCompareCommonProperties(sourceEntity, decodedEntity);
            assertTrue(encodedHexString.contains(hexStringForUtf8(sourceEntity.getAddress1().city())));
            assertTrue(encodedHexString.contains(hexStringForUtf8(sourceEntity.getAddress2().getCity())));
            assertEquals(sourceEntity.getAge(), decodedEntity.getAge());
            assertEquals(sourceEntity.getAddress1().city(), decodedEntity.getAddress1().city());
            assertEquals(sourceEntity.getAddress2().getCity(), decodedEntity.getAddress2().getCity());
        }, false);

        doCodecTest(2, createSourceEntity(2), (sourceEntity, encodedHexString, decodedEntity) -> {
            doCompareCommonProperties(sourceEntity, decodedEntity);

            assertNotNull(sourceEntity.getAge());
            assertNull(decodedEntity.getAge());

            assertNotNull(sourceEntity.getAddress1().city());
            assertNull(decodedEntity.getAddress1().city());

            assertNotNull(sourceEntity.getAddress2().getCity());
            assertNull(decodedEntity.getAddress2().getCity());
        }, false);
    }

    @Test
    void testVersion3() {
        doCodecTest(3, createSourceEntity(1), (sourceEntity, encodedHexString, decodedEntity) -> {
            // System.out.println(sourceEntity);
            // System.out.println(encodedHexString);
            // System.out.println(decodedEntity);
            doCompareCommonProperties(sourceEntity, decodedEntity);
            assertTrue(encodedHexString.contains(hexStringForGbk(sourceEntity.getAddress1().city())));
            assertTrue(encodedHexString.contains(hexStringForGbk(sourceEntity.getAddress2().getCity())));
            assertEquals(sourceEntity.getAge(), decodedEntity.getAge());
            assertEquals(sourceEntity.getAddress1().city(), decodedEntity.getAddress1().city());
            assertEquals(sourceEntity.getAddress2().getCity(), decodedEntity.getAddress2().getCity());
        }, false);

        doCodecTest(3, createSourceEntity(2), (sourceEntity, encodedHexString, decodedEntity) -> {
            doCompareCommonProperties(sourceEntity, decodedEntity);

            assertNotNull(sourceEntity.getAge());
            assertNull(decodedEntity.getAge());

            assertNotNull(sourceEntity.getAddress1().city());
            assertNull(decodedEntity.getAddress1().city());

            assertNotNull(sourceEntity.getAddress2().getCity());
            assertNull(decodedEntity.getAddress2().getCity());
        }, false);
    }

    @Test
    void testVersionDefault() {
        doCodecTest(XtreamField.ALL_VERSION, createSourceEntity(1), (sourceEntity, encodedHexString, decodedEntity) -> {
            // System.out.println(sourceEntity);
            // System.out.println(encodedHexString);
            // System.out.println(decodedEntity);
            doCompareCommonProperties(sourceEntity, decodedEntity);

            assertNotNull(sourceEntity.getAge());
            assertNull(decodedEntity.getAge());

            assertNotNull(sourceEntity.getAddress1().city());
            assertNull(decodedEntity.getAddress1().city());

            assertNotNull(sourceEntity.getAddress2().getCity());
            assertNull(decodedEntity.getAddress2().getCity());
        }, false);

        doCodecTest(XtreamField.ALL_VERSION, createSourceEntity(2), (sourceEntity, encodedHexString, decodedEntity) -> {
            doCompareCommonProperties(sourceEntity, decodedEntity);

            assertNotNull(sourceEntity.getAge());
            assertNull(decodedEntity.getAge());

            assertNotNull(sourceEntity.getAddress1().city());
            assertNull(decodedEntity.getAddress1().city());

            assertNotNull(sourceEntity.getAddress2().getCity());
            assertNull(decodedEntity.getAddress2().getCity());
        }, false);
    }

    private void doCompareCommonProperties(User expected, User actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getRemark(), actual.getRemark());
        assertEquals(expected.getAddress1().status1(), actual.getAddress1().status1());
        assertEquals(expected.getAddress1().street(), actual.getAddress1().street());
        assertEquals(expected.getAddress2().getStatus2(), actual.getAddress2().getStatus2());
        assertEquals(expected.getAddress2().getStreet(), actual.getAddress2().getStreet());
    }

    private User createSourceEntity(int status) {
        return new User()
                .setId(111L)
                .setStatus(status)
                .setName("无名氏")
                .setAge(1000)
                .setAddress1(new Address1(status, "火星市", "火星街"))
                .setAddress2(new Address2().setStatus2(status).setCity("地球市").setStreet("地球街"))
                .setRemark("。。。");
    }

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class User {
        @Preset.RustStyle.u32
        private Long id;

        @Preset.RustStyle.i32
        private int status;

        @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u8)
        private String name;

        // @Preset.RustStyle.u16(version = 1, condition = "status == 1")
        @Preset.RustStyle.u16(version = 1, conditions = @Expression(spel = "status == 1", mvel = "self.status == 1", aviator = "self.status == 1"))
        // @Preset.RustStyle.u16(version = 2, condition = "#status == 1")
        @Preset.RustStyle.u16(version = 2, conditions = @Expression(spel = "#status == 1", mvel = "self.status == 1", aviator = "self.status == 1"))
        // @Preset.RustStyle.u16(version = 3, condition = "#self.status == 1")
        @Preset.RustStyle.u16(version = 3, conditions = @Expression(spel = "#self.status == 1", mvel = "self.status == 1", aviator = "self.status == 1"))
        private Integer age;

        @Preset.RustStyle.struct
        private Address1 address1;

        @Preset.RustStyle.struct
        private Address2 address2;

        @Preset.RustStyle.str
        private String remark;
    }

    public record Address1(
            @Preset.RustStyle.i32
            int status1,
            // @Preset.RustStyle.str(version = 1, condition = "status1 == 1", prependLengthFieldType = PrependLengthFieldType.u8, charset = "GBK")
            @Preset.RustStyle.str(version = 1, conditions = @Expression(spel = "status1 == 1", mvel = "self.status1 == 1", aviator = "self.status1 == 1"), prependLengthFieldType = PrependLengthFieldType.u8, charset = "GBK")
            // @Preset.RustStyle.str(version = 2, condition = "#status1 == 1", prependLengthFieldType = PrependLengthFieldType.u8)
            @Preset.RustStyle.str(version = 2, conditions = @Expression(spel = "#status1 == 1", mvel = "self.status1 == 1", aviator = "self.status1 == 1"), prependLengthFieldType = PrependLengthFieldType.u8)
            // @Preset.RustStyle.str(version = 3, condition = "#self.status1 == 1", prependLengthFieldType = PrependLengthFieldType.u8, charset = "GBK")
            @Preset.RustStyle.str(version = 3, conditions = @Expression(spel = "#self.status1 == 1", mvel = "self.status1 == 1", aviator = "self.status1 == 1"), prependLengthFieldType = PrependLengthFieldType.u8, charset = "GBK")
            String city,
            @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u8)
            String street) {

        // for Aviator
        @SuppressWarnings("unused")
        public int getStatus1() {
            return status1;
        }
    }

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class Address2 {
        @Preset.RustStyle.i32
        private int status2;
        // @Preset.RustStyle.str(version = 1, condition = "status2 == 1", prependLengthFieldType = PrependLengthFieldType.u8, charset = "GBK")
        @Preset.RustStyle.str(version = 1, conditions = @Expression(spel = "status2 == 1", mvel = "self.status2 == 1", aviator = "self.status2 == 1"), prependLengthFieldType = PrependLengthFieldType.u8, charset = "GBK")
        // @Preset.RustStyle.str(version = 2, condition = "#status2 == 1", prependLengthFieldType = PrependLengthFieldType.u8)
        @Preset.RustStyle.str(version = 2, conditions = @Expression(spel = "status2 == 1", mvel = "self.status2 == 1", aviator = "self.status2 == 1"), prependLengthFieldType = PrependLengthFieldType.u8)
        // @Preset.RustStyle.str(version = 3, condition = "#self.status2 == 1", prependLengthFieldType = PrependLengthFieldType.u8, charset = "GBK")
        @Preset.RustStyle.str(version = 3, conditions = @Expression(spel = "status2 == 1", mvel = "self.status2 == 1", aviator = "self.status2 == 1"), prependLengthFieldType = PrependLengthFieldType.u8, charset = "GBK")
        private String city;
        @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u8)
        private String street;
    }
}
