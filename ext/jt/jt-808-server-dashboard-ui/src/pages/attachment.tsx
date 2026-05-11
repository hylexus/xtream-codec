import { SessionTable } from "@/components/sessions/table.tsx";

export const AttachmentPage = () => {
  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight text-foreground">
          附件服务会话
        </h2>
        <p className="mt-1 max-w-3xl text-sm leading-relaxed text-muted">
          附件上传 / 下载等通道上的活跃会话列表。
        </p>
      </div>
      <SessionTable type="attachment" />
    </div>
  );
};
