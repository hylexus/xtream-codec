import { Chip, Input } from "@heroui/react";
import { Search } from "lucide-react";
import { ReactNode } from "react";

import { ListTableToolbar } from "@/components/ui/list-table-toolbar.tsx";
import {
  DashboardCard,
  DashboardCardBody,
  DashboardCardFooter,
  DashboardCardHeader,
} from "@/components/ui/dashboard-card.tsx";

type TableCardProps = {
  title: string;
  total?: number;
  searchPlaceholder?: string;
  toolbar?: ReactNode;
  footer?: ReactNode;
  children: ReactNode;
};

export function TableCard({
  title,
  total,
  searchPlaceholder = "搜索...",
  toolbar,
  footer,
  children,
}: TableCardProps) {
  return (
    <DashboardCard>
      <DashboardCardHeader
        actions={
          <div className="flex flex-wrap items-center gap-2">
            {toolbar ?? <ListTableToolbar />}
            <div className="relative w-full min-w-[10rem] sm:w-48">
              <Search
                className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted"
                strokeWidth={1.75}
              />
              <Input
                aria-label="搜索"
                className="pl-9"
                placeholder={searchPlaceholder}
                variant="secondary"
              />
            </div>
          </div>
        }
        title={
          <span className="flex flex-wrap items-center gap-2">
            <span>{title}</span>
            {total != null ? (
              <Chip className="tabular-nums" size="sm" variant="soft">
                {total}
              </Chip>
            ) : null}
          </span>
        }
      />
      <DashboardCardBody flush>{children}</DashboardCardBody>
      {footer ? <DashboardCardFooter>{footer}</DashboardCardFooter> : null}
    </DashboardCard>
  );
}
