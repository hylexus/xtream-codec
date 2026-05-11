import { SessionTable } from "@/components/sessions/table.tsx";

export const InstructionPage = () => {
  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight text-foreground">
          指令服务会话
        </h2>
        <p className="mt-1 max-w-3xl text-sm leading-relaxed text-muted">
          808 指令通道上的活跃会话列表；支持分页、链路监控与删除。
        </p>
      </div>
      <SessionTable type="instruction" />
    </div>
  );
};
