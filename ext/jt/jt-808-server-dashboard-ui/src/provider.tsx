import { NextUIProvider } from "@nextui-org/system";
import { ReactNode } from "react";
import { useNavigate, useHref } from "react-router-dom";

export function Provider({ children }: { children: ReactNode }) {
  const navigate = useNavigate();

  return (
    <NextUIProvider navigate={navigate} useHref={useHref}>
      {children}
    </NextUIProvider>
  );
}
