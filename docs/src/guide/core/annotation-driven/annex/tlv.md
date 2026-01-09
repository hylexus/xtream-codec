---
article: false
date: 2026-01-03
icon: fa-tags
tag:
  - 内置
  - 注解
  - TLV
---

# TLV 类型

## 介绍 <Badge text="0.4.0" type="tip" vertical="top"/> <Badge text="Experimental" type="danger" vertical="top" />

`TLV（Type-Length-Value）` 是一种广泛应用于二进制协议中的数据编码格式，常用于高效、灵活地序列化结构化数据。

从版本 <Badge text="0.4.0" type="tip" vertical="middle" /> 开始，**xtream-codec** 引入了对 `TLV` 类型的内置支持，
允许开发者通过注解定义 `TLV` 格式的消息体。

## 示例

- [GitHub](https://github.com/hylexus/xtream-codec/blob/develop/ext/jt/jt-808-server-spring-boot-starter-reactive/src/main/java/io/github/hylexus/xtream/codec/ext/jt808/builtin/messages/request/BuiltinMessage0104SampleTLV.java)
- [Gitee](https://gitee.com/hylexus/xtream-codec/blob/develop/ext/jt/jt-808-server-spring-boot-starter-reactive/src/main/java/io/github/hylexus/xtream/codec/ext/jt808/builtin/messages/request/BuiltinMessage0104SampleTLV.java)

```java {27-81}
/**
 * 查询终端参数应答 0x0104
 *
 * @author hylexus
 * @since 0.4.0
 *
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
@ApiStatus.AvailableSince("0.4.0")
@Jt808ResponseBody(messageId = 0x0104, desc = "查询终端参数应答(TLV示例)")
public class BuiltinMessage0104SampleTLV {
    /**
     * 对应的终端参数查询消息的流水号
     */
    @Preset.JtStyle.Word
    private int flowId;

    /**
     * 应答参数个数
     */
    @Preset.JtStyle.Byte
    private short parameterCount;

    @XtreamTLVFieldSequence(
            decoder = @XtreamTLVFieldSequence.Decoder(
                    tag = @Key(type = KeyType.u32),
                    length = @ValueLength(type = LengthFieldType.u8),
                    value = @XtreamTLVFieldSequence.Value(
                            matchers = {
                                    @ValueMatcher(
                                            matchU32 = {
                                                    0x0001L, 0x0002L, 0x0003L, 0x0004L, 0x0005L, 0x0006L, 0x0007L,
                                                    0x001BL, 0x001CL,
                                                    0x0020L, 0x0021L, 0x0022L,
                                                    0x0027L, 0x0028L, 0x0029L,
                                                    0x002CL, 0x002DL, 0x002EL, 0x002FL, 0x0030L,
                                            },
                                            valueType = XtreamDataType.u32
                                    ),
                                    @ValueMatcher(
                                            matchU32 = {
                                                    0x0045L, 0x0046L, 0x0047L,
                                                    0x0050L, 0x0051L, 0x0052L, 0x0053L, 0x0054L, 0x0055L,
                                                    0x0056L, 0x0057L, 0x0058L, 0x0059L, 0x005AL,
                                                    0x0064L, 0x0065L,
                                                    0x0070L, 0x0071L, 0x0072L, 0x0073L, 0x0074L,
                                                    0x0080L, 0x0093L, 0x0095L, 0x0100L, 0x0102L,
                                            },
                                            valueType = XtreamDataType.u32
                                    ),
                                    @ValueMatcher(
                                            matchU32 = {
                                                    0x0011L, 0x0012L, 0x0013L, 0x0014L, 0x0015L, 0x0016L, 0x0017L,
                                                    0x001AL, 0x001DL,
                                                    0x0023L, 0x0024L, 0x0025L, 0x0026L,
                                                    0x0032L,
                                                    0x0040L, 0x0041L, 0x0042L, 0x0043L, 0x0044L,
                                            },
                                            valueType = XtreamDataType.string_gbk
                                    ),
                                    @ValueMatcher(matchU32 = {0x0048L, 0x0049L, 0x0083L,}, valueType = XtreamDataType.string_gbk),
                                    @ValueMatcher(matchU32 = 0x0010L, valueType = XtreamDataType.string_gbk),
                                    @ValueMatcher(matchU32 = 0x0031L, valueType = XtreamDataType.u16),
                                    @ValueMatcher(
                                            matchU32 = {
                                                    0x005BL, 0x005CL, 0x005DL, 0x005EL,
                                                    0x0081L, 0x0082L, 0x0101L, 0x0103L,
                                            },
                                            valueType = XtreamDataType.u16
                                    ),
                                    @ValueMatcher(matchU32 = {0x0084L, 0x0090L, 0x0091L, 0x0092L, 0x0094L,}, valueType = XtreamDataType.u8)
                            },
                            // 其他没匹配到的参数 都解析为 hexString
                            fallbackMatchers = @FallbackValueMatcher(valueType = XtreamDataType.string_hex)
                    )
            )
    )
    private List<TLV> parameterItems;
}
```
