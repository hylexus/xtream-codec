import { Spinner, Table, Tooltip } from "@heroui/react";
import React, { FC, useMemo } from "react";

import { PagePagination } from "@/components/page-pagination.tsx";
import { usePageList } from "@/hooks/use-page-list.ts";
import { Dic, Jt1078Session } from "@/types";

interface CellProps {
  item: Dic;
  columnKey: React.Key;
}

const RenderCell: FC<CellProps> = ({ item, columnKey }) => {
  const cellValue = item[columnKey as keyof Dic];

  if (cellValue == null) {
    return "—";
  }

  return String(cellValue);
};

const pageIntro = (
  <div>
    <h2 className="text-2xl font-semibold tracking-tight text-foreground">
      媒体会话
    </h2>
    <p className="mt-1 max-w-3xl text-sm leading-relaxed text-muted">
      当前 JT1078 流媒体连接会话列表。
    </p>
  </div>
);

export const SessionsPage = () => {
  const path = "sessions";
  const { setPage, page, pages, tableData, isLoading } =
    usePageList<Jt1078Session>(path, 10);

  const loadingState =
    isLoading && (tableData?.data?.length ?? 0) === 0 ? "loading" : "idle";

  const columns = [
    { key: "id", label: "会话 ID", width: "18%" },
    { key: "convertedSim", label: "SIM", width: "12%" },
    { key: "protocolType", label: "协议", width: "10%" },
    { key: "audioType", label: "音频", width: "10%" },
    { key: "videoType", label: "视频", width: "10%" },
    { key: "creationTime", label: "创建时间", width: "20%" },
    { key: "lastCommunicateTime", label: "最后通信", width: "20%" },
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
            <Table.Content aria-label="Sessions">
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
                        <Tooltip>
                          <Tooltip.Trigger>
                            <p className="line-clamp-1">
                              <RenderCell
                                columnKey={column.key}
                                item={item as unknown as Dic}
                              />
                            </p>
                          </Tooltip.Trigger>
                          <Tooltip.Content>
                            <RenderCell
                              columnKey={column.key}
                              item={item as unknown as Dic}
                            />
                          </Tooltip.Content>
                        </Tooltip>
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
