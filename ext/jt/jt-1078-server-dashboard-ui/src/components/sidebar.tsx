import { Avatar, Button, Chip } from "@heroui/react";
import clsx from "clsx";
import { LifeBuoy, LogOut, PanelLeftClose, PanelLeft } from "lucide-react";
import { NavLink, useLocation } from "react-router-dom";

import { ExternalLink } from "@/components/external-link.tsx";
import { LuChevronRightIcon } from "@/components/icons";
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
          "flex flex-1 flex-col px-3 pb-4 pt-3 md:px-4 md:pb-6 md:pt-5",
          compact ? "items-center px-2" : "",
        )}
      >
        <div
          className={clsx(
            "mb-6 flex w-full items-center gap-3",
            compact ? "justify-center" : "",
          )}
        >
          <Avatar className="h-10 w-10 shrink-0 border-0">
            <Avatar.Fallback className="dashboard-profile-gradient text-sm font-semibold text-white">
              {siteConfig.user.name.slice(0, 1)}
            </Avatar.Fallback>
          </Avatar>
          {!compact ? (
            <div className="min-w-0 flex-1 text-left">
              <p className="truncate text-sm font-semibold text-foreground">
                {siteConfig.user.name}
              </p>
              <p className="truncate text-xs text-muted">{siteConfig.user.role}</p>
            </div>
          ) : null}
        </div>

        <nav aria-label="主导航" className="flex flex-col gap-0.5">
          {siteConfig.sidenav.map((link) => {
            const Icon = link.icon;
            const isOn = pathMatches(pathname, link.href);

            return (
              <NavLink
                key={link.href}
                className={clsx(
                  "flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors",
                  compact ? "justify-center px-2" : "",
                  isOn
                    ? "dashboard-nav-active"
                    : "text-muted hover:bg-background-tertiary hover:text-foreground",
                )}
                to={link.href}
                onClick={() => {
                  if (window.matchMedia("(max-width: 767px)").matches) {
                    onCloseMobile();
                  }
                }}
              >
                <Icon className="!text-current size-[1.15rem] shrink-0 [&>svg]:block" />
                {!compact ? (
                  <span className="flex flex-1 items-center justify-between gap-2">
                    <span className="truncate">{link.name}</span>
                    {link.href === "/subscribers" ? (
                      <Chip
                        className="h-5 min-h-5 px-1.5 text-[10px]"
                        color="success"
                        size="sm"
                        variant="soft"
                      >
                        Live
                      </Chip>
                    ) : null}
                  </span>
                ) : null}
              </NavLink>
            );
          })}
        </nav>

        <div
          className={clsx(
            "mt-auto flex flex-col gap-0.5 border-t border-separator pt-4",
            compact ? "items-center" : "",
          )}
        >
          <ExternalLink
            unstyled
            className={clsx(
              "flex items-center gap-2 rounded-xl px-3 py-2 text-sm text-muted transition-colors hover:bg-background-tertiary hover:text-foreground",
              compact ? "justify-center px-0" : "",
            )}
            href={siteConfig.links.docs}
          >
            <LifeBuoy className="size-4 shrink-0" strokeWidth={1.75} />
            {!compact ? <span>帮助与文档</span> : null}
          </ExternalLink>
          <button
            className={clsx(
              "flex w-full items-center gap-2 rounded-xl px-3 py-2 text-sm text-muted transition-colors hover:bg-background-tertiary hover:text-foreground",
              compact ? "justify-center px-0" : "",
            )}
            type="button"
          >
            <LogOut className="size-4 shrink-0" strokeWidth={1.75} />
            {!compact ? <span>退出登录</span> : null}
          </button>
        </div>
      </div>

      <Button
        isIconOnly
        aria-label={compact ? "展开侧栏" : "收起侧栏"}
        className="absolute right-[-11px] top-28 z-20 hidden h-8 w-8 min-w-8 rounded-full border border-border bg-surface text-default-foreground shadow-sm hover:bg-default dark:border-white/[0.08] dark:shadow-[inset_0_1px_0_0_rgb(255_255_255/0.07)] md:flex"
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
