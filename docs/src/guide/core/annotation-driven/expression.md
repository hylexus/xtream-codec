---
date: 2026-01-17
icon: fa-solid fa-square-root-variable
tag:
  - 注解
  - 表达式
  - SpEL
  - MVEL
  - Aviator
---

# 表达式注解

## 介绍 <Badge text="0.4.0" type="tip" vertical="top"/> <Badge text="Experimental" type="danger" vertical="top" />

在 <Badge text="0.4.0" type="tip" vertical="middle"/> 之前的版本中，无论是 内置注解 还是 自定义注解，**表达式** 都是通过 `SpEL` 来实现的。

从 <Badge text="0.4.0" type="tip" vertical="middle"/> 开始，**xtream-codec** 新增了 `@Expression` 注解。支持三种表达式：

- [SpEL（默认）](https://docs.spring.io/spring-framework/reference/core/expressions.html)
- [MVEL](http://mvel.documentnode.com/)
- [Aviator](https://github.com/killme2008/aviatorscript)

::: warning

建议你读一下 [注意事项](#注意事项) 部分。

:::

## 表达式类型切换

::: tip 可以通过下面的配置来指定表达式类型

```yaml {4-5}
xtream:
  codec:
    expression:
      # 取值 spel | mvel | aviator | custom
      type: spel
```

:::

## Expression 注解定义

::: details 点击查看 `@Expression` 详细信息

```java
public @interface Expression {

    /**
     * @see io.github.hylexus.xtream.codec.base.expression.SpelXtreamExpressionEngine
     * @see io.github.hylexus.xtream.codec.base.expression.SpelXtreamExpressionEngine.SpelXtreamExpression
     * @see io.github.hylexus.xtream.codec.base.expression.SpelXtreamExpressionEngine.SpelXtreamEvaluationContext
     */
    String spel() default "";

    /**
     * @see io.github.hylexus.xtream.codec.base.expression.MvelXtreamExpressionEngine
     * @see io.github.hylexus.xtream.codec.base.expression.MvelXtreamExpressionEngine.MvelXtreamExpression
     * @see io.github.hylexus.xtream.codec.base.expression.MvelXtreamExpressionEngine.MvelXtreamEvaluationContext
     */
    String mvel() default "";

    /**
     * @see io.github.hylexus.xtream.codec.base.expression.AviatorXtreamExpressionEngine
     * @see io.github.hylexus.xtream.codec.base.expression.AviatorXtreamExpressionEngine.AviatorXtreamExpression
     * @see io.github.hylexus.xtream.codec.base.expression.AviatorXtreamExpressionEngine.AviatorXtreamEvaluationContext
     */
    String aviator() default "";

    /**
     * @see io.github.hylexus.xtream.codec.base.expression.CustomXtreamExpressionEngine
     */
    String custom() default "";
}

```

:::

## 用途

可以 直接 或 间接 地将 `@Expression` 注解传递给 `@XtreamField` 注解的以下属性：

- `XtreamField.lengthExpressions()`
- `XtreamField.conditions()`
- `XtreamField.iterationTimesExpressions()`

::: tip 提示

自定义注解的属性最终都会合并到 `@XtreamField` 注解中。比如下面的注解：

```java

// @Preset.JtStyle.Word(condition = "hasThresholdProperty()", desc = "路段行驶过长阈值")
@Preset.JtStyle.Word(conditions = @Expression(spel = "hasThresholdProperty()", mvel = "self.hasThresholdProperty()", aviator = "self.hasThresholdProperty"), desc = "路段行驶过长阈值")
private Integer longDriveThreshold;
```

- `@Preset.JtStyle.Word.conditions()` 会被合并到 `@XtreamField.conditions()` 注解中。
- `@Preset.JtStyle.Word.condition()` 会被合并到 `@XtreamField.condition()` 注解中。

自定义注解也是一样的逻辑。

:::

### XtreamField 底层注解

`@XtreamField` 底层注解使用比较繁琐，不建议直接使用(自定义注解除外)。

建议使用 **别名注解**: `@Preset.RustStyle.XXX`、`@Preset.JtStyle.XXX` 或者 自定义注解。

| 属性                                         | 版本                                                  | 备注                         |
|--------------------------------------------|-----------------------------------------------------|----------------------------|
| `@XtreamField.lengthExpression()`          | 0.0.1+                                              | 默认支持 `SpEL` 表达式            |
| `@XtreamField.lengthExpressions()`         | <Badge text="0.4.0+" type="tip" vertical="middle"/> | 支持 `SpEL`、`MVEL`、`Aviator` |
| `@XtreamField.condition()`                 | 0.0.1+                                              | 默认支持 `SpEL` 表达式            |
| `@XtreamField.conditions()`                | <Badge text="0.4.0+" type="tip" vertical="middle"/> | 支持 `SpEL`、`MVEL`、`Aviator` |
| `@XtreamField.iterationTimesExpression()`  | 0.0.1+                                              | 默认支持 `SpEL` 表达式            |
| `@XtreamField.iterationTimesExpressions()` | <Badge text="0.4.0+" type="tip" vertical="middle"/> | 支持 `SpEL`、`MVEL`、`Aviator` |

### Rust 风格注解

| 属性                                                  | 版本                                                  | 备注                         |
|-----------------------------------------------------|-----------------------------------------------------|----------------------------|
| `@Preset.RustStyle.XXX.lengthExpression()`          | 0.0.1+                                              | 默认支持 `SpEL` 表达式            |
| `@Preset.RustStyle.XXX.lengthExpressions()`         | <Badge text="0.0.4+" type="tip" vertical="middle"/> | 支持 `SpEL`、`MVEL`、`Aviator` |
| `@Preset.RustStyle.XXX.condition()`                 | 0.0.1+                                              | 默认支持 `SpEL` 表达式            |
| `@Preset.RustStyle.XXX.conditions()`                | <Badge text="0.4.0+" type="tip" vertical="middle"/> | 支持 `SpEL`、`MVEL`、`Aviator` |
| `@Preset.RustStyle.XXX.iterationTimesExpression()`  | 0.0.1+                                              | 默认支持 `SpEL` 表达式            |
| `@Preset.RustStyle.XXX.iterationTimesExpressions()` | <Badge text="0.4.0+" type="tip" vertical="middle"/> | 支持 `SpEL`、`MVEL`、`Aviator` |

### JT 风格注解

| 属性                                                | 版本                                                  | 备注                         |
|---------------------------------------------------|-----------------------------------------------------|----------------------------|
| `@Preset.JtStyle.XXX.lengthExpression()`          | 0.0.1+                                              | 默认支持 `SpEL` 表达式            |
| `@Preset.JtStyle.XXX.lengthExpressions()`         | <Badge text="0.0.4+" type="tip" vertical="middle"/> | 支持 `SpEL`、`MVEL`、`Aviator` |
| `@Preset.JtStyle.XXX.condition()`                 | 0.0.1+                                              | 默认支持 `SpEL` 表达式            |
| `@Preset.JtStyle.XXX.conditions()`                | <Badge text="0.4.0+" type="tip" vertical="middle"/> | 支持 `SpEL`、`MVEL`、`Aviator` |
| `@Preset.JtStyle.XXX.iterationTimesExpression()`  | 0.0.1+                                              | 默认支持 `SpEL` 表达式            |
| `@Preset.JtStyle.XXX.iterationTimesExpressions()` | <Badge text="0.4.0+" type="tip" vertical="middle"/> | 支持 `SpEL`、`MVEL`、`Aviator` |

## 示例

```java {21-28}

@Jt808ResponseBody(messageId = 0x8800, desc = "多媒体数据上传应答")
public class BuiltinMessage8800 {
    /**
     * 多媒体 ID
     * <p>
     * > 0，如收到全部数据包则没有后续字段
     */
    @Preset.JtStyle.Dword(desc = "多媒体 ID")
    private long multimediaId;

    @Preset.JtStyle.Byte(desc = "重传包总数")
    private short retransmittedPackageCount;

    /**
     * 重传包 ID 列表
     * <p>
     * 重传包序号顺序排列，如“包 ID1 包 ID2......包IDn”。
     */
    // @Preset.JtStyle.Bytes(lengthExpression = "2 * getRetransmittedPackageCount()", desc = "重传包 ID 列表")
    @Preset.JtStyle.Bytes(
            lengthExpressions = @Expression(
                    spel = "2 * getRetransmittedPackageCount()",
                    mvel = "2 * self.getRetransmittedPackageCount()",
                    aviator = "2 * self.retransmittedPackageCount"
            ),
            desc = "重传包 ID 列表"
    )
    @JsonSerialize(using = XtreamCodecDebugJsonSerializer.class)
    private byte[] retransmittedPackageIdList;

    public short getRetransmittedPackageCount() {
        return retransmittedPackageCount;
    }
}
```

## 注意事项

### 优先级

优先读取 <span style="color:red;">不带 s</span> 的表达式属性：

- `@XtreamField.lengthExpression()`
- `@XtreamField.condition()`
- `@XtreamField.iterationTimesExpression()`
- `@Preset.RustStyle.XXX.lengthExpression()`
- `@Preset.RustStyle.XXX.condition()`
- `@Preset.RustStyle.XXX.iterationTimesExpression()`
- `@Preset.JtStyle.XXX.lengthExpression()`
- `@Preset.JtStyle.XXX.condition()`
- `@Preset.JtStyle.XXX.iterationTimesExpression()`

如果没配置 <span style="color:red;">不带 s</span> 的表达式属性，则读取以 <span style="color:red;">以 s 结尾</span>  的表达式属性：

- `@XtreamField.lengthExpressions()`
- `@XtreamField.conditions()`
- `@XtreamField.iterationTimesExpressions()`
- `@Preset.RustStyle.XXX.lengthExpressions()`
- `@Preset.RustStyle.XXX.conditions()`
- `@Preset.RustStyle.XXX.iterationTimesExpressions()`
- `@Preset.JtStyle.XXX.lengthExpressions()`
- `@Preset.JtStyle.XXX.conditions()`
- `@Preset.JtStyle.XXX.iterationTimesExpressions()`

### 使用建议

应该将表达式视为 **逻辑路由器**，而非 **逻辑处理器**。

- 表达式应该仅用于调用 **无副作用** 的方法或访问属性。
- 业务逻辑应尽量封装在 Java 方法中，而不是直接写在表达式里。
- 不建议在表达式中编写 **复杂运算逻辑、条件或流程控制**。
- 若表达式逻辑开始变复杂，应回退为 Java 方法。
- 表达式的 **可读性** 优先于 **灵活性**。

### 安全说明

- 表达式应仅来自 **硬编码的注解属性**，不允许通过外部配置或用户输入注入。
- 表达式是否安全，和表达式引擎无关，**取决于表达式本身**(表达式引擎不保证表达式的执行是否安全可控)
- 使用方应遵守 **“无副作用、简单可读”** 的原则，避免在表达式中写业务逻辑或复杂操作。
