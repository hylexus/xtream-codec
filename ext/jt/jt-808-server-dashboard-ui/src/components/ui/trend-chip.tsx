import { Chip } from "@heroui/react";
import { ArrowDown, ArrowUp } from "lucide-react";

type TrendChipProps = {
  value: string;
  direction: "up" | "down";
};

export function TrendChip({ value, direction }: TrendChipProps) {
  const Icon = direction === "up" ? ArrowUp : ArrowDown;

  return (
    <Chip
      color={direction === "up" ? "success" : "danger"}
      size="sm"
      variant="soft"
    >
      <span className="flex items-center gap-0.5">
        <Icon className="size-3" strokeWidth={2.5} />
        {value}
      </span>
    </Chip>
  );
}
