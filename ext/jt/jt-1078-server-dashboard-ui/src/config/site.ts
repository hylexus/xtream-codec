import { LuGaugeIcon, LuDesktopIcon, LuTagsIcon } from "@/components/icons.tsx";

export const siteConfig = {
  name: "Xtream",
  description: "JT/T 1078 流媒体服务监控",
  sidenav: [
    { name: "仪表盘", href: "/dashboard", icon: LuGaugeIcon },
    { name: "媒体会话", href: "/sessions", icon: LuDesktopIcon },
    { name: "数据订阅", href: "/subscribers", icon: LuTagsIcon },
  ],
  links: {
    gitee: "https://gitee.com/hylexus/xtream-codec",
    github: "https://github.com/hylexus/xtream-codec",
    sponsor: "https://github.com/hylexus/jt-framework",
  },
};
