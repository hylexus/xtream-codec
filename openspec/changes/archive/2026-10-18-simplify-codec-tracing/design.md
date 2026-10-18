## Context

当前 `CodecTracker` 已经使用 `CodecTrace` 和 `CodecTraceNode` 作为数据模型，但编解码过程仍通过可变的 `current` 节点、临时 Map 类型提示和临时字段名来维护上下文。各类 codec 需要显式传递 parent、手动结束节点，并自行处理 slice 和临时 ByteBuf 的坐标换算。

本变更必须继续满足现有 `codec-tracing` 主 spec：tracker 编解码入口保持兼容，生产路径不收集 trace，嵌套实体、集合、Map、长度字段、错误诊断和 Web trace view 的结果合同保持不变。

## Goals / Non-Goals

**Goals:**

- 让普通字段、嵌套实体和集合的埋点只需要表达“进入、完成、失败”，不再操作当前节点指针。
- 自动维护 trace 节点的父子关系和生命周期，减少顺序敏感的恢复操作。
- 将根 ByteBuf、slice 和临时 ByteBuf 的坐标转换集中到独立的坐标映射能力中。
- 保持 `FieldCodec`、`EntityCodec`、`EntityEncoder`、`EntityDecoder` 的 tracker 入口以及 `CodecTracker#visit` 的源码兼容性。
- 让普通字段尽量只记录结构化值和 byte range，由 trace view 在最终 payload 已知时生成字段 hex。
- 保持 debug 路径和 production 路径的性能边界，未启用 tracker 时不创建 trace 节点。

**Non-Goals:**

- 不改变实体编解码语义、字段顺序、长度计算、报文布局或异常传播语义。
- 不在本变更中重写 JT808/JT1078 的协议专用 trace 逻辑。
- 不在本变更中重做 React 调试页面或 Web trace view 的视觉交互。
- 不立即删除已有公开的 tracker 辅助方法；兼容方法可以保留为委托适配层，后续是否废弃另行决定。
- 不把 trace scope 设计成面向业务使用方的通用埋点框架；它只服务于 codec 内部调试路径。

## Decisions

### 1. `CodecTracker` 保留为兼容门面

`CodecTracker` 继续作为用户创建 tracker、获取 `CodecTrace`、转换 `CodecTraceView` 和执行 `visit` 的入口。它不再承担完整的埋点状态机，而是委托给一次 trace operation 对应的内部上下文。

仓库内已经没有调用的 `startNew...Span`、`finishCurrentSpan`、手动坐标栈和通用 `add...Span` 方法直接删除，不再为未正式承诺的内部辅助 API 保留兼容层。长度回填改用显式 `DeferredNode`，需要获取或迁移一次操作所产生节点的流程改用 `TraceCheckpoint`；checkpoint 在后续兄弟节点创建前封存本次新增节点，调用方不再读取 parent 或子节点下标。Map item 类型和动态 value 名称改用有明确生命周期、只消费一次的 `NodeOverrideScope`。完成迁移后删除 `getCurrentSpan()`、可变 `current` 指针、一次性 hint 字段及旧的 Span 更新方法。

该取舍会影响直接调用旧 tracker 辅助方法的自定义 codec，但不会影响已经明确要求兼容的 `FieldCodec`、`EntityCodec`、`EntityEncoder`、`EntityDecoder` 和 `CodecTracker#visit` 等入口。项目仍处于 0.9.0 预发布阶段，因此选择在本版本收缩这部分意外暴露的 API。

### 2. 使用内部 scope 栈表达节点生命周期

内部引入只面向 core 编解码实现的 trace context。context 维护当前 operation 的节点栈，并提供以下语义：

```text
enter(kind, metadata) -> scope
scope.complete(value, location)
scope.fail(error, location)
scope.close()
```

`enter` 自动把新节点挂到栈顶节点下；scope 完成或关闭时自动恢复父节点。调用方不再传递 parent，也不再调用 `getCurrentSpan()` 或 `finishCurrentSpan()`。

普通字段使用一个 scope；嵌套实体、集合字段、集合项和 Map entry 使用嵌套 scope。节点种类仍由 `CodecTraceNodeKind` 表达，scope 只是生命周期和父子关系的实现方式，不新增一套平行的节点模型。

scope 的异常语义必须明确：正常完成时提交值和范围；在 codec 抛出异常且 scope 尚未完成时，保留已创建节点并把节点标记为错误。operation 级异常处理把原始异常关联到最深的未完成节点，祖先节点只传播错误状态，不为同一异常重复生成诊断。不能因为异常退出而丢弃此前已经成功记录的兄弟节点。

所有 `TraceScope` 调用方统一使用 try-with-resources 管理生命周期，不再手动调用 `close()`。标准异常收尾由 tracker 统一完成；只有需要恢复、丢弃节点、转换异常或继续执行时，调用方才使用显式 `try/catch`。不引入吞入整段编解码逻辑的 lambda executor、回调模板或通用执行器，避免重新隐藏编解码步骤和异常位置。

`enterScope` 保留完整节点参数入口，供 Map entry、collection item 和 DataField 等合成节点使用；同时提供接收 `BeanPropertyMetadata` 和处理组件类型的重载，统一提取属性名称、Java 类型和字段说明，避免调用方重复传递多个相邻字符串参数。

替代方案是继续使用 `current` 指针加 `finishCurrentSpan()`。它的代码量较少，但调用顺序只能靠约定维持，Map、动态类型和嵌套 codec 很容易出现父节点错位，因此不采用。

### 3. 将坐标映射与节点记录分离

节点记录只关心逻辑位置；坐标映射器负责把不同 ByteBuf segment 中的局部位置转换为 root payload 的相对位置。

```text
RootSegment       [0, N)
  SliceSegment    root + local offset
  TemporarySegment [0, M) --attach--> RootSegment [start, start + M)
```

进入 slice 时创建带基准位置的 segment；在临时 ByteBuf 中编码时，节点先记录临时 segment 的局部范围；当临时内容真正写入正式输出时，由 segment attach 操作一次性平移整个子树。普通字段不需要显式调用 `pushCoordinateBase`、`popCoordinateBase` 或 `relocateTemporaryChildren`。

Map 的 value 先编码、后写入 value length 和 value bytes 是该机制的主要使用场景。这个特殊流程保留在 Map codec 内，但它只操作内部 segment/attach 能力，不向普通 FieldCodec 暴露坐标细节。

替代方案是让每个 codec 自己维护绝对 reader/writer index。该方案在简单字段上可行，但无法统一处理嵌套 slice 和临时输出，也正是当前实现重复和出错的来源。

### 4. 把字段范围作为首要记录结果

埋点过程中优先记录字段的开始位置、结束位置、值、处理组件类型和元数据。处理组件统一使用 `processorType` 表示实际负责当前节点编解码或流程组织的类：基础字段记录 FieldCodec，容器节点记录对应 metadata，长度字段记录长度策略，DataField 节点记录其编码器。该字段不再使用只能准确描述部分节点的 `codecType` 名称。对于属于 root payload 的节点，`CodecTraceView` 在 payload 完成后可以根据范围生成局部 hex；只有临时 segment 尚未附着、或 payload 不可用的节点，才需要保留独立的局部 hex。

这不会改变现有 trace view 对字段范围和 hex 的消费合同。`CodecTraceNode#getHex()` 在兼容需要时仍可提供值，但不再要求每一个默认 tracker 方法都立即调用 `FormatUtils.toHexString`。

替代方案是继续在每个 FieldCodec tracker 方法中立即生成 hex。实现直观，但会重复扫描 ByteBuf，也会把展示数据格式耦合到每一个 codec 的埋点调用点。

### 5. 保留两条公开编解码路径，收敛内部埋点入口

不启用 tracker 时继续走生产路径，不创建 scope、节点或 Web view 类型。启用 tracker 时保留现有 `withTracker` 入口，以确保自定义 FieldCodec 的行为和性能边界不被无意改变。

在 debug 分支内部，基础字段统一通过 context 的字段 scope 记录；嵌套实体、集合、Map、TLV、Pair 和动态类型逐步迁移到同一套 scope 语义。特殊 codec 可以创建专用 scope，但不能重新引入一套独立的 current/hint 状态。

这样既不把 tracker 判空逻辑扩散到生产 codec，也不要求一次性把所有生产和 debug 实现强行合并成一个带 no-op sink 的算法。

### 6. 使用测试验证结果合同，而不是内部调用次数

测试重点放在可观察结果：

- 普通字段、嵌套实体、集合、Map、TLV 和动态类型的树结构与 byte range。
- 临时 value attach 后的最终范围和长度字段位置。
- slice 嵌套时范围相对于 root payload 的正确性。
- 异常时已完成节点和诊断信息的保留。
- `visit` 的深度优先顺序。
- tracker 与非 tracker 编解码结果一致，以及生产路径不创建 trace 节点。

不把 `CodecTracker` 内部调用了多少次 `enter` 或是否调用某个兼容方法作为测试合同。

## Risks / Trade-offs

- **自定义 codec 直接调用旧 tracker 辅助方法** → 删除仓库内零调用且未承诺兼容的旧方法；仍承担特殊流程的方法在完成显式 API 迁移后再删除。
- **scope 未正常结束导致父节点或范围不完整** → scope 必须支持异常收尾；在 operation 结束时校验未关闭 scope，并把异常写入诊断而不是静默丢失。
- **异常逐层退出产生重复诊断** → 仅把原始异常关联到最深的未完成节点，祖先节点只标记错误状态。
- **临时 segment attach 后范围偏移错误** → 为 root、slice、嵌套临时 buffer 和 Map value 分别增加范围断言测试，要求子节点范围始终位于父节点范围内。
- **重构后 debug 路径性能退化** → 保持 tracker 为空时的生产分支不创建 trace 对象，并使用现有 codec benchmark 或基准测试比较 tracker 与非 tracker 路径。
- **过早移除节点 hex 造成现有页面或调用方不兼容** → 第一阶段保留 `getHex()` 和 view 字段；仅改变其生成位置，确认消费者迁移后再考虑进一步收缩模型。

## Migration Plan

1. 在现有 `CodecTrace` 模型旁引入内部 trace context、scope 和 segment/坐标映射能力，不改变公开入口。
2. 先迁移普通字段、嵌套实体和集合 tracker 分支，保持旧 tracker 辅助方法可用，并补充结果合同测试。
3. 迁移 Map、TLV、Pair、动态类型和长度字段等需要特殊层级或临时 buffer 的分支。
4. 删除仓库内零调用的旧辅助方法，以显式节点句柄替代特殊流程对 current 的访问，以有界 override scope 替代一次性 hint 字段。
5. 在 core 测试、JT808 dashboard 编码/解码测试和生产路径测试中验证兼容性。
6. 完成一次性能比较和完整构建后，再单独评估是否在后续版本废弃旧的公开辅助方法。

回滚时可以保留兼容门面和现有 `CodecTrace` 数据模型，只回退内部 context/scope 的调用迁移；不会影响 production 编解码 API 或协议报文格式。
