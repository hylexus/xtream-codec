import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tsconfigPaths from "vite-tsconfig-paths";
import { visualizer } from "rollup-plugin-visualizer";
import tailwindcss from "@tailwindcss/vite";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react(), tsconfigPaths(), visualizer(), tailwindcss()],
  base: process.env.VITE_BASE_PATH || "/dashboard-ui/",
  server: {
    proxy: {
      "/dashboard-api/jt808": {
        target: "http://localhost:8888",
        changeOrigin: true,
        rewrite: (path) =>
          path.replace(/^\/dashboard-api\/jt808/, "/dashboard-api/jt808/v1"),
      },
    },
  },
  build: {
    chunkSizeWarningLimit: 600,
    rollupOptions: {
      output: {
        manualChunks: (id) => {
          if (
            ["@heroui/accordion", "@heroui/avatar"].some((e) => id.includes(e))
          ) {
            return "vendors1";
          } else if (id.includes("@visx/")) {
            return "vendors2";
          }
        },
      },
    },
  },
});
