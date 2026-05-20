import { ArrowDown, ArrowUp } from "lucide-react";
import clsx from "clsx";

import { CountNumber } from "@/components/dashboard/count-number.tsx";
import {
  DashboardCard,
  DashboardCardBody,
} from "@/components/ui/dashboard-card.tsx";

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

export function MetricCard({
  label,
  value,
  trend,
  className,
}: MetricCardProps) {
  const numericValue = typeof value === "number";

  return (
    <DashboardCard className={clsx("min-h-[7.5rem]", className)}>
      <DashboardCardBody>
        <p className="text-sm font-medium text-muted">{label}</p>
        <div className="mt-3 flex flex-wrap items-end justify-between gap-2">
          <p className="dashboard-metric-value text-foreground">
            {numericValue ? <CountNumber end={value} /> : value}
          </p>
          {trend ? (
            <span
              className={
                trend.direction === "up"
                  ? "dashboard-trend-up"
                  : "dashboard-trend-down"
              }
            >
              {trend.direction === "up" ? (
                <ArrowUp className="size-3" strokeWidth={2.5} />
              ) : (
                <ArrowDown className="size-3" strokeWidth={2.5} />
              )}
              {trend.value}
            </span>
          ) : null}
        </div>
      </DashboardCardBody>
    </DashboardCard>
  );
}
