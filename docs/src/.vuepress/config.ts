import {defineUserConfig} from "vuepress";
import theme from "./theme.js";
import {path} from "vuepress/utils"
import llmstxt from "vuepress-plugin-llms";
import {viteBundler} from "@vuepress/bundler-vite";
import {frontendNotices} from "../../../build-script/license/frontend-notices.mjs";

export default defineUserConfig({
    bundler: viteBundler({
        configureVite(config, isServer) {
            if (!isServer) config.plugins = [...(config.plugins ?? []), frontendNotices({
                additionalLicenseFiles: [
                    'src/.vuepress/public/webfonts/LICENSE.txt',
                    'src/.vuepress/public/webfonts/README.txt',
                ],
            })];
        },
    }),
    base: "/xtream-codec/",
    port: 8090,
    locales: {
        "/": {
            lang: "zh-CN",
            title: "xtream-codec",
            description: "xtream-codec 文档",
        },
    },

    theme,

    // Enable it with pwa
    // shouldPrefetch: false,
    markdown: {
        importCode: {
            handleImportPath: (str) => {
                if (str.startsWith('@project')) {
                    return str.replace(/^@project/, path.resolve(__dirname, '../../../'));
                }
                if (str.startsWith('@core-test')) {
                    return str.replace(/^@core-test/, path.resolve(__dirname, '../../../xtream-codec-core/src/test/java'));
                }
                if (str.startsWith('@core-debug-test')) {
                    return str.replace(/^@core-debug-test/, path.resolve(__dirname, '../../../debug/xtream-codec-core-debug/src/test/java'));
                }
                if (str.startsWith('@core-debug')) {
                    return str.replace(/^@core-debug/, path.resolve(__dirname, '../../../debug/xtream-codec-core-debug/src/main/java'));
                }
                return str.replace(
                    /^@src/,
                    path.resolve(__dirname, '../code-snippet/')
                );
            },
        },
    },

    plugins: [
        llmstxt({
            // 主标题(默认从站点配置或index.md中提取) | Main title (defaults to site config or extracted from index.md)
            title: 'Xtream-Codec Documentation',

            // 描述(默认从站点配置或index.md中提取) | Description (defaults to site config or extracted from index.md)
            description: 'Xtream-Codec 文档',

            // 详细说明(默认从index.md中提取或自动生成) | Details (defaults to extracted from index.md or auto-generated)
            // details: 'This file contains links to all documentation sections.',
            details: '当前文件包含 Xtream-Codec 文档的所有章节目录。',

            // 是否生成llms.txt(默认为true) | Whether to generate llms.txt (defaults to true)
            generateLLMsTxt: true,

            // 是否生成llms-full.txt(默认为true) | Whether to generate llms-full.txt (defaults to true)
            generateLLMsFullTxt: false,

            // 是否从Markdown中删除HTML标签(默认为true) | Whether to strip HTML tags from Markdown (defaults to true)
            stripHTML: true,

            // 工作目录(默认为VuePress源目录) | Working directory (defaults to VuePress source directory)
            // workDir: 'docs',

            // 忽略的文件模式(使用glob语法) | File patterns to ignore (using glob syntax)
            ignoreFiles: ['**/about.md', 'sponsor/*'],

            // 自定义域名(可选，用于生成的链接) | Custom domain (optional, used for generated links)
            domain: 'https://hylexus.github.io/xtream-codec',
        })
    ],
});
