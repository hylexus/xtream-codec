import { Button } from "@heroui/react";
import useSWR from "swr";

import { ChartCard } from "@/components/dashboard/chart-card.tsx";
import { DashboardToolbar } from "@/components/dashboard/dashboard-toolbar.tsx";
import { MetricCard } from "@/components/dashboard/metric-card.tsx";
import { SalesBarChart } from "@/components/dashboard/sales-bar-chart.tsx";
import { SessionsPreviewTable } from "@/components/dashboard/sessions-preview-table.tsx";
import { TrafficLineChart } from "@/components/dashboard/traffic-line-chart.tsx";
import { PageShell } from "@/components/ui/page-shell.tsx";
import { usePageList } from "@/hooks/use-page-list.ts";
import { Jt1078Session } from "@/types";
import { request } from "@/utils/request.ts";

const PREVIEW_PAGE_SIZE = 6;

export const DashboardPage = () => {
  const { data: sessions } = useSWR("dashboard-sessions-count", () =>
    request<{ total: number }>({
      path: "sessions",
      method: "GET",
      params: { pageNumber: 1, pageSize: 1 },
    }),
  );
  const { data: subscribers } = useSWR("dashboard-subscribers-count", () =>
    request<{ total: number }>({
      path: "subscribers",
      method: "GET",
      params: { pageNumber: 1, pageSize: 1 },
    }),
  );

  const { tableData: previewSessions, isLoading: previewLoading } =
    usePageList<Jt1078Session>("sessions", PREVIEW_PAGE_SIZE);

  const sessionTotal = sessions?.total ?? 0;
  const subscriberTotal = subscribers?.total ?? 0;
  const previewItems = previewSessions?.data ?? [];
  const previewTotal = previewSessions?.total ?? sessionTotal;

  const kpis = [
    {
      key: "revenue",
      label: "媒体会话",
      value: sessions?.total ?? "—",
      trend: { value: "3.3%", direction: "up" as const },
    },
    {
      key: "expenses",
      label: "数据订阅",
      value: subscribers?.total ?? "—",
      trend: { value: "3.3%", direction: "up" as const },
    },
    {
      key: "sales",
      label: "活跃连接",
      value: sessionTotal,
      trend: { value: "1.2%", direction: "up" as const },
    },
    {
      key: "profit",
      label: "订阅通道",
      value: subscriberTotal,
      trend: { value: "4.1%", direction: "up" as const },
    },
  ];

  const salesStats = [
    {
      label: "会话总量",
      value: sessionTotal,
      trend: { value: "3.3%", direction: "up" as const },
    },
    {
      label: "周均连接",
      value: sessionTotal > 0 ? Math.round(sessionTotal / 7) : 0,
      trend: { value: "3.3%", direction: "up" as const },
    },
    {
      label: "日均连接",
      value: sessionTotal > 0 ? Math.round(sessionTotal / 14) : 0,
      trend: { value: "3.3%", direction: "up" as const },
    },
    {
      label: "订阅总数",
      value: subscriberTotal,
      trend: { value: "4.1%", direction: "up" as const },
    },
  ];

  return (
    <PageShell toolbar={<DashboardToolbar />}>
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-4">
        {kpis.map((kpi) => (
          <MetricCard
            key={kpi.key}
            label={kpi.label}
            trend={kpi.trend}
            value={kpi.value}
          />
        ))}
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <ChartCard
          actions={
            <Button size="sm" variant="secondary">
              近 2 周
            </Button>
          }
          bodyClassName="h-52 p-6 sm:h-56"
          className="lg:col-span-2"
          stats={salesStats}
          title="会话表现"
        >
          <SalesBarChart />
        </ChartCard>

        <ChartCard
          bodyClassName="h-52 p-6 pt-20 sm:h-56 sm:pt-24"
          legend={
            <div className="flex items-center gap-4 text-xs text-muted">
              <span className="flex items-center gap-1.5">
                <span className="size-2 rounded-full bg-accent" />
                TCP
              </span>
              <span className="flex items-center gap-1.5">
                <span className="size-2 rounded-full bg-default" />
                UDP
              </span>
            </div>
          }
          metric={{ suffix: "Sessions", value: sessionTotal }}
          title="流量来源"
        >
          <TrafficLineChart />
        </ChartCard>
      </div>

      <SessionsPreviewTable
        items={previewItems}
        loading={previewLoading && previewItems.length === 0}
        total={previewTotal}
      />
    </PageShell>
  );
};
