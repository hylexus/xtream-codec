import { Outlet } from "react-router-dom";

import { Navbar } from "@/components/navbar";
import { Sidebar } from "@/components/sidebar.tsx";
import { Provider } from "@/provider.tsx";
import { AwesomeBg } from "@/components/awesome-bg.tsx";

export const DashboardLayout = () => {
  return (
    <Provider>
      <AwesomeBg />
      <div className="flex h-screen max-w-8xl mx-auto overflow-hidden">
        <Sidebar />
        <div className="flex w-full flex-1 flex-col p-4 box-border overflow-hidden">
          <Navbar />
          <main className="m-4 flex-1 w-full overflow-y-auto relative">
            <Outlet />
          </main>
        </div>
      </div>
    </Provider>
  );
};
