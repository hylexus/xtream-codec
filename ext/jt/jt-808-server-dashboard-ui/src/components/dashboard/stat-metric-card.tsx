import { Chip } from "@heroui/react";
import { ReactNode } from "react";
import clsx from "clsx";

import { CountNumber } from "@/components/dashboard/count-number.tsx";
import {
  DashboardCard,
  DashboardCardBody,
  DashboardCardHeader,
} from "@/components/ui/dashboard-card.tsx";
import {
  dashboardLabel,
  dashboardValueSm,
} from "@/components/ui/dashboard-typography.tsx";

type StatMetricCardProps = {
  title: string;
  badges?: ReactNode;
  current?: number;
  max?: number | string;
  total?: number;
  totalLabel?: string;
  actions?: ReactNode;
  className?: string;
};

/** Pro 风格会话/请求统计卡 — 标题行 + 当前/峰值分层配色 */
export function StatMetricCard({
  title,
  badges,
  current,
  max,
  total,
  totalLabel = "总请求数",
  actions,
  className,
}: StatMetricCardProps) {
  const showTotal = total !== undefined;
  const showPeak = max !== undefined && !showTotal;

  return (
    <DashboardCard className={clsx("min-h-0", className)}>
      <DashboardCardHeader
        title={
          <span className="flex flex-wrap items-center gap-2">
            <span>{title}</span>
            {badges}
          </span>
        }
      />
      <DashboardCardBody className="flex flex-col gap-4 p-6">
        {showTotal ? (
          <div className="flex flex-wrap items-end justify-between gap-2">
            <div>
              <p className={dashboardLabel}>{totalLabel}</p>
              <p className={dashboardValueSm}>
                <CountNumber end={total} />
              </p>
            </div>
            {actions}
          </div>
        ) : (
          <>
            <div>
              <p className={dashboardLabel}>当前</p>
              <p className={dashboardValueSm}>
                <CountNumber end={current ?? 0} />
              </p>
            </div>
            {showPeak ? (
              <div>
                <p className={dashboardLabel}>峰值</p>
                <p className="text-base font-semibold tabular-nums text-foreground">
                  {max ?? "—"}
                </p>
              </div>
            ) : null}
            {actions ? (
              <div className="flex justify-end">{actions}</div>
            ) : null}
          </>
        )}
      </DashboardCardBody>
    </DashboardCard>
  );
}

type SessionStatBadgesProps = {
  serverRole: string;
  protocolType: string;
};

export function SessionStatBadges({
  serverRole,
  protocolType,
}: SessionStatBadgesProps) {
  const isAttachment = serverRole === "附件服务器";

  return (
    <>
      <Chip color={isAttachment ? "warning" : "success"} size="sm" variant="soft">
        {isAttachment ? "附件" : "指令"}
      </Chip>
      <Chip
        color={protocolType === "TCP" ? "accent" : "default"}
        size="sm"
        variant="soft"
      >
        {protocolType}
      </Chip>
    </>
  );
}
