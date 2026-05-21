import { Avatar, Button } from "@heroui/react";
import clsx from "clsx";
import { LifeBuoy, LogOut, PanelLeftClose, PanelLeft } from "lucide-react";
import { NavLink, useLocation } from "react-router-dom";

import { ExternalLink } from "@/components/external-link.tsx";
import { LuChevronRightIcon } from "@/components/icons";
import { sidebarFooterLinks, siteConfig } from "@/config/site.ts";

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
        "dashboard-sidebar relative z-50 transition-[width] duration-200 ease-out",
        "fixed left-0 top-0 md:static",
        "md:translate-x-0",
        mobileOpen
          ? "translate-x-0 shadow-lg"
          : "-translate-x-full md:translate-x-0",
      )}
    >
      <div className="flex items-center justify-end gap-1 px-2 pt-3 md:hidden">
        <Button
          isIconOnly
          aria-label="关闭菜单"
          size="sm"
          variant="ghost"
          onPress={onCloseMobile}
        >
          <PanelLeftClose className="size-5" strokeWidth={1.75} />
        </Button>
      </div>

      <div
        className={clsx(
          "flex flex-1 flex-col px-4 pb-6 pt-4",
          compact ? "items-center px-2" : "",
        )}
      >
        <div
          className={clsx(
            "mb-6 flex w-full items-center gap-3",
            compact ? "justify-center" : "",
          )}
        >
          <div className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-accent text-sm font-bold text-accent-foreground">
            {siteConfig.name.slice(0, 1)}
          </div>
          {!compact ? (
            <span className="truncate text-sm font-semibold text-foreground">
              {siteConfig.name}
            </span>
          ) : null}
        </div>

        <nav aria-label="主导航" className="flex flex-col gap-1">
          {siteConfig.sidenav.map((link) => {
            const Icon = link.icon;
            const isOn = pathMatches(pathname, link.href);

            return (
              <NavLink
                key={link.href}
                className={clsx(
                  "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
                  compact ? "justify-center px-2" : "",
                  isOn
                    ? "bg-default text-default-foreground"
                    : "text-muted hover:bg-default/60 hover:text-foreground",
                )}
                to={link.href}
                onClick={() => {
                  if (window.matchMedia("(max-width: 767px)").matches) {
                    onCloseMobile();
                  }
                }}
              >
                <Icon className="size-[1.125rem] shrink-0" strokeWidth={1.75} />
                {!compact ? (
                  <span className="truncate">{link.name}</span>
                ) : null}
              </NavLink>
            );
          })}
        </nav>

        <div
          className={clsx(
            "mt-auto flex flex-col gap-1 border-t border-separator pt-4",
            compact ? "items-center" : "",
          )}
        >
          {sidebarFooterLinks.map((link) => {
            const Icon = link.icon;

            return (
              <ExternalLink
                key={link.href}
                unstyled
                className={clsx(
                  "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-muted transition-colors hover:bg-default/60 hover:text-foreground",
                  compact ? "justify-center px-2" : "",
                )}
                href={link.href}
              >
                <Icon className="size-[1.125rem] shrink-0" strokeWidth={1.75} />
                {!compact ? <span>{link.name}</span> : null}
              </ExternalLink>
            );
          })}
          <ExternalLink
            unstyled
            className={clsx(
              "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-muted transition-colors hover:bg-default/60 hover:text-foreground",
              compact ? "justify-center px-2" : "",
            )}
            href={siteConfig.links.docs}
          >
            <LifeBuoy className="size-[1.125rem] shrink-0" strokeWidth={1.75} />
            {!compact ? <span>帮助与文档</span> : null}
          </ExternalLink>

          <div
            className={clsx(
              "mt-2 flex items-center gap-3 rounded-lg px-2 py-2",
              compact ? "justify-center" : "",
            )}
          >
            <Avatar className="size-9 shrink-0">
              <Avatar.Fallback className="text-xs font-semibold">
                {siteConfig.user.name.slice(0, 2)}
              </Avatar.Fallback>
            </Avatar>
            {!compact ? (
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium text-foreground">
                  {siteConfig.user.name}
                </p>
                <p className="truncate text-xs text-muted">
                  {siteConfig.user.email}
                </p>
              </div>
            ) : null}
          </div>

          <button
            className={clsx(
              "flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-muted transition-colors hover:bg-default/60 hover:text-foreground",
              compact ? "justify-center px-2" : "",
            )}
            type="button"
          >
            <LogOut className="size-[1.125rem] shrink-0" strokeWidth={1.75} />
            {!compact ? <span>退出登录</span> : null}
          </button>
        </div>
      </div>

      <Button
        isIconOnly
        aria-label={compact ? "展开侧栏" : "收起侧栏"}
        className="absolute right-[-11px] top-28 z-20 hidden md:flex"
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
