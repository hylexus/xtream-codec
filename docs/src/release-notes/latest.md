---
icon: code-branch
article: false
---

# v-0.1.x

## 0.1.1(2025-10-08)

### 🐞 Bug Fixes

- `BuiltinMessage0200.Item0x11#locationId` 类型标记错误

## 0.1.0(2025-10-01)

### 🔨 Dependency Upgrades

- `spring-boot` 升级到 **3.5.6**
- `spring-cloud` 升级到 **2025.0.0**

### ⭐ New Features

- 实体映射支持多版本
    - 新增 `@XtreamField.version()` 属性
    - 提供几个多版本合一的实体映射示例(**quick-start** 项目同步修改)
        - `BuiltinMessage0100AllInOne`
        - `BuiltinMessage0102AllInOne`
        - `BuiltinMessage0107AllInOne`
        - `BuiltinMessage0702AllInOne`
- 实体映射支持 `record` 类
- 提供更简洁的 `Map` 映射注解
    - `@Preset.RustStyle.simple_map`
    - `@Preset.JtStyle.SimpleMap`

### ⚠️ Deprecations

下面废弃的类将在 `1.x` 版本中删除。

- 废弃 ~~`@Preset.JtStyle.Map`~~
    - 使用 `@Preset.JtStyle.SimpleMap` 代替
    - 简化了 ~~`@Preset.JtStyle.Map`~~ 的配置
- 废弃 ~~`@Preset.RustStyle.map`~~
    - 使用 `@Preset.RustStyle.simple_map` 代替
    - 简化了 ~~`@Preset.RustStyle.map`~~ 的配置
- 下面几个内置编解码器单例已废弃，使用 `I8FieldCodecs` 代替
    - ~~`I8FieldCodec.INSTANCE`~~
- 下面几个内置编解码器单例已废弃，使用 `U8FieldCodecs` 代替
    - ~~`U8FieldCodec.INSTANCE`~~
- 下面几个内置编解码器单例已废弃，使用 `I16FieldCodecs` 代替
    - ~~`I16FieldCodec.INSTANCE`~~
    - ~~`I16FieldCodecLittleEndian.INSTANCE`~~
- 下面几个内置编解码器单例已废弃，使用 `U16FieldCodecs` 代替
    - ~~`U16FieldCodec.INSTANCE`~~
    - ~~`U16FieldCodecLittleEndian.INSTANCE`~~
- 下面几个内置编解码器单例已废弃，使用 `I32FieldCodecs` 代替
    - ~~`I32FieldCodec.INSTANCE`~~
    - ~~`I32FieldCodecLittleEndian.INSTANCE`~~
- 下面几个内置编解码器单例已废弃，使用 `U32FieldCodecs` 代替
    - ~~`U32FieldCodec.INSTANCE`~~
    - ~~`U32FieldCodecLittleEndian.INSTANCE`~~
- 下面几个内置编解码器单例已废弃，使用 `U32FieldCodecs` 代替
    - ~~`I64FieldCodecs.INSTANCE`~~
    - ~~`I64FieldCodecLittleEndian.INSTANCE`~~
- 下面几个内置编解码器单例已废弃，使用 `StringFieldCodecs` 代替
    - ~~`StringFieldCodec.INSTANCE_BCD_8421`~~
    - ~~`StringFieldCodec.INSTANCE_UTF8`~~
    - ~~`StringFieldCodec.INSTANCE_GBK`~~
    - ~~`StringFieldCodec.INSTANCE_GB_2312`~~
    - ~~`StringFieldCodec.INSTANCE_HEX`~~
- 下面几个内置编解码器单例已废弃，使用 `BytesFieldCodecs` 代替
    - ~~`ByteBufFieldCodec.INSTANCE`~~
    - ~~`ByteBufContainerFieldCodec.INSTANCE`~~
    - ~~`ByteBoxArrayFieldCodec.INSTANCE`~~
    - ~~`ByteArrayFieldCodec.INSTANCE`~~
    - ~~`ByteArrayContainerFieldCodec.INSTANCE`~~
- 下面几个内置编解码器单例已废弃，使用 `DataWrapperFieldCodes` 代替
    - ~~`DataWrapperFieldCodec.INSTANCE`~~
    - ~~`DwordWrapperFieldCodec.INSTANCE`~~
    - ~~`I8WrapperFieldCodec.INSTANCE`~~
    - ~~`I16WrapperFieldCodec.INSTANCE`~~
    - ~~`I32WrapperFieldCodec.INSTANCE`~~
    - ~~`StringWrapperGbkFieldCodec.INSTANCE`~~
    - ~~`StringWrapperBcdFieldCodec.INSTANCE`~~
    - ~~`StringWrapperUtf8FieldCodec.INSTANCE`~~
    - ~~`U8WrapperFieldCodec.INSTANCE`~~
    - ~~`U16WrapperFieldCodec.INSTANCE`~~
    - ~~`U32WrapperFieldCodec.INSTANCE`~~
    - ~~`WordWrapperFieldCodec.INSTANCE`~~

### 🐞 Bug Fixes

- `BuiltinMessage0107V2013.type` 从 `short` 改为 `int`
- `BuiltinMessage0107V2019.type` 从 `short` 改为 `int`
- `BuiltinMessage0500.flowId` 从 `short` 改为 `int`
- `BuiltinMessage0802.multimediaDataItemCount` 从 `short` 改为 `int`
- `BuiltinMessage0802.BuiltinMessage8805` 从 `int` 改为 `long`
