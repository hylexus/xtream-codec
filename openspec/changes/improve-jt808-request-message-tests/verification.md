# 验证记录

## 测试覆盖

- `request` 包的 38 个顶层消息类均有对应测试类，排除 `package-info.java`。
- 新增 `BuiltinMessage0100V2011Test`、`BuiltinMessage0102V2011Test`，完善 13 个已有请求测试类。
- 请求包用例由 75 个增加至 86 个，全部通过，无跳过。
- 核心模块新增字段版本顺序和 U32 字典键边界回归用例；修复前选定的 17 个核心用例中有 11 个失败，修复后全部通过。

## 完整模块测试

```bash
./gradlew :xtream-codec-core:test :ext:jt:jt-808-server-spring-boot-starter-reactive:test
```

核心模块 72 个测试类、559 个用例；JT808 模块 101 个测试类、246 个用例。合计 805 个用例通过，失败、错误及跳过均为 0。

## 风格与许可证

```bash
./gradlew :xtream-codec-core:checkstyleMain :xtream-codec-core:checkstyleTest \
  :xtream-codec-core:checkLicenseMain :xtream-codec-core:checkLicenseTest \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkstyleTest \
  :ext:jt:jt-808-server-spring-boot-starter-reactive:checkLicenseTest \
  -Pxtream.backend.build.checkstyle.enabled=true
```

全部通过。修改的源码和测试未被文档直接引用，本次无需更新文档代码片段。

## 变更校验

- `openspec validate improve-jt808-request-message-tests --strict` 通过。
- `git diff --check` 通过。
