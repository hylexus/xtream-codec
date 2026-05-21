import { siteConfig } from "@/config/site.ts";

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
