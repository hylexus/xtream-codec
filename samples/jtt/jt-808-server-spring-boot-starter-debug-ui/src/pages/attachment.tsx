import SessionTable from "@/components/session-table.tsx";

export default function AttachmentPage() {
  const path = `${import.meta.env.VITE_API_DASHBOARD_V1}session/attachment-session/list`;

  return (
    <section className="flex flex-col items-center justify-center gap-4 py-8 md:py-10">
      <SessionTable path={path} />
    </section>
  );
}
