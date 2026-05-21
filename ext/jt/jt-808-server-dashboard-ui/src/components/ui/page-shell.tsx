import clsx from "clsx";
import { ReactNode } from "react";

type PageShellProps = {
  toolbar?: ReactNode;
  children: ReactNode;
  className?: string;
};

export function PageShell({ toolbar, children, className }: PageShellProps) {
  return (
    <div className={clsx("dashboard-page", className)}>
      {toolbar}
      {children}
    </div>
  );
}
