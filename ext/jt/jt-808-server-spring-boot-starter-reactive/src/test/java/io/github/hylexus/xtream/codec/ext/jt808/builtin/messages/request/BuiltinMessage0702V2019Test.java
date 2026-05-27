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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage0702V2019Test extends BaseCodecTest {

    LocalDateTime time = LocalDateTime.of(2024, 10, 29, 21, 39, 3);
    LocalDate certificateExpiresDate = LocalDate.of(3024, 10, 29);

    @Test
    void testEncode() {
        final BuiltinMessage0702V2019 entity = new BuiltinMessage0702V2019();
        entity.setStatus((short) 0x01);
        entity.setTime(time);
        entity.setIcCardReadResult((short) 0x00);
        entity.setDriverName("张无忌");
        entity.setProfessionalLicenseNo("11335577992244668800");
        entity.setCertificateAuthorityName("没有名字");
        entity.setCertificateExpiresDate(certificateExpiresDate);
        entity.setDriverIdCardNo("11223344556677889900");

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019, 0x0702);
        assertEquals("7e0702404401000000000139123443290000012410292139030006d5c5cedebcc9313133333535373739393232343436363838303008c3bbd3d0c3fbd7d6302410293131323233333434353536363737383839393030677e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "0702404401000000000139123443290000012410292139030006d5c5cedebcc9313133333535373739393232343436363838303008c3bbd3d0c3fbd7d630241029313132323333343435353636373738383939303067";
        final BuiltinMessage0702V2019 entity = decodeAsEntity(BuiltinMessage0702V2019.class, hex);
        assertEquals(1, entity.getStatus());
        assertEquals(time, entity.getTime());
        assertEquals(0, entity.getIcCardReadResult());
        assertEquals("张无忌", entity.getDriverName());
        assertEquals("11335577992244668800", entity.getProfessionalLicenseNo());
        assertEquals("没有名字", entity.getCertificateAuthorityName());
        assertEquals(certificateExpiresDate, entity.getCertificateExpiresDate());
        assertEquals("11223344556677889900", entity.getDriverIdCardNo());
    }
}
