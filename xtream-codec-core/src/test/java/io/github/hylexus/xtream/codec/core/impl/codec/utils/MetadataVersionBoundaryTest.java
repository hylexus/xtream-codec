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

package io.github.hylexus.xtream.codec.core.impl.codec.utils;

import io.github.hylexus.xtream.codec.core.annotation.XtreamField;
import io.github.hylexus.xtream.codec.core.annotation.ext.Key;
import io.github.hylexus.xtream.codec.core.annotation.ext.KeyType;
import io.github.hylexus.xtream.codec.core.annotation.ext.LengthFieldType;
import io.github.hylexus.xtream.codec.core.annotation.ext.ValueLength;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 公共键及长度元数据的版本冲突、数组顺序与合法读写测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
class MetadataVersionBoundaryTest {
    private static final int ALL = XtreamField.ALL_VERSION;

    static Stream<Arguments> mixedVersions() {
        return Stream.of(false, true).flatMap(length -> Stream.of(
                new int[]{ALL, 1}, new int[]{1, ALL},
                new int[]{ALL, 1, 2}, new int[]{1, ALL, 2}, new int[]{1, 2, ALL})
                .map(versions -> Arguments.of(length, versions)));
    }

    @ParameterizedTest
    @MethodSource("mixedVersions")
    void rejectsMixingDefaultAndExplicitVersionsRegardlessOfArrayOrder(boolean length, int[] versions) {
        final int[] before = versions.clone();
        assertThatThrownBy(() -> create(length, new int[][]{versions}))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("ALL_VERSION").hasMessageContaining("other versions");
        assertThat(versions).containsExactly(before);
    }

    static Stream<Arguments> duplicateVersions() {
        return Stream.of(false, true).flatMap(length -> Stream.of(
                new int[][]{{1}, {1}}, new int[][]{{1, 1}},
                new int[][]{{ALL}, {ALL}}, new int[][]{{1, 2}, {2, 3}})
                .map(versions -> Arguments.of(length, (Object) versions)));
    }

    @ParameterizedTest
    @MethodSource("duplicateVersions")
    void rejectsDuplicatesInsideAndAcrossConfigurationsIncludingDefaults(boolean length, int[][] versions) {
        final int duplicate = versions.length == 2 && versions[0].length == 2 ? 2 : versions[0][0];
        assertThatThrownBy(() -> create(length, versions)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate version").hasMessageContaining("Components:")
                .hasMessageContaining("Key: " + duplicate);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void acceptsUnsortedExplicitVersionsAndAnIndependentDefaultAndUsesSelectedWidths(int version) {
        final int[] versions = {2, 1};
        final Key exactKey = key(versions, KeyType.u16);
        final Key fallbackKey = key(new int[]{ALL}, KeyType.u8);
        final ValueLength exactLength = length(versions, LengthFieldType.u16);
        final ValueLength fallbackLength = length(new int[]{ALL}, LengthFieldType.u8);
        final boolean exact = version != 3;
        final var keyMeta = MetadataUtils.createKeyMeta(version, new Key[]{fallbackKey, exactKey});
        final var lengthMeta = MetadataUtils.createValueLengthMeta(version, new ValueLength[]{exactLength, fallbackLength});
        assertThat(keyMeta.version()).isEqualTo(exact ? version : ALL);
        assertThat(keyMeta.type()).isEqualTo(exact ? KeyType.u16 : KeyType.u8);
        assertThat(lengthMeta.type()).isEqualTo(exact ? LengthFieldType.u16 : LengthFieldType.u8);
        assertThat(versions).containsExactly(2, 1);
        assertThat(exactKey.version()).containsExactly(2, 1);
        assertThat(exactLength.version()).containsExactly(2, 1);
        final ByteBuf output = Unpooled.buffer().writeByte(0x55);
        final ByteBuf input = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(exact ? "55123466" : "553466")).readerIndex(1);
        try {
            final int expected = exact ? 0x1234 : 0x34;
            final var readKey = keyMeta.readFrom(input);
            assertThat(((Number) readKey.value()).intValue()).isEqualTo(expected);
            readKey.encode(output);
            assertThat(ByteBufUtil.hexDump(output)).isEqualTo(exact ? "551234" : "5534");
            input.readerIndex(1);
            assertThat(lengthMeta.type().readFrom(input).intValue()).isEqualTo(expected);
            assertThat(input.readerIndex()).isEqualTo(exact ? 3 : 2);
            assertThat(input.getUnsignedByte(input.readerIndex())).isEqualTo((short) 0x66);
            output.writerIndex(1);
            lengthMeta.type().writeTo(output, expected);
            assertThat(ByteBufUtil.hexDump(output)).isEqualTo(exact ? "551234" : "5534");
            assertThat(output.readerIndex()).isZero();
            assertThat(input.refCnt()).isEqualTo(1);
            assertThat(output.refCnt()).isEqualTo(1);
        } finally {
            input.release();
            output.release();
        }
    }

    private static void create(boolean length, int[][] versions) {
        if (length) {
            MetadataUtils.createValueLengthMeta(1, Stream.of(versions).map(v -> length(v, LengthFieldType.u8)).toArray(ValueLength[]::new));
        } else {
            MetadataUtils.createKeyMeta(1, Stream.of(versions).map(v -> key(v, KeyType.u8)).toArray(Key[]::new));
        }
    }

    private static Key key(int[] versions, KeyType type) {
        return AnnotationUtils.synthesizeAnnotation(Map.of("version", versions, "type", type), Key.class, null);
    }

    private static ValueLength length(int[] versions, LengthFieldType type) {
        return AnnotationUtils.synthesizeAnnotation(Map.of("version", versions, "type", type), ValueLength.class, null);
    }
}
