import{_ as d}from"./plugin-vue_export-helper-DlAUqK2U.js";import{c as o,a as l,o as r}from"./app-DuUg9O0T.js";const e="/xtream-codec/img/core/annotation/jt-808-data-types.png",i={};function s(c,t){return r(),o("div",null,t[0]||(t[0]=[l('<h1 id="内置注解" tabindex="-1"><a class="header-anchor" href="#内置注解"><span>内置注解</span></a></h1><h2 id="请先读我" tabindex="-1"><a class="header-anchor" href="#请先读我"><span>请先读我</span></a></h2><p>内置注解都是从 <code>@XtreamField</code> 注解扩展而来的。目前有两种风格的注解：</p><ul><li><code>Rust</code> 数据类型命名风格</li><li><code>JT/T 808</code> 协议数据类型命名风格</li></ul><div class="hint-container tip"><p class="hint-container-title">提示</p><ul><li>强烈推荐优先使用 <code>Rust</code> 风格内置注解 <ul><li>数据类型比较全面 <ul><li><code>JT/T 808</code> 风格的注解只支持无符号，因为 <code>JT/T 808</code> 官方文档里都是无符号数</li></ul></li><li>支持小端序</li></ul></li><li>不推荐<strong>直接</strong>使用底层的 <code>@XtreamField</code> 注解 <ul><li>虽然功能最完整，但是使用略显繁琐</li><li>但是如果你要自定义注解，那非常推荐使用 <code>@XtreamField</code> 来进行扩展</li></ul></li></ul></div><h2 id="rust-风格内置注解" tabindex="-1"><a class="header-anchor" href="#rust-风格内置注解"><span>Rust 风格内置注解</span></a></h2><h3 id="整数类型" tabindex="-1"><a class="header-anchor" href="#整数类型"><span>整数类型</span></a></h3><ul><li><code>@Preset.RustStyle.i8</code><ul><li>有符号整数</li><li><strong>8bit</strong>、<strong>1byte</strong></li><li>大端序</li><li><strong>JavaType</strong>: <code>byte/Byte</code></li></ul></li><li><code>@Preset.RustStyle.u8</code><ul><li>无符号整数</li><li><strong>8bit</strong>、<strong>1byte</strong></li><li>大端序</li><li><strong>JavaType</strong>: <code>short/Short</code>, <strong>byte</strong> 有溢出的风险</li></ul></li><li><code>@Preset.RustStyle.i16</code><ul><li>有符号整数</li><li><strong>16bit</strong>、<strong>2byte</strong></li><li>大端序</li><li><strong>JavaType</strong>: <code>short/Short</code></li></ul></li><li><code>@Preset.RustStyle.u16</code><ul><li>无符号整数</li><li><strong>16bit</strong>、<strong>2byte</strong></li><li>大端序</li><li><strong>JavaType</strong>: <code>int/Integer</code>, <strong>short</strong> 有溢出的风险</li></ul></li><li><code>@Preset.RustStyle.i16_le</code><ul><li>有符号整数</li><li><strong>16bit</strong>、<strong>2byte</strong></li><li>小端序</li><li><strong>JavaType</strong>: <code>short/Short</code></li></ul></li><li><code>@Preset.RustStyle.u16_le</code><ul><li>无符号整数</li><li><strong>16bit</strong>、<strong>2byte</strong></li><li>小端序</li><li><strong>JavaType</strong>: <code>int/Integer</code>, <strong>short</strong> 有溢出的风险</li></ul></li><li><code>@Preset.RustStyle.i32</code><ul><li>有符号整数</li><li><strong>32bit</strong>、<strong>4byte</strong></li><li>大端序</li><li><strong>JavaType</strong>: <code>int/Integer</code></li></ul></li><li><code>@Preset.RustStyle.u32</code><ul><li>无符号整数</li><li><strong>32bit</strong>、<strong>4byte</strong></li><li>大端序</li><li><strong>JavaType</strong>: <code>long/Long</code>, <strong>int</strong> 有溢出的风险</li></ul></li><li><code>@Preset.RustStyle.i32_le</code><ul><li>有符号整数</li><li><strong>32bit</strong>、<strong>4byte</strong></li><li>小端序</li><li><strong>JavaType</strong>: <code>int/Integer</code></li></ul></li><li><code>@Preset.RustStyle.u32_le</code><ul><li>无符号整数</li><li><strong>32bit</strong>、<strong>4byte</strong></li><li>小端序</li><li><strong>JavaType</strong>: <code>long/Long</code>, <strong>int</strong> 有溢出的风险</li></ul></li></ul><h3 id="字符串类型" tabindex="-1"><a class="header-anchor" href="#字符串类型"><span>字符串类型</span></a></h3><ul><li><code>@Preset.RustStyle.str</code><ul><li>字符串</li><li>编码: 默认 <code>utf-8</code></li><li><strong>JavaType</strong>: <code>java.lang.String</code></li></ul></li></ul><h3 id="嵌套类型" tabindex="-1"><a class="header-anchor" href="#嵌套类型"><span>嵌套类型</span></a></h3><ul><li><code>@Preset.RustStyle.struct</code><ul><li>结构体、内嵌对象</li><li><strong>JavaType</strong>: 自定义实体类</li></ul></li></ul><h3 id="列表类型" tabindex="-1"><a class="header-anchor" href="#列表类型"><span>列表类型</span></a></h3><ul><li><code>@Preset.RustStyle.list</code><ul><li><strong>JavaType</strong>: <code>java.util.List</code></li></ul></li></ul><h3 id="动态类型" tabindex="-1"><a class="header-anchor" href="#动态类型"><span>动态类型</span></a></h3><ul><li><code>@Preset.RustStyle.dyn</code><ul><li><strong>JavaType</strong>: <code>Object</code></li><li>运行时才能确定具体类型</li></ul></li></ul><h2 id="jt-t-808-风格内置注解" tabindex="-1"><a class="header-anchor" href="#jt-t-808-风格内置注解"><span>JT/T 808 风格内置注解</span></a></h2><h3 id="jt-t-808-的相关说明" tabindex="-1"><a class="header-anchor" href="#jt-t-808-的相关说明"><span>JT/T 808 的相关说明</span></a></h3><div class="hint-container tip"><p class="hint-container-title">提示</p><p>在 <code>JT/T 808</code> 协议中:</p><ul><li>对于数字类型而言，都是无符号数 <ul><li>然而 <strong>Java</strong> 本身没有无符号数的概念</li><li>所以在映射到 <strong>Java</strong> 数据类型时，往往需要将数据类型的范围扩大一倍，以避免溢出的风险 <ul><li>以 <code>BYTE</code> 类型为例: <code>JT/T 808</code> 协议中指的是 <strong>1</strong> 字节的 <em>无</em> 符号整数</li><li>虽然 <strong>Java</strong> 的 <code>byte/Byte</code> 也是 <strong>1</strong> 字节</li><li>但是 <strong>Java</strong> 的 <code>byte/Byte</code> 是 <em>有</em> 符号数</li><li>所以不得不将 <code>JT/T 808</code> 的 <code>BYTE</code> 映射到 <strong>Java</strong> 的 <code>short/Short</code> 以避免溢出的风险</li></ul></li></ul></li><li>对于字符串类型而言，<code>JT/T 808</code> 协议都是 &quot;GBK&quot; 编码</li></ul><p>所以，<code>JT/T 808</code> 风格的注解，可以看做是 <code>Rust</code> 风格注解的子集，两者的对应关系参考 <a href="#%E5%86%85%E7%BD%AE%E7%B1%BB%E5%9E%8B%E5%AF%B9%E6%AF%94">内置类型对比</a></p></div><details class="hint-container details"><summary>点击查看 JT/T 808 官方文档中的数据类型声明</summary><figure><img src="'+e+'" alt="" tabindex="0" loading="lazy"><figcaption></figcaption></figure></details><h3 id="整数类型-1" tabindex="-1"><a class="header-anchor" href="#整数类型-1"><span>整数类型</span></a></h3><ul><li><code>@Preset.JtStyle.Byte</code><ul><li>无符号整数</li><li><strong>8bit</strong>、<strong>1byte</strong></li><li>大端序</li><li><strong>JavaType</strong>: <code>short/Short</code>, <strong>byte</strong> 有溢出的风险</li></ul></li><li><code>@Preset.JtStyle.Word</code><ul><li>无符号整数</li><li><strong>16bit</strong>、<strong>2byte</strong></li><li>大端序</li><li><strong>JavaType</strong>: <code>int/Integer</code>, <strong>short</strong> 有溢出的风险</li></ul></li><li><code>@Preset.JtStyle.Dword</code><ul><li>无符号整数</li><li><strong>32bit</strong>、<strong>4byte</strong></li><li>大端序</li><li><strong>JavaType</strong>: <code>long/Long</code></li></ul></li></ul><h3 id="字符串类型-1" tabindex="-1"><a class="header-anchor" href="#字符串类型-1"><span>字符串类型</span></a></h3><ul><li><code>@Preset.RustStyle.Str</code><ul><li>字符串</li><li>编码: 默认 <code>GBK</code></li><li><strong>JavaType</strong>: <code>java.lang.String</code></li></ul></li><li><code>@Preset.RustStyle.Bcd</code><ul><li>字符串</li><li>编码固定为: <code>BCD_8421</code></li><li><strong>JavaType</strong>: <code>java.lang.String</code></li></ul></li></ul><h3 id="嵌套类型-1" tabindex="-1"><a class="header-anchor" href="#嵌套类型-1"><span>嵌套类型</span></a></h3><ul><li><code>@Preset.JtStyle.Object</code><ul><li>结构体、内嵌对象</li><li><strong>JavaType</strong>: 自定义实体类</li></ul></li></ul><h3 id="列表类型-1" tabindex="-1"><a class="header-anchor" href="#列表类型-1"><span>列表类型</span></a></h3><ul><li><code>@Preset.JtStyle.List</code><ul><li><strong>JavaType</strong>: <code>java.util.List</code></li></ul></li></ul><h3 id="动态类型-1" tabindex="-1"><a class="header-anchor" href="#动态类型-1"><span>动态类型</span></a></h3><ul><li><code>@Preset.JtStyle.RuntimeType</code><ul><li><strong>JavaType</strong>: <code>Object</code></li><li>运行时才能确定具体类型</li></ul></li></ul><h3 id="其他" tabindex="-1"><a class="header-anchor" href="#其他"><span>其他</span></a></h3><ul><li><code>@Preset.RustStyle.Bytes</code><ul><li><strong>N 字节</strong></li><li><strong>JavaType</strong>: <code>byte[]</code></li></ul></li><li><code>@Preset.RustStyle.BcdDateTime</code><ul><li><strong>N 字节</strong></li><li><strong>JavaType</strong>: <code>java.time.LocalDateTime/java.util.Date/String</code></li></ul></li></ul><h2 id="内置类型对比" tabindex="-1"><a class="header-anchor" href="#内置类型对比"><span>内置类型对比</span></a></h2><h3 id="rust风格-vs-jt-t-808风格" tabindex="-1"><a class="header-anchor" href="#rust风格-vs-jt-t-808风格"><span><strong><code>Rust风格</code></strong> <strong>VS <code>JT/T 808</code>风格</strong></span></a></h3><details class="hint-container details"><summary>点击查看 JT/T 808 官方文档中的数据类型声明</summary><figure><img src="'+e+'" alt="" tabindex="0" loading="lazy"><figcaption></figcaption></figure></details><p>下面表格是 <code>@Preset.RustStyle.xxx</code> 和 <code>@Preset.JtStyle</code> 的对应关系，当然你也可以自定义自己的注解:</p><table><thead><tr><th><strong>Rust</strong> 风格注解</th><th><strong>JT/T 808</strong> 风格注解</th><th>JavaType</th></tr></thead><tbody><tr><td><code>@Preset.RustStyle.i8</code></td><td>-</td><td><code>byte/Byte</code></td></tr><tr><td><code>@Preset.RustStyle.u8</code></td><td><code>@Preset.JtStyle.Byte</code></td><td><code>short/Short</code></td></tr><tr><td><code>@Preset.RustStyle.i16</code></td><td>-</td><td><code>short/Short</code></td></tr><tr><td><code>@Preset.RustStyle.i16_le</code></td><td>-</td><td><code>short/Short</code></td></tr><tr><td><code>@Preset.RustStyle.u16</code></td><td><code>@Preset.JtStyle.Word</code></td><td><code>int/Integer</code></td></tr><tr><td><code>@Preset.RustStyle.u16_le</code></td><td>-</td><td><code>int/Integer</code></td></tr><tr><td><code>@Preset.RustStyle.i32</code></td><td>-</td><td><code>int/Integer</code></td></tr><tr><td><code>@Preset.RustStyle.i32_le</code></td><td>-</td><td><code>int/Integer</code></td></tr><tr><td><code>@Preset.RustStyle.u32</code></td><td><code>@Preset.JtStyle.Dword</code></td><td><code>long/Long</code></td></tr><tr><td><code>@Preset.RustStyle.u32_le</code></td><td>-</td><td><code>long/Long</code></td></tr><tr><td><code>@Preset.RustStyle.str(charset=&quot;GBK&quot;)</code></td><td><code>@Preset.JtStyle.Str</code></td><td><code>String</code></td></tr><tr><td><code>@Preset.RustStyle.str(charset=&quot;bcd_8421&quot;)</code></td><td><code>@Preset.JtStyle.Bcd</code></td><td><code>String</code></td></tr><tr><td><code>@Preset.RustStyle.str(charset=&quot;UTF-8&quot;)</code></td><td><code>@Preset.JtStyle.Str(charset=&quot;UTF-8&quot;)</code></td><td><code>String</code></td></tr><tr><td><code>@Preset.RustStyle.str(charset=&quot;xxx&quot;)</code></td><td><code>@Preset.JtStyle.Str(charset=&quot;xxx&quot;)</code></td><td><code>String</code></td></tr><tr><td><code>@Preset.RustStyle.struct</code></td><td><code>@Preset.JtStyle.Object</code></td><td>自定义实体类</td></tr><tr><td><code>@Preset.RustStyle.list</code></td><td><code>@Preset.JtStyle.List</code></td><td><code>java.util.List</code></td></tr><tr><td><code>@Preset.RustStyle.dyn</code></td><td><code>@Preset.JtStyle.RuntimeType</code></td><td><code>Object</code></td></tr></tbody></table><h3 id="rust风格-vs-xtreamfield风格" tabindex="-1"><a class="header-anchor" href="#rust风格-vs-xtreamfield风格"><span><strong><code>Rust风格</code></strong> <strong>VS <code>@XtreamField</code>风格</strong></span></a></h3><table><thead><tr><th><strong>Rust</strong> 风格注解</th><th><strong>@XtreamField</strong> 风格注解</th><th>JavaType</th></tr></thead><tbody><tr><td><code>@Preset.RustStyle.i8</code></td><td><code>@XtreamField(length = 1)</code></td><td><code>byte/Byte</code></td></tr><tr><td><code>@Preset.RustStyle.u8</code></td><td><code>@XtreamField(length = 1)</code></td><td><code>short/Short</code></td></tr><tr><td><code>@Preset.RustStyle.i16</code></td><td><code>@XtreamField(length = 2)</code></td><td><code>short/Short</code></td></tr><tr><td><code>@Preset.RustStyle.i16_le</code></td><td><code>@XtreamField(length = 2, littleEndian = true)</code></td><td><code>short/Short</code></td></tr><tr><td><code>@Preset.RustStyle.u16</code></td><td><code>@XtreamField(length = 2)</code></td><td><code>int/Integer</code></td></tr><tr><td><code>@Preset.RustStyle.u16_le</code></td><td><code>@XtreamField(length = 2, littleEndian = true)</code></td><td><code>int/Integer</code></td></tr><tr><td><code>@Preset.RustStyle.i32</code></td><td><code>@XtreamField(length = 4)</code></td><td><code>int/Integer</code></td></tr><tr><td><code>@Preset.RustStyle.i32_le</code></td><td><code>@XtreamField(length = 4, littleEndian = true)</code></td><td><code>int/Integer</code></td></tr><tr><td><code>@Preset.RustStyle.u32</code></td><td><code>@XtreamField(length = 4)</code></td><td><code>long/Long</code></td></tr><tr><td><code>@Preset.RustStyle.u32_le</code></td><td><code>@XtreamField(length = 4, littleEndian = true)</code></td><td><code>long/Long</code></td></tr><tr><td><code>@Preset.RustStyle.str(charset=&quot;UTF-8&quot;)</code></td><td><code>@XtreamField(charset=&quot;UTF-8&quot;)</code></td><td><code>String</code></td></tr><tr><td><code>@Preset.RustStyle.str(charset=&quot;GBK&quot;)</code></td><td><code>@XtreamField(charset=&quot;GBK&quot;)</code></td><td><code>String</code></td></tr><tr><td><code>@Preset.RustStyle.str(charset=&quot;bcd_8421&quot;)</code></td><td><code>@XtreamField(charset=&quot;bcd_8421&quot;)</code></td><td><code>String</code></td></tr><tr><td><code>@Preset.RustStyle.str(charset=&quot;xxx&quot;)</code></td><td><code>@XtreamField(charset=&quot;xxx&quot;)</code></td><td><code>String</code></td></tr><tr><td><code>@Preset.RustStyle.struct</code></td><td><code>@XtreamField(dataType=struct)</code></td><td>自定义实体类</td></tr><tr><td><code>@Preset.RustStyle.list</code></td><td><code>@XtreamField(dataType=sequence)</code></td><td><code>java.util.List</code></td></tr><tr><td><code>@Preset.RustStyle.dyn</code></td><td><code>@XtreamField(dataType=dynamic)</code></td><td><code>Object</code></td></tr></tbody></table>',39)]))}const u=d(i,[["render",s],["__file","builtin-annotations.html.vue"]]),g=JSON.parse('{"path":"/guide/core/annotation-driven/builtin-annotations.html","title":"内置注解","lang":"zh-CN","frontmatter":{"date":"2024-03-10T00:00:00.000Z","icon":"at","tag":["内置","注解"],"description":"内置注解 请先读我 内置注解都是从 @XtreamField 注解扩展而来的。目前有两种风格的注解： Rust 数据类型命名风格 JT/T 808 协议数据类型命名风格 提示 强烈推荐优先使用 Rust 风格内置注解 数据类型比较全面 JT/T 808 风格的注解只支持无符号，因为 JT/T 808 官方文档里都是无符号数 支持小端序 不推荐直接使用底...","head":[["meta",{"property":"og:url","content":"https://mister-hope.github.io/xtream-codec/guide/core/annotation-driven/builtin-annotations.html"}],["meta",{"property":"og:site_name","content":"xtream-codec"}],["meta",{"property":"og:title","content":"内置注解"}],["meta",{"property":"og:description","content":"内置注解 请先读我 内置注解都是从 @XtreamField 注解扩展而来的。目前有两种风格的注解： Rust 数据类型命名风格 JT/T 808 协议数据类型命名风格 提示 强烈推荐优先使用 Rust 风格内置注解 数据类型比较全面 JT/T 808 风格的注解只支持无符号，因为 JT/T 808 官方文档里都是无符号数 支持小端序 不推荐直接使用底..."}],["meta",{"property":"og:type","content":"article"}],["meta",{"property":"og:image","content":"https://mister-hope.github.io/xtream-codec/img/core/annotation/jt-808-data-types.png"}],["meta",{"property":"og:locale","content":"zh-CN"}],["meta",{"property":"og:updated_time","content":"2024-12-14T13:41:50.000Z"}],["meta",{"property":"article:tag","content":"内置"}],["meta",{"property":"article:tag","content":"注解"}],["meta",{"property":"article:published_time","content":"2024-03-10T00:00:00.000Z"}],["meta",{"property":"article:modified_time","content":"2024-12-14T13:41:50.000Z"}],["script",{"type":"application/ld+json"},"{\\"@context\\":\\"https://schema.org\\",\\"@type\\":\\"Article\\",\\"headline\\":\\"内置注解\\",\\"image\\":[\\"https://mister-hope.github.io/xtream-codec/img/core/annotation/jt-808-data-types.png\\",\\"https://mister-hope.github.io/xtream-codec/img/core/annotation/jt-808-data-types.png\\"],\\"datePublished\\":\\"2024-03-10T00:00:00.000Z\\",\\"dateModified\\":\\"2024-12-14T13:41:50.000Z\\",\\"author\\":[{\\"@type\\":\\"Person\\",\\"name\\":\\"xtream-codec\\",\\"url\\":\\"https://github.com/hylexus\\"}]}"]]},"headers":[{"level":2,"title":"请先读我","slug":"请先读我","link":"#请先读我","children":[]},{"level":2,"title":"Rust 风格内置注解","slug":"rust-风格内置注解","link":"#rust-风格内置注解","children":[{"level":3,"title":"整数类型","slug":"整数类型","link":"#整数类型","children":[]},{"level":3,"title":"字符串类型","slug":"字符串类型","link":"#字符串类型","children":[]},{"level":3,"title":"嵌套类型","slug":"嵌套类型","link":"#嵌套类型","children":[]},{"level":3,"title":"列表类型","slug":"列表类型","link":"#列表类型","children":[]},{"level":3,"title":"动态类型","slug":"动态类型","link":"#动态类型","children":[]}]},{"level":2,"title":"JT/T 808 风格内置注解","slug":"jt-t-808-风格内置注解","link":"#jt-t-808-风格内置注解","children":[{"level":3,"title":"JT/T 808 的相关说明","slug":"jt-t-808-的相关说明","link":"#jt-t-808-的相关说明","children":[]},{"level":3,"title":"整数类型","slug":"整数类型-1","link":"#整数类型-1","children":[]},{"level":3,"title":"字符串类型","slug":"字符串类型-1","link":"#字符串类型-1","children":[]},{"level":3,"title":"嵌套类型","slug":"嵌套类型-1","link":"#嵌套类型-1","children":[]},{"level":3,"title":"列表类型","slug":"列表类型-1","link":"#列表类型-1","children":[]},{"level":3,"title":"动态类型","slug":"动态类型-1","link":"#动态类型-1","children":[]},{"level":3,"title":"其他","slug":"其他","link":"#其他","children":[]}]},{"level":2,"title":"内置类型对比","slug":"内置类型对比","link":"#内置类型对比","children":[{"level":3,"title":"Rust风格 VS JT/T 808风格","slug":"rust风格-vs-jt-t-808风格","link":"#rust风格-vs-jt-t-808风格","children":[]},{"level":3,"title":"Rust风格 VS @XtreamField风格","slug":"rust风格-vs-xtreamfield风格","link":"#rust风格-vs-xtreamfield风格","children":[]}]}],"git":{"createdTime":1710065417000,"updatedTime":1734183710000,"contributors":[{"name":"hylexus","username":"hylexus","email":"hylexus@163.com","commits":4}]},"readingTime":{"minutes":3.53,"words":1058},"filePathRelative":"guide/core/annotation-driven/builtin-annotations.md","localizedDate":"2024年3月10日","excerpt":"\\n<h2>请先读我</h2>\\n<p>内置注解都是从 <code>@XtreamField</code> 注解扩展而来的。目前有两种风格的注解：</p>\\n<ul>\\n<li><code>Rust</code> 数据类型命名风格</li>\\n<li><code>JT/T 808</code> 协议数据类型命名风格</li>\\n</ul>\\n<div class=\\"hint-container tip\\">\\n<p class=\\"hint-container-title\\">提示</p>\\n<ul>\\n<li>强烈推荐优先使用 <code>Rust</code> 风格内置注解\\n<ul>\\n<li>数据类型比较全面\\n<ul>\\n<li><code>JT/T 808</code> 风格的注解只支持无符号，因为 <code>JT/T 808</code> 官方文档里都是无符号数</li>\\n</ul>\\n</li>\\n<li>支持小端序</li>\\n</ul>\\n</li>\\n<li>不推荐<strong>直接</strong>使用底层的 <code>@XtreamField</code> 注解\\n<ul>\\n<li>虽然功能最完整，但是使用略显繁琐</li>\\n<li>但是如果你要自定义注解，那非常推荐使用 <code>@XtreamField</code> 来进行扩展</li>\\n</ul>\\n</li>\\n</ul>\\n</div>","autoDesc":true}');export{u as comp,g as data};