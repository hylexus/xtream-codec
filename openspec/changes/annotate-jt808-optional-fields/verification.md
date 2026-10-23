# 验证记录

## 范围

本轮修改 21 个生产文件及 1 个既有测试文件，保留此前测试补充和用户已添加的标注。统一使用 org.jspecify.annotations.Nullable，字段、getter 返回类型和 setter 参数保持一致；fluent setter 的实体返回值仍非空。

- 条件字段共 46 个：29 个引用类型字段（包含两个 record 组件）均已标注可空，17 个基本类型字段保留原类型。
- 条件字段涉及区域及路线的时间、行驶阈值、限速、注册鉴权码、多媒体 ID 列表和胎压事件列表；record 组件已有正确标注并保留。
- 版本省略字段涉及 0102 合一实体的 IMEI/软件版本、0107 合一实体的模块属性、0702 合一实体的状态、时间、读卡结果、身份证号和证件到期日期。
- 其他已支持的空值包含无参数的 8105 命令、空 0608 数据列表、零补传的 8800 数组、0102 旧版空消息体、0200 可选附加项及衍生字段、8500 可选参数和 1210 附件的外部 group 元数据。
- 数组使用 byte @Nullable [] 表示数组引用可空；集合仅标容器引用，不扩大元素的空值契约。

## 格式化 getter

0102 合一实体的 getFormatedSoftwareVersion() 同步标注可空。旧版协议无软件版本号时，直接返回 null；2019 版继续去除末尾填充。原先记录旧版空指针行为的两个断言更新为 assertNull。

## 复核结果

- 按活动注解扫描 condition/conditions，全部引用字段已具备可空标注。
- 扫描整个消息包已有可空字段，未发现 getter 返回值或 setter 参数遗漏。
- 与本轮修改前的源码快照比较，除上述格式化 getter 的空值保护及对应说明外，生产改动均为可空注解及导入；协议注解、字段类型和报文字节保持一致。
- 相关定向测试 302 个用例通过。
- JT808 完整模块 115 个测试类、355 个用例通过；其中 builtin.messages 为 107 个测试类、319 个用例。失败、错误、跳过均为 0。
- JT808 checkstyleMain、checkstyleTest（显式开启）、checkLicenseMain、checkLicenseTest 通过。
- JT808 Debug 模块 compileJava 通过。
- openspec validate annotate-jt808-optional-fields --strict 和 git diff --check 通过。
- 修改未涉及 VuePress 直接导入的源码片段。

## 复现命令

```bash
./gradlew :ext:jt:jt-808-server-spring-boot-starter-reactive:test \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkstyleMain \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkstyleTest \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkLicenseMain \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkLicenseTest \
  :debug:jt:jt-808-server-spring-boot-starter-reactive-debug:compileJava \
  -Pxtream.backend.build.checkstyle.enabled=true

openspec validate annotate-jt808-optional-fields --strict
git diff --check
```
