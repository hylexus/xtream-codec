// export type SiteConfig = typeof siteConfig;

import {
  FaFileCirclePlusIcon,
  FaTagsIcon,
  FaShuffleIcon,
  FaGaugeIcon,
  FaCommentsIcon,
  FaGearIcon,
  FaCloneIcon,
  FaChartSimpleIcon,
  FaBugIcon,
  FaCubesIcon,
  FaDataBaseIcon,
} from "@/components/icons.tsx";

export const siteConfig = {
  name: "Xtream",
  description: "Xtream",
  sidenav: [
    { name: "仪表盘", href: "/dashboard", icon: FaGaugeIcon },
    { name: "指令服务客户端", href: "/instruction", icon: FaCommentsIcon },
    {
      name: "附件服务客户端",
      href: "/attachment",
      icon: FaFileCirclePlusIcon,
    },
    { name: "数据订阅", href: "/subscriber", icon: FaTagsIcon },
    { name: "请求映射", href: "/mappings", icon: FaShuffleIcon },
    { name: "线程转储", href: "/dump", icon: FaCloneIcon },
    { name: "线程监控", href: "/threads", icon: FaChartSimpleIcon },
    { name: "编解码器", href: "/codec-metadata", icon: FaDataBaseIcon },
    { name: "类元信息", href: "/bean-metadata", icon: FaCubesIcon },
    { name: "配置", href: "/configuration", icon: FaGearIcon },
    { name: "调试", href: "/debug", icon: FaBugIcon },
  ],
  links: {
    gitee: "https://gitee.com/hylexus/xtream-codec",
    github: "https://github.com/hylexus/xtream-codec",
    sponsor: "https://github.com/hylexus/jt-framework",
  },
};
