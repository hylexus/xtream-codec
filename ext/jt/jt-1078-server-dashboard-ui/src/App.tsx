import { createBrowserRouter, RouterProvider } from "react-router-dom";

import { DashboardLayout } from "@/layouts/dashboard.tsx";
import { DashboardPage } from "@/pages/dashboard.tsx";
import { SessionsPage } from "@/pages/sessions.tsx";
import { SubscribersPage } from "@/pages/subscribers.tsx";

const router = createBrowserRouter(
  [
    {
      path: "/",
      element: <DashboardLayout />,
      children: [
        {
          index: true,
          element: <DashboardPage />,
        },
        {
          path: "dashboard",
          element: <DashboardPage />,
        },
        {
          path: "sessions",
          element: <SessionsPage />,
        },
        {
          path: "subscribers",
          element: <SubscribersPage />,
        },
      ],
    },
  ],
  { basename: import.meta.env.BASE_URL },
);

function App() {
  return <RouterProvider router={router} />;
}

export default App;
