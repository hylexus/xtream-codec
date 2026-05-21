import { Chip, Tooltip } from "@heroui/react";
import { FC, Key, useState } from "react";

import { SessionMonitor } from "./monitor.tsx";

import { DataTable } from "@/components/ui/data-table.tsx";
import { usePageList } from "@/hooks/use-page-list.ts";
import { Session, SessionType } from "@/types";
import { request } from "@/utils/request.ts";
import { LuEyeIcon, LuTrashIcon } from "@/components/icons.tsx";

const ServerMap = {
  INSTRUCTION_SERVER: "808 服务",
  ATTACHMENT_SERVER: "附件服务",
};

const TABLE_LABEL: Record<SessionType, string> = {
  instruction: "指令服务在线会话",
  attachment: "附件服务在线会话",
};

interface CellProps {
  handleMonitor: (session: Session) => void;
  handleDel: (session: Session) => void;
  session: Session;
  columnKey: Key;
}

const SessionCell: FC<CellProps> = ({
  handleMonitor,
  handleDel,
  session,
  columnKey,
}) => {
  const cellValue = session[columnKey as keyof Session];

  switch (columnKey) {
    case "id":
      return (
        <Tooltip>
          <Tooltip.Trigger>
            <span className="line-clamp-1 font-mono text-sm text-muted">
              {String(cellValue)}
            </span>
          </Tooltip.Trigger>
          <Tooltip.Content>{String(cellValue)}</Tooltip.Content>
        </Tooltip>
      );
    case "serverType":
      return (
        <span className="text-sm text-foreground">
          {ServerMap[cellValue as keyof typeof ServerMap]}
        </span>
      );
    case "protocolVersion":
      return (
        <span className="text-sm text-foreground">
          {String(cellValue).replace("VERSION_", "")}
        </span>
      );
    case "protocolType":
      return (
        <Chip color="accent" size="sm" variant="soft">
          {String(cellValue)}
        </Chip>
      );
    case "operation":
      return (
        <div className="flex items-center justify-end gap-2">
          <Tooltip>
            <Tooltip.Trigger>
              <span
                className="inline-flex cursor-pointer text-muted transition-colors hover:text-foreground"
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
                <LuEyeIcon />
              </span>
            </Tooltip.Trigger>
            <Tooltip.Content>链路监控</Tooltip.Content>
          </Tooltip>
          <Tooltip>
            <Tooltip.Trigger>
              <span
                className="inline-flex cursor-pointer text-danger transition-opacity hover:opacity-80"
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
                <LuTrashIcon />
              </span>
            </Tooltip.Trigger>
            <Tooltip.Content>删除会话</Tooltip.Content>
          </Tooltip>
        </div>
      );
    default:
      return (
        <span className="line-clamp-1 text-sm text-foreground">
          {cellValue == null ? "—" : String(cellValue)}
        </span>
      );
  }
};

export interface SessionTableProps {
  type: SessionType;
}

export const SessionTable: FC<SessionTableProps> = ({ type }) => {
  const { setPage, page, pages, tableData, isLoading, mutate } = usePageList(
    `session/${type}-sessions`,
  );
  const [isOpen, setIsOpen] = useState(false);
  const [selectedRow, setSelectedRow] = useState<Session | null>(null);

  const columns = [
    { key: "id", label: "会话 ID", isRowHeader: true },
    { key: "terminalId", label: "终端" },
    { key: "protocolVersion", label: "协议版本" },
    { key: "protocolType", label: "协议" },
    { key: "creationTime", label: "创建时间" },
    { key: "lastCommunicateTime", label: "最后通信" },
    { key: "operation", label: "操作" },
  ];

  const handleMonitor = (item: Session) => {
    setSelectedRow(item);
    setIsOpen(true);
  };

  const handleDel = async (session: Session) => {
    try {
      const res = await request<{ closed?: boolean }>({
        path: `session/${type}-session/${session.id}`,
        method: "DELETE",
      });

      if (res.closed) {
        await mutate();
      }
    } catch {
      /* 保持原状 */
    }
  };

  const items = tableData?.data ?? [];
  const total = tableData?.total ?? 0;
  const loading = isLoading && items.length === 0;

  return (
    <>
      <DataTable
        ariaLabel={TABLE_LABEL[type]}
        columns={columns}
        items={items}
        label={TABLE_LABEL[type]}
        loading={loading}
        page={page}
        pages={pages}
        renderCell={(item, columnKey) => (
          <SessionCell
            columnKey={columnKey}
            handleDel={handleDel}
            handleMonitor={handleMonitor}
            session={item}
          />
        )}
        searchPlaceholder="搜索会话..."
        total={total}
        onPageChange={setPage}
      />
      <SessionMonitor isOpen={isOpen} row={selectedRow} setIsOpen={setIsOpen} />
    </>
  );
};
