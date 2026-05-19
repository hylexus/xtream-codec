import { useEffect, useState } from "react";
import {
  EventSourceMessage,
  fetchEventSource,
} from "@microsoft/fetch-event-source";
import { Card } from "@heroui/react";

import { PageSection } from "@/components/page-header.tsx";
import { JsonPreview } from "@/components/json-preview.tsx";
import { Thread } from "@/types";

export const ThreadsPage = () => {
  const [threads, setThreads] = useState<Thread[]>([]);

  useEffect(() => {
    const ctrl = new AbortController();

    fetchEventSource(
      `${import.meta.env.VITE_API_DASHBOARD_V1}metrics/schedulers`,
      {
        method: "GET",
        signal: ctrl.signal,
        onmessage: (event: EventSourceMessage) => {
          const data = JSON.parse(event.data);

          setThreads((pre) => {
            const tempThread = {
              time: data.time,
              name: data.value.name,
              value: data.value.value,
            };
            const index = pre.findIndex((e) => e.name === data.value.name);

            if (index !== -1) {
              return pre.toSpliced(index, 1, tempThread);
            } else {
              return pre.concat([tempThread]);
            }
          });
        },
      },
    ).then(() => {
      // TODO
    });

    return () => {
      ctrl.abort();
    };
  }, []);

  return (
    <PageSection description="各调度器线程池的实时配置快照" title="调度器指标">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
        {threads.map((thread: Thread) => (
          <Card key={thread.name} className="border border-border">
            <Card.Header className="text-foreground">{thread.name}</Card.Header>
            <Card.Content>
              <p className="mb-2 text-sm text-muted">
                time: {thread.time.slice(0, -4)}
              </p>
              <JsonPreview json={thread.value} page="threads" />
            </Card.Content>
          </Card>
        ))}
      </div>
    </PageSection>
  );
};
