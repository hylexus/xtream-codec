import { scaleBand } from "@visx/scale";
import type { ScaleBand } from "@visx/vendor/d3-scale";

import {
  HISTORICAL_BAR_WIDTH,
  LIVE_SLOT_COUNT,
  TIMELINE_WINDOW_MS,
  TimelineDisplayMode,
} from "./constants.ts";
import { Group } from "./types.ts";
import { getOrderedDumpTimes, parseDumpTime } from "./utils.ts";

export type BarGeometry = { x: number; width: number };

export type TimelineScaleConfig = {
  mode: TimelineDisplayMode;
  plotWidth: number;
  scrollable: boolean;
  windowStartMs: number;
  windowEndMs: number;
  getBarGeometry: (time: string) => BarGeometry | null;
  axisScale: ScaleBand<string>;
  axisTickFormat: (slotId: string) => string;
  /** Vertical grid line x positions. */
  gridXs: number[];
};

const formatMsTick = (ms: number) => {
  const d = new Date(ms);

  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}:${String(d.getSeconds()).padStart(2, "0")}`;
};

const formatBandTick = (time: string) => {
  const ms = parseDumpTime(time);

  if (Number.isNaN(ms)) {
    return time;
  }

  return formatMsTick(ms);
};

const liveAxisSlotIds = () =>
  Array.from({ length: LIVE_SLOT_COUNT }, (_, i) => String(i));

/** Times in window, chronological unique order (one column per dump round). */
export const getOrderedTimesInWindow = (
  windowStartMs: number,
  groups: Group[],
): string[] => {
  const windowEnd = windowStartMs + TIMELINE_WINDOW_MS;

  return getOrderedDumpTimes(groups).filter((time) => {
    const ms = parseDumpTime(time);

    return !Number.isNaN(ms) && ms >= windowStartMs && ms <= windowEnd;
  });
};

export const resolveTimelineDisplayMode = (
  windowStartMs: number,
  orderedTimes: string[],
): TimelineDisplayMode => {
  if (orderedTimes.length === 0) {
    return "live";
  }

  const timesMs = orderedTimes.map(parseDumpTime).filter((t) => !Number.isNaN(t));

  if (timesMs.length === 0) {
    return "live";
  }

  const max = Math.max(...timesMs);
  const min = Math.min(...timesMs);
  const windowEnd = windowStartMs + TIMELINE_WINDOW_MS;

  if (
    orderedTimes.length > LIVE_SLOT_COUNT ||
    max > windowEnd ||
    max - min > TIMELINE_WINDOW_MS
  ) {
    return "historical";
  }

  return "live";
};

const buildContinuousBandScale = (
  domain: string[],
  plotWidth: number,
) => {
  const scale = scaleBand<string>({
    domain,
    range: [0, plotWidth],
    padding: 0,
  });

  return {
    scale,
    slotWidth: domain.length > 0 ? scale.bandwidth() : plotWidth,
    gridXs: domain.map((d) => (scale(d) ?? 0) + scale.bandwidth()),
  };
};

export const buildTimelineScale = (
  windowStartMs: number,
  groups: Group[],
  plotContainerWidth: number,
): TimelineScaleConfig => {
  const allOrderedTimes = getOrderedDumpTimes(groups);
  const mode = resolveTimelineDisplayMode(windowStartMs, allOrderedTimes);
  const windowEndMs = windowStartMs + TIMELINE_WINDOW_MS;

  if (mode === "live") {
    const plotWidth = plotContainerWidth;
    const dataTimes = getOrderedTimesInWindow(windowStartMs, groups);
    const timeToIndex = new Map(dataTimes.map((t, i) => [t, i]));
    const slotWidth = plotWidth / LIVE_SLOT_COUNT;
    const axisDomain = liveAxisSlotIds();
    const axisScale = scaleBand<string>({
      domain: axisDomain,
      range: [0, plotWidth],
      padding: 0,
    });
    const gridXs = Array.from(
      { length: LIVE_SLOT_COUNT },
      (_, i) => (i + 1) * slotWidth,
    ).slice(0, -1);

    return {
      mode: "live",
      plotWidth,
      scrollable: false,
      windowStartMs,
      windowEndMs,
      axisScale,
      gridXs,
      axisTickFormat: (slotId) =>
        formatMsTick(windowStartMs + Number(slotId) * 1000),
      getBarGeometry: (time: string) => {
        const index = timeToIndex.get(time);

        if (index === undefined || index >= LIVE_SLOT_COUNT) {
          return null;
        }

        return {
          x: index * slotWidth,
          width: slotWidth,
        };
      },
    };
  }

  const plotWidth = Math.max(
    plotContainerWidth,
    allOrderedTimes.length * HISTORICAL_BAR_WIDTH,
  );
  const scrollable = plotWidth > plotContainerWidth;
  const { scale, gridXs } = buildContinuousBandScale(
    allOrderedTimes,
    plotWidth,
  );

  return {
    mode: "historical",
    plotWidth,
    scrollable,
    windowStartMs,
    windowEndMs,
    axisScale: scale,
    gridXs,
    axisTickFormat: formatBandTick,
    getBarGeometry: (time: string) => {
      const x = scale(time);

      if (x == null) {
        return null;
      }

      return { x, width: scale.bandwidth() };
    },
  };
};
