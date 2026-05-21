export type RouteMeta = {
  showGreeting?: boolean;
  title?: string;
  description?: string;
};

export const routeMetaByPath: Record<string, RouteMeta> = {
  "/": { showGreeting: true },
  "/dashboard": { showGreeting: true },
  "/sessions": {
    title: "媒体会话",
    description: "当前 JT1078 流媒体连接会话列表",
  },
  "/subscribers": {
    title: "数据订阅",
    description: "流媒体通道上的订阅方列表及元数据",
  },
};

export function metaForPath(pathname: string): RouteMeta {
  if (pathname === "/" || pathname === "") {
    return routeMetaByPath["/"];
  }

  return routeMetaByPath[pathname] ?? {};
}
