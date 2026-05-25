@Setter
@Getter
@ToString
public class JtStyleDebugEntity01Flatten {
    /// ///////////////////// header
    // 固定为 0x80901234
    @Preset.JtStyle.Dword
    private long magicNumber = 0x80901234L;

    // 主版本号 无符号数 1字节
    @Preset.JtStyle.Byte
    private short majorVersion;
    // 次版本号 无符号数 1字节

    @Preset.JtStyle.Byte
    private short minorVersion;

    // 消息类型 无符号数 2字节
    @Preset.JtStyle.Word
    private int msgType;

    // 消息体长度 无符号数 2字节
    @Preset.JtStyle.Word
    private int msgBodyLength;

    /// ///////////////////// body
    // 下一个字段长度 无符号数 2字节
    @Preset.JtStyle.Word
    private int usernameLength;

    // 用户名 String, "UTF-8"
    // @Preset.JtStyle.Str(charset = "UTF-8", lengthExpression = "getUsernameLength()")
    @Preset.JtStyle.Str(charset = "UTF-8", lengthExpressions = @Expression(spel = "getUsernameLength()",mvel = "self.getUsernameLength()",aviator = "self.usernameLength"))
    private String username;

    // 下一个字段长度 无符号数 2字节
    @Preset.JtStyle.Word
    private int passwordLength;

    // 密码 String, "GBK"
    // @Preset.JtStyle.Str(charset = XtreamConstants.CHARSET_NAME_GBK, lengthExpression = "getPasswordLength()")
    @Preset.JtStyle.Str(charset = XtreamConstants.CHARSET_NAME_GBK, lengthExpressions = @Expression(spel = "getPasswordLength()",mvel = "self.getPasswordLength()",aviator = "self.passwordLength"))
    private String password;

    // 生日 String[8], "yyyyMMdd", "UTF-8"
    @Preset.JtStyle.Str(charset = "UTF-8", length = 8)
    private String birthday;

    // 手机号 BCD_8421[6]
    @Preset.JtStyle.Bcd(length = 6)
    private String phoneNumber;

    // 年龄 无符号数 2字节
    @Preset.JtStyle.Word
    private int age;

    // 状态 无符号数 2字节
    @Preset.JtStyle.Word
    private int status;
}
