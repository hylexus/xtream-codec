import { Button } from "@heroui/react";
import confetti from "canvas-confetti";
import { Bell, Menu, Search } from "lucide-react";

import { ExternalLink } from "@/components/external-link.tsx";
import { siteConfig } from "@/config/site";
import { ThemeSwitch } from "@/components/theme-switch";
import { GiteeIcon, GithubIcon, LuHeatIcon } from "@/components/icons";

const iconLinkClass =
  "inline-flex size-9 shrink-0 items-center justify-center rounded-full text-muted transition-colors hover:bg-background-tertiary hover:text-foreground";

function greetingLabel() {
  const h = new Date().getHours();

  if (h < 12) {
    return "早上好";
  }
  if (h < 18) {
    return "下午好";
  }

  return "晚上好";
}

type NavbarProps = {
  onOpenMobileNav: () => void;
};

export const Navbar = ({ onOpenMobileNav }: NavbarProps) => {
  const handleSponsor = () => {
    confetti({
      particleCount: 80,
      spread: 90,
      origin: { y: 0.65 },
    });
    window.open(siteConfig.links.sponsor, "_blank", "noopener,noreferrer");
  };

  return (
    <header className="flex shrink-0 items-center justify-between gap-3 border-b border-border bg-transparent px-3 py-3 dark:border-b-white/[0.05] md:gap-4 md:px-6 md:py-4">
      <div className="flex min-w-0 flex-1 items-center gap-3">
        <Button
          isIconOnly
          aria-label="打开菜单"
          className="shrink-0 text-muted hover:bg-background-tertiary hover:text-foreground md:hidden"
          size="sm"
          variant="ghost"
          onPress={onOpenMobileNav}
        >
          <Menu className="size-5" strokeWidth={1.75} />
        </Button>
        <div className="min-w-0">
          <p className="text-xs font-medium text-muted">
            {greetingLabel()}，欢迎使用
          </p>
          <h1 className="truncate text-lg font-semibold tracking-tight text-foreground md:text-xl">
            JT/T 808 服务面板
          </h1>
        </div>
      </div>

      <div className="flex shrink-0 items-center gap-0.5 sm:gap-1">
        <Button
          isIconOnly
          aria-label="搜索（示意）"
          className="hidden text-muted hover:bg-background-tertiary hover:text-foreground sm:flex"
          size="sm"
          variant="ghost"
        >
          <Search className="size-[1.125rem]" strokeWidth={1.75} />
        </Button>
        <Button
          isIconOnly
          aria-label="通知（示意）"
          className="hidden text-muted hover:bg-background-tertiary hover:text-foreground sm:flex"
          size="sm"
          variant="ghost"
        >
          <Bell className="size-[1.125rem]" strokeWidth={1.75} />
        </Button>

        <span title="GitHub">
          <ExternalLink
            unstyled
            className={iconLinkClass}
            href={siteConfig.links.github}
          >
            <GithubIcon size={20} />
          </ExternalLink>
        </span>
        <span title="Gitee">
          <ExternalLink
            unstyled
            className={iconLinkClass}
            href={siteConfig.links.gitee}
          >
            <GiteeIcon className="size-5" />
          </ExternalLink>
        </span>

        <div className="ml-1 rounded-full border border-border bg-background-tertiary p-0.5 dark:border-white/[0.08]">
          <ThemeSwitch />
        </div>

        <Button
          className="ml-1 hidden rounded-full px-4 sm:inline-flex"
          size="sm"
          variant="primary"
          onPress={handleSponsor}
        >
          <span className="flex items-center gap-2">
            <LuHeatIcon className="size-4 text-accent-foreground" />
            赞助
          </span>
        </Button>
        <Button
          isIconOnly
          aria-label="赞助"
          className="ml-0.5 rounded-full sm:hidden"
          size="sm"
          variant="primary"
          onPress={handleSponsor}
        >
          <LuHeatIcon className="size-4 text-accent-foreground" />
        </Button>
      </div>
    </header>
  );
};
