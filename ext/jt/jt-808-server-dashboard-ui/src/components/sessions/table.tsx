import { Chip, Spinner, Table, Tooltip } from "@heroui/react";
import React, { FC, useMemo, useState } from "react";

import { SessionMonitor } from "./monitor.tsx";

import { PagePagination } from "@/components/page-pagination.tsx";
import { usePageList } from "@/hooks/use-page-list.ts";
import { Session, SessionType } from "@/types";
import { request } from "@/utils/request.ts";
import { FaEyeIcon, FaTrashIcon } from "@/components/icons.tsx";

interface CellProps {
  handleMonitor: Function;
  handleDel: Function;
  session: Session;
  columnKey: React.Key;
}
const ServerMap = {
  INSTRUCTION_SERVER: "808 服务",
  ATTACHMENT_SERVER: "附件服务",
};

const SessionCell: FC<CellProps> = ({
  handleMonitor,
  handleDel,
  session,
  columnKey,
}) => {
  const cellValue = session[columnKey as keyof Session];

  switch (columnKey) {
    case "serverType":
      return ServerMap[cellValue as keyof typeof ServerMap];
    case "protocolVersion":
      return String(cellValue).replace("VERSION_", "");
    case "protocolType":
      return (
        <Chip color="accent" size="sm">
          {cellValue}
        </Chip>
      );
    case "operation":
      return (
        <div className="relative flex items-center gap-2">
          <Tooltip>
            <Tooltip.Trigger>
              <span
                className="inline-flex cursor-pointer text-lg text-default-400 active:opacity-50"
                role="button"
                tabIndex={0}
                onClick={() => handleMonitor(session)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault();
                    handleMonitor(session);
                  }
                }}
              >
                <FaEyeIcon />
              </span>
            </Tooltip.Trigger>
            <Tooltip.Content>链路监控</Tooltip.Content>
          </Tooltip>
          <Tooltip>
            <Tooltip.Trigger>
              <span
                className="inline-flex cursor-pointer text-lg text-danger active:opacity-50"
                role="button"
                tabIndex={0}
                onClick={() => handleDel(session)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault();
                    handleDel(session);
                  }
                }}
              >
                <FaTrashIcon />
              </span>
            </Tooltip.Trigger>
            <Tooltip.Content>删除会话</Tooltip.Content>
          </Tooltip>
        </div>
      );
    default:
      return cellValue;
  }
};

export interface SessionTableProps {
  type: SessionType;
}
export const SessionTable: FC<SessionTableProps> = ({ type }) => {
  const { setPage, page, pages, tableData, isLoading, mutate } = usePageList(
    `session/${type}-sessions`,
  );
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [selectedRow, setSelectedRow] = useState<Session | null>(null);

  const loadingState =
    isLoading && tableData?.data?.length === 0 ? "loading" : "idle";

  const columns = [
    { key: "id", label: "会话ID" },
    { key: "terminalId", label: "终端手机号" },
    { key: "protocolVersion", label: "808协议版本" },
    { key: "protocolType", label: "协议" },
    { key: "creationTime", label: "创建时间" },
    { key: "lastCommunicateTime", label: "最近一次通信时间" },
    { key: "operation", label: "操作" },
  ];

  const handleMonitor = (item: Session) => {
    setSelectedRow(item);
    setIsOpen(true);
  };
  const handleDel = async (session: Session) => {
    try {
      const res: any = await request({
        path: `session/${type}-session/${session.id}`,
        method: "DELETE",
      });

      if (res.closed) {
        await mutate();
      }
    } catch (_e) {
      console.error(_e);
    }
  };
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
    return <p>总数： {tableData?.total}</p>;
  }, [tableData?.total]);

  const items = tableData?.data ?? [];

  if (loadingState === "loading" && items.length === 0) {
    return (
      <>
        <div className="flex flex-col gap-4">
          {topContent}
          <div className="flex justify-center p-12">
            <Spinner />
          </div>
        </div>
        <SessionMonitor
          isOpen={isOpen}
          row={selectedRow}
          setIsOpen={setIsOpen}
        />
      </>
    );
  }

  return (
    <>
      <div className="flex flex-col gap-2">
        {topContent}
        <Table.Root className="w-full">
          <Table.ScrollContainer>
            <Table.Content aria-label="Example table with dynamic content">
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
                        <SessionCell
                          columnKey={column.key}
                          handleDel={handleDel}
                          handleMonitor={handleMonitor}
                          session={item}
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
      <SessionMonitor isOpen={isOpen} row={selectedRow} setIsOpen={setIsOpen} />
    </>
  );
};
