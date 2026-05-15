import { Accordion, Chip } from "@heroui/react";
import {
  EventSourceMessage,
  fetchEventSource,
} from "@microsoft/fetch-event-source";
import { useEffect, useState } from "react";
import { Axis } from "@visx/axis";
import { timeFormat } from "@visx/vendor/d3-time-format";
import { coerceNumber, scaleUtc } from "@visx/scale";
import { ParentSize } from "@visx/responsive";
import { NumberValue } from "@visx/vendor/d3-scale";

import { Dump, Group } from "./types.ts";

// spring-boot-admin
const types = {
  NEW: { color: "#f5f5f5", textColor: "#000" },
  RUNNABLE: { color: "#48c78e", textColor: "#000" },
  BLOCKED: { color: "#f14668", textColor: "#fff" },
  WAITING: { color: "#ffe08a", textColor: "#000" },
  TIMED_WAITING: { color: "#ffe08a", textColor: "#000" },
  TERMINATED: { color: "#f5f5f5", textColor: "#000" },
};
const format = timeFormat("%H:%m:%S");
const maxPixelsPerSeconds = 15;

export const DumpGroup = () => {
  const [groups, setGroups] = useState<Group[]>([]);
  const [timeValues, setTimeValues] = useState<NumberValue[]>([]);
  const calcTimeValues = (time: string) => {
    const dateTime = +new Date(time.slice(0, -4));
    let _tempTimes = [...timeValues];

    if (dateTime) {
      if (timeValues.includes(dateTime)) {
        return;
      }
      _tempTimes.push(+dateTime);
      setTimeValues(() => _tempTimes);
    }
  };

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

            calcTimeValues(data.time);
            setGroups((prevState) => {
              const groupIndex = prevState.findIndex(
                (group) => group.name === data.value.group,
              );

              if (groupIndex > -1) {
                const tempGroup = prevState[groupIndex];
                const threadIndex = tempGroup.threads.findIndex(
                  (thread) =>
                    thread.threadName === data.value.dumpInfo.threadName,
                );

                if (threadIndex > -1) {
                  const tempThread = tempGroup.threads[threadIndex];

                  if (tempThread.dumps.length > 20) {
                    tempThread.dumps.shift();
                  }
                  if (!tempThread.dumps.find((e) => e.time === data.time)) {
                    tempThread.dumps.push({
                      time: data.time,
                      threadState: data.value.dumpInfo.threadState,
                    });
                  }
                  tempThread.latestDumpInfo = data.value.dumpInfo;
                  tempGroup.threads[threadIndex] = tempThread;
                } else {
                  tempGroup.threads.push({
                    threadName: data.value.dumpInfo.threadName,
                    stackTrace: data.value.dumpInfo.stackTrace,
                    threadId: data.value.dumpInfo.threadId,
                    dumps: [
                      {
                        time: data.time,
                        threadState: data.value.dumpInfo.threadState,
                      },
                    ],
                    latestDumpInfo: data.value.dumpInfo,
                  });
                }

                return prevState.toSpliced(groupIndex, 1, tempGroup);
              } else {
                return prevState.concat([
                  {
                    name: data.value.group,
                    threads: [
                      {
                        threadName: data.value.dumpInfo.threadName,
                        stackTrace: data.value.dumpInfo.stackTrace,
                        threadId: data.value.dumpInfo.threadId,
                        dumps: [
                          {
                            time: data.time,
                            threadState: data.value.dumpInfo.threadState,
                          },
                        ],
                        latestDumpInfo: data.value.dumpInfo,
                      },
                    ],
                  },
                ]);
              }
            });
          }
        },
      },
    ).then(() => {
      // TODO
    });

    return () => {
      ctrl.abort();
    };
  }, []);
  const getMinMax = (vals: (number | { valueOf(): number })[]) => {
    const numericVals = vals.map(coerceNumber);

    return [Math.min(...numericVals), Math.max(...numericVals)];
  };

  return (
    <div className="w-full">
      <div className="ml-32 h-10 w-full">
        <ParentSize>
          {({ width, height }) => {
            const [start, end] = getMinMax(timeValues);
            const totalSeconds = Math.floor(width / maxPixelsPerSeconds);
            const axisWidth = width - 128 - 40 + 10;

            return (
              <svg className="text-muted" height={height} width={axisWidth}>
                <Axis
                  hideAxisLine
                  left={40}
                  scale={scaleUtc({
                    domain: [
                      start,
                      Math.max(start + (totalSeconds + 1) * 1000, end),
                    ],
                    range: [0, axisWidth],
                    nice: true,
                  })}
                  tickFormat={(v: NumberValue) => format(v as Date)}
                  tickLabelProps={{
                    fill: "currentColor",
                    fontSize: 12,
                    textAnchor: "middle",
                  }}
                  tickStroke="currentColor"
                />
              </svg>
            );
          }}
        </ParentSize>
      </div>
      <Accordion allowsMultipleExpanded className="w-full">
        {groups.map((group) => (
          <Accordion.Item key={group.name} id={group.name}>
            <Accordion.Heading>
              <Accordion.Trigger className="flex w-full items-center justify-between gap-2">
                <h2 className="text-lg font-semibold text-foreground">
                  {group.name}
                </h2>
                <div className="flex items-center gap-2">
                  <Chip size="sm" variant="soft">
                    {group.threads.length}
                  </Chip>
                  <Accordion.Indicator />
                </div>
              </Accordion.Trigger>
            </Accordion.Heading>
            <Accordion.Panel>
              {group.threads.map((thread) => (
                <Accordion
                  key={thread.threadName}
                  allowsMultipleExpanded
                  className="w-full"
                >
                  <Accordion.Item id={`${group.name}-${thread.threadName}`}>
                    <Accordion.Heading>
                      <Accordion.Trigger className="flex w-full items-stretch gap-2">
                        <div className="flex min-w-0 flex-1">
                          <div className="w-40 shrink-0 truncate font-medium text-foreground">
                            {thread.threadName}
                          </div>
                          <div className="min-w-0 flex-1">
                            <ParentSize>
                              {({ width, height }) => {
                                const reactWidth = width - 128;

                                return (
                                  <svg height={height} width={reactWidth}>
                                    {thread.dumps.map((e, i) => (
                                      <rect
                                        key={i}
                                        className="duration-150 transition-width"
                                        fill={types[e.threadState].color}
                                        height={20}
                                        width={20}
                                        x={i * 20}
                                        y={10}
                                      />
                                    ))}
                                  </svg>
                                );
                              }}
                            </ParentSize>
                          </div>
                        </div>
                        <Accordion.Indicator className="hidden" />
                      </Accordion.Trigger>
                    </Accordion.Heading>
                    <Accordion.Panel>
                      {(() => {
                        const dump = thread.latestDumpInfo;

                        if (!dump) return null;

                        const renderStackTrace = (
                          stack: typeof dump.stackTrace,
                        ) =>
                          stack.map((frame, i) => {
                            const isNative = frame.nativeMethod;
                            const location =
                              frame.fileName != null
                                ? `${frame.fileName}:${frame.lineNumber}`
                                : isNative
                                  ? "Native Method"
                                  : "Unknown";

                            return (
                              <div
                                key={i}
                                className={isNative ? "text-warning" : ""}
                              >
                                {`    at ${frame.className}.${frame.methodName}(${location})`}
                                {isNative && (
                                  <span className="ml-2 font-semibold text-warning">
                                    [native]
                                  </span>
                                )}
                              </div>
                            );
                          });

                        return (
                          <div className="space-y-4 pl-5">
                            <div className="flex items-center gap-3">
                              <Chip
                                size="sm"
                                style={{
                                  backgroundColor:
                                    types[dump.threadState].color,
                                  color: types[dump.threadState].textColor,
                                }}
                              >
                                {dump.threadState}
                              </Chip>
                              <span className="text-sm text-muted-foreground">
                                {dump.threadName}
                              </span>
                            </div>

                            <div className="grid grid-cols-2 gap-x-6 gap-y-2 text-sm">
                              <div>
                                <span className="text-muted">Thread ID: </span>
                                <span className="text-foreground">
                                  {dump.threadId}
                                </span>
                              </div>
                              <div>
                                <span className="text-muted">Priority: </span>
                                <span className="text-foreground">
                                  {dump.priority}
                                </span>
                              </div>
                              <div>
                                <span className="text-muted">Daemon: </span>
                                <span className="text-foreground">
                                  {dump.daemon ? "Yes" : "No"}
                                </span>
                              </div>
                              <div>
                                <span className="text-muted">
                                  Blocked Count:{" "}
                                </span>
                                <span className="text-foreground">
                                  {dump.blockedCount}
                                </span>
                              </div>
                              <div>
                                <span className="text-muted">
                                  Blocked Time:{" "}
                                </span>
                                <span className="text-foreground">
                                  {dump.blockedTime}
                                </span>
                              </div>
                              <div>
                                <span className="text-muted">
                                  Waited Count:{" "}
                                </span>
                                <span className="text-foreground">
                                  {dump.waitedCount}
                                </span>
                              </div>
                              <div>
                                <span className="text-muted">
                                  Waited Time:{" "}
                                </span>
                                <span className="text-foreground">
                                  {dump.waitedTime}
                                </span>
                              </div>
                              {dump.lockName && (
                                <div className="col-span-2">
                                  <span className="text-muted">Lock: </span>
                                  <span className="text-foreground">
                                    {dump.lockName}
                                  </span>
                                </div>
                              )}
                              {dump.lockOwnerName && (
                                <div className="col-span-2">
                                  <span className="text-muted">
                                    Lock Owner:{" "}
                                  </span>
                                  <span className="text-foreground">
                                    {dump.lockOwnerName} (ID: {dump.lockOwnerId}
                                    )
                                  </span>
                                </div>
                              )}
                            </div>

                            <div>
                              <div className="mb-1 text-sm font-medium text-foreground">
                                Stack Trace
                              </div>
                              <div className="overflow-x-auto rounded-md border border-border bg-background-tertiary/80 p-3 font-mono text-xs text-foreground">
                                {renderStackTrace(dump.stackTrace)}
                              </div>
                            </div>
                          </div>
                        );
                      })()}
                    </Accordion.Panel>
                  </Accordion.Item>
                </Accordion>
              ))}
            </Accordion.Panel>
          </Accordion.Item>
        ))}
      </Accordion>
    </div>
  );
};
