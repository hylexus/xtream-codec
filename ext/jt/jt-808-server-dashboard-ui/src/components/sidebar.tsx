import { Button, Link, ListBox, Tooltip } from "@heroui/react";
import { useState } from "react";
import clsx from "clsx";
import { useRouteLoaderData } from "react-router-dom";

import { FaChevronRightIcon, LogoIcon } from "@/components/icons";
import { siteConfig } from "@/config/site.ts";
import { ServerInfo } from "@/types";

const TopContent = () => {
  return (
    <Link
      className="mb-8 flex items-center justify-center gap-0 px-3"
      href={siteConfig.links.github}
      rel="noopener noreferrer"
      target="_blank"
    >
      <LogoIcon className="flex h-8 w-8 items-center justify-center rounded-full" />
    </Link>
  );
};

export const Sidebar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const { config } = useRouteLoaderData("root") as { config: ServerInfo };

  const sideNavList = siteConfig.sidenav.filter((item) => {
    if (item.href === "/attachment") {
      return (
        config.jt808ServerConfig?.attachmentServer?.tcpServer?.enabled ||
        config.jt808ServerConfig?.attachmentServer?.udpServer?.enabled
      );
    }
    if (item.href === "/instruction") {
      return (
        config.jt808ServerConfig?.instructionServer?.tcpServer?.enabled ||
        config.jt808ServerConfig?.instructionServer?.udpServer?.enabled
      );
    }

    return true;
  });

  return (
    <div className="relative flex h-full flex-col">
      <div className="flex-1 overflow-y-auto">
        <div
          className={clsx(
            "relative flex flex-col items-center border-r border-default-200 p-6 transition-[width]",
            isOpen ? "w-56" : "w-16 px-2 py-6",
          )}
        >
          <TopContent />
          <ListBox
            aria-label="sideBar"
            className="flex w-full flex-1 flex-col gap-0.5 overflow-visible"
            selectionMode="none"
          >
            {sideNavList.map((link) => {
              const LinkIcon = link.icon;
              const iconClasses =
                "pointer-events-none mx-1 flex-shrink-0 text-xl fa-fw";

              return (
                <ListBox.Item
                  key={link.href}
                  className={clsx(
                    "group relative min-h-11 rounded-medium py-2 text-default-500 hover:bg-default-100",
                    link.href === "/debug" && "text-danger",
                  )}
                  href={link.href}
                  id={link.href}
                  isDisabled={link.href === "/debug"}
                  textValue={link.name}
                >
                  {!isOpen ? (
                    <Tooltip>
                      <Tooltip.Trigger>
                        <span className="flex w-full items-center justify-between gap-2 truncate">
                          <LinkIcon className={iconClasses} />
                          <span className="flex-1 truncate text-base font-medium" />
                        </span>
                      </Tooltip.Trigger>
                      <Tooltip.Content placement="right">
                        {link.name}
                      </Tooltip.Content>
                    </Tooltip>
                  ) : (
                    <div className="flex w-full items-center justify-between gap-2 truncate">
                      <LinkIcon className={iconClasses} />
                      <span className="flex-1 truncate text-base font-medium">
                        {link.name}
                      </span>
                    </div>
                  )}
                </ListBox.Item>
              );
            })}
          </ListBox>
          <div className="mt-28 flex-1" />
        </div>
      </div>
      <Button
        isIconOnly
        className="absolute right-[-10px] top-[30px] z-10 h-5 min-w-5 w-5 rounded-full text-small text-white shadow-lg"
        size="sm"
        onPress={() => setIsOpen(!isOpen)}
      >
        <FaChevronRightIcon
          className={clsx("fa-sm", isOpen ? "fa-rotate-180" : "")}
        />
      </Button>
    </div>
  );
};
