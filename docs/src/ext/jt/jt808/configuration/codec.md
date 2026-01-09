---
icon: square-binary
article: false
---

# 编解码器

## 说明 <Badge text="0.4.0" type="tip" vertical="middle" /> <Badge text="Experimental" type="danger" vertical="middle" />

这里指的是 **xtream-codec-core** 模块的 [EntityCodec](/guide/core/annotation-driven/entity-codec.md) 的配置。

## 表达式引擎

```yaml {3-6}
xtream:
  codec:
    # 表达式引擎(v0.4.0)
    expression:
      # 取值: spel(默认) | mvel | aviator
      type: spel
```

::: tip MVEL 依赖

如果 `xtream.codec.expression.type=mvel`，请手动将 `mvel2` 的依赖添加到你的项目中。

```xml

<dependency>
    <groupId>org.mvel</groupId>
    <artifactId>mvel2</artifactId>
    <version>2.5.2.Final</version>
</dependency>
```

::: 

::: tip Aviator 依赖

如果 `xtream.codec.expression.type=aviator`，请手动将 `aviator` 的依赖添加到你的项目中。

```xml

<dependency>
    <groupId>com.googlecode.aviator</groupId>
    <artifactId>aviator</artifactId>
    <version>5.4.3</version>
</dependency>
```

:::
