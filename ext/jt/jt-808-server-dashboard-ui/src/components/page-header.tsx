import clsx from "clsx";
import { ReactNode } from "react";

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
    <div className={clsx("shrink-0", className)}>
      <h2 className="text-2xl font-semibold tracking-tight text-foreground">
        {title}
      </h2>
      {description ? (
        <p className="mt-1 max-w-3xl text-sm leading-relaxed text-muted">
          {description}
        </p>
      ) : null}
      {children}
    </div>
  );
}

export type PageSectionProps = {
  title: string;
  description?: string;
  className?: string;
  children: ReactNode;
};

/** 常规内容页：标题 + 说明 + 主体，统一间距 */
export function PageSection({
  title,
  description,
  className,
  children,
}: PageSectionProps) {
  return (
    <div className={clsx("flex flex-col gap-6", className)}>
      <PageHeader description={description} title={title} />
      {children}
    </div>
  );
}
