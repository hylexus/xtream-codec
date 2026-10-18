# Proposal

## Why

动态编解码目前只有一篇以能力和 Java API 为主的文档，缺少从协议字段表编写完整 DSL、准备编码值并核对报文字节的连续教程。用户仍需阅读源码和测试，才能确定嵌套结构、分支、条目集合及长度之间应该如何配置。

## What Changes

- 增加入门教程，按“协议字段表 → DSL → 编码值 → 预期 HEX → 解码结果”说明一份完整报文的配置过程，并提供 Studio 操作路径。
- 增加 DSL 配置参考，集中说明顶层结构、字段属性、类型与 JSON 值形状、长度来源及单位、条件、分支、版本、派生值、生成规则和选项的适用范围。
- 增加常用配置示例，每个完整示例给出可独立导入的定义、编码值、预期 HEX 和解码结果，覆盖文本填充、嵌套数组、条件分支、有序 TLV、位段、自动生成和多版本。
- 增加常见错误排查，解释长度、引用、位对齐、值形状和校验和问题；真实 JT/T 808 样例作为进阶入口，并明确帧边界和转义的处理范围。
- 增加 Studio 启动与部署指南，覆盖源码构建、单文件 JAR、前端热更新开发、Docker、配置项和工作区持久化，使用户能先启动编辑器再完成 DSL 教程；根目录 README 提供镜像快速启动命令、访问地址和数据卷说明。
- 在 Studio 指南和根目录 README 中配入用户提供的八张截图，展示基础报文与 JT/T 808 0200 的结构编辑、DSL 和运行结果；在 README 说明 Studio 与 Dashboard 的用途和运行方式，区分内置报文样例与服务端联调入口。
- 在现有概览和侧栏增加阅读路径，保留已有 Java 集成说明及原有章节链接；以实际引擎验证新增完整示例，并构建文档站。

## Capabilities

### New Capabilities

无。这次补全现有能力的使用文档，不新增运行时功能；变更声明 `skip_specs: true`。

### Modified Capabilities

无。`dynamic-binary-codec`、`dynamic-codec-authoring` 等现有能力的行为合同保持不变。

## Impact

- 文档：`docs/src/guide/core/dynamic/`，新增教程、参考、示例、排错及 Studio 启动部署页面。
- 导航：`docs/src/.vuepress/sidebar/zh.ts`。
- 项目入口与配图：根目录 `README.md`、`docs/src/.vuepress/public/img/readme/` 中的八张 Studio 截图。
- 验证：core 动态引擎的文档示例测试及必要的测试资源；已有 `DynamicQuickStartTest`、条件分支、TLV、派生值测试和 Studio 预设可复用。
- 进阶资料：引用现有 JT/T 808 动态 DSL 样例及其测试结果，注明不同样例是否包含两侧 `7e` 和转义。
- 无新的生产依赖、DSL 属性、HTTP 接口或协议兼容性变化。
