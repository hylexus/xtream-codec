# dynamic-codec-authoring Specification

## Purpose

为动态二进制结构定义提供可分发的机器可读结构描述，并在现有调试页面中补全属性、枚举和字段引用，使定义作者无需反复查阅源码即可发现 DSL 支持的配置，同时保留编译器的关系校验职责。

## Requirements

### Requirement: JSON Schema 发布
系统 SHALL 发布 schemaVersion 1 的 Draft 2020-12 JSON Schema，描述字段类型、长度、表达式、生成字段和默认分支，并提供核心 API 和调试页面资源访问。

#### Scenario: 获取结构描述
- **WHEN** 调用方通过 API 或调试页面资源获取 JSON Schema
- **THEN** 返回同一版本的结构描述并可用于通用 JSON 编辑工具

### Requirement: 定义编辑补全
调试页面 SHALL 根据结构描述补全属性和枚举，并根据当前定义提供字段引用候选。

#### Scenario: 编辑定义
- **WHEN** 用户在定义编辑器触发补全
- **THEN** 页面展示当前 JSON 上下文适用的属性、枚举或字段引用，并能插入选项
