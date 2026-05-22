import { Popover } from "@heroui/react";
import clsx from "clsx";

import {
  CountNumber,
  MsgMiniTable,
} from "@/components/dashboard/dashboard-widgets.tsx";
import {
  DashboardCard,
  DashboardCardBody,
  DashboardCardHeader,
} from "@/components/ui/dashboard-card.tsx";
import {
  dashboardLabel,
  dashboardMetaSm,
  dashboardMetricMd,
} from "@/components/ui/dashboard-typography.tsx";
import { Metrics } from "@/types";
import {
  CHANNEL_METRIC_ROWS,
  channelShortLabel,
  sliceForKey,
} from "@/utils/dashboard-metrics.ts";

type ChannelMetricCellProps = {
  label: string;
  protocolType: string;
  current: number;
  max?: number;
  requestTotal: number;
  requestDetails?: unknown;
};

function ChannelMetricCell({
  label,
  protocolType,
  current,
  max,
  requestTotal,
  requestDetails,
}: ChannelMetricCellProps) {
  const isTcp = protocolType === "TCP";

  return (
    <div className="rounded-xl border border-separator/70 bg-background-tertiary/35 px-4 py-3.5">
      <div className="flex items-center gap-2">
        <span
          aria-hidden
          className={clsx(
            "size-2 shrink-0 rounded-full",
            isTcp ? "bg-accent" : "bg-muted-foreground/60",
          )}
        />
        <span className={clsx(dashboardLabel, "truncate text-foreground")}>
          {label}
        </span>
      </div>
      <p className={clsx(dashboardMetricMd, "mt-2 leading-none")}>
        <CountNumber end={current} />
        <span className={clsx(dashboardMetaSm, "ml-1.5 font-normal")}>
          会话
        </span>
      </p>
      <div
        className={clsx(
          dashboardMetaSm,
          "mt-2 flex flex-wrap items-center gap-x-2 gap-y-0.5 tabular-nums",
        )}
      >
        <span>峰 {max ?? "—"}</span>
        <span aria-hidden className="text-separator">
          ·
        </span>
        {requestTotal > 0 ? (
          <Popover>
            <Popover.Trigger>
              <button
                className="inline-flex items-center gap-0.5 font-semibold text-foreground underline-offset-2 hover:text-accent hover:underline"
                type="button"
              >
                请求 <CountNumber end={requestTotal} />
              </button>
            </Popover.Trigger>
            <Popover.Content className="p-2">
              <MsgMiniTable data={requestDetails} />
            </Popover.Content>
          </Popover>
        ) : (
          <span>请求 0</span>
        )}
      </div>
    </div>
  );
}

/** 四通道数量概览：单行网格，适合放在 KPI 下方 */
export function ChannelMetricsCard({ metrics }: { metrics: Metrics }) {
  return (
    <DashboardCard className="h-auto">
      <DashboardCardHeader
        actions={
          <span className={clsx(dashboardMetaSm, "hidden sm:inline")}>
            点击请求数可查看消息明细
          </span>
        }
        className="py-2.5"
        subtitle="各通道会话与请求累计"
        title="通道指标"
      />
      <DashboardCardBody className="px-5 pb-4 pt-0">
        <div className="grid grid-cols-2 gap-3 sm:gap-4 lg:grid-cols-4">
          {CHANNEL_METRIC_ROWS.map(({ session, request }) => {
            const sessionSlice = sliceForKey(metrics, session.key);
            const requestSlice = sliceForKey(metrics, request.key);

            return (
              <ChannelMetricCell
                key={session.key}
                current={sessionSlice.current ?? 0}
                label={channelShortLabel(session)}
                max={sessionSlice.max}
                protocolType={session.protocolType}
                requestDetails={requestSlice.details}
                requestTotal={requestSlice.total ?? 0}
              />
            );
          })}
        </div>
      </DashboardCardBody>
    </DashboardCard>
  );
}
