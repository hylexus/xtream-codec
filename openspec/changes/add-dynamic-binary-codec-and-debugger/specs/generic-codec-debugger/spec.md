# Spec Delta

## Purpose

提供独立于 JT/T 808 等具体协议的可视化编解码调试应用，使用户能调试已有 Java 实体或自由组合的动态结构，并查看原始字节、字段值和定位信息。

## ADDED Requirements

### Requirement: 独立轻量应用

系统 SHALL 提供可单独启动的调试后端及前端，不要求启动 Spring Boot 或任何协议专用服务器，也 SHALL 不要求依赖 JT/T 808 模块。后端 SHALL 提供页面资源和版本化 JSON API。

#### Scenario: 单独启动调试工具

- **WHEN** 用户仅安装通用调试应用及其 core 依赖并启动服务
- **THEN** 用户 SHALL 能打开页面并调用通用编解码接口，无须配置 JT/T 808 服务

### Requirement: 实体类模式

系统 SHALL 从显式注册或受限扫描范围提供可调试实体目录。用户 SHALL 能选择实体、指定 core 版本参数、提交 JSON 编码或原始 HEX 解码，并获得值、HEX、消费量及 Trace。HTTP 请求中的任意类名 SHALL NOT 直接作为可实例化目标。

#### Scenario: 调试已注册实体

- **WHEN** 用户选择已注册实体并分别提交合法 JSON 和对应 HEX
- **THEN** 页面 SHALL 展示两个方向的结果及字段与字节关联的 Trace

#### Scenario: 拒绝未注册实体

- **WHEN** 请求引用目录以外的实体标识
- **THEN** 后端 SHALL 返回可识别的请求错误，且不会按请求内容加载 Java 类

### Requirement: 自由组合模式

系统 SHALL 允许用户在页面中维护有序和嵌套的动态结构定义、编辑编码值或解码 HEX，并用同一份定义执行双向编解码。页面 SHALL 支持结构定义的 JSON 导入和导出，并能配置位字段、长度、条件、分支及扩展处理器可用项。

#### Scenario: 指定动态定义的协议版本

- **WHEN** 用户为自由组合模式选择目标协议版本并提交编解码请求
- **THEN** 后端 SHALL 以该版本编译结构，使用选定布局执行请求
- **THEN** 页面 SHALL 支持 uN/iN 类型及 versions 属性的编辑和导入导出

#### Scenario: 同一结构往返

- **WHEN** 用户编辑包含位字段、长度字段和嵌套集合的定义并提交值
- **THEN** 页面 SHALL 展示编码 HEX；将该 HEX 用相同定义解码 SHALL 展示对应字段值

#### Scenario: 页面暴露定义错误

- **WHEN** 用户输入无法编译的定义
- **THEN** 页面 SHALL 展示字段路径和错误原因，并保留当前编辑内容

#### Scenario: 可视化字段编辑

- **WHEN** 用户在 React 工作台新增、修改、复制、删除和重排字段，或编辑 struct 子字段与 array 元素定义
- **THEN** 同一结构 SHALL 同步到 JSON 表示；字段值 SHALL 按标识保持绑定，完整 JSON 导入导出 SHALL 保留高级 DSL 属性
- **THEN** 页面 SHALL 提供类型对应的值输入、可增删数组项及独立单字段试验，长整数 SHALL 不因 JavaScript 数值转换而丢失精度

#### Scenario: 工作台主题与草稿

- **WHEN** 用户切换深色或明亮模式，或在实体、自由组合、单字段试验间切换并重新打开页面
- **THEN** 页面 SHALL 保留主题和各模式输入草稿，在不同窗口宽度下保持编辑与执行操作可用

### Requirement: 结果与跟踪展示

系统 SHALL 展示编码或解码值、原始 HEX、已消费与剩余输入、结构化字段树、诊断及字节定位。位字段 SHALL 能显示精确 bit 范围；复制原始 HEX SHALL 不自动加入任何协议帧定界符。

#### Scenario: 字段与字节双向定位

- **WHEN** 用户选择字段节点或原始字节
- **THEN** 页面 SHALL 高亮对应字节及相关字段；位字段 SHALL 进一步标示涉及的 bit

#### Scenario: 跟踪节点展开与折叠

- **WHEN** 用户切换含子节点的 Trace 节点的展开状态
- **THEN** 页面 SHALL 显示或隐藏该节点的子树，保持字段选择和字节高亮不变
- **THEN** 用户从 HEX 定位被折叠的字段时，页面 SHALL 展开其祖先节点，保留其他分支的折叠状态

#### Scenario: 解码失败仍可检查部分结果

- **WHEN** 输入在若干字段成功解码后发生截断或长度错误
- **THEN** 页面 SHALL 展示失败位置、诊断和已完成的跟踪节点

### Requirement: 可移植的业务边界

通用调试业务逻辑 SHALL 位于 core.debugger 包，可供另一 HTTP 容器调用，且 SHALL 不依赖独立应用的路由或静态资源实现，不单独发布服务模块。Java 请求、响应和实体模板 SHALL 使用 Map 字段值，不要求调用方构造 JSON 树；HTTP 合同 SHALL 保持 JSON 对象格式。本次 SHALL 不要求修改现有 JT/T 808 Dashboard 页面。

#### Scenario: 接入其他 HTTP 容器

- **WHEN** 另一应用在自己的 HTTP 层调用通用调试业务入口
- **THEN** 它 SHALL 获得与独立应用等价的实体和动态编解码结果合同，无需启动独立调试服务

#### Scenario: Java 字段值调用

- **WHEN** Java 调用方用包含嵌套 Map、List 和标量的字段映射构造编码请求
- **THEN** core 调试入口 SHALL 接受该映射并返回字段映射；HTTP 反序列化相同 JSON 对象 SHALL 得到等价的编解码结果

#### Scenario: 枚举请求与 JSON 名称

- **WHEN** Java 调用方使用模式和方向枚举构造请求，或 HTTP 调用方提交 entity/dynamic 与 encode/decode 名称
- **THEN** 请求 SHALL 获得相同的模式和方向；JSON 序列化 SHALL 保留小写名称
- **THEN** HTTP 请求中未知、缺失或 null 的模式和方向 SHALL 返回 400 请求错误

#### Scenario: 宿主实体值转换

- **WHEN** 宿主向完整构造器提供实体值转换器
- **THEN** 实体模板、编码输入和两个方向的响应值 SHALL 使用该转换器；动态模式 SHALL 直接使用 Map，不调用实体转换器
- **THEN** 服务构造器及转换接口 SHALL 不出现 Jackson 类型；默认构造器 SHALL 使用 Jackson 2 适配器，且宿主 SHALL 能通过该适配器保留 ObjectMapper 的定制配置

#### Scenario: Jackson 实现按需提供

- **WHEN** 宿主仅引入 core 并通过完整构造器提供不依赖 Jackson 2 的实体值转换器
- **THEN** core SHALL 不传递 Jackson 2 databind 或 Spring Boot JSON starter，实体及动态调试 SHALL 能在没有 Jackson 2 实现的环境运行
- **THEN** 默认构造器和 Jackson 2 适配器 SHALL 由宿主显式提供 databind，独立 HTTP 应用 SHALL 自行声明该依赖
