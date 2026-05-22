import { useRouteLoaderData } from "react-router-dom";

import { JsonPreview } from "@/components/json-preview.tsx";
import { PageSection } from "@/components/page-header.tsx";
import {
  DashboardCard,
  DashboardCardBody,
} from "@/components/ui/dashboard-card.tsx";
import { ServerInfo } from "@/types";

export const ConfigurationPage = () => {
  const { config } = useRouteLoaderData("root") as { config: ServerInfo };

  return (
    <PageSection>
      <DashboardCard>
        <DashboardCardBody className="p-6">
          <JsonPreview json={config.jt808ServerConfig} />
        </DashboardCardBody>
      </DashboardCard>
    </PageSection>
  );
};
