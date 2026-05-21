import { FC } from "react";

import { StackFrame } from "./types.ts";

const frameLocation = (frame: StackFrame) => {
  if (frame.fileName != null) {
    return `${frame.fileName}:${frame.lineNumber}`;
  }
  if (frame.nativeMethod) {
    return "Native Method";
  }

  return "Unknown";
};

export const StackTraceView: FC<{ frames: StackFrame[] }> = ({ frames }) => (
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
