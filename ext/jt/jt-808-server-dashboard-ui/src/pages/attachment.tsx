import { PageSection } from "@/components/page-header.tsx";
import { SessionTable } from "@/components/sessions/table.tsx";

export const AttachmentPage = () => {
  return (
    <PageSection>
      <SessionTable type="attachment" />
    </PageSection>
  );
};
