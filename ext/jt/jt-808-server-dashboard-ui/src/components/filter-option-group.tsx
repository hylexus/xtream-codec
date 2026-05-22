import { Segment } from "@/components/ui/segment.tsx";

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

/** 单选筛选：Tabs 分段样式（Segment 封装） */
export function FilterOptionGroup({
  label,
  value,
  options,
  onChange,
}: FilterOptionGroupProps) {
  return (
    <div className="flex items-center gap-2">
      <span className="shrink-0 text-xs text-muted">{label}</span>
      <Segment
        selectedKey={value}
        variant="default"
        onSelectionChange={(key) => onChange(String(key))}
      >
        {options.map((opt, index) => (
          <Segment.Item key={opt.key} id={opt.key}>
            {index > 0 ? <Segment.Separator /> : null}
            {opt.label}
          </Segment.Item>
        ))}
      </Segment>
    </div>
  );
}
