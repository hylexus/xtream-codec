import {hopeTheme} from "vuepress-theme-hope";
import {zhNavbar} from "./navbar";
import {zhSidebar} from "./sidebar";
import {path} from "vuepress/utils"


export default hopeTheme({
    hostname: "https://hylexus.github.io",
    author: {
        name: "xtream-codec",
        url: "https://github.com/hylexus",
    },

    logo: "/logo.svg",

    repo: "https://github.com/hylexus/xtream-codec",

    docsDir: "src",

    blog: {
        medias: {
            Gitee: "https://gitee.com/hylexus/xtream-codec",
            GitHub: "https://github.com/hylexus/xtream-codec",
        },
    },
    navbarLayout: {
        start: ["Brand"],
        center: ["Links"],
        end: ["RepoGitee", "Repo", 'SocialLink', "Outlook", "Search"],
    },
    locales: {
        /**
         * Chinese locale config
         */
        "/": {
            // navbar
            navbar: zhNavbar,

            // sidebar
            sidebar: zhSidebar,

            // footer: "默认页脚",

            displayFooter: true,

            blog: {
                description: "......",
                intro: "/core/index.html",
            },

            // page meta
            metaLocales: {
                editLink: "在 GitHub 上编辑此页",
            },
        },
    },

    // enable it to preview all changes in time
    hotReload: true,
    markdown: {
        component: true,
        figure: true,
        imgLazyload: true,
        imgSize: true,
        imgMark: true,
        tabs: true,
        include: {
            resolvePath: (file) => {
                if (file.startsWith("@src"))
                    return file.replace("@src", path.resolve(__dirname, ".."));

                return file;
            },
        },
        align: true,
        attrs: true,
        demo: true,
        mark: true,
        stylize: [
            {
                matcher: "Recommended",
                replacer: ({tag}) => {
                    if (tag === "em")
                        return {
                            tag: "Badge",
                            attrs: {type: "tip"},
                            content: "Recommended",
                        };
                },
            },
        ],
        sub: true,
        sup: true,
        vPre: true,
    },
    plugins: {
        slimsearch: true,
        icon: {
            // https://theme-hope.vuejs.press/zh/guide/interface/icon.html
            assets: "fontawesome-with-brands"
        },
        blog: true,
        components: {
            components: ["Badge", "VPCard", "VPBanner"],
        },
    },
});
