import { Button } from "@heroui/react";
import useSWR from "swr";

import { ChartCard } from "@/components/dashboard/chart-card.tsx";
import { DashboardToolbar } from "@/components/dashboard/dashboard-toolbar.tsx";
import { MetricCard } from "@/components/dashboard/metric-card.tsx";
import { SalesBarChart } from "@/components/dashboard/sales-bar-chart.tsx";
import { TrafficLineChart } from "@/components/dashboard/traffic-line-chart.tsx";
import { PageShell } from "@/components/ui/page-shell.tsx";
import { request } from "@/utils/request.ts";

export const DashboardPage = () => {
  const { data: sessions } = useSWR("sessions-count", () =>
    request<{ total: number }>({
      path: "sessions",
      method: "GET",
      params: { pageNumber: 1, pageSize: 1 },
    }),
  );
  const { data: subscribers } = useSWR("subscribers-count", () =>
    request<{ total: number }>({
      path: "subscribers",
      method: "GET",
      params: { pageNumber: 1, pageSize: 1 },
    }),
  );

  const sessionTotal = sessions?.total ?? 0;
  const subscriberTotal = subscribers?.total ?? 0;

  const stats = [
    {
      key: "sessions",
      label: "媒体会话",
      value: sessions?.total ?? "—",
      trend: { value: "3.3%", direction: "up" as const },
    },
    {
      key: "subscribers",
      label: "数据订阅",
      value: subscribers?.total ?? "—",
      trend: { value: "3.3%", direction: "up" as const },
    },
    {
      key: "active",
      label: "活跃连接",
      value: sessionTotal,
      trend: { value: "1.2%", direction: "up" as const },
    },
    {
      key: "channels",
      label: "订阅通道",
      value: subscriberTotal,
      trend: { value: "0.8%", direction: "down" as const },
    },
  ];

  return (
    <PageShell showGreeting toolbar={<DashboardToolbar />}>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 sm:gap-5 xl:grid-cols-4">
        {stats.map((stat) => (
          <MetricCard
            key={stat.key}
            label={stat.label}
            trend={stat.trend}
            value={stat.value}
          />
        ))}
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3 lg:gap-5">
        <ChartCard
          actions={
            <Button className="text-muted" size="sm" variant="secondary">
              近 2 周
            </Button>
          }
          bodyClassName="h-52 p-4 sm:h-56"
          className="lg:col-span-2"
          subtitle="近两周流媒体连接趋势（示意）"
          title="会话趋势"
        >
          <SalesBarChart />
        </ChartCard>

        <ChartCard
          bodyClassName="h-52 p-4 sm:h-56"
          legend={
            <div className="flex items-center gap-4 text-xs text-muted">
              <span className="flex items-center gap-1.5">
                <span className="size-2 rounded-full bg-[var(--chart-primary)]" />
                TCP
              </span>
              <span className="flex items-center gap-1.5">
                <span className="size-2 rounded-full bg-[var(--chart-secondary)]" />
                UDP
              </span>
            </div>
          }
          title="协议分布"
        >
          <TrafficLineChart />
        </ChartCard>
      </div>
    </PageShell>
  );
};
