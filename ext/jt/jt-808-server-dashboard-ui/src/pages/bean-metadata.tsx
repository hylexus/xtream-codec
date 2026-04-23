import {
  Table,
  TableBody,
  TableCell,
  TableColumn,
  TableHeader,
  TableRow,
} from "@heroui/table";
import { Spinner } from "@heroui/spinner";
import { Pagination } from "@heroui/pagination";
import React, { FC, useMemo } from "react";

import { Dic } from "@/types";
import { usePageList } from "@/hooks/use-page-list.ts";

const columns = [
  { key: "rawClass", label: "类名" },
  { key: "constructor", label: "构造函数" },
  { key: "properties", label: "属性数" },
];

const RenderCell: FC<{ item: Dic; columnKey: React.Key }> = ({
  item,
  columnKey,
}) => {
  switch (columnKey) {
    case "properties":
      return item.properties?.length ?? 0;
    default:
      return item[columnKey as keyof Dic];
  }
};

export const BeanMetadataPage = () => {
  const { setPage, page, pages, tableData, isLoading } = usePageList<Dic>(
    "codec/bean-metadata",
    10,
  );

  const loadingState =
    isLoading && tableData?.data?.length === 0 ? "loading" : "idle";

  const bottomContent = useMemo(() => {
    return (
      <div className="flex w-full items-center justify-center gap-3">
        <p className="text-small text-default-400">
          总数：{tableData?.total ?? 0}
        </p>
        {pages > 0 && (
          <Pagination
            isCompact
            showControls
            showShadow
            color="secondary"
            page={page}
            total={pages}
            onChange={(page) => setPage(page)}
          />
        )}
      </div>
    );
  }, [page, pages, tableData?.total]);

  return (
    <Table
      isHeaderSticky
      aria-label="Bean Metadata"
      bottomContent={bottomContent}
      bottomContentPlacement="outside"
      classNames={{
        wrapper: "max-h-[80vh]",
        td: "whitespace-nowrap",
      }}
    >
      <TableHeader columns={columns}>
        {(column) => <TableColumn key={column.key}>{column.label}</TableColumn>}
      </TableHeader>
      <TableBody
        emptyContent={"暂无数据"}
        items={tableData?.data ?? []}
        loadingContent={<Spinner />}
        loadingState={loadingState}
      >
        {(item) => (
          <TableRow key={item.rawClass}>
            {(columnKey) => (
              <TableCell>
                <RenderCell columnKey={columnKey} item={item} />
              </TableCell>
            )}
          </TableRow>
        )}
      </TableBody>
    </Table>
  );
};
