import{_ as i}from"./plugin-vue_export-helper-DlAUqK2U.js";import{c as a,a as n,o as e}from"./app-DuUg9O0T.js";const l={};function t(h,s){return e(),a("div",null,s[0]||(s[0]=[n(`<h1 id="基本术语" tabindex="-1"><a class="header-anchor" href="#基本术语"><span>基本术语</span></a></h1><h2 id="xtreamrequest" tabindex="-1"><a class="header-anchor" href="#xtreamrequest"><span>XtreamRequest</span></a></h2><div class="hint-container tip"><p class="hint-container-title">提示</p><p><code>XtreamRequest</code> 借鉴于 <code>org.springframework.http.server.reactive.ServerHttpRequest</code></p></div><ul><li>客户端报文中的字节流最终会解析到 <code>XtreamRequest</code> 里。</li><li>同时会将本次请求的其他元数据封装到 <code>XtreamRequest</code> 中， 例如：对方 IP 地址、协议类型等。</li><li><code>XtreamRequest</code> 是不可变对象( <strong>Immutable</strong>)。一旦创建便不可修改。</li><li>通过 <code>request.mutate()</code> 方法可以从当前实例构建出一个新实例的 <code>Builder</code>。</li></ul><p><code>XtreamRequest</code> 接口内容如下：</p><div class="language-java line-numbers-mode" data-highlighter="shiki" data-ext="java" data-title="java" style="--shiki-light:#383A42;--shiki-dark:#abb2bf;--shiki-light-bg:#FAFAFA;--shiki-dark-bg:#282c34;"><pre class="shiki shiki-themes one-light one-dark-pro vp-code"><code><span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">public</span><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;"> interface</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> XtreamRequest</span><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;"> extends</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> XtreamInbound</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;"> {</span></span>
<span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">    enum</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> Type</span><span style="--shiki-light:#383A42;--shiki-dark:#E06C75;"> {</span></span>
<span class="line"><span style="--shiki-light:#986801;--shiki-dark:#D19A66;">        TCP</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">,</span></span>
<span class="line"><span style="--shiki-light:#986801;--shiki-dark:#D19A66;">        UDP</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#E06C75;">    }</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    Type</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> type</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    ByteBufAllocator</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> bufferFactory</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    NettyInbound</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> underlyingInbound</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    Channel</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> underlyingChannel</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">    /**</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">     * 同一个 ”网络包“ 的请求和响应，该值应该确保一致（即使是在中途重新包装或修改了 {@link XtreamRequest} 对象）。</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">     */</span></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    String</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> requestId</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    InetSocketAddress</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> remoteAddress</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    ByteBuf</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> payload</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    Map</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&lt;</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">String</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">,</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> Object</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&gt;</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> attributes</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">    default</span><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;"> void</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> release</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">()</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;"> {</span></span>
<span class="line"><span style="--shiki-light:#E45649;--shiki-dark:#E5C07B;">        XtreamBytes</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">.</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;">releaseBuf</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">(</span><span style="--shiki-light:#E45649;--shiki-dark:#E5C07B;">this</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">.</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;">payload</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">());</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">    }</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    XtreamRequestBuilder</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> mutate</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">    @</span><span style="--shiki-light:#A626A4;--shiki-dark:#E5C07B;">SuppressWarnings</span><span style="--shiki-light:#383A42;--shiki-dark:#E06C75;">(</span><span style="--shiki-light:#50A14F;--shiki-dark:#98C379;">&quot;unchecked&quot;</span><span style="--shiki-light:#383A42;--shiki-dark:#E06C75;">)</span></span>
<span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">    default</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;"> &lt;</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">T</span><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;"> extends</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> XtreamRequest</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&gt;</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> T</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> castAs</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">(</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">Class</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&lt;</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">T</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&gt; </span><span style="--shiki-light:#383A42;--shiki-light-font-style:inherit;--shiki-dark:#E06C75;--shiki-dark-font-style:italic;">ignored</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">)</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;"> {</span></span>
<span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">        return</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;"> (T) </span><span style="--shiki-light:#E45649;--shiki-dark:#E5C07B;">this</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">;</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">    }</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">}</span></span></code></pre><div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0;"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><h2 id="xtreamresponse" tabindex="-1"><a class="header-anchor" href="#xtreamresponse"><span>XtreamResponse</span></a></h2><div class="hint-container tip"><p class="hint-container-title">提示</p><p><code>XtreamResponse</code> 借鉴于 <code>org.springframework.http.server.reactive.ServerHttpResponse</code></p></div><p>有 <code>Request</code> 就有 <code>response</code>。<code>XtreamResponse</code> 是和 <code>XtreamRequest</code> 相对应的。也就是说 <code>XtreamResponse</code> 不会单独存在，一定是和某个 <code>XtreamRequest</code> 对应的。</p><p>通过 <code>XtreamResponse</code> 可以下发数据给客户端。</p><div class="language-java line-numbers-mode" data-highlighter="shiki" data-ext="java" data-title="java" style="--shiki-light:#383A42;--shiki-dark:#abb2bf;--shiki-light-bg:#FAFAFA;--shiki-dark-bg:#282c34;"><pre class="shiki shiki-themes one-light one-dark-pro vp-code"><code><span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">public</span><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;"> interface</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> XtreamResponse</span><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;"> extends</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> XtreamOutbound</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;"> {</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">    // 其实这几个方法都是在 XtreamOutbound 中定义的</span></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    ByteBufAllocator</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> bufferFactory</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    XtreamRequest</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">.</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">Type</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> type</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    NettyOutbound</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> outbound</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    InetSocketAddress</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> remoteAddress</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    Mono</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&lt;</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">Void</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&gt;</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> writeWith</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">(</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">Publisher</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&lt;</span><span style="--shiki-light:#C18401;--shiki-dark:#C678DD;">?</span><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;"> extends</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> ByteBuf</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&gt; </span><span style="--shiki-light:#383A42;--shiki-light-font-style:inherit;--shiki-dark:#E06C75;--shiki-dark-font-style:italic;">body</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">);</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    Mono</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&lt;</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">Void</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&gt;</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> writeAndFlushWith</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">(</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">Publisher</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&lt;</span><span style="--shiki-light:#C18401;--shiki-dark:#C678DD;">?</span><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;"> extends</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> Publisher</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&lt;</span><span style="--shiki-light:#C18401;--shiki-dark:#C678DD;">?</span><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;"> extends</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> ByteBuf</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&gt;&gt; </span><span style="--shiki-light:#383A42;--shiki-light-font-style:inherit;--shiki-dark:#E06C75;--shiki-dark-font-style:italic;">publisher</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">);</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">}</span></span></code></pre><div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0;"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><h2 id="xtreamsession" tabindex="-1"><a class="header-anchor" href="#xtreamsession"><span>XtreamSession</span></a></h2><div class="hint-container tip"><p class="hint-container-title">提示</p><p><code>XtreamSession</code> 借鉴于 <code>org.springframework.web.server.WebSession</code></p></div><p><code>XtreamSession</code> 是对当前客户端的会话的封装，主要有如下作用：</p><ul><li>和 <code>XtreamResponse</code> 一样，<code>XtreamSession</code> 也有下发数据的能力(<code>XtreamOutbound</code>)</li><li><code>session.invalidate()</code> 方法可以关闭会话(断开连接)，进而触发对 <code>XtreamSessionEventListener</code> 的回调</li><li>通过 <code>session.attributes()</code> 方法可以保存和当前回话相关的元数据</li></ul><div class="language-java line-numbers-mode" data-highlighter="shiki" data-ext="java" data-title="java" style="--shiki-light:#383A42;--shiki-dark:#abb2bf;--shiki-light-bg:#FAFAFA;--shiki-dark-bg:#282c34;"><pre class="shiki shiki-themes one-light one-dark-pro vp-code"><code><span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">public</span><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;"> interface</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> XtreamSession</span><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;"> extends</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> XtreamOutbound</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;"> {</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    String</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> id</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    Map</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&lt;</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">String</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">,</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> Object</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&gt;</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> attributes</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    Instant</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> creationTime</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">    /**</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">     * </span><span style="--shiki-light:#A626A4;--shiki-light-font-style:italic;--shiki-dark:#C678DD;--shiki-dark-font-style:italic;">@return</span><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;"> 上次通信时间</span></span>
<span class="line"><span style="--shiki-light:#A0A1A7;--shiki-light-font-style:italic;--shiki-dark:#7F848E;--shiki-dark-font-style:italic;">     */</span></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    Instant</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> lastCommunicateTime</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    XtreamSession</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> lastCommunicateTime</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">(</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">Instant</span><span style="--shiki-light:#383A42;--shiki-light-font-style:inherit;--shiki-dark:#E06C75;--shiki-dark-font-style:italic;"> current</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">);</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">    void</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> invalidate</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">(</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">XtreamSessionEventListener</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">.</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">SessionCloseReason</span><span style="--shiki-light:#383A42;--shiki-light-font-style:inherit;--shiki-dark:#E06C75;--shiki-dark-font-style:italic;"> reason</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">);</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">}</span></span></code></pre><div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0;"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><h2 id="xtreamexchange" tabindex="-1"><a class="header-anchor" href="#xtreamexchange"><span>XtreamExchange</span></a></h2><div class="hint-container tip"><p class="hint-container-title">提示</p><p><code>XtreamExchange</code> 借鉴于 <code>org.springframework.web.server.ServerWebExchange</code></p></div><ul><li><code>XtreamExchange</code> 聚合了<code>XtreamRequest</code> 、<code>XtreamResponse</code> 和 <code>XtreamSession</code>。</li><li><code>XtreamExchange</code> 是不可变对象( <strong>Immutable</strong>)。一旦创建便不可修改。</li><li>通过 <code>exchange.mutate()</code> 方法可以从当前实例构建出一个新实例的 <code>Builder</code>。</li></ul><div class="language-java line-numbers-mode" data-highlighter="shiki" data-ext="java" data-title="java" style="--shiki-light:#383A42;--shiki-dark:#abb2bf;--shiki-light-bg:#FAFAFA;--shiki-dark-bg:#282c34;"><pre class="shiki shiki-themes one-light one-dark-pro vp-code"><code><span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">public</span><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;"> interface</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> XtreamExchange</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;"> {</span></span>
<span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">    default</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> ByteBufAllocator</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> bufferFactory</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">()</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;"> {</span></span>
<span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">        return</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> response</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">().</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;">bufferFactory</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">    }</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    XtreamRequest</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> request</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    XtreamResponse</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> response</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">    Mono</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&lt;</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">XtreamSession</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&gt;</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> session</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">    default</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> XtreamExchangeBuilder</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> mutate</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">()</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;"> {</span></span>
<span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">        return</span><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;"> new</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> DefaultXtreamExchangeBuilder</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">(</span><span style="--shiki-light:#E45649;--shiki-dark:#E5C07B;">this</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">);</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">    }</span></span>
<span class="line"></span>
<span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">    default</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> Map</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&lt;</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;">String</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">,</span><span style="--shiki-light:#C18401;--shiki-dark:#E5C07B;"> Object</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">&gt;</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> attributes</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">()</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;"> {</span></span>
<span class="line"><span style="--shiki-light:#A626A4;--shiki-dark:#C678DD;">        return</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;"> request</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">().</span><span style="--shiki-light:#4078F2;--shiki-dark:#61AFEF;">attributes</span><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">();</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">    }</span></span>
<span class="line"><span style="--shiki-light:#383A42;--shiki-dark:#ABB2BF;">}</span></span></code></pre><div class="line-numbers" aria-hidden="true" style="counter-reset:line-number 0;"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div>`,20)]))}const r=i(l,[["render",t],["__file","terminology.html.vue"]]),d=JSON.parse('{"path":"/guide/server/quick-start/terminology.html","title":"基本术语","lang":"zh-CN","frontmatter":{"icon":"file-lines","article":false,"description":"基本术语 XtreamRequest 提示 XtreamRequest 借鉴于 org.springframework.http.server.reactive.ServerHttpRequest 客户端报文中的字节流最终会解析到 XtreamRequest 里。 同时会将本次请求的其他元数据封装到 XtreamRequest 中， 例如：对方 IP ...","head":[["meta",{"property":"og:url","content":"https://mister-hope.github.io/xtream-codec/guide/server/quick-start/terminology.html"}],["meta",{"property":"og:site_name","content":"xtream-codec"}],["meta",{"property":"og:title","content":"基本术语"}],["meta",{"property":"og:description","content":"基本术语 XtreamRequest 提示 XtreamRequest 借鉴于 org.springframework.http.server.reactive.ServerHttpRequest 客户端报文中的字节流最终会解析到 XtreamRequest 里。 同时会将本次请求的其他元数据封装到 XtreamRequest 中， 例如：对方 IP ..."}],["meta",{"property":"og:type","content":"website"}],["meta",{"property":"og:locale","content":"zh-CN"}],["meta",{"property":"og:updated_time","content":"2024-12-17T15:13:15.000Z"}],["meta",{"property":"article:modified_time","content":"2024-12-17T15:13:15.000Z"}],["script",{"type":"application/ld+json"},"{\\"@context\\":\\"https://schema.org\\",\\"@type\\":\\"WebPage\\",\\"name\\":\\"基本术语\\",\\"description\\":\\"基本术语 XtreamRequest 提示 XtreamRequest 借鉴于 org.springframework.http.server.reactive.ServerHttpRequest 客户端报文中的字节流最终会解析到 XtreamRequest 里。 同时会将本次请求的其他元数据封装到 XtreamRequest 中， 例如：对方 IP ...\\"}"]]},"headers":[{"level":2,"title":"XtreamRequest","slug":"xtreamrequest","link":"#xtreamrequest","children":[]},{"level":2,"title":"XtreamResponse","slug":"xtreamresponse","link":"#xtreamresponse","children":[]},{"level":2,"title":"XtreamSession","slug":"xtreamsession","link":"#xtreamsession","children":[]},{"level":2,"title":"XtreamExchange","slug":"xtreamexchange","link":"#xtreamexchange","children":[]}],"git":{"createdTime":1734100399000,"updatedTime":1734448395000,"contributors":[{"name":"hylexus","username":"hylexus","email":"hylexus@163.com","commits":3}]},"readingTime":{"minutes":1.68,"words":504},"filePathRelative":"guide/server/quick-start/terminology.md","localizedDate":"2024年12月13日","excerpt":"\\n<h2>XtreamRequest</h2>\\n<div class=\\"hint-container tip\\">\\n<p class=\\"hint-container-title\\">提示</p>\\n<p><code>XtreamRequest</code> 借鉴于 <code>org.springframework.http.server.reactive.ServerHttpRequest</code></p>\\n</div>\\n<ul>\\n<li>客户端报文中的字节流最终会解析到 <code>XtreamRequest</code> 里。</li>\\n<li>同时会将本次请求的其他元数据封装到 <code>XtreamRequest</code> 中， 例如：对方 IP 地址、协议类型等。</li>\\n<li><code>XtreamRequest</code> 是不可变对象( <strong>Immutable</strong>)。一旦创建便不可修改。</li>\\n<li>通过 <code>request.mutate()</code> 方法可以从当前实例构建出一个新实例的 <code>Builder</code>。</li>\\n</ul>","autoDesc":true}');export{r as comp,d as data};