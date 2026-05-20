# JT1078 Dashboard UI 设计指南

本文档是 `jt-1078-server-dashboard-ui` 的**唯一视觉与组件规范**，参照 [HeroUI Pro Dashboard](https://heroui.pro/templates/dashboard) 模板，并适配 JT/T 1078 监控场景。新增页面或组件前请先阅读本文。

## 1. 设计原则

1. **单一卡片语言**：KPI、图表、表格的外层容器必须使用 `DashboardCard`（`.dashboard-card`），禁止在页面内手写 `rounded-*` + `bg-*` 组合出第二套卡片样式。
2. **单一页面间距**：内容区使用 `.dashboard-page`（`gap-6 md:gap-8`），不要在页面根节点再叠一层不一致的 `gap-4` / `gap-10`。
3. **双主题成对定义**：颜色通过 `globals.css` 中 CSS 变量表达，组件只引用语义类名（`text-muted`、`bg-surface`、`border-border`），禁止硬编码 `#fff` / `#000`（布局特例除外，见下文）。
4. **HeroUI v3 一致**：表单、按钮、表格主体使用 `@heroui/react`；图标使用 `lucide-react`，描边 `strokeWidth={1.75}`。

## 2. 色彩令牌

### 2.1 语义色（Light / Dark）

| 令牌 | Light | Dark | 用途 |
|------|-------|------|------|
| `--background` | `#F4F4F5` | `#000000` | 应用背景 / 侧栏（dark） |
| 主画布 | `#F4F4F5` | `#0A0A0A` | `.dashboard-main-column` |
| `--surface` | `#FFFFFF` | `#18181B` | 卡片、顶栏 |
| `--foreground` | `#18181B` | `#FFFFFF` | 主文字 |
| `--muted` | `#71717A` | `#A1A1AA` | 次要文字 |
| `--default` | `#F4F4F5` | `#27272A` | 导航激活底、分段 Tab 底 |
| `--border` | `#E4E4E7` | `rgb(255 255 255 / 6%)` | 分割线、描边 |
| `--accent` | `#0070F3` | `#0070F3` | 主按钮、链接、图表主色 |
| `--success` | `#17C964` | `#17C964` | 上升趋势 |
| `--danger` | `#F31260` | `#F31260` | 下降趋势、删除 |

### 2.2 图表色

| 令牌 | 说明 |
|------|------|
| `--chart-primary` | 柱图 / 折线主系列（TCP、Organic） |
| `--chart-secondary` | 折线次系列（UDP、Paid Ads） |
| `--chart-grid` | 网格线 |

在 SVG 中使用类名：`.dashboard-chart-bar`、`.dashboard-chart-line-primary` 等。

## 3. 布局结构

```
┌─────────────┬──────────────────────────────────────┐
│  Sidebar    │  Navbar（搜索 / 通知 / 主题 / 邀请）   │
│  用户 + 导航  ├──────────────────────────────────────┤
│  帮助 / 退出  │  Main（.dashboard-main-column）       │
│             │    └─ .dashboard-page（max 1600px）    │
└─────────────┴──────────────────────────────────────┘
```

- **侧栏宽度**：展开 `260px`，收起 `76px`。
- **主内容内边距**：`px-4 pb-8 pt-4`（md：`px-8 pt-6`）。

## 4.  typography

| 层级 | 类名 | 场景 |
|------|------|------|
| 问候标题 | `.dashboard-greeting-title` | 仪表盘首页「早上好，xxx」 |
| 页面标题 | `.dashboard-page-title` | 列表页 H2 |
| 页面描述 | `.dashboard-page-description` | 标题下说明 |
| 卡片标题 | `text-sm font-semibold` | 卡片 Header 内 H3 |
| KPI 数值 | `.dashboard-metric-value` | 统计数字 |
| 表格单元格 | `.dashboard-table-cell-text` | 表格正文 |

## 5. 圆角与阴影

| 元素 | 圆角 | 阴影 |
|------|------|------|
| 卡片 | `rounded-2xl`（16px） | Light：`--surface-shadow`；Dark：顶内高光 `inset` |
| 按钮（主/次） | `rounded-xl` | 跟随 HeroUI |
| 导航项 | `rounded-xl` | 无 |
| 分段 Tab 容器 | `rounded-full` | 无 |
| 分段 Tab 激活项 | `rounded-full` | Light：`shadow-sm` |

## 6. 组件清单（必须使用）

路径均在 `src/components/`。

| 组件 | 用途 | 禁止 |
|------|------|------|
| `ui/dashboard-card` | 所有卡片外壳 + Header/Body/Footer 分区 | 裸 `Card` 加自定义边框 |
| `ui/page-shell` | 页面根布局（问候 / 标题 / 工具栏） | 页面根 `div` 随意 gap |
| `ui/data-table` | 列表页表格 + 工具栏 + 分页 | 复制粘贴 Table 结构 |
| `ui/list-table-toolbar` | 筛选 / 排序 / 列 | 每页各写一套按钮 |
| `ui/loading-panel` | 首屏加载 | 各处 `p-16` + Spinner |
| `dashboard/metric-card` | KPI 四宫格 | 自建统计卡 |
| `dashboard/chart-card` | 图表区 | 自建图表外框 |
| `dashboard/dashboard-toolbar` | 首页分段 Tab + 操作 | — |

### 6.1 DashboardCard 结构

```tsx
<DashboardCard>
  <DashboardCardHeader title="标题" subtitle="可选" actions={<>...</>} />
  <DashboardCardBody>或 flush</DashboardCardBody>
  <DashboardCardFooter>可选</DashboardCardFooter>
</DashboardCard>
```

- **Header**：统一顶栏分割线 `dashboard-card-header`。
- **Body flush**：表格类内容 `flush`，避免双重 padding。
- **Footer**：分页与「共 N 条」统计。

### 6.2 页面类型模板

**仪表盘首页**

```tsx
<PageShell showGreeting toolbar={<DashboardToolbar />}>
  <div className="grid ...">{/* MetricCard x4 */}</div>
  <div className="grid ...">{/* ChartCard */}</div>
</PageShell>
```

**列表页**

```tsx
<PageSection title="..." description="...">
  <DataTable ... />
</PageSection>
// 加载中：
<PageSection ...><LoadingPanel /></PageSection>
```

## 7. 导航与顶栏

- **侧栏激活**：`.dashboard-nav-active`（`bg-default` 药丸，无左侧色条）。
- **用户区**：渐变头像 `.dashboard-profile-gradient` + 姓名 + 角色。
- **顶栏**：`sticky` + 半透明 `bg-surface/90` + `backdrop-blur`；主操作「邀请」使用 `variant="primary"` + `rounded-xl`。

## 8. KPI 与趋势

- 标签：`text-sm font-medium text-muted`。
- 数值：`MetricCard` + `CountNumber`（数字动画）。
- 趋势徽章：`.dashboard-trend-up` / `.dashboard-trend-down`，带箭头图标。

## 9. 表格

- 外层：`DataTable` → `DashboardCard`。
- 工具栏：左 `ListTableToolbar`，右搜索框（`Input variant="secondary"` + 左侧 Search 图标）。
- 滚动：`dashboard-table-scroll`（`max-h-[70vh]`）。
- 分页：`DashboardCardFooter` 内居中 `PagePagination`。

## 10. 主题切换

- 使用 `ThemeSwitch`（`@heroui/react` 的 `useTheme`）。
- 默认深色；`html.dark` / `html:not(.dark)` 驱动 CSS 变量。
- 开发时务必在两种主题下各检查一页（仪表盘 + 列表）。

## 11. 文件与样式修改流程

1. **改颜色 / 间距**：先改 `src/styles/globals.css` 变量或 utility，再改本文档第 2、5 节。
2. **新卡片类型**：扩展 `dashboard-card.tsx`，不要新建 `*-card.tsx` 重复包一层。
3. **新列表页**：复制 `sessions.tsx` 模式，替换 columns 与 `renderCell`。
4. **提交前**：`pnpm build` 通过；目视 light/dark 无「一半新一半旧」组件。

## 12. 参考

- 模板：[HeroUI Pro Dashboard](https://heroui.pro/templates/dashboard)
- 在线预览：[template-dashboard.heroui.pro](https://template-dashboard.heroui.pro/)
- 实现入口：`src/styles/globals.css`、`src/components/ui/`
