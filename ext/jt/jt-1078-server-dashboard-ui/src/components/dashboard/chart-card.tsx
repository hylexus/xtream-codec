import { ReactNode } from "react";

import {
  DashboardCard,
  DashboardCardBody,
  DashboardCardHeader,
} from "@/components/ui/dashboard-card.tsx";

type ChartCardProps = {
  title: string;
  subtitle?: string;
  legend?: ReactNode;
  actions?: ReactNode;
  children: ReactNode;
  className?: string;
  bodyClassName?: string;
};

export function ChartCard({
  title,
  subtitle,
  legend,
  actions,
  children,
  className,
  bodyClassName,
}: ChartCardProps) {
  return (
    <DashboardCard className={className}>
      <DashboardCardHeader
        actions={actions}
        legend={legend}
        subtitle={subtitle}
        title={title}
      />
      <DashboardCardBody className={bodyClassName ?? "h-52 p-4 sm:h-56"}>
        {children}
      </DashboardCardBody>
    </DashboardCard>
  );
}
