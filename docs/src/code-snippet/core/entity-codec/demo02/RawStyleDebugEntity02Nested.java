@Setter
@Getter
@ToString
public class RawStyleDebugEntity02Nested {

    // 消息头
    @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.struct)
    private Header header;

    // 消息体
    @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.struct, lengthExpression = "header.msgBodyLength()")
    private Body body;

    // 校验码
    @XtreamField(length = 1)
    private byte checkSum;

    @Getter
    @Setter
    @ToString
    public static class Header {
        // byte[0-2)    消息ID word(16)
        @XtreamField(length = 2)
        private int msgId;

        // byte[2-4)    消息体属性 word(16)
        @XtreamField(length = 2)
        private int msgBodyProps;

        // byte[4]     协议版本号
        @XtreamField(length = 1)
        private byte protocolVersion;

        // byte[5-15)    终端手机号或设备ID bcd[10]
        @XtreamField(charset = XtreamConstants.CHARSET_NAME_BCD_8421, length = 10)
        private String terminalId;

        // byte[15-17)    消息流水号 word(16)
        @XtreamField(length = 2)
        private int msgSerialNo;

        // byte[17-21)    消息包封装项
        @XtreamField(length = 4, condition = "hasSubPackage()")
        private Long subPackageInfo;

        // bit[0-9] 0000,0011,1111,1111(3FF)(消息体长度)
        public int msgBodyLength() {
            return msgBodyProps & 0x3ff;
        }

        // bit[13] 0010,0000,0000,0000(2000)(是否有子包)
        public boolean hasSubPackage() {
            // return ((msgBodyProperty & 0x2000) >> 13) == 1;
            return (msgBodyProps & 0x2000) > 0;
        }
    }

    @Getter
    @Setter
    @ToString
    public static class Body {
        // 报警标志  DWORD(4)
        @XtreamField(length = 4)
        private long alarmFlag;

        // 状态  DWORD(4)
        @XtreamField(length = 4)
        private long status;

        // 纬度  DWORD(4)
        @XtreamField(length = 4)
        private long latitude;

        // 经度  DWORD(4)
        @XtreamField(length = 4)
        private long longitude;

        // 高程  WORD(2)
        @XtreamField(length = 2)
        private int altitude;

        // 速度  WORD(2)
        @XtreamField(length = 2)
        private int speed;

        // 方向  WORD(2)
        @XtreamField(length = 2)
        private int direction;

        // 时间  BCD[6] yyMMddHHmmss
        @XtreamField(charset = XtreamConstants.CHARSET_NAME_BCD_8421, length = 6)
        private String time;

        @XtreamField(dataType = BeanPropertyMetadata.FiledDataType.sequence)
        private List<ExtraItem> extraItems;
    }

    @Setter
    @Getter
    @ToString
    public static class ExtraItem {
        // 附加信息ID   BYTE(1~255)
        @XtreamField(length = 1)
        private short id;
        // 附加信息长度   BYTE(1~255)
        @XtreamField(length = 1)
        private short contentLength;
        // 附加信息内容  BYTE[N]
        @XtreamField(lengthExpression = "getContentLength()")
        private byte[] content;

        public ExtraItem() {
        }

        public ExtraItem(short id, short contentLength, byte[] content) {
            this.id = id;
            this.contentLength = contentLength;
            this.content = content;
        }
    }
}
