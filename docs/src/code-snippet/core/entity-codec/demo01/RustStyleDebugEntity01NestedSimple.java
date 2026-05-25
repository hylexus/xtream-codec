@Setter
@Getter
@ToString
// 这里使用了 prependLengthFieldType 和 prependLengthFieldLength 属性
// 从而省略了 msgBodyLength, usernameLength 和 passwordLength
public class RustStyleDebugEntity01NestedSimple {

    // 整个 Header 封装到一个实体类中
    @Preset.RustStyle.struct
    private Header header;

    // 整个 Body 封装到一个实体类中
    // prependLengthFieldType: 前面自动添加一个u16(2字节)类型字段作为该字段的长度字段
    @Preset.RustStyle.struct(prependLengthFieldLength = 2)
    private Body body;

    // 下面是 Header 和 Body 实体类的声明
    @Data
    public static class Header {
        // 固定为 0x80901234
        @Preset.RustStyle.i32
        private int magicNumber = 0x80901234;

        // 主版本号 无符号数 1字节
        @Preset.RustStyle.u8
        private short majorVersion;
        // 次版本号 无符号数 1字节

        @Preset.RustStyle.u8
        private short minorVersion;

        // 消息类型 无符号数 2字节
        @Preset.RustStyle.u16
        private int msgType;
    }

    @Data
    public static class Body {

        // 用户名 String, "UTF-8"
        // prependLengthFieldType: 前面自动添加一个u16类型字段作为该字段的长度字段
        @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u16)
        private String username;

        // 密码 String, "GBK"
        // prependLengthFieldType: 前面自动添加一个u16类型(2字节)字段作为该字段的长度字段
        @Preset.RustStyle.str(charset = XtreamConstants.CHARSET_NAME_GBK, prependLengthFieldLength = 2)
        private String password;

        // 生日 String[8], "yyyyMMdd", "UTF-8"
        @Preset.RustStyle.str(length = 8)
        private String birthday;

        // 手机号 BCD_8421[6] "GBK"
        @Preset.RustStyle.str(charset = XtreamConstants.CHARSET_NAME_BCD_8421, length = 6)
        private String phoneNumber;

        // 年龄 无符号数 2字节
        @Preset.RustStyle.u16
        private int age;

        // 状态 有符号数 2字节
        @Preset.RustStyle.i16
        private short status;
    }
}
