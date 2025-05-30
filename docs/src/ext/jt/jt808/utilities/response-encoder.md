---
icon: hammer
article: false
---

# 808报文构建器

这里介绍的是 `Jt808ResponseEncoder`，可以用来手动构建报文。

下面以 `0x8001` 消息为例，简单介绍下如何使用:

```java{35}
class Jt808ResponseEncoderTest {

    // 如果是 Spring 环境，可以直接注入 Jt808ResponseEncoder
    Jt808ResponseEncoder responseEncoder;

    @BeforeEach
    void setUp() {
        final ByteBufAllocator byteBufAllocator = ByteBufAllocator.DEFAULT;
        this.responseEncoder = new DefaultJt808ResponseEncoder(
                byteBufAllocator,
                // 流水号生成器(每次都返回0，方便测试)
                increment -> 0,
                EntityCodec.DEFAULT,
                new DefaultJt808BytesProcessor(byteBufAllocator),
                new Jt808MessageEncryptionHandler.NoOps()
        );
    }

    @Test
    void test() {
        final Jt808MessageDescriber describer = new Jt808MessageDescriber(
                // 消息头中的消息ID
                0x8001,
                // 协议版本
                Jt808ProtocolVersion.VERSION_2013,
                // 终端手机号或设备ID
                "013912344323"
        );
        // 可以手动指定流水号，默认使用 DefaultJt808ResponseEncoder.flowIdGenerator 生成流水号
        // describer.flowId(1);
        // 可以手动指定流水号生成器，默认使用 DefaultJt808ResponseEncoder.flowIdGenerator
        // describer.flowIdGenerator(increment -> 1);

        final BuiltinMessage8001 entity = createEntity();
        final ByteBuf buffer = responseEncoder.encode(entity, describer);
        try {
            final String hexString = FormatUtils.toHexString(buffer);
            assertEquals("7e800100050139123443230000007b020000837e", hexString);
        } finally {
            XtreamBytes.releaseBuf(buffer);
        }
    }

    private static BuiltinMessage8001 createEntity() {
        return new BuiltinMessage8001()
                // 应答流水号; 对应终端消息的流水号
                .setClientFlowId(123)
                // 应答id; 对应终端消息的ID
                .setClientMessageId(0x0200)
                // 结果; 0:成功/确认; 1:失败; 2:消息有误; 3:不支持; 4:报警处理确认
                .setResult((short) 0);
    }

}
```
