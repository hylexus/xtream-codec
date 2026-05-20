import clsx from "clsx";
import { ComponentProps, ReactNode } from "react";

type ExternalLinkProps = ComponentProps<"a"> & {
  children: ReactNode;
  unstyled?: boolean;
};

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
