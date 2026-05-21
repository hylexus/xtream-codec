import { AxisBottom } from "@visx/axis";
import { Group } from "@visx/group";
import { LegendOrdinal } from "@visx/legend";
import { ParentSize } from "@visx/responsive";
import { scaleBand, scaleOrdinal } from "@visx/scale";
import clsx from "clsx";
import { FC, KeyboardEvent, useMemo } from "react";

import {
  AXIS_BODY_GAP,
  DumpViewMode,
  GROUP_HEADER_HEIGHT,
  GROUP_SECTION_GAP,
  STACK_KEYS,
  THREAD_LABEL_WIDTH,
  THREAD_ROW_HEIGHT,
  THREAD_STATE_STYLES,
  TIME_AXIS_HEIGHT,
} from "./constants.ts";
import {
  DumpBarSegment,
  formatDumpTimeLabel,
  getOrderedDumpTimes,
  getTimelinePlotWidth,
  segmentsMatch,
} from "./utils.ts";
import {
  Group as DumpGroupModel,
  SelectedDumpContext,
  Thread,
} from "./types.ts";

const legendScale = scaleOrdinal({
  domain: STACK_KEYS,
  range: STACK_KEYS.map((k) => THREAD_STATE_STYLES[k].color),
});

type LayoutRow =
  | {
      type: "group";
      groupName: string;
      top: number;
      threadCount: number;
    }
  | {
      type: "thread";
      groupName: string;
      thread: Thread;
      top: number;
      showGroupHint: boolean;
    };

const buildLayout = (
  groups: DumpGroupModel[],
  viewMode: DumpViewMode,
): LayoutRow[] => {
  const rows: LayoutRow[] = [];
  let top = 0;

  if (viewMode === "grouped") {
    groups.forEach((group, groupIndex) => {
      if (groupIndex > 0) {
        top += GROUP_SECTION_GAP;
      }
      rows.push({
        type: "group",
        groupName: group.name,
        top,
        threadCount: group.threads.length,
      });
      top += GROUP_HEADER_HEIGHT;

      group.threads.forEach((thread) => {
        rows.push({
          type: "thread",
          groupName: group.name,
          thread,
          top,
          showGroupHint: false,
        });
        top += THREAD_ROW_HEIGHT;
      });
    });
  } else {
    const flatThreads = groups
      .flatMap((group) =>
        group.threads.map((thread) => ({ group, thread })),
      )
      .sort((a, b) => {
        const g = a.group.name.localeCompare(b.group.name);

        if (g !== 0) {
          return g;
        }

        return a.thread.threadName.localeCompare(b.thread.threadName);
      });

    flatThreads.forEach(({ group, thread }) => {
      rows.push({
        type: "thread",
        groupName: group.name,
        thread,
        top,
        showGroupHint: true,
      });
      top += THREAD_ROW_HEIGHT;
    });
  }

  return rows;
};

export interface DumpTimelineChartProps {
  groups: DumpGroupModel[];
  viewMode: DumpViewMode;
  selectedDump: SelectedDumpContext | null;
  hoveredSegment: DumpBarSegment | null;
  onBarClick: (segment: DumpBarSegment) => void;
  onBarHover: (segment: DumpBarSegment | null) => void;
}

interface TimelineBarsProps {
  layout: LayoutRow[];
  timeScale: ReturnType<typeof scaleBand<string>>;
  bodyTop: number;
  selectedDump: SelectedDumpContext | null;
  hoveredSegment: DumpBarSegment | null;
  onBarClick: (segment: DumpBarSegment) => void;
  onBarHover: (segment: DumpBarSegment | null) => void;
}

const TimelineBars: FC<TimelineBarsProps> = ({
  layout,
  timeScale,
  bodyTop,
  selectedDump,
  hoveredSegment,
  onBarClick,
  onBarHover,
}) => {
  const handleBarKeyDown = (
    e: KeyboardEvent<SVGRectElement>,
    segment: DumpBarSegment,
  ) => {
    if (e.key === "Enter" || e.key === " ") {
      e.preventDefault();
      onBarClick(segment);
    }
  };

  const barHeight = THREAD_ROW_HEIGHT - 10;

  return (
    <Group top={bodyTop}>
      {layout.map((row) => {
        if (row.type === "group") {
          return null;
        }

        const { thread, groupName, top } = row;
        const barY = top + 5;

        return (
          <g key={`${groupName}-${thread.threadName}`}>
            {thread.dumps.map((entry, index) => {
              const x = timeScale(entry.time);

              if (x == null) {
                return null;
              }

              const state = entry.dumpInfo.threadState;
              const style = THREAD_STATE_STYLES[state];
              const segment: DumpBarSegment = {
                groupName,
                threadName: thread.threadName,
                time: entry.time,
                threadState: state,
              };
              const highlighted = segmentsMatch(hoveredSegment, segment);
              const selected =
                selectedDump?.groupName === groupName &&
                selectedDump.focusedThreadName === thread.threadName &&
                selectedDump.time === entry.time &&
                selectedDump.threadState === state;
              const dimmed =
                selectedDump != null &&
                selectedDump.groupName === groupName &&
                !selected;

              return (
                <rect
                  key={`${entry.time}-${index}`}
                  aria-label={`${thread.threadName} ${state} at ${entry.time}`}
                  className="outline-none transition-[stroke,opacity] duration-150"
                  fill={style.color}
                  height={barHeight}
                  opacity={dimmed ? 0.4 : 1}
                  role="button"
                  rx={2}
                  stroke={
                    highlighted || selected ? "white" : "transparent"
                  }
                  strokeWidth={highlighted || selected ? 2 : 0}
                  style={{ cursor: "pointer" }}
                  tabIndex={0}
                  width={timeScale.bandwidth()}
                  x={x}
                  y={barY}
                  onBlur={() => onBarHover(null)}
                  onClick={() => onBarClick(segment)}
                  onFocus={() => onBarHover(segment)}
                  onKeyDown={(e) => handleBarKeyDown(e, segment)}
                  onMouseEnter={() => onBarHover(segment)}
                  onMouseLeave={() => onBarHover(null)}
                />
              );
            })}
          </g>
        );
      })}
    </Group>
  );
};

const LabelCell: FC<{ row: LayoutRow }> = ({ row }) => {
  if (row.type === "group") {
    return (
      <div
        className="flex items-center gap-2 px-2 text-sm font-semibold text-foreground"
        style={{ height: GROUP_HEADER_HEIGHT }}
      >
        <span className="truncate">{row.groupName}</span>
        <span className="shrink-0 text-xs font-normal text-muted-foreground">
          {row.threadCount}
        </span>
      </div>
    );
  }

  const { thread, groupName, showGroupHint } = row;

  return (
    <div
      className="flex min-w-0 flex-col justify-center px-2"
      style={{ height: THREAD_ROW_HEIGHT }}
      title={`${groupName} · ${thread.threadName}`}
    >
      {showGroupHint && (
        <span className="truncate text-[10px] leading-tight text-muted-foreground">
          {groupName}
        </span>
      )}
      <span className="truncate text-xs font-medium text-foreground">
        {thread.threadName}
      </span>
    </div>
  );
};

export const DumpTimelineChart: FC<DumpTimelineChartProps> = ({
  groups,
  viewMode,
  selectedDump,
  hoveredSegment,
  onBarClick,
  onBarHover,
}) => {
  const orderedTimes = useMemo(() => getOrderedDumpTimes(groups), [groups]);
  const layout = useMemo(
    () => buildLayout(groups, viewMode),
    [groups, viewMode],
  );

  const bodyHeight = useMemo(() => {
    if (layout.length === 0) {
      return 0;
    }
    const last = layout[layout.length - 1];

    return last.top + (last.type === "group" ? GROUP_HEADER_HEIGHT : THREAD_ROW_HEIGHT);
  }, [layout]);

  const chartHeight = TIME_AXIS_HEIGHT + AXIS_BODY_GAP + bodyHeight;

  if (layout.length === 0 || orderedTimes.length === 0) {
    return (
      <p className="py-12 text-center text-sm text-muted-foreground">
        Waiting for thread dump samples…
      </p>
    );
  }

  return (
    <div className="w-full min-w-0">
      <div
        className="w-full min-w-0 overflow-hidden"
        style={{ height: chartHeight }}
      >
        <div className="flex h-full w-full min-w-0">
          <div className="shrink-0" style={{ width: THREAD_LABEL_WIDTH }}>
            <div style={{ height: TIME_AXIS_HEIGHT + AXIS_BODY_GAP }} />
            {layout.map((row, index) => {
              if (
                viewMode === "grouped" &&
                row.type === "group" &&
                index > 0
              ) {
                return (
                  <div key={`gap-${row.groupName}`}>
                    <div style={{ height: GROUP_SECTION_GAP }} />
                    <LabelCell row={row} />
                  </div>
                );
              }

              return (
                <LabelCell key={`${row.type}-${row.groupName}-${index}`} row={row} />
              );
            })}
          </div>
          <div className="min-w-0 flex-1">
            <ParentSize debounceTime={10}>
              {({ width: plotContainerWidth }) => {
                const plotWidth = getTimelinePlotWidth(
                  orderedTimes.length,
                  plotContainerWidth,
                );
                const scrollable = plotWidth > plotContainerWidth;

                const timeScale = scaleBand<string>({
                  domain: orderedTimes,
                  range: [0, plotWidth],
                  padding: orderedTimes.length <= 1 ? 0 : 0.05,
                });

                const bodyTop = TIME_AXIS_HEIGHT + AXIS_BODY_GAP;

                return (
                  <div
                    className={clsx(
                      "h-full w-full",
                      scrollable && "overflow-x-auto",
                    )}
                  >
                    <svg
                      className="text-foreground"
                      height={chartHeight}
                      width={Math.max(plotContainerWidth, plotWidth)}
                    >
                      <rect
                        fill="var(--color-background-secondary)"
                        height={TIME_AXIS_HEIGHT + AXIS_BODY_GAP}
                        width={Math.max(plotContainerWidth, plotWidth)}
                        x={0}
                        y={0}
                      />
                      <AxisBottom
                        scale={timeScale}
                        stroke="var(--color-border)"
                        tickFormat={formatDumpTimeLabel}
                        tickLabelProps={() => ({
                          fill: "currentColor",
                          fontSize: 11,
                          textAnchor: "middle",
                        })}
                        tickStroke="currentColor"
                        top={TIME_AXIS_HEIGHT - 6}
                      />
                      <TimelineBars
                        bodyTop={bodyTop}
                        hoveredSegment={hoveredSegment}
                        layout={layout}
                        selectedDump={selectedDump}
                        timeScale={timeScale}
                        onBarClick={onBarClick}
                        onBarHover={onBarHover}
                      />
                    </svg>
                  </div>
                );
              }}
            </ParentSize>
          </div>
        </div>
      </div>
      <div className="mt-3 flex flex-wrap justify-center gap-x-4 gap-y-1">
        <LegendOrdinal scale={legendScale}>
          {(labels) =>
            labels.map((label) => (
              <span
                key={label.datum}
                className="inline-flex items-center gap-1.5 text-xs text-muted-foreground"
              >
                <span
                  className="inline-block size-2.5 rounded-sm"
                  style={{ backgroundColor: label.value }}
                />
                {label.datum}
              </span>
            ))
          }
        </LegendOrdinal>
      </div>
    </div>
  );
};
