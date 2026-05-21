import { useEffect, useState } from "react";
import { Outlet } from "react-router-dom";

import { MainHeader } from "@/components/layout/main-header.tsx";
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
    <div className="dashboard-shell">
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
      <div className="dashboard-main flex min-w-0 flex-col">
        <header className="shrink-0 px-6 pt-6">
          <div className="mx-auto w-full max-w-[1600px]">
            <MainHeader
              onOpenMobileNav={() => setMobileNavOpen(true)}
              onToggleSidebar={() => setSidebarCompact((v) => !v)}
            />
          </div>
        </header>
        <main className="min-h-0 flex-1 overflow-y-auto px-6 pb-8">
          <div className="mx-auto w-full max-w-[1600px]">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};
