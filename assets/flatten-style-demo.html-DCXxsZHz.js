import{_ as p}from"./plugin-vue_export-helper-DlAUqK2U.js";import{r as c,o as u,c as r,b as n,d as s,e as o,w as a}from"./app-Be8ydcID.js";const k={},d=n("h1",{id:"扁平化写法示例",tabindex:"-1"},[n("a",{class:"header-anchor",href:"#扁平化写法示例"},[n("span",null,"扁平化写法示例")])],-1),m={class:"hint-container tip"},b=n("p",{class:"hint-container-title"},"提示",-1),v=n("li",null,[s("所谓的 "),n("em",null,"扁平化写法"),s(" 就是不用单独封装 "),n("code",null,"Header"),s(" 和 "),n("code",null,"Body"),s("，而是直接将 "),n("code",null,"Header"),s(" 和 "),n("code",null,"Body"),s(" 的数据写在同一个实体类中")],-1),y=n("h2",{id:"解码",tabindex:"-1"},[n("a",{class:"header-anchor",href:"#解码"},[n("span",null,"解码")])],-1),w=n("h3",{id:"实体类定义",tabindex:"-1"},[n("a",{class:"header-anchor",href:"#实体类定义"},[n("span",null,"实体类定义")])],-1),g=n("div",{class:"language-java line-numbers-mode","data-ext":"java","data-title":"java"},[n("pre",{class:"language-java"},[n("code",null,[n("span",{class:"token annotation punctuation"},"@Setter"),s(`
`),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
`),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
`),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"RustStyleDebugEntity02ForDecode"),s(),n("span",{class:"token punctuation"},"{"),s(`

    `),n("span",{class:"token comment"},"// region 消息头"),s(`
    `),n("span",{class:"token comment"},"// byte[0-2) 	消息ID word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgId"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[2-4) 	消息体属性 word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgBodyProps"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[4]     协议版本号"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u8"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),s(" protocolVersion"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[5-15) 	终端手机号或设备ID bcd[10]"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.str"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"bcd_8421"'),n("span",{class:"token punctuation"},","),s(" length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"10"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" terminalId"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[15-17) 	消息流水号 word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgSerialNo"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[17-21) 	消息包封装项"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u32"),n("span",{class:"token punctuation"},"("),s("condition "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"hasSubPackage()"'),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Long"),s(" subPackageInfo"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token comment"},"// endregion 消息头"),s(`

    `),n("span",{class:"token comment"},"// region 消息体"),s(`
    `),n("span",{class:"token comment"},"// 报警标志  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u32"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" alarmFlag"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 状态  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u32"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" status"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 纬度  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u32"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" latitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 经度  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u32"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" longitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 高程  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" altitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 高程  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" speed"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 方向  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" direction"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 时间  BCD[6] yyMMddHHmmss"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.str"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"bcd_8421"'),n("span",{class:"token punctuation"},","),s(" length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"6"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" time"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 长度：消息体长度减去前面的 28 字节"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.list"),n("span",{class:"token punctuation"},"("),s("lengthExpression "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"msgBodyLength() - 28"'),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"List"),n("span",{class:"token generics"},[n("span",{class:"token punctuation"},"<"),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},">")]),s(" extraItems"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token comment"},"// endregion 消息体"),s(`

    `),n("span",{class:"token comment"},"// 校验码"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.i8"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),s(" checkSum"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// bit[0-9] 0000,0011,1111,1111(3FF)(消息体长度)"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"int"),s(),n("span",{class:"token function"},"msgBodyLength"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token keyword"},"return"),s(" msgBodyProps "),n("span",{class:"token operator"},"&"),s(),n("span",{class:"token number"},"0x3ff"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`

    `),n("span",{class:"token comment"},"// bit[13] 0010,0000,0000,0000(2000)(是否有子包)"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"boolean"),s(),n("span",{class:"token function"},"hasSubPackage"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// return ((msgBodyProperty & 0x2000) >> 13) == 1;"),s(`
        `),n("span",{class:"token keyword"},"return"),s(),n("span",{class:"token punctuation"},"("),s("msgBodyProps "),n("span",{class:"token operator"},"&"),s(),n("span",{class:"token number"},"0x2000"),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token operator"},">"),s(),n("span",{class:"token number"},"0"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`


    `),n("span",{class:"token annotation punctuation"},"@Setter"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
    `),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"ExtraItem"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 附加信息ID   BYTE(1~255)"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u8"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" id"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token comment"},"// 附加信息长度   BYTE(1~255)"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u8"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" contentLength"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token comment"},"// 附加信息内容  BYTE[N]"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.byte_array"),n("span",{class:"token punctuation"},"("),s("lengthExpression "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"getContentLength()"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),n("span",{class:"token punctuation"},"["),n("span",{class:"token punctuation"},"]"),s(" content"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(`

        `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},"("),n("span",{class:"token keyword"},"short"),s(" id"),n("span",{class:"token punctuation"},","),s(),n("span",{class:"token keyword"},"short"),s(" length"),n("span",{class:"token punctuation"},","),s(),n("span",{class:"token keyword"},"byte"),n("span",{class:"token punctuation"},"["),n("span",{class:"token punctuation"},"]"),s(" content"),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("id "),n("span",{class:"token operator"},"="),s(" id"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("contentLength "),n("span",{class:"token operator"},"="),s(" length"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("content "),n("span",{class:"token operator"},"="),s(" content"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`
`),n("span",{class:"token punctuation"},"}"),s(`
`)])]),n("div",{class:"line-numbers","aria-hidden":"true"},[n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"})])],-1),h=n("div",{class:"language-java line-numbers-mode","data-ext":"java","data-title":"java"},[n("pre",{class:"language-java"},[n("code",null,[n("span",{class:"token annotation punctuation"},"@Setter"),s(`
`),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
`),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
`),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"RawStyleDebugEntity02ForDecode"),s(),n("span",{class:"token punctuation"},"{"),s(`

    `),n("span",{class:"token comment"},"// region 消息头"),s(`
    `),n("span",{class:"token comment"},"// byte[0-2) 	消息ID word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgId"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[2-4) 	消息体属性 word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgBodyProps"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[4]     协议版本号"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"1"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),s(" protocolVersion"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[5-15) 	终端手机号或设备ID bcd[10]"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"bcd_8421"'),n("span",{class:"token punctuation"},","),s(" length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"10"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" terminalId"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[15-17) 	消息流水号 word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgSerialNo"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[17-21) 	消息包封装项"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("condition "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"hasSubPackage()"'),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Long"),s(" subPackageInfo"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token comment"},"// endregion 消息头"),s(`

    `),n("span",{class:"token comment"},"// region 消息体"),s(`
    `),n("span",{class:"token comment"},"// 报警标志  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"4"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" alarmFlag"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 状态  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"4"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" status"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 纬度  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"4"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" latitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 经度  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"4"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" longitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 高程  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" altitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 高程  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" speed"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 方向  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" direction"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 时间  BCD[6] yyMMddHHmmss"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"bcd_8421"'),n("span",{class:"token punctuation"},","),s(" length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"6"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" time"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 长度：消息体长度减去前面的 28 字节"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("dataType "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token class-name"},[s("BeanPropertyMetadata"),n("span",{class:"token punctuation"},"."),s("FiledDataType")]),n("span",{class:"token punctuation"},"."),s("sequence"),n("span",{class:"token punctuation"},","),s(" lengthExpression "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"msgBodyLength() - 28"'),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"List"),n("span",{class:"token generics"},[n("span",{class:"token punctuation"},"<"),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},">")]),s(" extraItems"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token comment"},"// endregion 消息体"),s(`

    `),n("span",{class:"token comment"},"// 校验码"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"1"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),s(" checkSum"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// bit[0-9] 0000,0011,1111,1111(3FF)(消息体长度)"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"int"),s(),n("span",{class:"token function"},"msgBodyLength"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token keyword"},"return"),s(" msgBodyProps "),n("span",{class:"token operator"},"&"),s(),n("span",{class:"token number"},"0x3ff"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`

    `),n("span",{class:"token comment"},"// bit[13] 0010,0000,0000,0000(2000)(是否有子包)"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"boolean"),s(),n("span",{class:"token function"},"hasSubPackage"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// return ((msgBodyProperty & 0x2000) >> 13) == 1;"),s(`
        `),n("span",{class:"token keyword"},"return"),s(),n("span",{class:"token punctuation"},"("),s("msgBodyProps "),n("span",{class:"token operator"},"&"),s(),n("span",{class:"token number"},"0x2000"),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token operator"},">"),s(),n("span",{class:"token number"},"0"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`


    `),n("span",{class:"token annotation punctuation"},"@Setter"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
    `),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"ExtraItem"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 附加信息ID   BYTE(1~255)"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"1"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" id"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token comment"},"// 附加信息长度   BYTE(1~255)"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"1"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" contentLength"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token comment"},"// 附加信息内容  BYTE[N]"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("dataType "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token class-name"},[s("BeanPropertyMetadata"),n("span",{class:"token punctuation"},"."),s("FiledDataType")]),n("span",{class:"token punctuation"},"."),s("basic"),n("span",{class:"token punctuation"},","),s(" lengthExpression "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"getContentLength()"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),n("span",{class:"token punctuation"},"["),n("span",{class:"token punctuation"},"]"),s(" content"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(`

        `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},"("),n("span",{class:"token keyword"},"short"),s(" id"),n("span",{class:"token punctuation"},","),s(),n("span",{class:"token keyword"},"short"),s(" length"),n("span",{class:"token punctuation"},","),s(),n("span",{class:"token keyword"},"byte"),n("span",{class:"token punctuation"},"["),n("span",{class:"token punctuation"},"]"),s(" content"),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("id "),n("span",{class:"token operator"},"="),s(" id"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("contentLength "),n("span",{class:"token operator"},"="),s(" length"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("content "),n("span",{class:"token operator"},"="),s(" content"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`
`),n("span",{class:"token punctuation"},"}"),s(`
`)])]),n("div",{class:"line-numbers","aria-hidden":"true"},[n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"})])],-1),S=n("div",{class:"language-java line-numbers-mode","data-ext":"java","data-title":"java"},[n("pre",{class:"language-java"},[n("code",null,[n("span",{class:"token annotation punctuation"},"@Setter"),s(`
`),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
`),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
`),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"JtStyleDebugEntity02ForDecode"),s(),n("span",{class:"token punctuation"},"{"),s(`

    `),n("span",{class:"token comment"},"// region 消息头"),s(`
    `),n("span",{class:"token comment"},"// byte[0-2) 	消息ID word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgId"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[2-4) 	消息体属性 word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgBodyProps"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[4]     协议版本号"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Byte"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),s(" protocolVersion"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[5-15) 	终端手机号或设备ID bcd[10]"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.BCD"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"10"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" terminalId"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[15-17) 	消息流水号 word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgSerialNo"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[17-21) 	消息包封装项"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Dword"),n("span",{class:"token punctuation"},"("),s("condition "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"hasSubPackage()"'),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Long"),s(" subPackageInfo"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token comment"},"// endregion 消息头"),s(`

    `),n("span",{class:"token comment"},"// region 消息体"),s(`
    `),n("span",{class:"token comment"},"// 报警标志  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Dword"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" alarmFlag"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 状态  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Dword"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" status"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 纬度  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Dword"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" latitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 经度  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Dword"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" longitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 高程  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" altitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 高程  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" speed"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 方向  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" direction"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 时间  BCD[6] yyMMddHHmmss"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.BCD"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"6"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" time"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 长度：消息体长度减去前面的 28 字节"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.List"),n("span",{class:"token punctuation"},"("),s("lengthExpression "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"msgBodyLength() - 28"'),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"List"),n("span",{class:"token generics"},[n("span",{class:"token punctuation"},"<"),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},">")]),s(" extraItems"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token comment"},"// endregion 消息体"),s(`

    `),n("span",{class:"token comment"},"// 校验码"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Byte"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),s(" checkSum"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// bit[0-9] 0000,0011,1111,1111(3FF)(消息体长度)"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"int"),s(),n("span",{class:"token function"},"msgBodyLength"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token keyword"},"return"),s(" msgBodyProps "),n("span",{class:"token operator"},"&"),s(),n("span",{class:"token number"},"0x3ff"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`

    `),n("span",{class:"token comment"},"// bit[13] 0010,0000,0000,0000(2000)(是否有子包)"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"boolean"),s(),n("span",{class:"token function"},"hasSubPackage"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// return ((msgBodyProperty & 0x2000) >> 13) == 1;"),s(`
        `),n("span",{class:"token keyword"},"return"),s(),n("span",{class:"token punctuation"},"("),s("msgBodyProps "),n("span",{class:"token operator"},"&"),s(),n("span",{class:"token number"},"0x2000"),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token operator"},">"),s(),n("span",{class:"token number"},"0"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`


    `),n("span",{class:"token annotation punctuation"},"@Setter"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
    `),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"ExtraItem"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 附加信息ID   BYTE(1~255)"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Byte"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" id"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token comment"},"// 附加信息长度   BYTE(1~255)"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Byte"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" contentLength"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token comment"},"// 附加信息内容  BYTE[N]"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Bytes"),n("span",{class:"token punctuation"},"("),s("lengthExpression "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"getContentLength()"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),n("span",{class:"token punctuation"},"["),n("span",{class:"token punctuation"},"]"),s(" content"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(`

        `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},"("),n("span",{class:"token keyword"},"short"),s(" id"),n("span",{class:"token punctuation"},","),s(),n("span",{class:"token keyword"},"short"),s(" length"),n("span",{class:"token punctuation"},","),s(),n("span",{class:"token keyword"},"byte"),n("span",{class:"token punctuation"},"["),n("span",{class:"token punctuation"},"]"),s(" content"),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("id "),n("span",{class:"token operator"},"="),s(" id"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("contentLength "),n("span",{class:"token operator"},"="),s(" length"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("content "),n("span",{class:"token operator"},"="),s(" content"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`
`),n("span",{class:"token punctuation"},"}"),s(`
`)])]),n("div",{class:"line-numbers","aria-hidden":"true"},[n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"})])],-1),D=n("h3",{id:"反序列化",tabindex:"-1"},[n("a",{class:"header-anchor",href:"#反序列化"},[n("span",null,"反序列化")])],-1),P=n("div",{class:"language-java line-numbers-mode","data-ext":"java","data-title":"java"},[n("pre",{java:"",class:"language-java"},[n("code",null,[n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"EntityDecodeTest"),s(),n("span",{class:"token punctuation"},"{"),s(`

    `),n("span",{class:"token annotation punctuation"},"@Test"),s(`
    `),n("span",{class:"token keyword"},"void"),s(),n("span",{class:"token function"},"testDecode"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token keyword"},"final"),s(),n("span",{class:"token class-name"},"EntityCodec"),s(" entityCodec "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token keyword"},"new"),s(),n("span",{class:"token class-name"},"EntityCodec"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// buffer 中存储的是要反序列化的数据(这里写死用来演示)"),s(`
        `),n("span",{class:"token keyword"},"final"),s(),n("span",{class:"token class-name"},"String"),s(" hexString "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"0200402a01000000000139111122220063000000580000006f01dc9a0707456246231d029a005a240322222633010400001a0a02020058030200595b"'),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token keyword"},"final"),s(),n("span",{class:"token class-name"},"ByteBuf"),s(" buffer "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token class-name"},"ByteBufAllocator"),n("span",{class:"token punctuation"},"."),n("span",{class:"token constant"},"DEFAULT"),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"buffer"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"writeBytes"),n("span",{class:"token punctuation"},"("),n("span",{class:"token class-name"},"XtreamBytes"),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"decodeHex"),n("span",{class:"token punctuation"},"("),s("hexString"),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token keyword"},"try"),s(),n("span",{class:"token punctuation"},"{"),s(`
            `),n("span",{class:"token keyword"},"final"),s(),n("span",{class:"token class-name"},"RustStyleDebugEntity02ForDecode"),s(" entity "),n("span",{class:"token operator"},"="),s(" entityCodec"),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"decode"),n("span",{class:"token punctuation"},"("),n("span",{class:"token class-name"},"RustStyleDebugEntity02ForDecode"),n("span",{class:"token punctuation"},"."),n("span",{class:"token keyword"},"class"),n("span",{class:"token punctuation"},","),s(" buffer"),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token class-name"},"System"),n("span",{class:"token punctuation"},"."),s("out"),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"println"),n("span",{class:"token punctuation"},"("),s("entity"),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(),n("span",{class:"token keyword"},"finally"),s(),n("span",{class:"token punctuation"},"{"),s(`
            buffer`),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"release"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`
`),n("span",{class:"token punctuation"},"}"),s(`
`)])]),n("div",{class:"highlight-lines"},[n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("div",{class:"highlight-line"}," "),n("br"),n("br"),n("div",{class:"highlight-line"}," "),n("br"),n("br"),n("br")]),n("div",{class:"line-numbers","aria-hidden":"true"},[n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"})])],-1),f=n("h2",{id:"编码",tabindex:"-1"},[n("a",{class:"header-anchor",href:"#编码"},[n("span",null,"编码")])],-1),R=n("h3",{id:"实体类定义-1",tabindex:"-1"},[n("a",{class:"header-anchor",href:"#实体类定义-1"},[n("span",null,"实体类定义")])],-1),B=n("div",{class:"language-java line-numbers-mode","data-ext":"java","data-title":"java"},[n("pre",{class:"language-java"},[n("code",null,[n("span",{class:"token annotation punctuation"},"@Setter"),s(`
`),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
`),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
`),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"RustStyleDebugEntity02ForEncode"),s(),n("span",{class:"token punctuation"},"{"),s(`

    `),n("span",{class:"token comment"},"// region 消息头"),s(`
    `),n("span",{class:"token comment"},"// byte[0-2) 	消息ID word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgId"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[2-4) 	消息体属性 word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgBodyProps"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[4]     协议版本号"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u8"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),s(" protocolVersion"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[5-15) 	终端手机号或设备ID bcd[10]"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.str"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"bcd_8421"'),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" terminalId"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[15-17) 	消息流水号 word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgSerialNo"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[17-21) 	消息包封装项"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u32"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Long"),s(" subPackageInfo"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token comment"},"// endregion 消息头"),s(`

    `),n("span",{class:"token comment"},"// region 消息体"),s(`
    `),n("span",{class:"token comment"},"// 报警标志  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u32"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" alarmFlag"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 状态  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u32"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" status"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 纬度  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u32"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" latitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 经度  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u32"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" longitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 高程  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" altitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 高程  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" speed"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 方向  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" direction"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 时间  BCD[6] yyMMddHHmmss"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.str"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"bcd_8421"'),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" time"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.list"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"List"),n("span",{class:"token generics"},[n("span",{class:"token punctuation"},"<"),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},">")]),s(" extraItems"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token comment"},"// endregion 消息体"),s(`

    `),n("span",{class:"token comment"},"// 校验码"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.i8"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),s(" checkSum"),n("span",{class:"token punctuation"},";"),s(`


    `),n("span",{class:"token annotation punctuation"},"@Setter"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
    `),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"ExtraItem"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 附加信息ID   BYTE(1~255)"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u8"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" id"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token comment"},"// 附加信息长度   BYTE(1~255)"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u8"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" contentLength"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token comment"},"// 附加信息内容  BYTE[N]"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.byte_array"),n("span",{class:"token punctuation"},"("),s("lengthExpression "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"getLength()"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),n("span",{class:"token punctuation"},"["),n("span",{class:"token punctuation"},"]"),s(" content"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(`

        `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},"("),n("span",{class:"token keyword"},"short"),s(" id"),n("span",{class:"token punctuation"},","),s(),n("span",{class:"token keyword"},"short"),s(" contentLength"),n("span",{class:"token punctuation"},","),s(),n("span",{class:"token keyword"},"byte"),n("span",{class:"token punctuation"},"["),n("span",{class:"token punctuation"},"]"),s(" content"),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("id "),n("span",{class:"token operator"},"="),s(" id"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("contentLength "),n("span",{class:"token operator"},"="),s(" contentLength"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("content "),n("span",{class:"token operator"},"="),s(" content"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`
`),n("span",{class:"token punctuation"},"}"),s(`
`)])]),n("div",{class:"line-numbers","aria-hidden":"true"},[n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"})])],-1),x=n("div",{class:"language-java line-numbers-mode","data-ext":"java","data-title":"java"},[n("pre",{class:"language-java"},[n("code",null,[n("span",{class:"token annotation punctuation"},"@Setter"),s(`
`),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
`),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
`),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"RawStyleDebugEntity02ForEncode"),s(),n("span",{class:"token punctuation"},"{"),s(`

    `),n("span",{class:"token comment"},"// region 消息头"),s(`
    `),n("span",{class:"token comment"},"// byte[0-2) 	消息ID word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgId"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[2-4) 	消息体属性 word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgBodyProps"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[4]     协议版本号"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"1"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),s(" protocolVersion"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[5-15) 	终端手机号或设备ID bcd[10]"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"bcd_8421"'),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" terminalId"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[15-17) 	消息流水号 word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgSerialNo"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[17-21) 	消息包封装项"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"4"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Long"),s(" subPackageInfo"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token comment"},"// endregion 消息头"),s(`

    `),n("span",{class:"token comment"},"// region 消息体"),s(`
    `),n("span",{class:"token comment"},"// 报警标志  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"4"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" alarmFlag"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 状态  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"4"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" status"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 纬度  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"4"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" latitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 经度  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"4"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" longitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 高程  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" altitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 高程  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" speed"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 方向  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" direction"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 时间  BCD[6] yyMMddHHmmss"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"bcd_8421"'),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" time"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("dataType "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token class-name"},[s("BeanPropertyMetadata"),n("span",{class:"token punctuation"},"."),s("FiledDataType")]),n("span",{class:"token punctuation"},"."),s("sequence"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"List"),n("span",{class:"token generics"},[n("span",{class:"token punctuation"},"<"),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},">")]),s(" extraItems"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token comment"},"// endregion 消息体"),s(`

    `),n("span",{class:"token comment"},"// 校验码"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"1"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),s(" checkSum"),n("span",{class:"token punctuation"},";"),s(`


    `),n("span",{class:"token annotation punctuation"},"@Setter"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
    `),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"ExtraItem"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 附加信息ID   BYTE(1~255)"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"1"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" id"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token comment"},"// 附加信息长度   BYTE(1~255)"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"1"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" contentLength"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token comment"},"// 附加信息内容  BYTE[N]"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("lengthExpression "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"getContentLength()"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),n("span",{class:"token punctuation"},"["),n("span",{class:"token punctuation"},"]"),s(" content"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(`

        `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},"("),n("span",{class:"token keyword"},"short"),s(" id"),n("span",{class:"token punctuation"},","),s(),n("span",{class:"token keyword"},"short"),s(" contentLength"),n("span",{class:"token punctuation"},","),s(),n("span",{class:"token keyword"},"byte"),n("span",{class:"token punctuation"},"["),n("span",{class:"token punctuation"},"]"),s(" content"),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("id "),n("span",{class:"token operator"},"="),s(" id"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("contentLength "),n("span",{class:"token operator"},"="),s(" contentLength"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("content "),n("span",{class:"token operator"},"="),s(" content"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`
`),n("span",{class:"token punctuation"},"}"),s(`
`)])]),n("div",{class:"line-numbers","aria-hidden":"true"},[n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"})])],-1),E=n("div",{class:"language-java line-numbers-mode","data-ext":"java","data-title":"java"},[n("pre",{class:"language-java"},[n("code",null,[n("span",{class:"token annotation punctuation"},"@Setter"),s(`
`),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
`),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
`),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"JtStyleDebugEntity02ForEncode"),s(),n("span",{class:"token punctuation"},"{"),s(`

    `),n("span",{class:"token comment"},"// region 消息头"),s(`
    `),n("span",{class:"token comment"},"// byte[0-2) 	消息ID word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgId"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[2-4) 	消息体属性 word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgBodyProps"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[4]     协议版本号"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Byte"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),s(" protocolVersion"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[5-15) 	终端手机号或设备ID bcd[10]"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.BCD"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" terminalId"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[15-17) 	消息流水号 word(16)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgSerialNo"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// byte[17-21) 	消息包封装项"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Dword"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Long"),s(" subPackageInfo"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token comment"},"// endregion 消息头"),s(`

    `),n("span",{class:"token comment"},"// region 消息体"),s(`
    `),n("span",{class:"token comment"},"// 报警标志  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Dword"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" alarmFlag"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 状态  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Dword"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" status"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 纬度  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Dword"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" latitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 经度  DWORD(4)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Dword"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"long"),s(" longitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 高程  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" altitude"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 高程  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" speed"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 方向  WORD(2)"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" direction"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 时间  BCD[6] yyMMddHHmmss"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.BCD"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" time"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.List"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"List"),n("span",{class:"token generics"},[n("span",{class:"token punctuation"},"<"),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},">")]),s(" extraItems"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token comment"},"// endregion 消息体"),s(`

    `),n("span",{class:"token comment"},"// 校验码"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Byte"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),s(" checkSum"),n("span",{class:"token punctuation"},";"),s(`


    `),n("span",{class:"token annotation punctuation"},"@Setter"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
    `),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"ExtraItem"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 附加信息ID   BYTE(1~255)"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Byte"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" id"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token comment"},"// 附加信息长度   BYTE(1~255)"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Byte"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" contentLength"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token comment"},"// 附加信息内容  BYTE[N]"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Bytes"),n("span",{class:"token punctuation"},"("),s("lengthExpression "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"getLength()"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"byte"),n("span",{class:"token punctuation"},"["),n("span",{class:"token punctuation"},"]"),s(" content"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(`

        `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token class-name"},"ExtraItem"),n("span",{class:"token punctuation"},"("),n("span",{class:"token keyword"},"short"),s(" id"),n("span",{class:"token punctuation"},","),s(),n("span",{class:"token keyword"},"short"),s(" contentLength"),n("span",{class:"token punctuation"},","),s(),n("span",{class:"token keyword"},"byte"),n("span",{class:"token punctuation"},"["),n("span",{class:"token punctuation"},"]"),s(" content"),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("id "),n("span",{class:"token operator"},"="),s(" id"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("contentLength "),n("span",{class:"token operator"},"="),s(" contentLength"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token keyword"},"this"),n("span",{class:"token punctuation"},"."),s("content "),n("span",{class:"token operator"},"="),s(" content"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`
`),n("span",{class:"token punctuation"},"}"),s(`
`)])]),n("div",{class:"line-numbers","aria-hidden":"true"},[n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"})])],-1),_=n("h3",{id:"序列化",tabindex:"-1"},[n("a",{class:"header-anchor",href:"#序列化"},[n("span",null,"序列化")])],-1),I=n("div",{class:"language-java line-numbers-mode","data-ext":"java","data-title":"java"},[n("pre",{java:"",class:"language-java"},[n("code",null,[n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"EntityEncodeTest"),s(),n("span",{class:"token punctuation"},"{"),s(`

    `),n("span",{class:"token annotation punctuation"},"@Test"),s(`
    `),n("span",{class:"token keyword"},"void"),s(),n("span",{class:"token function"},"testEncode"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token keyword"},"final"),s(),n("span",{class:"token class-name"},"EntityCodec"),s(" entityCodec "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token keyword"},"new"),s(),n("span",{class:"token class-name"},"EntityCodec"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token keyword"},"final"),s(),n("span",{class:"token class-name"},"ByteBuf"),s(" buffer "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token class-name"},"ByteBufAllocator"),n("span",{class:"token punctuation"},"."),n("span",{class:"token constant"},"DEFAULT"),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"buffer"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token keyword"},"try"),s(),n("span",{class:"token punctuation"},"{"),s(`
            `),n("span",{class:"token keyword"},"final"),s(),n("span",{class:"token class-name"},"RustStyleDebugEntity02ForEncode"),s(" instance "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token keyword"},"new"),s(),n("span",{class:"token class-name"},"RustStyleDebugEntity02ForEncode"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token comment"},"// TODO 省略属性赋值"),s(`
            `),n("span",{class:"token comment"},"// instance.setXxx(someValue);"),s(`
            `),n("span",{class:"token comment"},"// instance.setMsgId(someValue);"),s(`
            `),n("span",{class:"token comment"},"// ..."),s(`

            `),n("span",{class:"token comment"},"// 将 instance 的数据序列化到 buffer 中"),s(`
            entityCodec`),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"encode"),n("span",{class:"token punctuation"},"("),s("instance"),n("span",{class:"token punctuation"},","),s(" buffer"),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token comment"},"// 使用 buffer"),s(`
            `),n("span",{class:"token class-name"},"System"),n("span",{class:"token punctuation"},"."),s("out"),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"println"),n("span",{class:"token punctuation"},"("),n("span",{class:"token class-name"},"ByteBufUtil"),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"hexDump"),n("span",{class:"token punctuation"},"("),s("buffer"),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(),n("span",{class:"token keyword"},"finally"),s(),n("span",{class:"token punctuation"},"{"),s(`
            buffer`),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"release"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`
`),n("span",{class:"token punctuation"},"}"),s(`
`)])]),n("div",{class:"highlight-lines"},[n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("div",{class:"highlight-line"}," "),n("br"),n("br"),n("br"),n("div",{class:"highlight-line"}," "),n("br"),n("br"),n("br")]),n("div",{class:"line-numbers","aria-hidden":"true"},[n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"})])],-1);function F(T,W){const i=c("RouteLink"),l=c("Tabs");return u(),r("div",null,[d,n("div",m,[b,n("ul",null,[n("li",null,[s("在阅读本文之前，建议先阅读 "),o(i,{to:"/core/samples/custom-protocol-sample-02/"},{default:a(()=>[s("协议格式说明")]),_:1})]),v])]),y,w,o(l,{id:"23",data:[{id:"rust-style"},{id:"raw-style"},{id:"jt-style"}],active:0,"tab-id":"demo1"},{title0:a(({value:e,isActive:t})=>[s("Rust 命名风格")]),title1:a(({value:e,isActive:t})=>[s("原始命名风格")]),title2:a(({value:e,isActive:t})=>[s("JT/T 808 命名风格")]),tab0:a(({value:e,isActive:t})=>[g]),tab1:a(({value:e,isActive:t})=>[h]),tab2:a(({value:e,isActive:t})=>[S]),_:1}),D,P,f,R,o(l,{id:"44",data:[{id:"rust-style"},{id:"raw-style"},{id:"jt-style"}],active:0,"tab-id":"demo1"},{title0:a(({value:e,isActive:t})=>[s("Rust 命名风格")]),title1:a(({value:e,isActive:t})=>[s("原始命名风格")]),title2:a(({value:e,isActive:t})=>[s("JT/T 808 命名风格")]),tab0:a(({value:e,isActive:t})=>[B]),tab1:a(({value:e,isActive:t})=>[x]),tab2:a(({value:e,isActive:t})=>[E]),_:1}),_,I])}const O=p(k,[["render",F],["__file","flatten-style-demo.html.vue"]]),X=JSON.parse('{"path":"/core/samples/custom-protocol-sample-02/flatten-style-demo.html","title":"扁平化写法示例","lang":"zh-CN","frontmatter":{"date":"2024-03-24T00:00:00.000Z","icon":"at","author":"hylexus","category":["示例"],"tag":["编码","解码","JT/T 808"],"description":"扁平化写法示例 提示 在阅读本文之前，建议先阅读 所谓的 扁平化写法 就是不用单独封装 Header 和 Body，而是直接将 Header 和 Body 的数据写在同一个实体类中 解码 实体类定义 反序列化 编码 实体类定义 序列化","head":[["meta",{"property":"og:url","content":"https://mister-hope.github.io/xtream-codec/core/samples/custom-protocol-sample-02/flatten-style-demo.html"}],["meta",{"property":"og:site_name","content":"xtream-codec"}],["meta",{"property":"og:title","content":"扁平化写法示例"}],["meta",{"property":"og:description","content":"扁平化写法示例 提示 在阅读本文之前，建议先阅读 所谓的 扁平化写法 就是不用单独封装 Header 和 Body，而是直接将 Header 和 Body 的数据写在同一个实体类中 解码 实体类定义 反序列化 编码 实体类定义 序列化"}],["meta",{"property":"og:type","content":"article"}],["meta",{"property":"og:locale","content":"zh-CN"}],["meta",{"property":"og:updated_time","content":"2024-03-24T10:38:48.000Z"}],["meta",{"property":"article:author","content":"hylexus"}],["meta",{"property":"article:tag","content":"编码"}],["meta",{"property":"article:tag","content":"解码"}],["meta",{"property":"article:tag","content":"JT/T 808"}],["meta",{"property":"article:published_time","content":"2024-03-24T00:00:00.000Z"}],["meta",{"property":"article:modified_time","content":"2024-03-24T10:38:48.000Z"}],["script",{"type":"application/ld+json"},"{\\"@context\\":\\"https://schema.org\\",\\"@type\\":\\"Article\\",\\"headline\\":\\"扁平化写法示例\\",\\"image\\":[\\"\\"],\\"datePublished\\":\\"2024-03-24T00:00:00.000Z\\",\\"dateModified\\":\\"2024-03-24T10:38:48.000Z\\",\\"author\\":[{\\"@type\\":\\"Person\\",\\"name\\":\\"hylexus\\"}]}"]]},"headers":[{"level":2,"title":"解码","slug":"解码","link":"#解码","children":[{"level":3,"title":"实体类定义","slug":"实体类定义","link":"#实体类定义","children":[]},{"level":3,"title":"反序列化","slug":"反序列化","link":"#反序列化","children":[]}]},{"level":2,"title":"编码","slug":"编码","link":"#编码","children":[{"level":3,"title":"实体类定义","slug":"实体类定义-1","link":"#实体类定义-1","children":[]},{"level":3,"title":"序列化","slug":"序列化","link":"#序列化","children":[]}]}],"git":{"createdTime":1710065417000,"updatedTime":1711276728000,"contributors":[{"name":"hylexus","email":"hylexus@163.com","commits":2}]},"readingTime":{"minutes":1.04,"words":312},"filePathRelative":"core/samples/custom-protocol-sample-02/flatten-style-demo.md","localizedDate":"2024年3月24日","excerpt":"\\n<div class=\\"hint-container tip\\">\\n<p class=\\"hint-container-title\\">提示</p>\\n<ul>\\n<li>在阅读本文之前，建议先阅读 <a href=\\"/xtream-codec/core/samples/custom-protocol-sample-02/\\" target=\\"_blank\\">协议格式说明</a></li>\\n<li>所谓的 <em>扁平化写法</em> 就是不用单独封装 <code>Header</code> 和 <code>Body</code>，而是直接将 <code>Header</code> 和 <code>Body</code> 的数据写在同一个实体类中</li>\\n</ul>\\n</div>","autoDesc":true}');export{O as comp,X as data};
