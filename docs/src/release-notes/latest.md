---
icon: code-branch
article: false
---

# Latest

## 0.3.0(2025-10-26)

### 🎯 Highlights

- 默认属性访问策略由 `反射` 改为 `java.lang.invoke.LambdaMetafactory`

### ⭐ New Features

- 新增 `@XtreamEntity` 注解，支持配置类级别的属性访问策略
- 新增 `@XtreamField.propertyAccessStrategy()` 属性，支持配置字段级别的属性访问策略

### 🐞 Bug Fixes

- 修复 `AbstractJt808Message` 初始化异常

### 🔨 Dependency Updates

- 可空性标记全部使用 [jspecify](https://jspecify.dev/)
- 彻底移除 `jakarta.annotation-api`

## 0.2.0(2025-10-18)

### ⭐ New Features

- 增强 `Record` 类型的表达式解析功能
- 增强 `Record` 类型的 `CodecTracker` 埋点
- 重构 `BeanPropertyMetadata.PropertyGetter` 和 `BeanPropertyMetadata.PropertySetter` 的实现类
