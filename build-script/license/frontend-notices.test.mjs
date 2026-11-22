// Copyright 2026 the xtream-codec authors. SPDX-License-Identifier: Apache-2.0
import assert from 'node:assert/strict';
import { mkdtempSync, mkdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import test from 'node:test';
import { frontendNotices } from './frontend-notices.mjs';

test('前端许可覆盖输出模块与样式、完整保留复合许可和 NOTICE，缺失材料时失败', () => {
  const root = mkdtempSync(join(tmpdir(), 'xtream-frontend-license-'));
  const license = 'MIT License\nCopyright 2026 Fixture Authors\nOriginal complete license text.\n';
  const supplement = readFileSync(new URL('../../licenses/frontend/open_source_licenses.txt', import.meta.url), 'utf8').trimEnd();
  try {
    function fixture(name, version, files) {
      const directory = join(root, 'node_modules', name);
      mkdirSync(directory, { recursive: true });
      writeFileSync(join(directory, 'package.json'), JSON.stringify({ name, version, license: 'MIT' }));
      for (const [file, text] of Object.entries(files)) writeFileSync(join(directory, file), text);
      return directory;
    }
    const runtime = fixture('runtime-library', '1.0.0', {
      'index.js': 'export const answer = 42;', LICENSE: license + '\nAdditional upstream license terms.\n', NOTICE: 'Original copyright notice.\n',
    });
    const styles = fixture('styles-library', '2.0.0', { 'style.css': '.a { color: red; }', LICENSE: license });
    const empty = fixture('unused-library', '3.0.0', { 'index.js': '' });
    fixture('tailwindcss', '4.0.0', { 'index.js': 'export {};', LICENSE: license });
    const different = fixture('different-copyright-library', '1.0.0', {
      'index.js': 'export const other = true;', LICENSE: 'MIT License\nCopyright 2026 Another Author\nDifferent original terms.',
    });
    writeFileSync(join(root, 'package.json'), JSON.stringify({ devDependencies: { tailwindcss: '4.0.0' } }));
    function collect(modules, loaded = Object.keys(modules), options) {
      let emitted;
      const plugin = frontendNotices(options);
      plugin.configResolved({ root });
      plugin.generateBundle.call({
        getModuleIds: () => loaded.values(), emitFile: (asset) => { emitted = asset; return 'asset'; },
      }, {}, { 'app.js': { type: 'chunk', modules } });
      return emitted.source;
    }
    const report = collect({
      [join(runtime, 'index.js')]: { renderedLength: 24 },
      [join(empty, 'index.js')]: { renderedLength: 0 },
      [join(different, 'index.js')]: { renderedLength: 20 },
    }, [join(runtime, 'index.js'), join(styles, 'style.css'), join(empty, 'index.js')]);
    assert.ok(report.includes(license.trimEnd()));
    assert.ok(report.includes(supplement));
    assert.equal(report.split(supplement).length - 1, 1);
    assert.ok(report.includes('Additional upstream license terms.'));
    assert.ok(report.includes('Original copyright notice.'));
    assert.ok(report.includes('styles-library@2.0.0'));
    assert.ok(report.includes('tailwindcss@4.0.0'));
    const projectNotice = readFileSync(new URL('../../licenses/frontend/NOTICE.txt', import.meta.url), 'utf8').trimEnd();
    assert.ok(readFileSync(new URL('../../NOTICE', import.meta.url), 'utf8').startsWith(`${projectNotice}\n\n`));
    assert.ok(report.includes(projectNotice));
    for (const name of ['MIT-SmallChi-JT808.txt', 'MIT-SmallChi-JT1078.txt', 'MIT-EyerH264Decoder.txt', 'MIT-maz-g711.txt']) {
      assert.ok(!report.includes(name));
    }
    assert.ok(!report.includes('THIRD-PARTY-SOURCE-NOTICES.txt'));
    const styleRef = report.match(/styles-library@2\.0\.0\n[^]*?--- LICENSE -> 许可原文 \/ Legal text #(\d+) ---/)[1];
    const toolRef = report.match(/tailwindcss@4\.0\.0\n[^]*?--- LICENSE -> 许可原文 \/ Legal text #(\d+) ---/)[1];
    const differentRef = report.match(/different-copyright-library@1\.0\.0\n[^]*?--- LICENSE -> 许可原文 \/ Legal text #(\d+) ---/)[1];
    assert.equal(styleRef, toolRef);
    assert.notEqual(styleRef, differentRef);
    assert.equal(report.split(`\n许可原文 / Legal text #${styleRef}\n`).length - 1, 1);
    assert.ok(report.includes('Copyright 2026 Another Author\nDifferent original terms.'));
    assert.ok(!report.includes('unused-library@3.0.0'));
    writeFileSync(join(root, 'LICENSE'), 'Original local template license.');
    assert.ok(collect({}, []).includes('前端模板许可 / Frontend template LICENSE ---\nOriginal local template license.'));
    writeFileSync(join(root, 'FONT-LICENSE.txt'), 'Static font license and attribution.');
    assert.ok(collect({}, [], { additionalLicenseFiles: ['FONT-LICENSE.txt'] }).includes('Static font license and attribution.'));
    assert.throws(() => collect({}, [], { additionalLicenseFiles: ['MISSING-FONT-LICENSE.txt'] }), /MISSING-FONT-LICENSE/);
    const missing = fixture('missing-library', '1.0.0', { 'index.js': 'export const missing = true;' });
    assert.throws(() => collect({ [join(missing, 'index.js')]: { renderedLength: 20 } }), /missing-library@1\.0\.0/);
    const upgraded = fixture('@uiw/react-codemirror', '999.0.0', { 'index.js': 'export const upgraded = true;' });
    const upgradedModules = { [join(upgraded, 'index.js')]: { renderedLength: 20 } };
    assert.ok(collect(upgradedModules).includes('@uiw/react-codemirror@999.0.0'));
    writeFileSync(join(upgraded, 'package.json'), JSON.stringify({ name: '@uiw/react-codemirror', version: '999.0.0', license: 'GPL-3.0-only' }));
    assert.throws(() => collect(upgradedModules), /缺少许可全文/);
    writeFileSync(join(upgraded, 'LICENSE'), 'New upstream license and copyright.');
    assert.ok(collect(upgradedModules).includes('New upstream license and copyright.'));
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});
