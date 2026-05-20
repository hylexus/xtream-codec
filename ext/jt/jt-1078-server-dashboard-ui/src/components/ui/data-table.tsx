import { Input, Table } from "@heroui/react";
import { Search } from "lucide-react";
import { Key, ReactNode } from "react";

import {
  DashboardCard,
  DashboardCardBody,
  DashboardCardFooter,
  DashboardCardHeader,
} from "@/components/ui/dashboard-card.tsx";
import { ListTableToolbar } from "@/components/ui/list-table-toolbar.tsx";
import { PagePagination } from "@/components/page-pagination.tsx";

export type DataTableColumn<T> = {
  key: keyof T | string;
  label: string;
  isRowHeader?: boolean;
};

type DataTableProps<T extends { id?: string }> = {
  label: string;
  total: number;
  columns: DataTableColumn<T>[];
  items: T[];
  ariaLabel: string;
  page: number;
  pages: number;
  onPageChange: (page: number) => void;
  renderCell: (item: T, columnKey: Key) => ReactNode;
  searchPlaceholder?: string;
};

export function DataTable<T extends { id?: string }>({
  label,
  total,
  columns,
  items,
  ariaLabel,
  page,
  pages,
  onPageChange,
  renderCell,
  searchPlaceholder = "搜索...",
}: DataTableProps<T>) {
  return (
    <DashboardCard>
      <DashboardCardHeader
        actions={
          <div className="flex flex-wrap items-center gap-2">
            <ListTableToolbar />
            <div className="relative w-48 min-w-[10rem]">
              <Search
                className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted"
                strokeWidth={1.75}
              />
              <Input
                aria-label="搜索"
                className="pl-9"
                placeholder={searchPlaceholder}
                variant="secondary"
              />
            </div>
          </div>
        }
        subtitle={
          <span className="tabular-nums font-semibold text-foreground">
            {total}
          </span>
        }
        title={label}
      />
      <DashboardCardBody flush>
        <Table.Root className="w-full">
          <Table.ScrollContainer className="dashboard-table-scroll">
            <Table.Content aria-label={ariaLabel}>
              <Table.Header>
                {columns.map((column) => (
                  <Table.Column
                    key={String(column.key)}
                    isRowHeader={column.isRowHeader}
                  >
                    {column.label}
                  </Table.Column>
                ))}
              </Table.Header>
              <Table.Body items={items}>
                {(item) => (
                  <Table.Row id={String(item?.id ?? "")}>
                    {columns.map((column) => (
                      <Table.Cell key={String(column.key)}>
                        {renderCell(item, column.key as Key)}
                      </Table.Cell>
                    ))}
                  </Table.Row>
                )}
              </Table.Body>
            </Table.Content>
          </Table.ScrollContainer>
        </Table.Root>
      </DashboardCardBody>
      <DashboardCardFooter>
        <p className="text-sm text-muted">共 {total} 条</p>
        {pages > 0 ? (
          <PagePagination page={page} total={pages} onChange={onPageChange} />
        ) : null}
      </DashboardCardFooter>
    </DashboardCard>
  );
}
