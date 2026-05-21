export interface StackFrame {
  classLoaderName: string;
  moduleName: string;
  moduleVersion: string;
  methodName: string;
  fileName: string;
  lineNumber: number;
  className: string;
  nativeMethod: boolean;
}

export interface DumpInfo {
  threadName: string;
  threadId: number;
  blockedTime: number;
  blockedCount: number;
  waitedTime: number;
  waitedCount: number;
  lockName: string;
  lockOwnerId: number;
  lockOwnerName: string;
  daemon: boolean;
  inNative: boolean;
  suspended: boolean;
  threadState:
    | "NEW"
    | "RUNNABLE"
    | "BLOCKED"
    | "WAITING"
    | "TIMED_WAITING"
    | "TERMINATED";
  priority: number;
  stackTrace: StackFrame[];
  lockedMonitors: unknown[];
  lockedSynchronizers: unknown[];
  lockInfo: { className: string; identityHashCode: number };
}

export type ThreadState = DumpInfo["threadState"];

export interface Dump {
  time: string;
  value: {
    group: string;
    dumpInfo: DumpInfo;
  };
}

export interface ThreadDumpEntry {
  time: string;
  dumpInfo: DumpInfo;
}

export interface Thread {
  threadName: string;
  threadId: number;
  dumps: ThreadDumpEntry[];
}

export interface Group {
  name: string;
  threads: Thread[];
}

export interface SelectedDumpContext {
  groupName: string;
  time: string;
  threadState: ThreadState;
  focusedThreadName: string;
  threads: { threadName: string; dumpInfo: DumpInfo }[];
}
