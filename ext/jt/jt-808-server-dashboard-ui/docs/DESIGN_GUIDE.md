# JT808 Dashboard UI 设计指南

与 `jt-1078-server-dashboard-ui` 共用同一套 [HeroUI Pro Dashboard](https://heroui.pro/templates/dashboard) 规范。

## 布局

- 标题：`MainHeader` + `config/routes.ts`
- 首页：`dashboard-overview.tsx`（KPI ← SSE · 图表 ← metrics/threads · 预览表 ← 指令会话 API · 通道指标表）
- 列表页：`PageSection` + `DataTable` / `TableCard`

## 组件

| 用途 | 路径 |
|------|------|
| 首页编排 | `dashboard/dashboard-overview.tsx` |
| KPI | `dashboard/metric-card.tsx` |
| 图表壳 | `dashboard/chart-card.tsx` |
| 流量趋势图 | `dashboard/traffic-line-chart.tsx`（SSE 会话数时序） |
| 线程迷你图 | `dashboard/threads-charts.tsx` |
| 通道指标（紧凑四格） | `dashboard/channel-metrics-card.tsx` |
| 服务信息 | `dashboard/server-info-section.tsx` |
| 首页小部件 | `dashboard/dashboard-widgets.tsx` |
| Thread Dump 页 | `dump/dump-view.tsx` |
| Dump 抽屉/堆栈 | `dump/dump-ui.tsx` |
| Dump 时间轴 | `dump/dump-timeline-chart.tsx` + `dump-timeline-bar.tsx` |
| 列表表 | `ui/data-table.tsx` |
| 排版 | `ui/dashboard-typography.tsx` |

## 构建

```bash
pnpm build
```
