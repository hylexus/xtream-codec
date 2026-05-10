import { Accordion, Chip, useTheme } from "@heroui/react";
import {
  EventSourceMessage,
  fetchEventSource,
} from "@microsoft/fetch-event-source";
import { useEffect, useMemo, useState } from "react";
import { Axis } from "@visx/axis";
import { timeFormat } from "@visx/vendor/d3-time-format";
import { coerceNumber, scaleUtc } from "@visx/scale";
import { ParentSize } from "@visx/responsive";
import { NumberValue } from "@visx/vendor/d3-scale";

import { Dump, Group } from "./types.ts";

const types = {
  NEW: { color: "#7b9ce1" },
  RUNNABLE: { color: "#bd6d6c" },
  BLOCKED: { color: "#75d874" },
  WAITING: { color: "#e0bc78" },
  TIMED_WAITING: { color: "#dc77dc" },
  TERMINATED: { color: "#72b362" },
};
const format = timeFormat("%H:%m:%S");
const maxPixelsPerSeconds = 15;

export const DumpGroup = () => {
  const [groups, setGroups] = useState<Group[]>([]);
  const [timeValues, setTimeValues] = useState<NumberValue[]>([]);
  const { theme } = useTheme();
  const strokeColor = useMemo(
    () => (theme === "dark" ? "#e4e4e7" : "#18181b"),
    [theme],
  );
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
              <svg height={height} width={axisWidth}>
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
                    fill: strokeColor,
                    fontSize: 12,
                    textAnchor: "middle",
                  }}
                  tickStroke={strokeColor}
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
                <h2>{group.name}</h2>
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
                          <div className="line-clamp-1 w-40 shrink-0">
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
                      <ul>
                        <li>
                          <span>ID: </span>
                          <span>{thread.threadId}</span>
                        </li>
                        <li>
                          <span>StackTrance: </span>
                          <span>
                            <pre>
                              {JSON.stringify(thread.stackTrace, null, 2)}
                            </pre>
                          </span>
                        </li>
                      </ul>
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
