import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import ElementPlus from 'unplugin-element-plus/vite';
import UnoCSS from 'unocss/vite'

// https://vite.dev/config/
export default defineConfig({
    plugins: [vue(), ElementPlus({}), UnoCSS()],
    server: {
        host: '0.0.0.0',
        proxy: {
            '/jt-1078-server-spring-boot-starter-reactive-debug': {
                target: "http://localhost:9999",
                changeOrigin: true,
                rewrite: (path) => {
                    console.log(`Proxying request: ${path} -> http://localhost:9999${path}`);
                    return path;
                },
            },
            '/dashboard-api/jt1078': {
                target: "http://localhost:9999",
                changeOrigin: true,
                rewrite: (path) => {
                    console.log(`Proxying request: ${path} -> http://localhost:9999${path}`);
                    return path;
                },
            },
        },
    },
});
