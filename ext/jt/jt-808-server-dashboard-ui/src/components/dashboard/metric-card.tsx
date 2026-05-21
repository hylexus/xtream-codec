import clsx from "clsx";
import { ReactNode } from "react";

import { CountNumber } from "@/components/dashboard/count-number.tsx";
import {
  DashboardCard,
  DashboardCardBody,
} from "@/components/ui/dashboard-card.tsx";
import {
  dashboardLabel,
  dashboardValue,
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
  footer?: ReactNode;
  className?: string;
};

/** Pro KPI：标签 muted、数值 foreground 2xl/3xl、趋势 Chip */
export function MetricCard({
  label,
  value,
  trend,
  footer,
  className,
}: MetricCardProps) {
  const numericValue = typeof value === "number";

  return (
    <DashboardCard className={clsx(className)}>
      <DashboardCardBody className="flex flex-col gap-3 p-6">
        <p className={dashboardLabel}>{label}</p>
        <div className="flex flex-wrap items-end justify-between gap-2">
          <p className={dashboardValue}>
            {numericValue ? <CountNumber end={value} /> : value}
          </p>
          {trend ? (
            <TrendChip direction={trend.direction} value={trend.value} />
          ) : null}
        </div>
        {footer}
      </DashboardCardBody>
    </DashboardCard>
  );
}
