#!/usr/bin/env python3
# Copyright 2026 the xtream-codec authors. SPDX-License-Identifier: Apache-2.0
"""重建 jump3r 或在新的 Spring Boot 应用副本中替换该库；保留原文件。"""

import argparse
from pathlib import Path
import re
import subprocess
import tempfile
import zipfile


def rebuild(args):
    with tempfile.TemporaryDirectory(prefix="jump3r-build-") as temporary:
        root = Path(temporary)
        sources = root / "src"
        classes = root / "classes"
        classes.mkdir()
        files = []
        with zipfile.ZipFile(args.sources) as archive:
            for entry in archive.infolist():
                if not entry.filename.endswith(".java"):
                    continue
                target = (sources / entry.filename).resolve()
                if not target.is_relative_to(sources.resolve()):
                    raise ValueError("源码路径超出归档根目录")
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(archive.read(entry))
                files.append(target)
        if not files:
            raise ValueError("归档不包含 Java 源码")
        arguments = root / "sources.args"
        arguments.write_text("\n".join('"' + str(p).replace('\\', '\\\\').replace('"', '\\"') + '"'
                                       for p in sorted(files)), encoding="utf-8")
        subprocess.run(["javac", "--release", "8", "-encoding", "UTF-8", "-d", str(classes),
                        "@" + str(arguments)], check=True)
        subprocess.run(["jar", "--create", "--file", str(args.output.resolve()), "-C", str(classes), "."],
                       check=True)


def replace(args):
    with args.application.open("rb") as stream:
        if stream.read(4) != b"PK\x03\x04":
            raise ValueError("不支持带前置启动脚本的应用")
    with zipfile.ZipFile(args.library) as library:
        required = {"de/sciss/jump3r/lowlevel/LameEncoder.class", "de/sciss/jump3r/mp3/Lame.class"}
        if not required.issubset(library.namelist()) or library.testzip():
            raise ValueError("替换库不是完整的 jump3r JAR")
    with zipfile.ZipFile(args.application) as original:
        entries = original.infolist()
        names = [entry.filename for entry in entries]
        if len(names) != len(set(names)) or original.testzip():
            raise ValueError("应用存在重复条目或损坏数据")
        matches = [name for name in names if re.fullmatch(r"BOOT-INF/lib/jump3r-[^/]+\.jar", name)]
        if len(matches) != 1:
            raise ValueError("应用必须包含唯一的 BOOT-INF/lib/jump3r-*.jar")
        for entry in entries:
            if re.fullmatch(r"META-INF/[^/]+\.(SF|RSA|DSA|EC)", entry.filename, re.IGNORECASE):
                # Spring Boot 的空 BOOT.SF 是结构标记，并非签名。
                if entry.filename != "META-INF/BOOT.SF" or entry.file_size != 0:
                    raise ValueError("签名应用需另行提供重建及签名流程")
        with args.output.open("xb") as target:
            try:
                with zipfile.ZipFile(target, "w") as result:
                    result.comment = original.comment
                    for entry in entries:
                        data = original.read(entry)
                        if entry.filename == matches[0]:
                            data = args.library.read_bytes()
                            entry.compress_type = zipfile.ZIP_STORED
                        result.writestr(entry, data)
            except BaseException:
                target.close()
                args.output.unlink(missing_ok=True)
                raise


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    build_parser = commands.add_parser("rebuild", help="用 Python 3.9+ 和 JDK 11+ 从完整源码重建")
    build_parser.add_argument("sources", type=Path)
    build_parser.add_argument("--output", required=True, type=Path)
    replace_parser = commands.add_parser("replace", help="在新的 Boot 应用副本中替换 jump3r")
    replace_parser.add_argument("application", type=Path)
    replace_parser.add_argument("library", type=Path)
    replace_parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    if args.output.exists():
        parser.error("输出必须是新文件")
    try:
        if args.command == "rebuild":
            rebuild(args)
        else:
            replace(args)
    except (OSError, ValueError, zipfile.BadZipFile, subprocess.CalledProcessError) as error:
        parser.exit(1, f"FAIL {error}\n")


if __name__ == "__main__":
    main()
