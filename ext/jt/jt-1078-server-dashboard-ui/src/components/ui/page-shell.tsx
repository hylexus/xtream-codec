import clsx from "clsx";
import { ReactNode } from "react";

type PageShellProps = {
  toolbar?: ReactNode;
  children: ReactNode;
  className?: string;
};

/** 页面内容区：标题由 layout MainHeader 统一展示 */
export function PageShell({ toolbar, children, className }: PageShellProps) {
  return (
    <div className={clsx("dashboard-page", className)}>
      {toolbar}
      {children}
    </div>
  );
}
