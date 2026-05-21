import { Button } from "@heroui/react";
import { RefreshCw } from "lucide-react";

export function DashboardToolbar() {
  return (
    <div className="flex flex-wrap items-center justify-end gap-4">
      <div className="flex flex-wrap items-center gap-2">
        <Button isIconOnly aria-label="刷新" size="sm" variant="ghost">
          <RefreshCw className="size-4 text-muted" strokeWidth={1.75} />
        </Button>
      </div>
    </div>
  );
}
