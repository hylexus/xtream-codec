import { CountNumber } from "@/components/dashboard/count-number.tsx";
import { TrendChip } from "@/components/ui/trend-chip.tsx";

export type ChartMiniStatTrend = {
  value: string;
  direction: "up" | "down";
};

export type ChartMiniStatProps = {
  label: string;
  value: number | string;
  trend?: ChartMiniStatTrend;
};

/** Pro Sales Performance 头内联指标 */
export function ChartMiniStat({ label, value, trend }: ChartMiniStatProps) {
  const numeric = typeof value === "number";

  return (
    <div className="min-w-0 flex-1">
      <div className="flex flex-wrap items-center gap-2">
        <p className="text-lg font-semibold tabular-nums tracking-tight text-foreground sm:text-xl">
          {numeric ? <CountNumber end={value} /> : value}
        </p>
        {trend ? (
          <TrendChip direction={trend.direction} value={trend.value} />
        ) : null}
      </div>
      <p className="mt-1 text-xs text-muted">{label}</p>
    </div>
  );
}
