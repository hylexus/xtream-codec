import { Tooltip } from "@heroui/react";
import { FC, Key } from "react";

import { PageSection } from "@/components/page-header.tsx";
import { DataTable } from "@/components/ui/data-table.tsx";
import { Dic } from "@/types";
import { usePageList } from "@/hooks/use-page-list.ts";

interface CellProps {
  item: Dic;
  columnKey: Key;
}

const RenderCell: FC<CellProps> = ({ item, columnKey }) => {
  const cellValue = item[columnKey as keyof Dic];

  switch (columnKey) {
    case "interestedEvents":
    case "metadata":
      return (
        <Tooltip>
          <Tooltip.Trigger>
            <p className="line-clamp-1 text-sm text-foreground">
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
    case "createdAt":
      return (
        <span className="text-sm text-foreground">
          {String(cellValue).slice(0, -4)}
        </span>
      );
    default:
      return (
        <span className="line-clamp-1 text-sm text-foreground">
          {cellValue == null ? "—" : String(cellValue)}
        </span>
      );
  }
};

export const SubscribePage = () => {
  const path = "event-publisher/subscribers";
  const { setPage, page, pages, tableData, isLoading } = usePageList(path, 10);

  const columns = [
    { key: "id", label: "ID", isRowHeader: true },
    { key: "interestedEvents", label: "订阅事件" },
    { key: "metadata", label: "元数据" },
    { key: "createdAt", label: "创建时间" },
  ];

  const items = tableData?.data ?? [];
  const total = tableData?.total ?? 0;
  const loading = isLoading && items.length === 0;

  return (
    <PageSection>
      <DataTable
        ariaLabel="Subscribers"
        columns={columns}
        items={items}
        label="全部订阅"
        loading={loading}
        page={page}
        pages={pages}
        renderCell={(item, columnKey) => (
          <RenderCell columnKey={columnKey} item={item} />
        )}
        searchPlaceholder="搜索订阅..."
        total={total}
        onPageChange={setPage}
      />
    </PageSection>
  );
};
