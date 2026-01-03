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

::: tip

在 `JT/T 808` 协议中:

- 对于数字类型而言，都是无符号数
    - 然而 **Java** 本身没有无符号数的概念
    - 所以在映射到 **Java** 数据类型时，往往需要将数据类型的范围扩大一倍，以避免溢出的风险
        - 以 `BYTE` 类型为例: `JT/T 808` 协议中指的是 **1** 字节的 *无* 符号整数
        - 虽然 **Java** 的 `byte/Byte` 也是 **1** 字节
        - 但是 **Java** 的 `byte/Byte` 是 _有_ 符号数
        - 所以不得不将 `JT/T 808` 的 `BYTE` 映射到 **Java** 的 `short/Short` 以避免溢出的风险
- 对于字符串类型而言，`JT/T 808` 协议都是 "GBK" 编码

所以，`JT/T 808` 风格的注解，可以看做是 `Rust` 风格注解的子集，两者的对应关系参考 [内置类型对比](#内置类型对比)

:::

::: details 点击查看 JT/T 808 官方文档中的数据类型声明

![](/img/core/annotation/jt-808-data-types.png)

:::

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

- `@Preset.RustStyle.Str`
    - 字符串
    - 编码: 默认 `GBK`
    - **JavaType**: `java.lang.String`

### BCD

- `@Preset.RustStyle.Bcd`
    - 字符串
    - 编码固定为: `BCD_8421`
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

- `@Preset.RustStyle.Bytes`
    - **N 字节**
    - **JavaType**: `byte[]`

## 内置类型对比

### **`Rust风格`** **VS `JT/T 808`风格**

::: details 点击查看 JT/T 808 官方文档中的数据类型声明

![](/img/core/annotation/jt-808-data-types.png)

:::

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
