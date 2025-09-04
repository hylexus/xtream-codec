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

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BuiltinMessage0702AllInOneTest extends BaseCodecTest {
    final LocalDateTime time = LocalDateTime.of(2024, 10, 29, 21, 39, 3);
    final LocalDate certificateExpiresDate = LocalDate.of(5024, 10, 1);

    @Test
    void testV2011() {
        final BuiltinMessage0702AllInOne sourceEntity = createEntityV2011();
        final String hex = this.encode(sourceEntity, Jt808ProtocolVersion.VERSION_2011, terminalId2011);

        final BuiltinMessage0702AllInOne decodedEntity1 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2011, BuiltinMessage0702AllInOne.class, hex);
        doCompareV2011(sourceEntity, decodedEntity1);

        final BuiltinMessage0702AllInOne decodedEntity2 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2011, BuiltinMessage0702AllInOne.class, hex);
        doCompareV2011(sourceEntity, decodedEntity2);
    }

    @Test
    void testV2013() {
        final BuiltinMessage0702AllInOne sourceEntity = createEntityV2013();
        final String hex = this.encode(sourceEntity, Jt808ProtocolVersion.VERSION_2013, terminalId2013);
        final BuiltinMessage0702AllInOne decodeEntity1 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2013, BuiltinMessage0702AllInOne.class, hex);
        doCompareV2013(sourceEntity, decodeEntity1);

        final BuiltinMessage0702AllInOne decodeEntity2 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2013, BuiltinMessage0702AllInOne.class, hex);
        doCompareV2013(sourceEntity, decodeEntity2);
    }

    @Test
    void testV2019() {
        final BuiltinMessage0702AllInOne sourceEntity = createEntityV2019();
        final String hex = this.encode(sourceEntity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);

        final BuiltinMessage0702AllInOne decodeEntity1 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2019, BuiltinMessage0702AllInOne.class, hex);
        doCompareV2019(sourceEntity,decodeEntity1);

        final BuiltinMessage0702AllInOne decodeEntity2 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2019, BuiltinMessage0702AllInOne.class, hex);
        doCompareV2019(sourceEntity, decodeEntity2);
    }

    private void doCompareV2019(BuiltinMessage0702AllInOne sourceEntity, BuiltinMessage0702AllInOne decodeEntity) {
        assertEquals(sourceEntity.getStatus(), decodeEntity.getStatus());
        assertEquals(sourceEntity.getTime(), decodeEntity.getTime());
        assertEquals(sourceEntity.getIcCardReadResult(), decodeEntity.getIcCardReadResult());
        assertEquals(sourceEntity.getDriverName(), decodeEntity.getDriverName());
        assertEquals(sourceEntity.getProfessionalLicenseNo(), decodeEntity.getProfessionalLicenseNo());
        assertEquals(sourceEntity.getCertificateAuthorityName(), decodeEntity.getCertificateAuthorityName());
        assertEquals(sourceEntity.getCertificateExpiresDate(), decodeEntity.getCertificateExpiresDate());
        assertEquals(sourceEntity.getDriverIdCardNoV2019(), decodeEntity.getDriverIdCardNoV2019());
        assertNull(sourceEntity.getDriverIdCardNo());
    }

    private BuiltinMessage0702AllInOne createEntityV2019() {
        return new BuiltinMessage0702AllInOne()
                .setStatus((short) 1)
                .setTime(time)
                .setIcCardReadResult((short) 1)
                .setDriverName("王五")
                .setProfessionalLicenseNo("12345678901234567890")
                .setCertificateAuthorityName("不知道名字啊")
                .setCertificateExpiresDate(certificateExpiresDate)
                .setDriverIdCardNoV2019("12345678901234567890")
                ;
    }

    private void doCompareV2013(BuiltinMessage0702AllInOne sourceEntity, BuiltinMessage0702AllInOne targetEntity) {
        assertEquals(sourceEntity.getDriverName(), targetEntity.getDriverName());
        assertEquals(sourceEntity.getDriverIdCardNoV2019(), targetEntity.getDriverIdCardNoV2019());
        assertEquals(sourceEntity.getProfessionalLicenseNo(), targetEntity.getProfessionalLicenseNo());
        assertEquals(sourceEntity.getCertificateAuthorityName(), targetEntity.getCertificateAuthorityName());
    }

    private BuiltinMessage0702AllInOne createEntityV2013() {
        return new BuiltinMessage0702AllInOne()
                .setStatus((short) 1)
                .setTime(time)
                .setIcCardReadResult((short) 0)
                .setDriverName("李四")
                .setProfessionalLicenseNo("12345678901234567890")
                .setCertificateAuthorityName("没有名字哈哈哈")
                .setCertificateExpiresDate(certificateExpiresDate)
                ;
    }

    private static void doCompareV2011(BuiltinMessage0702AllInOne sourceEntity, BuiltinMessage0702AllInOne targetEntity) {
        assertEquals(sourceEntity.getDriverName(), targetEntity.getDriverName());
        assertEquals(sourceEntity.getDriverIdCardNo(), targetEntity.getDriverIdCardNo());
        assertEquals(sourceEntity.getProfessionalLicenseNo(), targetEntity.getProfessionalLicenseNo());
        assertEquals(sourceEntity.getCertificateAuthorityName(), targetEntity.getCertificateAuthorityName());
        assertNull(sourceEntity.getStatus());
        assertNull(sourceEntity.getTime());
        assertNull(sourceEntity.getIcCardReadResult());
        assertNull(sourceEntity.getCertificateExpiresDate());
        assertNull(sourceEntity.getDriverIdCardNoV2019());
    }

    static BuiltinMessage0702AllInOne createEntityV2011() {
        return new BuiltinMessage0702AllInOne()
                .setDriverName("张三")
                .setDriverIdCardNo("12345678901234567890")
                .setProfessionalLicenseNo("9876543210987654321098765432109876543210")
                .setCertificateAuthorityName("没有名字")
                ;
    }
}
