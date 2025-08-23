---
article: false
icon: rocket
tag:
  - Quick-Start
---

# 快速开始

## 引入依赖

最新版本请点击
[![Maven Central](https://img.shields.io/maven-central/v/io.github.hylexus.xtream/xtream-codec-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.hylexus.xtream%22%20AND%20a:%22xtream-codec-core%22)
查看。

::: tabs#starter

@tab Maven

```xml

<dependency>
    <groupId>io.github.hylexus.xtream</groupId>
    <artifactId>xtream-codec-core</artifactId>
    <version>0.0.1-rc.7</version>
</dependency>
```

@tab Gradle

```groovy

api("io.github.hylexus.xtream:xtream-codec-core:0.0.1-rc.7")
```

:::

## 简单示例

这里将以解析 `JT/T 808` 为例，演示如何使用 `xtream-codec` 进行私有协议解析。

### 定义实体类

```java

@Data
public class DebugMsg04ForDecode {

    // byte[0,2) 消息ID WORD
    @Preset.JtStyle.Word
    private int msgId;

    // byte[2,4) 消息体属性 WORD
    @Preset.JtStyle.Word
    private int msgBodyProperty;

    // byte[4,5) 协议版本号 BYTE
    @Preset.JtStyle.Byte
    private byte version;

    // byte[5,15) 终端手机号 BCD[10]
    @Preset.JtStyle.Bcd(length = 10)
    private String terminalPhoneNumber;

    // byte[15,17) 消息流水号 WORD
    @Preset.JtStyle.Word
    private int flowId;

    // 消息包封装项: 消息体属性中第 13 位为 1 时才有该属性
    // byte[17,19) 消息总包数 WORD
    @Preset.JtStyle.Word(condition = "hasSubPackage()")
    private Integer totalPackage;

    // 消息包封装项: 消息体属性中第 13 位为 1 时才有该属性
    // byte[19,21) 包序号 WORD
    @Preset.JtStyle.Word(condition = "hasSubPackage()")
    private Integer currentPackageNo;

    // bit[13] 0010,0000,0000,0000(2000)(是否有子包)
    public boolean hasSubPackage() {
        // return ((msgBodyProperty & 0x2000) >> 13) == 1;
        return (msgBodyProperty & 0x2000) > 0;
    }

    // body-byte[0,4) 报警标志 DWORD
    @Preset.JtStyle.Dword
    private long alarmIdentifier;

    // body-byte[4,8) 状态 DWORD
    @Preset.JtStyle.Dword
    private long status;

    // body-byte[8,12) 纬度 DWORD
    @Preset.JtStyle.Dword
    private long latitude;

    // body-byte[12,16) 经度 DWORD
    @Preset.JtStyle.Dword
    private long longitude;

    // body-byte[16,18) 高程 WORD
    @Preset.JtStyle.Word
    private int height;

    // body-byte[18,20) 速度 WORD
    @Preset.JtStyle.Word
    private int speed;

    // body-byte[20,22) 方向 WORD
    @Preset.JtStyle.Word
    private int direction;

    // body-byte[22,28) 时间 BCD[6]
    @Preset.JtStyle.Bcd(length = 6)
    private String time;
    // 或者 @Preset.JtStyle.BcdDateTime
    // 或者 @XtreamDateTimeField(pattern = "yyMMddHHmmss", length = 6, charset = "bcd_8421")
    // private LocalDateTime time;

    // bit[0-9] 0000,0011,1111,1111(3FF)(消息体长度)
    public int msgBodyLength() {
        return msgBodyProperty & 0x3ff;
    }

    @Preset.JtStyle.List(lengthExpression = "extraItemByteCount()")
    private List<ExtraItem> extraItemList;

    // 检验码
    @Preset.JtStyle.Byte
    private short checkSum;

    @Data
    public static class ExtraItem {

        @Preset.JtStyle.Byte
        private short id;

        @Preset.JtStyle.Byte
        private short length;

        @Preset.JtStyle.Bytes(lengthExpression = "getLength()")
        private byte[] content;
    }

    public int msgBodyStartIndex(boolean hasSubPackage) {
        // 2019
        return hasSubPackage ? 21 : 17;
    }

    public int extraItemByteCount() {
        final int msgBodyStartIndex = msgBodyStartIndex(hasSubPackage());
        return msgBodyLength() - 28;
    }
}
```

### 编解码

```java
public class Jt808CodecTest {
    final EntityCodec entityCodec = new EntityCodec();

    @Test
    void test() {
        String hexString = "0200402D01000000000139123443210000003000000040000101CD41C2072901B00929029A005A23042821314101040000029B020200430302030930016389";
        final DebugMsg04ForDecode entity = doDecode(DebugMsg04ForDecode.class, hexString);
        System.out.println(entity);
    }

    <T> T doDecode(Class<T> tClass, String hexString) {
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer().writeBytes(XtreamBytes.decodeHex(hexString));
        try {
            return entityCodec.decode(tClass, buffer);
        } finally {
            buffer.release();
        }
    }
}
```
