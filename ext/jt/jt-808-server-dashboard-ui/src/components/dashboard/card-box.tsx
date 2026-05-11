import {
  Accordion,
  Button,
  Card,
  Chip,
  Link,
  Popover,
  Tabs,
} from "@heroui/react";
import { useEffect, useState } from "react";
import {
  EventSourceMessage,
  fetchEventSource,
} from "@microsoft/fetch-event-source";
import { useRouteLoaderData } from "react-router-dom";

import { CountNumber } from "./count-number.tsx";
import { CountTime } from "./count-time.tsx";
import { SpotlightCard } from "./spolight-card.tsx";

import { Metrics, ServerInfo } from "@/types";
import { FaServerIcon } from "@/components/icons.tsx";
import { ThreadsCharts } from "@/components/dashboard/threads-charts.tsx";
import { MsgMiniTable } from "@/components/dashboard/msg-mini-table.tsx";
import { getKeyValue } from "@/utils/get-key-value.ts";

type MetricSlice = {
  current?: number;
  max?: number;
  total?: number;
  details?: unknown;
};

const asMetricSlice = (value: unknown): MetricSlice => {
  if (value != null && typeof value === "object") {
    return value as MetricSlice;
  }

  return {};
};

export const CardBox = () => {
  const { config } = useRouteLoaderData("root") as { config: ServerInfo };
  const [data, setData] = useState<{ time: string; value: Metrics }>({
    time: "",
    value: {},
  });

  const listCount = [
    {
      key: "tcpInstructionSession",
      name: "808服务会话数",
      protocolType: "TCP",
      serverRole: "指令服务器",
    },
    {
      key: "tcpAttachmentSession",
      name: "附件服务会话数",
      protocolType: "TCP",
      serverRole: "附件服务器",
    },
    {
      key: "udpInstructionSession",
      name: "808服务会话数",
      protocolType: "UDP",
      serverRole: "指令服务器",
    },
    {
      key: "udpAttachmentSession",
      name: "附件服务会话数",
      protocolType: "UDP",
      serverRole: "附件服务器",
    },
  ];
  const listRequest = [
    {
      key: "tcpInstructionRequest",
      name: "808服务请求数",
      protocolType: "TCP",
      serverRole: "指令服务器",
    },
    {
      key: "tcpAttachmentRequest",
      name: "附件服务请求数",
      protocolType: "TCP",
      serverRole: "附件服务器",
    },
    {
      key: "udpInstructionRequest",
      name: "808服务请求数",
      protocolType: "UDP",
      serverRole: "指令服务器",
    },
    {
      key: "udpAttachmentRequest",
      name: "附件服务请求数",
      protocolType: "UDP",
      serverRole: "附件服务器",
    },
  ];

  useEffect(() => {
    const ctrl = new AbortController();

    fetchEventSource(`${import.meta.env.VITE_API_DASHBOARD_V1}metrics/basic`, {
      method: "GET",
      signal: ctrl.signal,
      onmessage: (event: EventSourceMessage) => {
        setData(JSON.parse(event.data));
      },
    }).then(() => {
      // TODO
    });

    return () => {
      ctrl.abort();
    };
  }, []);

  return (
    <>
      <div className="mb-8">
        <h2 className="text-2xl font-semibold tracking-tight text-foreground">
          概览
        </h2>
        <p className="mt-1 max-w-2xl text-sm leading-relaxed text-muted">
          服务器、会话与请求指标的实时视图；配色随浅色 / 深色主题自动切换。
        </p>
      </div>
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-3 sm:gap-6">
        <SpotlightCard>
          <Card.Header className="text-sm font-semibold text-foreground">
            服务器信息
          </Card.Header>
          <Card.Content className="min-h-48 overflow-visible p-4">
            <Tabs className="w-full">
              <Tabs.ListContainer>
                <Tabs.List aria-label="服务器信息">
                  <Tabs.Tab id="version">版本</Tabs.Tab>
                  <Tabs.Tab id="java">java</Tabs.Tab>
                  <Tabs.Tab id="os">os</Tabs.Tab>
                </Tabs.List>
              </Tabs.ListContainer>
              <Tabs.Panel className="pt-2" id="version">
                <p className="text-xl text-muted">
                  {config.dependencies?.xtreamCodec?.version}
                </p>
              </Tabs.Panel>
              <Tabs.Panel className="pt-2" id="java">
                <Accordion>
                  <Accordion.Item id="java-info">
                    <Accordion.Heading>
                      <Accordion.Trigger className="flex w-full items-center justify-between gap-2">
                        <span>version: {config.java.version}</span>
                        <Accordion.Indicator />
                      </Accordion.Trigger>
                    </Accordion.Heading>
                    <Accordion.Panel>
                      <pre>
                        {JSON.stringify(config.java, null, 2)
                          .replace(/["{},]/g, "")
                          .replace(/\n {2}\n/g, "\n")}
                      </pre>
                    </Accordion.Panel>
                  </Accordion.Item>
                </Accordion>
              </Tabs.Panel>
              <Tabs.Panel className="pt-2" id="os">
                {Object.keys(config.os).map((key) => (
                  <p key={key}>
                    {key}: {String(getKeyValue(config.os, key))}
                  </p>
                ))}
              </Tabs.Panel>
            </Tabs>
          </Card.Content>
        </SpotlightCard>
        <SpotlightCard>
          <Card.Content className="min-h-48 overflow-visible p-4">
            <p>服务启动时间</p>
            <p className="text-xl text-muted">{config.serverStartupTime}</p>
            <div className="h-4" />
            <p>运行时间</p>
            <div className="text-xl text-muted">
              <CountTime start={new Date(config.serverStartupTime)} />
            </div>
          </Card.Content>
        </SpotlightCard>
        <SpotlightCard>
          <Card.Content className="min-h-48 overflow-visible p-4">
            <p>订阅者</p>
            <div className="flex justify-between">
              <p className="text-2xl text-muted">
                <CountNumber
                  end={
                    Number(
                      getKeyValue(
                        (data.value.eventPublisher?.subscriber ?? {}) as object,
                        "total",
                      ),
                    ) || 0
                  }
                />
              </p>
              <Link
                className="text-sm font-medium text-accent underline-offset-4 hover:underline"
                href="/subscriber"
              >
                详情
              </Link>
            </div>
          </Card.Content>
        </SpotlightCard>
      </div>
      <div className="h-2 sm:h-4" />
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4 sm:gap-5">
        {listCount.map((item, index) => (
          <SpotlightCard key={index} className="min-h-32">
            <Card.Header className="text-small flex flex-wrap items-center gap-2 text-muted">
              <span className="font-semibold text-foreground">会话数</span>
              <FaServerIcon />
              <Chip
                color={item.serverRole === "附件服务器" ? "warning" : "success"}
                size="sm"
                variant="soft"
              >
                {item.serverRole === "附件服务器" ? "附件" : "指令"}
              </Chip>
              <Chip
                color={item.protocolType === "TCP" ? "accent" : "default"}
                size="sm"
                variant="soft"
              >
                {item.protocolType}
              </Chip>
            </Card.Header>
            <Card.Content className="overflow-visible p-4">
              <p className="text-muted">
                当前:{" "}
                <CountNumber
                  end={
                    asMetricSlice(getKeyValue(data.value, item.key))?.current ??
                    0
                  }
                />
              </p>
              <span className="font-semibold text-foreground">
                峰值:{" "}
                {asMetricSlice(getKeyValue(data.value, item.key)).max ?? "—"}
              </span>
            </Card.Content>
          </SpotlightCard>
        ))}
        {listRequest.map((item, index) => (
          <SpotlightCard key={index} className="min-h-32">
            <Card.Header className="text-small flex flex-wrap items-center gap-2 text-muted">
              <span className="font-semibold text-foreground">请求数</span>
              <FaServerIcon />
              <Chip
                color={item.serverRole === "附件服务器" ? "warning" : "success"}
                size="sm"
                variant="soft"
              >
                {item.serverRole === "附件服务器" ? "附件" : "指令"}
              </Chip>
              <Chip
                color={item.protocolType === "TCP" ? "accent" : "default"}
                size="sm"
                variant="soft"
              >
                {item.protocolType}
              </Chip>
            </Card.Header>
            <Card.Content className="flex overflow-visible p-4">
              <div className="flex items-center justify-between">
                <p>总请求数:</p>
                <CountNumber
                  end={
                    asMetricSlice(getKeyValue(data.value, item.key)).total ?? 0
                  }
                />
                <div className="w-4 shrink-0" />
                {(asMetricSlice(getKeyValue(data.value, item.key)).total ?? 0) >
                  0 && (
                  <Popover>
                    <Popover.Trigger>
                      <Button variant="ghost">详情</Button>
                    </Popover.Trigger>
                    <Popover.Content className="p-2">
                      <MsgMiniTable
                        data={
                          asMetricSlice(getKeyValue(data.value, item.key))
                            .details
                        }
                      />
                    </Popover.Content>
                  </Popover>
                )}
              </div>
            </Card.Content>
          </SpotlightCard>
        ))}
      </div>
      <div className="h-2 sm:h-4" />
      <div className="grid grid-cols-1 gap-4 sm:gap-5">
        <SpotlightCard>
          <Card.Header className="text-sm font-semibold text-foreground">
            线程
          </Card.Header>
          <Card.Content className="h-96">
            <ThreadsCharts
              data={{
                date: data.time ? data.time.slice(11, 19) : "",
                ...(typeof data.value.threads === "object" &&
                data.value.threads !== null
                  ? data.value.threads
                  : {}),
              }}
            />
          </Card.Content>
        </SpotlightCard>
      </div>
    </>
  );
};
