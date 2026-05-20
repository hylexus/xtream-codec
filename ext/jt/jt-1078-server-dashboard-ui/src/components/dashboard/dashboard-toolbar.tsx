import { Button } from "@heroui/react";
import { Download, RefreshCw } from "lucide-react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { siteConfig } from "@/config/site.ts";

export function DashboardToolbar() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState("overview");

  const handleTab = (id: string) => {
    setActiveTab(id);
    if (id === "sessions") {
      navigate("/sessions");
    } else if (id === "subscribers") {
      navigate("/subscribers");
    }
  };

  return (
    <div className="flex flex-wrap items-center justify-between gap-4">
      <div className="dashboard-segment" role="tablist">
        {siteConfig.dashboardTabs.map((tab) => (
          <button
            key={tab.id}
            className="dashboard-segment-tab"
            data-active={activeTab === tab.id}
            role="tab"
            type="button"
            onClick={() => handleTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="flex flex-wrap items-center gap-2">
        <Button
          isIconOnly
          aria-label="刷新"
          className="text-muted"
          size="sm"
          variant="ghost"
        >
          <RefreshCw className="size-4" strokeWidth={1.75} />
        </Button>
        <Button className="text-muted" size="sm" variant="secondary">
          本月
        </Button>
        <Button className="rounded-xl" size="sm" variant="primary">
          <span className="flex items-center gap-1.5">
            <Download className="size-4" strokeWidth={1.75} />
            导出
          </span>
        </Button>
      </div>
    </div>
  );
}
