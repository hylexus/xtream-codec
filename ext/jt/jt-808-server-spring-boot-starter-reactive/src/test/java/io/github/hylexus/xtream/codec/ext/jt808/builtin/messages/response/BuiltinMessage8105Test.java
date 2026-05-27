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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.sample8105.types.ByteCommandValue;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.sample8105.types.CommandValue;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.sample8105.types.StringCommandValue;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.sample8105.types.WordCommandValue;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BuiltinMessage8105Test extends BaseCodecTest {

    @Test
    void testEncodeCommand1Case1() {
        final BuiltinMessage8105 entity = new BuiltinMessage8105();
        entity.setCommandWord((short) 1);
        // 这个 List 中的元素顺序可以任意(编码器会排序)、但是 offset 不能重复、并不是13 个命令都要出现(编码器会自动填充";"作为分隔符)
        entity.setCommandValue(List.of(
                // offset-0: 连接控制
                new ByteCommandValue(0, (short) 1),
                // offset-1: 拨号点名称
                new StringCommandValue(1, "Name..."),
                // offset-8: 监管平台鉴权码
                new StringCommandValue(8, "AuthCode..."),
                // offset-2: 拨号用户名
                new StringCommandValue(2, "Username..."),
                // offset-3: 拨号密码
                new StringCommandValue(3, "Password..."),
                // offset-4: 地址
                new StringCommandValue(4, "192.168.0.1"),
                // offset-5: TCP端口
                new WordCommandValue(5, 6666),
                // offset-6: UDP端口
                new WordCommandValue(6, 7777),
                // offset-10: 固件版本
                new StringCommandValue(10, "swv-1.0.1"),
                // offset-9: 硬件版本
                new StringCommandValue(9, "hwv-2.0.3"),
                // offset-7: 制造商ID
                new StringCommandValue(7, "id0001"),
                // offset-11: URL地址
                new StringCommandValue(11, "192.168.0.2"),
                // offset-12: 连接到指定服务器时限
                new WordCommandValue(12, 49)
        ));
        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8105406b0100000000013912344329000001013b4e616d652e2e2e3b557365726e616d652e2e2e3b50617373776f72642e2e2e3b3139322e3136382e302e313b1a0a3b1e613b6964303030313b41757468436f64652e2e2e3b6877762d322e302e333b7377762d312e302e313b3139322e3136382e302e323b00313b817e", hex);
    }

    @Test
    void testDecodeCommand1Case1() {
        final BuiltinMessage8105 entity = decodeAsEntity(BuiltinMessage8105.class, "8105406b0100000000013912344329000001013b4e616d652e2e2e3b557365726e616d652e2e2e3b50617373776f72642e2e2e3b3139322e3136382e302e313b1a0a3b1e613b6964303030313b41757468436f64652e2e2e3b6877762d322e302e333b7377762d312e302e313b3139322e3136382e302e323b00313b81");
        assertEquals(1, entity.getCommandWord());

        final List<CommandValue<?>> valueList = entity.getCommandValue();
        assertEquals(13, valueList.size());

        assertEquals((short) 1, valueList.getFirst().value());
        assertEquals("Name...", valueList.get(1).value());
        assertEquals("AuthCode...", valueList.get(8).value());
        assertEquals("Username...", valueList.get(2).value());
        assertEquals("Password...", valueList.get(3).value());
        assertEquals("192.168.0.1", valueList.get(4).value());
        assertEquals(6666, valueList.get(5).value());
        assertEquals(7777, valueList.get(6).value());
        assertEquals("swv-1.0.1", valueList.get(10).value());
        assertEquals("hwv-2.0.3", valueList.get(9).value());
        assertEquals("id0001", valueList.get(7).value());
        assertEquals("192.168.0.2", valueList.get(11).value());
        assertEquals(49, valueList.get(12).value());
    }

    @Test
    void testEncodeCommand1Case2() {
        final BuiltinMessage8105 entity = new BuiltinMessage8105();
        entity.setCommandWord((short) 1);
        // 这个 List 中的元素顺序可以任意(编码器会排序)、但是 offset 不能重复、并不是13 个命令都要出现(编码器会自动填充";"作为分隔符)
        entity.setCommandValue(List.of(
                // offset-1: 拨号点名称
                new StringCommandValue(1, "Name..."),
                // offset-3: 拨号密码
                new StringCommandValue(3, "Password..."),
                // offset-5: TCP端口
                new WordCommandValue(5, 6666)

        ));
        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8105402201000000000139123443290000013b4e616d652e2e2e3b3b50617373776f72642e2e2e3b3b1a0a3b3b3b3b3b3b3b3ba17e", hex);
    }

    @Test
    void testDecodeCommand1Case2() {
        final BuiltinMessage8105 entity = decodeAsEntity(BuiltinMessage8105.class, "8105402201000000000139123443290000013b4e616d652e2e2e3b3b50617373776f72642e2e2e3b3b1a0a3b3b3b3b3b3b3b3ba1");
        assertEquals(1, entity.getCommandWord());
        final List<CommandValue<?>> valueList = entity.getCommandValue();
        assertEquals(13, valueList.size());

        assertEquals("Name...", valueList.get(1).value());
        assertEquals("Password...", valueList.get(3).value());
        assertEquals(6666, valueList.get(5).value());
        assertNull(valueList.get(6).value());
        assertNull(valueList.get(7).value());
        assertNull(valueList.get(8).value());
        assertNull(valueList.get(9).value());
        assertNull(valueList.get(10).value());
        assertNull(valueList.get(11).value());
        assertNull(valueList.get(12).value());
    }

    @Test
    void testEncodeCommand2Case1() {
        final BuiltinMessage8105 entity = new BuiltinMessage8105();
        entity.setCommandWord((short) 2);
        // 这个 List 中的元素顺序可以任意(编码器会排序)、但是 offset 不能重复、并不是13 个命令都要出现(编码器会自动填充";"作为分隔符)
        entity.setCommandValue(List.of(
                // offset-0: 连接控制
                new ByteCommandValue(0, (short) 1),
                // offset-1: 拨号点名称
                new StringCommandValue(1, "Name..."),
                // offset-8: 监管平台鉴权码
                new StringCommandValue(8, "AuthCode..."),
                // offset-2: 拨号用户名
                new StringCommandValue(2, "Username..."),
                // offset-3: 拨号密码
                new StringCommandValue(3, "Password..."),
                // offset-4: 地址
                new StringCommandValue(4, "192.168.0.1"),
                // offset-5: TCP端口
                new WordCommandValue(5, 6666),
                // offset-6: UDP端口
                new WordCommandValue(6, 7777),
                // offset-10: 固件版本
                new StringCommandValue(10, "swv-1.0.1"),
                // offset-9: 硬件版本
                new StringCommandValue(9, "hwv-2.0.3"),
                // offset-7: 制造商ID
                new StringCommandValue(7, "id0001"),
                // offset-11: URL地址
                new StringCommandValue(11, "192.168.0.2"),
                // offset-12: 连接到指定服务器时限
                new WordCommandValue(12, 49)
        ));
        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8105406b0100000000013912344329000002013b4e616d652e2e2e3b557365726e616d652e2e2e3b50617373776f72642e2e2e3b3139322e3136382e302e313b1a0a3b1e613b6964303030313b41757468436f64652e2e2e3b6877762d322e302e333b7377762d312e302e313b3139322e3136382e302e323b00313b827e", hex);
    }

    @Test
    void testDecodeCommand2Case1() {
        final BuiltinMessage8105 entity = decodeAsEntity(BuiltinMessage8105.class, "8105406b0100000000013912344329000002013b4e616d652e2e2e3b557365726e616d652e2e2e3b50617373776f72642e2e2e3b3139322e3136382e302e313b1a0a3b1e613b6964303030313b41757468436f64652e2e2e3b6877762d322e302e333b7377762d312e302e313b3139322e3136382e302e323b00313b82");
        assertEquals(2, entity.getCommandWord());

        final List<CommandValue<?>> valueList = entity.getCommandValue();
        assertEquals(13, valueList.size());

        assertEquals((short) 1, valueList.getFirst().value());
        assertEquals("Name...", valueList.get(1).value());
        assertEquals("AuthCode...", valueList.get(8).value());
        assertEquals("Username...", valueList.get(2).value());
        assertEquals("Password...", valueList.get(3).value());
        assertEquals("192.168.0.1", valueList.get(4).value());
        assertEquals(6666, valueList.get(5).value());
        assertEquals(7777, valueList.get(6).value());
        assertEquals("swv-1.0.1", valueList.get(10).value());
        assertEquals("hwv-2.0.3", valueList.get(9).value());
        assertEquals("id0001", valueList.get(7).value());
        assertEquals("192.168.0.2", valueList.get(11).value());
        assertEquals(49, valueList.get(12).value());
    }

    @Test
    void testEncodeCommand2Case2() {
        final BuiltinMessage8105 entity = new BuiltinMessage8105();
        entity.setCommandWord((short) 2);
        // 这个 List 中的元素顺序可以任意(编码器会排序)、但是 offset 不能重复、并不是13 个命令都要出现(编码器会自动填充";"作为分隔符)
        entity.setCommandValue(List.of(
                // offset-1: 拨号点名称
                new StringCommandValue(1, "Name..."),
                // offset-3: 拨号密码
                new StringCommandValue(3, "Password..."),
                // offset-5: TCP端口
                new WordCommandValue(5, 6666)

        ));
        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e8105402201000000000139123443290000023b4e616d652e2e2e3b3b50617373776f72642e2e2e3b3b1a0a3b3b3b3b3b3b3b3ba27e", hex);
    }

    @Test
    void testDecodeCommand2Case2() {
        final BuiltinMessage8105 entity = decodeAsEntity(BuiltinMessage8105.class, "8105402201000000000139123443290000023b4e616d652e2e2e3b3b50617373776f72642e2e2e3b3b1a0a3b3b3b3b3b3b3b3ba2");
        assertEquals(2, entity.getCommandWord());
        final List<CommandValue<?>> valueList = entity.getCommandValue();
        assertEquals(13, valueList.size());

        assertEquals("Name...", valueList.get(1).value());
        assertEquals("Password...", valueList.get(3).value());
        assertEquals(6666, valueList.get(5).value());
        assertNull(valueList.get(6).value());
        assertNull(valueList.get(7).value());
        assertNull(valueList.get(8).value());
        assertNull(valueList.get(9).value());
        assertNull(valueList.get(10).value());
        assertNull(valueList.get(11).value());
        assertNull(valueList.get(12).value());
    }

    @Test
    void testEncodeCommand3() {
        final BuiltinMessage8105 entity = new BuiltinMessage8105();
        entity.setCommandWord((short) 3);
        // 无参数
        entity.setCommandValue(null);
        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e810540010100000000013912344329000003b37e", hex);
    }

    @Test
    void testDecodeCommand3() {
        final BuiltinMessage8105 entity = decodeAsEntity(BuiltinMessage8105.class, "810540010100000000013912344329000003b3");
        assertEquals(3, entity.getCommandWord());
        assertNull(entity.getCommandValue());
    }

    @Test
    void testEncodeCommand4() {
        final BuiltinMessage8105 entity = new BuiltinMessage8105();
        entity.setCommandWord((short) 4);
        // 无参数
        entity.setCommandValue(null);
        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e810540010100000000013912344329000004b47e", hex);
    }

    @Test
    void testDecodeCommand4() {
        final BuiltinMessage8105 entity = decodeAsEntity(BuiltinMessage8105.class, "810540010100000000013912344329000004b4");
        assertEquals(4, entity.getCommandWord());
        assertNull(entity.getCommandValue());
    }


    @Test
    void testEncodeCommand5() {
        final BuiltinMessage8105 entity = new BuiltinMessage8105();
        entity.setCommandWord((short) 5);
        // 无参数
        entity.setCommandValue(null);
        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e810540010100000000013912344329000005b57e", hex);
    }

    @Test
    void testDecodeCommand5() {
        final BuiltinMessage8105 entity = decodeAsEntity(BuiltinMessage8105.class, "810540010100000000013912344329000005b5");
        assertEquals(5, entity.getCommandWord());
        assertNull(entity.getCommandValue());
    }

    @Test
    void testEncodeCommand6() {
        final BuiltinMessage8105 entity = new BuiltinMessage8105();
        entity.setCommandWord((short) 6);
        // 无参数
        entity.setCommandValue(null);
        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e810540010100000000013912344329000006b67e", hex);
    }

    @Test
    void testDecodeCommand6() {
        final BuiltinMessage8105 entity = decodeAsEntity(BuiltinMessage8105.class, "810540010100000000013912344329000006b6");
        assertEquals(6, entity.getCommandWord());
        assertNull(entity.getCommandValue());
    }

    @Test
    void testEncodeCommand7() {
        final BuiltinMessage8105 entity = new BuiltinMessage8105();
        entity.setCommandWord((short) 7);
        // 无参数
        entity.setCommandValue(null);
        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2019, terminalId2019);
        assertEquals("7e810540010100000000013912344329000007b77e", hex);
    }

    @Test
    void testDecodeCommand7() {
        final BuiltinMessage8105 entity = decodeAsEntity(BuiltinMessage8105.class, "810540010100000000013912344329000007b7");
        assertEquals(7, entity.getCommandWord());
        assertNull(entity.getCommandValue());
    }

}
