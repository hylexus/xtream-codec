import type { TreeDataNode } from "antd";

import { Tree, ConfigProvider } from "antd";
import { FC } from "react";
import clsx from "clsx";

import {
  LuChevronDownIcon,
  LuCircleCheckIcon,
  LuCubeIcon,
  LuHashtagIcon,
  LuQuoteRightIcon,
} from "@/components/icons.tsx";
import { Dic } from "@/types";
const annotation: Dic = {
  features: "功能开关",
  requestLogger: "请求日志",
  requestDispatcherScheduler:
    "转发请求时使用指定的调度器(而不是上游默认的Reactor调度器)",
  requestCombiner: "分包请求合并",
  instructionServer: "指令服务器",
  attachmentServer: "附件服务器",
  schedulers: "Reactor调度器",
  requestDispatcher: "和 features.requestDispatcherScheduler 相对应的配置",
  nonBlockingHandler: "[非阻塞类型] 的处理器用到的调度器",
  blockingHandler: "[阻塞类型] 的处理器用到的调度器",
  eventPublisher: "[事件发布器] 用到的调度器",
  customSchedulers: "[用户自定义] 调度器",
};
const isObject = (val: unknown) => {
  return typeof val === "object" && val !== null;
};
const valueColor = (item: unknown): string => {
  if (typeof item === "number") {
    return "text-accent";
  }
  if (typeof item === "boolean") {
    return item ? "text-success" : "text-secondary";
  }
  if (typeof item === "string") {
    return "text-secondary";
  }

  return "text-secondary";
};
const valueIcon = (item: unknown) => {
  if (typeof item === "number") {
    return <LuHashtagIcon className="text-small" />;
  }
  if (typeof item === "boolean") {
    return <LuCircleCheckIcon className="text-small" />;
  }
  if (typeof item === "string") {
    return <LuQuoteRightIcon className="text-small" />;
  }

  return <LuQuoteRightIcon className="text-small" />;
};
const generateData = (json: object, _preKey: string, page?: string) => {
  const tree: TreeDataNode[] = [];

  Object.keys(json).forEach((key, index) => {
    const item: unknown = (json as Record<string, unknown>)[key];
    const curKey = _preKey + "-" + index;

    if (isObject(item)) {
      tree.push({
        key: curKey,
        icon: <LuCubeIcon className="text-small" />,
        title: (
          <div
            className={clsx(
              "inline-flex gap-x-12",
              page === "threads" ? "justify-between" : "justify-normal",
            )}
          >
            <span>{key}</span>
            {annotation[key] && (
              <span className="text-small text-muted">{annotation[key]}</span>
            )}
          </div>
        ),
        children: generateData(item as object, curKey, page),
      });
    } else {
      tree.push({
        key: curKey,
        icon: valueIcon(item),
        title: (
          <div
            className={clsx(
              "inline-flex gap-x-12 w-[80%] flex-grow",
              page === "threads" ? "justify-between" : "justify-normal",
            )}
          >
            <span>{key}</span>
            <span className={valueColor(item)}>
              {typeof item === "string" ||
              typeof item === "number" ||
              typeof item === "boolean"
                ? String(item)
                : JSON.stringify(item)}
            </span>
            {annotation[key] && <span>{annotation[key]}</span>}
          </div>
        ),
      });
    }
  });

  return tree;
};

export interface JSONPreviewProps {
  json: object;
  page?: string;
}
export const JsonPreview: FC<JSONPreviewProps> = ({ json, page }) => {
  const treeData: TreeDataNode[] = generateData(json, "0", page);

  return (
    <ConfigProvider
      theme={{
        token: {
          fontSize: 14,
          colorBgContainer: "var(--background)",
          colorText: "var(--foreground)",
        },
      }}
    >
      <Tree
        blockNode
        showIcon
        defaultExpandedKeys={["0-0", "0-1", "0-2", "0-3"]}
        selectable={false}
        showLine={page !== "threads"}
        switcherIcon={(props) =>
          props.expanded ? (
            <LuChevronDownIcon />
          ) : (
            <LuChevronDownIcon className="-rotate-90" />
          )
        }
        treeData={treeData}
      />
    </ConfigProvider>
  );
};
