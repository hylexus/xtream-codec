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

export function sessionBarValues(metrics: Metrics): number[] {
  return SESSION_METRIC_KEYS.map(
    (item) => sliceForKey(metrics, item.key).current ?? 0,
  );
}
