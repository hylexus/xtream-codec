import { Button } from "@heroui/react";
import confetti from "canvas-confetti";
import { Bell, Menu, PanelLeft, Search } from "lucide-react";
import { useLocation } from "react-router-dom";

import { ExternalLink } from "@/components/external-link.tsx";
import { DashboardGreeting } from "@/components/dashboard/dashboard-widgets.tsx";
import { GiteeIcon, GithubIcon, LuHeatIcon } from "@/components/icons";
import { ThemeSwitch } from "@/components/theme-switch";
import { metaForPath } from "@/config/routes.ts";
import { siteConfig } from "@/config/site";

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

  const handleSponsor = () => {
    confetti({
      particleCount: 80,
      spread: 90,
      origin: { y: 0.65 },
    });
    window.open(siteConfig.links.sponsor, "_blank", "noopener,noreferrer");
  };

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
        <Button
          isIconOnly
          aria-label="搜索"
          className="hidden sm:flex"
          size="sm"
          variant="ghost"
        >
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
        <ExternalLink
          unstyled
          className="inline-flex size-9 items-center justify-center rounded-lg text-muted transition-colors hover:bg-default/60 hover:text-foreground"
          href={siteConfig.links.github}
        >
          <GithubIcon size={20} />
        </ExternalLink>
        <ExternalLink
          unstyled
          className="inline-flex size-9 items-center justify-center rounded-lg text-muted transition-colors hover:bg-default/60 hover:text-foreground"
          href={siteConfig.links.gitee}
        >
          <GiteeIcon className="size-5" />
        </ExternalLink>
        <ThemeSwitch />
        <Button
          className="hidden sm:flex"
          size="sm"
          variant="primary"
          onPress={handleSponsor}
        >
          <span className="flex items-center gap-1.5">
            <LuHeatIcon className="size-4" />
            赞助
          </span>
        </Button>
        <Button
          isIconOnly
          aria-label="赞助"
          className="sm:hidden"
          size="sm"
          variant="primary"
          onPress={handleSponsor}
        >
          <LuHeatIcon className="size-4" />
        </Button>
      </div>
    </div>
  );
}
