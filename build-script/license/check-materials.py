#!/usr/bin/env python3
# Copyright 2026 the xtream-codec authors. SPDX-License-Identifier: Apache-2.0
"""核对许可分发材料，默认重建八个前端，也可核对指定产物或生成图片预览。"""

import argparse
import base64
import hashlib
from html import escape
import io
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import struct
from urllib.parse import unquote
import xml.etree.ElementTree as ET
import zlib
from zipfile import BadZipFile, ZipFile


ROOT = Path(__file__).resolve().parents[2]
PROJECT = {"LICENSE": "LICENSE.txt", "NOTICE": "NOTICE.txt",
           "THIRD-PARTY-SOURCE-NOTICES.txt": "THIRD-PARTY-SOURCE-NOTICES.txt"}
NOTICE = "THIRD-PARTY-FRONTEND-NOTICES.txt"
FRONTEND_ENTRY = re.compile(r"(?:BOOT-INF/classes/)?(?:ui|static(?:/[^/]+)*)/index\.html")
ADPCM = "ext/jt/jt-1078-server-spring-boot-starter-reactive/src/main/java/" \
        "io/github/hylexus/xtream/codec/ext/jt1078/codec/audio/impl/adpcm/AdpcmImaJt1078AudioCodec.java"


def require(condition, message):
    if not condition:
        raise ValueError(message)


def digest(raw):
    return hashlib.sha256(raw).hexdigest()


def exact(jar, name, expected):
    require(name in jar.namelist(), f"缺少 {name}")
    require(jar.read(name) == expected, f"内容不符 {name}")


def check_frontend(raw):
    text = raw.decode("utf-8")
    for name in ("LICENSE", "licenses/frontend/NOTICE.txt", "licenses/frontend/open_source_licenses.txt"):
        require((ROOT / name).read_text(encoding="utf-8").rstrip() in text, f"前端汇总缺少完整原文：{name}")


def check_checksums(jar, prefix):
    raw = jar.read(prefix + "ARTIFACTS.txt").decode("utf-8")
    coordinate = re.match(r"de\.sciss:jump3r:([^\s/]+) \(unmodified\)\n", raw)
    require(coordinate, "缺少 jump3r 实际版本坐标")
    version = coordinate[1]
    binary = f"jump3r-{version}.jar"
    source = f"jump3r-{version}-sources.jar"
    entries = re.findall(r"^(.+?) SHA-256 ([0-9a-f]{64})$", raw, re.M)
    require(len(entries) == 2 and {name for name, _ in entries} == {binary, source},
            f"jump3r 校验清单与声明版本不符：{version}")
    checksums = dict(entries)
    names = set(jar.namelist())
    require(prefix + source in names, f"缺少 jump3r 对应版本源码：{source}")
    require(digest(jar.read(prefix + source)) == checksums[source], f"源码哈希不符：{source}")
    libraries = {name for name in names if re.fullmatch(r"BOOT-INF/lib/jump3r-[^/]+\.jar", name)}
    if libraries:
        nested = "BOOT-INF/lib/" + binary
        require(libraries == {nested}, f"jump3r 源码与实际库版本不符：{version}")
        require(digest(jar.read(nested)) == checksums[binary], f"嵌套二进制哈希不符：{binary}")


APPLICATION_NOTICE = "META-INF/open_source_licenses.txt"
APPLICATION_TEXT = ROOT / "licenses/jvm/open_source_licenses.txt"
EPL_MODULES = {
    "logback-core": "ch.qos.logback",
    "logback-classic": "ch.qos.logback",
    "jakarta.annotation-api": "jakarta.annotation",
    "aspectjweaver": "org.aspectj",
}


def source_url(coordinate):
    group, artifact, version = coordinate.split(":")
    return f"https://repo.maven.apache.org/maven2/{group.replace('.', '/')}/{artifact}/{version}/{artifact}-{version}-sources.jar"


def check_application(jar, boot):
    names = set(jar.namelist())
    require(APPLICATION_NOTICE in names, "缺少应用第三方许可汇总")
    raw = jar.read(APPLICATION_NOTICE)
    original = APPLICATION_TEXT.read_bytes()
    require(raw.startswith(original), "应用第三方许可汇总原文不完整")
    sources = raw[len(original):].decode("utf-8")
    entries = re.findall(r"^(\S+:\S+:\S+)\n源码地址 / Source URL: (\S+)$", sources, re.M)
    for coordinate, url in entries:
        require(url == source_url(coordinate), f"EPL 源码地址与坐标不符：{coordinate}")
    coordinates = {coordinate for coordinate, _ in entries}

    if boot:
        for artifact, group in EPL_MODULES.items():
            for nested in sorted(names):
                match = re.fullmatch(rf"BOOT-INF/lib/{re.escape(artifact)}-(.+)\.jar", nested)
                if not match:
                    continue
                coordinate = f"{group}:{artifact}:{match[1]}"
                require(coordinate in coordinates, f"缺少对应版本的 EPL 源码地址：{coordinate}")
                if artifact == "jakarta.annotation-api":
                    try:
                        with ZipFile(io.BytesIO(jar.read(nested))) as dependency:
                            for name in ("META-INF/LICENSE.md", "META-INF/NOTICE.md"):
                                require(name in dependency.namelist(), f"依赖缺少上游原声明：{nested}/{name}")
                    except BadZipFile as error:
                        raise ValueError(f"依赖归档不可读：{nested}") from error
    else:
        # Studio 的 Shadow 分发包含 Logback；版本与源码地址由实际运行时依赖生成。
        for artifact in ("logback-core", "logback-classic"):
            if any(n.startswith("ch/qos/logback/") for n in names):
                require(any(c.startswith(f"ch.qos.logback:{artifact}:") for c in coordinates),
                        f"缺少 Shadow EPL 源码地址：{artifact}")


def check_jar(path):
    with ZipFile(path) as jar:
        names = set(jar.namelist())
        require(len(names) == len(jar.namelist()), "ZIP 包含重复路径")
        aggregate = "ch/qos/logback/core/Appender.class" in names
        for source, target in PROJECT.items():
            entry = "META-INF/" + target
            require(entry in names, f"缺少 {entry}")
            expected = (ROOT / source).read_bytes()
            require(expected in jar.read(entry) if aggregate else expected == jar.read(entry),
                    f"项目原文不完整：{entry}")
        exact(jar, "META-INF/licenses/source/open_source_licenses.txt",
              (ROOT / "licenses/source/open_source_licenses.txt").read_bytes())
        for entry in sorted(names):
            if FRONTEND_ENTRY.fullmatch(entry):
                notice = entry.removesuffix("index.html") + NOTICE
                require(notice in names, f"缺少 {notice}")
            if entry == NOTICE or entry.endswith("/" + NOTICE):
                check_frontend(jar.read(entry))

        boot = any(n.startswith("BOOT-INF/lib/") for n in names)
        if boot or aggregate or APPLICATION_NOTICE in names:
            check_application(jar, boot)

        adpcm_class = "io/github/hylexus/xtream/codec/ext/jt1078/codec/audio/impl/adpcm/AdpcmImaJt1078AudioCodec.class"
        source_name = "META-INF/third-party-sources/adpcm/" + adpcm_class.removesuffix(".class") + ".java"
        if adpcm_class in names or "BOOT-INF/classes/" + adpcm_class in names or source_name in names:
            exact(jar, source_name, (ROOT / ADPCM).read_bytes())
            exact(jar, "META-INF/licenses/MPL-2.0.txt", (ROOT / "licenses/MPL-2.0.txt").read_bytes())

        jump_prefix = "META-INF/third-party-sources/jump3r/"
        if any(n.startswith("BOOT-INF/lib/jump3r-") for n in names) or "de/sciss/jump3r/lowlevel/LameEncoder.class" in names:
            require(jump_prefix + "ARTIFACTS.txt" in names, "缺少 jump3r 匹配源码材料")
        if jump_prefix + "ARTIFACTS.txt" in names:
            exact(jar, "META-INF/licenses/LGPL-2.1.txt", (ROOT / "licenses/LGPL-2.1.txt").read_bytes())
            exact(jar, jump_prefix + "NOTICE.txt", (ROOT / "licenses/jump3r/NOTICE.txt").read_bytes())
            exact(jar, jump_prefix + "jump3r.py", (ROOT / "build-script/license/jump3r.py").read_bytes())
            check_checksums(jar, jump_prefix)
        for entry in names:
            if entry.endswith(".jar") and entry.startswith((
                "BOOT-INF/lib/jt-1078-server-spring-boot-starter-reactive-",
                "BOOT-INF/lib/jt-808-server-dashboard-spring-boot-starter-reactive-",
            )):
                try:
                    check_jar(io.BytesIO(jar.read(entry)))
                except (ValueError, BadZipFile) as error:
                    raise ValueError(f"{entry}：{error}") from error


LOGS = ROOT / "build/reports/frontend-license-checks"
FRONTENDS = (
    "xtream-codec-studio-ui",
    "ext/jt/jt-808-server-dashboard-ui",
    "ext/jt/jt-1078-server-dashboard-ui",
    "quick-start/jt/jt-808-server-quick-start-with-storage-ui",
    "quick-start/jt/jt-1078-server-quick-start-ui",
    "debug/jt/jt-808-server-spring-boot-starter-debug-ui",
    "debug/jt/jt-1078-server-spring-boot-starter-debug-ui",
    "docs",
)


def check_docs_fonts(output):
    public = ROOT / "docs/src/.vuepress/public"
    summary = (output / NOTICE).read_text(encoding="utf-8")
    for name in ("LICENSE.txt", "README.txt"):
        source = public / "webfonts" / name
        require((output / "webfonts" / source.name).read_bytes() == source.read_bytes(),
                f"docs 字体材料副本不符：{source.name}")
        require(source.read_text(encoding="utf-8").rstrip() in summary,
                f"docs 汇总缺少字体材料全文：{source.name}")
    css = public / "assets/fa-icon.css"
    require((output / "assets/fa-icon.css").read_bytes() == css.read_bytes(),
            "docs 字体 CSS 副本不符")
    fonts = set(re.findall(r"\.\./webfonts/([^)'\"\s]+\.woff2)", css.read_text(encoding="utf-8")))
    require(fonts, "docs CSS 未引用 WOFF2 字体")
    for name in sorted(fonts):
        source = public / "webfonts" / name
        raw = source.read_bytes()
        require((output / "webfonts" / source.name).read_bytes() == raw,
                f"docs 字体副本不符：{source.name}")


def check_frontend_output(directory, output):
    raw = (output / NOTICE).read_bytes()
    check_frontend(raw)
    text = raw.decode("utf-8")
    require(text.startswith("第三方前端许可与归属声明\nThird-party frontend license and attribution notices\n"),
            "前端汇总缺少双语标题")
    template = directory / "LICENSE"
    if directory.name in ("jt-808-server-dashboard-ui", "jt-1078-server-dashboard-ui"):
        require(template.read_text(encoding="utf-8").rstrip() in text,
                f"前端汇总缺少模板许可：{template}")
    if directory == ROOT / "docs":
        check_docs_fonts(output)
        require(not any(output.rglob("xtream-codec.drawio")), "文档产物包含可编辑源图：xtream-codec.drawio")


def run(command, directory, log):
    print(f"运行 {' '.join(map(str, command))}（{directory.relative_to(ROOT) or '.'}）", flush=True)
    with log.open("w", encoding="utf-8") as stream:
        result = subprocess.run(command, cwd=directory, stdout=stream, stderr=subprocess.STDOUT,
                                env={**os.environ, "CI": "true"})
    if result.returncode:
        tail = "\n".join(log.read_text(encoding="utf-8", errors="replace").splitlines()[-30:])
        raise ValueError(f"命令失败（退出码 {result.returncode}），日志：{log}\n{tail}")


def check_frontends(skip_install=False, check_only=False):
    LOGS.mkdir(parents=True, exist_ok=True)
    if not check_only:
        node, pnpm = shutil.which("node"), shutil.which("pnpm")
        require(node and pnpm, "请先安装 Node.js 22 和 pnpm 10，并将其加入 PATH")
        run([node, "--test", "build-script/license/frontend-notices.test.mjs"], ROOT, LOGS / "collector.log")
    else:
        print("仅核对已有产物；本次不会验证依赖安装或重新构建。", flush=True)
    for relative in FRONTENDS:
        directory = ROOT / relative
        output = directory / ("src/.vuepress/dist" if relative == "docs" else "dist")
        if not check_only:
            if not skip_install:
                run([pnpm, "install", "--frozen-lockfile"], directory, LOGS / f"{directory.name}-install.log")
            command = "docs:build" if relative == "docs" else "build"
            run([pnpm, "run", command], directory, LOGS / f"{directory.name}-build.log")
        check_frontend_output(directory, output)
        print(f"PASS {relative}：许可汇总" + ("、字体副本及双语材料" if relative == "docs" else ""), flush=True)
    print(f"PASS 全部 {len(FRONTENDS)} 个前端；构建日志：{LOGS}", flush=True)


def png_texts(raw):
    offset = 8
    result = {}
    while offset + 12 <= len(raw):
        size = struct.unpack(">I", raw[offset:offset + 4])[0]
        kind = raw[offset + 4:offset + 8]
        body = raw[offset + 8:offset + 8 + size]
        offset += size + 12
        if kind in (b"tEXt", b"zTXt", b"iTXt"):
            key, value = body.split(b"\0", 1)
            if kind == b"zTXt":
                value = zlib.decompress(value[1:])
            elif kind == b"iTXt":
                compressed, _, rest = value[0], value[1], value[2:]
                _, _, value = rest.split(b"\0", 2)
                if compressed:
                    value = zlib.decompress(value)
            result[key.decode("latin1")] = value.decode("utf-8")
        if kind == b"IEND":
            break
    return result


def pages(xml):
    result = []
    for diagram in ET.fromstring(xml).findall("diagram"):
        graph = diagram.find("mxGraphModel")
        if graph is None and diagram.text and diagram.text.strip():
            graph = ET.fromstring(unquote(zlib.decompress(base64.b64decode(diagram.text), -15).decode()))
        cells = set()
        if graph is not None:
            cells = {cell.get("id") for cell in graph.iter("mxCell") if cell.get("id") not in ("0", "1")}
        result.append((diagram.get("name"), cells))
    return result


def preview_assets():
    output = ROOT / "build/reports/static-assets"
    output.mkdir(parents=True, exist_ok=True)
    editable = ROOT / "assets/xtream-codec.drawio"
    reference = pages(editable.read_text(encoding="utf-8"))
    source_records = (ROOT / "assets/STATIC-ASSETS.md").read_text(encoding="utf-8")
    source_table = source_records.partition("\n## 已有来源记录的文档图片\n")[2].partition("\n## ")[0]
    recorded_paths = {path for line in source_table.splitlines() if line.startswith("|")
                      for path in re.findall(r"`([^`]+\.png)`", line)}
    documents = [(path.relative_to(ROOT).as_posix(), path.read_text(encoding="utf-8"))
                 for path in [ROOT / "README.md", *sorted((ROOT / "docs/src").rglob("*.md"))]]
    records, pending_cards, recorded_cards = [], [], []
    image_root = ROOT / "docs/src/.vuepress/public/img"
    for number, path in enumerate(sorted(image_root.rglob("*.png")), 1):
        raw = path.read_bytes()
        metadata = png_texts(raw)
        relative = path.relative_to(ROOT).as_posix()
        already_recorded = path.relative_to(image_root).as_posix() in recorded_paths
        record = {"number": number, "path": relative, "sha256": hashlib.sha256(raw).hexdigest(),
                  "metadataKeys": sorted(metadata), "status": "已有来源记录" if already_recorded else "待核对",
                  "referencedBy": [name for name, text in documents if path.name in text]}
        extracted_link = ""
        if "mxfile" in metadata:
            xml = unquote(metadata["mxfile"])
            extracted = f"{number:02d}-{path.stem}.drawio"
            extracted_pages = pages(xml)
            (output / extracted).write_text(xml, encoding="utf-8")
            record["extracted"] = extracted
            record["pages"] = [{"name": name, "sharedCellIds": {
                ref_name: len(cells & ref_cells) for ref_name, ref_cells in reference if cells & ref_cells
            }} for name, cells in extracted_pages]
            extracted_link = f'<a href="{escape(extracted)}">查看提取的可编辑数据</a>'
        records.append(record)
        image = base64.b64encode(raw).decode()
        cards = recorded_cards if already_recorded else pending_cards
        cards.append(f'<article><h3>{number:02d} · {escape(path.name)}</h3>'
                     f'<img loading="lazy" src="data:image/png;base64,{image}" alt="{escape(path.name)}">'
                     f'<p>{escape(relative)}</p><code>{record["sha256"]}</code>'
                     f'<p>引用页线索：{escape(", ".join(record["referencedBy"]) or "未找到")}</p>'
                     f'<p>元数据：{escape(", ".join(sorted(metadata)) or "无文本元数据")}</p>'
                     f'{extracted_link}</article>')
    (output / "inventory.json").write_text(json.dumps(records, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    html = '<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>文档图片来源核对</title>' \
           '<style>body{font:16px system-ui;margin:32px;background:#f4f7fa;color:#17293c}' \
           '.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(min(100%,340px),1fr));gap:24px}' \
           'article{background:white;padding:20px;border-radius:12px;overflow-wrap:anywhere}' \
           'img{width:100%;height:260px;object-fit:contain;background:#eef2f6}h3{font-size:18px}' \
           'details{margin:24px 0}summary{cursor:pointer;font-weight:600;margin-bottom:16px}' \
           'code{font-size:11px}a{color:#086bb5}</style><h1>文档图片来源核对</h1>' \
           '<p>编号对应 <a href="inventory.json">inventory.json</a>，制作和授权说明见 <a href="../../../assets/STATIC-ASSETS.md">静态资源记录</a>。</p>' \
           f'<main><details><summary>已有来源记录（{len(recorded_cards)} 张）</summary><div class="cards">' + \
           "\n".join(recorded_cards) + \
           f'</div></details><h2>待核对（{len(pending_cards)} 张）</h2><div class="cards">' + \
           "\n".join(pending_cards) + '</div></main></html>'
    (output / "index.html").write_text(html, encoding="utf-8")
    count = sum("extracted" in item for item in records)
    print(f"已生成 {len(records)} 张图片预览，提取 {count} 份可编辑数据：{output / 'index.html'}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar", nargs="+", type=Path, default=[], help="仅核对指定 Java 归档")
    parser.add_argument("--frontend", nargs="+", type=Path, default=[], help="仅核对指定前端汇总文件")
    parser.add_argument("--docs-dist", type=Path, help="核对指定文档站点目录的许可汇总、字体和发布内容")
    parser.add_argument("--jar-frontend", action="append", nargs=3, default=[], metavar=("JAR", "ENTRY", "DIST_NOTICE"),
                        help="逐字节比对 JAR 内的前端许可汇总和对应 dist 文件，可重复指定")
    parser.add_argument("--skip-install", action="store_true", help="复用已安装依赖，仍重建八个前端")
    parser.add_argument("--check-only", action="store_true", help="仅核对八前端现有产物，不安装或重建，不能证明产物是最新的")
    parser.add_argument("--preview-assets", action="store_true", help="仅生成图片来源核对预览，不构建前端或修改原图")
    args = parser.parse_args()
    specified = args.jar or args.frontend or args.jar_frontend or args.docs_dist
    if specified and (args.skip_install or args.check_only):
        parser.error("指定产物时不能与八前端选项 --skip-install/--check-only 混用")
    if args.preview_assets and (specified or args.skip_install or args.check_only):
        parser.error("--preview-assets 不能与归档或前端选项混用")
    current = "图片预览" if args.preview_assets else "八前端"
    try:
        if args.preview_assets:
            preview_assets()
            return
        if not specified:
            check_frontends(args.skip_install, args.check_only)
            return
        for current in args.jar:
            check_jar(current)
            print(f"PASS {current}")
        for current in args.frontend:
            check_frontend(current.read_bytes())
            print(f"PASS {current}")
        if args.docs_dist is not None:
            current = args.docs_dist
            check_frontend_output(ROOT / "docs", current)
            print(f"PASS {current}：许可汇总及站点资源")
        for current, entry, dist in args.jar_frontend:
            expected = Path(dist).read_bytes()
            check_frontend(expected)
            with ZipFile(current) as jar:
                require(jar.namelist().count(entry) <= 1, f"重复路径 {entry}")
                exact(jar, entry, expected)
            print(f"PASS {current}!/{entry} == {dist}")
    except (OSError, ValueError, KeyError, BadZipFile) as error:
        raise SystemExit(f"FAIL {current}: {error}") from error


if __name__ == "__main__":
    main()
