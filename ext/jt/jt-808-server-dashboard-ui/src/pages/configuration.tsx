import { useRouteLoaderData } from "react-router-dom";

import { JsonPreview } from "@/components/json-preview.tsx";
import { PageSection } from "@/components/page-header.tsx";
import { ServerInfo } from "@/types";

export const ConfigurationPage = () => {
  const { config } = useRouteLoaderData("root") as { config: ServerInfo };

  return (
    <PageSection
      description="当前 JT808 服务配置树；与 Ant Design Tree 的样式已绑定到主题变量。"
      title="服务配置"
    >
      <div className="min-h-0 rounded-2xl border border-border bg-surface/40 p-3 md:p-4">
        <JsonPreview json={config.jt808ServerConfig} />
      </div>
    </PageSection>
  );
};
