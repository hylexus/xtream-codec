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

package io.github.hylexus.xtream.debug.codec.core;

import io.github.hylexus.xtream.debug.codec.core.demo04.DemoJt808Msg0100V2019;
import io.github.hylexus.xtream.debug.codec.core.demo04.SimpleSubPackageEntityCodec;
import io.github.hylexus.xtream.debug.codec.core.demo04.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DebugEntity04Test extends BaseEntityCodecTest {

    @BeforeEach
    void setUp() {
        // 这里使用支持分包的实现类
        this.entityCodec = new SimpleSubPackageEntityCodec();
    }

    @Test
    void test() {
        // 这是一个完整的包(不是分包)
        final String hexString2019 = "010040560100000000013912344329007B000B00026964393837363534333231"
                + "747970653030313233343536373831323334353637383837363534333231494430303030313233"
                + "34353637383132333435363738383736353433323101B8CA4A2D31323334353925";

        final DemoJt808Msg0100V2019 instance = super.doDecode(DemoJt808Msg0100V2019.class, hexString2019);
        Assertions.assertEquals(0x0100, instance.getHeader().getMsgId());
        Assertions.assertEquals(Jt808ProtocolVersion.VERSION_2019, instance.getHeader().getVersion());
        Assertions.assertEquals(1, instance.getHeader().getMsgVersionNumber());
        Assertions.assertEquals("00000000013912344329", instance.getHeader().getTerminalPhoneNo());
        Assertions.assertEquals(123, instance.getHeader().getMsgSerialNo());
        Assertions.assertEquals(11, instance.getProvinceId());
        Assertions.assertEquals(2, instance.getCityId());
        Assertions.assertEquals("id987654321", instance.getManufacturerId());
        Assertions.assertEquals("type00123456781234567887654321", instance.getTerminalType());
        Assertions.assertEquals("ID0000123456781234567887654321", instance.getTerminalId());
        Assertions.assertEquals(1, instance.getColor());
        Assertions.assertEquals("甘J-123459", instance.getCarIdentifier());

        Assertions.assertNull(instance.getInternalTemporarySubPackageBodyByteBuf());
    }

    @Test
    void test2() {
        // 下面是 3 个分包
        final String package1 = "0100602A0100000000013912344329FFFD00030001000B000269643938373635343332317479706530303132333435363738313233343536373838373635342B";
        final String package2 = "0100602A0100000000013912344329FFFE0003000233323149443030303031323334353637383132333435363738383736353433323101B8CA4A2D313233341B";
        final String package3 = "010060020100000000013912344329FFFF0003000335391A";

        // 解析前两个子包时返回 null (还有子包没到齐)
        final DemoJt808Msg0100V2019 subPackage1 = super.doDecode(DemoJt808Msg0100V2019.class, package1);
        Assertions.assertNull(subPackage1);

        final DemoJt808Msg0100V2019 subPackage2 = super.doDecode(DemoJt808Msg0100V2019.class, package2);
        Assertions.assertNull(subPackage2);

        // 当第三个包到达后合并成一个完整包
        final DemoJt808Msg0100V2019 subPackage3 = super.doDecode(DemoJt808Msg0100V2019.class, package3);
        Assertions.assertNotNull(subPackage3);

        Assertions.assertEquals(0x0100, subPackage3.getHeader().getMsgId());
        Assertions.assertEquals(Jt808ProtocolVersion.VERSION_2019, subPackage3.getHeader().getVersion());
        Assertions.assertEquals(1, subPackage3.getHeader().getMsgVersionNumber());
        Assertions.assertEquals("00000000013912344329", subPackage3.getHeader().getTerminalPhoneNo());
        Assertions.assertEquals(65535, subPackage3.getHeader().getMsgSerialNo());
        Assertions.assertEquals(11, subPackage3.getProvinceId());
        Assertions.assertEquals(2, subPackage3.getCityId());
        Assertions.assertEquals("id987654321", subPackage3.getManufacturerId());
        Assertions.assertEquals("type00123456781234567887654321", subPackage3.getTerminalType());
        Assertions.assertEquals("ID0000123456781234567887654321", subPackage3.getTerminalId());
        Assertions.assertEquals(1, subPackage3.getColor());
        Assertions.assertEquals(0x1A, subPackage3.getCheckSum());
        Assertions.assertEquals("甘J-123459", subPackage3.getCarIdentifier());

        Assertions.assertNull(subPackage3.getInternalTemporarySubPackageBodyByteBuf());
    }
}
