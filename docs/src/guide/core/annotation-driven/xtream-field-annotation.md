---
date: 2024-03-10
#icon: at
tag:
  - 内置
  - 注解
---

# @XtreamField

## 介绍

`@XtreamField` 是最基础的注解。

内置的 **别名(Alias)** 注解 `@Preset.RustStyle.xxx` 和 `@Preset.JtStyle.xxx` 都是基于 `@XtreamField` 这个基础注解 **衍生** 的。

::: tip

注解别名都是依赖于 `Spring` 的 `@org.springframework.core.annotation.AliasFor` 注解实现的。

:::

## 属性说明

```java
@Documented
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface XtreamField {

    /**
     * 指定被当前注解标记的属性的类型: 基础类型、嵌套类型、List 类型。
     * <p>
     * 该属性主要用来确定 {@link FieldCodec}。
     */
    BeanPropertyMetadata.FiledDataType dataType() default BeanPropertyMetadata.FiledDataType.unknown;

    /**
     * 序列化/反序列化时当前属性的顺序。
     *
     * @see <a href="https://stackoverflow.com/questions/5001172/java-reflection-getting-fields-and-methods-in-declaration-order">java-reflection-getting-fields-and-methods-in-declaration-order</a>
     */
    int order() default -1;

    /**
     * 反序列化时当前属性的长度。
     *
     * @see #lengthExpression()
     */
    int length() default -1;

    /**
     * 反序列化时当前属性的长度。当长度无法直接确定时，可以指定一个表达式来确定长度。
     * <p>
     * 目前仅仅支持 <a href="https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/expressions.html">SpEL</a> 语法。
     *
     * @see #length()
     * @see <a href="https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/expressions.html">SpEL 官方文档</a>
     */
    String lengthExpression() default "";

    /**
     * 只有 {@link String} 类型用到
     */
    String charset() default XtreamConstants.CHARSET_NAME_GBK;

    /**
     * 只有 {@link String} 类型 <strong>序列化</strong> 时用到
     *
     * @since @since 0.0.1-rc.6
     */
    Padding paddingRight() default @Padding(minEncodedLength = 0);

    /**
     * 只有 {@link String} 类型 <strong>序列化</strong> 时用到
     *
     * @since @since 0.0.1-rc.6
     */
    Padding paddingLeft() default @Padding(minEncodedLength = 0);

    /**
     * 编码时: 自动给当前字段前面写入 N 字节, 表示当前字段的长度
     * <p>
     * 解码时: 自动读取 N 字节, 作为当前字段的长度
     *
     * @see #prependLengthFieldLength()
     */
    PrependLengthFieldType prependLengthFieldType() default PrependLengthFieldType.none;

    /**
     * 含义和 {@link #prependLengthFieldType()} 相同；
     * <p>
     * 取值只支持 {@code 1}、{@code 2}、{@code 4}
     *
     * @see #prependLengthFieldType()
     */
    int prependLengthFieldLength() default -1;

    /**
     * 是否是小端序。
     *
     * @see XtreamTypes#isNumberType(Class)
     */
    boolean littleEndian() default false;

    /**
     * List 类型的最大迭代次数
     * <li>反序列化才会用到；序列化用不到</li>
     * <li>只有 {@link java.util.List} 类型有效</li>
     */
    int iterationTimes() default -1;

    /**
     * List 类型的最大迭代次数表达式
     * <p>
     * 目前仅仅支持 <a href="https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/expressions.html">SpEL</a> 语法。
     *
     * <li>反序列化才会用到；序列化用不到</li>
     * <li>只有 {@link java.util.List} 类型有效</li>
     *
     * @see #iterationTimes()
     * @see <a href="https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/expressions.html">SpEL 官方文档</a>
     */
    String iterationTimesExpression() default "";

    /**
     * 当且仅当 {@code condition} 为 {@code true} 时，当前属性才会被序列化/反序列化。
     *
     * @see <a href="https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/expressions.html">SpEL 官方文档</a>
     */
    String condition() default "";

    /**
     * 当前属性序列化/反序列化时用到的 {@link FieldCodec}。
     * <p>
     * 内置类型可以自动检测，自定义类型需要指定该属性。
     *
     * @see FieldCodec
     */
    Class<? extends FieldCodec<?>> fieldCodec() default FieldCodec.Placeholder.class;

    /**
     * 只有 {@link java.util.Map} 和 {@link java.util.List} 类型用到
     *
     * @see BeanPropertyMetadata.FiledDataType#sequence
     * @see io.github.hylexus.xtream.codec.common.bean.impl.SequenceBeanPropertyMetadata
     * @see BeanPropertyMetadata.FiledDataType#map
     * @see io.github.hylexus.xtream.codec.common.bean.impl.MapBeanPropertyMetadata
     */
    Class<? extends ContainerInstanceFactory> containerInstanceFactory() default ContainerInstanceFactory.PlaceholderContainerInstanceFactory.class;

    /**
     * 描述字段；和 jt-framework 保持一致，没有特殊作用。
     */
    String desc() default "";

}
```

## 扩展你自己的注解

参考 [自定义注解](custom-annotation.md) 

