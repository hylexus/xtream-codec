---
date: 2025-03-02
icon: arrows-spin
---

# 实体编解码器调试

## 介绍

只需要给 `EntityCodec` 的 `encode` 或 `decode` 方法传入一个 `CodecTracker` 实例即可追踪每个字段的编解码详情。

## 注意

::: danger

`CodecTracker` 的设计目的仅仅是用来调试。 会对编解码性能产生 **严重** 影响。

:::

## 示例

@[code](@src/core/entity-codec/demo00/EntityCodecWithTrackerTest.java)

## 输出效果

```text
RootSpan[entityClass='EntityCodecWithTrackerTest$UserEntity', hexString='0000040009e697a0e5908de6b08f080006e4bf9de5af86']
	BasicFieldSpan[fieldName='id', fieldDesc='用户ID(32位无符号数)', fieldCodec='U32FieldCodec', value=1024, hexString='00000400']
	PrependLengthFieldSpan[fieldName='prependLengthField', fieldDesc='前置长度字段', fieldCodec='u8', value=9, hexString='09']
	BasicFieldSpan[fieldName='name', fieldDesc='用户名', fieldCodec='StringFieldCodec', value=无名氏, hexString='e697a0e5908de6b08f']
	BasicFieldSpan[fieldName='age', fieldDesc='年龄(16位无符号数)', fieldCodec='U16FieldCodec', value=2048, hexString='0800']
	PrependLengthFieldSpan[fieldName='prependLengthField', fieldDesc='前置长度字段', fieldCodec='u8', value=6, hexString='06']
	BasicFieldSpan[fieldName='address', fieldDesc='地址', fieldCodec='StringFieldCodec', value=保密, hexString='e4bf9de5af86']
```
