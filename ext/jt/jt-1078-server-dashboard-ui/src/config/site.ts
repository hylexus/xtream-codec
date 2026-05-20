import { LuGaugeIcon, LuDesktopIcon, LuTagsIcon } from "@/components/icons.tsx";

export const siteConfig = {
  name: "Xtream Codec",
  description: "JT/T 1078 流媒体服务监控",
  user: {
    name: "管理员",
    role: "Admin",
  },
  sidenav: [
    { name: "仪表盘", href: "/dashboard", icon: LuGaugeIcon },
    { name: "媒体会话", href: "/sessions", icon: LuDesktopIcon },
    { name: "数据订阅", href: "/subscribers", icon: LuTagsIcon },
  ],
  dashboardTabs: [
    { id: "overview", label: "概览" },
    { id: "sessions", label: "会话" },
    { id: "subscribers", label: "订阅" },
  ],
  links: {
    gitee: "https://gitee.com/hylexus/xtream-codec",
    github: "https://github.com/hylexus/xtream-codec",
    docs: "https://hylexus.github.io/xtream-codec",
    sponsor: "https://github.com/hylexus/jt-framework",
  },
};
