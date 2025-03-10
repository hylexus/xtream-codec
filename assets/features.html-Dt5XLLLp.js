import{_ as l}from"./plugin-vue_export-helper-DlAUqK2U.js";import{e as t,f as a,h as i,k as e,g as h,j as r,r as d,o as p}from"./app-AYOukFpQ.js";const k={},c={class:"hint-container info"};function g(o,s){const n=d("RouteLink");return p(),t("div",null,[s[5]||(s[5]=a(`<h1 id="features" tabindex="-1"><a class="header-anchor" href="#features"><span>features</span></a></h1><h2 id="概览" tabindex="-1"><a class="header-anchor" href="#概览"><span>概览</span></a></h2><div class="language-yaml line-numbers-mode" data-highlighter="shiki" data-ext="yaml" style="--shiki-light:#383A42;--shiki-dark:#abb2bf;--shiki-light-bg:#FAFAFA;--shiki-dark-bg:#282c34;"><pre class="shiki shiki-themes one-light one-dark-pro vp-code"><code><span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">jt808-server</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">  ## ... 省略其他配置 ...</span></span>
<span class="line highlighted"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">  features</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">    ### 转发请求时使用指定的调度器(而不是上游默认的Reactor调度器)</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">    request-dispatcher-scheduler</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">      enabled</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">: </span><span style="--shiki-light:#986801;--shiki-dark:#D19A66;">false</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">    ### 请求日志(仅仅用于调试)</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">    request-logger</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">      enabled</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">: </span><span style="--shiki-light:#986801;--shiki-dark:#D19A66;">true</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">    ### 请求合并(如果不启用的话，你需要手动合并子包请求)</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">    request-combiner</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">      enabled</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">: </span><span style="--shiki-light:#986801;--shiki-dark:#D19A66;">true</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">      # 子包暂存器</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">      sub-package-storage</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">        # 最大缓存数量</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">        maximum-size</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">: </span><span style="--shiki-light:#986801;--shiki-dark:#D19A66;">1024</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">        # 缓存过期时间</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">        ttl</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">: </span><span style="--shiki-light:#50A14F;--shiki-dark:#98C379;">45s</span></span></code></pre><div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0;"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><h2 id="request-dispatcher-scheduler" tabindex="-1"><a class="header-anchor" href="#request-dispatcher-scheduler"><span>request-dispatcher-scheduler</span></a></h2><p>默认情况下，转发请求时使用的调度器是 Reactor 请求入口处所使用的调度器。</p><p>通过 <code>jt808-server.features.request-dispatcher-scheduler.enabled = true</code> 可以启用自定义调度器。</p>`,6)),i("div",c,[s[4]||(s[4]=i("p",{class:"hint-container-title"},"提示",-1)),i("ul",null,[s[2]||(s[2]=i("li",null,[e("这个配置项只是个开关，默认值为 "),i("code",null,"false"),e("。")],-1)),s[3]||(s[3]=i("li",null,[e("其对应的调度器配置在 "),i("code",null,"jt808-server.schedulers.request-dispatcher"),e(" 配置项中指定。")],-1)),i("li",null,[s[1]||(s[1]=e("参考 ")),h(n,{to:"/ext/jt/jt808/configuration/schedulers.html#request-dispatcher"},{default:r(()=>s[0]||(s[0]=[e("schedulers#request-dispatcher")])),_:1})])])]),s[6]||(s[6]=a(`<h2 id="request-logger" tabindex="-1"><a class="header-anchor" href="#request-logger"><span>request-logger</span></a></h2><p>开启之后，会在日志中打印请求的详细信息。格式如下：</p><div class="language-text line-numbers-mode" data-highlighter="shiki" data-ext="text" style="--shiki-light:#383A42;--shiki-dark:#abb2bf;--shiki-light-bg:#FAFAFA;--shiki-dark-bg:#282c34;"><pre class="shiki shiki-themes one-light one-dark-pro vp-code"><code><span class="line"><span>2025-01-21 22:37:43.600 [x8i-tcp-nio-2] INFO  i.g.h.x.c.e.j.e.l.Jt808RequestLoggerListener:41 - ===&gt; Receive [TCP/0x0200(定位数据上报)] message: requestId = 2c022850b1d14812aa3c044995e6a527, traceId = d7a6ff29ffc54121a4e2e4d40ce04203, remoteAddr = /127.0.0.1:61391, payload = 7e02004086010000000001893094655200e4000000000000000101d907f2073d336c000000000000211124114808010400000026030200003001153101002504000000001404000000011504000000fa160400000000170200001803000000ea10ffffffffffffffffffffffffffffffff02020000ef0400000000f31b017118000000000000000000000000000000000000000000000000567e</span></span>
<span class="line"><span>2025-01-21 22:37:43.657 [xnbh-parallel-1] INFO  i.g.h.x.c.e.j.e.l.Jt808RequestLoggerListener:54 - &lt;=== Send [TCP] message: requestId = 2c022850b1d14812aa3c044995e6a527, traceId = d7a6ff29ffc54121a4e2e4d40ce04203, remoteAddr = /127.0.0.1:61391, payload = 7e800140050100000000018930946552000100e4020000397e</span></span></code></pre><div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0;"><div class="line-number"></div><div class="line-number"></div></div></div><h2 id="request-combiner" tabindex="-1"><a class="header-anchor" href="#request-combiner"><span>request-combiner</span></a></h2><p>该配置项用来控制和分包请求合并相关的配置。</p><div class="language-yaml line-numbers-mode" data-highlighter="shiki" data-ext="yaml" style="--shiki-light:#383A42;--shiki-dark:#abb2bf;--shiki-light-bg:#FAFAFA;--shiki-dark-bg:#282c34;"><pre class="shiki shiki-themes one-light one-dark-pro vp-code"><code><span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">jt808-server</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">  features</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line highlighted"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">    request-combiner</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line highlighted"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">      # 是否启用分包请求合并功能</span></span>
<span class="line highlighted"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">      # 如果不启用的话，你需要手动合并子包请求</span></span>
<span class="line highlighted"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">      enabled</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">: </span><span style="--shiki-light:#986801;--shiki-dark:#D19A66;">true</span></span>
<span class="line highlighted"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">      # 子包暂存器配置</span></span>
<span class="line highlighted"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">      sub-package-storage</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line highlighted"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">        # 暂存器中最大的缓存数量</span></span>
<span class="line highlighted"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">        maximum-size</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">: </span><span style="--shiki-light:#986801;--shiki-dark:#D19A66;">1024</span></span>
<span class="line highlighted"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">        # 暂存器中子包的过期时间(长时间没有使用的缓存条目将被丢弃)</span></span>
<span class="line highlighted"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">        # 配置示例: 45s(45秒), 3m(3分钟), 1h(1小时)</span></span>
<span class="line highlighted"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">        ttl</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">: </span><span style="--shiki-light:#50A14F;--shiki-dark:#98C379;">45s</span></span></code></pre><div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0;"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div>`,6))])}const A=l(k,[["render",g],["__file","features.html.vue"]]),m=JSON.parse('{"path":"/ext/jt/jt808/configuration/features.html","title":"features","lang":"zh-CN","frontmatter":{"icon":"toggle-on","article":false,"description":"features 概览 request-dispatcher-scheduler 默认情况下，转发请求时使用的调度器是 Reactor 请求入口处所使用的调度器。 通过 jt808-server.features.request-dispatcher-scheduler.enabled = true 可以启用自定义调度器。 提示 这个配置项只是个开关，...","head":[["meta",{"property":"og:url","content":"https://mister-hope.github.io/xtream-codec/ext/jt/jt808/configuration/features.html"}],["meta",{"property":"og:site_name","content":"xtream-codec"}],["meta",{"property":"og:title","content":"features"}],["meta",{"property":"og:description","content":"features 概览 request-dispatcher-scheduler 默认情况下，转发请求时使用的调度器是 Reactor 请求入口处所使用的调度器。 通过 jt808-server.features.request-dispatcher-scheduler.enabled = true 可以启用自定义调度器。 提示 这个配置项只是个开关，..."}],["meta",{"property":"og:type","content":"website"}],["meta",{"property":"og:locale","content":"zh-CN"}],["meta",{"property":"og:updated_time","content":"2025-01-22T14:09:55.000Z"}],["meta",{"property":"article:modified_time","content":"2025-01-22T14:09:55.000Z"}],["script",{"type":"application/ld+json"},"{\\"@context\\":\\"https://schema.org\\",\\"@type\\":\\"WebPage\\",\\"name\\":\\"features\\",\\"description\\":\\"features 概览 request-dispatcher-scheduler 默认情况下，转发请求时使用的调度器是 Reactor 请求入口处所使用的调度器。 通过 jt808-server.features.request-dispatcher-scheduler.enabled = true 可以启用自定义调度器。 提示 这个配置项只是个开关，...\\"}"]]},"git":{"createdTime":1734529468000,"updatedTime":1737554995000,"contributors":[{"name":"hylexus","username":"hylexus","email":"hylexus@163.com","commits":2}]},"readingTime":{"minutes":1.4,"words":421},"filePathRelative":"ext/jt/jt808/configuration/features.md","localizedDate":"2024年12月18日","excerpt":"\\n<h2>概览</h2>\\n<div class=\\"language-yaml line-numbers-mode\\" data-highlighter=\\"shiki\\" data-ext=\\"yaml\\" style=\\"--shiki-light:#383A42;--shiki-dark:#abb2bf;--shiki-light-bg:#FAFAFA;--shiki-dark-bg:#282c34\\"><pre class=\\"shiki shiki-themes one-light one-dark-pro vp-code\\"><code><span class=\\"line\\"><span style=\\"--shiki-light:#E45649;--shiki-dark:#E06C75\\">jt808-server</span><span style=\\"--shiki-light:#383A42;--shiki-dark:#ABB2BF\\">:</span></span>\\n<span class=\\"line\\"><span style=\\"--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic\\">  ## ... 省略其他配置 ...</span></span>\\n<span class=\\"line highlighted\\"><span style=\\"--shiki-light:#E45649;--shiki-dark:#E06C75\\">  features</span><span style=\\"--shiki-light:#383A42;--shiki-dark:#ABB2BF\\">:</span></span>\\n<span class=\\"line\\"><span style=\\"--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic\\">    ### 转发请求时使用指定的调度器(而不是上游默认的Reactor调度器)</span></span>\\n<span class=\\"line\\"><span style=\\"--shiki-light:#E45649;--shiki-dark:#E06C75\\">    request-dispatcher-scheduler</span><span style=\\"--shiki-light:#383A42;--shiki-dark:#ABB2BF\\">:</span></span>\\n<span class=\\"line\\"><span style=\\"--shiki-light:#E45649;--shiki-dark:#E06C75\\">      enabled</span><span style=\\"--shiki-light:#383A42;--shiki-dark:#ABB2BF\\">: </span><span style=\\"--shiki-light:#986801;--shiki-dark:#D19A66\\">false</span></span>\\n<span class=\\"line\\"><span style=\\"--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic\\">    ### 请求日志(仅仅用于调试)</span></span>\\n<span class=\\"line\\"><span style=\\"--shiki-light:#E45649;--shiki-dark:#E06C75\\">    request-logger</span><span style=\\"--shiki-light:#383A42;--shiki-dark:#ABB2BF\\">:</span></span>\\n<span class=\\"line\\"><span style=\\"--shiki-light:#E45649;--shiki-dark:#E06C75\\">      enabled</span><span style=\\"--shiki-light:#383A42;--shiki-dark:#ABB2BF\\">: </span><span style=\\"--shiki-light:#986801;--shiki-dark:#D19A66\\">true</span></span>\\n<span class=\\"line\\"><span style=\\"--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic\\">    ### 请求合并(如果不启用的话，你需要手动合并子包请求)</span></span>\\n<span class=\\"line\\"><span style=\\"--shiki-light:#E45649;--shiki-dark:#E06C75\\">    request-combiner</span><span style=\\"--shiki-light:#383A42;--shiki-dark:#ABB2BF\\">:</span></span>\\n<span class=\\"line\\"><span style=\\"--shiki-light:#E45649;--shiki-dark:#E06C75\\">      enabled</span><span style=\\"--shiki-light:#383A42;--shiki-dark:#ABB2BF\\">: </span><span style=\\"--shiki-light:#986801;--shiki-dark:#D19A66\\">true</span></span>\\n<span class=\\"line\\"><span style=\\"--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic\\">      # 子包暂存器</span></span>\\n<span class=\\"line\\"><span style=\\"--shiki-light:#E45649;--shiki-dark:#E06C75\\">      sub-package-storage</span><span style=\\"--shiki-light:#383A42;--shiki-dark:#ABB2BF\\">:</span></span>\\n<span class=\\"line\\"><span style=\\"--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic\\">        # 最大缓存数量</span></span>\\n<span class=\\"line\\"><span style=\\"--shiki-light:#E45649;--shiki-dark:#E06C75\\">        maximum-size</span><span style=\\"--shiki-light:#383A42;--shiki-dark:#ABB2BF\\">: </span><span style=\\"--shiki-light:#986801;--shiki-dark:#D19A66\\">1024</span></span>\\n<span class=\\"line\\"><span style=\\"--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic\\">        # 缓存过期时间</span></span>\\n<span class=\\"line\\"><span style=\\"--shiki-light:#E45649;--shiki-dark:#E06C75\\">        ttl</span><span style=\\"--shiki-light:#383A42;--shiki-dark:#ABB2BF\\">: </span><span style=\\"--shiki-light:#50A14F;--shiki-dark:#98C379\\">45s</span></span></code></pre>\\n<div class=\\"line-numbers\\" aria-hidden=\\"true\\" style=\\"counter-reset:line-number 0\\"><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div><div class=\\"line-number\\"></div></div></div>","autoDesc":true}');export{A as comp,m as data};
