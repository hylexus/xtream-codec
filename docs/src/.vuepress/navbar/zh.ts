import {navbar} from "vuepress-theme-hope";

export const zhNavbar = navbar([
    // "/",
    {
        text: "指南",
        icon: "signs-post",
        link: '/guide/'
    },
    {
        text: "内置扩展",
        icon: "ethernet",
        link: '/ext/jt/jt808/'
    },
    {
        text: "FAQ",
        icon: "circle-question",
        link: '/frequently-asked-questions/'
    },
    {
        text: "发版记录",
        icon: "code-branch",
        link: '/release-notes/latest.md'
    },
    {
        text: '致谢',
        icon: 'gift',
        children: [
            {text: 'Reactor Netty', link: 'https://projectreactor.io', icon: 'leaf'},
            {text: 'Netty', link: 'https://github.com/netty/netty', icon: 'leaf'},
            {text: 'Spring', icon: 'leaf', link: 'https://github.com/spring-projects'},
            {
                text: 'Vue', children: [
                    {text: 'VuePress', icon: 'fa-brands fa-vuejs', link: 'https://www.vuepress.cn/'},
                    {text: 'vuepress-theme-hope', icon: 'fa-brands fa-vuejs', link: 'https://theme-hope.vuejs.press/zh/'},
                ],
            },
            {text: 'NetworkCat（App Store）', icon: 'ethernet', link: 'https://apps.apple.com/cn/app/networkcat/id6503148471?mt=12'},
        ]
    },
    {
        text: '免责声明',
        icon: 'scale-balanced',
        link: '/disclaimer.md'
    },
    {
        text: '关于我们',
        icon: 'copyright',
        link: '/about.md'
    },
]);
