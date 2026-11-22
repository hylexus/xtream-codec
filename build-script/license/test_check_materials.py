# Copyright 2026 the xtream-codec authors. SPDX-License-Identifier: Apache-2.0
"""验证归档、前端字体与模板的材料完整性，以及统一入口的模式选择。"""

from contextlib import redirect_stderr, redirect_stdout
import base64
import hashlib
import importlib.util
import io
import json
from pathlib import Path
import sys
import tempfile
from types import SimpleNamespace
import unittest
from unittest.mock import patch
import warnings
from zipfile import ZipFile


spec = importlib.util.spec_from_file_location("materials", Path(__file__).with_name("check-materials.py"))
materials = importlib.util.module_from_spec(spec)
spec.loader.exec_module(materials)
spec = importlib.util.spec_from_file_location("jump3r", Path(__file__).with_name("jump3r.py"))
jump3r = importlib.util.module_from_spec(spec)
spec.loader.exec_module(jump3r)


def archive(files):
    output = io.BytesIO()
    with ZipFile(output, "w") as jar:
        for name, raw in files.items():
            jar.writestr(name, raw)
    output.seek(0)
    return output


def fixture(extra=None, missing=None):
    files = {"META-INF/" + target: (materials.ROOT / source).read_bytes()
             for source, target in materials.PROJECT.items()}
    files["META-INF/licenses/source/open_source_licenses.txt"] = \
        (materials.ROOT / "licenses/source/open_source_licenses.txt").read_bytes()
    files.update(extra or {})
    files.pop(missing, None)
    return archive(files)


def application(coordinates, boot=True):
    files = {}
    notice = bytearray(materials.APPLICATION_TEXT.read_bytes())
    notice.extend("\n实际 EPL 依赖的源码获取地址\nSource availability for bundled EPL dependencies\n\n".encode())
    for coordinate in coordinates:
        group, artifact, version = coordinate.split(":")
        if artifact in materials.EPL_MODULES:
            notice.extend(f"{coordinate}\n源码地址 / Source URL: {materials.source_url(coordinate)}\n\n".encode())
        if boot:
            files[f"BOOT-INF/lib/{artifact}-{version}.jar"] = archive({"example/Dependency.class": b"fixture"}).getvalue()
    if not boot:
        files["ch/qos/logback/core/Appender.class"] = b"fixture"
    files[materials.APPLICATION_NOTICE] = bytes(notice)
    return files


def jump3r_application(version="1.0.5"):
    files = application([f"de.sciss:jump3r:{version}"])
    prefix = "META-INF/third-party-sources/jump3r/"
    binary = f"jump3r-{version}.jar"
    source = f"jump3r-{version}-sources.jar"
    files["BOOT-INF/lib/" + binary] = archive({"example/Dependency.class": version.encode()}).getvalue()
    files[prefix + source] = archive({"example/Dependency.java": version.encode()}).getvalue()
    files[prefix + "ARTIFACTS.txt"] = (
        f"de.sciss:jump3r:{version} (unmodified)\n"
        f"{binary} SHA-256 {hashlib.sha256(files['BOOT-INF/lib/' + binary]).hexdigest()}\n"
        f"{source} SHA-256 {hashlib.sha256(files[prefix + source]).hexdigest()}\n"
    ).encode()
    files[prefix + "NOTICE.txt"] = (materials.ROOT / "licenses/jump3r/NOTICE.txt").read_bytes()
    files[prefix + "jump3r.py"] = (materials.ROOT / "build-script/license/jump3r.py").read_bytes()
    files["META-INF/licenses/LGPL-2.1.txt"] = (materials.ROOT / "licenses/LGPL-2.1.txt").read_bytes()
    return files


class DistributionTest(unittest.TestCase):
    def test_source_summary_must_be_complete(self):
        name = "META-INF/licenses/source/open_source_licenses.txt"
        with self.assertRaisesRegex(ValueError, "缺少"):
            materials.check_jar(fixture(missing=name))
        with self.assertRaisesRegex(ValueError, "内容不符"):
            materials.check_jar(fixture({name: b"omitted"}))

    def test_jump3r_notice_and_tool_are_required(self):
        prefix = "META-INF/third-party-sources/jump3r/"
        extra = jump3r_application()
        for name in ("NOTICE.txt", "jump3r.py"):
            with self.subTest(name=name):
                with self.assertRaisesRegex(ValueError, "缺少"):
                    materials.check_jar(fixture(extra, missing=prefix + name))
                with self.assertRaisesRegex(ValueError, "内容不符"):
                    materials.check_jar(fixture({**extra, prefix + name: b"omitted"}))

    def test_plain_library_needs_no_dependency_report_or_summary(self):
        materials.check_jar(fixture({"example/Library.class": b"fixture"}))
        with self.assertRaisesRegex(ValueError, "缺少 META-INF/THIRD-PARTY-SOURCE"):
            materials.check_jar(fixture(missing="META-INF/THIRD-PARTY-SOURCE-NOTICES.txt"))

    def test_duplicate_zip_path(self):
        output = fixture()
        with warnings.catch_warnings():
            warnings.simplefilter("ignore", UserWarning)
            with ZipFile(output, "a") as jar:
                jar.writestr("META-INF/NOTICE.txt", b"duplicated")
        with self.assertRaisesRegex(ValueError, "重复路径"):
            materials.check_jar(output)

    def test_dependency_report_does_not_mean_dependencies_are_bundled(self):
        materials.check_jar(fixture({
            "example/Library.class": b"fixture",
            "META-INF/THIRD-PARTY-NOTICES.txt":
                b"Group: ch.qos.logback  Name: logback-core  Version: 1.5.32",
        }))

    def test_boot_application_needs_complete_summary_without_dependency_report(self):
        extra = application(["example:dependency:1.0"])
        materials.check_jar(fixture(extra))
        with self.assertRaisesRegex(ValueError, "缺少应用第三方许可汇总"):
            materials.check_jar(fixture(extra, missing=materials.APPLICATION_NOTICE))
        for fragment in [b"Copyright 2017, OnGres.", b"Based on code by O. Kaser.", b"Eclipse Public License - v 2.0"]:
            with self.subTest(fragment=fragment):
                corrupted = extra[materials.APPLICATION_NOTICE].replace(fragment, b"omitted")
                with self.assertRaisesRegex(ValueError, "汇总原文不完整"):
                    materials.check_jar(fixture({**extra, materials.APPLICATION_NOTICE: corrupted}))

    def test_epl_uses_actual_boot_version_without_source_archive(self):
        for version in ["1.5.32", "1.6.0"]:
            coordinate = f"ch.qos.logback:logback-core:{version}"
            extra = application([coordinate])
            materials.check_jar(fixture(extra))
            with self.assertRaisesRegex(ValueError, "缺少对应版本的 EPL 源码地址"):
                materials.check_jar(fixture({**extra, materials.APPLICATION_NOTICE: materials.APPLICATION_TEXT.read_bytes()}))
            wrong = extra[materials.APPLICATION_NOTICE].replace(materials.source_url(coordinate).encode(), b"https://example.invalid/sources.jar")
            with self.assertRaisesRegex(ValueError, "EPL 源码地址与坐标不符"):
                materials.check_jar(fixture({**extra, materials.APPLICATION_NOTICE: wrong}))

    def test_shadow_keeps_summary_and_epl_sources(self):
        extra = application(["ch.qos.logback:logback-core:1.5.32", "ch.qos.logback:logback-classic:1.5.32"], boot=False)
        materials.check_jar(fixture(extra))
        with self.assertRaisesRegex(ValueError, "缺少应用第三方许可汇总"):
            materials.check_jar(fixture(extra, missing=materials.APPLICATION_NOTICE))
        with self.assertRaisesRegex(ValueError, "缺少 Shadow EPL 源码地址"):
            materials.check_jar(fixture({**extra, materials.APPLICATION_NOTICE: materials.APPLICATION_TEXT.read_bytes()}))

    def test_ordinary_dependency_upgrade_has_no_supplement_version_gate(self):
        for artifact in ["protobuf-java", "bcprov-jdk18on", "client-v2"]:
            materials.check_jar(fixture(application([f"example:{artifact}:99.0"])))

    def test_boot_keeps_jakarta_original_materials(self):
        extra = application(["jakarta.annotation:jakarta.annotation-api:2.1.1"])
        with self.assertRaisesRegex(ValueError, "缺少上游原声明"):
            materials.check_jar(fixture(extra))
        extra["BOOT-INF/lib/jakarta.annotation-api-2.1.1.jar"] = archive({
            "META-INF/LICENSE.md": b"original license", "META-INF/NOTICE.md": b"original notice",
        }).getvalue()
        materials.check_jar(fixture(extra))

    def test_adpcm_keeps_actual_source_and_mpl(self):
        relative = materials.ADPCM.split("/src/main/java/", 1)[1]
        source = "META-INF/third-party-sources/adpcm/" + relative
        extra = {
            relative.removesuffix(".java") + ".class": b"fixture",
            source: (materials.ROOT / materials.ADPCM).read_bytes(),
            "META-INF/licenses/MPL-2.0.txt": (materials.ROOT / "licenses/MPL-2.0.txt").read_bytes(),
        }
        materials.check_jar(fixture(extra))
        with self.assertRaisesRegex(ValueError, "缺少"):
            materials.check_jar(fixture(extra, missing=source))

    def test_bundled_jump3r_still_needs_matching_sources(self):
        with self.assertRaisesRegex(ValueError, "缺少 jump3r 匹配源码"):
            materials.check_jar(fixture(application(["de.sciss:jump3r:1.0.5"])))

    def test_jump3r_upgrade_needs_no_fixed_hash(self):
        for version in ("1.0.5", "99.0"):
            with self.subTest(version=version):
                extra = jump3r_application(version)
                materials.check_jar(fixture(extra))
                del extra[f"BOOT-INF/lib/jump3r-{version}.jar"]
                extra["de/sciss/jump3r/lowlevel/LameEncoder.class"] = b"fixture"
                materials.check_jar(fixture(extra))

    def test_jump3r_source_and_library_versions_must_match(self):
        extra = jump3r_application()
        binary = extra.pop("BOOT-INF/lib/jump3r-1.0.5.jar")
        extra["BOOT-INF/lib/jump3r-99.0.jar"] = binary
        with self.assertRaisesRegex(ValueError, "源码与实际库版本不符"):
            materials.check_jar(fixture(extra))
        extra = jump3r_application()
        manifest = "META-INF/third-party-sources/jump3r/ARTIFACTS.txt"
        extra[manifest] = extra[manifest].replace(b"jump3r-1.0.5-sources.jar", b"jump3r-99.0-sources.jar")
        with self.assertRaisesRegex(ValueError, "校验清单与声明版本不符"):
            materials.check_jar(fixture(extra))

    def test_jump3r_sources_and_lgpl_cannot_be_omitted(self):
        extra = jump3r_application()
        for name in ("META-INF/third-party-sources/jump3r/jump3r-1.0.5-sources.jar",
                     "META-INF/licenses/LGPL-2.1.txt"):
            with self.subTest(name=name), self.assertRaisesRegex(ValueError, "缺少"):
                materials.check_jar(fixture(extra, missing=name))

    def test_jump3r_distribution_checksums_still_apply(self):
        extra = jump3r_application()
        for name in ("META-INF/third-party-sources/jump3r/jump3r-1.0.5-sources.jar",
                     "BOOT-INF/lib/jump3r-1.0.5.jar"):
            with self.subTest(name=name), self.assertRaisesRegex(ValueError, "哈希不符"):
                materials.check_jar(fixture({**extra, name: b"corrupted"}))

    def test_frontend_keeps_project_license_without_backend_notices(self):
        raw = (materials.ROOT / "LICENSE").read_bytes() + b"\n" + \
            (materials.ROOT / "licenses/frontend/NOTICE.txt").read_bytes()
        with self.assertRaisesRegex(ValueError, "frontend/open_source_licenses"):
            materials.check_frontend(raw)
        materials.check_frontend(raw + b"\n" + (materials.ROOT / "licenses/frontend/open_source_licenses.txt").read_bytes())
        with self.assertRaisesRegex(ValueError, "前端汇总缺少完整原文"):
            materials.check_frontend(b"omitted")

    def test_bundled_frontend_needs_notice_in_its_directory(self):
        notice = b"\n".join((materials.ROOT / name).read_bytes() for name in (
            "LICENSE", "licenses/frontend/NOTICE.txt", "licenses/frontend/open_source_licenses.txt",
        ))
        for directory, extra in (
            ("static/dashboard/808", {}),
            ("BOOT-INF/classes/static/quickstart-ui", application(["example:dependency:1.0"])),
            ("ui", application(["ch.qos.logback:logback-core:1.5.32", "ch.qos.logback:logback-classic:1.5.32"], boot=False)),
        ):
            with self.subTest(directory=directory):
                files = {**extra, f"{directory}/index.html": b"<html></html>"}
                name = f"{directory}/{materials.NOTICE}"
                with self.assertRaisesRegex(ValueError, f"缺少 {name}"):
                    materials.check_jar(fixture({**files, f"elsewhere/{materials.NOTICE}": notice}))
                with self.assertRaisesRegex(ValueError, "前端汇总缺少完整原文"):
                    materials.check_jar(fixture({**files, name: b"omitted"}))
                materials.check_jar(fixture({**files, name: notice}))

    def test_nested_dashboard_needs_frontend_notice(self):
        extra = application(["example:dependency:1.0"])
        extra["BOOT-INF/lib/jt-808-server-dashboard-spring-boot-starter-reactive-1.0.jar"] = fixture({
            "static/dashboard/808/index.html": b"<html></html>",
        }).getvalue()
        with self.assertRaisesRegex(ValueError, "缺少 static/dashboard/808/THIRD-PARTY-FRONTEND-NOTICES.txt"):
            materials.check_jar(fixture(extra))


class FrontendMaterialsTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        self.output = self.root / "docs/src/.vuepress/dist"
        self.source = self.root / "docs/src/.vuepress/public"
        files = {"webfonts/LICENSE.txt": b"Original OFL license and copyright.",
                 "webfonts/README.txt": "字体来源说明\nFont source notes.".encode(),
                 "webfonts/font.woff2": b"font bytes",
                 "assets/fa-icon.css": b"src:url(../webfonts/font.woff2)"}
        for relative, raw in files.items():
            for directory in (self.source, self.output):
                path = directory / relative
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes(raw)
        self.summary = self.output / materials.NOTICE
        self.summary.write_bytes(files["webfonts/LICENSE.txt"] + b"\n" + files["webfonts/README.txt"])

    def test_font_update_needs_no_review_hash(self):
        with patch.object(materials, "ROOT", self.root):
            materials.check_docs_fonts(self.output)
            for directory in (self.source, self.output):
                (directory / "webfonts/font.woff2").write_bytes(b"updated font bytes")
            materials.check_docs_fonts(self.output)

    def test_damaged_output_and_missing_font(self):
        font = self.output / "webfonts/font.woff2"
        with patch.object(materials, "ROOT", self.root):
            font.write_bytes(b"changed output")
            with self.assertRaisesRegex(ValueError, "字体副本不符"):
                materials.check_docs_fonts(self.output)
            font.unlink()
            with self.assertRaises(FileNotFoundError):
                materials.check_docs_fonts(self.output)

    def test_missing_readme_from_summary(self):
        self.summary.write_bytes((self.source / "webfonts/LICENSE.txt").read_bytes())
        with patch.object(materials, "ROOT", self.root):
            with self.assertRaisesRegex(ValueError, "汇总缺少字体材料全文：README"):
                materials.check_docs_fonts(self.output)

    def test_missing_font_reference(self):
        for directory in (self.source, self.output):
            (directory / "assets/fa-icon.css").write_text("/* 字体引用缺失 */")
        with patch.object(materials, "ROOT", self.root):
            with self.assertRaisesRegex(ValueError, "CSS 未引用 WOFF2"):
                materials.check_docs_fonts(self.output)

    def test_docs_dist_checks_release_materials(self):
        report = [
            "第三方前端许可与归属声明\nThird-party frontend license and attribution notices\n".encode(),
            self.summary.read_bytes(),
        ]
        for name in ("LICENSE", "licenses/frontend/NOTICE.txt", "licenses/frontend/open_source_licenses.txt"):
            source = (materials.ROOT / name).read_bytes()
            target = self.root / name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(source)
            report.append(source)
        self.summary.write_bytes(b"\n".join(report))

        args = ["check-materials.py", "--docs-dist", str(self.output)]
        with patch.object(materials, "ROOT", self.root), patch.object(sys, "argv", args), redirect_stdout(io.StringIO()):
            materials.main()
            editable = self.output / "files/xtream-codec.drawio"
            editable.parent.mkdir()
            editable.write_text("<mxfile/>")
            with self.assertRaisesRegex(SystemExit, "包含可编辑源图"):
                materials.main()
            editable.unlink()
            (self.output / "webfonts/font.woff2").unlink()
            with self.assertRaisesRegex(SystemExit, "font.woff2"):
                materials.main()

    def test_missing_dashboard_template(self):
        report = "第三方前端许可与归属声明\nThird-party frontend license and attribution notices\n"
        report += (materials.ROOT / "LICENSE").read_text(encoding="utf-8")
        report += (materials.ROOT / "licenses/frontend/NOTICE.txt").read_text(encoding="utf-8")
        report += (materials.ROOT / "licenses/frontend/open_source_licenses.txt").read_text(encoding="utf-8")
        self.summary.write_text(report, encoding="utf-8")
        dashboard = self.root / "jt-808-server-dashboard-ui"
        dashboard.mkdir()
        (dashboard / "LICENSE").write_text("Original dashboard template license.")
        with self.assertRaisesRegex(ValueError, "汇总缺少模板许可"):
            materials.check_frontend_output(dashboard, self.output)


class CommandLineTest(unittest.TestCase):
    def test_compare_embedded_frontend_notice_with_dist(self):
        project_text = b"\n".join((materials.ROOT / name).read_bytes() for name in (
            "LICENSE", "licenses/frontend/NOTICE.txt", "licenses/frontend/open_source_licenses.txt",
        ))
        complete = project_text + b"\nfixture@1.0.0\nLegal text #1\nFixture license\n"
        entry = "ui/" + materials.NOTICE
        with tempfile.TemporaryDirectory() as temporary:
            jar = Path(temporary) / "app.jar"
            dist = Path(temporary) / materials.NOTICE
            dist.write_bytes(complete)
            args = ["check-materials.py", "--jar-frontend", str(jar), entry, str(dist)]
            with patch.object(sys, "argv", args), redirect_stdout(io.StringIO()):
                jar.write_bytes(fixture({entry: complete}).getvalue())
                materials.main()
                for content in ({}, {entry: project_text}, {entry: complete + b"\n"}):
                    with self.subTest(content_length=len(content.get(entry, b""))):
                        jar.write_bytes(fixture(content).getvalue())
                        with self.assertRaisesRegex(SystemExit, f"FAIL .*app.jar: (缺少|内容不符) {entry}"):
                            materials.main()

    def test_preview_does_not_build_frontends(self):
        with patch.object(sys, "argv", ["check-materials.py", "--preview-assets"]), \
                patch.object(materials, "preview_assets") as preview, \
                patch.object(materials, "check_frontends") as frontends:
            materials.main()
            preview.assert_called_once_with()
            frontends.assert_not_called()

    def test_preview_rejects_other_modes(self):
        for options in [["--check-only"], ["--skip-install"], ["--jar", "app.jar"], ["--frontend", "notice.txt"],
                        ["--docs-dist", "dist"]]:
            with self.subTest(options=options), \
                    patch.object(sys, "argv", ["check-materials.py", "--preview-assets", *options]), \
                    patch.object(materials, "preview_assets") as preview, redirect_stderr(io.StringIO()), \
                    self.assertRaises(SystemExit) as error:
                materials.main()
            self.assertEqual(error.exception.code, 2)
            preview.assert_not_called()

    def test_default_and_quick_modes_use_all_frontends(self):
        for options, expected in [([], (False, False)), (["--skip-install"], (True, False)),
                                  (["--check-only"], (False, True))]:
            with self.subTest(options=options), patch.object(sys, "argv", ["check-materials.py", *options]), \
                    patch.object(materials, "check_frontends") as frontends:
                materials.main()
                frontends.assert_called_once_with(*expected)

    def test_explicit_artifacts_do_not_build_frontends(self):
        with tempfile.TemporaryDirectory() as temporary:
            notice = Path(temporary) / "notice.txt"
            notice.write_bytes(b"frontend notice")
            with patch.object(sys, "argv", ["check-materials.py", "--jar", "app.jar", "--frontend", str(notice)]), \
                    patch.object(materials, "check_frontends") as frontends, \
                    patch.object(materials, "check_jar") as check_jar, \
                    patch.object(materials, "check_frontend") as check_frontend, redirect_stdout(io.StringIO()):
                materials.main()
                frontends.assert_not_called()
                check_jar.assert_called_once_with(Path("app.jar"))
                check_frontend.assert_called_once_with(b"frontend notice")

    def test_conflicting_modes_fail_before_processing(self):
        for option in ["--skip-install", "--check-only"]:
            with self.subTest(option=option), patch.object(sys, "argv", ["check-materials.py", "--jar", "app.jar", option]), \
                    patch.object(materials, "check_jar") as check_jar, redirect_stderr(io.StringIO()), \
                    self.assertRaises(SystemExit) as error:
                materials.main()
            self.assertEqual(error.exception.code, 2)
            check_jar.assert_not_called()

    def test_missing_artifact_exits_with_path(self):
        with tempfile.TemporaryDirectory() as temporary:
            missing = str(Path(temporary) / "missing.jar")
            with patch.object(sys, "argv", ["check-materials.py", "--jar", missing]), \
                    self.assertRaisesRegex(SystemExit, "missing.jar"):
                materials.main()


class PreviewAssetsTest(unittest.TestCase):
    def test_inventory_and_preview_use_recorded_paths_without_changing_images(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            image = root / "docs/src/.vuepress/public/img/example.png"
            image.parent.mkdir(parents=True)
            raw = base64.b64decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aRZcAAAAASUVORK5CYII=")
            image.write_bytes(raw)
            recorded = image.parent / "guide/recorded.png"
            recorded.parent.mkdir()
            recorded.write_bytes(raw)
            editable = root / "assets/xtream-codec.drawio"
            editable.parent.mkdir()
            editable.write_text('<mxfile><diagram name="main"><mxGraphModel><root/></mxGraphModel></diagram></mxfile>')
            (root / "assets/STATIC-ASSETS.md").write_text(
                '# 静态资源来源\n\n## 已有来源记录的文档图片\n\n| `guide/recorded.png` | 本地截图 |\n'
                '\n## 待核实资源\n\n| `example.png` | 待核对 |\n', encoding="utf-8",
            )
            (root / "README.md").write_text("![image](docs/src/.vuepress/public/img/example.png)")
            (root / "docs/src/guide.md").write_text("![image](example.png)")
            with patch.object(materials, "ROOT", root), redirect_stdout(io.StringIO()):
                materials.preview_assets()
            output = root / "build/reports/static-assets"
            records = json.loads((output / "inventory.json").read_text())
            self.assertEqual(len(records), 2)
            self.assertEqual(records[0]["referencedBy"], ["README.md", "docs/src/guide.md"])
            self.assertEqual([(record["number"], record["status"]) for record in records],
                             [(1, "待核对"), (2, "已有来源记录")])
            self.assertEqual(image.read_bytes(), raw)
            self.assertEqual(recorded.read_bytes(), raw)
            html = (output / "index.html").read_text(encoding="utf-8")
            known, pending = html.split("<details>")[1].split("</details>")
            self.assertIn("docs/src/guide.md", pending)
            self.assertIn("01 · example.png", pending)
            self.assertNotIn("02 · recorded.png", pending)
            self.assertIn("02 · recorded.png", known)


class Jump3rToolTest(unittest.TestCase):
    def test_existing_output_is_preserved(self):
        with tempfile.TemporaryDirectory() as temporary:
            output = Path(temporary) / "existing.jar"
            output.write_bytes(b"original output")
            for command in [["rebuild", "sources.jar"], ["replace", "application.jar", "library.jar"]]:
                with self.subTest(command=command), \
                        patch.object(sys, "argv", ["jump3r.py", *command, "--output", str(output)]), \
                        redirect_stderr(io.StringIO()), self.assertRaises(SystemExit) as error:
                    jump3r.main()
                self.assertEqual(error.exception.code, 2)
                self.assertEqual(output.read_bytes(), b"original output")

    def test_rebuild_rejects_source_path_traversal(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            sources = root / "sources.jar"
            sources.write_bytes(archive({"../escaped.java": b"class Escaped {}"}).getvalue())
            args = SimpleNamespace(sources=sources, output=root / "rebuilt.jar")
            with patch.object(jump3r.subprocess, "run") as run, self.assertRaisesRegex(ValueError, "源码路径超出"):
                jump3r.rebuild(args)
            run.assert_not_called()
            self.assertFalse(args.output.exists())

    def test_replace_rejects_unsupported_applications(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            library = root / "library.jar"
            library.write_bytes(archive({"de/sciss/jump3r/lowlevel/LameEncoder.class": b"fixture",
                                         "de/sciss/jump3r/mp3/Lame.class": b"fixture"}).getvalue())
            nested = {"BOOT-INF/lib/jump3r-1.0.5.jar": b"original library"}
            cases = [
                (archive({**nested, "META-INF/APP.SF": b"signature"}).getvalue(), "签名应用"),
                (archive({**nested, "BOOT-INF/lib/jump3r-other.jar": b"other"}).getvalue(), "唯一"),
                (archive({"example/Other.class": b"fixture"}).getvalue(), "唯一"),
                (b"#!/bin/sh\n" + archive(nested).getvalue(), "前置启动脚本"),
            ]
            for raw, message in cases:
                with self.subTest(message=message):
                    application = root / "application.jar"
                    application.write_bytes(raw)
                    args = SimpleNamespace(application=application, library=library, output=root / "relinked.jar")
                    with self.assertRaisesRegex(ValueError, message):
                        jump3r.replace(args)
                    self.assertEqual(application.read_bytes(), raw)
                    self.assertFalse(args.output.exists())


if __name__ == "__main__":
    unittest.main()
