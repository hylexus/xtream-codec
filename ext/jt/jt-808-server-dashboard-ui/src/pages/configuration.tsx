import { useRouteLoaderData } from "react-router-dom";

import { JsonPreview } from "@/components/json-preview.tsx";
import { ServerInfo } from "@/types";

export const ConfigurationPage = () => {
  const { config } = useRouteLoaderData("root") as { config: ServerInfo };

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight text-foreground">
          服务配置
        </h2>
        <p className="mt-1 max-w-2xl text-sm leading-relaxed text-muted">
          当前 JT808 服务配置树；与 Ant Design Tree 的样式已绑定到主题变量。
        </p>
      </div>
      <div className="min-h-0 rounded-2xl border border-border bg-surface/40 p-3 md:p-4">
        <JsonPreview json={config.jt808ServerConfig} />
      </div>
    </div>
  );
};
