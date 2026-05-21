import { Button } from "@heroui/react";
import { ArrowUpDown, Columns3, Filter } from "lucide-react";
import { ReactNode } from "react";

const iconClass = "size-3.5 shrink-0";

function ToolbarButton({
  label,
  icon,
}: {
  label: string;
  icon: ReactNode;
}) {
  return (
    <Button size="sm" variant="secondary">
      <span className="flex items-center gap-1.5">
        {icon}
        {label}
      </span>
    </Button>
  );
}

export function ListTableToolbar() {
  return (
    <>
      <ToolbarButton
        icon={<Filter className={iconClass} strokeWidth={1.75} />}
        label="筛选"
      />
      <ToolbarButton
        icon={<ArrowUpDown className={iconClass} strokeWidth={1.75} />}
        label="排序"
      />
      <ToolbarButton
        icon={<Columns3 className={iconClass} strokeWidth={1.75} />}
        label="列"
      />
    </>
  );
}
