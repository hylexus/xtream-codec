import { Card } from "@heroui/react";
import useSWR from "swr";
import { Link } from "react-router-dom";

import { SpotlightCard } from "@/components/dashboard/spotlight-card.tsx";
import { LuDesktopIcon, LuTagsIcon } from "@/components/icons.tsx";
import { request } from "@/utils/request.ts";

export const DashboardPage = () => {
  const { data: sessions } = useSWR("sessions-count", () =>
    request<{ total: number }>({
      path: "sessions",
      method: "GET",
      params: { pageNumber: 1, pageSize: 1 },
    }),
  );
  const { data: subscribers } = useSWR("subscribers-count", () =>
    request<{ total: number }>({
      path: "subscribers",
      method: "GET",
      params: { pageNumber: 1, pageSize: 1 },
    }),
  );

  const stats = [
    {
      key: "sessions",
      label: "媒体会话",
      value: sessions?.total ?? "—",
      href: "/sessions",
      icon: LuDesktopIcon,
    },
    {
      key: "subscribers",
      label: "数据订阅",
      value: subscribers?.total ?? "—",
      href: "/subscribers",
      icon: LuTagsIcon,
    },
  ];

  return (
    <div className="flex flex-col gap-8">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight text-foreground">
          概览
        </h2>
        <p className="mt-1 max-w-3xl text-sm leading-relaxed text-muted">
          JT/T 1078 流媒体服务运行状态一览。
        </p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:max-w-2xl">
        {stats.map((stat) => {
          const Icon = stat.icon;

          return (
            <Link key={stat.key} className="block" to={stat.href}>
              <SpotlightCard className="p-6 transition-opacity hover:opacity-95">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="text-sm text-muted">{stat.label}</p>
                    <p className="mt-2 text-3xl font-semibold tabular-nums text-foreground">
                      {stat.value}
                    </p>
                  </div>
                  <Card className="flex size-11 items-center justify-center rounded-xl border-0 bg-default shadow-none">
                    <Icon className="size-5 text-muted" />
                  </Card>
                </div>
              </SpotlightCard>
            </Link>
          );
        })}
      </div>
    </div>
  );
};
