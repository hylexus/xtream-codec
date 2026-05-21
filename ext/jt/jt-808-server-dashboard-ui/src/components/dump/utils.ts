import {
  Group,
  SelectedDumpContext,
  ThreadState,
} from "./types.ts";

/** Backend: yyyy-MM-dd HH:mm:ss.SSS (GMT+8) */
export const parseDumpTime = (time: string): number => {
  const iso = time.includes("T") ? time : time.replace(" ", "T");
  const ms = Date.parse(iso);

  return Number.isNaN(ms) ? Date.parse(time) : ms;
};

export const formatDumpTimeLabel = (time: string) => {
  const ms = parseDumpTime(time);

  if (Number.isNaN(ms)) {
    return time;
  }
  const d = new Date(ms);

  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}:${String(d.getSeconds()).padStart(2, "0")}`;
};

export const getOrderedDumpTimes = (groups: Group[]): string[] => {
  const times = new Set<string>();

  groups.forEach((group) => {
    group.threads.forEach((thread) => {
      thread.dumps.forEach((d) => times.add(d.time));
    });
  });

  return Array.from(times).sort(
    (a, b) => parseDumpTime(a) - parseDumpTime(b),
  );
};

export const collectThreadsAtDump = (
  group: Group,
  time: string,
  threadState: ThreadState,
): SelectedDumpContext["threads"] =>
  group.threads.flatMap((thread) => {
    const entry = thread.dumps.find(
      (d) => d.time === time && d.dumpInfo.threadState === threadState,
    );

    return entry
      ? [{ threadName: thread.threadName, dumpInfo: entry.dumpInfo }]
      : [];
  });

export type DumpBarSegment = {
  groupName: string;
  threadName: string;
  time: string;
  threadState: ThreadState;
};

export const segmentsMatch = (
  a: DumpBarSegment | null,
  b: DumpBarSegment | null,
) =>
  a != null &&
  b != null &&
  a.groupName === b.groupName &&
  a.threadName === b.threadName &&
  a.time === b.time &&
  a.threadState === b.threadState;
