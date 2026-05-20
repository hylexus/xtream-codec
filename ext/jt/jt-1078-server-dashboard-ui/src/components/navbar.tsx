import { Button } from "@heroui/react";
import { Bell, Menu, Plus, Search } from "lucide-react";

import { ThemeSwitch } from "@/components/theme-switch";

type NavbarProps = {
  onOpenMobileNav: () => void;
};

export const Navbar = ({ onOpenMobileNav }: NavbarProps) => {
  return (
    <header className="sticky top-0 z-30 flex shrink-0 items-center justify-between gap-3 border-b border-border bg-surface/90 px-4 py-3 backdrop-blur-md dark:border-b-white/[0.06] dark:bg-[#0a0a0a]/90 md:px-6">
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

      <div className="hidden min-w-0 flex-1 md:block" />

      <div className="flex shrink-0 items-center gap-1 sm:gap-2">
        <Button
          isIconOnly
          aria-label="搜索"
          className="text-muted hover:bg-background-tertiary hover:text-foreground"
          size="sm"
          variant="ghost"
        >
          <Search className="size-[1.125rem]" strokeWidth={1.75} />
        </Button>
        <Button
          isIconOnly
          aria-label="通知"
          className="text-muted hover:bg-background-tertiary hover:text-foreground"
          size="sm"
          variant="ghost"
        >
          <Bell className="size-[1.125rem]" strokeWidth={1.75} />
        </Button>

        <div className="ml-0.5 rounded-full border border-border bg-background-tertiary p-0.5 dark:border-white/[0.08]">
          <ThemeSwitch />
        </div>

        <Button className="ml-1 rounded-xl px-4" size="sm" variant="primary">
          <span className="flex items-center gap-1.5">
            <Plus className="size-4" strokeWidth={2} />
            邀请
          </span>
        </Button>
      </div>
    </header>
  );
};
