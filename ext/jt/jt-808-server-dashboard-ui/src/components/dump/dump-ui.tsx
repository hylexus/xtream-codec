import {
  Button,
  Chip,
  Drawer,
  ScrollShadow,
  useOverlayState,
} from "@heroui/react";
import clsx from "clsx";
import { useEffect, useMemo, useState } from "react";
import { X } from "lucide-react";

import { THREAD_STATE_STYLES } from "./constants.ts";
import { DumpInfo, SelectedDumpContext, StackFrame } from "./types.ts";
import { formatDumpTimeLabel } from "./utils.ts";

import { subtitle } from "@/components/primitives.ts";

const frameLocation = (frame: StackFrame) => {
  if (frame.fileName != null) {
    return `${frame.fileName}:${frame.lineNumber}`;
  }
  if (frame.nativeMethod) {
    return "Native Method";
  }

  return "Unknown";
};

export function StackTraceView({ frames }: { frames: StackFrame[] }) {
  return (
    <div className="overflow-x-auto rounded-md border border-border bg-zinc-950 p-4 font-mono text-xs leading-relaxed">
      {frames.map((frame, i) => {
        const isNative = frame.nativeMethod;

        return (
          <div key={i} className={isNative ? "text-warning" : "text-zinc-300"}>
            <span className="text-zinc-500">{"    at "}</span>
            <span className="text-sky-400">{frame.className}</span>
            <span className="text-zinc-500">.</span>
            <span className="text-amber-200">{frame.methodName}</span>
            <span className="text-zinc-500">({frameLocation(frame)})</span>
            {isNative && (
              <span className="ml-2 font-semibold text-warning">[native]</span>
            )}
          </div>
        );
      })}
    </div>
  );
}

function ThreadMeta({ dump }: { dump: DumpInfo }) {
  return (
    <div className="grid grid-cols-2 gap-x-6 gap-y-2 text-sm">
      <div>
        <span className="text-muted">Thread ID: </span>
        <span className="text-foreground">{dump.threadId}</span>
      </div>
      <div>
        <span className="text-muted">Priority: </span>
        <span className="text-foreground">{dump.priority}</span>
      </div>
      <div>
        <span className="text-muted">Daemon: </span>
        <span className="text-foreground">{dump.daemon ? "Yes" : "No"}</span>
      </div>
      <div>
        <span className="text-muted">Blocked Count: </span>
        <span className="text-foreground">{dump.blockedCount}</span>
      </div>
      <div>
        <span className="text-muted">Blocked Time: </span>
        <span className="text-foreground">{dump.blockedTime}</span>
      </div>
      <div>
        <span className="text-muted">Waited Count: </span>
        <span className="text-foreground">{dump.waitedCount}</span>
      </div>
      <div>
        <span className="text-muted">Waited Time: </span>
        <span className="text-foreground">{dump.waitedTime}</span>
      </div>
      {dump.lockName && (
        <div className="col-span-2">
          <span className="text-muted">Lock: </span>
          <span className="text-foreground">{dump.lockName}</span>
        </div>
      )}
      {dump.lockOwnerName && (
        <div className="col-span-2">
          <span className="text-muted">Lock Owner: </span>
          <span className="text-foreground">
            {dump.lockOwnerName} (ID: {dump.lockOwnerId})
          </span>
        </div>
      )}
    </div>
  );
}

export interface DumpDetailDrawerProps {
  context: SelectedDumpContext | null;
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  onThreadHover?: (threadName: string | null) => void;
}

export function DumpDetailDrawer({
  context,
  isOpen,
  onOpenChange,
  onThreadHover,
}: DumpDetailDrawerProps) {
  const drawerState = useOverlayState({ isOpen, onOpenChange });
  const [activeThreadName, setActiveThreadName] = useState<string | null>(null);

  useEffect(() => {
    if (context) {
      setActiveThreadName(context.focusedThreadName);
    }
  }, [context]);

  const activeThread = useMemo(() => {
    if (!context || !activeThreadName) {
      return null;
    }

    return (
      context.threads.find((t) => t.threadName === activeThreadName) ?? null
    );
  }, [context, activeThreadName]);

  const stateStyle = context ? THREAD_STATE_STYLES[context.threadState] : null;

  return (
    <Drawer state={drawerState}>
      <Drawer.Backdrop variant="blur">
        <Drawer.Content placement="right">
          <Drawer.Dialog className="flex w-full max-w-6xl flex-col sm:w-2/3">
            <Drawer.Header className="flex items-start justify-between gap-4 border-b border-border pb-4">
              <div className="min-w-0 space-y-2">
                <h2 className={subtitle({ class: "text-lg" })}>
                  Thread Dump
                  {context && (
                    <span className="ml-2 font-mono text-base font-normal text-muted-foreground">
                      {formatDumpTimeLabel(context.time)}
                    </span>
                  )}
                </h2>
                {context && stateStyle && (
                  <div className="flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
                    <span>{context.groupName}</span>
                    <Chip
                      size="sm"
                      style={{
                        backgroundColor: stateStyle.color,
                        color: stateStyle.textColor,
                      }}
                    >
                      {context.threadState}
                    </Chip>
                    <span>
                      {context.threads.length} thread
                      {context.threads.length === 1 ? "" : "s"} in this state
                    </span>
                  </div>
                )}
              </div>
              <Button
                isIconOnly
                aria-label="Close"
                size="sm"
                variant="ghost"
                onPress={() => onOpenChange(false)}
              >
                <X className="size-4" />
              </Button>
            </Drawer.Header>
            <Drawer.Body className="flex min-h-0 flex-1 flex-col gap-4 overflow-hidden p-0">
              {context && activeThread ? (
                <div className="flex min-h-0 flex-1 gap-0">
                  <ScrollShadow
                    hideScrollBar
                    className="w-72 shrink-0 border-r border-border"
                  >
                    <ul className="list-none p-2">
                      {context.threads.map((thread) => {
                        const isActive = thread.threadName === activeThreadName;

                        return (
                          <li key={thread.threadName}>
                            <button
                              className={clsx(
                                "w-full rounded-md px-3 py-2 text-left font-mono text-xs transition-colors",
                                isActive
                                  ? "bg-accent/15 text-foreground"
                                  : "text-muted-foreground hover:bg-background-tertiary hover:text-foreground",
                              )}
                              type="button"
                              onClick={() =>
                                setActiveThreadName(thread.threadName)
                              }
                              onMouseEnter={() => {
                                setActiveThreadName(thread.threadName);
                                onThreadHover?.(thread.threadName);
                              }}
                              onMouseLeave={() => onThreadHover?.(null)}
                            >
                              <div className="truncate font-medium">
                                {thread.threadName}
                              </div>
                              <div className="mt-0.5 text-muted">
                                ID {thread.dumpInfo.threadId}
                              </div>
                            </button>
                          </li>
                        );
                      })}
                    </ul>
                  </ScrollShadow>
                  <ScrollShadow hideScrollBar className="min-w-0 flex-1 p-4">
                    <div className="space-y-4">
                      <ThreadMeta dump={activeThread.dumpInfo} />
                      <div>
                        <div className="mb-2 text-sm font-medium text-foreground">
                          Stack Trace
                        </div>
                        <StackTraceView
                          frames={activeThread.dumpInfo.stackTrace}
                        />
                      </div>
                    </div>
                  </ScrollShadow>
                </div>
              ) : (
                <p className="p-4 text-sm text-muted-foreground">
                  Select a dump block to view thread details.
                </p>
              )}
            </Drawer.Body>
          </Drawer.Dialog>
        </Drawer.Content>
      </Drawer.Backdrop>
    </Drawer>
  );
}
