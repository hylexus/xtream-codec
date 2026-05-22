import { Table } from "@heroui/react";
import {
  FC,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
  type RefObject,
} from "react";
import { useCountUp } from "react-countup";

import { siteConfig } from "@/config/site.ts";
import { TrendChip } from "@/components/ui/trend-chip.tsx";
import {
  dashboardLabel,
  dashboardMetricMd,
} from "@/components/ui/dashboard-typography.tsx";
import { getKeyValue } from "@/utils/get-key-value.ts";

/** 数字滚动动画（KPI / 图表大数） */
export function CountNumber({ end }: { end: number }) {
  const countUpRef = useRef<HTMLSpanElement>(null);
  const { update } = useCountUp({
    ref: countUpRef as unknown as RefObject<HTMLElement>,
    start: 0,
    end,
  });

  useEffect(() => {
    update(end);
  }, [end, update]);

  return <span ref={countUpRef} />;
}

/** 自启动时间起的运行时长 */
export function CountTime({ start }: { start: Date }) {
  const [timeLeft, setTimeLeft] = useState("");

  useEffect(() => {
    let timer: ReturnType<typeof setTimeout> | undefined;

    const tick = () => {
      const distance = Date.now() - start.getTime();
      const days = Math.floor(distance / (1000 * 60 * 60 * 24));
      const hours = Math.floor(
        (distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60),
      );
      const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((distance % (1000 * 60)) / 1000);

      setTimeLeft(
        `${days > 0 ? `${days}d ` : ""}${hours > 0 ? `${hours}h ` : ""}${minutes}m ${seconds}s`,
      );
      timer = setTimeout(tick, 1000);
    };

    tick();

    return () => clearTimeout(timer);
  }, [start]);

  return <span>{timeLeft}</span>;
}

/** 消息明细 Popover 内嵌表 */
export const MsgMiniTable: FC<{ data: unknown }> = ({ data }) => {
  const tableData = useMemo(() => {
    if (!data || typeof data !== "object") {
      return [];
    }

    return Object.keys(data).map((key) => {
      const cellValue = getKeyValue(data, key);

      return {
        id: key,
        ...(typeof cellValue === "object" && cellValue !== null
          ? (cellValue as object)
          : { value: cellValue }),
      };
    });
  }, [data]);

  const columns = [
    { id: "messageIdAsHexString", name: "消息ID" },
    { id: "desc", name: "消息描述" },
    { id: "count", name: "总数" },
  ];

  return (
    <Table.Root className="max-h-[520px] min-h-[100px] w-full overflow-auto shadow-none">
      <Table.ScrollContainer>
        <Table.Content aria-label="Detail">
          <Table.Header>
            {columns.map((col) => (
              <Table.Column
                key={col.id}
                isRowHeader={col.id === "messageIdAsHexString"}
              >
                {col.name}
              </Table.Column>
            ))}
          </Table.Header>
          <Table.Body items={tableData}>
            {(item: Record<string, unknown>) => (
              <Table.Row id={String(item.id)}>
                {columns.map((col) => (
                  <Table.Cell key={col.id}>
                    {getKeyValue(item, col.id) as ReactNode}
                  </Table.Cell>
                ))}
              </Table.Row>
            )}
          </Table.Body>
        </Table.Content>
      </Table.ScrollContainer>
    </Table.Root>
  );
};

export type ChartMiniStatTrend = {
  value: string;
  direction: "up" | "down";
};

export type ChartMiniStatProps = {
  label: string;
  value: number | string;
  trend?: ChartMiniStatTrend;
};

/** 图表卡片顶栏子指标 */
export function ChartMiniStat({ label, value, trend }: ChartMiniStatProps) {
  const numeric = typeof value === "number";

  return (
    <div className="min-w-0">
      <p className={dashboardLabel}>{label}</p>
      <div className="mt-1.5 flex flex-wrap items-end gap-2">
        <p className={dashboardMetricMd}>
          {numeric ? <CountNumber end={value} /> : value}
        </p>
        {trend ? (
          <TrendChip direction={trend.direction} value={trend.value} />
        ) : null}
      </div>
    </div>
  );
}

function greetingLabel() {
  const h = new Date().getHours();

  if (h < 12) {
    return "早上好";
  }
  if (h < 18) {
    return "下午好";
  }

  return "晚上好";
}

export function DashboardGreeting() {
  return (
    <h1 className="text-xl font-semibold tracking-tight text-foreground sm:text-2xl">
      {greetingLabel()}，欢迎使用 {siteConfig.name}
    </h1>
  );
}
