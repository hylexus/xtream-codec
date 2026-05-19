import { DumpGroup } from "@/components/dump/group.tsx";
import { PageSection } from "@/components/page-header.tsx";

export const DumpPage = () => {
  return (
    <PageSection description="线程实时状态" title="线程 Dump">
      <DumpGroup />
    </PageSection>
  );
};
