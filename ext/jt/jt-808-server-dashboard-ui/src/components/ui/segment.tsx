import {
  ToggleButton,
  ToggleButtonGroup,
  type ToggleButtonGroupRootProps,
  type ToggleButtonRootProps,
} from "@heroui/react";
import { createContext, useContext, type ReactNode } from "react";

type SegmentVariant = "default" | "ghost";
type SegmentKey = string | number;

const SegmentVariantContext = createContext<SegmentVariant>("default");

type SegmentProps = Omit<
  ToggleButtonGroupRootProps,
  | "selectionMode"
  | "selectedKeys"
  | "defaultSelectedKeys"
  | "onSelectionChange"
> & {
  variant?: SegmentVariant;
  selectedKey?: SegmentKey | null;
  defaultSelectedKey?: SegmentKey;
  onSelectionChange?: (key: SegmentKey) => void;
  children: ReactNode;
};

function SegmentRoot({
  variant = "default",
  selectedKey,
  defaultSelectedKey,
  onSelectionChange,
  size = "sm",
  children,
  ...rest
}: SegmentProps) {
  const selectedKeys =
    selectedKey != null ? new Set<SegmentKey>([selectedKey]) : undefined;
  const defaultSelectedKeys =
    defaultSelectedKey != null
      ? new Set<SegmentKey>([defaultSelectedKey])
      : undefined;

  return (
    <SegmentVariantContext.Provider value={variant}>
      <ToggleButtonGroup
        disallowEmptySelection
        defaultSelectedKeys={defaultSelectedKeys}
        selectedKeys={selectedKeys}
        selectionMode="single"
        size={size}
        onSelectionChange={(keys) => {
          const key = [...keys][0];

          if (key != null) {
            onSelectionChange?.(key);
          }
        }}
        {...rest}
      >
        {children}
      </ToggleButtonGroup>
    </SegmentVariantContext.Provider>
  );
}

type SegmentItemProps = Omit<ToggleButtonRootProps, "id"> & {
  id: SegmentKey;
};

function SegmentItem({ id, variant, children, ...rest }: SegmentItemProps) {
  const groupVariant = useContext(SegmentVariantContext);

  return (
    <ToggleButton id={id} variant={variant ?? groupVariant} {...rest}>
      {children}
    </ToggleButton>
  );
}

function SegmentSeparator() {
  return <ToggleButtonGroup.Separator />;
}

/** Segmented single-select control (HeroUI ToggleButtonGroup, Segment-compatible API). */
export const Segment = Object.assign(SegmentRoot, {
  Item: SegmentItem,
  Separator: SegmentSeparator,
});
