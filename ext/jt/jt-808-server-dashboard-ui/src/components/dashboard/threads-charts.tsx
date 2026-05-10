import { ParentSize } from "@visx/responsive";
import {
  AnimatedGrid,
  AnimatedAxis,
  AnimatedLineSeries,
  XYChart,
} from "@visx/xychart";
import { lightTheme, darkTheme, XYChartTheme, Tooltip } from "@visx/xychart";
import { FC, useEffect, useState } from "react";
import { useTheme } from "@heroui/react";
import { curveLinear } from "@visx/curve";

interface Threads {
  date: string;
  started: number;
  peak: number;
  live: number;
  daemon: number;
}

export type XYChartProps = {
  width: number;
  height: number;
  data: Threads[];
};

interface chartProps {
  data: Threads;
  maxLength?: number;
}
const getDate = (d: Threads) => d.date;

export const LineCharts = ({ width, height, data }: XYChartProps) => {
  const [theme, setTheme] = useState<XYChartTheme>(darkTheme);
  const { theme: webTheme } = useTheme();

  useEffect(() => {
    if (webTheme === "light") {
      setTheme(lightTheme);
    } else {
      setTheme(darkTheme);
    }
  }, [webTheme]);

  const config = {
    x: { type: "band", paddingInner: 0.3 } as const,
    y: { type: "linear" } as const,
  };

  return (
    <XYChart
      captureEvents={true}
      height={Math.min(400, height)}
      theme={theme}
      width={width}
      xScale={config.x}
      yScale={config.y}
    >
      <AnimatedGrid columns={false} numTicks={4} />
      <AnimatedAxis orientation="bottom" tickFormat={getDate} />
      <AnimatedAxis numTicks={4} orientation="left" />
      <AnimatedLineSeries
        curve={curveLinear}
        data={data}
        dataKey="started"
        xAccessor={getDate}
        yAccessor={(d) => d.started}
      />
      <AnimatedLineSeries
        curve={curveLinear}
        data={data}
        dataKey="peak"
        xAccessor={getDate}
        yAccessor={(d) => d.peak}
      />
      <AnimatedLineSeries
        curve={curveLinear}
        data={data}
        dataKey="live"
        xAccessor={getDate}
        yAccessor={(d) => d.live}
      />
      <AnimatedLineSeries
        curve={curveLinear}
        data={data}
        dataKey="daemon"
        xAccessor={getDate}
        yAccessor={(d) => d.daemon}
      />
      <Tooltip
        detectBounds
        showDatumGlyph
        showSeriesGlyphs
        snapTooltipToDatumX
        snapTooltipToDatumY
        renderTooltip={({ tooltipData }) => {
          const datum = tooltipData?.nearestDatum?.datum as Threads | undefined;
          const seriesKey = tooltipData?.nearestDatum?.key as
            | keyof Threads
            | undefined;

          return (
            <div>
              <div>{datum?.date}</div>
              {seriesKey && datum && (
                <div>
                  <strong>{String(seriesKey)}</strong>{" "}
                  {datum[seriesKey] as number}
                </div>
              )}
            </div>
          );
        }}
      />
    </XYChart>
  );
};

export const ThreadsCharts: FC<chartProps> = ({ data, maxLength = 20 }) => {
  const [threads, setThreads] = useState<Threads[]>([]);

  useEffect(() => {
    setThreads((pre) => {
      if (pre.length > maxLength) {
        pre.shift();
      }

      return pre.concat([data]);
    });
  }, [data, maxLength]);

  return (
    <ParentSize>
      {({ width, height }) => (
        <LineCharts data={threads} height={height} width={width} />
      )}
    </ParentSize>
  );
};
