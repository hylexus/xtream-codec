import { Spinner, Table, Tooltip } from "@heroui/react";
import React, { FC, useMemo } from "react";

import { PagePagination } from "@/components/page-pagination.tsx";
import { Dic } from "@/types";
import { usePageList } from "@/hooks/use-page-list.ts";

interface CellProps {
  item: Dic;
  columnKey: React.Key;
}

const RenderCell: FC<CellProps> = ({ item, columnKey }) => {
  const cellValue = item[columnKey as keyof Dic];

  switch (columnKey) {
    case "interestedEvents":
    case "metadata":
      return (
        <Tooltip>
          <Tooltip.Trigger>
            <p className="line-clamp-1">{JSON.stringify(cellValue)}</p>
          </Tooltip.Trigger>
          <Tooltip.Content>
            <pre className="max-w-md whitespace-pre-wrap text-xs">
              {JSON.stringify(cellValue, null, 2)}
            </pre>
          </Tooltip.Content>
        </Tooltip>
      );
    case "createdAt":
      return String(cellValue).slice(0, -4);
    default:
      return cellValue;
  }
};

const pageIntro = (
  <div>
    <h2 className="text-2xl font-semibold tracking-tight text-foreground">
      事件订阅者
    </h2>
    <p className="mt-1 max-w-3xl text-sm leading-relaxed text-muted">
      事件发布器上的订阅方列表及元数据。
    </p>
  </div>
);

export const SubscribePage = () => {
  const path = "event-publisher/subscribers";
  const { setPage, page, pages, tableData, isLoading } = usePageList(path, 10);

  const loadingState =
    isLoading && tableData?.data?.length === 0 ? "loading" : "idle";

  const columns = [
    { key: "id", label: "ID", width: "20%" },
    { key: "interestedEvents", label: "订阅事件", width: "30%" },
    { key: "metadata", label: "元数据", width: "30%" },
    { key: "createdAt", label: "创建时间", width: "20%" },
  ];

  const bottomContent = useMemo(() => {
    return (
      pages > 0 && (
        <div className="flex w-full justify-center py-4">
          <PagePagination page={page} total={pages} onChange={setPage} />
        </div>
      )
    );
  }, [page, pages, setPage]);

  const topContent = useMemo(() => {
    return (
      <p className="text-sm text-muted">总数：{tableData?.total ?? "—"}</p>
    );
  }, [tableData?.total]);

  const items = tableData?.data ?? [];

  if (loadingState === "loading" && items.length === 0) {
    return (
      <div className="flex flex-col gap-6">
        {pageIntro}
        <div className="flex flex-col gap-4">
          {topContent}
          <div className="flex justify-center p-12">
            <Spinner />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6">
      {pageIntro}
      <div className="flex flex-col gap-2">
        {topContent}
        <Table.Root className="w-full">
          <Table.ScrollContainer className="max-h-[80vh]">
            <Table.Content aria-label="Subscribe">
              <Table.Header>
                {columns.map((column) => (
                  <Table.Column
                    key={String(column.key)}
                    isRowHeader={column.key === "id"}
                  >
                    {column.label}
                  </Table.Column>
                ))}
              </Table.Header>
              <Table.Body items={items}>
                {(item) => (
                  <Table.Row id={String(item?.id)}>
                    {columns.map((column) => (
                      <Table.Cell key={String(column.key)}>
                        <RenderCell columnKey={column.key} item={item} />
                      </Table.Cell>
                    ))}
                  </Table.Row>
                )}
              </Table.Body>
            </Table.Content>
          </Table.ScrollContainer>
        </Table.Root>
        {bottomContent}
      </div>
    </div>
  );
};
