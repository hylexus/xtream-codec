import { Spinner } from "@heroui/react";

export function LoadingPanel() {
  return (
    <div className="flex min-h-48 items-center justify-center p-12">
      <Spinner size="lg" />
    </div>
  );
}
