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

package io.github.hylexus.xtream.codec.core.type;

import java.lang.annotation.*;

final class PresetAliasContainers {

    private PresetAliasContainers() {
        throw new UnsupportedOperationException();
    }

    // region RustStyle
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerI8 {
        Preset.RustStyle.i8[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerU8 {
        Preset.RustStyle.u8[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerI16 {
        Preset.RustStyle.i16[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerI16Le {
        Preset.RustStyle.i16_le[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerU16 {
        Preset.RustStyle.u16[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerU16Le {
        Preset.RustStyle.u16_le[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerI32 {
        Preset.RustStyle.i32[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerI32Le {
        Preset.RustStyle.i32_le[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerU32 {
        Preset.RustStyle.u32[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerU32Le {
        Preset.RustStyle.u32_le[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerStr {
        Preset.RustStyle.str[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerStruct {
        Preset.RustStyle.struct[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerDyn {
        Preset.RustStyle.dyn[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerList {
        Preset.RustStyle.list[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerMap {
        Preset.RustStyle.map[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerByteArray {
        Preset.RustStyle.byte_array[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.RECORD_COMPONENT})
    @Documented
    public @interface RustStyleContainerTransientRecordComponent {
        Preset.RustStyle.transient_record_component[] value();
    }

    // endregion RustStyle

    // region JtStyle
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface JtStyleContainerWord {
        Preset.JtStyle.Word[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface JtStyleContainerDword {
        Preset.JtStyle.Dword[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface JtStyleContainerBcd {
        Preset.JtStyle.Bcd[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface JtStyleContainerBcdDateTime {
        Preset.JtStyle.BcdDateTime[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface JtStyleContainerByte {
        Preset.JtStyle.Byte[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface JtStyleContainerBytes {
        Preset.JtStyle.Bytes[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface JtStyleContainerStr {
        Preset.JtStyle.Str[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface JtStyleContainerObject {
        Preset.JtStyle.Object[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface JtStyleContainerRuntimeType {
        Preset.JtStyle.RuntimeType[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface JtStyleContainerList {
        Preset.JtStyle.List[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    @Documented
    public @interface JtStyleContainerMap {
        Preset.JtStyle.Map[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.RECORD_COMPONENT})
    @Documented
    public @interface JtStyleContainerTransientRecordComponent {
        Preset.JtStyle.TransientRecordComponent[] value();
    }
    // endregion JtStyle

}
