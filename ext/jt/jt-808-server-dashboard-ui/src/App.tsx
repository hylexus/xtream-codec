import { createBrowserRouter, RouterProvider } from "react-router-dom";

import { DebugPage } from "@/pages/debug.tsx";
import { AttachmentPage } from "@/pages/attachment.tsx";
import { InstructionPage } from "@/pages/instruction.tsx";
import { DashboardPage } from "@/pages/dashboard.tsx";
import { ConfigurationPage } from "@/pages/configuration.tsx";
import { DashboardLayout } from "@/layouts/dashboard.tsx";
import { SubscribePage } from "@/pages/subscriber.tsx";
import { MappingsPage } from "@/pages/mappings.tsx";
import { DumpPage } from "@/pages/dump.tsx";
import { loader as rootLoader } from "@/routes/root.tsx";
import { ThreadsPage } from "@/pages/threads.tsx";
import {BeanMetadataPage} from "@/pages/bean-metadata.tsx";
import {CodecMetadataPage} from "@/pages/codec-metadata.tsx";

const router = createBrowserRouter(
  [
    {
      path: "/",
      id: "root",
      element: <DashboardLayout />,
      loader: rootLoader,
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
          path: "instruction",
          element: <InstructionPage />,
        },
        {
          path: "attachment",
          element: <AttachmentPage />,
        },
        {
          path: "debug",
          element: <DebugPage />,
        },
        {
          path: "mappings",
          element: <MappingsPage />,
        },
        {
          path: "dump",
          element: <DumpPage />,
        },
        {
          path: "threads",
          element: <ThreadsPage />,
        },
        {
          path: "subscriber",
          element: <SubscribePage />,
        },
        {
          path: "configuration",
          element: <ConfigurationPage />,
        },
        {
          path: "bean-metadata",
          element: <BeanMetadataPage />,
        },
        {
          path: "codec-metadata",
          element: <CodecMetadataPage />,
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
