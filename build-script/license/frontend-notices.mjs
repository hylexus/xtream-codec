// Copyright 2026 the xtream-codec authors. SPDX-License-Identifier: Apache-2.0
import { existsSync, readFileSync, readdirSync, realpathSync, statSync } from 'node:fs';
import { basename, dirname, isAbsolute, join, resolve, sep } from 'node:path';
import { createRequire } from 'node:module';
import { fileURLToPath } from 'node:url';

const repository = fileURLToPath(new URL('../../', import.meta.url));
const supplementalFile = join(repository, 'licenses/frontend/open_source_licenses.txt');
const supplementalText = readFileSync(supplementalFile, 'utf8');
const supplementedPackages = new Set([...supplementalText.matchAll(/^适用包 \/ Packages: (.+)$/gm)]
  .flatMap(([, names]) => names.split(/,\s*/)));
const supplementalLegalTexts = new Set(supplementalText
  .split(/^(?:上游原文 \/ Upstream text|链接所指的标准条款 \/ Standard terms linked above)\n-{60}\n/m)
  .slice(1).map((text) => text.split(/^={60}$/m)[0].trimEnd()));
const legalName = /^(licen[cs]e|copying|notice|copyright)(?:$|[._-])/i;

function legalFiles(directory) {
  return readdirSync(directory).filter((name) => legalName.test(name)).sort().flatMap((name) => {
    const path = join(directory, name);
    return statSync(path).isDirectory()
      ? readdirSync(path).sort().filter((child) => statSync(join(path, child)).isFile()).map((child) => join(path, child))
      : [path];
  });
}

function packageDirectory(id) {
  const path = id.split('?')[0];
  if (path.includes('\0') || !isAbsolute(path) || !path.includes(`${sep}node_modules${sep}`) || !existsSync(path)) {
    return undefined;
  }
  let current = dirname(realpathSync(path));
  while (current !== dirname(current)) {
    if (existsSync(join(current, 'package.json'))) {
      const metadata = JSON.parse(readFileSync(join(current, 'package.json'), 'utf8'));
      // 某些包在 dist/esm 中只有 type 字段，应继续查找包含名称和版本的包根目录。
      if (metadata.name && metadata.version) return current;
    }
    current = dirname(current);
  }
  throw new Error(`无法定位前端模块所属包：${id}`);
}

export function frontendNotices({ additionalLicenseFiles = [] } = {}) {
  const generatedCodePackages = new Set();
  let frontendRoot;
  return {
    name: 'xtream-frontend-license-notices',
    apply: 'build',
    configResolved(config) {
      let root = config.root;
      while (!existsSync(join(root, 'package.json')) && root !== dirname(root)) root = dirname(root);
      frontendRoot = root;
      const metadata = JSON.parse(readFileSync(join(root, 'package.json'), 'utf8'));
      const dependencies = { ...metadata.dependencies, ...metadata.devDependencies };
      const require = createRequire(join(root, 'package.json'));
      generatedCodePackages.clear();
      // 这些工具会注入虚拟辅助模块、预置样式，不能只依赖普通模块 ID 追踪归属。
      for (const name of ['vite', '@vitejs/plugin-vue', '@vitejs/plugin-react', 'tailwindcss', 'unocss']) {
        if (dependencies[name]) generatedCodePackages.add(packageDirectory(require.resolve(name)));
      }
      if (dependencies['@vuepress/bundler-vite']) {
        const bundler = require.resolve('@vuepress/bundler-vite');
        const bundlerRequire = createRequire(bundler);
        for (const name of ['vite', '@vitejs/plugin-vue']) {
          generatedCodePackages.add(packageDirectory(bundlerRequire.resolve(name)));
        }
      }
    },
    generateBundle(_options, bundle) {
      const modules = new Set();
      for (const chunk of Object.values(bundle)) {
        if (chunk.type === 'chunk') {
          for (const [id, details] of Object.entries(chunk.modules)) {
            if (details.renderedLength > 0) modules.add(id);
          }
        }
      }
      // CSS 的 JavaScript 占位模块可能为空，仍须为实际提取的样式保留许可。
      for (const id of this.getModuleIds()) {
        if (/\.(css|less|s[ac]ss|styl)(?:\?|$)/i.test(id)) modules.add(id);
      }
      const directories = new Set([...generatedCodePackages, ...[...modules].map(packageDirectory)].filter(Boolean));
      const missing = [];
      const packages = [...directories].map((directory) => {
        const metadata = JSON.parse(readFileSync(join(directory, 'package.json'), 'utf8'));
        const files = legalFiles(directory);
        const hasLicense = files.some((path) => /licen[cs]e|copying/i.test(basename(path)));
        const supplement = !hasLicense && supplementedPackages.has(metadata.name) && metadata.license === 'MIT';
        if (!hasLicense && !supplement) {
          missing.push(`${metadata.name}@${metadata.version}`);
        }
        return {
          key: `${metadata.name}@${metadata.version}`,
          text: `${metadata.name}@${metadata.version}\n声明的许可 / Declared license: ${JSON.stringify(metadata.license ?? metadata.licenses ?? 'unspecified')}\n` +
            (supplement ? '补充许可 / Supplemental licenses: licenses/frontend/open_source_licenses.txt\n' : ''),
          files: files.map((path) => ({
            name: path.startsWith(directory + sep) ? path.slice(directory.length + 1) : path.slice(repository.length),
            text: readFileSync(path, 'utf8').trimEnd(),
          })),
        };
      }).sort((left, right) => left.key.localeCompare(right.key, 'en'));
      if (missing.length) throw new Error(`缺少许可全文：${missing.sort().join(', ')}`);
      const unique = [...new Map(packages.map((item) => [JSON.stringify(item), item])).values()];
      // 仅按完整正文去重，保留每个包及原许可文件到正文的对应关系。
      const legalTexts = new Map();
      const packageNotices = unique.map((item) => item.text + item.files.map((file) => {
        if (supplementalLegalTexts.has(file.text)) {
          return `--- ${file.name} -> 补充许可原文 / Supplemental legal text: licenses/frontend/open_source_licenses.txt ---\n`;
        }
        if (!legalTexts.has(file.text)) legalTexts.set(file.text, legalTexts.size + 1);
        return `--- ${file.name} -> 许可原文 / Legal text #${legalTexts.get(file.text)} ---\n`;
      }).join(''));
      const projectFiles = [join(repository, 'LICENSE'), join(repository, 'licenses/frontend/NOTICE.txt'), supplementalFile];
      // 手工放入 public 的资源不经过模块图，由对应前端显式提供适用的许可全文。
      projectFiles.push(...additionalLicenseFiles.map((path) => resolve(frontendRoot, path)));
      const localLicense = resolve(frontendRoot, 'LICENSE');
      if (existsSync(localLicense) && localLicense !== join(repository, 'LICENSE')) projectFiles.push(localLicense);
      const source = '第三方前端许可与归属声明\nThird-party frontend license and attribution notices\n\n' +
        '本文件由生产构建自动生成，请通过共享生成器维护。\n' +
        'This file is generated automatically during production builds. Maintain it through the shared generator.\n\n' +
        '本文件包含项目许可、适用的模板和静态资源声明，以及本次构建收集的 npm 许可材料。\n' +
        'This file includes the project license, applicable template and static asset notices, and npm license materials collected for this build.\n\n' +
        '收集范围包括向生产产物提供实际输出的 JavaScript 和已加载样式的包。\n' +
        'Includes packages contributing rendered JavaScript and loaded styles to the production bundle.\n\n' +
        '还包括本次构建使用的打包器、框架辅助代码及样式生成器。\n' +
        'Also includes bundler/framework helpers and stylesheet generators used by this build.\n\n' +
        '许可声明属于包元数据，所附完整原始许可文本的条款保持不变。\n' +
        'License declarations are metadata; the full original texts below retain their terms.\n\n' +
        '完全相同的 npm 许可正文只列一次，各包的原文件名指向对应正文编号。\n' +
        'Identical npm legal texts appear once; each package lists its original filenames and the corresponding text references.\n\n' +
        '第三方代码、模板及资源保留各自的许可证；项目 Apache-2.0 许可不替代这些条款。\n' +
        "Third-party code, templates and assets retain their respective licenses; the project's Apache-2.0 license does not replace those terms.\n\n" +
        '分发静态站点、前端产物或包含这些资源的 JAR、镜像时，请保留本文件。\n' +
        'Retain this file when distributing the static site, frontend build, or JARs and container images containing these resources.\n\n' +
        projectFiles.map((path) => `--- ${path === localLicense ? '前端模板许可 / Frontend template LICENSE' : path.slice(repository.length)} ---\n${readFileSync(path, 'utf8').trimEnd()}\n`).join('\n') +
        '\n' + packageNotices.map((text) => `============================================================\n${text}`).join('\n') +
        '\n' + [...legalTexts].map(([text, number]) => `============================================================\n许可原文 / Legal text #${number}\n\n${text}\n`).join('\n');
      this.emitFile({ type: 'asset', fileName: 'THIRD-PARTY-FRONTEND-NOTICES.txt', source });
    },
  };
}
