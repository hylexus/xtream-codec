import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import {frontendNotices} from '../../../build-script/license/frontend-notices.mjs'
import ElementPlus from 'unplugin-element-plus/vite';
import UnoCSS from 'unocss/vite'

// https://vite.dev/config/
export default defineConfig({
    plugins: [frontendNotices(), vue(), ElementPlus({}), UnoCSS()],
    base: '/jt1078-quick-start-ui/',
    server: {
        host: '0.0.0.0',
        proxy: {
            '/jt-1078-server-quick-start-api': {
                target: "http://localhost:9999",
                changeOrigin: true,
                rewrite: (path) => {
                    // console.log(`Proxying request: ${path} -> http://localhost:9999${path}`);
                    return path;
                },
            },
            '/dashboard-api/jt1078': {
                target: "http://localhost:9999",
                changeOrigin: true,
                rewrite: (path) => {
                    // console.log(`Proxying request: ${path} -> http://localhost:9999${path}`);
                    return path;
                },
            },
        },
    },
});
