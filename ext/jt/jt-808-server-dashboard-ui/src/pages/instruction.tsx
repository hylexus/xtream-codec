import { PageSection } from "@/components/page-header.tsx";
import { SessionTable } from "@/components/sessions/table.tsx";

export const InstructionPage = () => {
  return (
    <PageSection>
      <SessionTable type="instruction" />
    </PageSection>
  );
};
