import { execFileSync } from 'node:child_process';
import { createRequire } from 'node:module';
import { copyFile, mkdir, readFile, writeFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const require = createRequire(import.meta.url);
const { Resvg } = require(process.argv[2] || '@resvg/resvg-js');
const root = resolve(dirname(fileURLToPath(import.meta.url)), '../..');
const branding = resolve(root, 'assets/branding');
execFileSync('python3', [resolve(root, 'build-script/branding/generate.py')], { stdio: 'inherit' });
const mark = await readFile(resolve(branding, 'mark.svg'), 'utf8');
const wordmarkSource = await readFile(resolve(branding, 'wordmark.svg'), 'utf8');
const output = async (path, content) => {
  const target = resolve(root, path);
  await mkdir(dirname(target), { recursive: true });
  await writeFile(target, content);
};
const png = (svg, width) => new Resvg(svg, {
  fitTo: { mode: 'width', value: width },
  font: { loadSystemFonts: false },
}).render().asPng();
const inner = (svg) => svg.replace(/^<svg[^>]*>\s*/, '').replace(/\s*<\/svg>\s*$/, '');
const indent = (value, spaces) => value.replace(/^/gm, ' '.repeat(spaces));
const markBody = inner(mark).replace(/^ {2}/gm, '');
const groups = [...markBody.matchAll(/<g(?: [^>]*)?>[\s\S]*?<\/g>/g)].map(([group]) => group);
const outline = groups[1]?.match(/  <path [^>]*stroke="#0B1224"[^>]*\/>\n/)?.[0];
if (groups.length !== 2 || !outline) throw new Error('mark.svg 须保留两条通道及上层通道的留白描边');

// 单色版通过遮罩挖空交叉处，确保透明底也能保留上下层间隙。
const monochrome = `<svg xmlns="http://www.w3.org/2000/svg" width="96" height="96" viewBox="0 0 96 96" fill="none" role="img" aria-label="xtream-codec">
  <defs>
    <mask id="xtream-codec-overpass" maskUnits="userSpaceOnUse" x="0" y="0" width="96" height="96">
      <rect width="96" height="96" fill="white"/>
      ${groups[1].split('\n')[0]}
${indent(outline.trim().replace('#0B1224', 'black'), 8)}
      </g>
    </mask>
  </defs>
${indent(groups[0].replace('<g>', '<g mask="url(#xtream-codec-overpass)">'), 2)}
${indent(groups[1].replace(outline, ''), 2)}
</svg>\n`.replaceAll('#4D7CFE', 'currentColor').replaceAll('#22D3C5', 'currentColor');
await output('assets/branding/monochrome.svg', monochrome);

// 字形保留手工路径，仅替换字标中标记出的图形区域。
const wordmarkRegion = /  <!-- 标志开始 -->[\s\S]*?  <!-- 标志结束 -->/;
if (!wordmarkRegion.test(wordmarkSource)) throw new Error('wordmark.svg 缺少标志同步区域');
const wordmark = wordmarkSource.replace(wordmarkRegion, `  <!-- 标志开始 -->
  <g transform="translate(8 8)">
${indent(markBody, 4)}
  </g>
  <!-- 标志结束 -->`);
await output('assets/branding/wordmark.svg', wordmark);

// Dashboard 保留现有组件参数，图形内容直接从主 SVG 同步。
const jsxBody = indent(markBody.replaceAll('stroke-width=', 'strokeWidth=').replaceAll('/>', ' />'), 4);
for (const name of ['jt-808-server-dashboard-ui', 'jt-1078-server-dashboard-ui']) {
  const path = `ext/jt/${name}/src/components/icons.tsx`;
  const source = await readFile(resolve(root, path), 'utf8');
  const region = /(export const LogoIcon:[\s\S]*?<svg[\s\S]*?\n  >\n)[\s\S]*?(  <\/svg>\n\);)/;
  if (!region.test(source)) throw new Error(`${path} 缺少 LogoIcon 图形同步区域`);
  await output(path, source.replace(region, (_, before, after) => `${before}${jsxBody}\n${after}`));
}

const svgTargets = [
  '.idea/icon.svg',
  'docs/src/.vuepress/public/logo.svg',
  'xtream-codec-studio-ui/public/logo.svg',
  'ext/jt/jt-808-server-dashboard-ui/public/logo.svg',
  'ext/jt/jt-1078-server-dashboard-ui/public/logo.svg',
  'quick-start/jt/jt-808-server-quick-start-with-storage-ui/public/logo.svg',
  'quick-start/jt/jt-1078-server-quick-start-ui/public/logo.svg',
  'debug/jt/jt-808-server-spring-boot-starter-debug-ui/public/logo.svg',
  'debug/jt/jt-1078-server-spring-boot-starter-debug-ui/public/logo.svg',
];
for (const path of svgTargets) await output(path, mark);
await output('assets/branding/mark.png', png(mark, 512));
await output('assets/branding/monochrome.png', png(monochrome, 512));
await output('assets/branding/wordmark.png', png(wordmark, 1248));
await copyFile(resolve(branding, 'mark.png'), resolve(root, 'docs/src/.vuepress/public/logo.png'));

const iconRoot = 'docs/src/.vuepress/public/assets/icon';
for (const [name, size] of [
  ['chrome-192.png', 192],
  ['chrome-512.png', 512],
  ['android-chrome-512x512.png', 512],
  ['ms-icon-144.png', 144],
  ['apple-icon-152.png', 152],
]) await output(`${iconRoot}/${name}`, png(mark, size));
// Maskable 图标使用不透明背景，核心图形位于中心 80% 安全圆内。
const maskable = mark.replace('rx="24"', 'rx="0"');
for (const size of [192, 512]) {
  await output(`${iconRoot}/chrome-mask-${size}.png`, png(maskable, size));
}

// ICO 直接嵌入 PNG；256 像素在目录字段中按格式要求记作 0。
function ico(sizes) {
  const entries = sizes.map((size) => ({ size, data: png(mark, size) }));
  const header = Buffer.alloc(6 + entries.length * 16);
  header.writeUInt16LE(1, 2);
  header.writeUInt16LE(entries.length, 4);
  let offset = header.length;
  entries.forEach(({ size, data }, index) => {
    const entry = 6 + index * 16;
    header[entry] = header[entry + 1] = size === 256 ? 0 : size;
    header.writeUInt16LE(1, entry + 4);
    header.writeUInt16LE(32, entry + 6);
    header.writeUInt32LE(data.length, entry + 8);
    header.writeUInt32LE(offset, entry + 12);
    offset += data.length;
  });
  return Buffer.concat([header, ...entries.map(({ data }) => data)]);
}
const favicon = ico([16, 32, 48, 64, 128, 256]);
for (const path of [
  'assets/branding/favicon.ico',
  'docs/src/.vuepress/public/favicon.ico',
  `${iconRoot}/favicon.ico`,
  'ext/jt/jt-1078-server-dashboard-ui/favicon.ico',
]) await output(path, favicon);
await output(`${iconRoot}/favicon-16x16.ico`, ico([16]));

const preview = `<svg xmlns="http://www.w3.org/2000/svg" width="1000" height="440" viewBox="0 0 1000 440" fill="none">
  <rect width="1000" height="440" rx="24" fill="#F0F4FA"/>
  <rect x="24" y="24" width="952" height="200" rx="20" fill="white"/>
  <g transform="translate(110 54) scale(1.25)">${inner(wordmark)}</g>
  <rect x="24" y="248" width="580" height="168" rx="20" fill="#0B1224"/>
  <g transform="translate(70 284)">${inner(mark)}</g>
  <g transform="translate(210 296) scale(.75)">${inner(monochrome).replaceAll('currentColor', '#FFFFFF').replaceAll('xtream-codec-overpass', 'preview-light-overpass')}</g>
  <g transform="translate(336 308) scale(.5)">${inner(mark)}</g>
  <g transform="translate(446 316) scale(.333333)">${inner(mark)}</g>
  <g transform="translate(526 324) scale(.166667)">${inner(mark)}</g>
  <rect x="628" y="248" width="348" height="168" rx="20" fill="white"/>
  <g transform="translate(668 284)">${inner(monochrome).replaceAll('currentColor', '#0B1224').replaceAll('xtream-codec-overpass', 'preview-dark-overpass')}</g>
  <g transform="translate(820 284)">${inner(mark)}</g>
</svg>\n`;
await output('assets/branding/preview.png', png(preview, 1500));
console.log('已从 mark.svg 同步彩色/单色/字标、Dashboard 内联标志、PNG、ICO 和预览。');
