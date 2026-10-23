# 验证记录

## 全包映射

核对 builtin.messages 下 116 个顶层生产类型（不含 package-info）。请求与响应主体已有独立测试；接口、抽象基类和嵌套支持类型通过实际消息路径验证。

| 生产范围 | 类型数 | 测试映射 |
| --- | ---: | --- |
| request | 38 | 38 个同名测试类；0608 补充矩形、多边形、线路的相邻嵌套记录及四种空应答 |
| response 顶层 | 50 | 50 个同名测试类，保留上一轮全部断言 |
| ext | 6 | 1210、1211、1212、9208、9212 和 30316364 均有测试；9208 的测试位于 ext.location |
| ext.location | 9 | 0x11/12/13/64 经 0200Sample2 固定报文验证；0x65/66/67、AlarmIdentifier、BuiltinMessage0200Support 新增独立测试 |
| codec | 3 | 两个共享编解码器新增测试；ParameterItem 的 ID、长度、值及类型标记经参数编解码验证 |
| response.sample8105.codec | 1 | 新增直接边界测试，并保留 BuiltinMessage8105Test 的完整与部分字段测试 |
| response.sample8105.types | 6 | Byte、Word、String、Empty、CommandValue、AbstractCommandValue 经 8105 实际路径验证 |
| jt1078 | 2 | 保留 9101、9102 固定报文及字段断言 |
| 根包 | 1 | 无字段的 BuiltinBodilessMessage 由 0002、0003、0004 的空消息测试间接验证 |

另改进测试侧 mixed 三个测试类：四个用例均断言独立计算的固定字节；自行持有的缓冲区在 finally 中释放。没有仅打印且无断言的测试文件。

## 本轮补充

- 新增 9 个测试类，改进 6 个已有测试类，并扩充 BaseCodecTest 原始消息体辅助方法。
- 相较上一轮，本轮增加 41 个执行用例。
- 文件上传覆盖 GBK 文件名、50 字节定长填充、高位偏移、声明数据长度和尾部哨兵。
- 位置扩展覆盖全部字段、报警标识、无事件与多事件、车辆及报警位标志、相邻附加项的普通和追踪编解码。
- 参数编解码覆盖已知类型及调试标记、未知高位参数 ID 的十六进制回退、后续记录、空项和不支持的编码。
- 8105 覆盖空字段、非零 readerIndex、声明长度、偏移排序、重复偏移、GBK 及 WORD 低字节为分隔符 0x3B。
- 0200Sample2 增加三个协议版本的固定完整报文断言和独立固定消息体解码，避免仅以往返比较验证。
- 1210 明确验证附件 fileType/group 是外部元数据，不参与线格式。

## 失败证据与最小修复

1. 30316364 文件名未补齐到 50 字节，后续 DWORD 提前写入。改用右补零的定长 GBK 字符串注解；解码继续保留原始填充，测试同时断言原始值和 trim 后名称。
2. 8105 重复偏移引发持续填充分隔符，受限缓冲区最终越界；解码未推进 readerIndex、忽略 length，并误把 WORD 低字节的 0x3B 当作结束符。新增五个用例在修复前全部失败；修复后全部通过。
3. 0x66 未限制事件列表数量，普通模式吞掉后续附加项，追踪模式越界。增加事件数量表达式后，两条实际 Map 分派路径均保留后续 0x67 和里程项。
4. 位置附加项 0x2B 被注册为 U16，Integer.MIN_VALUE 编码成 2b020000，丢失高位。与同包 BuiltinMessage0200 的四字节定义对齐为 I32，固定报文验证 2b0480000000。

## 执行结果

| 范围 | 测试类 | 用例 | 失败 / 错误 / 跳过 |
| --- | ---: | ---: | --- |
| builtin.messages 全包 | 107 | 319 | 0 / 0 / 0 |
| JT808 整个模块 | 115 | 355 | 0 / 0 / 0 |
| request 子包 | 38 | 95 | 0 / 0 / 0 |
| response 顶层测试 | 50 | 174 | 0 / 0 / 0 |

- JT808 checkstyleMain、checkstyleTest（显式开启）、checkLicenseMain、checkLicenseTest 全部通过。
- 相关 JT808 Debug 模块 compileJava 通过；test 为 NO-SOURCE，该模块没有测试源。
- 静态扫描识别出 231 处完整固定报文字面量，去重后 137 条。按头部长度、消息体长度、转义和 XOR 复核，校验错误为 0。
- openspec validate review-jt808-builtin-message-tests --strict 通过。
- git diff --check 通过。
- 本轮修改未涉及 VuePress 直接导入的源码片段，相关页面只有仓库链接。

## 验证边界

类型映射和固定报文扫描不是行覆盖率或分支覆盖率统计；扫描不包含运行时拼接的报文片段。8105 沿用现有分号表示空项的格式，本轮验证 WORD 低字节为 0x3B 的读取，没有为数值首字节与空项分隔符的格式歧义定义新规则。未知位置附加项继续按已有约定拒绝解码。

## 复现命令

```bash
./gradlew :ext:jt:jt-808-server-spring-boot-starter-reactive:test \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkstyleMain \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkstyleTest \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkLicenseMain \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkLicenseTest \
  :debug:jt:jt-808-server-spring-boot-starter-reactive-debug:compileJava \
  :debug:jt:jt-808-server-spring-boot-starter-reactive-debug:test \
  -Pxtream.backend.build.checkstyle.enabled=true

openspec validate review-jt808-builtin-message-tests --strict
git diff --check
```

