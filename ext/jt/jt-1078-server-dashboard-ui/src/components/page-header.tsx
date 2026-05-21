import clsx from "clsx";
import { ReactNode } from "react";

import { PageShell } from "@/components/ui/page-shell.tsx";

/** @deprecated 标题由 MainHeader 展示 */
export type PageHeaderProps = {
  title: string;
  description?: string;
  className?: string;
  children?: ReactNode;
};

export function PageHeader({
  className,
  children,
}: PageHeaderProps) {
  return <div className={clsx(className)}>{children}</div>;
}

export type PageSectionProps = {
  title?: string;
  description?: string;
  className?: string;
  children: ReactNode;
};

/** 列表页内容外壳（标题/描述在 layout MainHeader） */
export function PageSection({ className, children }: PageSectionProps) {
  return <PageShell className={className}>{children}</PageShell>;
}
