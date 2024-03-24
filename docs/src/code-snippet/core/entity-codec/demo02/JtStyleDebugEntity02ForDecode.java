@Setter
@Getter
@ToString
public class JtStyleDebugEntity02ForDecode {

    // region 消息头
    // byte[0-2) 	消息ID word(16)
    @Preset.JtStyle.Word
    private int msgId;

    // byte[2-4) 	消息体属性 word(16)
    @Preset.JtStyle.Word
    private int msgBodyProps;

    // byte[4]     协议版本号
    @Preset.JtStyle.Byte
    private byte protocolVersion;

    // byte[5-15) 	终端手机号或设备ID bcd[10]
    @Preset.JtStyle.BCD(length = 10)
    private String terminalId;

    // byte[15-17) 	消息流水号 word(16)
    @Preset.JtStyle.Word
    private int msgSerialNo;

    // byte[17-21) 	消息包封装项
    @Preset.JtStyle.Dword(condition = "hasSubPackage()")
    private Long subPackageInfo;
    // endregion 消息头

    // region 消息体
    // 报警标志  DWORD(4)
    @Preset.JtStyle.Dword
    private long alarmFlag;

    // 状态  DWORD(4)
    @Preset.JtStyle.Dword
    private long status;

    // 纬度  DWORD(4)
    @Preset.JtStyle.Dword
    private long latitude;

    // 经度  DWORD(4)
    @Preset.JtStyle.Dword
    private long longitude;

    // 高程  WORD(2)
    @Preset.JtStyle.Word
    private int altitude;

    // 高程  WORD(2)
    @Preset.JtStyle.Word
    private int speed;

    // 方向  WORD(2)
    @Preset.JtStyle.Word
    private int direction;

    // 时间  BCD[6] yyMMddHHmmss
    @Preset.JtStyle.BCD(length = 6)
    private String time;

    // 长度：消息体长度减去前面的 28 字节
    @Preset.JtStyle.List(lengthExpression = "msgBodyLength() - 28")
    private List<ExtraItem> extraItems;
    // endregion 消息体

    // 校验码
    @Preset.JtStyle.Byte
    private byte checkSum;

    // bit[0-9] 0000,0011,1111,1111(3FF)(消息体长度)
    public int msgBodyLength() {
        return msgBodyProps & 0x3ff;
    }

    // bit[13] 0010,0000,0000,0000(2000)(是否有子包)
    public boolean hasSubPackage() {
        // return ((msgBodyProperty & 0x2000) >> 13) == 1;
        return (msgBodyProps & 0x2000) > 0;
    }


    @Setter
    @Getter
    @ToString
    public static class ExtraItem {
        // 附加信息ID   BYTE(1~255)
        @Preset.JtStyle.Byte
        private short id;
        // 附加信息长度   BYTE(1~255)
        @Preset.JtStyle.Byte
        private short contentLength;
        // 附加信息内容  BYTE[N]
        @Preset.JtStyle.Bytes(lengthExpression = "getContentLength()")
        private byte[] content;

        public ExtraItem() {
        }

        public ExtraItem(short id, short length, byte[] content) {
            this.id = id;
            this.contentLength = length;
            this.content = content;
        }
    }
}
