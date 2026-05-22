import { ParentSize } from "@visx/responsive";
import {
  AnimatedAxis,
  AnimatedGrid,
  AnimatedLineSeries,
  Tooltip,
  XYChart,
} from "@visx/xychart";
import { curveLinear } from "@visx/curve";
import { useTheme } from "@heroui/react";
import { FC, useEffect, useMemo, useState } from "react";

import {
  dashboardXyThemeForMode,
  resolveDashboardThemeMode,
} from "@/components/dashboard/chart-theme.ts";

type ThreadPoint = {
  date: string;
  started: number;
  peak: number;
  live: number;
  daemon: number;
};

const SERIES = ["started", "peak", "live", "daemon"] as const;

const CHART_MARGIN = { top: 8, right: 8, bottom: 24, left: 36 };

type ThreadsChartsProps = {
  data: ThreadPoint;
  maxLength?: number;
};

export const ThreadsCharts: FC<ThreadsChartsProps> = ({
  data,
  maxLength = 20,
}) => {
  const { theme: webTheme } = useTheme();
  const resolvedMode = resolveDashboardThemeMode(
    typeof webTheme === "string" ? webTheme : undefined,
  );
  const [series, setSeries] = useState<ThreadPoint[]>([]);

  const xyTheme = useMemo(
    () => dashboardXyThemeForMode(typeof webTheme === "string" ? webTheme : undefined),
    [webTheme],
  );

  const tickFill = resolvedMode === "light" ? "#52525b" : "#a1a1aa";

  useEffect(() => {
    setSeries((prev) => {
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
            xScale={{ type: "band", paddingInner: 0.35 }}
            yScale={{ type: "linear" }}
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
              numTicks={3}
              orientation="left"
              tickLabelProps={() => ({
                fill: tickFill,
                fontSize: 10,
                textAnchor: "end",
              })}
            />
            {SERIES.map((key) => (
              <AnimatedLineSeries
                key={key}
                curve={curveLinear}
                data={series}
                dataKey={key}
                strokeWidth={2.5}
                xAccessor={(d) => d.date}
                yAccessor={(d) => d[key]}
              />
            ))}
            <Tooltip
              detectBounds
              showDatumGlyph
              showSeriesGlyphs
              snapTooltipToDatumX
              snapTooltipToDatumY
              renderTooltip={({ tooltipData }) => {
                const datum = tooltipData?.nearestDatum?.datum as
                  | ThreadPoint
                  | undefined;
                const key = tooltipData?.nearestDatum?.key as
                  | (typeof SERIES)[number]
                  | undefined;

                if (!datum || !key) {
                  return null;
                }

                return (
                  <div className="text-xs">
                    <div>{datum.date}</div>
                    <div>
                      <strong>{key}</strong> {datum[key]}
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
