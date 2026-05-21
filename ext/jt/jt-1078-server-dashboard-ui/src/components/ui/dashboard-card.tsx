import { Card } from "@heroui/react";
import clsx from "clsx";
import { ComponentProps, ReactNode } from "react";

type DashboardCardProps = ComponentProps<typeof Card> & {
  children: ReactNode;
};

/** 仪表盘卡片 — HeroUI Card 默认 variant，无额外主题覆盖 */
export function DashboardCard({
  children,
  className,
  ...props
}: DashboardCardProps) {
  return (
    <Card className={clsx("h-full", className)} variant="default" {...props}>
      {children}
    </Card>
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
    <Card.Header
      className={clsx(
        "flex flex-row flex-wrap items-center justify-between gap-3",
        className,
      )}
    >
      <div className="min-w-0 flex-1">
        <Card.Title className="text-sm font-semibold">{title}</Card.Title>
        {subtitle ? (
          <Card.Description className="mt-0.5">{subtitle}</Card.Description>
        ) : null}
        {legend ? <div className="mt-2">{legend}</div> : null}
      </div>
      {actions ? <div className="flex shrink-0 flex-wrap items-center gap-2">{actions}</div> : null}
    </Card.Header>
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
    <Card.Content className={clsx(flush && "p-0", className)}>
      {children}
    </Card.Content>
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
    <Card.Footer
      className={clsx(
        "flex flex-wrap items-center justify-between gap-3 border-t border-separator",
        className,
      )}
    >
      {children}
    </Card.Footer>
  );
}
