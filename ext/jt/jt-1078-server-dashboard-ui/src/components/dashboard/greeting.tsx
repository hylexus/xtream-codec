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
    <h1 className="dashboard-greeting-title">
      {greetingLabel()}，{siteConfig.user.name}
    </h1>
  );
}
