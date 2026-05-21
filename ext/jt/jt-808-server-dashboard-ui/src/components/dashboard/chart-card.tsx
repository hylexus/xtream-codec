import { ReactNode } from "react";
import clsx from "clsx";

import {
  ChartMiniStat,
  ChartMiniStatProps,
} from "@/components/dashboard/chart-mini-stat.tsx";
import { CountNumber } from "@/components/dashboard/count-number.tsx";
import {
  DashboardCard,
  DashboardCardBody,
  DashboardCardHeader,
} from "@/components/ui/dashboard-card.tsx";

type ChartCardMetric = {
  value: number | string;
  suffix?: string;
};

type ChartCardProps = {
  title: string;
  subtitle?: string;
  legend?: ReactNode;
  actions?: ReactNode;
  stats?: ChartMiniStatProps[];
  metric?: ChartCardMetric;
  children: ReactNode;
  className?: string;
  bodyClassName?: string;
};

export function ChartCard({
  title,
  subtitle,
  legend,
  actions,
  stats,
  metric,
  children,
  className,
  bodyClassName,
}: ChartCardProps) {
  return (
    <DashboardCard className={className}>
      <DashboardCardHeader
        actions={actions}
        legend={legend}
        subtitle={subtitle}
        title={title}
      />
      {stats && stats.length > 0 ? (
        <div className="grid grid-cols-2 gap-4 border-b border-separator px-6 py-4 sm:grid-cols-4 sm:gap-6">
          {stats.map((stat) => (
            <ChartMiniStat key={stat.label} {...stat} />
          ))}
        </div>
      ) : null}
      <DashboardCardBody
        className={clsx(
          metric && "relative",
          bodyClassName ?? "h-52 p-6 sm:h-56",
        )}
      >
        {metric ? (
          <div className="pointer-events-none absolute left-6 top-6 z-10">
            <p className="text-2xl font-semibold tabular-nums tracking-tight text-foreground sm:text-3xl">
              {typeof metric.value === "number" ? (
                <CountNumber end={metric.value} />
              ) : (
                metric.value
              )}
            </p>
            {metric.suffix ? (
              <p className="text-sm text-muted">{metric.suffix}</p>
            ) : null}
          </div>
        ) : null}
        {children}
      </DashboardCardBody>
    </DashboardCard>
  );
}
