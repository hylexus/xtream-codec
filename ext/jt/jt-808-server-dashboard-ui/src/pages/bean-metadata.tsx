import { Spinner } from "@heroui/spinner";
import { Pagination } from "@heroui/pagination";
import { Input } from "@heroui/input";
import { Button } from "@heroui/button";
import { Chip } from "@heroui/chip";
import { Card, CardBody } from "@heroui/card";
import { useEffect, useState } from "react";
import { FC, useCallback, useMemo } from "react";

import { Dic } from "@/types";
import { usePageList } from "@/hooks/use-page-list.ts";

const getSimpleClassName = (fullName: string) => {
  return fullName.includes(".")
    ? fullName.slice(fullName.lastIndexOf(".") + 1)
    : fullName;
};

const getSimpleTypeName = (type: string) => {
  if (type.includes(".")) {
    return type.slice(type.lastIndexOf(".") + 1);
  }
  if (type.startsWith("[B")) {
    return "byte[]";
  }
  if (type.startsWith("[")) {
    return type.slice(1) + "[]";
  }

  return type;
};

const ECLIPSE_COLORS = {
  keyword: "text-[#0000FF]",
  type: "text-[#0000FF]",
  field: "text-[#000000]",
  string: "text-[#3F7F5F]",
  number: "text-[#3F7F5F]",
  comment: "text-[#808080]",
  annotation: "text-[#3F7F5F]",
} as const;

const INTELLIJ_COLORS = {
  keyword: "text-[#CC7832]",
  type: "text-[#FF8020]",
  field: "text-[#A9B7C6]",
  string: "text-[#A9B7C6]",
  number: "text-[#6897BB]",
  comment: "text-[#808080]",
  annotation: "text-[#A9B7C6]",
} as const;

const useCodeColors = () => {
  const [isDark, setIsDark] = useState(() => {
    if (typeof window === 'undefined') return false;
    return document.documentElement.classList.contains('dark');
  });

  useEffect(() => {
    const observer = new MutationObserver(() => {
      setIsDark(document.documentElement.classList.contains('dark'));
    });
    observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['class'],
    });
    return () => observer.disconnect();
  }, []);

  return isDark ? INTELLIJ_COLORS : ECLIPSE_COLORS;
};

const getImplDetail = (
  impl: string,
  detail: Dic | undefined,
): string | undefined => {
  if (!detail || !impl) return undefined;
  const d = detail as Dic;

  switch (impl) {
    case "LambdaMetaFactory":
      return d?.method as string | undefined;
    case "VarHandle":
      return d?.field as string | undefined;
    case "MethodHandle":
      return (d?.field ?? d?.method) as string | undefined;
    case "Reflection":
      return (d?.field ?? d?.method) as string | undefined;
    default:
      return undefined;
  }
};

const PropertyDetail: FC<{ property: Dic }> = ({ property }) => {
  const codeColors = useCodeColors();
  const { keyword, type, field, number, annotation, comment } = codeColors;
  const p = property as Dic;
  const getterImpl = p.getter?.implementation as string;
  const setterImpl = p.setter?.implementation as string;
  const getterDetail = getImplDetail(getterImpl, p.getter?.[getterImpl] as Dic);
  const setterDetail = getImplDetail(setterImpl, p.setter?.[setterImpl] as Dic);

  const lengthDesc = useMemo(() => {
    const length = p.length as Dic;

    if (!length) return "-";
    switch (length.type) {
      case "constant":
        return `constant(${length.constant?.value})`;
      case "expression":
        return `expression(${length.expression?.value})`;
      case "placeholder":
        return `placeholder(${length.placeholder?.value})`;
      case "prepend":
        return `prepend(${length.prepend?.value})`;
      case "custom":
        return `custom(${length.custom?.value})`;
      default:
        return length.type;
    }
  }, [p.length]);

  const conditionDesc = useMemo(() => {
    const cond = p.condition as Dic;

    if (!cond) return "-";
    switch (cond.type) {
      case "always":
        return "always";
      case "never":
        return "never";
      case "expression":
        return `expr(${cond.expression?.value})`;
      case "custom":
        return `custom(${cond.custom?.value})`;
      default:
        return cond.type;
    }
  }, [p.condition]);

  return (
    <div className="flex flex-col gap-2 p-3 bg-zinc-50 dark:bg-zinc-900 rounded-md font-mono text-xs overflow-x-auto">
      {p.desc && <div className={comment + " italic"}>// {p.desc}</div>}
      <div className="flex flex-wrap gap-x-4 gap-y-1">
        <span>
          <span className={keyword}>name</span>
          <span className="text-zinc-500">=</span>
          <span className={field}>{p.name}</span>
        </span>
        <span>
          <span className={keyword}>type</span>
          <span className="text-zinc-500">=</span>
          <span className={type}>
            {getSimpleTypeName(p.type as string)}
          </span>
        </span>
      </div>
      <div className="flex flex-wrap gap-x-4 gap-y-1">
        <span>
          <span className={keyword}>codec</span>
          <span className="text-zinc-500">=</span>
          <span className={type}>
            {getSimpleClassName(p.codec?.name)}
            {p.codec?.isBuiltIn !== true && " (custom)"}
          </span>
        </span>
        <span>
          <span className={keyword}>length</span>
          <span className="text-zinc-500">=</span>
          <span className={annotation}>
            {lengthDesc}
          </span>
        </span>
      </div>
      <div className="flex flex-wrap gap-x-4 gap-y-1">
        <span>
          <span className={keyword}>version</span>
          <span className="text-zinc-500">=</span>
          <span
            className={
              p.version?.isDefault === true
                ? "text-zinc-400"
                : number + " font-semibold"
            }
          >
            {p.version?.isDefault === true ? "default" : p.version?.value}
          </span>
        </span>
        <span>
          <span className={keyword}>order</span>
          <span className="text-zinc-500">=</span>
          <span className={field}>
            {p.order?.isDefault === true ? "default" : p.order?.value}
          </span>
        </span>
        <span>
          <span className={keyword}>dataType</span>
          <span className="text-zinc-500">=</span>
          <span className={annotation}>{p.dataType}</span>
        </span>
        {(p.recordClass || p.recordComponent) && (
          <span>
            <span className={keyword}>record</span>
            <span className="text-zinc-500">=</span>
            <span className={number}>
              {p.recordComponent ? "component" : ""}
              {p.recordClass ? " class" : ""}
            </span>
          </span>
        )}
      </div>
      <div className="flex flex-wrap gap-x-4 gap-y-1">
        <span>
          <span className={keyword}>condition</span>
          <span className="text-zinc-500">=</span>
          <span className={number}>
            {conditionDesc}
          </span>
        </span>
      </div>
      <div className="flex flex-wrap gap-x-4 gap-y-1">
        <span>
          <span className={keyword}>getter</span>
          <span className="text-zinc-500">=</span>
          <span className={annotation}>
            {getterImpl}({getterDetail || "-"})
          </span>
        </span>
        <span>
          <span className={keyword}>setter</span>
          <span className="text-zinc-500">=</span>
          <span className={annotation}>
            {setterImpl}({setterDetail || "-"})
          </span>
        </span>
      </div>
    </div>
  );
};

const PropertyRow: FC<{ property: Dic }> = ({ property }) => {
  const [expanded, setExpanded] = useState(false);
  const codeColors = useCodeColors();
  const { keyword, type, field, comment: commentColor } = codeColors;

  const typeName = getSimpleTypeName(property.type as string);
  const propName = property.name as string;
  const desc = property.desc as string | undefined;
  const getterImpl = property.getter?.implementation as string;

  return (
    <div className="border-b border-zinc-200 dark:border-zinc-700 last:border-b-0">
      <Button
        className="w-full justify-between h-auto min-h-8 py-1 px-2"
        size="sm"
        variant="light"
        onPress={() => setExpanded(!expanded)}
      >
        <span className="font-mono text-sm">
          <span className={keyword}>private</span>{" "}
          <span className={type}>{typeName}</span>{" "}
          <span className={field}>{propName}</span>
          {desc && (
            <span className={`text-xs italic ${commentColor}`}> // {desc}</span>
          )}
        </span>
        <div className="flex gap-1 flex-wrap">
          {getterImpl && (
            <Chip color="primary" size="sm" variant="flat">
              {getterImpl}
            </Chip>
          )}
          {property.codec?.name && (
            <Chip
              color={property.codec?.isBuiltIn === true ? "success" : "warning"}
              size="sm"
              variant="flat"
            >
              {getSimpleClassName(property.codec.name)}
            </Chip>
          )}
          {property.length?.type === "constant" && (
            <Chip color="success" size="sm" variant="flat">
              {property.length.constant?.value}B
            </Chip>
          )}
          {property.length?.type === "placeholder" && (
            <Chip color="primary" size="sm" variant="flat">
              placeholder
            </Chip>
          )}
          {property.length?.type === "expression" && (
            <Chip color="secondary" size="sm" variant="flat">
              expr
            </Chip>
          )}
          {property.length?.type === "prepend" && (
            <Chip color="warning" size="sm" variant="flat">
              prepend
            </Chip>
          )}
          {property.length?.type === "custom" && (
            <Chip color="danger" size="sm" variant="flat">
              custom
            </Chip>
          )}
          {property.version?.isDefault !== true && (
            <Chip color="danger" size="sm" variant="flat">
              v{property.version?.value}
            </Chip>
          )}
          {property.condition?.type !== "always" && (
            <Chip color="warning" size="sm" variant="flat">
              {property.condition?.type}
            </Chip>
          )}
        </div>
      </Button>
      {expanded && (
        <div className="pl-4">
          <PropertyDetail property={property} />
        </div>
      )}
    </div>
  );
};

const BeanCard: FC<{ item: Dic }> = ({ item }) => {
  const [expanded, setExpanded] = useState(false);
  const codeColors = useCodeColors();
  const { keyword, field } = codeColors;
  const properties = (item.properties as Dic[]) || [];
  const typeName = getSimpleClassName(item.rawClass as string);
  const fullClassName = item.rawClass as string;
  const isRecord = item.type === "RECORD";

  return (
    <Card className="mb-2 flex-shrink-0 w-full">
      <CardBody className="p-0 w-full">
        <Button
          className="w-full justify-between h-auto min-h-12 py-2 px-3 rounded-none"
          variant="flat"
          onPress={() => setExpanded(!expanded)}
        >
          <div className="flex items-center gap-2">
            <span
              className={`transition-transform ${expanded ? "rotate-90" : ""} text-xs`}
            >
              ▶
            </span>
            <span className="font-mono text-sm">
              <span className={keyword}>public</span>{" "}
              <span className={keyword}>
                {isRecord ? "record" : "class"}
              </span>{" "}
              <span className={field}>{typeName}</span>
            </span>
            {fullClassName.includes("io.github.hylexus.xtream.debug") && (
              <Chip color="danger" size="sm" variant="flat">
                DEBUG
              </Chip>
            )}
            {fullClassName.includes(
              "io.github.hylexus.xtream.codec.ext.jt808.builtin.messages",
            ) && (
              <Chip color="secondary" size="sm" variant="flat">
                内置
              </Chip>
            )}
          </div>
          <Chip color="primary" size="sm" variant="flat">
            {properties.length} 个属性
          </Chip>
        </Button>
        {expanded && (
          <div className="pl-4 p-2 bg-zinc-50 dark:bg-zinc-900/30 border-t border-zinc-200 dark:border-zinc-700">
            <div className="text-xs font-mono text-zinc-500 mb-2 pl-2 truncate">
              {String(item.constructor)}
            </div>
            <div className="flex flex-col gap-1">
              {properties.map((prop, idx) => (
                <PropertyRow key={prop.name ?? idx} property={prop} />
              ))}
            </div>
          </div>
        )}
      </CardBody>
    </Card>
  );
};

const versionOptions = [
  { key: "", label: "全部" },
  { key: "2019", label: "2019" },
  { key: "2013", label: "2013" },
  { key: "2011", label: "2011" },
];

const dataTypeOptions = [
  { key: "", label: "全部" },
  { key: "basic", label: "basic" },
  { key: "struct", label: "struct" },
  { key: "sequence", label: "sequence" },
  { key: "map", label: "map" },
];

export const BeanMetadataPage = () => {
  const [filterClassName, setFilterClassName] = useState("");
  const [filterVersion, setFilterVersion] = useState("");
  const [filterDataType, setFilterDataType] = useState("");

  const extraParams = useMemo(() => {
    const params: Record<string, string> = {};

    if (filterClassName) params.className = filterClassName;
    if (filterVersion) params.version = filterVersion;
    if (filterDataType) params.dataType = filterDataType;

    return params;
  }, [filterClassName, filterVersion, filterDataType]);

  const { setPage, page, pages, tableData, isLoading } = usePageList<Dic>(
    "codec/bean-metadata",
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
          className="w-56"
          placeholder="输入类名"
          size="sm"
          value={filterClassName}
          onValueChange={onFilterChange(setFilterClassName)}
        />
        <div className="flex items-center gap-1">
          <span className="text-xs text-default-500">版本:</span>
          <div className="flex">
            {versionOptions.map((o) => (
              <Button
                key={o.key}
                color={filterVersion === o.key ? "primary" : "default"}
                size="sm"
                variant={filterVersion === o.key ? "flat" : "light"}
                onPress={() => onFilterChange(setFilterVersion)(o.key)}
              >
                {o.label}
              </Button>
            ))}
          </div>
        </div>
        <div className="flex items-center gap-1">
          <span className="text-xs text-default-500">类型:</span>
          <div className="flex">
            {dataTypeOptions.map((o) => (
              <Button
                key={o.key}
                color={filterDataType === o.key ? "primary" : "default"}
                size="sm"
                variant={filterDataType === o.key ? "flat" : "light"}
                onPress={() => onFilterChange(setFilterDataType)(o.key)}
              >
                {o.label}
              </Button>
            ))}
          </div>
        </div>
      </div>
    );
  }, [filterClassName, filterVersion, filterDataType, onFilterChange]);

  if (isLoading && tableData?.data?.length === 0) {
    return (
      <div className="flex items-center justify-center min-h-[200px]">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full box-border">
      {topContent}
      <div className="flex-1 overflow-y-auto flex flex-col gap-2">
        {tableData?.data?.map((item, idx) => (
          <BeanCard key={`${item.rawClass}-${idx}`} item={item} />
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
