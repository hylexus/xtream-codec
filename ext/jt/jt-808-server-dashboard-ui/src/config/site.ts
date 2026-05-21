// export type SiteConfig = typeof siteConfig;

import {
  LuFileCirclePlusIcon,
  LuTagsIcon,
  LuShuffleIcon,
  LuGaugeIcon,
  LuCommentsIcon,
  LuGearIcon,
  LuCloneIcon,
  LuChartSimpleIcon,
  LuBugIcon,
  LuCubesIcon,
  LuDataBaseIcon,
} from "@/components/icons.tsx";

export const siteConfig = {
  name: "Xtream Codec",
  description: "JT/T 808 服务监控控制台",
  user: {
    name: "管理员",
    email: "admin@example.com",
  },
  dashboardTabs: [
    { id: "overview", label: "Overview" },
    { id: "instruction", label: "Instruction" },
    { id: "attachment", label: "Attachment" },
    { id: "subscriber", label: "Subscriber" },
  ],
  sidenav: [
    { name: "仪表盘", href: "/dashboard", icon: LuGaugeIcon },
    { name: "指令服务客户端", href: "/instruction", icon: LuCommentsIcon },
    {
      name: "附件服务客户端",
      href: "/attachment",
      icon: LuFileCirclePlusIcon,
    },
    { name: "数据订阅", href: "/subscriber", icon: LuTagsIcon },
    { name: "请求映射", href: "/mappings", icon: LuShuffleIcon },
    { name: "线程转储", href: "/dump", icon: LuCloneIcon },
    { name: "线程监控", href: "/threads", icon: LuChartSimpleIcon },
    { name: "编解码器", href: "/codec-metadata", icon: LuDataBaseIcon },
    { name: "类元信息", href: "/bean-metadata", icon: LuCubesIcon },
    { name: "配置", href: "/configuration", icon: LuGearIcon },
    { name: "调试", href: "/debug", icon: LuBugIcon },
  ],
  links: {
    gitee: "https://gitee.com/hylexus/xtream-codec",
    github: "https://github.com/hylexus/xtream-codec",
    docs: "https://hylexus.github.io/xtream-codec",
    sponsor: "https://github.com/hylexus/xtream-codec",
  },
};
