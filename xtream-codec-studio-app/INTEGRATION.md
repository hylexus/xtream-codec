# 可复用调试服务接入

通用调试 API 位于 `xtream-codec-core` 的 `io.github.hylexus.xtream.codec.core.debugger` 包，宿主只需引入 core，无需额外的调试服务制品。这些类不引用 HTTP 路由、静态资源或 JT/T 808 类型。宿主可在自己的 Spring MVC、WebFlux、Netty 或其他容器中调用相同的业务入口。

core 仅传递 `jackson-annotations`，不传递 Jackson 2 databind 或 Spring Boot JSON starter。下面使用默认构造器的示例需要宿主显式提供 Jackson 2 databind；如果已有依赖提供它，无需重复添加。版本可由项目 BOM 或 Spring Boot 依赖管理统一指定。

```kotlin
implementation("io.github.hylexus.xtream:xtream-codec-core:<项目版本>")
implementation("com.fasterxml.jackson.core:jackson-databind")
```

```java
import io.github.hylexus.xtream.codec.core.debugger.CodecDebuggerService;
import io.github.hylexus.xtream.codec.core.debugger.DebugCodecDirection;
import io.github.hylexus.xtream.codec.core.debugger.DebugCodecMode;
import io.github.hylexus.xtream.codec.core.debugger.DebugCodecRequest;
import io.github.hylexus.xtream.codec.core.debugger.DebugCodecResponse;
import io.github.hylexus.xtream.codec.core.debugger.DebugEntityCatalog;

DebugEntityCatalog catalog = new DebugEntityCatalog(Map.of("example", ExampleEntity.class));
CodecDebuggerService service = new CodecDebuggerService(catalog);
List<DebugEntityCatalog.Entry> entities = service.entities();
Map<String, Object> template = service.entityTemplate("example");
DebugCodecRequest request = new DebugCodecRequest(
        DebugCodecMode.ENTITY, DebugCodecDirection.ENCODE, "example", null, null, Map.of("number", 0x1234), null);
DebugCodecResponse response = service.execute(request);
```

示例假设 `ExampleEntity` 定义了 `number` 字段。请求值、响应值和实体模板均使用 `Map<String, Object>`；嵌套对象使用 Map，数组使用 List，标量使用 Java 数值、布尔值、字符串或 null。Java 调用方无需先创建 `JsonNode`，HTTP 中的 `values` 仍是 JSON 对象。需要显式 null 值时，使用支持 null 的 Map/List 实现；`Map.of` 和 `List.of` 不接受 null。

动态模式直接使用 Map 执行计划，输出中超过 JSON 安全整数范围的值仍为十进制字符串。实体模式通过 `DebugEntityValueMapper` 完成 Map 与实体的转换；该接口只定义 `toEntity(values, entityType)` 和 `toValues(entity)`，不引用具体 JSON 库类型。

服务仅保留默认构造器和完整配置构造器。默认入口使用 Jackson 2 的默认配置；宿主可通过 `Jackson2DebugEntityValueMapper` 复用已配置的 mapper，实体模板及两个编解码方向使用同一份命名、注解和模块配置：

```java
import io.github.hylexus.xtream.codec.core.debugger.Jackson2DebugEntityValueMapper;

var valueMapper = new Jackson2DebugEntityValueMapper(hostObjectMapper);
var service = new CodecDebuggerService(
        catalog, valueMapper, DynamicCompileOptions.DEFAULT, DynamicLimits.DEFAULT);
```

适配器不复制或修改宿主 mapper；请在构建适配器前完成配置，服务使用期间保持配置稳定。自定义 `DebugEntityValueMapper` 也必须支持并发调用、返回非空结果，输出嵌套 Map/List，并通过运行时异常报告转换失败。动态模式不调用实体值转换器。

未来适配 Jackson 3 时，可以实现同一接口，服务构造器、请求/响应与执行调用无需改变；创建 mapper 和选择适配器的接入代码仍需迁移。目前只提供 Jackson 2 适配器，core 的其他既有 Jackson API 也仍使用 Jackson 2。

使用完整构造器并传入不依赖 Jackson 2 的 `DebugEntityValueMapper` 时，实体和动态调试均可在没有 Jackson 2 实现的环境运行。普通 `EntityCodec` 编解码、Java 构造的动态计划以及 `DynamicSchemaJson.jsonSchema()` 资源读取也不需要 Jackson 2 实现。`DynamicSchemaJson.read/write`、`DataFields` 的 JSON 转换及其他 Jackson 2 专用入口仍需要宿主提供 databind。独立 HTTP 调试应用已显式声明该依赖。

可通过 `./gradlew :xtream-codec-core:testWithoutJackson` 验证没有 Jackson 实现的运行环境；该任务也纳入 `check` 和提交前检查。

目录由宿主明确注册，或使用 `DebugEntityCatalog.scan("your.allowed.package")` 限定扫描。请求中的 `entityId` 只查目录，不触发按类名加载。动态结构的扩展同样由宿主传入 `DynamicExtensionRegistry` 白名单。

## 请求和响应

`DebugCodecRequest` 的字段：

| 字段        | 说明                                                        |
|-------------|-------------------------------------------------------------|
| `mode`      | Java 为 `DebugCodecMode`，JSON 为 `entity` 或 `dynamic`     |
| `direction` | Java 为 `DebugCodecDirection`，JSON 为 `encode` 或 `decode` |
| `entityId`  | 实体模式的目录标识                                          |
| `version`   | 可选的 core 实体版本，缺省为全部版本                        |
| `schema`    | 动态模式的版本化结构                                        |
| `values`    | 编码输入字段映射，HTTP 中为 JSON 对象                       |
| `hex`       | 解码输入原始 HEX                                            |

`mode` 和 `direction` 是必填枚举，不接受未知名称、缺失或 null；独立 HTTP 应用对这些输入返回 400。合法请求继续使用原有的小写 JSON 名称，无需调整前端。

`DebugCodecResponse` 始终包含 `success`、`hex`、`consumedBits`、`consumedBytes`、`remainingHex`、`trace` 和 `checksums`；成功时还有字段映射 `values`，失败时有 `error.message`、可用的 `fieldPath` 和 `bitOffset`。失败 Trace 保留已完成字段。动态校验和不匹配默认仍返回成功响应、核对结果及 Trace 警告，便于查看损坏的报文。HTTP 适配器可把成功映射为 200、业务失败映射为 422。

`CodecDebuggerServiceTest` 演示无需启动 HTTP 服务的实体与动态调用。调试服务本身不管理调用方的 HTTP 连接。


## 可选表达式引擎

默认服务和独立应用仅支持受控 AST。需要字符串表达式时，由宿主在启动时提供配置：

```java
var engines = new XtreamExpressionEngineRegistry(false);
engines.register(new SpelXtreamExpressionEngine());
var options = new DynamicCompileOptions(
        DynamicSchema.ALL_VERSION, DynamicExtensionRegistry.EMPTY, engines);
var service = new CodecDebuggerService(catalog, new Jackson2DebugEntityValueMapper(), options, DynamicLimits.DEFAULT);
```

请求中的 version 覆盖编译配置的版本，扩展和表达式引擎继续使用宿主配置。DSL 可以在 derived.expression、when.expression、length.expression 中声明 engine/source/bindings；source 遵守所选引擎语法，例如 SpEL 使用 #n，Aviator/MVEL 使用 n。配置加载及自定义引擎的并发合同见 core 动态 DSL 文档。

HTTP 不提供引擎注册接口，导入 JSON 不会启用引擎。未启用的标识在编译期返回业务错误；求值失败仍返回字段位置和已完成的 Trace。宿主负责可信定义及引擎自身的权限和资源策略。
