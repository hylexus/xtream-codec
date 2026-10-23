# 验证记录

## 覆盖范围

- 响应包共 50 个消息类（不含 package-info），均有同名独立测试类。
- 新增 `BuiltinMessage8600V2019Test`、`BuiltinMessage8602V2019Test`、`BuiltinMessage8604V2019Test`、`BuiltinMessage8606V2019Test` 和 `ServerCommonReplyMessageTest`。
- 改进 16 个已有响应测试类：修正空消息报文及资源释放，补齐参数 ID、长度、重复项顺序、字段与集合数量断言，增加 GBK、转义、高位参数 ID、注册失败、可选字段省略和零补传场景。
- 区域/路线测试覆盖时间和速度属性的四种组合，并验证嵌入解码保留后续数据；应答别名覆盖三个协议版本和全部工厂重载。
- 固定报文按字段宽度、GBK、BCD、异或校验及转义规则独立计算，编码输出和解码输入分别验证。

## 复现与修复

- 首次运行四个 2019 测试类：32 个用例中 12 个解码用例失败；`8602V2019`、`8606V2019` 名称吞掉后续记录，`8604V2019` 顶点列表读取到尾部字段。补充数量及名称长度表达式后全部通过，另增加嵌入边界回归测试。
- 两个参数样例新增解码测试均复现 `NegativeArraySizeException`；补充参数值长度表达式后，参数元数据、重复项、GBK 及转义用例全部通过。
- 生产改动仅涉及上述五个消息模型的表达式注解及对应导入。

## 执行结果

- 响应包定向测试：50 类、174 用例，失败、错误、跳过均为 0。
- JT808 完整模块：106 类、314 用例，失败、错误、跳过均为 0；其中请求包 38 类、86 用例继续通过。
- JT808 模块 `checkstyleMain`、`checkstyleTest`（显式开启 Checkstyle）、`checkLicenseMain`、`checkLicenseTest` 全部通过。
- `openspec validate improve-jt808-response-message-tests --strict` 通过。
- `git diff --check` 通过。
- 本次修改未涉及文档直接导入的源码片段；相关文档页面仅含源码仓库链接，无需重新构建片段。

## 复现命令

```bash
./gradlew :ext:jt:jt-808-server-spring-boot-starter-reactive:test \
  --tests 'io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.*'

./gradlew :ext:jt:jt-808-server-spring-boot-starter-reactive:test \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkstyleMain \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkstyleTest \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkLicenseMain \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkLicenseTest \
  -Pxtream.backend.build.checkstyle.enabled=true

openspec validate improve-jt808-response-message-tests --strict
git diff --check
```
