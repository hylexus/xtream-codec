import { useEffect, useState } from "react";
import { Outlet } from "react-router-dom";

import { Navbar } from "@/components/navbar";
import { Sidebar } from "@/components/sidebar.tsx";

export const DashboardLayout = () => {
  const [sidebarCompact, setSidebarCompact] = useState(false);
  const [mobileNavOpen, setMobileNavOpen] = useState(false);

  useEffect(() => {
    const mq = window.matchMedia("(min-width: 768px)");
    const closeMobile = () => {
      if (mq.matches) {
        setMobileNavOpen(false);
      }
    };

    mq.addEventListener("change", closeMobile);

    return () => mq.removeEventListener("change", closeMobile);
  }, []);

  return (
    <div className="relative flex h-dvh min-h-0 overflow-hidden bg-background text-foreground">
      {mobileNavOpen ? (
        <button
          aria-label="关闭导航"
          className="fixed inset-0 z-40 bg-backdrop backdrop-blur-sm md:hidden"
          type="button"
          onClick={() => setMobileNavOpen(false)}
        />
      ) : null}
      <Sidebar
        compact={sidebarCompact}
        mobileOpen={mobileNavOpen}
        onCloseMobile={() => setMobileNavOpen(false)}
        onToggleCompact={() => setSidebarCompact((v) => !v)}
      />
      <div className="dashboard-main-column flex min-w-0 flex-col">
        <Navbar onOpenMobileNav={() => setMobileNavOpen(true)} />
        <main className="min-h-0 flex-1 px-3 pb-4 pt-1 md:px-7 md:pb-7 md:pt-3">
          <div className="dashboard-main-surface mx-auto h-full min-h-0 overflow-y-auto p-5 md:p-8">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};
