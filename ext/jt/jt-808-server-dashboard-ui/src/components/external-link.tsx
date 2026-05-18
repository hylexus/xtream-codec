import clsx from "clsx";
import { ComponentProps, ReactNode } from "react";

type ExternalLinkProps = ComponentProps<"a"> & {
  children: ReactNode;
  /** 仅图标/圆形按钮样式，不套用 `.link` 文本样式 */
  unstyled?: boolean;
};

/** 站外链接：原生 `<a>`，避免误走 SPA 路由 */
export function ExternalLink({
  className,
  unstyled,
  children,
  rel = "noopener noreferrer",
  target = "_blank",
  ...props
}: ExternalLinkProps) {
  return (
    <a
      className={clsx(!unstyled && "link", className)}
      rel={rel}
      target={target}
      {...props}
    >
      {children}
    </a>
  );
}
