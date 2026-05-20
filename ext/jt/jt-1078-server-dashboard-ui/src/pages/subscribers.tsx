import { Tooltip } from "@heroui/react";
import { Key } from "react";

import { PageSection } from "@/components/page-header.tsx";
import { DataTable } from "@/components/ui/data-table.tsx";
import { LoadingPanel } from "@/components/ui/loading-panel.tsx";
import { usePageList } from "@/hooks/use-page-list.ts";
import { Jt1078Subscriber } from "@/types";

const PAGE_TITLE = "数据订阅";
const PAGE_DESCRIPTION = "流媒体通道上的订阅方列表及元数据";

const columns = [
  { key: "id", label: "ID", isRowHeader: true },
  { key: "convertedSim", label: "SIM" },
  { key: "channel", label: "通道" },
  { key: "convertedAudioType", label: "音频" },
  { key: "rawVideoType", label: "视频" },
  { key: "createdAt", label: "创建时间" },
  { key: "metadata", label: "元数据" },
] as const;

function renderSubscriberCell(item: Jt1078Subscriber, columnKey: Key) {
  const cellValue = item[columnKey as keyof Jt1078Subscriber];

  if (columnKey === "metadata") {
    return (
      <Tooltip>
        <Tooltip.Trigger>
          <p className="dashboard-table-cell-text">
            {JSON.stringify(cellValue)}
          </p>
        </Tooltip.Trigger>
        <Tooltip.Content>
          <pre className="max-w-md whitespace-pre-wrap text-xs">
            {JSON.stringify(cellValue, null, 2)}
          </pre>
        </Tooltip.Content>
      </Tooltip>
    );
  }

  if (cellValue == null) {
    return <span className="dashboard-table-cell-text">—</span>;
  }

  return (
    <span className="dashboard-table-cell-text">{String(cellValue)}</span>
  );
}

export const SubscribersPage = () => {
  const path = "subscribers";
  const { setPage, page, pages, tableData, isLoading } =
    usePageList<Jt1078Subscriber>(path, 10);

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
        ariaLabel="Subscribers"
        columns={columns.map((c) => ({ ...c }))}
        items={items}
        label="全部订阅"
        page={page}
        pages={pages}
        renderCell={renderSubscriberCell}
        searchPlaceholder="搜索订阅..."
        total={total}
        onPageChange={setPage}
      />
    </PageSection>
  );
};
