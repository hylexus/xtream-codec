import { Tooltip } from "@heroui/react";
import { Key } from "react";

import { PageSection } from "@/components/page-header.tsx";
import { DataTable } from "@/components/ui/data-table.tsx";
import { LoadingPanel } from "@/components/ui/loading-panel.tsx";
import { usePageList } from "@/hooks/use-page-list.ts";
import { Jt1078Session } from "@/types";

const PAGE_TITLE = "媒体会话";
const PAGE_DESCRIPTION = "当前 JT1078 流媒体连接会话列表";

const columns = [
  { key: "id", label: "会话 ID", isRowHeader: true },
  { key: "convertedSim", label: "SIM" },
  { key: "protocolType", label: "协议" },
  { key: "audioType", label: "音频" },
  { key: "videoType", label: "视频" },
  { key: "creationTime", label: "创建时间" },
  { key: "lastCommunicateTime", label: "最后通信" },
] as const;

function renderSessionCell(item: Jt1078Session, columnKey: Key) {
  const cellValue = item[columnKey as keyof Jt1078Session];

  if (cellValue == null) {
    return <span className="dashboard-table-cell-text">—</span>;
  }

  const text = String(cellValue);

  return (
    <Tooltip>
      <Tooltip.Trigger>
        <p className="dashboard-table-cell-text">{text}</p>
      </Tooltip.Trigger>
      <Tooltip.Content>{text}</Tooltip.Content>
    </Tooltip>
  );
}

export const SessionsPage = () => {
  const path = "sessions";
  const { setPage, page, pages, tableData, isLoading } =
    usePageList<Jt1078Session>(path, 10);

  const loading =
    isLoading && (tableData?.data?.length ?? 0) === 0;
  const items = tableData?.data ?? [];
  const total = tableData?.total ?? 0;

  if (loading) {
    return (
      <PageSection description={PAGE_DESCRIPTION} title={PAGE_TITLE}>
        <LoadingPanel />
      </PageSection>
    );
  }

  return (
    <PageSection description={PAGE_DESCRIPTION} title={PAGE_TITLE}>
      <DataTable
        ariaLabel="Sessions"
        columns={columns.map((c) => ({ ...c }))}
        items={items}
        label="全部会话"
        page={page}
        pages={pages}
        renderCell={renderSessionCell}
        searchPlaceholder="搜索会话..."
        total={total}
        onPageChange={setPage}
      />
    </PageSection>
  );
};
