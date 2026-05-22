import {
  EventSourceMessage,
  fetchEventSource,
} from "@microsoft/fetch-event-source";
import { useCallback, useEffect, useState } from "react";

import { DumpViewMode, MAX_DUMPS_PER_THREAD } from "./constants.ts";
import { DumpDetailDrawer } from "./dump-ui.tsx";
import { DumpTimelineChart } from "./dump-timeline-chart.tsx";
import { Dump, Group, SelectedDumpContext, ThreadDumpEntry } from "./types.ts";
import { DumpBarSegment, collectThreadsAtDump } from "./utils.ts";

import { Segment } from "@/components/ui/segment.tsx";

/** Thread Dump 页：SSE 订阅 + 时间轴 + 详情抽屉 */
export function DumpView() {
  const [windowStartMs] = useState(() => Date.now());
  const [groups, setGroups] = useState<Group[]>([]);
  const [viewMode, setViewMode] = useState<DumpViewMode>("grouped");
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [selectedDump, setSelectedDump] = useState<SelectedDumpContext | null>(
    null,
  );
  const [hoveredSegment, setHoveredSegment] = useState<DumpBarSegment | null>(
    null,
  );

  const handleBarClick = useCallback(
    (segment: DumpBarSegment) => {
      const group = groups.find((g) => g.name === segment.groupName);

      if (!group) {
        return;
      }
      const threads = collectThreadsAtDump(
        group,
        segment.time,
        segment.threadState,
      );

      if (threads.length === 0) {
        return;
      }

      setSelectedDump({
        groupName: segment.groupName,
        time: segment.time,
        threadState: segment.threadState,
        focusedThreadName: segment.threadName,
        threads,
      });
      setDrawerOpen(true);
      setHoveredSegment(segment);
    },
    [groups],
  );

  const handleDrawerThreadHover = useCallback(
    (threadName: string | null) => {
      if (!selectedDump) {
        setHoveredSegment(null);

        return;
      }
      if (!threadName) {
        setHoveredSegment({
          groupName: selectedDump.groupName,
          threadName: selectedDump.focusedThreadName,
          time: selectedDump.time,
          threadState: selectedDump.threadState,
        });

        return;
      }
      setHoveredSegment({
        groupName: selectedDump.groupName,
        threadName,
        time: selectedDump.time,
        threadState: selectedDump.threadState,
      });
    },
    [selectedDump],
  );

  useEffect(() => {
    const ctrl = new AbortController();

    fetchEventSource(
      `${import.meta.env.VITE_API_DASHBOARD_V1}metrics/thread-dump`,
      {
        method: "GET",
        signal: ctrl.signal,
        onmessage: (event: EventSourceMessage) => {
          if (event.event === "dumpInfo") {
            const data: Dump = JSON.parse(event.data);

            setGroups((prevState) => {
              const groupIndex = prevState.findIndex(
                (g) => g.name === data.value.group,
              );
              const entry: ThreadDumpEntry = {
                time: data.time,
                dumpInfo: data.value.dumpInfo,
              };

              if (groupIndex > -1) {
                const tempGroup = { ...prevState[groupIndex] };
                const threadIndex = tempGroup.threads.findIndex(
                  (t) => t.threadName === data.value.dumpInfo.threadName,
                );

                if (threadIndex > -1) {
                  const tempThread = { ...tempGroup.threads[threadIndex] };
                  const dumps = [...tempThread.dumps];

                  if (dumps.length >= MAX_DUMPS_PER_THREAD) {
                    dumps.shift();
                  }
                  const existingIndex = dumps.findIndex(
                    (d) => d.time === data.time,
                  );

                  if (existingIndex > -1) {
                    dumps[existingIndex] = entry;
                  } else {
                    dumps.push(entry);
                  }
                  tempThread.dumps = dumps;
                  tempGroup.threads = tempGroup.threads.toSpliced(
                    threadIndex,
                    1,
                    tempThread,
                  );
                } else {
                  tempGroup.threads = [
                    ...tempGroup.threads,
                    {
                      threadName: data.value.dumpInfo.threadName,
                      threadId: data.value.dumpInfo.threadId,
                      dumps: [entry],
                    },
                  ];
                }

                return prevState.toSpliced(groupIndex, 1, tempGroup);
              }

              return prevState.concat([
                {
                  name: data.value.group,
                  threads: [
                    {
                      threadName: data.value.dumpInfo.threadName,
                      threadId: data.value.dumpInfo.threadId,
                      dumps: [entry],
                    },
                  ],
                },
              ]);
            });
          }
        },
      },
    ).then(() => {
      // stream ended
    });

    return () => {
      ctrl.abort();
    };
  }, []);

  return (
    <div className="flex w-full min-w-0 flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Segment
          aria-label="展示方式"
          selectedKey={viewMode}
          variant="default"
          onSelectionChange={(key) => setViewMode(key as DumpViewMode)}
        >
          <Segment.Item id="grouped">分组</Segment.Item>
          <Segment.Item id="flat">
            <Segment.Separator />
            平铺
          </Segment.Item>
        </Segment>
        <span className="text-xs text-muted-foreground">
          {groups.length} 个分组 ·{" "}
          {groups.reduce((n, g) => n + g.threads.length, 0)} 条线程
        </span>
      </div>
      <DumpTimelineChart
        groups={groups}
        hoveredSegment={hoveredSegment}
        selectedDump={selectedDump}
        viewMode={viewMode}
        windowStartMs={windowStartMs}
        onBarClick={handleBarClick}
        onBarHover={setHoveredSegment}
      />
      <DumpDetailDrawer
        context={selectedDump}
        isOpen={drawerOpen}
        onOpenChange={(open) => {
          setDrawerOpen(open);
          if (!open) {
            setHoveredSegment(null);
            setSelectedDump(null);
          }
        }}
        onThreadHover={handleDrawerThreadHover}
      />
    </div>
  );
}
