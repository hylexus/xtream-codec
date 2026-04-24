import { Spinner } from "@heroui/spinner";
import { Pagination } from "@heroui/pagination";
import { Input } from "@heroui/input";
import { Button } from "@heroui/button";
import { Chip } from "@heroui/chip";
import { Card, CardBody } from "@heroui/card";
import { FC, useCallback, useMemo, useState } from "react";

import { Dic } from "@/types";
import { usePageList } from "@/hooks/use-page-list.ts";

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

const getSimpleClassName = (fullName: string) => {
  return fullName.includes(".") ? fullName.slice(fullName.lastIndexOf(".") + 1) : fullName;
};

const CodecDetail: FC<{ item: Dic }> = ({ item }) => {
  return (
    <div className="flex flex-col gap-2 p-3 bg-zinc-50 dark:bg-zinc-900 rounded-md font-mono text-xs overflow-x-auto">
      <div className="flex flex-wrap gap-x-4 gap-y-1">
        <span>
          <span className="text-blue-600 dark:text-blue-400">key</span>
          <span className="text-zinc-500">=</span>
          <span className="text-amber-600 dark:text-amber-400">{item.key}</span>
        </span>
        <span>
          <span className="text-blue-600 dark:text-blue-400">implementation</span>
          <span className="text-zinc-500">=</span>
          <span className="text-emerald-600 dark:text-emerald-400">{getSimpleClassName(item.rawClassName)}</span>
        </span>
        <span>
          <span className="text-blue-600 dark:text-blue-400">signedness</span>
          <span className="text-zinc-500">=</span>
          <span className="text-purple-600 dark:text-purple-400">{item.signedness}</span>
        </span>
        <span>
          <span className="text-blue-600 dark:text-blue-400">endian</span>
          <span className="text-zinc-500">=</span>
          <span className="text-cyan-600 dark:text-cyan-400">{item.endian}</span>
        </span>
      </div>
      <div className="flex flex-wrap gap-x-4 gap-y-1">
        <span>
          <span className="text-blue-600 dark:text-blue-400">charset</span>
          <span className="text-zinc-500">=</span>
          <span className="text-pink-600 dark:text-pink-400">{item.charset}</span>
        </span>
        <span>
          <span className="text-blue-600 dark:text-blue-400">isBuiltin</span>
          <span className="text-zinc-500">=</span>
          <span className={item.isBuiltin ? "text-green-600" : "text-zinc-400"}>
            {String(item.isBuiltin)}
          </span>
        </span>
      </div>
      <div className="flex flex-col gap-1">
        <span className="text-xs text-zinc-500">Full Class Name:</span>
        <span className="text-xs text-zinc-600 dark:text-zinc-300 break-all">
          {item.rawClassName}
        </span>
      </div>
    </div>
  );
};

const CodecCard: FC<{ item: Dic; idx: number }> = ({ item, idx }) => {
  const [expanded, setExpanded] = useState(false);

  return (
    <Card className="mb-2 flex-shrink-0" key={`${item.key}-${idx}`}>
      <CardBody className="p-0">
        <Button
          className="w-full justify-between h-auto min-h-10 py-2 px-3 rounded-none"
          variant="flat"
          onPress={() => setExpanded(!expanded)}
        >
          <div className="flex items-center gap-2">
            <span
              className={`transition-transform ${expanded ? "rotate-90" : ""} text-xs`}
            >
              ▶
            </span>
            <span className="text-xs font-mono font-semibold">{item.key}</span>
          </div>
          <div className="flex gap-1 flex-wrap">
            {item.isBuiltin && (
              <Chip color="success" size="sm" variant="flat">
                内置
              </Chip>
            )}
            <Chip
              color={item.signedness === "SIGNED" ? "success" : "warning"}
              size="sm"
              variant="flat"
            >
              {item.signedness === "SIGNED" ? "有符号" : "无符号"}
            </Chip>
            <Chip
              color={item.endian === "BIG_ENDIAN" ? "secondary" : "warning"}
              size="sm"
              variant="flat"
            >
              {item.endian === "BIG_ENDIAN" ? "大端" : "小端"}
            </Chip>
            {item.charset && item.charset !== "NONE" && (
              <Chip color="primary" size="sm" variant="flat">
                {item.charset}
              </Chip>
            )}
          </div>
        </Button>
        {expanded && (
          <div className="p-2 bg-zinc-50 dark:bg-zinc-900/30 border-t border-zinc-200 dark:border-zinc-700">
            <CodecDetail item={item} />
          </div>
        )}
      </CardBody>
    </Card>
  );
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

  const onFilterChange = useCallback(
    (setter: (v: string) => void) => (value: string) => {
      setter(value);
      setPage(1);
    },
    [setPage],
  );

  const topContent = useMemo(() => {
    return (
      <div className="flex flex-wrap gap-3 items-center mb-4 shrink-0">
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

  if (isLoading && tableData?.data?.length === 0) {
    return (
      <div className="flex items-center justify-center min-h-[200px]">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full p-4 box-border">
      {topContent}
      <div className="flex-1 overflow-y-auto flex flex-col gap-2">
        {tableData?.data?.map((item, idx) => (
          <CodecCard key={`${item.key}-${idx}`} item={item} idx={idx} />
        ))}
        {(!tableData?.data || tableData.data.length === 0) && (
          <div className="text-center text-default-400 py-8">暂无数据</div>
        )}
      </div>
      <div className="flex w-full items-center justify-center gap-3 mt-4 shrink-0">
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
    </div>
  );
};