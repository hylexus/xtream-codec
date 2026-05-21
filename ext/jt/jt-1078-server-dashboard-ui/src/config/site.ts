import {
  Download,
  LayoutDashboard,
  Radio,
  Users,
} from "lucide-react";

export const siteConfig = {
  name: "Xtream Codec",
  description: "JT/T 1078 流媒体服务监控",
  user: {
    name: "管理员",
    role: "Admin",
    email: "admin@example.com",
  },
  sidenav: [
    { name: "概览", href: "/dashboard", icon: LayoutDashboard },
    { name: "媒体会话", href: "/sessions", icon: Radio },
    { name: "数据订阅", href: "/subscribers", icon: Users },
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

export const sidebarFooterLinks = [
  { name: "下载报告", href: siteConfig.links.docs, icon: Download },
];
