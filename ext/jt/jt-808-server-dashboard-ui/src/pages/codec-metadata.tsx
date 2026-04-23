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
import React, { FC, useCallback, useMemo, useState } from "react";
import { Chip } from "@heroui/chip";
import { Input } from "@heroui/input";
import { Button } from "@heroui/button";
import { Tooltip } from "@heroui/tooltip";

import { Dic } from "@/types";
import { usePageList } from "@/hooks/use-page-list.ts";

const columns = [
  { key: "key", label: "key" },
  { key: "rawClassName", label: "实现类" },
  { key: "tags", label: "备注" },
];

const signednessOptions = [
  { key: "", label: "全部" },
  { key: "SIGNED", label: "有符号" },
  { key: "UNSIGNED", label: "无符号" },
];

const endianOptions = [
  { key: "", label: "全部" },
  { key: "BIG_ENDIAN", label: "大端" },
  { key: "LITTLE_ENDIAN", label: "小端" },
];

const builtinOptions = [
  { key: "", label: "全部" },
  { key: "true", label: "内置" },
  { key: "false", label: "自定义" },
];

const signednessMap: Record<
  string,
  { label: string; color: "success" | "warning" }
> = {
  SIGNED: { label: "有符号", color: "success" },
  UNSIGNED: { label: "无符号", color: "warning" },
};

const endianMap: Record<
  string,
  { label: string; color: "secondary" | "warning" }
> = {
  BIG_ENDIAN: { label: "大端", color: "secondary" },
  LITTLE_ENDIAN: { label: "小端", color: "warning" },
};

const RenderCell: FC<{ item: Dic; columnKey: React.Key }> = ({
  item,
  columnKey,
}) => {
  switch (columnKey) {
    case "key":
      return <span>{item.key}</span>;
    case "rawClassName": {
      const fullName: string = item.rawClassName ?? "";
      const simpleName = fullName.includes(".")
        ? fullName.slice(fullName.lastIndexOf(".") + 1)
        : fullName;

      return (
        <Tooltip
          content={<span className="text-xs font-mono">{fullName}</span>}
        >
          <span className="text-xs font-mono cursor-default">{simpleName}</span>
        </Tooltip>
      );
    }
    case "tags": {
      const tags: React.ReactNode[] = [];

      if (item.isBuiltin) {
        tags.push(
          <Chip key="builtin" color="success" size="sm" variant="flat">
            内置
          </Chip>,
        );
      }

      const sig = signednessMap[item.signedness];

      if (sig) {
        tags.push(
          <Chip key="sign" color={sig.color} size="sm" variant="flat">
            {sig.label}
          </Chip>,
        );
      }

      const end = endianMap[item.endian];

      if (end) {
        tags.push(
          <Chip key="endian" color={end.color} size="sm" variant="flat">
            {end.label}
          </Chip>,
        );
      }

      if (item.charset && item.charset !== "NONE") {
        tags.push(
          <Chip key="charset" color="primary" size="sm" variant="flat">
            {item.charset}
          </Chip>,
        );
      }

      return (
        <div className="flex gap-1 flex-nowrap overflow-hidden">{tags}</div>
      );
    }
    default:
      return item[columnKey as keyof Dic];
  }
};

export const CodecMetadataPage = () => {
  const [filterKey, setFilterKey] = useState("");
  const [filterClassName, setFilterClassName] = useState("");
  const [filterCharset, setFilterCharset] = useState("");
  const [filterSignedness, setFilterSignedness] = useState("");
  const [filterEndian, setFilterEndian] = useState("");
  const [filterBuiltin, setFilterBuiltin] = useState("");

  const extraParams = useMemo(() => {
    const params: Record<string, string> = {};

    if (filterKey) params.key = filterKey;
    if (filterClassName) params.className = filterClassName;
    if (filterCharset) params.charset = filterCharset;
    if (filterSignedness) params.signedness = filterSignedness;
    if (filterEndian) params.endian = filterEndian;
    if (filterBuiltin) params.builtin = filterBuiltin;

    return params;
  }, [
    filterKey,
    filterClassName,
    filterCharset,
    filterSignedness,
    filterEndian,
    filterBuiltin,
  ]);

  const { setPage, page, pages, tableData, isLoading } = usePageList<Dic>(
    "codec/codec-metadata",
    10,
    extraParams,
  );

  const loadingState =
    isLoading && tableData?.data?.length === 0 ? "loading" : "idle";

  const onFilterChange = useCallback(
    (setter: (v: string) => void) => (value: string) => {
      setter(value);
      setPage(1);
    },
    [setPage],
  );

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

  const topContent = useMemo(() => {
    return (
      <div className="flex flex-wrap gap-3 items-center">
        <Input
          className="w-40"
          placeholder="输入 key"
          size="sm"
          value={filterKey}
          onValueChange={onFilterChange(setFilterKey)}
        />
        <Input
          className="w-52"
          placeholder="输入实现类"
          size="sm"
          value={filterClassName}
          onValueChange={onFilterChange(setFilterClassName)}
        />
        <Input
          className="w-32"
          placeholder="输入字符集"
          size="sm"
          value={filterCharset}
          onValueChange={onFilterChange(setFilterCharset)}
        />
        <div className="flex items-center gap-1">
          <span className="text-xs text-default-500">符号:</span>
          <div className="flex">
            {signednessOptions.map((o) => (
              <Button
                key={o.key}
                color={filterSignedness === o.key ? "primary" : "default"}
                size="sm"
                variant={filterSignedness === o.key ? "flat" : "light"}
                onPress={() => onFilterChange(setFilterSignedness)(o.key)}
              >
                {o.label}
              </Button>
            ))}
          </div>
        </div>
        <div className="flex items-center gap-1">
          <span className="text-xs text-default-500">字节序:</span>
          <div className="flex">
            {endianOptions.map((o) => (
              <Button
                key={o.key}
                color={filterEndian === o.key ? "primary" : "default"}
                size="sm"
                variant={filterEndian === o.key ? "flat" : "light"}
                onPress={() => onFilterChange(setFilterEndian)(o.key)}
              >
                {o.label}
              </Button>
            ))}
          </div>
        </div>
        <div className="flex items-center gap-1">
          <span className="text-xs text-default-500">内置:</span>
          <div className="flex">
            {builtinOptions.map((o) => (
              <Button
                key={o.key}
                color={filterBuiltin === o.key ? "primary" : "default"}
                size="sm"
                variant={filterBuiltin === o.key ? "flat" : "light"}
                onPress={() => onFilterChange(setFilterBuiltin)(o.key)}
              >
                {o.label}
              </Button>
            ))}
          </div>
        </div>
      </div>
    );
  }, [
    filterKey,
    filterClassName,
    filterCharset,
    filterSignedness,
    filterEndian,
    filterBuiltin,
    onFilterChange,
  ]);

  return (
    <Table
      isHeaderSticky
      aria-label="Codec Metadata"
      bottomContent={bottomContent}
      bottomContentPlacement="outside"
      classNames={{
        wrapper: "max-h-[80vh]",
        td: "whitespace-nowrap",
      }}
      topContent={topContent}
      topContentPlacement="outside"
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
          <TableRow key={item.key}>
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
