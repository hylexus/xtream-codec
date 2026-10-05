import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

const schemaPath = new URL(
  '../xtream-codec-core/src/main/resources/io/github/hylexus/xtream/codec/core/dynamic/schema/dynamic-codec-schema.json',
  import.meta.url,
);

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const proxy = {
    '/api': { target: env.CODEC_API_TARGET || 'http://127.0.0.1:8765', changeOrigin: true },
  };
  return {
    plugins: [
      react(),
      tailwindcss(),
      {
        name: 'codec-json-schema',
        configureServer(server) {
          server.middlewares.use('/dynamic-codec-schema.json', (_request, response) => {
            response.setHeader('Content-Type', 'application/schema+json; charset=utf-8');
            response.end(readFileSync(schemaPath));
          });
        },
        generateBundle() {
          this.emitFile({
            type: 'asset',
            fileName: 'dynamic-codec-schema.json',
            source: readFileSync(schemaPath, 'utf8'),
          });
        },
      },
    ],
    server: {
      port: 5173,
      strictPort: true,
      proxy,
      fs: { allow: [fileURLToPath(new URL('../', import.meta.url))] },
    },
    preview: { proxy },
    build: {
      rolldownOptions: {
        output: {
          codeSplitting: {
            groups: [
              { name: 'editor', test: /(?:codemirror|@lezer)/, priority: 20 },
              { name: 'ui', test: /(?:@heroui|react-aria|react-stately)/, priority: 15 },
              {
                name: 'react',
                test: /node_modules\/(?:react|react-dom|scheduler)\//,
                priority: 10,
              },
            ],
          },
        },
      },
    },
  };
});
