import{_ as i}from"./plugin-vue_export-helper-DlAUqK2U.js";import{c as a,a as e,o as n}from"./app-DuUg9O0T.js";const l={};function t(h,s){return n(),a("div",null,s[0]||(s[0]=[e(`<h1 id="调度器" tabindex="-1"><a class="header-anchor" href="#调度器"><span>调度器</span></a></h1><h2 id="作用" tabindex="-1"><a class="header-anchor" href="#作用"><span>作用</span></a></h2><p>这里介绍的是 <strong>reactor</strong> 中 <code>Scheduler</code> 的配置。<code>Scheduler</code> 决定了你的代码在哪个线程上执行。</p><p>只要将你自定义的调度器注册到 <code>XtreamSchedulerRegistry</code> 中即可。</p><h2 id="自定义方式1" tabindex="-1"><a class="header-anchor" href="#自定义方式1"><span>自定义方式1</span></a></h2><p>直接调用 <code>XtreamSchedulerRegistry.registerScheduler()</code> 方法注册。</p><h2 id="自定义方式2" tabindex="-1"><a class="header-anchor" href="#自定义方式2"><span>自定义方式2</span></a></h2><p>提供了一种通过配置文件快速自定义的配置方式。</p><p>下面示例中注册了两个调度器，名称分别为 <code>my-scheduler-1</code> 和 <code>my-scheduler-2</code>。</p><div class="language-yaml line-numbers-mode" data-highlighter="shiki" data-ext="yaml" data-title="yaml" style="--shiki-light:#383A42;--shiki-dark:#abb2bf;--shiki-light-bg:#FAFAFA;--shiki-dark-bg:#282c34;"><pre class="shiki shiki-themes one-light one-dark-pro vp-code"><code><span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">jt808-server</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">  schedulers</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">    custom-schedulers</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">      my-scheduler-1</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">        # name可以不配置(默认使用 Key)</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">        # name: my-scheduler-1</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">        type</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">: </span><span style="--shiki-light:#50A14F;--shiki-dark:#98C379;">parallel</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">        parallel</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">          daemon</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">: </span><span style="--shiki-light:#986801;--shiki-dark:#D19A66;">true</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">          # ...</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">      my-scheduler-2</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">        name</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">: </span><span style="--shiki-light:#50A14F;--shiki-dark:#98C379;">my-scheduler-2</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">        type</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">: </span><span style="--shiki-light:#50A14F;--shiki-dark:#98C379;">bounded_elastic</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">        bounded-elastic</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">:</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E06C75;">          thread-name-prefix</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">: </span><span style="--shiki-light:#50A14F;--shiki-dark:#98C379;">my-scheduler-2</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">          # ...</span></span></code></pre><div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0;"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><p>然后就可以指定处理器运行在你自定义的 调度器上了：</p><div class="language-java line-numbers-mode" data-highlighter="shiki" data-ext="java" data-title="java" style="--shiki-light:#383A42;--shiki-dark:#abb2bf;--shiki-light-bg:#FAFAFA;--shiki-dark-bg:#282c34;"><pre class="shiki shiki-themes one-light one-dark-pro vp-code"><code><span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">@</span><span style="--shiki-light:#A626A4;--shiki-dark:#E5C07B;">Component</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">@</span><span style="--shiki-light:#A626A4;--shiki-dark:#E5C07B;">Jt808RequestHandler</span></span>
<span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">public</span><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;"> class</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> Jt808QuickStartRequestHandler</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;"> {</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">    @</span><span style="--shiki-light:#A626A4;--shiki-dark:#E5C07B;">Jt808RequestHandlerMapping</span><span style="--shiki-light:#383A42;--shiki-dark:#E06C75;">(</span></span>
<span class="line"><span style="--shiki-light:#986801;--shiki-dark:#D19A66;">            messageIds</span><span style="--shiki-light:#383A42;--shiki-dark:#56B6C2;"> =</span><span style="--shiki-light:#986801;--shiki-dark:#D19A66;"> 0x0200</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">,</span><span style="--shiki-light:#986801;--shiki-dark:#D19A66;"> versions</span><span style="--shiki-light:#383A42;--shiki-dark:#56B6C2;"> =</span><span style="--shiki-light:#E45649;--shiki-dark:#E5C07B;"> Jt808ProtocolVersion</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">.</span><span style="--shiki-light:#E45649;--shiki-dark:#E5C07B;">VERSION_2019</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">,</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">            // 使用 XtreamSchedulerRegistry 中注册的名为 &quot;my-scheduler-1&quot; 的调度器</span></span>
<span class="line highlighted"><span style="--shiki-light:#986801;--shiki-dark:#D19A66;">            scheduler</span><span style="--shiki-light:#383A42;--shiki-dark:#56B6C2;"> =</span><span style="--shiki-light:#50A14F;--shiki-dark:#98C379;"> &quot;my-scheduler-1&quot;</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#E06C75;">    )</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">    @</span><span style="--shiki-light:#A626A4;--shiki-dark:#E5C07B;">Jt808ResponseBody</span><span style="--shiki-light:#383A42;--shiki-dark:#E06C75;">(</span><span style="--shiki-light:#986801;--shiki-dark:#D19A66;">messageId</span><span style="--shiki-light:#383A42;--shiki-dark:#56B6C2;"> =</span><span style="--shiki-light:#986801;--shiki-dark:#D19A66;"> 0x8001</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">,</span><span style="--shiki-light:#986801;--shiki-dark:#D19A66;"> maxPackageSize</span><span style="--shiki-light:#383A42;--shiki-dark:#56B6C2;"> =</span><span style="--shiki-light:#986801;--shiki-dark:#D19A66;"> 1000</span><span style="--shiki-light:#383A42;--shiki-dark:#E06C75;">)</span></span>
<span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">    public</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> Mono</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&lt;</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">ServerCommonReplyMessage</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&gt;</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> processMessage0200V2019</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">(</span></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">            Jt808Session</span><span style="--shiki-light:#383A42;--shiki-light-font-style:inherit;--shiki-dark:#E06C75;--shiki-dark-font-style:italic;"> session</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">,</span></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">            Jt808Request</span><span style="--shiki-light:#383A42;--shiki-light-font-style:inherit;--shiki-dark:#E06C75;--shiki-dark-font-style:italic;"> request</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">,</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">            @</span><span style="--shiki-light:#A626A4;--shiki-dark:#E5C07B;">Jt808RequestBody</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> BuiltinMessage0200</span><span style="--shiki-light:#383A42;--shiki-light-font-style:inherit;--shiki-dark:#E06C75;--shiki-dark-font-style:italic;"> body</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">)</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;"> {</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E5C07B;">        log</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">.</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;">info</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">(</span><span style="--shiki-light:#50A14F;--shiki-dark:#98C379;">&quot;v2019-0x0200: {}&quot;</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">, body);</span></span>
<span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">        return</span><span style="--shiki-light:#E45649;--shiki-dark:#E5C07B;"> this</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">.</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;">processLocationMessage</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">(session, body).</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;">map</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">(result </span><span style="--shiki-light:#C18401;--shiki-dark:#C678DD;">-&gt;</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;"> {</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">            // ...</span></span>
<span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">            return</span><span style="--shiki-light:#E45649;--shiki-dark:#E5C07B;"> ServerCommonReplyMessage</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">.</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;">of</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">(request, result);</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">        });</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">    }</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">}</span></span></code></pre><div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0;"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div>`,12)]))}const r=i(l,[["render",t],["__file","scheduler.html.vue"]]),d=JSON.parse('{"path":"/ext/jt/jt808/customization/scheduler.html","title":"调度器","lang":"zh-CN","frontmatter":{"icon":"arrows-rotate","article":false,"description":"调度器 作用 这里介绍的是 reactor 中 Scheduler 的配置。Scheduler 决定了你的代码在哪个线程上执行。 只要将你自定义的调度器注册到 XtreamSchedulerRegistry 中即可。 自定义方式1 直接调用 XtreamSchedulerRegistry.registerScheduler() 方法注册。 自定义方式2...","head":[["meta",{"property":"og:url","content":"https://mister-hope.github.io/xtream-codec/ext/jt/jt808/customization/scheduler.html"}],["meta",{"property":"og:site_name","content":"xtream-codec"}],["meta",{"property":"og:title","content":"调度器"}],["meta",{"property":"og:description","content":"调度器 作用 这里介绍的是 reactor 中 Scheduler 的配置。Scheduler 决定了你的代码在哪个线程上执行。 只要将你自定义的调度器注册到 XtreamSchedulerRegistry 中即可。 自定义方式1 直接调用 XtreamSchedulerRegistry.registerScheduler() 方法注册。 自定义方式2..."}],["meta",{"property":"og:type","content":"website"}],["meta",{"property":"og:locale","content":"zh-CN"}],["meta",{"property":"og:updated_time","content":"2024-12-15T10:01:18.000Z"}],["meta",{"property":"article:modified_time","content":"2024-12-15T10:01:18.000Z"}],["script",{"type":"application/ld+json"},"{\\"@context\\":\\"https://schema.org\\",\\"@type\\":\\"WebPage\\",\\"name\\":\\"调度器\\",\\"description\\":\\"调度器 作用 这里介绍的是 reactor 中 Scheduler 的配置。Scheduler 决定了你的代码在哪个线程上执行。 只要将你自定义的调度器注册到 XtreamSchedulerRegistry 中即可。 自定义方式1 直接调用 XtreamSchedulerRegistry.registerScheduler() 方法注册。 自定义方式2...\\"}"]]},"headers":[{"level":2,"title":"作用","slug":"作用","link":"#作用","children":[]},{"level":2,"title":"自定义方式1","slug":"自定义方式1","link":"#自定义方式1","children":[]},{"level":2,"title":"自定义方式2","slug":"自定义方式2","link":"#自定义方式2","children":[]}],"git":{"createdTime":1734256878000,"updatedTime":1734256878000,"contributors":[{"name":"hylexus","username":"hylexus","email":"hylexus@163.com","commits":1}]},"readingTime":{"minutes":0.86,"words":259},"filePathRelative":"ext/jt/jt808/customization/scheduler.md","localizedDate":"2024年12月15日","excerpt":"\\n<h2>作用</h2>\\n<p>这里介绍的是 <strong>reactor</strong> 中 <code>Scheduler</code> 的配置。<code>Scheduler</code> 决定了你的代码在哪个线程上执行。</p>\\n<p>只要将你自定义的调度器注册到 <code>XtreamSchedulerRegistry</code> 中即可。</p>\\n<h2>自定义方式1</h2>\\n<p>直接调用 <code>XtreamSchedulerRegistry.registerScheduler()</code> 方法注册。</p>\\n<h2>自定义方式2</h2>\\n<p>提供了一种通过配置文件快速自定义的配置方式。</p>","autoDesc":true}');export{r as comp,d as data};