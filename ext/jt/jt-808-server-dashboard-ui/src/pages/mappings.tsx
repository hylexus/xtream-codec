import {
  Table,
  TableBody,
  TableCell,
  TableColumn,
  TableHeader,
  TableRow,
} from "@heroui/table";
import { Spinner } from "@heroui/spinner";
import React, { FC, useMemo, useState } from "react";
import useSWR from "swr";
import { Tooltip } from "@heroui/tooltip";
import { Chip } from "@heroui/chip";
import { Input } from "@heroui/input";
import { Card, CardBody } from "@heroui/card";
import { Button } from "@heroui/button";
import { Tabs, Tab } from "@heroui/tabs";

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

const getVersionColor = (
  version: string,
): "primary" | "secondary" | "success" | "warning" | "danger" => {
  const map: Record<
    string,
    "primary" | "secondary" | "success" | "warning" | "danger"
  > = {
    VERSION_2011: "warning",
    VERSION_2013: "secondary",
    VERSION_2019: "primary",
    AUTO_DETECTION: "success",
  };

  return map[version] ?? "default";
};

const StatusChip: FC<{ item: HandlerInfo }> = ({ item }) => {
  const { color, label } = calcStatus(item);

  return (
    <Chip color={color} size="sm" variant="dot">
      {label}
    </Chip>
  );
};

const VersionChip: FC<{ version: string }> = ({ version }) => {
  return (
    <Chip color={getVersionColor(version)} size="sm" variant="flat">
      {getVersionLabel(version)}
    </Chip>
  );
};

const getHandlerClassName = (handler: string): string => {
  const parts = handler.split("#");

  return parts[0].replace(/^.*\./, "");
};

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

  const rawData = data?.dispatcherXtreamHandler ?? [];

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

  React.useEffect(() => {
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
        <Chip color={colors[group.label] || "default"} size="sm" variant="flat">
          {group.label}
        </Chip>
      );
    }
    if (groupBy === "version") {
      return (
        <Chip color={getVersionColor(group.key)} size="sm" variant="flat">
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
            className="w-5 h-5 flex items-center justify-center text-xs text-default-400 hover:text-default-600 transition-colors"
            onClick={() => toggleExpand(group.key)}
          >
            {expanded.has(group.key) ? "▼" : "▶"}
          </button>
          <Chip className="font-mono" size="sm" variant="flat">
            {group.label}
          </Chip>
          <span className="text-sm">{group.subLabel}</span>
          <Chip color="default" size="sm" variant="shadow">
            {group.handlers.length} 个
          </Chip>
        </div>
      );
    }

    return (
      <div className="flex items-center gap-2">
        <button
          className="w-5 h-5 flex items-center justify-center text-xs text-default-400 hover:text-default-600 transition-colors"
          onClick={() => toggleExpand(group.key)}
        >
          {expanded.has(group.key) ? "▼" : "▶"}
        </button>
        {getGroupLabel(group)}
        <Chip color="default" size="sm" variant="shadow">
          {group.handlers.length} 个
        </Chip>
      </div>
    );
  };

  return (
    <div className="flex flex-col h-full p-4 gap-4 box-border">
      <div className="flex flex-wrap gap-4 items-center shrink-0">
        <Tabs
          selectedKey={groupBy}
          size="md"
          onSelectionChange={(key) => setGroupBy(String(key))}
        >
          <Tab key="messageId" title="消息ID" />
          <Tab key="version" title="协议版本" />
          <Tab key="className" title="处理器" />
          <Tab key="status" title="状态" />
          <Tab key="scheduler" title="调度器" />
        </Tabs>
        <Input
          className="w-96"
          placeholder="搜索消息ID、描述、处理器、调度器..."
          size="md"
          startContent={<span className="text-default-400 text-xs">🔍</span>}
          value={searchKey}
          onValueChange={setSearchKey}
        />
        <div className="flex gap-2 ml-auto">
          <Button size="sm" variant="light" onPress={expandAll}>
            展开全部
          </Button>
          <Button size="sm" variant="light" onPress={collapseAll}>
            折叠全部
          </Button>
        </div>
      </div>

      <div className="flex gap-4 shrink-0">
        <Card className="flex-1">
          <CardBody className="py-2 px-4 flex flex-row items-center justify-between">
            <span className="text-sm text-default-500">总计</span>
            <Chip color="primary" variant="flat">
              {stats.total}
            </Chip>
          </CardBody>
        </Card>
        <Card className="flex-1">
          <CardBody className="py-2 px-4 flex flex-row items-center justify-between">
            <span className="text-sm text-default-500">非阻塞</span>
            <Chip color="success" variant="flat">
              {stats.nonBlocking}
            </Chip>
          </CardBody>
        </Card>
        <Card className="flex-1">
          <CardBody className="py-2 px-4 flex flex-row items-center justify-between">
            <span className="text-sm text-default-500">阻塞</span>
            <Chip color="warning" variant="flat">
              {stats.blocking}
            </Chip>
          </CardBody>
        </Card>
        <Card className="flex-1">
          <CardBody className="py-2 px-4 flex flex-row items-center justify-between">
            <span className="text-sm text-default-500">虚拟线程</span>
            <Chip color="secondary" variant="flat">
              {stats.virtualThread}
            </Chip>
          </CardBody>
        </Card>
      </div>

      <div className="flex-1 overflow-y-auto">
        {isLoading && rawData.length === 0 ? (
          <div className="flex items-center justify-center min-h-[200px]">
            <Spinner />
          </div>
        ) : groupedData.length === 0 ? (
          <div className="text-center text-default-400 py-8">暂无数据</div>
        ) : (
          <Table hideHeader aria-label="mappings table">
            <TableHeader>
              <TableColumn>分组</TableColumn>
            </TableHeader>
            <TableBody loadingContent={<Spinner />} loadingState={loadingState}>
              {groupedData.map((group) => (
                <React.Fragment key={group.key}>
                  <TableRow>
                    <TableCell>{getGroupHeader(group)}</TableCell>
                  </TableRow>
                  {expanded.has(group.key) && (
                    <TableRow>
                      <TableCell className="pl-8 bg-zinc-50 dark:bg-zinc-900/30">
                        <Table aria-label="inner table">
                          <TableHeader>
                            <TableColumn>消息ID</TableColumn>
                            <TableColumn>状态</TableColumn>
                            <TableColumn>处理器</TableColumn>
                            <TableColumn>协议版本</TableColumn>
                            <TableColumn>调度器</TableColumn>
                            <TableColumn>备注</TableColumn>
                          </TableHeader>
                          <TableBody>
                            {group.handlers.map((handler, idx) => (
                              <TableRow key={`${group.key}-${idx}`}>
                                <TableCell>
                                  <div className="flex items-center gap-1">
                                    <Chip
                                      className="font-mono text-xs"
                                      size="sm"
                                      variant="flat"
                                    >
                                      {handler.messageIdAsHexString}
                                    </Chip>
                                    <span className="text-xs text-default-500 whitespace-nowrap">
                                      {handler.messageIdDesc}
                                    </span>
                                  </div>
                                </TableCell>
                                <TableCell>
                                  <StatusChip item={handler} />
                                </TableCell>
                                <TableCell>
                                  <Tooltip content={handler.handler}>
                                    <span className="cursor-pointer font-mono text-xs">
                                      {handler.handler.replace(/^.*\./, "")}
                                    </span>
                                  </Tooltip>
                                </TableCell>
                                <TableCell>
                                  <VersionChip version={handler.version} />
                                </TableCell>
                                <TableCell>
                                  <span className="text-xs text-default-500 font-mono">
                                    {handler.scheduler}
                                  </span>
                                </TableCell>
                                <TableCell>
                                  <span className="text-xs text-default-400">
                                    {handler.handlerDesc || "-"}
                                  </span>
                                </TableCell>
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>
                      </TableCell>
                    </TableRow>
                  )}
                </React.Fragment>
              ))}
            </TableBody>
          </Table>
        )}
      </div>
    </div>
  );
};
