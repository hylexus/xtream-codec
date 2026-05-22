import { Table } from "@heroui/react";
import { Key, ReactNode } from "react";

import { PagePagination } from "@/components/page-pagination.tsx";
import { LoadingPanel } from "@/components/ui/loading-panel.tsx";
import { TableCard } from "@/components/ui/table-card.tsx";

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
  loading?: boolean;
  toolbar?: ReactNode;
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
  searchPlaceholder,
  loading,
  toolbar,
}: DataTableProps<T>) {
  return (
    <TableCard
      footer={
        <>
          <p className="text-sm text-muted">共 {total} 条</p>
          {pages > 0 ? (
            <PagePagination page={page} total={pages} onChange={onPageChange} />
          ) : null}
        </>
      }
      searchPlaceholder={searchPlaceholder}
      title={label}
      toolbar={toolbar}
      total={total}
    >
      {loading && items.length === 0 ? (
        <LoadingPanel />
      ) : (
        <Table.Root className="w-full">
          <Table.ScrollContainer className="max-h-[70vh]">
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
      )}
    </TableCard>
  );
}
