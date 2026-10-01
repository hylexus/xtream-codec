# 独立通用编解码调试工具

本工具只依赖 `xtream-codec-core`，通用调试服务包含在 core 中，不依赖 JT/T 808 或 Spring Boot。后端使用 JDK 21 的 `HttpServer`，默认只监听 `127.0.0.1`。

## 启动

在仓库根目录运行：

```bash
JAVA_HOME="$(mise where java)" ./gradlew :debug:xtream-codec-debugger-app:run --args="8765"
```

打开 [http://127.0.0.1:8765](http://127.0.0.1:8765)。不传端口时也使用 `8765`。前端静态资源在 Gradle `processResources` 中从 `debug/xtream-codec-debugger-ui/src` 打包；发布的应用运行时不需要 Node。独立前端检查和产物构建：

```bash
cd debug/xtream-codec-debugger-ui
pnpm run check
pnpm run build
```

前端构建产物在本模块 `dist/`。本地 Java 应用会自动使用前端源文件，修改后重启应用即可重新打包。

## 实体类模式

选择“实体类”，在目录里搜索并选择已注册实体。默认提供一个协议无关的 `sample` 实体，包含 `sequence`（u16）和 `flags`（u8）。可指定实体版本，留空则使用 core 的全部版本。点击“载入初始值”得到 JSON 草稿；点击“编码”生成原始 HEX；把 HEX 输入解码框可反向得到 JSON 和字段 Trace。

若需要调试宿主项目实体，可在启动命令的端口之后传入一个或多个允许扫描的包名；或在自己的程序中构建 `DebugEntityCatalog` 显式注册实体。HTTP 请求只能引用目录标识，不能按请求加载类。

## 自由组合模式

字段树支持新增、编辑、排序、删除和 `struct` 子字段。字段编辑器可设置位宽、大小端、位序、长度来源、填充与对齐；复杂属性如条件、分支、派生表达式和注册扩展可在“其他属性 JSON”中填写，也可直接展开“定义 JSON · 完整属性”编辑。数组元素结构在完整 JSON 中通过 `item` 修改。

整数类型统一为 `uN` / `iN`（N 为 1–64），如 `u16`、`i32`、`u3`，无需另填位宽；浮点数和原始位串仍填写 `bits`。早期 JSON 中的 `uint/int + bits` 需要迁移为对应名称。字段“其他属性 JSON”可填写 `versions: [2013, 2019]`；省略表示默认定义，同一标识的精确版本定义覆盖默认布局。页面顶部的目标协议版本也适用于自由组合模式，留空只使用默认定义，与 `schemaVersion` 无关。

“载入示例”给出 3+5 bit、长度回填、嵌套结构和计数数组的双向样例。对应值的原始编码为 `b10702011234025678`。编码结果会放入解码框；解码使用同一份结构。可以导入或导出 `codec-schema.json` 分享结构定义，值与定义分离。本地草稿存入浏览器的 `localStorage`。

表达式 AST 可直接用于 `derived.expression`、`when.expression` 与 `length.expression`。完整定义 JSON 的导入、导出会保留这些属性。默认应用不启用字符串表达式；导入含 `engine/source/bindings` 的定义后执行，会显示“未启用表达式引擎”的字段错误。宿主显式启用的方式见 [INTEGRATION.md](./INTEGRATION.md)，页面和 HTTP 请求均不能注册引擎。

## 结果与定位

结果区域显示 JSON 值、原始 HEX、消费的 bit/byte 数、剩余输入和诊断。点击 Trace 节点高亮对应字节及精确 bit；点击原始字节可定位相关节点。复制 HEX 只复制响应中的 `hex`，不添加协议帧边界。失败时，已经完成的节点与错误 bit 偏移仍可查看。

## API 与移植

独立应用提供：

- `GET /api/v1/entities`：实体目录。
- `GET /api/v1/entities/{id}/template`：实体初始 JSON。
- `GET /api/v1/extensions`：宿主允许的扩展标识。
- `POST /api/v1/codec`：实体或动态编解码。

业务 DTO 与入口在 `xtream-codec-core` 的 `io.github.hylexus.xtream.codec.core.debugger` 包中，无需额外引入或发布调试服务模块。其他 HTTP 容器的调用方式见 [INTEGRATION.md](./INTEGRATION.md)；将来接入 808 Dashboard Starter 时可直接复用 core 中的调试入口与请求/响应合同。
