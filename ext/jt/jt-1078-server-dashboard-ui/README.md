# JT1078 Server Dashboard UI

JT/T 1078 流媒体服务监控控制台（Vite + React 19 + HeroUI v3）。

## 文档

- **[设计指南](./docs/DESIGN_GUIDE.md)** — 对齐 [HeroUI Pro Dashboard](https://heroui.pro/templates/dashboard) 的布局与组件规范（开发前必读）

## 开发

```bash
pnpm install
pnpm dev
```

生产构建：

```bash
pnpm build
```

API 代理见 `vite.config.ts`（默认 `/dashboard-api/jt1078` → 后端 `v1`）。

## 技术栈

- [Vite](https://vite.dev/)
- [HeroUI](https://heroui.com) v3
- [Tailwind CSS](https://tailwindcss.com) v4
- [React Router](https://reactrouter.com/)

## 目录约定

```
src/
  components/
    layout/       # MainHeader（Pro 顶栏）
    ui/           # DashboardCard、PageShell、DataTable
    dashboard/    # MetricCard、ChartCard、Toolbar
  config/
    routes.ts     # 各路由顶栏标题/描述
  styles/
    globals.css
docs/
  DESIGN_GUIDE.md
```
