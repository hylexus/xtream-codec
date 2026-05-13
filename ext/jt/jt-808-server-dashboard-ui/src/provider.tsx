import { RouterProvider as HeroUIRouter } from "@heroui/react";
import { ReactNode } from "react";
import { useHref, useNavigate } from "react-router-dom";

/** HeroUI 的 Router 集成层，与 `react-router-dom` 的 `RouterProvider` 不同 */
export function Provider({ children }: { children: ReactNode }) {
  const navigate = useNavigate();

  return (
    <HeroUIRouter navigate={navigate} useHref={useHref}>
      {children}
    </HeroUIRouter>
  );
}
