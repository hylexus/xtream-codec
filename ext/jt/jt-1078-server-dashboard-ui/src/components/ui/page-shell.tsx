import clsx from "clsx";
import { ReactNode } from "react";

import { DashboardGreeting } from "@/components/dashboard/greeting.tsx";

type PageShellProps = {
  /** 列表页等使用标题；仪表盘首页用 greeting 时可省略 */
  title?: string;
  description?: string;
  /** 仅仪表盘首页为 true */
  showGreeting?: boolean;
  toolbar?: ReactNode;
  children: ReactNode;
  className?: string;
};

/** 页面统一外层：间距、标题层级与 DESIGN_GUIDE 一致 */
export function PageShell({
  title,
  description,
  showGreeting = false,
  toolbar,
  children,
  className,
}: PageShellProps) {
  return (
    <div className={clsx("dashboard-page", className)}>
      {showGreeting ? <DashboardGreeting /> : null}
      {!showGreeting && title ? (
        <header className="dashboard-page-header">
          <h2 className="dashboard-page-title">{title}</h2>
          {description ? (
            <p className="dashboard-page-description">{description}</p>
          ) : null}
        </header>
      ) : null}
      {toolbar}
      {children}
    </div>
  );
}
