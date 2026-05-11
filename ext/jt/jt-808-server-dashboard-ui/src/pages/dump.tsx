import { DumpGroup } from "@/components/dump/group.tsx";

export const DumpPage = () => {
  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight text-foreground">
          线程 Dump
        </h2>
        <p className="mt-1 max-w-3xl text-sm leading-relaxed text-muted">
          按分组与线程名查看线程状态时间轴与栈信息；时间轴刻度颜色随主题变化。
        </p>
      </div>
      <DumpGroup />
    </div>
  );
};
