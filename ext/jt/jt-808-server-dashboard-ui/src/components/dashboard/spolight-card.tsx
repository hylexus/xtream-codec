import { ComponentProps, ReactNode } from "react";

import { DashboardCard } from "@/components/ui/dashboard-card.tsx";

/** @deprecated 请使用 DashboardCard */
export const SpotlightCard = ({
  children,
  className,
  ...props
}: {
  children: ReactNode;
  className?: string;
} & ComponentProps<typeof DashboardCard>) => {
  return (
    <DashboardCard className={className} {...props}>
      {children}
    </DashboardCard>
  );
};
