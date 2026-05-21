import { Button, Tabs } from "@heroui/react";
import { Download, RefreshCw } from "lucide-react";
import { useNavigate } from "react-router-dom";

import { siteConfig } from "@/config/site.ts";

export function DashboardToolbar() {
  const navigate = useNavigate();

  return (
    <div className="flex flex-wrap items-center justify-between gap-4">
      <Tabs
        selectedKey="overview"
        variant="secondary"
        onSelectionChange={(key) => {
          if (key === "overview") {
            navigate("/dashboard");
          } else if (key === "sessions") {
            navigate("/sessions");
          } else if (key === "subscribers") {
            navigate("/subscribers");
          }
        }}
      >
        <Tabs.ListContainer>
          <Tabs.List aria-label="仪表盘视图">
            {siteConfig.dashboardTabs.map((tab) => (
              <Tabs.Tab key={tab.id} id={tab.id}>
                {tab.label}
                <Tabs.Indicator />
              </Tabs.Tab>
            ))}
          </Tabs.List>
        </Tabs.ListContainer>
      </Tabs>

      <div className="flex flex-wrap items-center gap-2">
        <Button isIconOnly aria-label="刷新" size="sm" variant="ghost">
          <RefreshCw className="size-4 text-muted" strokeWidth={1.75} />
        </Button>
        <Button size="sm" variant="secondary">
          本月
        </Button>
        <Button size="sm" variant="primary">
          <span className="flex items-center gap-1.5">
            <Download className="size-4" strokeWidth={1.75} />
            导出
          </span>
        </Button>
      </div>
    </div>
  );
}
