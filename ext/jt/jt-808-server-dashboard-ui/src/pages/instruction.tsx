import { PageSection } from "@/components/page-header.tsx";
import { SessionTable } from "@/components/sessions/table.tsx";

export const InstructionPage = () => {
  return (
    <PageSection
      description="808 指令通道上的活跃会话列表"
      title="指令服务在线会话"
    >
      <SessionTable type="instruction" />
    </PageSection>
  );
};
