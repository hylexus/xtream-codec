---
date: 2024-03-10
icon: at
tag:
  - 内置
  - 注解
---

# 内置注解

## 请先读我

内置注解都是从 `@XtreamField` 注解扩展而来的。目前有两种风格的注解：

- `Rust` 数据类型命名风格
- `JT/T 808` 协议数据类型命名风格

::: tip

- 强烈推荐优先使用 `Rust` 风格内置注解
    - 数据类型比较全面
        - `JT/T 808` 风格的注解只支持无符号，因为 `JT/T 808` 官方文档里都是无符号数
    - 支持小端序
- 不推荐**直接**使用底层的 `@XtreamField` 注解
    - 虽然功能最完整，但是使用略显繁琐
    - 但是如果你要自定义注解，那非常推荐使用 `@XtreamField` 来进行扩展

:::

## Rust 风格内置注解

### 整数类型

- `@Preset.RustStyle.i8`
    - 有符号整数
    - **8bit**、**1byte**
    - 大端序
    - **JavaType**: `byte/Byte`
- `@Preset.RustStyle.u8`
    - 无符号整数
    - **8bit**、**1byte**
    - 大端序
    - **JavaType**: `short/Short` (**byte** 有溢出的风险)
- `@Preset.RustStyle.i16`
    - 有符号整数
    - **16bit**、**2byte**
    - 大端序
    - **JavaType**: `short/Short`
- `@Preset.RustStyle.u16`
    - 无符号整数
    - **16bit**、**2byte**
    - 大端序
    - **JavaType**: `int/Integer` (**short** 有溢出的风险)
- `@Preset.RustStyle.i16_le`
    - 有符号整数
    - **16bit**、**2byte**
    - 小端序
    - **JavaType**: `short/Short`
- `@Preset.RustStyle.u16_le`
    - 无符号整数
    - **16bit**、**2byte**
    - 小端序
    - **JavaType**: `int/Integer` (**short** 有溢出的风险)
- `@Preset.RustStyle.i32`
    - 有符号整数
    - **32bit**、**4byte**
    - 大端序
    - **JavaType**: `int/Integer`
- `@Preset.RustStyle.u32`
    - 无符号整数
    - **32bit**、**4byte**
    - 大端序
    - **JavaType**: `long/Long` (**int** 有溢出的风险)
- `@Preset.RustStyle.i32_le`
    - 有符号整数
    - **32bit**、**4byte**
    - 小端序
    - **JavaType**: `int/Integer`
- `@Preset.RustStyle.u32_le`
    - 无符号整数
    - **32bit**、**4byte**
    - 小端序
    - **JavaType**: `long/Long` (**int** 有溢出的风险)

### 字符串类型

- `@Preset.RustStyle.str`
    - 字符串
    - 编码: 默认 `utf-8`
    - **JavaType**: `java.lang.String`

### BCD

- `@Preset.RustStyle.str(charset="bcd_8421")`
    - 字符串
    - 编码固定为: `BCD_8421`
    - **JavaType**: `java.lang.String`
- `@Preset.RustStyle.BcdDateTime`
    - **N 字节**
    - **JavaType**: `java.time.LocalDateTime/java.util.Date/String`

### 嵌套类型

- `@Preset.RustStyle.struct`
    - 结构体、内嵌对象
    - **JavaType**: 自定义实体类

### 列表类型

- `@Preset.RustStyle.list`
    - **JavaType**: `java.util.List`

### 动态类型

- `@Preset.RustStyle.dyn`
    - **JavaType**: `Object`
    - 运行时才能确定具体类型

### 其他

- `@Preset.RustStyle.byte_array`
    - **N 字节**
    - **JavaType**: `byte[]`

## JT/T 808 风格内置注解

### JT/T 808 的相关说明

下文介绍如何用项目的注解读写 JT/T 808—2019 中的数据类型。`BYTE`、`WORD` 和 `DWORD` 分别占 1、2 和 4 字节。在 Java 中用更宽的整数类型保留无符号值，例如用 `short` 表示 `BYTE` 的 `0` 至 `255`。JT/T 808 风格注解与 Rust 风格注解的对应关系见[内置类型对比](#内置类型对比)。

### 整数类型

- `@Preset.JtStyle.Byte`
    - 无符号整数
    - **8bit**、**1byte**
    - 大端序
    - **JavaType**: `short/Short` (**byte** 有溢出的风险)
- `@Preset.JtStyle.Word`
    - 无符号整数
    - **16bit**、**2byte**
    - 大端序
    - **JavaType**: `int/Integer` (**short** 有溢出的风险)
- `@Preset.JtStyle.Dword`
    - 无符号整数
    - **32bit**、**4byte**
    - 大端序
    - **JavaType**: `long/Long` (**int** 有溢出的风险)

### 字符串类型

- `@Preset.JtStyle.Str`
    - 字符串
    - 编码: 默认 `GBK`
    - **JavaType**: `java.lang.String`

### BCD

- `@Preset.JtStyle.Bcd`
    - `BCD[n]` 占 `n` 字节，每字节编码两位十进制数字
    - 使用 `BCD_8421` 编码
    - **JavaType**: `java.lang.String`
- `@Preset.JtStyle.BcdDateTime`
    - **N 字节**
    - **JavaType**: `java.time.LocalDateTime/java.util.Date/String`

### 嵌套类型

- `@Preset.JtStyle.Object`
    - 结构体、内嵌对象
    - **JavaType**: 自定义实体类

### 列表类型

- `@Preset.JtStyle.List`
    - **JavaType**: `java.util.List`

### 动态类型

- `@Preset.JtStyle.RuntimeType`
    - **JavaType**: `Object`
    - 运行时才能确定具体类型

### 其他

- `@Preset.JtStyle.Bytes`
    - `BYTE[n]` 对应 `n` 个原始字节
    - **JavaType**: `byte[]`

## 内置类型对比

### **`Rust风格`** **VS `JT/T 808`风格**

下面表格是 `@Preset.RustStyle.xxx` 和 `@Preset.JtStyle` 的对应关系，当然你也可以自定义自己的注解:

| **Rust** 风格注解                               | **JT/T 808** 风格注解                      | JavaType         |
|---------------------------------------------|----------------------------------------|------------------|
| `@Preset.RustStyle.i8`                      | -                                      | `byte/Byte`      |
| `@Preset.RustStyle.u8`                      | `@Preset.JtStyle.Byte`                 | `short/Short`    |
| `@Preset.RustStyle.i16`                     | -                                      | `short/Short`    |
| `@Preset.RustStyle.i16_le`                  | -                                      | `short/Short`    |
| `@Preset.RustStyle.u16`                     | `@Preset.JtStyle.Word`                 | `int/Integer`    |
| `@Preset.RustStyle.u16_le`                  | -                                      | `int/Integer`    |
| `@Preset.RustStyle.i32`                     | -                                      | `int/Integer`    |
| `@Preset.RustStyle.i32_le`                  | -                                      | `int/Integer`    |
| `@Preset.RustStyle.u32`                     | `@Preset.JtStyle.Dword`                | `long/Long`      |
| `@Preset.RustStyle.u32_le`                  | -                                      | `long/Long`      |
| `@Preset.RustStyle.str(charset="GBK")`      | `@Preset.JtStyle.Str`                  | `String`         |
| `@Preset.RustStyle.str(charset="bcd_8421")` | `@Preset.JtStyle.Bcd`                  | `String`         |
| `@Preset.RustStyle.str(charset="UTF-8")`    | `@Preset.JtStyle.Str(charset="UTF-8")` | `String`         |
| `@Preset.RustStyle.str(charset="xxx")`      | `@Preset.JtStyle.Str(charset="xxx")`   | `String`         |
| `@Preset.RustStyle.struct`                  | `@Preset.JtStyle.Object`               | 自定义实体类           |
| `@Preset.RustStyle.list`                    | `@Preset.JtStyle.List`                 | `java.util.List` |
| `@Preset.RustStyle.dyn`                     | `@Preset.JtStyle.RuntimeType`          | `Object`         |

### **`Rust风格`** **VS `@XtreamField`风格**

| **Rust** 风格注解                               | **@XtreamField** 风格注解                           | JavaType         |
|---------------------------------------------|-------------------------------------------------|------------------|
| `@Preset.RustStyle.i8`                      | `@XtreamField(length = 1)`                      | `byte/Byte`      |
| `@Preset.RustStyle.u8`                      | `@XtreamField(length = 1)`                      | `short/Short`    |
| `@Preset.RustStyle.i16`                     | `@XtreamField(length = 2)`                      | `short/Short`    |
| `@Preset.RustStyle.i16_le`                  | `@XtreamField(length = 2, littleEndian = true)` | `short/Short`    |
| `@Preset.RustStyle.u16`                     | `@XtreamField(length = 2)`                      | `int/Integer`    |
| `@Preset.RustStyle.u16_le`                  | `@XtreamField(length = 2, littleEndian = true)` | `int/Integer`    |
| `@Preset.RustStyle.i32`                     | `@XtreamField(length = 4)`                      | `int/Integer`    |
| `@Preset.RustStyle.i32_le`                  | `@XtreamField(length = 4, littleEndian = true)` | `int/Integer`    |
| `@Preset.RustStyle.u32`                     | `@XtreamField(length = 4)`                      | `long/Long`      |
| `@Preset.RustStyle.u32_le`                  | `@XtreamField(length = 4, littleEndian = true)` | `long/Long`      |
| `@Preset.RustStyle.str(charset="UTF-8")`    | `@XtreamField(charset="UTF-8")`                 | `String`         |
| `@Preset.RustStyle.str(charset="GBK")`      | `@XtreamField(charset="GBK")`                   | `String`         |
| `@Preset.RustStyle.str(charset="bcd_8421")` | `@XtreamField(charset="bcd_8421")`              | `String`         |
| `@Preset.RustStyle.str(charset="xxx")`      | `@XtreamField(charset="xxx")`                   | `String`         |
| `@Preset.RustStyle.struct`                  | `@XtreamField(dataType=struct)`                 | 自定义实体类           |
| `@Preset.RustStyle.list`                    | `@XtreamField(dataType=sequence)`               | `java.util.List` |
| `@Preset.RustStyle.dyn`                     | `@XtreamField(dataType=dynamic)`                | `Object`         |
