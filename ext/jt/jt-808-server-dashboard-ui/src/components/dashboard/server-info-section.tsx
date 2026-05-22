import clsx from "clsx";

import { CountTime } from "@/components/dashboard/dashboard-widgets.tsx";
import {
  DashboardCard,
  DashboardCardBody,
  DashboardCardHeader,
} from "@/components/ui/dashboard-card.tsx";
import {
  dashboardLabel,
  dashboardMeta,
  dashboardValueSm,
} from "@/components/ui/dashboard-typography.tsx";
import { ServerInfo } from "@/types";

/** 单行摘要，减少首页纵向占用 */
export function ServerInfoSection({ config }: { config: ServerInfo }) {
  return (
    <DashboardCard className="h-auto">
      <DashboardCardHeader className="py-2" title="运行环境" />
      <DashboardCardBody className="px-4 py-3">
        <div className="grid grid-cols-2 gap-x-4 gap-y-3 sm:grid-cols-4">
          <div className="min-w-0">
            <p className={dashboardLabel}>Codec</p>
            <p className={clsx(dashboardValueSm, "mt-1 truncate")}>
              {config.dependencies?.xtreamCodec?.version ?? "—"}
            </p>
          </div>
          <div className="min-w-0">
            <p className={dashboardLabel}>Java</p>
            <p className={clsx(dashboardValueSm, "mt-1 truncate")}>
              {config.java.version}
            </p>
          </div>
          <div className="min-w-0">
            <p className={dashboardLabel}>系统</p>
            <p className={clsx(dashboardValueSm, "mt-1 truncate text-base")}>
              {config.os.name}
            </p>
            <p className={dashboardMeta}>{config.os.arch}</p>
          </div>
          <div className="min-w-0">
            <p className={dashboardLabel}>运行</p>
            <p className={clsx(dashboardValueSm, "mt-1 tabular-nums")}>
              <CountTime start={new Date(config.serverStartupTime)} />
            </p>
            <p className={dashboardMeta}>自 {config.serverStartupTime}</p>
          </div>
        </div>
      </DashboardCardBody>
    </DashboardCard>
  );
}
