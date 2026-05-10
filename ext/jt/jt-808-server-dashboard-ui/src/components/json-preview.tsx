import type { TreeDataNode } from "antd";

import { Tree, ConfigProvider } from "antd";
import { FC } from "react";
import clsx from "clsx";

import {
  FaChevronDownIcon,
  FaCircleCheckIcon,
  FaCubeIcon,
  FaHashtagIcon,
  FaQuoteRightIcon,
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
const isObject = (val: any) => {
  return typeof val === "object" && val !== null;
};
const valueColor = (item: string | number | boolean) => {
  switch (typeof item) {
    case "number":
      return "text-primary";
    case "boolean":
      return item ? "text-success" : "text-secondary";
    case "string":
      return "text-secondary";
  }
};
const valueIcon = (item: string | number | boolean) => {
  switch (typeof item) {
    case "number":
      return <FaHashtagIcon className="text-small" />;
    case "boolean":
      return <FaCircleCheckIcon className="text-small" />;
    case "string":
      return <FaQuoteRightIcon className="text-small" />;
  }
};
const generateData = (json: Object, _preKey: string, page?: string) => {
  const tree: TreeDataNode[] = [];

  Object.keys(json).forEach((key, index) => {
    const item: any = json[key as keyof Object];
    const curKey = _preKey + "-" + index;

    if (isObject(item)) {
      tree.push({
        key: curKey,
        icon: <FaCubeIcon className="text-small" />,
        title: (
          <div
            className={clsx(
              "inline-flex gap-x-12",
              page === "threads" ? "justify-between" : "justify-normal",
            )}
          >
            <span>{key}</span>
            {annotation[key] && (
              <span className="text-small text-default-300">
                {annotation[key]}
              </span>
            )}
          </div>
        ),
        children: generateData(item, curKey, page),
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
            <span className={valueColor(item)}>{String(item)}</span>
            {annotation[key] && <span>{annotation[key]}</span>}
          </div>
        ),
      });
    }
  });

  return tree;
};

export interface JSONPreviewProps {
  json: Object;
  page?: string;
}
export const JsonPreview: FC<JSONPreviewProps> = ({ json, page }) => {
  const treeData: TreeDataNode[] = generateData(json, "0", page);

  return (
    <ConfigProvider
      theme={{
        token: {
          // @ts-ignore
          fontSize: "0.875rem",
          colorBgContainer: "var(--background)",
          titleHeight: "1.5rem",
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
            <FaChevronDownIcon />
          ) : (
            <FaChevronDownIcon className="fa-rotate-270" />
          )
        }
        treeData={treeData}
      />
    </ConfigProvider>
  );
};
