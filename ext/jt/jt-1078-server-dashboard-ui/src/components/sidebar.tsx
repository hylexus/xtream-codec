import { Avatar, Button, Link } from "@heroui/react";
import clsx from "clsx";
import {
  ExternalLink,
  LifeBuoy,
  PanelLeftClose,
  PanelLeft,
} from "lucide-react";
import { NavLink, useLocation } from "react-router-dom";

import { LuChevronRightIcon, LogoIcon } from "@/components/icons";
import { siteConfig } from "@/config/site.ts";

function pathMatches(pathname: string, href: string): boolean {
  if (href === "/dashboard") {
    return pathname === "/" || pathname === "" || pathname === "/dashboard";
  }

  return pathname === href || pathname.startsWith(`${href}/`);
}

type SidebarProps = {
  compact: boolean;
  onToggleCompact: () => void;
  mobileOpen: boolean;
  onCloseMobile: () => void;
};

export const Sidebar = ({
  compact,
  onToggleCompact,
  mobileOpen,
  onCloseMobile,
}: SidebarProps) => {
  const { pathname } = useLocation();
  const asideWidth = compact ? "w-[76px]" : "w-[260px]";

  return (
    <aside
      className={clsx(
        asideWidth,
        "dashboard-sidebar relative z-50 flex h-full shrink-0 flex-col transition-[width] duration-200 ease-out",
        "md:translate-x-0",
        "fixed left-0 top-0 md:static",
        mobileOpen
          ? "translate-x-0 shadow-[0_24px_48px_-12px_rgb(0_0_0/0.65)] dark:shadow-[0_24px_48px_-12px_rgb(0_0_0/0.75)]"
          : "-translate-x-full md:translate-x-0",
      )}
    >
      <div className="flex items-center justify-end gap-1 px-2 pt-3 md:hidden">
        <Button
          isIconOnly
          aria-label="关闭菜单"
          className="text-muted hover:bg-background-tertiary hover:text-foreground"
          size="sm"
          variant="ghost"
          onPress={onCloseMobile}
        >
          <PanelLeftClose className="size-5" strokeWidth={1.75} />
        </Button>
      </div>

      <div
        className={clsx(
          "flex flex-col px-3 pb-4 pt-2 md:px-4 md:pb-6 md:pt-5",
          compact ? "items-center px-2" : "",
        )}
      >
        <Link
          className={clsx(
            "mb-6 flex w-full items-center gap-3 rounded-2xl px-2 py-2 ring-1 ring-black/5 transition-colors hover:bg-background-tertiary dark:ring-0 dark:shadow-[inset_0_1px_0_0_rgb(255_255_255/0.06)]",
            compact ? "justify-center" : "",
          )}
          href={siteConfig.links.github}
          rel="noopener noreferrer"
          target="_blank"
        >
          <Avatar className="h-11 w-11 shrink-0 border-0 bg-surface shadow-[inset_0_1px_0_0_rgb(255_255_255/0.06)]">
            <Avatar.Fallback className="bg-transparent">
              <LogoIcon className="mx-auto size-8" />
            </Avatar.Fallback>
          </Avatar>
          {!compact ? (
            <div className="min-w-0 flex-1 text-left">
              <p className="truncate text-sm font-semibold text-foreground">
                {siteConfig.name}
              </p>
              <p className="truncate text-xs text-muted">1078 监控控制台</p>
            </div>
          ) : null}
        </Link>

        <nav aria-label="主导航" className="flex flex-1 flex-col gap-1">
          {siteConfig.sidenav.map((link) => {
            const Icon = link.icon;
            const isOn = pathMatches(pathname, link.href);

            return (
              <NavLink
                key={link.href}
                className={clsx(
                  "flex items-center gap-3 rounded-2xl px-3 py-2.5 text-sm font-medium transition-colors",
                  compact ? "justify-center px-2" : "",
                  isOn
                    ? clsx("dashboard-nav-active", compact ? "compact-nav" : "")
                    : "text-muted hover:bg-background-tertiary hover:text-foreground",
                )}
                to={link.href}
                onClick={() => {
                  if (window.matchMedia("(max-width: 767px)").matches) {
                    onCloseMobile();
                  }
                }}
              >
                <Icon className="!text-current size-[1.15rem] shrink-0 opacity-90 [&>svg]:block" />
                {!compact ? (
                  <span className="truncate">{link.name}</span>
                ) : null}
              </NavLink>
            );
          })}
        </nav>

        <div
          className={clsx(
            "mt-6 border-t border-separator pt-4",
            compact
              ? "flex w-full flex-col items-center gap-2"
              : "flex flex-col gap-1",
          )}
        >
          <Link
            className={clsx(
              "flex items-center gap-2 rounded-2xl px-3 py-2 text-sm text-muted transition-colors hover:bg-background-tertiary hover:text-foreground",
              compact ? "justify-center px-0" : "",
            )}
            href={siteConfig.links.github}
            rel="noopener noreferrer"
            target="_blank"
          >
            <LifeBuoy className="size-4 shrink-0" strokeWidth={1.75} />
            {!compact ? <span>帮助与文档</span> : null}
          </Link>
          <Link
            className={clsx(
              "flex items-center gap-2 rounded-2xl px-3 py-2 text-sm text-muted transition-colors hover:bg-background-tertiary hover:text-foreground",
              compact ? "justify-center px-0" : "",
            )}
            href={siteConfig.links.sponsor}
            rel="noopener noreferrer"
            target="_blank"
          >
            <ExternalLink className="size-4 shrink-0" strokeWidth={1.75} />
            {!compact ? <span>赞助 / 仓库</span> : null}
          </Link>
        </div>
      </div>

      <Button
        isIconOnly
        aria-label={compact ? "展开侧栏" : "收起侧栏"}
        className="absolute right-[-11px] top-24 z-20 hidden h-8 w-8 min-w-8 rounded-full border border-border bg-surface text-default-foreground shadow-none ring-1 ring-black/5 hover:bg-default dark:border-white/[0.08] dark:ring-0 dark:shadow-[inset_0_1px_0_0_rgb(255_255_255/0.07)] md:flex"
        size="sm"
        variant="secondary"
        onPress={onToggleCompact}
      >
        {compact ? (
          <PanelLeft className="size-4" strokeWidth={1.75} />
        ) : (
          <LuChevronRightIcon className="rotate-180" size={14} />
        )}
      </Button>
    </aside>
  );
};
