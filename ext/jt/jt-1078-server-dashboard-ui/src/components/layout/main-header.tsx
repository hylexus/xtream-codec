import { Button } from "@heroui/react";
import { Bell, Menu, PanelLeft, Plus, Search } from "lucide-react";
import { useLocation } from "react-router-dom";

import { DashboardGreeting } from "@/components/dashboard/greeting.tsx";
import { ThemeSwitch } from "@/components/theme-switch";
import { metaForPath } from "@/config/routes.ts";

type MainHeaderProps = {
  onOpenMobileNav: () => void;
  onToggleSidebar: () => void;
};

export function MainHeader({
  onOpenMobileNav,
  onToggleSidebar,
}: MainHeaderProps) {
  const { pathname } = useLocation();
  const meta = metaForPath(pathname);

  return (
    <div className="flex flex-wrap items-start justify-between gap-4 pb-6 md:items-center">
      <div className="flex min-w-0 flex-1 items-center gap-3">
        <Button
          isIconOnly
          aria-label="打开菜单"
          className="md:hidden"
          size="sm"
          variant="ghost"
          onPress={onOpenMobileNav}
        >
          <Menu className="size-5" strokeWidth={1.75} />
        </Button>
        <Button
          isIconOnly
          aria-label="收起侧栏"
          className="hidden md:flex"
          size="sm"
          variant="ghost"
          onPress={onToggleSidebar}
        >
          <PanelLeft className="size-5" strokeWidth={1.75} />
        </Button>
        <div className="min-w-0 flex-1">
          {meta.showGreeting ? (
            <DashboardGreeting />
          ) : (
            <>
              <h1 className="truncate text-xl font-semibold tracking-tight text-foreground sm:text-2xl">
                {meta.title}
              </h1>
              {meta.description ? (
                <p className="mt-1 line-clamp-2 text-sm text-muted">
                  {meta.description}
                </p>
              ) : null}
            </>
          )}
        </div>
      </div>

      <div className="flex shrink-0 items-center gap-1 sm:gap-2">
        <Button isIconOnly aria-label="搜索" size="sm" variant="ghost">
          <Search className="size-[1.125rem] text-muted" strokeWidth={1.75} />
        </Button>
        <Button
          isIconOnly
          aria-label="通知"
          className="hidden sm:flex"
          size="sm"
          variant="ghost"
        >
          <Bell className="size-[1.125rem] text-muted" strokeWidth={1.75} />
        </Button>
        <ThemeSwitch />
        <Button className="hidden sm:flex" size="sm" variant="primary">
          <span className="flex items-center gap-1.5">
            <Plus className="size-4" strokeWidth={2} />
            邀请
          </span>
        </Button>
        <Button
          isIconOnly
          aria-label="邀请"
          className="sm:hidden"
          size="sm"
          variant="primary"
        >
          <Plus className="size-4" strokeWidth={2} />
        </Button>
      </div>
    </div>
  );
}
