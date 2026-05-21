import { ThreadState } from "./types.ts";

export const STACK_KEYS: ThreadState[] = [
  "TERMINATED",
  "NEW",
  "RUNNABLE",
  "TIMED_WAITING",
  "WAITING",
  "BLOCKED",
];

export const THREAD_STATE_STYLES: Record<
  ThreadState,
  { color: string; textColor: string }
> = {
  NEW: { color: "#f5f5f5", textColor: "#000" },
  RUNNABLE: { color: "#48c78e", textColor: "#000" },
  BLOCKED: { color: "#f14668", textColor: "#fff" },
  WAITING: { color: "#ffe08a", textColor: "#000" },
  TIMED_WAITING: { color: "#ffe08a", textColor: "#000" },
  TERMINATED: { color: "#f5f5f5", textColor: "#000" },
};

export const MAX_DUMPS_PER_THREAD = 20;
export const DUMP_SEGMENT_WIDTH = 15;
export const TIME_AXIS_HEIGHT = 32;
export const AXIS_BODY_GAP = 12;
export const THREAD_ROW_HEIGHT = 30;
export const THREAD_LABEL_WIDTH = 200;
export const GROUP_HEADER_HEIGHT = 36;
export const GROUP_SECTION_GAP = 12;

export type DumpViewMode = "grouped" | "flat";
