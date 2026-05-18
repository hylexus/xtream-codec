import { PageSection } from "@/components/page-header.tsx";
import { SessionTable } from "@/components/sessions/table.tsx";

export const AttachmentPage = () => {
  return (
    <PageSection
      description="附件上传 / 下载等通道上的活跃会话列表。"
      title="附件服务会话"
    >
      <SessionTable type="attachment" />
    </PageSection>
  );
};
