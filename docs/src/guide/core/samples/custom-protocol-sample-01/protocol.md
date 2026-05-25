---
date: 2024-03-09
icon: file-lines
author: hylexus
contributors: true
category:
  - 示例
tag:
  - Quick-Start
  - 编码
  - 解码
---

# 协议格式说明

## 请先读我

::: warning

这里的协议格式是 *瞎编的* 一种私有协议，仅仅用于演示如何基于注解进行协议编解码。

:::

- 本示例将演示 **3** 种风格的注解使用示例:
    1) `Rust` 命名风格的注解
    2) `JT/T 808` 协议 命名风格的注解
    3) 最底层的 `@XtreamField` 风格注解(实际上前两种注解都是 `@XtreamField` 的别名)
- 同时也会分为 `扁平化`、`扁平化-简化`、`内嵌` 和 `内嵌-简化` **4** 种方式的注解使用示例：也就是 `3 * 4 = 12` 个 基于注解的实体类示例

## 协议格式

协议格式分为两大块：

- 消息头
- 消息体

```
header # 消息头
  magicNumber:      固定为 0x80901234
  majorVersion:     主版本号 无符号数 1字节
  minorVersion:     次版本号 无符号数 1字节
  msgType:          消息类型 无符号数 2字节
  msgBodyLength:    消息体长度 无符号数 2字节
body # 消息体
  if msgType == 0x0007:
    usernameLength:     下一个字段长度 无符号数 2字节
    userName:           String "UTF-8"
    passwordLength:     下一个字段长度 无符号数 2字节
    password:           String "GBK"
    birthDay:           "yyyyMMdd" "UTF-8" 8字节
    phoneNumber:        BCD_8421[6] 6字节
    age:                无符号数 2字节
    status:             "有"符号数 2字节 
  else if msgType == 0x0002:
    ....
  else if msgType == ...:
    ...
  else:
    ...
```

## 编解码

- 参考 [扁平化写法示例](flatten-style-demo.md)
- 参考 [嵌套写法示例](nested-style-demo.md)

