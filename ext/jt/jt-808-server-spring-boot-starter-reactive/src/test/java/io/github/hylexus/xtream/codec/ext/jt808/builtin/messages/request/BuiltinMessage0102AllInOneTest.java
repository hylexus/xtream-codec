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

import static org.junit.jupiter.api.Assertions.*;

class BuiltinMessage0102AllInOneTest extends BaseCodecTest {

    @Test
    void testV2011() {
        final BuiltinMessage0102AllInOne sourceEntity = createEntityV2011();
        final String hex = this.encode(sourceEntity, Jt808ProtocolVersion.VERSION_2011, terminalId2011);

        final BuiltinMessage0102AllInOne decodedEntity1 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2011, BuiltinMessage0102AllInOne.class, hex);
        doCompareV2011(sourceEntity, decodedEntity1);

        final BuiltinMessage0102AllInOne decodedEntity2 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2011, BuiltinMessage0102AllInOne.class, hex);
        doCompareV2011(sourceEntity, decodedEntity2);
    }

    private static void doCompareV2011(BuiltinMessage0102AllInOne sourceEntity, BuiltinMessage0102AllInOne decodedEntity) {
        assertEquals(sourceEntity.getAuthenticationCode(), decodedEntity.getAuthenticationCode());
        assertNull(decodedEntity.getImei());
        assertNull(decodedEntity.getSoftwareVersion());
        assertThrows(NullPointerException.class, decodedEntity::getFormatedSoftwareVersion);
    }

    @Test
    void testV2013() {
        final BuiltinMessage0102AllInOne sourceEntity = createEntityV2013();
        final String hex = this.encode(sourceEntity, Jt808ProtocolVersion.VERSION_2013, terminalId2013);

        final BuiltinMessage0102AllInOne decodedEntity1 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2013, BuiltinMessage0102AllInOne.class, hex);
        doCompareV2013(sourceEntity, decodedEntity1);

        final BuiltinMessage0102AllInOne decodedEntity2 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2013, BuiltinMessage0102AllInOne.class, hex);
        doCompareV2013(sourceEntity, decodedEntity2);
    }

    private void doCompareV2013(BuiltinMessage0102AllInOne sourceEntity, BuiltinMessage0102AllInOne decodedEntity) {
        assertEquals(sourceEntity.getAuthenticationCode(), decodedEntity.getAuthenticationCode());
        assertNull(decodedEntity.getImei());
        assertNull(decodedEntity.getSoftwareVersion());
        assertThrows(NullPointerException.class, decodedEntity::getFormatedSoftwareVersion);
    }

    @Test
    void testV2019() {
        final BuiltinMessage0102AllInOne sourceEntity = createEntityV2019();
        final String hex = this.encode(sourceEntity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        final BuiltinMessage0102AllInOne decodedEntity1 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2019, BuiltinMessage0102AllInOne.class, hex);
        doCompareV2019(sourceEntity, decodedEntity1);

        final BuiltinMessage0102AllInOne decodedEntity2 = this.decodeAsEntity(Jt808ProtocolVersion.VERSION_2019, BuiltinMessage0102AllInOne.class, hex);
        doCompareV2019(sourceEntity, decodedEntity2);
    }

    private void doCompareV2019(BuiltinMessage0102AllInOne sourceEntity, BuiltinMessage0102AllInOne decodedEntity) {
        assertEquals(sourceEntity.getAuthenticationCode(), decodedEntity.getAuthenticationCode());
        assertEquals(sourceEntity.getImei(), decodedEntity.getImei());
        assertEquals(sourceEntity.getSoftwareVersion(), decodedEntity.getSoftwareVersion());
        assertEquals(sourceEntity.getFormatedSoftwareVersion(), decodedEntity.getFormatedSoftwareVersion());
    }

    private BuiltinMessage0102AllInOne createEntityV2011() {
        return new BuiltinMessage0102AllInOne()
                .setAuthenticationCode("AuthCodeV2011")
                // 其他字段用不到
                .setImei("xxxx");
    }

    private BuiltinMessage0102AllInOne createEntityV2013() {
        return new BuiltinMessage0102AllInOne()
                .setAuthenticationCode("AuthCodeV2013")
                // 其他字段用不到
                .setImei("xxxx");
    }

    private BuiltinMessage0102AllInOne createEntityV2019() {
        return new BuiltinMessage0102AllInOne()
                .setAuthenticationCode("AuthCodeV2013")
                .setImei("imei01234567890")
                .setSoftwareVersion("v0123456789012345678");
    }
}
