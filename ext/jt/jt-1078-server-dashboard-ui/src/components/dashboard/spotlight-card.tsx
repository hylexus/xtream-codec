import { Card } from "@heroui/react";
import { ReactNode } from "react";

export const SpotlightCard = ({
  children,
  ...props
}: {
  children: ReactNode;
  className?: string;
}) => {
  return (
    <div className="h-full min-h-[1px] w-full">
      <Card
        className="dashboard-stat-card h-full rounded-2xl border-0 bg-surface shadow-none transition-[background-color,box-shadow] duration-150"
        {...props}
      >
        {children}
      </Card>
    </div>
  );
};
