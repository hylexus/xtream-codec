## TODO

- [ ] 7: [jt-808-server-dashboard-ui](ext/jt/jt-808-server-dashboard-ui) 优化
- [x] 6: 补充 **JT/T 808** 解析示例
    - 示例位于 `io.github.hylexus.xtream.codec.ext.jt808.builtin.messages` 包
- [ ] 5: 术语重命名为 `Jt808` 标准中出现的单词
    - 模块: jt-808 扩展
- [x] 4: **UDP** 会话管理
    - 模块: **xtream-codec-server-reactive**
    - 模块: jt-808 扩展、指令服务器 + 附件服务器
- [x] 3: `XtreamResponseBodyHandlerResultHandler` 支持从实体类上读取 `@XtreamResponseBody`
    - 模块: **xtream-codec-server-reactive**
- [x] 2: `Jt808ResponseBodyHandlerResultHandler` 支持从实体类上读取 `@XtreamResponseBody`
    - 模块: jt-808 扩展
- [x] 1: `FieldCodec` 的序列化方法完善
    - 模块: **xtream-codec-core**
    - 描述: `FieldCodec` 的序列化方法扩展元数据参数: 正在进行序列化的 `Field` 的元数据
