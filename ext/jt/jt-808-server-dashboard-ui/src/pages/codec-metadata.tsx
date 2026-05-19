import {
  Button,
  Card,
  Chip,
  Input,
  Label,
  Spinner,
  TextField,
} from "@heroui/react";
import { FC, useCallback, useMemo, useState } from "react";

import { FilterOptionGroup } from "@/components/filter-option-group.tsx";
import { ListPageFooter } from "@/components/list-page-footer.tsx";
import { PageSection } from "@/components/page-header.tsx";
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
  return fullName.includes(".")
    ? fullName.slice(fullName.lastIndexOf(".") + 1)
    : fullName;
};

const CodecDetail: FC<{ item: Dic }> = ({ item }) => {
  return (
    <div className="flex flex-col gap-2 overflow-x-auto rounded-md border border-border bg-background-tertiary/90 p-3 font-mono text-xs">
      <div className="flex flex-wrap gap-x-4 gap-y-1">
        <span>
          <span className="text-accent">key</span>
          <span className="text-muted">=</span>
          <span className="text-foreground">{item.key}</span>
        </span>
        <span>
          <span className="text-accent">implementation</span>
          <span className="text-muted">=</span>
          <span className="text-success">
            {getSimpleClassName(item.rawClassName)}
          </span>
        </span>
        <span>
          <span className="text-accent">signedness</span>
          <span className="text-muted">=</span>
          <span className="text-warning">{item.signedness}</span>
        </span>
        <span>
          <span className="text-accent">endian</span>
          <span className="text-muted">=</span>
          <span className="text-muted">{item.endian}</span>
        </span>
      </div>
      <div className="flex flex-wrap gap-x-4 gap-y-1">
        <span>
          <span className="text-accent">charset</span>
          <span className="text-muted">=</span>
          <span className="text-muted">{item.charset}</span>
        </span>
        <span>
          <span className="text-accent">isBuiltin</span>
          <span className="text-muted">=</span>
          <span className={item.isBuiltin ? "text-success" : "text-muted"}>
            {String(item.isBuiltin)}
          </span>
        </span>
      </div>
      <div className="flex flex-col gap-1">
        <span className="text-xs text-muted">Full Class Name:</span>
        <span className="break-all text-xs text-foreground">
          {item.rawClassName}
        </span>
      </div>
    </div>
  );
};

const CodecCard: FC<{ item: Dic; idx: number }> = ({ item, idx }) => {
  const [expanded, setExpanded] = useState(false);

  return (
    <Card key={`${item.key}-${idx}`} className="mb-2 flex-shrink-0">
      <Card.Content className="p-0">
        <Button
          className="h-auto min-h-10 w-full justify-between bg-transparent px-3 py-2"
          variant="secondary"
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
          <div className="flex flex-wrap gap-1">
            <Chip color="accent" size="sm" variant="soft">
              {getSimpleClassName(item.rawClassName)}
            </Chip>
            {item.isBuiltin && (
              <Chip color="success" size="sm" variant="soft">
                内置
              </Chip>
            )}
            {item.signedness !== "NONE" && (
              <Chip
                color={item.signedness === "SIGNED" ? "default" : "accent"}
                size="sm"
                variant="soft"
              >
                {item.signedness === "SIGNED" ? "有符号" : "无符号"}
              </Chip>
            )}
            {item.endian !== "NONE" && (
              <Chip
                color={item.endian === "BIG_ENDIAN" ? "accent" : "default"}
                size="sm"
                variant="soft"
              >
                {item.endian === "BIG_ENDIAN" ? "大端" : "小端"}
              </Chip>
            )}
            {item.charset && item.charset !== "NONE" && (
              <Chip color="default" size="sm" variant="soft">
                {item.charset}
              </Chip>
            )}
          </div>
        </Button>
        {expanded && (
          <div className="border-t border-border bg-background-tertiary/60 p-2">
            <CodecDetail item={item} />
          </div>
        )}
      </Card.Content>
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
        <TextField
          className="w-40"
          value={filterKey}
          onChange={onFilterChange(setFilterKey)}
        >
          <Label className="sr-only">key</Label>
          <Input placeholder="输入 key" />
        </TextField>
        <TextField
          className="w-52"
          value={filterClassName}
          onChange={onFilterChange(setFilterClassName)}
        >
          <Label className="sr-only">实现类</Label>
          <Input placeholder="输入实现类" />
        </TextField>
        <TextField
          className="w-32"
          value={filterCharset}
          onChange={onFilterChange(setFilterCharset)}
        >
          <Label className="sr-only">字符集</Label>
          <Input placeholder="输入字符集" />
        </TextField>
        <FilterOptionGroup
          label="符号"
          options={signednessOptions}
          value={filterSignedness}
          onChange={onFilterChange(setFilterSignedness)}
        />
        <FilterOptionGroup
          label="字节序"
          options={endianOptions}
          value={filterEndian}
          onChange={onFilterChange(setFilterEndian)}
        />
        <FilterOptionGroup
          label="内置"
          options={builtinOptions}
          value={filterBuiltin}
          onChange={onFilterChange(setFilterBuiltin)}
        />
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
    <PageSection
      className="box-border h-full"
      description="字段编解码器注册表，可按 key、符号性、字节序等筛选"
      title="Codec 元数据"
    >
      {topContent}
      <div className="flex flex-1 flex-col gap-2 overflow-y-auto">
        {tableData?.data?.map((item, idx) => (
          <CodecCard key={`${item.key}-${idx}`} idx={idx} item={item} />
        ))}
        {(!tableData?.data || tableData.data.length === 0) && (
          <p className="py-8 text-center text-muted">暂无数据</p>
        )}
      </div>
      <ListPageFooter
        page={page}
        pages={pages}
        total={tableData?.total ?? 0}
        onPageChange={setPage}
      />
    </PageSection>
  );
};
