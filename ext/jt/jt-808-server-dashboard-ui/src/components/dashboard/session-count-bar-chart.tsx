type SessionCountBarChartProps = {
  values: number[];
};

/** 用四路会话当前值驱动柱状图（示意趋势） */
export function SessionCountBarChart({ values }: SessionCountBarChartProps) {
  const bars = values.length > 0 ? values : [0, 0, 0, 0];
  const max = Math.max(...bars, 1);

  return (
    <svg
      aria-hidden
      className="h-full w-full"
      preserveAspectRatio="none"
      viewBox="0 0 360 160"
    >
      {[0, 1, 2, 3].map((i) => (
        <line
          key={i}
          className="dashboard-chart-grid-line"
          x1="0"
          x2="360"
          y1={40 + i * 30}
          y2={40 + i * 30}
        />
      ))}
      {bars.map((h, i) => {
        const barH = (h / max) * 110;
        const x = 24 + i * 80;
        const y = 150 - barH;

        return (
          <rect
            key={i}
            className="dashboard-chart-bar"
            height={barH}
            rx="6"
            ry="6"
            width="48"
            x={x}
            y={y}
          />
        );
      })}
    </svg>
  );
}
