import{_ as n}from"./plugin-vue_export-helper-DlAUqK2U.js";import{e as l,h as t,f as r,k as o,g as i,j as c,r as a,o as s}from"./app-AYOukFpQ.js";const m="/xtream-codec/img/faq/code-analysis/field-codec.png",p={};function u(y,e){const d=a("RouteLink");return s(),l("div",null,[e[9]||(e[9]=t("h1",{id:"源码阅读建议",tabindex:"-1"},[t("a",{class:"header-anchor",href:"#源码阅读建议"},[t("span",null,"源码阅读建议")])],-1)),e[10]||(e[10]=t("h2",{id:"codec-core",tabindex:"-1"},[t("a",{class:"header-anchor",href:"#codec-core"},[t("span",null,"codec-core")])],-1)),t("p",null,[e[1]||(e[1]=o("这里介绍的是 ")),i(d,{to:"/guide/core/quick-start/quick-start.html"},{default:c(()=>e[0]||(e[0]=[o("xtream-codec-core")])),_:1}),e[2]||(e[2]=o(" 模块的源码阅读建议。可以从下面几个关键组件入手："))]),e[11]||(e[11]=r('<ul><li><code>EntityEncoder</code>: 基于 <code>@XtreamField</code> 注解的编码器实现</li><li><code>EntityDecoder</code>: 基于 <code>@XtreamField</code> 注解的解码器实现</li><li><code>EntityCodec</code>: <code>EntityEncoder</code> 和 <code>EntityDecoder</code> 的组合</li><li><code>FieldCodec</code>: 对 <code>@XtreamField</code> 注解的元数据封装</li></ul><p>下面是一些内置的 <code>FieldCodec</code> 实现：</p><figure><img src="'+m+'" alt="内置 FieldCodec" tabindex="0" loading="lazy"><figcaption>内置 FieldCodec</figcaption></figure><h2 id="codec-server-reactive" tabindex="-1"><a class="header-anchor" href="#codec-server-reactive"><span>codec-server-reactive</span></a></h2>',4)),t("p",null,[e[4]||(e[4]=o("这里介绍的是 ")),i(d,{to:"/guide/server/quick-start/terminology.html"},{default:c(()=>e[3]||(e[3]=[o("xtream-codec-server-reactive")])),_:1}),e[5]||(e[5]=o(" 模块的源码阅读建议。可以从下面几个关键组件入手："))]),e[12]||(e[12]=r('<ul><li><code>XtreamNettyHandlerAdapter</code>: 从这里开始接管了 <strong>ReactorNetty</strong> 收到的数据</li><li><code>XtreamExchangeCreator</code>: 封装请求信息</li><li><code>XtreamHandler</code>: 请求处理器 <ul><li><code>DispatcherXtreamHandler</code>: 默认 <code>XtreamHandler</code> 实现 <ul><li><code>XtreamHandlerMapping</code></li><li><code>XtreamHandlerAdapter</code></li><li><code>XtreamHandlerResultHandler</code></li></ul></li><li><code>FilteringXtreamHandler</code>: 具有 <code>Filter</code> 能力的 <code>XtreamHandler</code> 实现 <ul><li><code>XtreamFilterChain</code></li><li><code>XtreamFilter</code></li></ul></li><li><code>ExceptionHandlingXtreamHandler</code>: 具有异常处理能力的 <code>XtreamHandler</code> 实现 <ul><li><code>XtreamRequestExceptionHandler</code></li></ul></li></ul></li></ul><h2 id="jt-808-server扩展" tabindex="-1"><a class="header-anchor" href="#jt-808-server扩展"><span>jt-808-server扩展</span></a></h2>',2)),t("p",null,[e[7]||(e[7]=o("这里介绍的是 ")),i(d,{to:"/ext/jt/jt808/quick-start/terminology.html"},{default:c(()=>e[6]||(e[6]=[o("ext/jt/jt-808-server-spring-boot-starter-reactive")])),_:1}),e[8]||(e[8]=o(" 模块的源码阅读建议。可以从下面几个关键组件入手："))]),e[13]||(e[13]=r("<ul><li><code>BuiltinJt808InstructionServerExchangeCreator 和 BuiltinJt808AttachmentServerExchangeCreator</code>: 封装 <strong>JT/T 808</strong> 请求信息</li><li><code>Jt808RequestMappingHandlerMapping</code>: 请求映射(确定请求处理器 <code>@Jt808RequestHandlerMapping</code>)</li><li><code>Jt808ResponseBodyHandlerResultHandler</code>: 响应体处理器(<code>@Jt808ResponseBody</code>)</li><li><code>Jt808RequestLifecycleListener</code>: 请求生命周期监听器</li><li><code>Jt808RequestCombinerFilter</code>: 分包合并</li></ul>",1))])}const f=n(p,[["render",u],["__file","code-analysis.html.vue"]]),x=JSON.parse('{"path":"/frequently-asked-questions/code-analysis.html","title":"源码阅读建议","lang":"zh-CN","frontmatter":{"icon":"book","article":false,"description":"源码阅读建议 codec-core 这里介绍的是 模块的源码阅读建议。可以从下面几个关键组件入手： EntityEncoder: 基于 @XtreamField 注解的编码器实现 EntityDecoder: 基于 @XtreamField 注解的解码器实现 EntityCodec: EntityEncoder 和 EntityDecoder 的组合 ...","head":[["meta",{"property":"og:url","content":"https://mister-hope.github.io/xtream-codec/frequently-asked-questions/code-analysis.html"}],["meta",{"property":"og:site_name","content":"xtream-codec"}],["meta",{"property":"og:title","content":"源码阅读建议"}],["meta",{"property":"og:description","content":"源码阅读建议 codec-core 这里介绍的是 模块的源码阅读建议。可以从下面几个关键组件入手： EntityEncoder: 基于 @XtreamField 注解的编码器实现 EntityDecoder: 基于 @XtreamField 注解的解码器实现 EntityCodec: EntityEncoder 和 EntityDecoder 的组合 ..."}],["meta",{"property":"og:type","content":"website"}],["meta",{"property":"og:image","content":"https://mister-hope.github.io/xtream-codec/img/faq/code-analysis/field-codec.png"}],["meta",{"property":"og:locale","content":"zh-CN"}],["meta",{"property":"og:updated_time","content":"2025-03-02T09:43:59.000Z"}],["meta",{"property":"article:modified_time","content":"2025-03-02T09:43:59.000Z"}],["script",{"type":"application/ld+json"},"{\\"@context\\":\\"https://schema.org\\",\\"@type\\":\\"WebPage\\",\\"name\\":\\"源码阅读建议\\",\\"description\\":\\"源码阅读建议 codec-core 这里介绍的是 模块的源码阅读建议。可以从下面几个关键组件入手： EntityEncoder: 基于 @XtreamField 注解的编码器实现 EntityDecoder: 基于 @XtreamField 注解的解码器实现 EntityCodec: EntityEncoder 和 EntityDecoder 的组合 ...\\"}"]]},"git":{"createdTime":1734792660000,"updatedTime":1740908639000,"contributors":[{"name":"hylexus","username":"hylexus","email":"hylexus@163.com","commits":2}]},"readingTime":{"minutes":0.98,"words":294},"filePathRelative":"frequently-asked-questions/code-analysis.md","localizedDate":"2024年12月21日","excerpt":"\\n<h2>codec-core</h2>\\n<p>这里介绍的是 <a href=\\"/xtream-codec/guide/core/quick-start/quick-start.html\\" target=\\"_blank\\">xtream-codec-core</a> 模块的源码阅读建议。可以从下面几个关键组件入手：</p>\\n<ul>\\n<li><code>EntityEncoder</code>: 基于 <code>@XtreamField</code> 注解的编码器实现</li>\\n<li><code>EntityDecoder</code>: 基于 <code>@XtreamField</code> 注解的解码器实现</li>\\n<li><code>EntityCodec</code>: <code>EntityEncoder</code> 和 <code>EntityDecoder</code> 的组合</li>\\n<li><code>FieldCodec</code>: 对 <code>@XtreamField</code> 注解的元数据封装</li>\\n</ul>","autoDesc":true}');export{f as comp,x as data};
