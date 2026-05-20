import { ComponentProps, ReactNode } from "react";

import { DashboardCard } from "@/components/ui/dashboard-card.tsx";

/** @deprecated 请使用 DashboardCard；保留别名以兼容旧引用 */
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
