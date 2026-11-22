import type { LucideProps } from "lucide-react";

import {
  Bot,
  Box,
  Boxes,
  Bug,
  ChartColumnIncreasing,
  ChevronDown,
  ChevronRight,
  CircleCheck,
  Copy,
  Database,
  Eye,
  FilePlus,
  Gauge,
  Hash,
  Heart,
  MessageSquare,
  Monitor,
  Moon,
  Quote,
  RotateCcw,
  Server,
  Settings,
  Shuffle,
  Sun,
  Tags,
  Terminal,
  Trash2,
} from "lucide-react";
import * as React from "react";
import clsx from "clsx";

import { IconSvgProps } from "@/types";

const baseClass = "text-default-500 shrink-0";

function resolveIconSize(size: LucideProps["size"]): number | undefined {
  if (size == null) {
    return undefined;
  }
  if (typeof size === "number") {
    return size;
  }
  if (size === "xl") {
    return 24;
  }

  return 20;
}

function LucideIcon({
  as: Icon,
  className,
  size,
  strokeWidth = 2,
  ...props
}: LucideProps & { as: React.ComponentType<LucideProps> }) {
  const resolved = resolveIconSize(size);

  return (
    <Icon
      aria-hidden
      className={clsx(baseClass, className)}
      size={resolved ?? 18}
      strokeWidth={strokeWidth}
      {...props}
    />
  );
}

export const LuSunIcon = (props: LucideProps) => (
  <LucideIcon as={Sun} {...props} />
);
export const LuMoonIcon = (props: LucideProps) => (
  <LucideIcon as={Moon} {...props} />
);
export const LuGaugeIcon = (props: LucideProps) => (
  <LucideIcon as={Gauge} {...props} />
);
export const LuBugIcon = (props: LucideProps) => (
  <LucideIcon as={Bug} {...props} />
);
export const LuCommentsIcon = (props: LucideProps) => (
  <LucideIcon as={MessageSquare} {...props} />
);
export const LuTerminalIcon = (props: LucideProps) => (
  <LucideIcon as={Terminal} {...props} />
);
export const LuHeatIcon = (props: LucideProps) => (
  <LucideIcon as={Heart} {...props} />
);
export const LuTagsIcon = (props: LucideProps) => (
  <LucideIcon as={Tags} {...props} />
);
export const LuEyeIcon = (props: LucideProps) => (
  <LucideIcon as={Eye} {...props} />
);
export const LuTrashIcon = (props: LucideProps) => (
  <LucideIcon as={Trash2} {...props} />
);
export const LuChevronDownIcon = (props: LucideProps) => (
  <LucideIcon as={ChevronDown} {...props} />
);
export const LuFileCirclePlusIcon = (props: LucideProps) => (
  <LucideIcon as={FilePlus} {...props} />
);
export const LuGearIcon = (props: LucideProps) => (
  <LucideIcon as={Settings} {...props} />
);
export const LuShuffleIcon = (props: LucideProps) => (
  <LucideIcon as={Shuffle} {...props} />
);
export const LuCloneIcon = (props: LucideProps) => (
  <LucideIcon as={Copy} {...props} />
);
export const LuResetIcon = (props: LucideProps) => (
  <LucideIcon as={RotateCcw} {...props} />
);
export const LuChartSimpleIcon = (props: LucideProps) => (
  <LucideIcon as={ChartColumnIncreasing} {...props} />
);
export const LuServerIcon = (props: LucideProps) => (
  <LucideIcon as={Server} {...props} />
);
export const LuDesktopIcon = (props: LucideProps) => (
  <LucideIcon as={Monitor} {...props} />
);
export const LuRobotIcon = (props: LucideProps) => (
  <LucideIcon as={Bot} {...props} />
);
export const LuHashtagIcon = (props: LucideProps) => (
  <LucideIcon as={Hash} {...props} />
);
export const LuQuoteRightIcon = (props: LucideProps) => (
  <LucideIcon as={Quote} {...props} />
);
export const LuCubeIcon = (props: LucideProps) => (
  <LucideIcon as={Box} {...props} />
);
export const LuCircleCheckIcon = (props: LucideProps) => (
  <LucideIcon as={CircleCheck} {...props} />
);
export const LuChevronRightIcon = (props: LucideProps) => (
  <LucideIcon as={ChevronRight} {...props} />
);
export const LuDataBaseIcon = (props: LucideProps) => (
  <LucideIcon as={Database} {...props} />
);
export const LuCubesIcon = (props: LucideProps) => (
  <LucideIcon as={Boxes} {...props} />
);

export const LogoIcon: React.FC<IconSvgProps> = ({
  size = 36,
  height,
  ...props
}) => (
  <svg
    fill="none"
    height={size || height}
    viewBox="0 0 96 96"
    width={size || height}
    {...props}
  >
    <rect width="96" height="96" rx="24" fill="#0B1224" />
    <g>
      <path d="M29 66C48 66 48 30 67 30" stroke="#4D7CFE" strokeWidth="10" />
      <path d="M16 65L28 55Q30 53 30 56V76Q30 79 28 77L16 67Q14 66 16 65ZM80 29L68 19Q66 17 66 20V40Q66 43 68 41L80 31Q82 30 80 29Z" fill="#4D7CFE" />
    </g>
    <g transform="translate(0 96) scale(1 -1)">
      <path d="M29 66C48 66 48 30 67 30" stroke="#0B1224" strokeWidth="14" />
      <path d="M29 66C48 66 48 30 67 30" stroke="#22D3C5" strokeWidth="10" />
      <path d="M16 65L28 55Q30 53 30 56V76Q30 79 28 77L16 67Q14 66 16 65ZM80 29L68 19Q66 17 66 20V40Q66 43 68 41L80 31Q82 30 80 29Z" fill="#22D3C5" />
    </g>
  </svg>
);

export const GiteeIcon: React.FC<IconSvgProps> = ({
  size = 24,
  width,
  height,
  ...props
}) => {
  return (
    <svg
      height={size || height}
      viewBox="0 -2 24 24"
      width={size || width}
      {...props}
    >
      <path
        clipRule="evenodd"
        d="M11.0592 22.1184C4.9766 22.1184 0 17.1418 0 11.0592S4.9766 0 11.0592 0s11.0592 4.9766 11.0592 11.0592-4.9766 11.0592-11.0592 11.0592zm5.5987-12.3034H10.368c-.2765 0-.553.2765-.553.553v1.3824c0 .2765.2765.553.553.553h3.8016c.2765 0 .553.2765.553.553v.2765c0 .8986-.7603 1.6589-1.6589 1.6589h-5.184c-.2765 0-.553-.2765-.553-.553V8.9856c0-.8986.7603-1.6589 1.6589-1.6589h7.6723c.2765 0 .553-.2765.553-.553v-1.3824c0-.2765-.2765-.553-.553-.553H8.9856c-2.281 0-4.0781 1.8662-4.0781 4.0781V16.5888c0 .2765.2765.553.553.553h8.087c2.0045 0 3.6634-1.6589 3.6634-3.6634v-3.1104c0-.2765-.2765-.553-.553-.553z"
        fill="currentColor"
        fillRule="evenodd"
      />
    </svg>
  );
};

export const GithubIcon: React.FC<IconSvgProps> = ({
  size = 24,
  width,
  height,
  ...props
}) => {
  return (
    <svg
      height={size || height}
      viewBox="0 0 24 24"
      width={size || width}
      {...props}
    >
      <path
        clipRule="evenodd"
        d="M12.026 2c-5.509 0-9.974 4.465-9.974 9.974 0 4.406 2.857 8.145 6.821 9.465.499.09.679-.217.679-.481 0-.237-.008-.865-.011-1.696-2.775.602-3.361-1.338-3.361-1.338-.452-1.152-1.107-1.459-1.107-1.459-.905-.619.069-.605.069-.605 1.002.07 1.527 1.028 1.527 1.028.89 1.524 2.336 1.084 2.902.829.091-.645.351-1.085.635-1.334-2.214-.251-4.542-1.107-4.542-4.93 0-1.087.389-1.979 1.024-2.675-.101-.253-.446-1.268.099-2.64 0 0 .837-.269 2.742 1.021a9.582 9.582 0 0 1 2.496-.336 9.554 9.554 0 0 1 2.496.336c1.906-1.291 2.742-1.021 2.742-1.021.545 1.372.203 2.387.099 2.64.64.696 1.024 1.587 1.024 2.675 0 3.833-2.33 4.675-4.552 4.922.355.308.675.916.675 1.846 0 1.334-.012 2.41-.012 2.737 0 .267.178.577.687.479C19.146 20.115 22 16.379 22 11.974 22 6.465 17.535 2 12.026 2z"
        fill="currentColor"
        fillRule="evenodd"
      />
    </svg>
  );
};
