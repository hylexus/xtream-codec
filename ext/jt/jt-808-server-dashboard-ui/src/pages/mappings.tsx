import {
  Button,
  Card,
  Chip,
  Input,
  Label,
  Spinner,
  TextField,
  Tooltip,
} from "@heroui/react";
import { FC, useEffect, useMemo, useState } from "react";
import useSWR from "swr";

import { Segment } from "@/components/ui/segment.tsx";
import { PageShell } from "@/components/ui/page-shell.tsx";
import { request } from "@/utils/request.ts";

interface HandlerInfo {
  handler: string;
  nonBlocking: boolean;
  handlerDesc: string;
  messageId: number;
  messageIdAsHexString: string;
  messageIdDesc: string;
  version: string;
  scheduler: string;
  rejectBlockingTask: boolean;
  virtualThread: boolean;
}

type GroupKey = string;

interface GroupData {
  key: GroupKey;
  label: string;
  subLabel?: string;
  handlers: HandlerInfo[];
}

/** 避免 `?? []` 每次渲染新引用，触发 groupedData / expandedKeys 连锁更新与 effect 死循环 */
const EMPTY_HANDLER_LIST: HandlerInfo[] = [];

const calcStatus = (item: HandlerInfo) => {
  const { virtualThread, nonBlocking, rejectBlockingTask } = item;
  let color: "success" | "warning" | "danger";
  let label: string;

  if (virtualThread) {
    color = "success";
    label = "虚拟线程";
  } else {
    if (nonBlocking) {
      if (rejectBlockingTask) {
        color = "success";
        label = "非阻塞";
      } else {
        color = "warning";
        label = "非阻塞(可执行同步任务)";
      }
    } else {
      if (rejectBlockingTask) {
        color = "danger";
        label = "请检查调度器配置";
      } else {
        color = "success";
        label = "阻塞";
      }
    }
  }

  return { color, label };
};

const getVersionLabel = (version: string) => {
  const map: Record<string, string> = {
    VERSION_2011: "2011",
    VERSION_2013: "2013",
    VERSION_2019: "2019",
    AUTO_DETECTION: "自动",
  };

  return map[version] ?? version;
};

const getVersionChipColor = (
  version: string,
): "accent" | "default" | "success" | "warning" | "danger" => {
  const map: Record<
    string,
    "accent" | "default" | "success" | "warning" | "danger"
  > = {
    VERSION_2011: "warning",
    VERSION_2013: "default",
    VERSION_2019: "accent",
    AUTO_DETECTION: "success",
  };

  return map[version] ?? "default";
};

const StatusChip: FC<{ item: HandlerInfo }> = ({ item }) => {
  const { color, label } = calcStatus(item);

  return (
    <Chip color={color} size="sm" variant="soft">
      {label}
    </Chip>
  );
};

const VersionChip: FC<{ version: string }> = ({ version }) => {
  return (
    <Chip color={getVersionChipColor(version)} size="sm" variant="soft">
      {getVersionLabel(version)}
    </Chip>
  );
};

const getHandlerClassName = (handler: string): string => {
  const parts = handler.split("#");

  return parts[0].replace(/^.*\./, "");
};

const GROUP_BY_OPTIONS = [
  { id: "messageId", label: "消息ID" },
  { id: "version", label: "协议版本" },
  { id: "className", label: "处理器" },
  { id: "status", label: "状态" },
  { id: "scheduler", label: "调度器" },
] as const;

export const MappingsPage = () => {
  const [searchKey, setSearchKey] = useState("");
  const [groupBy, setGroupBy] = useState<string>("messageId");

  const { data, isLoading } = useSWR<{
    dispatcherXtreamHandler: HandlerInfo[];
  }>(
    "mappings",
    () =>
      request({
        path: "actuator/mappings",
        method: "GET",
      }),
    {},
  );

  const rawData = data?.dispatcherXtreamHandler ?? EMPTY_HANDLER_LIST;

  const filteredData = useMemo(() => {
    if (!searchKey.trim()) return rawData;
    const key = searchKey.toLowerCase();

    return rawData.filter(
      (item) =>
        item.messageIdAsHexString.toLowerCase().includes(key) ||
        item.messageIdDesc.toLowerCase().includes(key) ||
        item.handler.toLowerCase().includes(key) ||
        item.scheduler.toLowerCase().includes(key),
    );
  }, [rawData, searchKey]);

  const groupedData = useMemo<GroupData[]>(() => {
    const map = new Map<GroupKey, GroupData>();

    for (const item of filteredData) {
      let key: GroupKey;
      let label: string;
      let subLabel: string | undefined;

      switch (groupBy) {
        case "messageId":
          key = String(item.messageId);
          label = item.messageIdAsHexString;
          subLabel = item.messageIdDesc;
          break;
        case "version":
          key = item.version;
          label = getVersionLabel(item.version);
          break;
        case "className":
          key = getHandlerClassName(item.handler);
          label = getHandlerClassName(item.handler);
          break;
        case "status":
          key = calcStatus(item).label;
          label = calcStatus(item).label;
          break;
        case "scheduler":
          key = item.scheduler;
          label = item.scheduler;
          break;
        default:
          key = String(item.messageId);
          label = item.messageIdAsHexString;
      }

      const existing = map.get(key);

      if (existing) {
        existing.handlers.push(item);
      } else {
        map.set(key, { key, label, subLabel, handlers: [item] });
      }
    }

    const sorted = Array.from(map.values());

    if (groupBy === "messageId") {
      sorted.sort((a, b) => Number(a.key) - Number(b.key));
    } else {
      sorted.sort((a, b) => a.label.localeCompare(b.label));
    }

    return sorted;
  }, [filteredData, groupBy]);

  const stats = useMemo(() => {
    const total = rawData.length;
    let nonBlocking = 0;
    let blocking = 0;
    let virtualThread = 0;

    for (const item of rawData) {
      if (item.virtualThread) virtualThread++;
      else if (item.nonBlocking) nonBlocking++;
      else blocking++;
    }

    return { total, nonBlocking, blocking, virtualThread };
  }, [rawData]);

  const expandedKeys = useMemo(
    () => new Set(groupedData.map((g) => g.key)),
    [groupedData],
  );
  const [expanded, setExpanded] = useState<Set<GroupKey>>(new Set());

  useEffect(() => {
    setExpanded(expandedKeys);
  }, [expandedKeys]);

  const toggleExpand = (key: GroupKey) => {
    setExpanded((prev) => {
      const next = new Set(prev);

      if (next.has(key)) next.delete(key);
      else next.add(key);

      return next;
    });
  };

  const expandAll = () => setExpanded(expandedKeys);
  const collapseAll = () => setExpanded(new Set());

  const loadingState =
    isLoading && filteredData.length === 0 ? "loading" : "idle";

  const getGroupLabel = (group: GroupData) => {
    if (groupBy === "status") {
      const colors: Record<string, "success" | "warning" | "danger"> = {
        虚拟线程: "success",
        非阻塞: "success",
        "非阻塞(可执行同步任务)": "warning",
        阻塞: "success",
        请检查调度器配置: "danger",
      };

      return (
        <Chip color={colors[group.label] || "default"} size="sm" variant="soft">
          {group.label}
        </Chip>
      );
    }
    if (groupBy === "version") {
      return (
        <Chip color={getVersionChipColor(group.key)} size="sm" variant="soft">
          {group.label}
        </Chip>
      );
    }

    return <span className="font-mono">{group.label}</span>;
  };

  const getGroupHeader = (group: GroupData) => {
    if (groupBy === "messageId") {
      return (
        <div className="flex items-center gap-2">
          <button
            className="flex h-5 w-5 items-center justify-center text-xs text-default-400 transition-colors hover:text-default-600"
            type="button"
            onClick={() => toggleExpand(group.key)}
          >
            {expanded.has(group.key) ? "▼" : "▶"}
          </button>
          <Chip className="font-mono" size="sm" variant="soft">
            {group.label}
          </Chip>
          <span className="text-sm">{group.subLabel}</span>
          <Chip color="default" size="sm" variant="tertiary">
            {group.handlers.length} 个
          </Chip>
        </div>
      );
    }

    return (
      <div className="flex items-center gap-2">
        <button
          className="flex h-5 w-5 items-center justify-center text-xs text-default-400 transition-colors hover:text-default-600"
          type="button"
          onClick={() => toggleExpand(group.key)}
        >
          {expanded.has(group.key) ? "▼" : "▶"}
        </button>
        {getGroupLabel(group)}
        <Chip color="default" size="sm" variant="tertiary">
          {group.handlers.length} 个
        </Chip>
      </div>
    );
  };

  return (
    <PageShell className="h-full">
      <div className="flex shrink-0 flex-wrap items-center gap-4">
        <Segment
          aria-label="分组方式"
          selectedKey={groupBy}
          variant="default"
          onSelectionChange={(key) => setGroupBy(String(key))}
        >
          {GROUP_BY_OPTIONS.map((opt, index) => (
            <Segment.Item key={opt.id} id={opt.id}>
              {index > 0 ? <Segment.Separator /> : null}
              {opt.label}
            </Segment.Item>
          ))}
        </Segment>
        <div className="flex w-96 max-w-full items-center gap-2">
          <TextField
            className="min-w-0 flex-1"
            value={searchKey}
            onChange={setSearchKey}
          >
            <Label className="sr-only">搜索</Label>
            <Input placeholder="搜索消息ID、描述、处理器、调度器..." />
          </TextField>
        </div>
        <div className="ml-auto flex gap-2">
          <Button size="sm" variant="ghost" onPress={expandAll}>
            展开全部
          </Button>
          <Button size="sm" variant="ghost" onPress={collapseAll}>
            折叠全部
          </Button>
        </div>
      </div>

      <div className="flex shrink-0 flex-wrap gap-4">
        <Card className="flex-1 min-w-[140px] border border-border">
          <Card.Content className="flex flex-row items-center justify-between px-4 py-2">
            <span className="text-sm text-default-500">总计</span>
            <Chip color="accent" variant="soft">
              {stats.total}
            </Chip>
          </Card.Content>
        </Card>
        <Card className="flex-1 min-w-[140px] border border-border">
          <Card.Content className="flex flex-row items-center justify-between px-4 py-2">
            <span className="text-sm text-default-500">非阻塞</span>
            <Chip color="success" variant="soft">
              {stats.nonBlocking}
            </Chip>
          </Card.Content>
        </Card>
        <Card className="flex-1 min-w-[140px] border border-border">
          <Card.Content className="flex flex-row items-center justify-between px-4 py-2">
            <span className="text-sm text-default-500">阻塞</span>
            <Chip color="warning" variant="soft">
              {stats.blocking}
            </Chip>
          </Card.Content>
        </Card>
        <Card className="flex-1 min-w-[140px] border border-border">
          <Card.Content className="flex flex-row items-center justify-between px-4 py-2">
            <span className="text-sm text-default-500">虚拟线程</span>
            <Chip color="default" variant="soft">
              {stats.virtualThread}
            </Chip>
          </Card.Content>
        </Card>
      </div>

      <div className="flex-1 overflow-y-auto">
        {isLoading && rawData.length === 0 ? (
          <div className="flex min-h-[200px] items-center justify-center">
            <Spinner />
          </div>
        ) : groupedData.length === 0 ? (
          <div className="py-8 text-center text-default-400">暂无数据</div>
        ) : (
          <div
            aria-label="mappings table"
            className="flex flex-col gap-2"
            role="table"
          >
            {loadingState === "loading" && (
              <div className="flex justify-center py-2">
                <Spinner />
              </div>
            )}
            {groupedData.map((group) => (
              <div
                key={group.key}
                className="rounded-medium border border-border"
              >
                <div className="border-b border-border px-3 py-2">
                  {getGroupHeader(group)}
                </div>
                {expanded.has(group.key) && (
                  <div className="bg-background-tertiary/50 px-2 py-2">
                    <table className="w-full border-collapse text-sm">
                      <thead>
                        <tr className="text-left text-default-500">
                          <th className="p-2 font-medium">消息ID</th>
                          <th className="p-2 font-medium">状态</th>
                          <th className="p-2 font-medium">处理器</th>
                          <th className="p-2 font-medium">协议版本</th>
                          <th className="p-2 font-medium">调度器</th>
                          <th className="p-2 font-medium">备注</th>
                        </tr>
                      </thead>
                      <tbody>
                        {group.handlers.map((handler, idx) => (
                          <tr
                            key={`${group.key}-${idx}`}
                            className="border-t border-separator"
                          >
                            <td className="p-2 align-top">
                              <div className="flex items-center gap-1">
                                <Chip
                                  className="font-mono text-xs"
                                  size="sm"
                                  variant="soft"
                                >
                                  {handler.messageIdAsHexString}
                                </Chip>
                                <span className="whitespace-nowrap text-xs text-default-500">
                                  {handler.messageIdDesc}
                                </span>
                              </div>
                            </td>
                            <td className="p-2 align-top">
                              <StatusChip item={handler} />
                            </td>
                            <td className="p-2 align-top">
                              <Tooltip>
                                <Tooltip.Trigger>
                                  <span className="cursor-pointer font-mono text-xs">
                                    {handler.handler.replace(/^.*\./, "")}
                                  </span>
                                </Tooltip.Trigger>
                                <Tooltip.Content>
                                  <span className="max-w-md break-all text-xs">
                                    {handler.handler}
                                  </span>
                                </Tooltip.Content>
                              </Tooltip>
                            </td>
                            <td className="p-2 align-top">
                              <VersionChip version={handler.version} />
                            </td>
                            <td className="p-2 align-top">
                              <span className="font-mono text-xs text-default-500">
                                {handler.scheduler}
                              </span>
                            </td>
                            <td className="p-2 align-top">
                              <span className="text-xs text-default-400">
                                {handler.handlerDesc || "-"}
                              </span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </PageShell>
  );
};
