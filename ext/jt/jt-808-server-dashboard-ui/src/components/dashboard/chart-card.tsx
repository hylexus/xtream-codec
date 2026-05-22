import { ReactNode } from "react";
import clsx from "clsx";

import {
  ChartMiniStat,
  ChartMiniStatProps,
  CountNumber,
} from "@/components/dashboard/dashboard-widgets.tsx";
import {
  DashboardCard,
  DashboardCardBody,
} from "@/components/ui/dashboard-card.tsx";
import {
  dashboardChartMetric,
  dashboardMetaSm,
} from "@/components/ui/dashboard-typography.tsx";

/** Pro 图表绘图区高度区间 */
export const CHART_PLOT_CLASS = "min-h-[8.5rem] max-h-[12rem] sm:min-h-[9rem]";

type ChartCardProps = {
  title: string;
  subtitle?: ReactNode;
  legend?: ReactNode;
  actions?: ReactNode;
  stats?: ChartMiniStatProps[];
  metric?: { value: number | string; suffix?: string };
  children: ReactNode;
  className?: string;
  plotClassName?: string;
  footer?: ReactNode;
  compact?: boolean;
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
  plotClassName,
  footer,
  compact = false,
}: ChartCardProps) {
  return (
    <DashboardCard className={clsx("flex h-full flex-col", className)}>
      <div
        className={clsx(
          "flex shrink-0 flex-wrap items-center justify-between gap-2 border-b border-separator px-4",
          compact ? "py-2" : "py-2.5",
        )}
      >
        <div className="min-w-0">
          <p className="text-sm font-semibold text-foreground">{title}</p>
          {subtitle ? (
            <div className="mt-0.5 text-xs text-muted">{subtitle}</div>
          ) : null}
        </div>
        {(legend || actions) && (
          <div className="flex shrink-0 flex-wrap items-center gap-2">
            {legend}
            {actions}
          </div>
        )}
      </div>

      {stats && stats.length > 0 ? (
        <div className="grid shrink-0 grid-cols-2 gap-x-3 gap-y-1.5 border-b border-separator px-4 py-2 sm:grid-cols-4">
          {stats.map((stat) => (
            <ChartMiniStat key={stat.label} {...stat} />
          ))}
        </div>
      ) : null}

      <DashboardCardBody className="flex min-h-0 flex-1 flex-col p-0">
        {metric ? (
          <div className="shrink-0 px-5 pb-2 pt-3">
            <p className={dashboardChartMetric}>
              {typeof metric.value === "number" ? (
                <CountNumber end={metric.value} />
              ) : (
                metric.value
              )}
            </p>
            {metric.suffix ? (
              <p className={clsx(dashboardMetaSm, "mt-1")}>{metric.suffix}</p>
            ) : null}
          </div>
        ) : null}

        <div
          className={clsx(
            "flex min-h-0 flex-1 flex-col px-3",
            metric ? "pb-3 pt-0" : "py-3",
            plotClassName,
          )}
        >
          <div
            className={clsx(
              "relative min-h-0 flex-1 overflow-hidden rounded-lg bg-background-tertiary/35",
              CHART_PLOT_CLASS,
            )}
          >
            <div className="absolute inset-0">{children}</div>
          </div>
        </div>

        {footer ? (
          <div className="shrink-0 border-t border-separator px-4 py-2 text-xs text-muted">
            {footer}
          </div>
        ) : null}
      </DashboardCardBody>
    </DashboardCard>
  );
}
