import {cp, mkdir} from "node:fs/promises";
import {fileURLToPath} from "node:url";
import {resolve} from "node:path";

const root = fileURLToPath(new URL("../", import.meta.url));
const output = resolve(root, "dist");
// 前端使用原生模块与静态资源；构建只复制页面文件，供轻量 HTTP 应用打包。
await mkdir(output, {recursive: true});
for (const file of ["index.html", "app.js", "completion.js", "style.css"]) {
    await cp(resolve(root, "src", file), resolve(output, file));
}
await cp(resolve(root, "../../xtream-codec-core/src/main/resources/io/github/hylexus/xtream/codec/core/dynamic/schema/dynamic-codec-schema.json"),
    resolve(output, "dynamic-codec-schema.json"));
console.log("已生成前端构建产物: " + output);
