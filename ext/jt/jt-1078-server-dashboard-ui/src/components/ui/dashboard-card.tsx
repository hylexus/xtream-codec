import { Card } from "@heroui/react";
import clsx from "clsx";
import { ComponentProps, ReactNode } from "react";

type DashboardCardProps = ComponentProps<typeof Card> & {
  children: ReactNode;
};

/** 仪表盘统一卡片容器 — 所有 KPI / 图表 / 表格外层必须使用 */
export function DashboardCard({
  children,
  className,
  ...props
}: DashboardCardProps) {
  return (
    <div className="h-full min-h-0 w-full">
      <Card className={clsx("dashboard-card h-full", className)} {...props}>
        {children}
      </Card>
    </div>
  );
}

type DashboardCardHeaderProps = {
  title: ReactNode;
  subtitle?: ReactNode;
  legend?: ReactNode;
  actions?: ReactNode;
  className?: string;
};

export function DashboardCardHeader({
  title,
  subtitle,
  legend,
  actions,
  className,
}: DashboardCardHeaderProps) {
  return (
    <div className={clsx("dashboard-card-header", className)}>
      <div className="min-w-0">
        <h3 className="text-sm font-semibold text-foreground">{title}</h3>
        {subtitle ? (
          <p className="dashboard-page-description mt-0.5">{subtitle}</p>
        ) : null}
        {legend ? <div className="mt-2">{legend}</div> : null}
      </div>
      {actions}
    </div>
  );
}

type DashboardCardBodyProps = {
  children: ReactNode;
  flush?: boolean;
  className?: string;
};

export function DashboardCardBody({
  children,
  flush,
  className,
}: DashboardCardBodyProps) {
  return (
    <div
      className={clsx(
        flush ? "dashboard-card-body--flush" : "dashboard-card-body",
        className,
      )}
    >
      {children}
    </div>
  );
}

type DashboardCardFooterProps = {
  children: ReactNode;
  className?: string;
};

export function DashboardCardFooter({
  children,
  className,
}: DashboardCardFooterProps) {
  return (
    <div className={clsx("dashboard-card-footer", className)}>{children}</div>
  );
}
