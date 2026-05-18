import { DumpGroup } from "@/components/dump/group.tsx";
import { PageSection } from "@/components/page-header.tsx";

export const DumpPage = () => {
  return (
    <PageSection
      description="按分组与线程名查看线程状态时间轴与栈信息；时间轴刻度颜色随主题变化。"
      title="线程 Dump"
    >
      <DumpGroup />
    </PageSection>
  );
};
