# JT1078 Dashboard UI 设计指南

对齐 [HeroUI Pro Dashboard](https://heroui.pro/templates/dashboard) · [在线 Demo](https://template-dashboard.heroui.pro/)。

## 原则

1. **默认主题**：色彩、圆角、阴影全部来自 `@heroui/styles`，`globals.css` 不覆盖语义色。
2. **原生 Card**：`DashboardCard` = `Card variant="default"` + `Card.Header` / `Card.Content` / `Card.Footer`。
3. **统一趋势**：`TrendChip`（`Chip` success/danger soft）。
4. **统一表格**：`TableCard` 外壳（Pro All Employees）。
5. **统一间距**：页面 `gap-6`，主区域 `px-6`，卡片内容 `p-6`。

## 布局

```
Sidebar (bg-background)  |  Main (dark: bg-background-secondary)
  Logo + 导航               |    MainHeader：问候 + 操作
  下载 / 帮助               |    Toolbar：Tabs secondary
  用户 + 退出               |    KPI → 图表 → 表格
```

## 组件映射（Pro → 本项目）

| Pro | 组件 |
|-----|------|
| Revenue / Expenses / Sales / Profit | `MetricCard` × 4 |
| Overview / Sales / Expenses tabs | `DashboardToolbar`（`Tabs variant="secondary"`） |
| Sales Performance + 内联指标 | `ChartCard` + `stats` |
| Traffic Source + 大数字 | `ChartCard` + `metric` |
| All Employees | `TableCard` / `SessionsPreviewTable` |
| 列表页完整表 | `DataTable` |

## 文件

- `src/styles/globals.css` — 仅布局 + `--chart-*`
- `src/components/ui/dashboard-card.tsx`
- `src/components/ui/table-card.tsx`
- `src/components/ui/trend-chip.tsx`
- `src/components/dashboard/*`
- `src/components/layout/main-header.tsx`

## 新增页面 checklist

1. `config/routes.ts` 增加 meta
2. `config/site.ts` 增加 sidenav
3. `App.tsx` 注册路由
4. 使用 `PageShell` + `DataTable` 或 `MetricCard` / `ChartCard`

## 构建

```bash
pnpm build
```

改主题请用 [HeroUI Theme Builder](https://heroui.com/themes)，勿在 `globals.css` 写 hex 色板。
