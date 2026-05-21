import { useEffect, useState } from "react";
import {
  EventSourceMessage,
  fetchEventSource,
} from "@microsoft/fetch-event-source";

import { Metrics } from "@/types";

export function useDashboardMetrics() {
  const [data, setData] = useState<{ time: string; value: Metrics }>({
    time: "",
    value: {},
  });

  useEffect(() => {
    const ctrl = new AbortController();

    fetchEventSource(`${import.meta.env.VITE_API_DASHBOARD_V1}metrics/basic`, {
      method: "GET",
      signal: ctrl.signal,
      onmessage: (event: EventSourceMessage) => {
        setData(JSON.parse(event.data));
      },
    }).catch(() => {
      /* SSE 断开时保留上一帧 */
    });

    return () => ctrl.abort();
  }, []);

  return data;
}
