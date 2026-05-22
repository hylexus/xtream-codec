import clsx from "clsx";

import { CountNumber } from "@/components/dashboard/dashboard-widgets.tsx";
import { CHART_TCP, CHART_UDP } from "@/components/dashboard/chart-theme.ts";
import {
  dashboardMetaSm,
  dashboardMetricMd,
} from "@/components/ui/dashboard-typography.tsx";

function LegendDot({ color, label }: { color: string; label: string }) {
  return (
    <span className={clsx("flex items-center gap-1.5", dashboardMetaSm)}>
      <span
        className="size-2 rounded-full"
        style={{ backgroundColor: color }}
      />
      {label}
    </span>
  );
}

export function ProtocolChartLegend() {
  return (
    <div className="flex items-center gap-3">
      <LegendDot color={CHART_TCP} label="TCP" />
      <LegendDot color={CHART_UDP} label="UDP" />
    </div>
  );
}

export function TcpUdpFooter({ tcp, udp }: { tcp: number; udp: number }) {
  return (
    <div className={clsx("flex items-center justify-between", dashboardMetaSm)}>
      <span>
        TCP{" "}
        <span className={dashboardMetricMd}>
          <CountNumber end={tcp} />
        </span>
      </span>
      <span>
        UDP{" "}
        <span className={dashboardMetricMd}>
          <CountNumber end={udp} />
        </span>
      </span>
    </div>
  );
}
