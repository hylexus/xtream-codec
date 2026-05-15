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
  stackTrace: {
    classLoaderName: string;
    moduleName: string;
    moduleVersion: string;
    methodName: string;
    fileName: string;
    lineNumber: number;
    className: string;
    nativeMethod: boolean;
  }[];
  lockedMonitors: any[];
  lockedSynchronizers: any[];
  lockInfo: { className: string; identityHashCode: number };
}

export interface Dump {
  time: string;
  value: {
    group: string;
    dumpInfo: DumpInfo;
  };
}

export interface Group {
  name: string;
  threads: [
    {
      threadName: string;
      threadId: number;
      stackTrace: any;
      dumps: {
        time: string;
        threadState:
          | "NEW"
          | "RUNNABLE"
          | "BLOCKED"
          | "WAITING"
          | "TIMED_WAITING"
          | "TERMINATED";
      }[];
      latestDumpInfo?: DumpInfo;
    },
  ];
}
