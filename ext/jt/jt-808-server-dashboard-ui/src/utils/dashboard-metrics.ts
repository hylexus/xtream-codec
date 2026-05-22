import { Metrics } from "@/types";
import { getKeyValue } from "@/utils/get-key-value.ts";

export type MetricSlice = {
  current?: number;
  max?: number;
  total?: number;
  details?: unknown;
};

export const SESSION_METRIC_KEYS = [
  {
    key: "tcpInstructionSession",
    protocolType: "TCP",
    serverRole: "指令服务器",
  },
  {
    key: "tcpAttachmentSession",
    protocolType: "TCP",
    serverRole: "附件服务器",
  },
  {
    key: "udpInstructionSession",
    protocolType: "UDP",
    serverRole: "指令服务器",
  },
  {
    key: "udpAttachmentSession",
    protocolType: "UDP",
    serverRole: "附件服务器",
  },
] as const;

export const REQUEST_METRIC_KEYS = [
  {
    key: "tcpInstructionRequest",
    protocolType: "TCP",
    serverRole: "指令服务器",
  },
  {
    key: "tcpAttachmentRequest",
    protocolType: "TCP",
    serverRole: "附件服务器",
  },
  {
    key: "udpInstructionRequest",
    protocolType: "UDP",
    serverRole: "指令服务器",
  },
  {
    key: "udpAttachmentRequest",
    protocolType: "UDP",
    serverRole: "附件服务器",
  },
] as const;

export const CHANNEL_METRIC_ROWS = SESSION_METRIC_KEYS.map(
  (session, index) => ({
    session,
    request: REQUEST_METRIC_KEYS[index],
  }),
);

export function channelShortLabel(session: {
  protocolType: string;
  serverRole: string;
}) {
  const role = session.serverRole === "附件服务器" ? "附件" : "指令";

  return `${session.protocolType} · ${role}`;
}

export function asMetricSlice(value: unknown): MetricSlice {
  if (value != null && typeof value === "object") {
    return value as MetricSlice;
  }

  return {};
}

export function sliceForKey(metrics: Metrics, key: string): MetricSlice {
  return asMetricSlice(getKeyValue(metrics, key));
}

export function sumSessionCurrent(
  metrics: Metrics,
  protocol?: "TCP" | "UDP",
): number {
  return SESSION_METRIC_KEYS.filter(
    (item) => !protocol || item.protocolType === protocol,
  ).reduce(
    (sum, item) => sum + (sliceForKey(metrics, item.key).current ?? 0),
    0,
  );
}

export function sumRequestTotal(metrics: Metrics): number {
  return REQUEST_METRIC_KEYS.reduce(
    (sum, item) => sum + (sliceForKey(metrics, item.key).total ?? 0),
    0,
  );
}

export function subscriberTotal(metrics: Metrics): number {
  return (
    Number(
      getKeyValue(
        (metrics.eventPublisher?.subscriber ?? {}) as object,
        "total",
      ),
    ) || 0
  );
}

export function threadLiveCount(metrics: Metrics): number | null {
  const threads = metrics.threads;

  if (threads == null || typeof threads !== "object") {
    return null;
  }

  const live = (threads as { live?: number }).live;

  return typeof live === "number" ? live : null;
}

export type TrafficChartPoint = {
  date: string;
  tcp: number;
  udp: number;
};

/** 流量趋势图单帧：各协议活跃会话合计 */
export function trafficChartPoint(
  metrics: Metrics,
  time: string,
): TrafficChartPoint {
  return {
    date: time ? time.slice(11, 19) : "",
    tcp: sumSessionCurrent(metrics, "TCP"),
    udp: sumSessionCurrent(metrics, "UDP"),
  };
}

export function threadChartPoint(metrics: Metrics, time: string) {
  const empty = {
    date: time.slice(11, 19),
    started: 0,
    peak: 0,
    live: 0,
    daemon: 0,
  };

  if (metrics.threads == null || typeof metrics.threads !== "object") {
    return empty;
  }

  return { ...empty, ...metrics.threads };
}
