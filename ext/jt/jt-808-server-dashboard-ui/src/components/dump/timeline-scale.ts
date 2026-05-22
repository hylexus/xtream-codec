import type { ScaleBand } from "@visx/vendor/d3-scale";

import { scaleBand } from "@visx/scale";

import {
  HISTORICAL_BAR_WIDTH,
  LIVE_SLOT_COUNT,
  MAX_SLOT_TIMELINE_COUNT,
  SLOT_MS,
  TimelineDisplayMode,
} from "./constants.ts";
import { Group } from "./types.ts";
import { getOrderedDumpTimes, parseDumpTime } from "./utils.ts";

export type BarGeometry = { x: number; width: number };

export type TimelineScaleConfig = {
  mode: TimelineDisplayMode;
  slotCount: number;
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

const axisSlotIds = (slotCount: number) =>
  Array.from({ length: slotCount }, (_, i) => String(i));

/** Times in window, chronological unique order (one column per dump round). */
export const getOrderedTimesInWindow = (
  windowStartMs: number,
  groups: Group[],
  windowMs: number,
): string[] => {
  const windowEnd = windowStartMs + windowMs;

  return getOrderedDumpTimes(groups).filter((time) => {
    const ms = parseDumpTime(time);

    return !Number.isNaN(ms) && ms >= windowStartMs && ms <= windowEnd;
  });
};

const slotTimelineNeedsExpansion = (
  windowStartMs: number,
  orderedTimes: string[],
  slotCount: number,
): boolean => {
  if (orderedTimes.length === 0) {
    return false;
  }

  const timesMs = orderedTimes
    .map(parseDumpTime)
    .filter((t) => !Number.isNaN(t));

  if (timesMs.length === 0) {
    return false;
  }

  const max = Math.max(...timesMs);
  const min = Math.min(...timesMs);
  const windowEnd = windowStartMs + slotCount * SLOT_MS;

  return (
    orderedTimes.length > slotCount ||
    max > windowEnd ||
    max - min > slotCount * SLOT_MS
  );
};

/** Double slot-axis length each time data exceeds the current window (15→30→60…). */
export const resolveTimelineSlotCount = (
  windowStartMs: number,
  orderedTimes: string[],
): number => {
  let slots = LIVE_SLOT_COUNT;

  while (
    slotTimelineNeedsExpansion(windowStartMs, orderedTimes, slots) &&
    slots < MAX_SLOT_TIMELINE_COUNT
  ) {
    slots *= 2;
  }

  return slots;
};

export const shouldUseHistoricalScale = (
  windowStartMs: number,
  orderedTimes: string[],
): boolean => {
  const slots = resolveTimelineSlotCount(windowStartMs, orderedTimes);

  return (
    slots >= MAX_SLOT_TIMELINE_COUNT &&
    slotTimelineNeedsExpansion(windowStartMs, orderedTimes, slots)
  );
};

const buildContinuousBandScale = (domain: string[], plotWidth: number) => {
  const scale = scaleBand<string>({
    domain,
    range: [0, plotWidth],
    padding: 0,
  });

  return {
    scale,
    slotWidth: domain.length > 0 ? scale.bandwidth() : plotWidth,
    gridXs: [],
  };
};

const buildSlotTimelineScale = (
  windowStartMs: number,
  groups: Group[],
  plotContainerWidth: number,
  slotCount: number,
): TimelineScaleConfig => {
  const windowMs = slotCount * SLOT_MS;
  const windowEndMs = windowStartMs + windowMs;
  const plotWidth = plotContainerWidth * (slotCount / LIVE_SLOT_COUNT);
  const scrollable = slotCount > LIVE_SLOT_COUNT;
  const dataTimes = getOrderedTimesInWindow(windowStartMs, groups, windowMs);
  const timeToIndex = new Map(dataTimes.map((t, i) => [t, i]));
  const slotWidth = plotWidth / slotCount;
  const axisScale = scaleBand<string>({
    domain: axisSlotIds(slotCount),
    range: [0, plotWidth],
    padding: 0,
  });

  return {
    mode: "live",
    slotCount,
    plotWidth,
    scrollable,
    windowStartMs,
    windowEndMs,
    axisScale,
    gridXs: [],
    axisTickFormat: (slotId) =>
      formatMsTick(windowStartMs + Number(slotId) * SLOT_MS),
    getBarGeometry: (time: string) => {
      const index = timeToIndex.get(time);

      if (index === undefined || index >= slotCount) {
        return null;
      }

      return {
        x: index * slotWidth,
        width: slotWidth,
      };
    },
  };
};

export const buildTimelineScale = (
  windowStartMs: number,
  groups: Group[],
  plotContainerWidth: number,
): TimelineScaleConfig => {
  const allOrderedTimes = getOrderedDumpTimes(groups);

  if (!shouldUseHistoricalScale(windowStartMs, allOrderedTimes)) {
    const slotCount = resolveTimelineSlotCount(windowStartMs, allOrderedTimes);

    return buildSlotTimelineScale(
      windowStartMs,
      groups,
      plotContainerWidth,
      slotCount,
    );
  }

  const slotCount = MAX_SLOT_TIMELINE_COUNT;
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
    slotCount,
    plotWidth,
    scrollable,
    windowStartMs,
    windowEndMs: windowStartMs + slotCount * SLOT_MS,
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
