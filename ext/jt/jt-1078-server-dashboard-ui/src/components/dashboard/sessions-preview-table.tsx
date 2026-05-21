import { Avatar, Button, Chip, Table } from "@heroui/react";
import { MoreHorizontal } from "lucide-react";
import { Link } from "react-router-dom";

import { LoadingPanel } from "@/components/ui/loading-panel.tsx";
import { TableCard } from "@/components/ui/table-card.tsx";
import { Jt1078Session } from "@/types";

function simInitials(sim: string): string {
  const trimmed = sim.replace(/\s/g, "");

  if (trimmed.length >= 2) {
    return trimmed.slice(-2).toUpperCase();
  }

  return trimmed.slice(0, 2).toUpperCase() || "—";
}

function mediaTypeLabel(session: Jt1078Session): string {
  if (session.videoType && session.audioType) {
    return "音视频";
  }
  if (session.videoType) {
    return "视频";
  }
  if (session.audioType) {
    return "音频";
  }

  return "媒体";
}

type SessionsPreviewTableProps = {
  items: Jt1078Session[];
  total: number;
  loading?: boolean;
};

export function SessionsPreviewTable({
  items,
  total,
  loading,
}: SessionsPreviewTableProps) {
  return (
    <TableCard
      searchPlaceholder="搜索..."
      title="全部会话"
      total={total}
      footer={
        <>
          <p className="text-sm text-muted">
            显示 {items.length} / {total} 条
          </p>
          <Link
            className="text-sm font-medium text-accent underline-offset-4 hover:underline"
            to="/sessions"
          >
            查看全部
          </Link>
        </>
      }
    >
      {loading ? (
        <LoadingPanel />
      ) : (
        <Table.Root className="w-full">
          <Table.ScrollContainer>
            <Table.Content aria-label="最近会话">
              <Table.Header>
                <Table.Column isRowHeader>会话 ID</Table.Column>
                <Table.Column>终端</Table.Column>
                <Table.Column>协议</Table.Column>
                <Table.Column>类型</Table.Column>
                <Table.Column className="text-end">操作</Table.Column>
              </Table.Header>
              <Table.Body items={items}>
                {(session) => (
                  <Table.Row id={session.id}>
                    <Table.Cell>
                      <span className="font-mono text-sm text-muted">
                        #{session.id.slice(0, 8)}
                      </span>
                    </Table.Cell>
                    <Table.Cell>
                      <div className="flex min-w-0 items-center gap-3">
                        <Avatar className="size-9 shrink-0">
                          <Avatar.Fallback className="text-xs font-semibold">
                            {simInitials(session.convertedSim)}
                          </Avatar.Fallback>
                        </Avatar>
                        <div className="min-w-0">
                          <p className="truncate text-sm font-medium text-foreground">
                            {session.convertedSim}
                          </p>
                          <p className="truncate text-xs text-muted">
                            {session.rawSim}
                          </p>
                        </div>
                      </div>
                    </Table.Cell>
                    <Table.Cell>
                      <Chip size="sm" variant="soft">
                        {session.protocolType || "—"}
                      </Chip>
                    </Table.Cell>
                    <Table.Cell>
                      <Chip size="sm" variant="secondary">
                        {mediaTypeLabel(session)}
                      </Chip>
                    </Table.Cell>
                    <Table.Cell className="text-end">
                      <Button
                        isIconOnly
                        aria-label="更多操作"
                        size="sm"
                        variant="ghost"
                      >
                        <MoreHorizontal
                          className="size-4 text-muted"
                          strokeWidth={1.75}
                        />
                      </Button>
                    </Table.Cell>
                  </Table.Row>
                )}
              </Table.Body>
            </Table.Content>
          </Table.ScrollContainer>
        </Table.Root>
      )}
    </TableCard>
  );
}
