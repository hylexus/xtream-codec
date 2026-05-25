---
date: 2024-03-09
icon: at
author: hylexus
category:
  - 示例
tag:
  - 编码
  - 解码
---

# 嵌套写法示例

::: tip

- 在阅读本文之前，建议先阅读 [协议格式说明](protocol.md)
- 所谓的 _嵌套写法_ 就是将 `Header` 和 `Body` 分别封装到单独的实体类中。

:::

## 实体类定义

::: tabs#demo1

@tab:active Rust 命名风格#rust-style

@[code](@src/core/entity-codec/demo01/RustStyleDebugEntity01Nested.java)

@tab:active Rust 命名风格(简化)#rust-style-simple

@[code java{14,42,47}](@src/core/entity-codec/demo01/RustStyleDebugEntity01NestedSimple.java)

@tab 原始命名风格#raw-style

@[code](@src/core/entity-codec/demo01/RawStyleDebugEntity01Nested.java)

@tab 原始命名风格(简化)#raw-style-simple

@[code java{14,42,47}](@src/core/entity-codec/demo01/RawStyleDebugEntity01NestedSimple.java)

@tab JT/T 808 命名风格#jt-style

@[code](@src/core/entity-codec/demo01/JtStyleDebugEntity01Nested.java)

@tab JT/T 808 命名风格(简化)#jt-style-simple

@[code java{14,42,47}](@src/core/entity-codec/demo01/JtStyleDebugEntity01NestedSimple.java)

:::

## 反序列化

```java {12,15}
public class EntityDecodeTest {

    @Test
    void testDecode() {
        final EntityCodec entityCodec = EntityCodec.DEFAULT;

        // buffer 中存储的是要反序列化的数据(这里写死用来演示)
        final String hexString = "8090123401020001003d001678747265616d2d636f6465632ee794a8e688b7e5908d001178747265616d2d636f6465632ec3dcc2eb3230323130323033013911112222270fff9c";
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer().writeBytes(XtreamBytes.decodeHex(hexString));

        try {
            final RustStyleDebugEntity01Nested entity = entityCodec.decode(RustStyleDebugEntity01Nested.class, buffer);
            System.out.println(entity);
        } finally {
            buffer.release();
        }
    }
}
```

## 序列化

```java {16,20}
public class EntityEncodeTest {

    @Test
    void testEncode() {
        final EntityCodec entityCodec = EntityCodec.DEFAULT;

        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        try {
            final RustStyleDebugEntity01Nested instance = new RustStyleDebugEntity01Nested();
            // 省略属性赋值
            // instance.setXxx(someValue);
            // ...
            instance.setHeader(...);

            // 将 instance 的数据序列化到 buffer 中
            entityCodec.encode(instance, buffer);
            // 使用 buffer
            System.out.println(ByteBufUtil.hexDump(buffer));
        } finally {
            buffer.release();
        }
    }
}
```
