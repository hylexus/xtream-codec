# JT808 Dashboard UI 设计指南

与 `jt-1078-server-dashboard-ui` 共用同一套 [HeroUI Pro Dashboard](https://heroui.pro/templates/dashboard) 规范。

## 布局

- 标题：`MainHeader` + `config/routes.ts`
- 首页：`DashboardOverview`（KPI ← SSE · 图表 ← metrics/threads · 预览表 ← 指令会话 API · 明细 Stat 卡）
- 列表页：`PageSection` + `DataTable` / `TableCard`
- `card-box.tsx` 为 `DashboardOverview` 别名

## 组件

| 用途 | 路径 |
|------|------|
| KPI | `dashboard/metric-card.tsx` |
| 图表 | `dashboard/chart-card.tsx` |
| 列表表 | `ui/data-table.tsx` |
| 排版 | `ui/dashboard-typography.tsx` |

## 构建

```bash
pnpm build
```
