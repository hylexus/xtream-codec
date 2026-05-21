import { Accordion, Button, Popover } from "@heroui/react";
import clsx from "clsx";
import { NavLink, useRouteLoaderData } from "react-router-dom";

import { ChartCard } from "@/components/dashboard/chart-card.tsx";
import { CountTime } from "@/components/dashboard/count-time.tsx";
import { DashboardToolbar } from "@/components/dashboard/dashboard-toolbar.tsx";
import { MetricCard } from "@/components/dashboard/metric-card.tsx";
import { MsgMiniTable } from "@/components/dashboard/msg-mini-table.tsx";
import { SessionCountBarChart } from "@/components/dashboard/session-count-bar-chart.tsx";
import { SessionsPreviewTable } from "@/components/dashboard/sessions-preview-table.tsx";
import {
  SessionStatBadges,
  StatMetricCard,
} from "@/components/dashboard/stat-metric-card.tsx";
import { ThreadsCharts } from "@/components/dashboard/threads-charts.tsx";
import { useDashboardMetrics } from "@/hooks/use-dashboard-metrics.ts";
import { usePageList } from "@/hooks/use-page-list.ts";
import { ServerInfo, Session } from "@/types";
import {
  REQUEST_METRIC_KEYS,
  SESSION_METRIC_KEYS,
  sliceForKey,
  subscriberTotal,
  sumRequestTotal,
  sumSessionCurrent,
} from "@/utils/dashboard-metrics.ts";
import { PageShell } from "@/components/ui/page-shell.tsx";
import {
  DashboardCard,
  DashboardCardBody,
  DashboardCardHeader,
} from "@/components/ui/dashboard-card.tsx";
import {
  dashboardLabel,
  dashboardMeta,
  dashboardValueSm,
} from "@/components/ui/dashboard-typography.tsx";

const PREVIEW_SIZE = 6;

export function DashboardOverview() {
  const { config } = useRouteLoaderData("root") as { config: ServerInfo };
  const { time, value: metrics } = useDashboardMetrics();
  const { tableData, isLoading } = usePageList<Session>(
    "session/instruction-sessions",
    PREVIEW_SIZE,
  );

  const previewItems = tableData?.data ?? [];
  const previewTotal = tableData?.total ?? 0;
  const tcpSessions = sumSessionCurrent(metrics, "TCP");
  const udpSessions = sumSessionCurrent(metrics, "UDP");
  const totalSessions = tcpSessions + udpSessions;
  const totalRequests = sumRequestTotal(metrics);
  const subscribers = subscriberTotal(metrics);

  const sessionChartStats = SESSION_METRIC_KEYS.map((item) => {
    const slice = sliceForKey(metrics, item.key);

    return {
      label: `${item.serverRole} · ${item.protocolType}`,
      value: slice.current ?? 0,
    };
  });

  return (
    <PageShell toolbar={<DashboardToolbar />}>
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-4">
        <MetricCard
          label="订阅者"
          value={subscribers}
          footer={
            <NavLink
              className="text-sm font-medium text-accent underline-offset-4 hover:underline"
              to="/subscriber"
            >
              查看详情
            </NavLink>
          }
        />
        <MetricCard label="TCP 会话" value={tcpSessions} />
        <MetricCard label="UDP 会话" value={udpSessions} />
        <MetricCard label="请求总量" value={totalRequests} />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <ChartCard
          actions={
            <Button size="sm" variant="secondary">
              实时
            </Button>
          }
          bodyClassName="min-h-52 p-6 sm:min-h-56"
          className="lg:col-span-2"
          stats={sessionChartStats}
          subtitle="四路会话当前值"
          title="会话表现"
        >
          <div className="grid h-full gap-6 lg:grid-cols-2">
            <div className="h-40 sm:h-44">
              <SessionCountBarChart
                values={SESSION_METRIC_KEYS.map(
                  (item) => sliceForKey(metrics, item.key).current ?? 0,
                )}
              />
            </div>
            <div className="min-h-40">
              <ThreadsCharts
                data={{
                  date: time ? time.slice(11, 19) : "",
                  ...(typeof metrics.threads === "object" &&
                  metrics.threads !== null
                    ? metrics.threads
                    : {}),
                }}
              />
            </div>
          </div>
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
          metric={{ suffix: "Sessions", value: totalSessions }}
          title="协议分布"
        >
          <div className="flex h-full flex-col justify-end gap-3 pb-2">
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted">TCP</span>
              <span className="font-semibold tabular-nums text-foreground">
                {tcpSessions}
              </span>
            </div>
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted">UDP</span>
              <span className="font-semibold tabular-nums text-foreground">
                {udpSessions}
              </span>
            </div>
          </div>
        </ChartCard>
      </div>

      <SessionsPreviewTable
        items={previewItems}
        loading={isLoading && previewItems.length === 0}
        total={previewTotal}
      />

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <DashboardCard className="lg:col-span-2">
          <DashboardCardHeader title="服务器信息" />
          <DashboardCardBody className="p-6">
            <div className="grid gap-4 sm:grid-cols-3">
              <div>
                <p className={dashboardLabel}>Codec 版本</p>
                <p className={clsx(dashboardValueSm, "mt-1")}>
                  {config.dependencies?.xtreamCodec?.version ?? "—"}
                </p>
              </div>
              <div>
                <p className={dashboardLabel}>Java</p>
                <p className={clsx(dashboardValueSm, "mt-1")}>
                  {config.java.version}
                </p>
              </div>
              <div>
                <p className={dashboardLabel}>OS</p>
                <p className={clsx("mt-1 text-sm text-foreground")}>
                  {config.os.name}
                </p>
                <p className={dashboardMeta}>{config.os.arch}</p>
              </div>
            </div>
            <Accordion className="mt-4">
              <Accordion.Item id="java-detail">
                <Accordion.Heading>
                  <Accordion.Trigger className="text-sm font-medium text-foreground">
                    Java 详情
                    <Accordion.Indicator />
                  </Accordion.Trigger>
                </Accordion.Heading>
                <Accordion.Panel>
                  <pre className="max-h-40 overflow-auto text-xs text-muted">
                    {JSON.stringify(config.java, null, 2)}
                  </pre>
                </Accordion.Panel>
              </Accordion.Item>
            </Accordion>
          </DashboardCardBody>
        </DashboardCard>

        <DashboardCard>
          <DashboardCardHeader title="运行状态" />
          <DashboardCardBody className="flex flex-col gap-4 p-6">
            <div>
              <p className={dashboardLabel}>启动时间</p>
              <p className={clsx(dashboardValueSm, "mt-1")}>
                {config.serverStartupTime}
              </p>
            </div>
            <div>
              <p className={dashboardLabel}>运行时间</p>
              <p className={clsx(dashboardValueSm, "mt-1")}>
                <CountTime start={new Date(config.serverStartupTime)} />
              </p>
            </div>
          </DashboardCardBody>
        </DashboardCard>
      </div>

      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-4">
        {SESSION_METRIC_KEYS.map((item) => {
          const slice = sliceForKey(metrics, item.key);

          return (
            <StatMetricCard
              key={item.key}
              badges={
                <SessionStatBadges
                  protocolType={item.protocolType}
                  serverRole={item.serverRole}
                />
              }
              current={slice.current ?? 0}
              max={slice.max ?? "—"}
              title="会话数"
            />
          );
        })}
      </div>

      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-4">
        {REQUEST_METRIC_KEYS.map((item) => {
          const slice = sliceForKey(metrics, item.key);
          const total = slice.total ?? 0;

          return (
            <StatMetricCard
              key={item.key}
              badges={
                <SessionStatBadges
                  protocolType={item.protocolType}
                  serverRole={item.serverRole}
                />
              }
              title="请求数"
              total={total}
              actions={
                total > 0 ? (
                  <Popover>
                    <Popover.Trigger>
                      <Button size="sm" variant="secondary">
                        详情
                      </Button>
                    </Popover.Trigger>
                    <Popover.Content className="p-2">
                      <MsgMiniTable data={slice.details} />
                    </Popover.Content>
                  </Popover>
                ) : null
              }
            />
          );
        })}
      </div>
    </PageShell>
  );
}
