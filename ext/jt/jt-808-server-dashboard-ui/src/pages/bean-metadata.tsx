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

/** 伪代码语法高亮：全部使用主题语义色，浅色 / 深色自动一致 */
const BEAN_CODE_SYNTAX = {
  keyword: "text-accent",
  type: "text-warning",
  field: "text-foreground",
  number: "text-success",
  annotation: "text-muted",
  comment: "text-muted",
} as const;

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
  const { keyword, type, field, number, annotation, comment } =
    BEAN_CODE_SYNTAX;
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
    <div className="flex flex-col gap-2 overflow-x-auto rounded-md border border-border bg-background-tertiary/90 p-3 font-mono text-xs">
      {p.desc && (
        <div className={comment + " italic"}>
          {"// "}
          {p.desc}
        </div>
      )}
      <div className="flex flex-wrap gap-x-4 gap-y-1">
        <span>
          <span className={keyword}>name</span>
          <span className="text-muted">=</span>
          <span className={field}>{p.name}</span>
        </span>
        <span>
          <span className={keyword}>type</span>
          <span className="text-muted">=</span>
          <span className={type}>{getSimpleTypeName(p.type as string)}</span>
        </span>
      </div>
      <div className="flex flex-wrap gap-x-4 gap-y-1">
        <span>
          <span className={keyword}>codec</span>
          <span className="text-muted">=</span>
          <span className={type}>
            {getSimpleClassName(p.codec?.name)}
            {p.codec?.isBuiltIn !== true && " (custom)"}
          </span>
        </span>
        <span>
          <span className={keyword}>length</span>
          <span className="text-muted">=</span>
          <span className={annotation}>{lengthDesc}</span>
        </span>
      </div>
      <div className="flex flex-wrap gap-x-4 gap-y-1">
        <span>
          <span className={keyword}>version</span>
          <span className="text-muted">=</span>
          <span
            className={
              p.version?.isDefault === true
                ? "text-muted"
                : number + " font-semibold"
            }
          >
            {p.version?.isDefault === true ? "default" : p.version?.value}
          </span>
        </span>
        <span>
          <span className={keyword}>order</span>
          <span className="text-muted">=</span>
          <span className={field}>
            {p.order?.isDefault === true ? "default" : p.order?.value}
          </span>
        </span>
        <span>
          <span className={keyword}>dataType</span>
          <span className="text-muted">=</span>
          <span className={annotation}>{p.dataType}</span>
        </span>
        {(p.recordClass || p.recordComponent) && (
          <span>
            <span className={keyword}>record</span>
            <span className="text-muted">=</span>
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
          <span className="text-muted">=</span>
          <span className={number}>{conditionDesc}</span>
        </span>
      </div>
      <div className="flex flex-wrap gap-x-4 gap-y-1">
        <span>
          <span className={keyword}>getter</span>
          <span className="text-muted">=</span>
          <span className={annotation}>
            {getterImpl}({getterDetail || "-"})
          </span>
        </span>
        <span>
          <span className={keyword}>setter</span>
          <span className="text-muted">=</span>
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
  const { keyword, type, field, comment: commentColor } = BEAN_CODE_SYNTAX;

  const typeName = getSimpleTypeName(property.type as string);
  const propName = property.name as string;
  const desc = property.desc as string | undefined;
  const getterImpl = property.getter?.implementation as string;

  return (
    <div className="border-b border-border last:border-b-0">
      <Button
        className="h-auto min-h-8 w-full justify-between px-2 py-1"
        size="sm"
        variant="ghost"
        onPress={() => setExpanded(!expanded)}
      >
        <span className="font-mono text-sm">
          <span className={keyword}>private</span>{" "}
          <span className={type}>{typeName}</span>{" "}
          <span className={field}>{propName}</span>
          {desc && (
            <span className={`text-xs italic ${commentColor}`}>
              {"// "}
              {desc}
            </span>
          )}
        </span>
        <div className="flex gap-1 flex-wrap">
          {getterImpl && (
            <Chip color="accent" size="sm" variant="soft">
              {getterImpl}
            </Chip>
          )}
          {property.codec?.name && (
            <Chip
              color={property.codec?.isBuiltIn === true ? "success" : "warning"}
              size="sm"
              variant="soft"
            >
              {getSimpleClassName(property.codec.name)}
            </Chip>
          )}
          {property.length?.type === "constant" && (
            <Chip color="success" size="sm" variant="soft">
              {property.length.constant?.value}B
            </Chip>
          )}
          {property.length?.type === "placeholder" && (
            <Chip color="accent" size="sm" variant="soft">
              placeholder
            </Chip>
          )}
          {property.length?.type === "expression" && (
            <Chip color="default" size="sm" variant="soft">
              expr
            </Chip>
          )}
          {property.length?.type === "prepend" && (
            <Chip color="warning" size="sm" variant="soft">
              prepend
            </Chip>
          )}
          {property.length?.type === "custom" && (
            <Chip color="danger" size="sm" variant="soft">
              custom
            </Chip>
          )}
          {property.version?.isDefault !== true && (
            <Chip color="danger" size="sm" variant="soft">
              v{property.version?.value}
            </Chip>
          )}
          {property.condition?.type !== "always" && (
            <Chip color="warning" size="sm" variant="soft">
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
  const { keyword, field } = BEAN_CODE_SYNTAX;
  const properties = (item.properties as Dic[]) || [];
  const typeName = getSimpleClassName(item.rawClass as string);
  const fullClassName = item.rawClass as string;
  const isRecord = item.type === "RECORD";

  return (
    <Card className="mb-2 w-full flex-shrink-0">
      <Card.Content className="w-full p-0">
        <Button
          className="h-auto min-h-12 w-full justify-between bg-transparent px-3 py-2"
          variant="secondary"
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
              <span className={keyword}>{isRecord ? "record" : "class"}</span>{" "}
              <span className={field}>{typeName}</span>
            </span>
            {fullClassName.includes("io.github.hylexus.xtream.debug") && (
              <Chip color="danger" size="sm" variant="soft">
                DEBUG
              </Chip>
            )}
            {fullClassName.includes(
              "io.github.hylexus.xtream.codec.ext.jt808.builtin.messages",
            ) && (
              <Chip color="default" size="sm" variant="soft">
                内置
              </Chip>
            )}
          </div>
          <Chip color="accent" size="sm" variant="soft">
            {properties.length} 个属性
          </Chip>
        </Button>
        {expanded && (
          <div className="border-t border-border bg-background-tertiary/60 p-2 pl-4">
            <div className="mb-2 truncate pl-2 font-mono text-xs text-muted">
              {String(item.constructor)}
            </div>
            <div className="flex flex-col gap-1">
              {properties.map((prop, idx) => (
                <PropertyRow key={prop.name ?? idx} property={prop} />
              ))}
            </div>
          </div>
        )}
      </Card.Content>
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
        <TextField
          className="w-56"
          value={filterClassName}
          onChange={onFilterChange(setFilterClassName)}
        >
          <Label className="sr-only">类名</Label>
          <Input placeholder="输入类名" />
        </TextField>
        <FilterOptionGroup
          label="版本"
          options={versionOptions}
          value={filterVersion}
          onChange={onFilterChange(setFilterVersion)}
        />
        <FilterOptionGroup
          label="类型"
          options={dataTypeOptions}
          value={filterDataType}
          onChange={onFilterChange(setFilterDataType)}
        />
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
    <PageSection className="box-border h-full">
      {topContent}
      <div className="flex flex-1 flex-col gap-2 overflow-y-auto">
        {tableData?.data?.map((item, idx) => (
          <BeanCard key={`${item.rawClass}-${idx}`} item={item} />
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
