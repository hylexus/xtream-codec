import { ParentSize } from "@visx/responsive";
import {
  AnimatedAreaSeries,
  AnimatedAxis,
  AnimatedGrid,
  Tooltip,
  XYChart,
} from "@visx/xychart";
import { curveMonotoneX } from "@visx/curve";
import { useTheme } from "@heroui/react";
import { FC, useEffect, useMemo, useState } from "react";

import {
  dashboardXyThemeForMode,
  resolveDashboardThemeMode,
} from "@/components/dashboard/chart-theme.ts";
import type { TrafficChartPoint } from "@/utils/dashboard-metrics.ts";

const CHART_MARGIN = { top: 8, right: 8, bottom: 22, left: 8 };

type TrafficLineChartProps = {
  /** 每次 SSE `metrics/basic` 推送的一帧 */
  data: TrafficChartPoint;
  maxLength?: number;
};

/** TCP / UDP 活跃会话随时间变化（数据来自 metrics/basic SSE） */
export const TrafficLineChart: FC<TrafficLineChartProps> = ({
  data,
  maxLength = 24,
}) => {
  const { theme: webTheme } = useTheme();
  const [series, setSeries] = useState<TrafficChartPoint[]>([]);

  const xyTheme = useMemo(() => {
    const base = dashboardXyThemeForMode(
      typeof webTheme === "string" ? webTheme : undefined,
    );

    return {
      ...base,
      colors: base.colors.slice(0, 2),
    };
  }, [webTheme]);

  const tickFill =
    resolveDashboardThemeMode(
      typeof webTheme === "string" ? webTheme : undefined,
    ) === "light"
      ? "#52525b"
      : "#a1a1aa";

  useEffect(() => {
    if (!data.date) {
      return;
    }

    setSeries((prev) => {
      const last = prev[prev.length - 1];

      if (last?.date === data.date) {
        return [...prev.slice(0, -1), data];
      }

      const next = prev.length >= maxLength ? prev.slice(1) : prev;

      return [...next, data];
    });
  }, [data, maxLength]);

  return (
    <div className="h-full min-h-0 w-full">
      <ParentSize debounceTime={10}>
        {({ width, height }) => (
          <XYChart
            captureEvents
            height={Math.max(height, 1)}
            margin={CHART_MARGIN}
            theme={xyTheme}
            width={width}
            xScale={{ type: "band", paddingInner: 0.4 }}
            yScale={{ type: "linear", nice: true, zero: true }}
          >
            <AnimatedGrid columns={false} numTicks={3} strokeDasharray="4 4" />
            <AnimatedAxis
              numTicks={4}
              orientation="bottom"
              tickFormat={(d) => String(d)}
              tickLabelProps={() => ({
                fill: tickFill,
                fontSize: 10,
                textAnchor: "middle",
              })}
            />
            <AnimatedAxis
              hideAxisLine
              hideTicks
              hideZero
              numTicks={3}
              orientation="left"
              tickLabelProps={() => ({
                fill: tickFill,
                fontSize: 10,
                textAnchor: "end",
              })}
            />
            <AnimatedAreaSeries
              curve={curveMonotoneX}
              data={series}
              dataKey="udp"
              fillOpacity={0.22}
              lineProps={{ strokeWidth: 2 }}
              xAccessor={(d) => d.date}
              yAccessor={(d) => d.udp}
            />
            <AnimatedAreaSeries
              curve={curveMonotoneX}
              data={series}
              dataKey="tcp"
              fillOpacity={0.28}
              lineProps={{ strokeWidth: 2 }}
              xAccessor={(d) => d.date}
              yAccessor={(d) => d.tcp}
            />
            <Tooltip
              detectBounds
              showDatumGlyph
              showSeriesGlyphs
              snapTooltipToDatumX
              renderTooltip={({ tooltipData }) => {
                const datum = tooltipData?.nearestDatum?.datum as
                  | TrafficChartPoint
                  | undefined;
                const key = tooltipData?.nearestDatum?.key as
                  | "tcp"
                  | "udp"
                  | undefined;

                if (!datum || !key) {
                  return null;
                }

                return (
                  <div className="text-xs">
                    <div>{datum.date}</div>
                    <div>
                      <strong>{key.toUpperCase()}</strong> {datum[key]}
                    </div>
                  </div>
                );
              }}
            />
          </XYChart>
        )}
      </ParentSize>
    </div>
  );
};
