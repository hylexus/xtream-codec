import clsx from "clsx";
import { ReactNode } from "react";

import { CountNumber } from "@/components/dashboard/dashboard-widgets.tsx";
import {
  DashboardCard,
  DashboardCardBody,
} from "@/components/ui/dashboard-card.tsx";
import {
  dashboardKpiValue,
  dashboardLabel,
} from "@/components/ui/dashboard-typography.tsx";
import { TrendChip } from "@/components/ui/trend-chip.tsx";

type Trend = {
  value: string;
  direction: "up" | "down";
};

type MetricCardProps = {
  label: string;
  value: number | string;
  trend?: Trend;
  /** 标题行右侧操作（如「详情」链接），不占底部空间 */
  action?: ReactNode;
  className?: string;
};

/** Pro 矮 KPI 卡：紧凑内边距，标签与数值两行 */
export function MetricCard({
  label,
  value,
  trend,
  action,
  className,
}: MetricCardProps) {
  const numericValue = typeof value === "number";

  return (
    <DashboardCard className={clsx("h-full", className)}>
      <DashboardCardBody className="px-4 py-2.5">
        <div className="flex items-center justify-between gap-2">
          <dt className={clsx(dashboardLabel, "min-w-0 leading-tight")}>
            {label}
          </dt>
          {action ? (
            <div className="shrink-0 leading-none">{action}</div>
          ) : null}
        </div>
        <dd className="m-0 mt-1 flex items-baseline justify-between gap-2">
          <span className={dashboardKpiValue}>
            {numericValue ? <CountNumber end={value} /> : value}
          </span>
          {trend ? (
            <TrendChip direction={trend.direction} value={trend.value} />
          ) : null}
        </dd>
      </DashboardCardBody>
    </DashboardCard>
  );
}
