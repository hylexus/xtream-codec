@Setter
@Getter
@ToString
public class RawStyleDebugEntity01ForDecode {
    //////////////////////// header
    // 固定为 0x80901234
    @XtreamField(length = 4)
    private int magicNumber = 0x80901234;

    // 主版本号 无符号数 1字节
    @XtreamField(length = 1)
    private short majorVersion;

    // 次版本号 无符号数 1字节
    @XtreamField(length = 1)
    private short minorVersion;

    // 消息类型 无符号数 2字节
    @XtreamField(length = 2)
    private int msgType;

    // 消息体长度 无符号数 2字节
    @XtreamField(length = 2)
    private int msgBodyLength;

    //////////////////////// body
    // 下一个字段长度 无符号数 2字节
    @XtreamField(length = 2)
    private int usernameLength;

    // 用户名 String, "UTF-8"
    @XtreamField(charset = "utf-8", lengthExpression = "getUsernameLength()")
    private String username;

    // 下一个字段长度 无符号数 2字节
    @XtreamField(length = 2)
    private int passwordLength;

    // 密码 String, "GBK"
    @XtreamField(charset = "GBK", lengthExpression = "getPasswordLength()")
    private String password;

    // 生日 String[8], "yyyyMMdd", "UTF-8"
    @XtreamField(charset = "UTF-8", length = 8)
    private String birthday;

    // 手机号 BCD_8421[6] "GBK"
    @XtreamField(charset = "bcd_8421", length = 6)
    private String phoneNumber;

    // 年龄 无符号数 2字节
    @XtreamField(length = 2)
    private int age;

    // 状态 有符号数 2字节
    @XtreamField(length = 2)
    private short status;
}
