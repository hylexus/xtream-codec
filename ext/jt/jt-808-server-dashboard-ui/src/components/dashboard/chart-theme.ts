import { darkTheme, lightTheme, type XYChartTheme } from "@visx/xychart";

/** Visx / SVG 需用具体色值，不能用未定义的 CSS 变量 */
export const DASHBOARD_SERIES_COLORS = {
  light: ["#006FEE", "#17C964", "#F5A524", "#F31260"],
  dark: ["#338EF7", "#4ADE80", "#FBBF24", "#FB7185"],
} as const;

/** SVG 静态图：跟随 globals.css 主题变量 */
export const CHART_SERIES = [
  "var(--dashboard-series-1)",
  "var(--dashboard-series-2)",
  "var(--dashboard-series-3)",
  "var(--dashboard-series-4)",
] as const;

export const CHART_TCP = "var(--chart-primary)";
export const CHART_UDP = "var(--chart-secondary)";

export function resolveDashboardThemeMode(
  theme: string | undefined,
): "light" | "dark" {
  if (theme === "dark") {
    return "dark";
  }
  if (theme === "light") {
    return "light";
  }
  if (
    typeof window !== "undefined" &&
    window.matchMedia("(prefers-color-scheme: dark)").matches
  ) {
    return "dark";
  }

  return "light";
}

export function dashboardSeriesColors(mode: string | undefined): string[] {
  return [...DASHBOARD_SERIES_COLORS[resolveDashboardThemeMode(mode)]];
}

export function dashboardXyThemeForMode(
  mode: string | undefined,
): XYChartTheme {
  const resolved = resolveDashboardThemeMode(mode);
  const base = resolved === "light" ? lightTheme : darkTheme;
  const colors = dashboardSeriesColors(mode);
  const tickFill = resolved === "light" ? "#52525b" : "#a1a1aa";

  return {
    ...base,
    colors,
    svgLabelSmall: {
      ...base.svgLabelSmall,
      fill: tickFill,
      fontSize: 10,
    },
    svgLabelBig: {
      ...base.svgLabelBig,
      fill: tickFill,
    },
    htmlLabel: {
      ...base.htmlLabel,
      color: tickFill,
    },
  };
}
