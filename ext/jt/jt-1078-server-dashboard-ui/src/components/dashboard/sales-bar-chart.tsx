const BARS = [42, 58, 45, 72, 68, 55, 80, 62, 48, 70, 65, 52];

export function SalesBarChart() {
  const max = Math.max(...BARS);

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
      {BARS.map((h, i) => {
        const barH = (h / max) * 110;
        const x = 12 + i * 28;
        const y = 150 - barH;

        return (
          <rect
            key={i}
            className="dashboard-chart-bar"
            height={barH}
            rx="6"
            ry="6"
            width="18"
            x={x}
            y={y}
          />
        );
      })}
    </svg>
  );
}
