import { Tabs, type TabsRootProps } from "@heroui/react";
import clsx from "clsx";
import { type ReactNode } from "react";

type SegmentVariant = "default" | "ghost";
type SegmentKey = string | number;

const VARIANT_MAP: Record<SegmentVariant, TabsRootProps["variant"]> = {
  default: "primary",
  ghost: "secondary",
};

type SegmentProps = Omit<
  TabsRootProps,
  "selectedKey" | "defaultSelectedKey" | "onSelectionChange" | "variant"
> & {
  variant?: SegmentVariant;
  selectedKey?: SegmentKey | null;
  defaultSelectedKey?: SegmentKey;
  onSelectionChange?: (key: SegmentKey) => void;
  /** @deprecated Tabs 无 size；保留以兼容调用方，映射为紧凑字号 */
  size?: "sm" | "md" | "lg";
  children: ReactNode;
};

function SegmentRoot({
  variant = "default",
  selectedKey,
  defaultSelectedKey,
  onSelectionChange,
  size,
  className,
  children,
  "aria-label": ariaLabel,
  ...rest
}: SegmentProps) {
  return (
    <Tabs
      className={clsx(
        "segment-control w-auto",
        size === "sm" && "text-sm",
        className,
      )}
      defaultSelectedKey={
        defaultSelectedKey != null ? String(defaultSelectedKey) : undefined
      }
      selectedKey={selectedKey != null ? String(selectedKey) : undefined}
      variant={VARIANT_MAP[variant]}
      onSelectionChange={(key) => {
        if (key != null) {
          onSelectionChange?.(key);
        }
      }}
      {...rest}
    >
      <Tabs.ListContainer className="w-auto">
        <Tabs.List
          aria-label={ariaLabel}
          className="w-auto max-w-none [&[data-orientation=horizontal]]:w-auto"
        >
          {children}
        </Tabs.List>
      </Tabs.ListContainer>
    </Tabs>
  );
}

type SegmentItemProps = {
  id: SegmentKey;
  children: ReactNode;
  className?: string;
  isDisabled?: boolean;
};

function SegmentItem({
  id,
  children,
  className,
  isDisabled,
}: SegmentItemProps) {
  return (
    <Tabs.Tab
      className={clsx("!w-auto shrink-0 whitespace-nowrap", className)}
      id={String(id)}
      isDisabled={isDisabled}
    >
      {children}
      <Tabs.Indicator />
    </Tabs.Tab>
  );
}

function SegmentSeparator() {
  return <Tabs.Separator />;
}

/** 分段单选控件（HeroUI Tabs，保留 Segment 兼容 API） */
export const Segment = Object.assign(SegmentRoot, {
  Item: SegmentItem,
  Separator: SegmentSeparator,
});
