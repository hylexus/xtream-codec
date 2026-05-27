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

package io.github.hylexus.xtream.codec.ext.jt808.spec;


import io.github.hylexus.xtream.codec.common.utils.FormatUtils;

public interface Jt808MessageEncryptionType {

    int DEFAULT_ENCRYPTION_TYPE = 0b000;

    static Jt808MessageEncryptionType fromMessageBodyProps(int bodyPros) {
        // bit[10-12] 0001,1100,0000,0000(1C00)(加密类型)
        return new Default((bodyPros & 0x1c00) >> 10);
    }

    static Jt808MessageEncryptionType fromIntValue(int value) {
        return new Default(value & 0b111);
    }

    static Jt808MessageEncryptionType fromBits(int bit10, int bit11, int bit12) {
        return new Default(
                (bit10 & 0b1)
                        | ((bit11 << 1) & 0b10)
                        | ((bit12 << 2) & 0b100)
        );
    }

    int intValue();

    default int bit10() {
        return this.intValue() & 0b001;
    }

    default int bit11() {
        return this.intValue() & 0b010;
    }

    default int bit12() {
        return this.intValue() & 0b100;
    }

    default boolean isEncrypted() {
        return this.intValue() != 0;
    }

    class Default implements Jt808MessageEncryptionType {
        private final int value;

        public Default(int value) {
            this.value = value;
        }

        @Override
        public int intValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return "Default{"
                    + "value=" + value
                    + "(" + FormatUtils.toBinaryString(value, 3) + ")"
                    + '}';
        }
    }
}
