@Setter
@Getter
@ToString
public class RawStyleDebugEntity01Flatten {
    /// ///////////////////// header
    // 固定为 0x80901234
    @XtreamField(length = 4, signedness = NumberSignedness.SIGNED)
    private int magicNumber = 0x80901234;

    // 主版本号 无符号数 1字节
    @XtreamField(length = 1, signedness = NumberSignedness.UNSIGNED)
    private short majorVersion;

    // 次版本号 无符号数 1字节
    @XtreamField(length = 1, signedness = NumberSignedness.UNSIGNED)
    private short minorVersion;

    // 消息类型 无符号数 2字节
    @XtreamField(length = 2, signedness = NumberSignedness.UNSIGNED)
    private int msgType;

    // 消息体长度 无符号数 2字节
    @XtreamField(length = 2, signedness = NumberSignedness.UNSIGNED)
    private int msgBodyLength;

    /// ///////////////////// body
    // 下一个字段长度 无符号数 2字节
    @XtreamField(length = 2, signedness = NumberSignedness.UNSIGNED)
    private int usernameLength;

    // 用户名 String, "UTF-8"
    // @XtreamField(charset = "utf-8", lengthExpression = "getUsernameLength()")
    @XtreamField(charset = "utf-8", lengthExpressions = @Expression(spel = "getUsernameLength()", mvel = "self.getUsernameLength()", aviator = "self.usernameLength"))
    private String username;

    // 下一个字段长度 无符号数 2字节
    @XtreamField(length = 2, signedness = NumberSignedness.UNSIGNED)
    private int passwordLength;

    // 密码 String, "GBK"
    // @XtreamField(charset = XtreamConstants.CHARSET_NAME_GBK, lengthExpression = "getPasswordLength()")
    @XtreamField(charset = XtreamConstants.CHARSET_NAME_GBK, lengthExpressions = @Expression(spel = "getPasswordLength()", mvel = "self.getPasswordLength()", aviator = "self.passwordLength"))
    private String password;

    // 生日 String[8], "yyyyMMdd", "UTF-8"
    @XtreamField(charset = "UTF-8", length = 8)
    private String birthday;

    // 手机号 BCD_8421[6] "GBK"
    @XtreamField(charset = XtreamConstants.CHARSET_NAME_BCD_8421, length = 6)
    private String phoneNumber;

    // 年龄 无符号数 2字节
    @XtreamField(length = 2, signedness = NumberSignedness.UNSIGNED)
    private int age;

    // 状态 有符号数 2字节
    @XtreamField(length = 2, signedness = NumberSignedness.SIGNED)
    private short status;
}
