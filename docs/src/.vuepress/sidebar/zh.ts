import {sidebar} from "vuepress-theme-hope";

export const zhSidebar = sidebar({
    "/guide/": [
        {
            text: "codec-core",
            icon: "book",
            collapsible: true,
            children: [
                {
                    text: "入门", icon: 'rocket', collapsible: false,
                    children: [
                        '/guide/core/quick-start/intro.md',
                        '/guide/core/quick-start/quick-start.md',
                        '/guide/core/quick-start/building-from-source.md',
                        '/guide/core/quick-start/code-analysis.md',
                    ]
                },
                // {
                //     text: "核心组件", icon: 'class', collapsible: false,
                //     children: [
                //         '/guide/core/core-component/entity-codec.md',
                //         '/guide/core/core-component/field-codec.md',
                //     ]
                // },
                {
                    text: "注解驱动开发", icon: "at", collapsible: false,
                    children: [
                        '/guide/core/annotation-driven/data-types.md',
                        // '/guide/core/annotation-driven/index.md',
                        '/guide/core/annotation-driven/xtream-field-annotation.md',
                        '/guide/core/annotation-driven/builtin-annotations.md',
                        '/guide/core/annotation-driven/custom-annotation.md',
                        '/guide/core/annotation-driven/entity-codec.md',
                        '/guide/core/annotation-driven/entity-codec-tracker.md',
                        {
                            text: "附录(建议阅读)",
                            icon: "fa-solid fa-book-open-reader", collapsible: false,
                            children: [
                                '/guide/core/annotation-driven/annex/',
                                '/guide/core/annotation-driven/annex/prepend-length-field.md',
                                '/guide/core/annotation-driven/annex/padding.md',
                            ]
                        }
                    ]
                },
                {
                    text: "示例", icon: "server", collapsible: false,
                    children: [
                        {
                            text: "示例1", icon: "server", collapsible: false, children: [
                                '/guide/core/samples/custom-protocol-sample-01/protocol.md',
                                '/guide/core/samples/custom-protocol-sample-01/flatten-style-demo.md',
                                '/guide/core/samples/custom-protocol-sample-01/nested-style-demo.md',
                            ]
                        },
                        {
                            text: "示例2", icon: "server", collapsible: false, children: [
                                '/guide/core/samples/custom-protocol-sample-02/protocol.md',
                                '/guide/core/samples/custom-protocol-sample-02/flatten-style-demo.md',
                                '/guide/core/samples/custom-protocol-sample-02/nested-style-demo.md',
                            ]
                        },
                    ]
                },
            ]
        },
        {
            text: "codec-server-reactive",
            icon: "server",
            collapsible: true,
            children: [
                {
                    text: "入门", icon: 'rocket', collapsible: false,
                    children: [
                        '/guide/server/quick-start/intro.md',
                        '/guide/server/quick-start/terminology.md',
                        '/guide/server/quick-start/building-from-source.md',
                        '/guide/server/quick-start/code-analysis.md',
                    ]
                },
                {
                    text: "核心组件", icon: 'layer-group', collapsible: false, children: [
                        '/guide/server/core-component/netty-handler-adapter.md',
                        '/guide/server/core-component/xtream-handler.md',
                        '/guide/server/core-component/session-manager.md',
                        '/guide/server/core-component/command-sender.md',
                        '/guide/server/core-component/scheduler-registry.md',
                        '/guide/server/core-component/ordered-component.md',
                    ]
                },
                {
                    text: "内置请求处理流程", icon: "microchip", collapsible: false, children: [
                        '/guide/server/request-processing/intro.md',
                        '/guide/server/request-processing/dispatcher-handler.md',
                        '/guide/server/request-processing/filter.md',
                        '/guide/server/request-processing/filtering-xtream-handler.md',
                        '/guide/server/request-processing/request-exception-handler.md',
                        '/guide/server/request-processing/exception-handling-xtream-handler.md',
                    ]
                },
                {
                    text: "注解驱动开发", icon: "at", collapsible: false, children: [
                        '/guide/server/annotation-driven/xtream-request-handler-mapping.md',
                        '/guide/server/annotation-driven/handler-method-argument-resolver.md',
                    ]
                },
            ]
        }
    ],
    "/ext/": [
        {
            text: "JT/T 808 扩展",
            icon: "earth-asia",
            children: [
                {
                    text: "入门", icon: "rocket", collapsible: false, children: [
                        '/ext/jt/jt808/quick-start/intro.md',
                        '/ext/jt/jt808/quick-start/quick-start.md',
                        '/ext/jt/jt808/quick-start/terminology.md',
                        '/ext/jt/jt808/quick-start/request-processing.md',
                        '/ext/jt/jt808/quick-start/building-from-source.md',
                        '/ext/jt/jt808/quick-start/code-analysis.md',
                    ]
                },
                {
                    text: "注解驱动开发", icon: "at", collapsible: false, children: [
                        '/ext/jt/jt808/annotation-driven/overview.md',
                        '/ext/jt/jt808/annotation-driven/request-message-mapping.md',
                        '/ext/jt/jt808/annotation-driven/argument-resolver.md',
                        '/ext/jt/jt808/annotation-driven/response-message-mapping.md',
                        '/ext/jt/jt808/annotation-driven/builtin-message.md',
                    ]
                },
                {
                    text: "复杂消息解析示例", icon: "at", collapsible: false, children: [
                        // '/ext/jt/jt808/complex-message-mapping/README.md',
                        '/ext/jt/jt808/complex-message-mapping/sample-0x0104.md',
                        '/ext/jt/jt808/complex-message-mapping/sample-0x0200.md',
                        '/ext/jt/jt808/complex-message-mapping/sample-0x0608.md',
                        '/ext/jt/jt808/complex-message-mapping/sample-0x0704.md',
                        '/ext/jt/jt808/complex-message-mapping/sample-0x0802.md',
                        '/ext/jt/jt808/complex-message-mapping/sample-0x8105.md',
                        '/ext/jt/jt808/complex-message-mapping/sample-0x8500.md',
                        '/ext/jt/jt808/complex-message-mapping/sample-0x8600.md',
                        '/ext/jt/jt808/complex-message-mapping/sample-0x8602.md',
                        '/ext/jt/jt808/complex-message-mapping/sample-0x8604.md',
                        '/ext/jt/jt808/complex-message-mapping/sample-0x8606.md',
                    ]
                },
                {
                    text: "辅助工具", icon: "wrench", collapsible: false, children: [
                        '/ext/jt/jt808/utilities/bit-operator.md',
                        '/ext/jt/jt808/utilities/response-encoder.md',
                    ]
                },
                {
                    text: "定制化", icon: "gear", collapsible: false, children: [
                        '/ext/jt/jt808/customization/netty.md',
                        '/ext/jt/jt808/customization/codec.md',
                        '/ext/jt/jt808/customization/scheduler.md',
                        '/ext/jt/jt808/customization/sub-package.md',
                        '/ext/jt/jt808/customization/listener.md',
                        '/ext/jt/jt808/customization/filter.md',
                        '/ext/jt/jt808/customization/encryption.md',
                        '/ext/jt/jt808/customization/argument-resolver.md',
                        '/ext/jt/jt808/customization/request-handler.md',
                    ]
                },
                {
                    text: "地方标准", icon: "file-lines", collapsible: false, children: [
                        '/ext/jt/jt808/extension/jiangsu.md',
                    ]
                },
                {
                    text: "Dashboard", icon: "gauge", collapsible: false, children: [
                        '/ext/jt/jt808/dashboard/intro.md',
                        '/ext/jt/jt808/dashboard/quick-start.md',
                    ]
                },
                {
                    text: "配置", icon: "gears", collapsible: false, children: [
                        '/ext/jt/jt808/configuration/overview.md',
                        '/ext/jt/jt808/configuration/features.md',
                        '/ext/jt/jt808/configuration/instruction-server.md',
                        '/ext/jt/jt808/configuration/attachment-server.md',
                        '/ext/jt/jt808/configuration/schedulers.md',
                    ]
                },
            ]
        }
    ],
    "/frequently-asked-questions/": [
        '/frequently-asked-questions/project-positioning.md',
        '/frequently-asked-questions/differences-between-xtream-codec-and-jt-framework.md',
        '/frequently-asked-questions/building-from-source.md',
        '/frequently-asked-questions/code-analysis.md',
    ],
    "/release-notes": [
        {text: 'Latest', link: '/release-notes/latest.md', icon: 'code-branch'},
        {text: 'v-0.1.x(Latest)', link: '/release-notes/0.1.x.md', icon: 'code-branch'},
        '/release-notes/0.0.x.md',
    ],
});
