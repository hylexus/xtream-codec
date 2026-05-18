import { Button, ButtonGroup } from "@heroui/react";

export type FilterOption = {
  key: string;
  label: string;
};

type FilterOptionGroupProps = {
  label: string;
  value: string;
  options: readonly FilterOption[];
  onChange: (key: string) => void;
};

/** 单选筛选：HeroUI ButtonGroup + primary/ghost 切换 */
export function FilterOptionGroup({
  label,
  value,
  options,
  onChange,
}: FilterOptionGroupProps) {
  return (
    <div className="flex items-center gap-2">
      <span className="shrink-0 text-xs text-muted">{label}</span>
      <ButtonGroup size="sm">
        {options.map((opt) => (
          <Button
            key={opt.key}
            variant={value === opt.key ? "primary" : "ghost"}
            onPress={() => onChange(opt.key)}
          >
            {opt.label}
          </Button>
        ))}
      </ButtonGroup>
    </div>
  );
}
