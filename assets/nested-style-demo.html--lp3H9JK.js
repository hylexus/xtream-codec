import{_ as p}from"./plugin-vue_export-helper-DlAUqK2U.js";import{r as c,o as u,c as r,b as n,d as s,e as o,w as a}from"./app-Be8ydcID.js";const k={},d=n("h1",{id:"嵌套写法示例",tabindex:"-1"},[n("a",{class:"header-anchor",href:"#嵌套写法示例"},[n("span",null,"嵌套写法示例")])],-1),m={class:"hint-container tip"},v=n("p",{class:"hint-container-title"},"提示",-1),b=n("li",null,[s("所谓的 "),n("em",null,"嵌套写法"),s(" 就是将 "),n("code",null,"Header"),s(" 和 "),n("code",null,"Body"),s(" 分别封装到单独的实体类中。")],-1),y=n("h2",{id:"解码",tabindex:"-1"},[n("a",{class:"header-anchor",href:"#解码"},[n("span",null,"解码")])],-1),w=n("h3",{id:"实体类定义",tabindex:"-1"},[n("a",{class:"header-anchor",href:"#实体类定义"},[n("span",null,"实体类定义")])],-1),g=n("div",{class:"language-java line-numbers-mode","data-ext":"java","data-title":"java"},[n("pre",{class:"language-java"},[n("code",null,[n("span",{class:"token annotation punctuation"},"@Setter"),s(`
`),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
`),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
`),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"RustStyleDebugEntity01ForDecodeNested"),s(),n("span",{class:"token punctuation"},"{"),s(`

    `),n("span",{class:"token comment"},"// 整个 Header 封装到一个实体类中"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.struct"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Header"),s(" header"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 消息体长度 无符号数 2字节"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgBodyLength"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 整个 Body 封装到一个实体类中"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.struct"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Body"),s(" body"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 下面是 Header 和 Body 实体类的声明"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Data"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"Header"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 固定为 0x80901234"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.i32"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" magicNumber "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"0x80901234"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 主版本号 无符号数 1字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u8"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" majorVersion"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token comment"},"// 次版本号 无符号数 1字节"),s(`

        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u8"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" minorVersion"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 消息类型 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgType"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`


    `),n("span",{class:"token annotation punctuation"},"@Data"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"Body"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 下一个字段长度 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" usernameLength"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 用户名 String, "UTF-8"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.str"),n("span",{class:"token punctuation"},"("),s("lengthExpression "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"getUsernameLength()"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" username"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 下一个字段长度 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" passwordLength"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 密码 String, "GBK"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.str"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"GBK"'),n("span",{class:"token punctuation"},","),s(" lengthExpression "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"getPasswordLength()"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" password"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 生日 String[8], "yyyyMMdd", "UTF-8"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.str"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"8"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" birthday"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 手机号 BCD_8421[6] "GBK"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.str"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"bcd_8421"'),n("span",{class:"token punctuation"},","),s(" length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"6"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" phoneNumber"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 年龄 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" age"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 状态 有符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.i16"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" status"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`
`),n("span",{class:"token punctuation"},"}"),s(`
`)])]),n("div",{class:"line-numbers","aria-hidden":"true"},[n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"})])],-1),h=n("div",{class:"language-java line-numbers-mode","data-ext":"java","data-title":"java"},[n("pre",{class:"language-java"},[n("code",null,[n("span",{class:"token annotation punctuation"},"@Setter"),s(`
`),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
`),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
`),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"RawStyleDebugEntity01ForDecodeNested"),s(),n("span",{class:"token punctuation"},"{"),s(`

    `),n("span",{class:"token comment"},"// 整个 Header 封装到一个实体类中"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("dataType "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token class-name"},[s("BeanPropertyMetadata"),n("span",{class:"token punctuation"},"."),s("FiledDataType")]),n("span",{class:"token punctuation"},"."),s("nested"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Header"),s(" header"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 消息体长度 无符号数 2字节"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgBodyLength"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 整个 Body 封装到一个实体类中"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("dataType "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token class-name"},[s("BeanPropertyMetadata"),n("span",{class:"token punctuation"},"."),s("FiledDataType")]),n("span",{class:"token punctuation"},"."),s("nested"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Body"),s(" body"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 下面是 Header 和 Body 实体类的声明"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Data"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"Header"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 固定为 0x80901234"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"4"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" magicNumber "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"0x80901234"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 主版本号 无符号数 1字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"1"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" majorVersion"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 次版本号 无符号数 1字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"1"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" minorVersion"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 消息类型 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgType"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`


    `),n("span",{class:"token annotation punctuation"},"@Data"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"Body"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 下一个字段长度 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" usernameLength"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 用户名 String, "UTF-8"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"utf-8"'),n("span",{class:"token punctuation"},","),s(" lengthExpression "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"getUsernameLength()"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" username"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 下一个字段长度 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" passwordLength"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 密码 String, "GBK"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.str"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"GBK"'),n("span",{class:"token punctuation"},","),s(" lengthExpression "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"getPasswordLength()"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" password"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 生日 String[8], "yyyyMMdd", "UTF-8"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"UTF-8"'),n("span",{class:"token punctuation"},","),s(" length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"8"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" birthday"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 手机号 BCD_8421[6] "GBK"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"bcd_8421"'),n("span",{class:"token punctuation"},","),s(" length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"6"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" phoneNumber"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 年龄 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" age"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 状态 有符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" status"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`
`),n("span",{class:"token punctuation"},"}"),s(`
`)])]),n("div",{class:"line-numbers","aria-hidden":"true"},[n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"})])],-1),S=n("div",{class:"language-java line-numbers-mode","data-ext":"java","data-title":"java"},[n("pre",{class:"language-java"},[n("code",null,[n("span",{class:"token annotation punctuation"},"@Setter"),s(`
`),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
`),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
`),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"JtStyleDebugEntity01ForDecodeNested"),s(),n("span",{class:"token punctuation"},"{"),s(`

    `),n("span",{class:"token comment"},"// 整个 Header 封装到一个实体类中"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Object"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Header"),s(" header"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 消息体长度 无符号数 2字节"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgBodyLength"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 整个 Body 封装到一个实体类中"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Object"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Body"),s(" body"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 下面是 Header 和 Body 实体类的声明"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Data"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"Header"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 固定为 0x80901234"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Dword"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" magicNumber "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"0x80901234"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 主版本号 无符号数 1字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Byte"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" majorVersion"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 次版本号 无符号数 1字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Byte"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" minorVersion"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 消息类型 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgType"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`


    `),n("span",{class:"token annotation punctuation"},"@Data"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"Body"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 下一个字段长度 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" usernameLength"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 用户名 String, "UTF-8"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Str"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"UTF-8"'),n("span",{class:"token punctuation"},","),s(" lengthExpression "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"getUsernameLength()"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" username"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 下一个字段长度 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" passwordLength"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 密码 String, "GBK"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Str"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"GBK"'),n("span",{class:"token punctuation"},","),s(" lengthExpression "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"getPasswordLength()"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" password"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 生日 String[8], "yyyyMMdd", "UTF-8"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Str"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"UTF-8"'),n("span",{class:"token punctuation"},","),s(" length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"8"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" birthday"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 手机号 BCD_8421[6] "GBK"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Bcd"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"6"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" phoneNumber"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 年龄 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" age"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 状态 有符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" status"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`
`),n("span",{class:"token punctuation"},"}"),s(`
`)])]),n("div",{class:"line-numbers","aria-hidden":"true"},[n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"})])],-1),B=n("h3",{id:"反序列化",tabindex:"-1"},[n("a",{class:"header-anchor",href:"#反序列化"},[n("span",null,"反序列化")])],-1),f=n("div",{class:"language-java line-numbers-mode","data-ext":"java","data-title":"java"},[n("pre",{java:"",class:"language-java"},[n("code",null,[n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"EntityDecodeTest"),s(),n("span",{class:"token punctuation"},"{"),s(`

    `),n("span",{class:"token annotation punctuation"},"@Test"),s(`
    `),n("span",{class:"token keyword"},"void"),s(),n("span",{class:"token function"},"testDecode"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token keyword"},"final"),s(),n("span",{class:"token class-name"},"EntityCodec"),s(" entityCodec "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token keyword"},"new"),s(),n("span",{class:"token class-name"},"EntityCodec"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// buffer 中存储的是要反序列化的数据(这里写死用来演示)"),s(`
        `),n("span",{class:"token keyword"},"final"),s(),n("span",{class:"token class-name"},"String"),s(" hexString "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"8090123401020001003d001678747265616d2d636f6465632ee794a8e688b7e5908d001178747265616d2d636f6465632ec3dcc2eb3230323130323033013911112222270fff9c"'),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token keyword"},"final"),s(),n("span",{class:"token class-name"},"ByteBuf"),s(" buffer "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token class-name"},"ByteBufAllocator"),n("span",{class:"token punctuation"},"."),n("span",{class:"token constant"},"DEFAULT"),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"buffer"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"writeBytes"),n("span",{class:"token punctuation"},"("),n("span",{class:"token class-name"},"XtreamBytes"),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"decodeHex"),n("span",{class:"token punctuation"},"("),s("hexString"),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token keyword"},"try"),s(),n("span",{class:"token punctuation"},"{"),s(`
            `),n("span",{class:"token keyword"},"final"),s(),n("span",{class:"token class-name"},"RustStyleDebugEntity01ForDecodeNested"),s(" entity "),n("span",{class:"token operator"},"="),s(" entityCodec"),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"decode"),n("span",{class:"token punctuation"},"("),n("span",{class:"token class-name"},"RustStyleDebugEntity01ForDecodeNested"),n("span",{class:"token punctuation"},"."),n("span",{class:"token keyword"},"class"),n("span",{class:"token punctuation"},","),s(" buffer"),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token class-name"},"System"),n("span",{class:"token punctuation"},"."),s("out"),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"println"),n("span",{class:"token punctuation"},"("),s("entity"),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(),n("span",{class:"token keyword"},"finally"),s(),n("span",{class:"token punctuation"},"{"),s(`
            buffer`),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"release"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`
`),n("span",{class:"token punctuation"},"}"),s(`
`)])]),n("div",{class:"highlight-lines"},[n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("div",{class:"highlight-line"}," "),n("br"),n("br"),n("div",{class:"highlight-line"}," "),n("br"),n("br"),n("br")]),n("div",{class:"line-numbers","aria-hidden":"true"},[n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"})])],-1),P=n("h2",{id:"编码",tabindex:"-1"},[n("a",{class:"header-anchor",href:"#编码"},[n("span",null,"编码")])],-1),_=n("h3",{id:"实体类定义-1",tabindex:"-1"},[n("a",{class:"header-anchor",href:"#实体类定义-1"},[n("span",null,"实体类定义")])],-1),F=n("div",{class:"language-java line-numbers-mode","data-ext":"java","data-title":"java"},[n("pre",{class:"language-java"},[n("code",null,[n("span",{class:"token annotation punctuation"},"@Setter"),s(`
`),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
`),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
`),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"RustStyleDebugEntity01ForEncodeNested"),s(),n("span",{class:"token punctuation"},"{"),s(`

    `),n("span",{class:"token comment"},"// 整个 Header 封装到一个实体类中"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.struct"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Header"),s(" header"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 消息体长度 无符号数 2字节"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgBodyLength"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 整个 Body 封装到一个实体类中"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.struct"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Body"),s(" body"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 下面是 Header 和 Body 实体类的声明"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Data"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"Header"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 固定为 0x80901234"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.i32"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" magicNumber "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"0x80901234"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 主版本号 无符号数 1字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u8"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" majorVersion"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token comment"},"// 次版本号 无符号数 1字节"),s(`

        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u8"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" minorVersion"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 消息类型 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgType"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`


    `),n("span",{class:"token annotation punctuation"},"@Data"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"Body"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 下一个字段长度 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" usernameLength"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 用户名 String, "UTF-8"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.str"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" username"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 下一个字段长度 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" passwordLength"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 密码 String, "GBK"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.str"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"GBK"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" password"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 生日 String[8], "yyyyMMdd", "UTF-8"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.str"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" birthday"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 手机号 BCD_8421[6] "GBK"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.str"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"bcd_8421"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" phoneNumber"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 年龄 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.u16"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" age"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 状态 有符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.i16"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" status"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`
`),n("span",{class:"token punctuation"},"}"),s(`
`)])]),n("div",{class:"line-numbers","aria-hidden":"true"},[n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"})])],-1),T=n("div",{class:"language-java line-numbers-mode","data-ext":"java","data-title":"java"},[n("pre",{class:"language-java"},[n("code",null,[n("span",{class:"token annotation punctuation"},"@Setter"),s(`
`),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
`),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
`),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"RawStyleDebugEntity01ForEncodeNested"),s(),n("span",{class:"token punctuation"},"{"),s(`

    `),n("span",{class:"token comment"},"// 整个 Header 封装到一个实体类中"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("dataType "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token class-name"},[s("BeanPropertyMetadata"),n("span",{class:"token punctuation"},"."),s("FiledDataType")]),n("span",{class:"token punctuation"},"."),s("nested"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Header"),s(" header"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 消息体长度 无符号数 2字节"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgBodyLength"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 整个 Body 封装到一个实体类中"),s(`
    `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("dataType "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token class-name"},[s("BeanPropertyMetadata"),n("span",{class:"token punctuation"},"."),s("FiledDataType")]),n("span",{class:"token punctuation"},"."),s("nested"),n("span",{class:"token punctuation"},")"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Body"),s(" body"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 下面是 Header 和 Body 实体类的声明"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Data"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"Header"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 固定为 0x80901234"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"4"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" magicNumber "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"0x80901234"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 主版本号 无符号数 1字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"1"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" majorVersion"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 次版本号 无符号数 1字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"1"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" minorVersion"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 消息类型 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgType"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`


    `),n("span",{class:"token annotation punctuation"},"@Data"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"Body"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 下一个字段长度 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" usernameLength"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 用户名 String, "UTF-8"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"utf-8"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" username"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 下一个字段长度 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" passwordLength"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 密码 String, "GBK"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.RustStyle.str"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"GBK"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" password"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 生日 String[8], "yyyyMMdd", "UTF-8"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"UTF-8"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" birthday"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 手机号 BCD_8421[6] "GBK"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"bcd_8421"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" phoneNumber"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 年龄 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" age"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 状态 有符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@XtreamField"),n("span",{class:"token punctuation"},"("),s("length "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"2"),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" status"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`
`),n("span",{class:"token punctuation"},"}"),s(`
`)])]),n("div",{class:"line-numbers","aria-hidden":"true"},[n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"})])],-1),x=n("div",{class:"language-java line-numbers-mode","data-ext":"java","data-title":"java"},[n("pre",{class:"language-java"},[n("code",null,[n("span",{class:"token annotation punctuation"},"@Setter"),s(`
`),n("span",{class:"token annotation punctuation"},"@Getter"),s(`
`),n("span",{class:"token annotation punctuation"},"@ToString"),s(`
`),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"JtStyleDebugEntity01ForEncodeNested"),s(),n("span",{class:"token punctuation"},"{"),s(`

    `),n("span",{class:"token comment"},"// 整个 Header 封装到一个实体类中"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Object"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Header"),s(" header"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 消息体长度 无符号数 2字节"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgBodyLength"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 整个 Body 封装到一个实体类中"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Object"),s(`
    `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"Body"),s(" body"),n("span",{class:"token punctuation"},";"),s(`

    `),n("span",{class:"token comment"},"// 下面是 Header 和 Body 实体类的声明"),s(`
    `),n("span",{class:"token annotation punctuation"},"@Data"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"Header"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 固定为 0x80901234"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Dword"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" magicNumber "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token number"},"0x80901234"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 主版本号 无符号数 1字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Byte"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" majorVersion"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 次版本号 无符号数 1字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Byte"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" minorVersion"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 消息类型 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" msgType"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`


    `),n("span",{class:"token annotation punctuation"},"@Data"),s(`
    `),n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"static"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"Body"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token comment"},"// 下一个字段长度 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" usernameLength"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 用户名 String, "UTF-8"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Str"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"UTF-8"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" username"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 下一个字段长度 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" passwordLength"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 密码 String, "GBK"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Str"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"GBK"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" password"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 生日 String[8], "yyyyMMdd", "UTF-8"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Str"),n("span",{class:"token punctuation"},"("),s("charset "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token string"},'"UTF-8"'),n("span",{class:"token punctuation"},")"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" birthday"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},'// 手机号 BCD_8421[6] "GBK"'),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Bcd"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token class-name"},"String"),s(" phoneNumber"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 年龄 无符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"int"),s(" age"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token comment"},"// 状态 有符号数 2字节"),s(`
        `),n("span",{class:"token annotation punctuation"},"@Preset.JtStyle.Word"),s(`
        `),n("span",{class:"token keyword"},"private"),s(),n("span",{class:"token keyword"},"short"),s(" status"),n("span",{class:"token punctuation"},";"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`
`),n("span",{class:"token punctuation"},"}"),s(`
`)])]),n("div",{class:"line-numbers","aria-hidden":"true"},[n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"})])],-1),j=n("h3",{id:"序列化",tabindex:"-1"},[n("a",{class:"header-anchor",href:"#序列化"},[n("span",null,"序列化")])],-1),D=n("div",{class:"language-java line-numbers-mode","data-ext":"java","data-title":"java"},[n("pre",{java:"",class:"language-java"},[n("code",null,[n("span",{class:"token keyword"},"public"),s(),n("span",{class:"token keyword"},"class"),s(),n("span",{class:"token class-name"},"EntityEncodeTest"),s(),n("span",{class:"token punctuation"},"{"),s(`

    `),n("span",{class:"token annotation punctuation"},"@Test"),s(`
    `),n("span",{class:"token keyword"},"void"),s(),n("span",{class:"token function"},"testEncode"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token punctuation"},"{"),s(`
        `),n("span",{class:"token keyword"},"final"),s(),n("span",{class:"token class-name"},"EntityCodec"),s(" entityCodec "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token keyword"},"new"),s(),n("span",{class:"token class-name"},"EntityCodec"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`

        `),n("span",{class:"token keyword"},"final"),s(),n("span",{class:"token class-name"},"ByteBuf"),s(" buffer "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token class-name"},"ByteBufAllocator"),n("span",{class:"token punctuation"},"."),n("span",{class:"token constant"},"DEFAULT"),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"buffer"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token keyword"},"try"),s(),n("span",{class:"token punctuation"},"{"),s(`
            `),n("span",{class:"token keyword"},"final"),s(),n("span",{class:"token class-name"},"RustStyleDebugEntity01ForEncodeNested"),s(" instance "),n("span",{class:"token operator"},"="),s(),n("span",{class:"token keyword"},"new"),s(),n("span",{class:"token class-name"},"RustStyleDebugEntity01ForEncodeNested"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token comment"},"// 省略属性赋值"),s(`
            `),n("span",{class:"token comment"},"// instance.setXxx(someValue);"),s(`
            `),n("span",{class:"token comment"},"// ..."),s(`
            instance`),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"setMajorVersion"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},"("),n("span",{class:"token keyword"},"short"),n("span",{class:"token punctuation"},")"),s(),n("span",{class:"token number"},"1"),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`

            `),n("span",{class:"token comment"},"// 将 instance 的数据序列化到 buffer 中"),s(`
            entityCodec`),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"encode"),n("span",{class:"token punctuation"},"("),s("instance"),n("span",{class:"token punctuation"},","),s(" buffer"),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`
            `),n("span",{class:"token comment"},"// 使用 buffer"),s(`
            `),n("span",{class:"token class-name"},"System"),n("span",{class:"token punctuation"},"."),s("out"),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"println"),n("span",{class:"token punctuation"},"("),n("span",{class:"token class-name"},"ByteBufUtil"),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"hexDump"),n("span",{class:"token punctuation"},"("),s("buffer"),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(),n("span",{class:"token keyword"},"finally"),s(),n("span",{class:"token punctuation"},"{"),s(`
            buffer`),n("span",{class:"token punctuation"},"."),n("span",{class:"token function"},"release"),n("span",{class:"token punctuation"},"("),n("span",{class:"token punctuation"},")"),n("span",{class:"token punctuation"},";"),s(`
        `),n("span",{class:"token punctuation"},"}"),s(`
    `),n("span",{class:"token punctuation"},"}"),s(`
`),n("span",{class:"token punctuation"},"}"),s(`
`)])]),n("div",{class:"highlight-lines"},[n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("br"),n("div",{class:"highlight-line"}," "),n("br"),n("br"),n("br"),n("div",{class:"highlight-line"}," "),n("br"),n("br"),n("br")]),n("div",{class:"line-numbers","aria-hidden":"true"},[n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"}),n("div",{class:"line-number"})])],-1);function R(J,E){const i=c("RouteLink"),l=c("Tabs");return u(),r("div",null,[d,n("div",m,[v,n("ul",null,[n("li",null,[s("在阅读本文之前，建议先阅读 "),o(i,{to:"/core/samples/custom-protocol-sample-01/"},{default:a(()=>[s("协议格式说明")]),_:1})]),b])]),y,w,o(l,{id:"23",data:[{id:"rust-style"},{id:"raw-style"},{id:"jt-style"}],active:0,"tab-id":"demo1"},{title0:a(({value:e,isActive:t})=>[s("Rust 命名风格")]),title1:a(({value:e,isActive:t})=>[s("原始命名风格")]),title2:a(({value:e,isActive:t})=>[s("JT/T 808 命名风格")]),tab0:a(({value:e,isActive:t})=>[g]),tab1:a(({value:e,isActive:t})=>[h]),tab2:a(({value:e,isActive:t})=>[S]),_:1}),B,f,P,_,o(l,{id:"44",data:[{id:"rust-style"},{id:"raw-style"},{id:"jt-style"}],active:0,"tab-id":"demo1"},{title0:a(({value:e,isActive:t})=>[s("Rust 命名风格")]),title1:a(({value:e,isActive:t})=>[s("原始命名风格")]),title2:a(({value:e,isActive:t})=>[s("JT/T 808 命名风格")]),tab0:a(({value:e,isActive:t})=>[F]),tab1:a(({value:e,isActive:t})=>[T]),tab2:a(({value:e,isActive:t})=>[x]),_:1}),j,D])}const L=p(k,[["render",R],["__file","nested-style-demo.html.vue"]]),N=JSON.parse('{"path":"/core/samples/custom-protocol-sample-01/nested-style-demo.html","title":"嵌套写法示例","lang":"zh-CN","frontmatter":{"date":"2024-03-09T00:00:00.000Z","icon":"at","author":"hylexus","category":["示例"],"tag":["编码","解码"],"description":"嵌套写法示例 提示 在阅读本文之前，建议先阅读 所谓的 嵌套写法 就是将 Header 和 Body 分别封装到单独的实体类中。 解码 实体类定义 反序列化 编码 实体类定义 序列化","head":[["meta",{"property":"og:url","content":"https://mister-hope.github.io/xtream-codec/core/samples/custom-protocol-sample-01/nested-style-demo.html"}],["meta",{"property":"og:site_name","content":"xtream-codec"}],["meta",{"property":"og:title","content":"嵌套写法示例"}],["meta",{"property":"og:description","content":"嵌套写法示例 提示 在阅读本文之前，建议先阅读 所谓的 嵌套写法 就是将 Header 和 Body 分别封装到单独的实体类中。 解码 实体类定义 反序列化 编码 实体类定义 序列化"}],["meta",{"property":"og:type","content":"article"}],["meta",{"property":"og:locale","content":"zh-CN"}],["meta",{"property":"og:updated_time","content":"2024-03-24T10:38:48.000Z"}],["meta",{"property":"article:author","content":"hylexus"}],["meta",{"property":"article:tag","content":"编码"}],["meta",{"property":"article:tag","content":"解码"}],["meta",{"property":"article:published_time","content":"2024-03-09T00:00:00.000Z"}],["meta",{"property":"article:modified_time","content":"2024-03-24T10:38:48.000Z"}],["script",{"type":"application/ld+json"},"{\\"@context\\":\\"https://schema.org\\",\\"@type\\":\\"Article\\",\\"headline\\":\\"嵌套写法示例\\",\\"image\\":[\\"\\"],\\"datePublished\\":\\"2024-03-09T00:00:00.000Z\\",\\"dateModified\\":\\"2024-03-24T10:38:48.000Z\\",\\"author\\":[{\\"@type\\":\\"Person\\",\\"name\\":\\"hylexus\\"}]}"]]},"headers":[{"level":2,"title":"解码","slug":"解码","link":"#解码","children":[{"level":3,"title":"实体类定义","slug":"实体类定义","link":"#实体类定义","children":[]},{"level":3,"title":"反序列化","slug":"反序列化","link":"#反序列化","children":[]}]},{"level":2,"title":"编码","slug":"编码","link":"#编码","children":[{"level":3,"title":"实体类定义","slug":"实体类定义-1","link":"#实体类定义-1","children":[]},{"level":3,"title":"序列化","slug":"序列化","link":"#序列化","children":[]}]}],"git":{"createdTime":1710065417000,"updatedTime":1711276728000,"contributors":[{"name":"hylexus","email":"hylexus@163.com","commits":2}]},"readingTime":{"minutes":0.98,"words":294},"filePathRelative":"core/samples/custom-protocol-sample-01/nested-style-demo.md","localizedDate":"2024年3月9日","excerpt":"\\n<div class=\\"hint-container tip\\">\\n<p class=\\"hint-container-title\\">提示</p>\\n<ul>\\n<li>在阅读本文之前，建议先阅读 <a href=\\"/xtream-codec/core/samples/custom-protocol-sample-01/\\" target=\\"_blank\\">协议格式说明</a></li>\\n<li>所谓的 <em>嵌套写法</em> 就是将 <code>Header</code> 和 <code>Body</code> 分别封装到单独的实体类中。</li>\\n</ul>\\n</div>\\n<h2>解码</h2>","autoDesc":true}');export{L as comp,N as data};
