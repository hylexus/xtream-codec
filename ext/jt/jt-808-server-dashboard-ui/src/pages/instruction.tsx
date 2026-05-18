import { PageSection } from "@/components/page-header.tsx";
import { SessionTable } from "@/components/sessions/table.tsx";

export const InstructionPage = () => {
  return (
    <PageSection
      description="808 指令通道上的活跃会话列表；支持分页、链路监控与删除。"
      title="指令服务会话"
    >
      <SessionTable type="instruction" />
    </PageSection>
  );
};
