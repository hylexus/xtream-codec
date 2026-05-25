@Setter
@Getter
@ToString
public class RustStyleDebugEntity01Flatten {
    /// ///////////////////// header
    // 固定为 0x80901234
    @Preset.RustStyle.i32
    private int magicNumber;

    // 主版本号 无符号数 1字节
    @Preset.RustStyle.u8
    private short majorVersion;
    // 次版本号 无符号数 1字节

    @Preset.RustStyle.u8
    private short minorVersion;

    // 消息类型 无符号数 2字节
    @Preset.RustStyle.u16
    private int msgType;

    // 消息体长度 无符号数 2字节
    @Preset.RustStyle.u16
    private int msgBodyLength;

    /// ///////////////////// body
    // 下一个字段(username)长度 无符号数 2字节
    @Preset.RustStyle.u16
    private int usernameLength;

    // 用户名 String, "UTF-8"
    // @Preset.RustStyle.str(lengthExpression = "getUsernameLength()")
    @Preset.RustStyle.str(lengthExpressions = @Expression(spel = "getUsernameLength()",mvel = "self.getUsernameLength()",aviator = "self.usernameLength"))
    private String username;

    // 下一个字段(password)长度 无符号数 2字节
    @Preset.RustStyle.u16
    private int passwordLength;

    // 密码 String, "GBK"
    // @Preset.RustStyle.str(charset = XtreamConstants.CHARSET_NAME_GBK, lengthExpression = "getPasswordLength()")
    @Preset.RustStyle.str(charset = XtreamConstants.CHARSET_NAME_GBK, lengthExpressions = @Expression(spel = "getPasswordLength()",mvel = "self.getPasswordLength()",aviator = "self.passwordLength"))
    private String password;

    // 生日 String[8], "yyyyMMdd", "UTF-8" 8字节
    @Preset.RustStyle.str(length = 8)
    private String birthday;

    // 手机号 BCD_8421[6] 6字节(12个数字)
    @Preset.RustStyle.str(charset = XtreamConstants.CHARSET_NAME_BCD_8421, length = 6)
    private String phoneNumber;

    // 年龄 无符号数 2字节
    @Preset.RustStyle.u16
    private int age;

    // 状态 有符号数 2字节
    @Preset.RustStyle.i16
    private short status;
}
