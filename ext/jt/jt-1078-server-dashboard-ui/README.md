# JT1078 Server Dashboard UI

JT/T 1078 流媒体服务监控控制台（Vite + React 19 + HeroUI v3）。

## 文档

- **[设计指南](./docs/DESIGN_GUIDE.md)** — 色彩、布局、组件使用规范（开发前必读）

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
    ui/           # 设计系统基元（DashboardCard、PageShell、DataTable）
    dashboard/    # 仪表盘业务组件（MetricCard、ChartCard）
  styles/
    globals.css   # 主题令牌与 utility 类
docs/
  DESIGN_GUIDE.md
```
