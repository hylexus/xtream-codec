import clsx from "clsx";
import { ReactNode } from "react";

import { PageShell } from "@/components/ui/page-shell.tsx";

/** @deprecated 请使用 PageShell；保留以兼容旧代码 */
export type PageHeaderProps = {
  title: string;
  description?: string;
  className?: string;
  children?: ReactNode;
};

export function PageHeader({
  title,
  description,
  className,
  children,
}: PageHeaderProps) {
  return (
    <header className={clsx("dashboard-page-header", className)}>
      <h2 className="dashboard-page-title">{title}</h2>
      {description ? (
        <p className="dashboard-page-description">{description}</p>
      ) : null}
      {children}
    </header>
  );
}

export type PageSectionProps = {
  title: string;
  description?: string;
  className?: string;
  children: ReactNode;
};

/** 列表页标准外壳 — 标题 + 描述 + 内容，间距遵循 DESIGN_GUIDE */
export function PageSection({
  title,
  description,
  className,
  children,
}: PageSectionProps) {
  return (
    <PageShell
      className={className}
      description={description}
      title={title}
    >
      {children}
    </PageShell>
  );
}
