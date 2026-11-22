import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import {frontendNotices} from '../../../build-script/license/frontend-notices.mjs'
import ElementPlus from 'unplugin-element-plus/vite';
import UnoCSS from 'unocss/vite'

// https://vite.dev/config/
export default defineConfig({
    plugins: [frontendNotices(), vue(), ElementPlus({}), UnoCSS()],
    server: {
        host: '0.0.0.0',
        proxy: {
            // "/api/jt-808-quick-start-with-storage": {
            //     target: "http://localhost:8080",
            //     changeOrigin: true,
            //     // rewrite: (path) => path.replace(/^\/api/, "/api/v1"),
            //     rewrite: (path) => path,
            // },
            '/dashboard-api': 'http://localhost:8888',
            '/api/jt-808-quick-start-with-storage': 'http://localhost:8888',
        },
    }
})
