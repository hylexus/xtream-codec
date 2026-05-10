import { RouterProvider } from "@heroui/react";
import { ReactNode } from "react";
import { useHref, useNavigate } from "react-router-dom";

export function Provider({ children }: { children: ReactNode }) {
  const navigate = useNavigate();

  return (
    <RouterProvider navigate={navigate} useHref={useHref}>
      {children}
    </RouterProvider>
  );
}
