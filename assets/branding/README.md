# 项目标志

本套标志于 2026-10-24 由 Codex（AI）在本仓库中绘制。
图形参考仓库旧标志的双向箭头和圆弧交错风格，矢量路径重新编写。
配色为蓝色 `#4D7CFE` 和青色 `#22D3C5`：蓝色撇在下、青色捺在上，两条通道由同一曲线镜像得到，线宽均为 10，箭头尺寸一致。
四端箭头表示双向编解码，中部留白区分上下层；深蓝底用于站点图标，透明底单色版用遮罩保留交叉处的间隙。
未直接复制旧 SVG 的路径数据，也未引入外部字库或位图素材；字标字形由手工编写的几何路径构成。
新绘制的标志资源随本项目按根目录的 [Apache-2.0 许可证](../../LICENSE)提供。商标使用适用该许可证第 6 节。
旧标志的历史授权未核实。含旧标志的截图来源见[静态资源记录](../STATIC-ASSETS.md#dashboard-与-studio-网页截图)。

来源署名及宣传说明见[免责声明与第三方商业化](../../docs/src/disclaimer.md)。

- `mark.svg`：由 [`generate.py`](../../build-script/branding/generate.py) 根据几何参数生成的主 SVG。
- `monochrome.svg`：脚本从主 SVG 派生的透明底单色版本，以 `currentColor` 控制颜色。
- `wordmark.svg`：字形路径为手工编写；脚本将标记区域内的图形与 `mark.svg` 同步。
- 同名 PNG、`favicon.ico`、`preview.png`：导出资源及预览。

在仓库根目录运行以下命令，仅使用 Python 3 标准库即可生成主 SVG。

```bash
python3 build-script/branding/generate.py
```

完整导出需要 Python 3、Node.js 22 和固定版本的 resvg。resvg 安装在临时目录。

```bash
branding_tools=$(mktemp -d)
mise exec -- npm install --prefix "$branding_tools" --no-audit --no-fund @resvg/resvg-js@2.6.2
mise exec -- node build-script/branding/export.mjs "$branding_tools/node_modules/@resvg/resvg-js"
```

修改图形时编辑 `generate.py`，然后运行 `export.mjs`。导出脚本先调用 Python 生成 `mark.svg`，再同步单色版、字标图形、两个 Dashboard 的内联 SVG，以及文档、Studio、两个 Dashboard、四个 quick-start/debug UI 和 IDE 的 SVG 副本，并生成 PNG、站点图标、maskable 图标及多尺寸 ICO。
文档的 `logo.png` 为 512×512；横向 PNG 为 1248×224；ICO 含 16/32/48/64/128/256 像素图像。
两个 Dashboard 的 `LogoIcon` 保持现有组件参数，脚本只替换图形内容；无需另行手动复制路径。
