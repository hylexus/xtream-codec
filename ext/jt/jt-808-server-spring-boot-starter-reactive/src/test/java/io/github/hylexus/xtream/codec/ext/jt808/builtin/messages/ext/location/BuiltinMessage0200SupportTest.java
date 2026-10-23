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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.Set;
import static io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.BuiltinMessage0200Support.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 报警和状态派生字段转换边界测试。
 *
 * @author Codex (AI)
 */
class BuiltinMessage0200SupportTest {
    @ParameterizedTest
    @CsvSource({"0, CARGO_EMPTY", "256, CARGO_HALF_LOADED", "512, CARGO_RESERVED", "768, CARGO_FULL_LOADED"})
    void testCargoRange(long bits, StatusBit cargo) {
        final StatusBitTransformer transformer = new StatusBitTransformer();
        assertEquals(Set.of(StatusBit.ACC_ON, StatusBit.MOVING, cargo), transformer.read(bits | 0x400001L));
        assertEquals(bits | 0x400001L, transformer.write(Set.of(StatusBit.ACC_ON, StatusBit.MOVING, cargo)).longValue());
    }

    @Test
    void testAlarmFlagsAndDescription() {
        final AlarmFlagTransformer flags = new AlarmFlagTransformer();
        final AlarmDescriptionTransformer description = new AlarmDescriptionTransformer();
        assertEquals(Set.of(AlarmFlag.EMERGENCY, AlarmFlag.ROLLOVER_WARNING), flags.read(0x40000001L));
        assertEquals(0x40000001L, flags.write(Set.of(AlarmFlag.EMERGENCY, AlarmFlag.ROLLOVER_WARNING)).longValue());
        assertEquals("紧急报警 | 侧翻预警", description.read(0x40000001L));
        assertEquals(Set.of(), flags.read(0x80000000L));
        assertEquals("", description.read(0x80000000L));
        assertEquals(0L, flags.write(Set.of()).longValue());
    }

    @Test
    void testNullTransformations() {
        assertNull(new AlarmFlagTransformer().read(null));
        assertNull(new AlarmFlagTransformer().write(null));
        assertNull(new AlarmDescriptionTransformer().read(null));
        assertNull(new StatusBitTransformer().read(null));
        assertNull(new StatusBitTransformer().write(null));
    }
}

