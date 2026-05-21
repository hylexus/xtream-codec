import clsx from "clsx";

import { CountNumber } from "@/components/dashboard/count-number.tsx";
import { DashboardCard, DashboardCardBody } from "@/components/ui/dashboard-card.tsx";
import { TrendChip } from "@/components/ui/trend-chip.tsx";

type Trend = {
  value: string;
  direction: "up" | "down";
};

type MetricCardProps = {
  label: string;
  value: number | string;
  trend?: Trend;
  className?: string;
};

/** Pro KPI：Revenue / Expenses / Sales / Profit */
export function MetricCard({
  label,
  value,
  trend,
  className,
}: MetricCardProps) {
  const numericValue = typeof value === "number";

  return (
    <DashboardCard className={clsx(className)}>
      <DashboardCardBody className="flex flex-col gap-3 p-6">
        <p className="text-sm font-medium text-muted">{label}</p>
        <div className="flex flex-wrap items-end justify-between gap-2">
          <p className="text-2xl font-semibold tabular-nums tracking-tight text-foreground sm:text-3xl">
            {numericValue ? <CountNumber end={value} /> : value}
          </p>
          {trend ? (
            <TrendChip direction={trend.direction} value={trend.value} />
          ) : null}
        </div>
      </DashboardCardBody>
    </DashboardCard>
  );
}
