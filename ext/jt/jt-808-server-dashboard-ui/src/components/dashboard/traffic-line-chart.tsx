const PRIMARY =
  "M 0 120 C 40 100, 60 80, 100 90 S 160 40, 200 55 S 280 20, 360 35";
const SECONDARY =
  "M 0 130 C 50 115, 80 105, 120 100 S 180 75, 220 70 S 300 50, 360 45";

export function TrafficLineChart() {
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
      <path
        className="dashboard-chart-line-secondary"
        d={SECONDARY}
        fill="none"
        strokeWidth="2.5"
      />
      <path
        className="dashboard-chart-line-primary"
        d={PRIMARY}
        fill="none"
        strokeWidth="2.5"
      />
    </svg>
  );
}
