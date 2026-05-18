import { Spinner, Table, Tooltip } from "@heroui/react";
import React, { FC, useMemo } from "react";

import { PagePagination } from "@/components/page-pagination.tsx";
import { usePageList } from "@/hooks/use-page-list.ts";
import { Dic, Jt1078Subscriber } from "@/types";

interface CellProps {
  item: Dic;
  columnKey: React.Key;
}

const RenderCell: FC<CellProps> = ({ item, columnKey }) => {
  const cellValue = item[columnKey as keyof Dic];

  switch (columnKey) {
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
    default:
      if (cellValue == null) {
        return "—";
      }

      return String(cellValue);
  }
};

const pageIntro = (
  <div>
    <h2 className="text-2xl font-semibold tracking-tight text-foreground">
      数据订阅
    </h2>
    <p className="mt-1 max-w-3xl text-sm leading-relaxed text-muted">
      流媒体通道上的订阅方列表及元数据。
    </p>
  </div>
);

export const SubscribersPage = () => {
  const path = "subscribers";
  const { setPage, page, pages, tableData, isLoading } =
    usePageList<Jt1078Subscriber>(path, 10);

  const loadingState =
    isLoading && (tableData?.data?.length ?? 0) === 0 ? "loading" : "idle";

  const columns = [
    { key: "id", label: "ID", width: "18%" },
    { key: "convertedSim", label: "SIM", width: "12%" },
    { key: "channel", label: "通道", width: "8%" },
    { key: "convertedAudioType", label: "音频", width: "12%" },
    { key: "rawVideoType", label: "视频", width: "12%" },
    { key: "createdAt", label: "创建时间", width: "18%" },
    { key: "metadata", label: "元数据", width: "20%" },
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

  if (loadingState === "loading") {
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
            <Table.Content aria-label="Subscribers">
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
                        <RenderCell
                          columnKey={column.key}
                          item={item as unknown as Dic}
                        />
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
