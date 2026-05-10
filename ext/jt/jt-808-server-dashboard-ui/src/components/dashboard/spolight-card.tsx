import { Card } from "@heroui/react";
import { ReactNode, useRef } from "react";

import { useMouseMove } from "@/hooks/use-mouse-move.ts";

export const SpotlightCard = ({
  children,
  ...props
}: {
  children: ReactNode;
  className?: string;
}) => {
  const ref = useRef<HTMLDivElement | null>(null);
  const { x, y } = useMouseMove(ref);

  return (
    <div ref={ref} className="h-full min-h-[1px] w-full">
      <Card
        className="h-full border border-default-100 shadow-sm"
        style={{
          background:
            x > 0 || y > 0
              ? `radial-gradient(450px at ${x}px ${y}px, rgba(120, 40, 200, 0.4), transparent 80%)`
              : undefined,
        }}
        {...props}
      >
        {children}
      </Card>
    </div>
  );
};
