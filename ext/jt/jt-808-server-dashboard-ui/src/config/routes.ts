export type RouteMeta = {
  showGreeting?: boolean;
  title?: string;
  description?: string;
};

export const routeMetaByPath: Record<string, RouteMeta> = {
  "/": { showGreeting: true },
  "/dashboard": { showGreeting: true },
  "/instruction": {
    title: "指令服务在线会话",
    description: "808 指令通道上的活跃会话列表",
  },
  "/attachment": {
    title: "附件服务在线会话",
    description: "808 附件通道上的活跃会话列表",
  },
  "/subscriber": {
    title: "数据订阅",
    description: "事件发布器上的订阅方列表及元数据",
  },
  "/mappings": {
    title: "请求映射",
    description: "按消息 ID、协议版本、处理器等维度查看 JT808 处理器注册情况",
  },
  "/dump": {
    title: "线程 Dump",
    description: "线程实时状态",
  },
  "/threads": {
    title: "调度器指标",
    description: "各调度器线程池的实时配置快照",
  },
  "/codec-metadata": {
    title: "编解码器",
    description: "JT808 消息编解码器元数据",
  },
  "/bean-metadata": {
    title: "类元信息",
    description: "Spring Bean 与组件元数据",
  },
  "/configuration": {
    title: "服务配置",
    description: "当前 JT808 服务配置树",
  },
  "/debug": {
    title: "调试",
    description: "开发调试工具（暂未开放）",
  },
};

export function metaForPath(pathname: string): RouteMeta {
  if (pathname === "/" || pathname === "") {
    return routeMetaByPath["/"];
  }

  return routeMetaByPath[pathname] ?? { title: "JT/T 808 服务面板" };
}
