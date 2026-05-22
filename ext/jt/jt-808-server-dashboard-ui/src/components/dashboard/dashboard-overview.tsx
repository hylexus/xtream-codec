import { NavLink, useRouteLoaderData } from "react-router-dom";

import { ChannelMetricsCard } from "@/components/dashboard/channel-metrics-card.tsx";
import { ChartCard } from "@/components/dashboard/chart-card.tsx";
import {
  ProtocolChartLegend,
  TcpUdpFooter,
} from "@/components/dashboard/chart-ui.tsx";
import { MetricCard } from "@/components/dashboard/metric-card.tsx";
import { ServerInfoSection } from "@/components/dashboard/server-info-section.tsx";
import { SessionsPreviewTable } from "@/components/dashboard/sessions-preview-table.tsx";
import { TrafficLineChart } from "@/components/dashboard/traffic-line-chart.tsx";
import { ThreadsCharts } from "@/components/dashboard/threads-charts.tsx";
import { useDashboardMetrics } from "@/hooks/use-dashboard-metrics.ts";
import { usePageList } from "@/hooks/use-page-list.ts";
import { ServerInfo, Session } from "@/types";
import {
  subscriberTotal,
  sumRequestTotal,
  sumSessionCurrent,
  threadChartPoint,
  threadLiveCount,
  trafficChartPoint,
} from "@/utils/dashboard-metrics.ts";
import { PageShell } from "@/components/ui/page-shell.tsx";

const PREVIEW_SIZE = 5;

export function DashboardOverview() {
  const { config } = useRouteLoaderData("root") as { config: ServerInfo };
  const { time, value: metrics } = useDashboardMetrics();
  const { tableData, isLoading } = usePageList<Session>(
    "session/instruction-sessions",
    PREVIEW_SIZE,
  );

  const tcpSessions = sumSessionCurrent(metrics, "TCP");
  const udpSessions = sumSessionCurrent(metrics, "UDP");
  const totalSessions = tcpSessions + udpSessions;

  return (
    <PageShell>
      <section
        aria-label="概览指标"
        className="grid grid-cols-2 items-stretch gap-3 lg:grid-cols-4"
      >
        <MetricCard
          action={
            <NavLink
              className="inline-flex items-center text-xs font-medium text-muted transition-colors hover:text-accent"
              to="/subscriber"
            >
              详情
            </NavLink>
          }
          label="订阅者"
          value={subscriberTotal(metrics)}
        />
        <MetricCard label="活跃会话" value={totalSessions} />
        <MetricCard label="请求总量" value={sumRequestTotal(metrics)} />
        <MetricCard label="活跃线程" value={threadLiveCount(metrics) ?? "—"} />
      </section>

      <ChannelMetricsCard metrics={metrics} />

      <section
        aria-label="图表"
        className="grid grid-cols-1 items-stretch gap-3 lg:grid-cols-12 lg:gap-4"
      >
        <ChartCard compact className="lg:col-span-8" title="线程趋势">
          <ThreadsCharts data={threadChartPoint(metrics, time)} />
        </ChartCard>

        <ChartCard
          className="lg:col-span-4"
          footer={<TcpUdpFooter tcp={tcpSessions} udp={udpSessions} />}
          legend={<ProtocolChartLegend />}
          metric={{ suffix: "Sessions", value: totalSessions }}
          title="流量趋势"
        >
          <TrafficLineChart data={trafficChartPoint(metrics, time)} />
        </ChartCard>
      </section>

      <SessionsPreviewTable
        items={tableData?.data ?? []}
        loading={isLoading && (tableData?.data?.length ?? 0) === 0}
        total={tableData?.total ?? 0}
      />

      <ServerInfoSection config={config} />
    </PageShell>
  );
}
