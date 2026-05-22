import { AxisBottom } from "@visx/axis";
import { Group } from "@visx/group";
import { LegendOrdinal } from "@visx/legend";
import { ParentSize } from "@visx/responsive";
import { scaleOrdinal } from "@visx/scale";
import clsx from "clsx";
import { FC, KeyboardEvent, useLayoutEffect, useMemo, useRef } from "react";

import {
  AXIS_BODY_GAP,
  DumpViewMode,
  GROUP_HEADER_HEIGHT,
  GROUP_SECTION_GAP,
  STACK_KEYS,
  THREAD_BAR_VERTICAL_INSET,
  THREAD_LABEL_WIDTH,
  THREAD_ROW_HEIGHT,
  THREAD_STATE_STYLES,
  TIME_AXIS_HEIGHT,
} from "./constants.ts";
import { DumpTimelineBar } from "./dump-timeline-bar.tsx";
import { buildTimelineScale } from "./timeline-scale.ts";
import { DumpBarSegment, dumpSegmentKey, segmentsMatch } from "./utils.ts";
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
      .flatMap((group) => group.threads.map((thread) => ({ group, thread })))
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
  windowStartMs: number;
  selectedDump: SelectedDumpContext | null;
  hoveredSegment: DumpBarSegment | null;
  onBarClick: (segment: DumpBarSegment) => void;
  onBarHover: (segment: DumpBarSegment | null) => void;
}

interface TimelineBarsProps {
  layout: LayoutRow[];
  bodyTop: number;
  shouldAnimateEnter: (
    groupName: string,
    threadName: string,
    time: string,
    rightmostTime: string | undefined,
  ) => boolean;
  getBarGeometry: (
    time: string,
  ) => import("./timeline-scale.ts").BarGeometry | null;
  selectedDump: SelectedDumpContext | null;
  hoveredSegment: DumpBarSegment | null;
  onBarClick: (segment: DumpBarSegment) => void;
  onBarHover: (segment: DumpBarSegment | null) => void;
}

const TimelineBars: FC<TimelineBarsProps> = ({
  layout,
  bodyTop,
  shouldAnimateEnter,
  getBarGeometry,
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

  const barHeight = THREAD_ROW_HEIGHT - THREAD_BAR_VERTICAL_INSET * 2;

  return (
    <Group top={bodyTop}>
      {layout.map((row) => {
        if (row.type === "group") {
          return null;
        }

        const { thread, groupName, top } = row;
        const barY = top + THREAD_BAR_VERTICAL_INSET;
        const rightmostTime = thread.dumps[thread.dumps.length - 1]?.time;

        return (
          <g key={`${groupName}-${thread.threadName}`}>
            {thread.dumps.map((entry) => {
              const geom = getBarGeometry(entry.time);

              if (!geom) {
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

              const animateEnter = shouldAnimateEnter(
                groupName,
                thread.threadName,
                entry.time,
                rightmostTime,
              );

              return (
                <DumpTimelineBar
                  key={`${groupName}-${thread.threadName}-${entry.time}`}
                  animateEnter={animateEnter}
                  ariaLabel={`${thread.threadName} ${state} at ${entry.time}`}
                  barHeight={barHeight}
                  barY={barY}
                  emphasize={animateEnter}
                  fill={style.color}
                  geom={geom}
                  highlighted={highlighted || selected}
                  opacity={dimmed ? 0.4 : 1}
                  onClick={() => onBarClick(segment)}
                  onHover={(active) => onBarHover(active ? segment : null)}
                  onKeyDown={(e) => handleBarKeyDown(e, segment)}
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
        <span className="truncate text-accent">{row.groupName}</span>
        <span className="shrink-0 text-xs font-normal text-accent-foreground">
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
  windowStartMs,
  selectedDump,
  hoveredSegment,
  onBarClick,
  onBarHover,
}) => {
  const layout = useMemo(
    () => buildLayout(groups, viewMode),
    [groups, viewMode],
  );
  const seenSegmentsRef = useRef(new Set<string>());

  const shouldAnimateEnter = useMemo(
    () =>
      (
        groupName: string,
        threadName: string,
        time: string,
        rightmostTime: string | undefined,
      ) =>
        time === rightmostTime &&
        !seenSegmentsRef.current.has(
          dumpSegmentKey(groupName, threadName, time),
        ),
    [],
  );

  useLayoutEffect(() => {
    groups.forEach((group) => {
      group.threads.forEach((thread) => {
        thread.dumps.forEach((entry) => {
          seenSegmentsRef.current.add(
            dumpSegmentKey(group.name, thread.threadName, entry.time),
          );
        });
      });
    });
  }, [groups]);

  const bodyHeight = useMemo(() => {
    if (layout.length === 0) {
      return THREAD_ROW_HEIGHT;
    }
    const last = layout[layout.length - 1];

    return (
      last.top +
      (last.type === "group" ? GROUP_HEADER_HEIGHT : THREAD_ROW_HEIGHT)
    );
  }, [layout]);

  const chartHeight = TIME_AXIS_HEIGHT + AXIS_BODY_GAP + bodyHeight;

  return (
    <div className="w-full min-w-0">
      <div
        className="w-full min-w-0 overflow-hidden"
        style={{ height: chartHeight }}
      >
        <div className="flex h-full w-full min-w-0">
          <div className="shrink-0" style={{ width: THREAD_LABEL_WIDTH }}>
            <div style={{ height: TIME_AXIS_HEIGHT + AXIS_BODY_GAP }} />
            {layout.length === 0 ? (
              <div
                className="flex items-center px-2 text-xs text-muted-foreground"
                style={{ height: THREAD_ROW_HEIGHT }}
              >
                等待线程数据…
              </div>
            ) : (
              layout.map((row, index) => {
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
                  <LabelCell
                    key={`${row.type}-${row.groupName}-${index}`}
                    row={row}
                  />
                );
              })
            )}
          </div>
          <div className="min-w-0 flex-1">
            <ParentSize debounceTime={10}>
              {({ width: plotContainerWidth }) => {
                const timeline = buildTimelineScale(
                  windowStartMs,
                  groups,
                  plotContainerWidth,
                );
                const bodyTop = TIME_AXIS_HEIGHT + AXIS_BODY_GAP;
                const svgWidth = Math.max(
                  plotContainerWidth,
                  timeline.plotWidth,
                );

                return (
                  <div
                    className={clsx(
                      "h-full w-full",
                      timeline.scrollable && "overflow-x-auto",
                    )}
                  >
                    <svg
                      className="text-foreground"
                      height={chartHeight}
                      width={svgWidth}
                    >
                      <rect
                        fill="var(--color-background-secondary)"
                        height={TIME_AXIS_HEIGHT + AXIS_BODY_GAP}
                        width={svgWidth}
                        x={0}
                        y={0}
                      />
                      <AxisBottom
                        scale={timeline.axisScale}
                        stroke="var(--color-border)"
                        tickFormat={(slotId) =>
                          timeline.axisTickFormat(String(slotId))
                        }
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
                        getBarGeometry={timeline.getBarGeometry}
                        hoveredSegment={hoveredSegment}
                        layout={layout}
                        selectedDump={selectedDump}
                        shouldAnimateEnter={shouldAnimateEnter}
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
