import { Button, Link } from "@heroui/react";
import confetti from "canvas-confetti";

import { siteConfig } from "@/config/site";
import { ThemeSwitch } from "@/components/theme-switch";
import { GiteeIcon, FaGithubIcon, FaHeatIcon } from "@/components/icons";

export const Navbar = () => {
  const handleConfetti = () => {
    confetti({
      particleCount: 100,
      spread: 100,
      origin: { y: 0.6 },
    });
  };

  return (
    <header className="sticky top-0 z-20 flex w-full max-w-full items-center justify-end gap-3 bg-transparent px-4 py-2">
      <div className="hidden items-center gap-2 sm:flex">
        <span title="GitHub">
          <Link
            className="text-default-500"
            href={siteConfig.links.github}
            rel="noopener noreferrer"
            target="_blank"
          >
            <FaGithubIcon size="xl" />
          </Link>
        </span>
        <span title="Gitee">
          <Link
            className="text-default-500"
            href={siteConfig.links.gitee}
            rel="noopener noreferrer"
            target="_blank"
          >
            <GiteeIcon />
          </Link>
        </span>
        <ThemeSwitch />
      </div>
      <div className="hidden md:flex">
        <Button
          className="text-sm font-normal text-default-600 bg-default-100"
          variant="secondary"
          onPress={handleConfetti}
        >
          <span className="flex items-center gap-2">
            <FaHeatIcon className="text-danger text-2xl" />
            Sponsor
          </span>
        </Button>
      </div>

      <div className="flex basis-1 items-center justify-end gap-2 pl-4 sm:hidden">
        <Link
          className="text-default-500"
          href={siteConfig.links.github}
          rel="noopener noreferrer"
          target="_blank"
        >
          <FaGithubIcon size="xl" />
        </Link>
        <span title="Gitee">
          <Link
            className="text-default-500"
            href={siteConfig.links.gitee}
            rel="noopener noreferrer"
            target="_blank"
          >
            <GiteeIcon />
          </Link>
        </span>
        <ThemeSwitch />
      </div>
    </header>
  );
};
