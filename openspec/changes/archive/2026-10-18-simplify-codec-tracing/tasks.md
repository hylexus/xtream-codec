## 1. 建立兼容基线

- [x] 1.1 梳理并固定 `FieldCodec`、`EntityCodec`、`EntityEncoder`、`EntityDecoder` 和 `CodecTracker#visit` 的现有 tracker 入口测试
- [x] 1.2 为普通字段、嵌套实体、集合、Map、长度字段和异常场景补充当前 trace 结果基线
- [x] 1.3 确认 tracker 为空时仍走 production 路径，并为不创建 trace 节点增加验证

## 2. 实现内部 scope 记录上下文

- [x] 2.1 新增只供 core 内部使用的 trace context 和 scope 栈，支持 enter、complete、fail 和异常收尾
- [x] 2.2 让 scope 自动维护父子节点关系和容器节点范围，消除普通流程对 `current` 指针和显式 parent 的依赖
- [x] 2.3 将 `CodecTracker` 改为委托内部 context，同时保留已有公开 tracker 辅助方法的兼容适配
- [x] 2.4 增加未关闭 scope、重复完成和错误收尾的测试，确保部分成功节点不会丢失

## 3. 内聚 ByteBuf 坐标映射

- [x] 3.1 实现 root segment 和 slice segment 的统一坐标转换，并验证嵌套 slice 的范围归属
- [x] 3.2 实现临时 segment 到正式输出 segment 的 attach 操作，支持整棵子树的范围迁移
- [x] 3.3 将临时 buffer 和坐标映射的细节从普通 FieldCodec 埋点入口中移除
- [x] 3.4 为 Map value、嵌套实体和动态 value 增加临时 segment 范围测试

## 4. 迁移各类 tracker 编解码分支

- [x] 4.1 将 `FieldCodec` 默认 tracker 方法迁移到统一字段 scope，并保留长度字段节点语义
- [x] 4.2 迁移嵌套实体、集合、集合项、RuntimeType 和 DataField 的 tracker 分支
- [x] 4.3 迁移 Map、Map entry、TLV、Pair 及其动态 value 的 tracker 分支
- [x] 4.4 清理 core 内部对 `getCurrentSpan`、`finishCurrentSpan`、一次性 tracker hints 和手动坐标栈的依赖，以显式句柄承载长度回填、临时节点迁移和单次节点元数据覆盖
- [x] 4.5 验证编码和解码两条 debug 路径的节点顺序、父子关系和最终 byte range 一致

## 5. 保持 trace view 和 hex 合同

- [x] 5.1 让 root payload 可用时由范围生成字段 hex，并为临时或缺少 payload 的节点保留兼容 fallback
- [x] 5.2 验证 `CodecTraceView` 的字段范围、值表示、诊断和前端双向定位数据不发生回归
- [x] 5.3 验证 `CodecTracker#visit` 仍按确定的深度优先顺序访问 root 和全部后代节点

## 6. 集成验证与收尾

- [x] 6.1 运行 core tracker 测试、实体编解码测试和 JT808 dashboard 相关测试
- [x] 6.2 对比 tracker 与 production 编解码结果，并检查 tracker 路径的性能变化（结果一致；平铺实体基准中 tracker 编码/解码约为 production 的 20.4%/21.1%）
- [x] 6.3 运行 `build-script/before-commit.sh` 和必要的完整构建检查
- [x] 6.4 检查兼容适配层的公开方法和 JavaDoc，删除仓库内零调用的旧 Span、`current` 指针、一次性 hint 字段、手动坐标栈和通用节点辅助 API
- [x] 6.5 使用可封存范围的 `TraceCheckpoint` 迁移剩余临时子树坐标操作，删除公开的手工 parent/子节点下标迁移入口
- [x] 6.6 删除 recorder 中零调用的节点辅助方法，收缩 recorder 实现的可见性并完成回归验证
- [x] 6.7 将遗留的 `FlattedSpan` marker 重命名为语义明确的 `FlattenedTrace`，清除生产代码中的旧 Span 术语
- [x] 6.8 将生产代码中的 `TraceScope` 生命周期统一为 try-with-resources，并记录禁止引入 lambda executor 或回调模板的维护约束

## 7. 收敛 scope API 与异常语义

- [x] 7.1 将 trace 节点和 Web view 的 `codecType` 统一迁移为 `processorType`，同步协议侧复制逻辑、前端类型和展示文案
- [x] 7.2 为 `CodecTracker#enterScope` 增加接收 `BeanPropertyMetadata` 和处理组件类型的重载，并迁移适用调用方
- [x] 7.3 由 tracker 将 operation 异常关联到最深的未完成 scope，祖先节点只标记错误，清理仅执行 `fail/discard` 后原样抛出的重复 catch
- [x] 7.4 补充 processor 类型、最深失败节点和单诊断语义测试，更新文档示例并完成相关模块验证
