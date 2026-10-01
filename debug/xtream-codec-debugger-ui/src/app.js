import {installCompletion} from "./completion.js";

const $ = id => document.getElementById(id);
const TYPES = ["u8", "u16", "u32", "u64", "i8", "i16", "i32", "i64", "float", "string", "bcd", "hex", "bytes", "bits", "padding", "align", "struct", "array", "tlv", "map", "pair", "choice", "derived", "extension"];
// 示例同时覆盖共享字节内的位字段、长度回填、嵌套结构和重复元素。
const EXAMPLE = {
    schemaVersion: 1,
    fields: [
        {id: "version", name: "版本", type: "u3"},
        {id: "flags", name: "标志", type: "u5"},
        {id: "length", name: "负载长度", type: "u8", computedLengthOf: "payload"},
        {id: "payload", name: "负载", type: "struct", length: {ref: "length", unit: "byte"}, fields: [
            {id: "count", name: "条目数", type: "u8"},
            {id: "items", name: "条目", type: "array", length: {ref: "count", unit: "item"}, item: {
                id: "item", type: "struct", fields: [
                    {id: "code", type: "u8"},
                    {id: "value", type: "u16"}
                ]
            }}
        ]}
    ]
};
const EXAMPLE_VALUES = {version: 5, flags: 17, payload: {count: 2, items: [
    {code: 1, value: 0x1234}, {code: 2, value: 0x5678}
]}};
let mode = "dynamic";
let schema = structuredClone(EXAMPLE);
let entities = [];
let latest = null;
let selectedNode = null;
let editing = null;
// 两种模式分别保存输入草稿，切换实体与自由组合时不覆盖另一种模式的输入。
const drafts = {
    dynamic: {values: JSON.stringify(EXAMPLE_VALUES, null, 2), hex: ""},
    entity: {values: "{}", hex: ""}
};

function status(message, failed = false) {
    $("status").textContent = message;
    $("status").style.color = failed ? "#b42335" : "#61758c";
}

function readSchemaJson() {
    // JSON 编辑区是最终提交给后端的定义来源；字段表单只是常见属性的可视化入口。
    const parsed = JSON.parse($("schema-json").value);
    if (parsed.schemaVersion !== 1 || !Array.isArray(parsed.fields)) throw new Error("定义必须包含 schemaVersion: 1 和 fields 数组");
    schema = parsed;
    renderFields();
}

function syncSchemaJson() {
    $("schema-json").value = JSON.stringify(schema, null, 2);
}

function renderFields() {
    // 路径由各层数组下标组成，允许同一套增删改排序逻辑作用于任意深度的 struct。
    const container = $("fields");
    container.replaceChildren();
    const add = (fields, path = []) => {
        fields.forEach((field, index) => {
            const current = [...path, index];
            const row = document.createElement("div");
            row.className = "field-row";
            row.style.paddingLeft = `${12 + path.length * 24}px`;
            const id = document.createElement("strong");
            id.textContent = field.id || "(未命名)";
            const name = document.createElement("span");
            name.className = "field-name";
            name.textContent = field.name ? `· ${field.name}` : "";
            const meta = document.createElement("span");
            meta.className = "field-meta";
            meta.textContent = [field.type, field.bits ? `${field.bits} bit` : "", field.versions ? `版本 ${field.versions.join("、")}` : "默认版本", field.length ? `长度 ${JSON.stringify(field.length)}` : ""].filter(Boolean).join(" · ");
            const actions = document.createElement("span");
            actions.className = "field-actions";
            for (const [label, action] of [["编辑", () => openField(current)], ["↑", () => move(current, -1)], ["↓", () => move(current, 1)], ["删除", () => remove(current)]]) {
                const button = document.createElement("button");
                button.textContent = label;
                button.type = "button";
                button.addEventListener("click", action);
                actions.append(button);
            }
            if (field.type === "struct") {
                const button = document.createElement("button");
                button.textContent = "添加子字段";
                button.addEventListener("click", () => openField([...current, -1]));
                actions.append(button);
            }
            row.append(id, name, meta, actions);
            container.append(row);
            if (Array.isArray(field.fields)) add(field.fields, current);
        });
    };
    add(schema.fields);
    if (!schema.fields.length) {
        const empty = document.createElement("p");
        empty.className = "empty";
        empty.textContent = "还没有字段。添加字段或导入 JSON 定义。";
        container.append(empty);
    }
    syncSchemaJson();
}

function listAt(path) {
    let list = schema.fields;
    for (const index of path.slice(0, -1)) list = list[index].fields;
    return list;
}
function move(path, direction) {
    const list = listAt(path), index = path.at(-1), target = index + direction;
    if (target < 0 || target >= list.length) return;
    [list[index], list[target]] = [list[target], list[index]];
    renderFields();
}
function remove(path) {
    listAt(path).splice(path.at(-1), 1);
    renderFields();
}
function openField(path = [-1]) {
    // 常用字段属性单独展示；其余 DSL 属性保留在“附加属性”JSON 中，避免编辑后丢失。
    editing = path;
    const field = path.at(-1) < 0 ? {id: "", type: "u8"} : listAt(path)[path.at(-1)];
    $("field-title").textContent = path.at(-1) < 0 ? "添加字段" : "编辑字段";
    $("field-id").value = field.id || "";
    $("field-name").value = field.name || "";
    $("field-type").value = field.type || "u8";
    $("field-bits").value = field.bits ?? "";
    $("field-endian").value = field.endian || "";
    $("field-order").value = field.bitOrder || "";
    $("field-charset").value = field.charset || "";
    const length = field.length || {};
    const kind = ["fixed", "ref", "prefixBits", "remaining"].find(key => length[key] !== undefined) || "";
    $("field-length-kind").value = kind;
    $("field-length-value").value = kind && kind !== "remaining" ? length[kind] : "";
    $("field-unit").value = length.unit || "byte";
    $("field-align").value = field.alignTo ?? "";
    $("field-fill").value = field.fillBit ?? "";
    const common = new Set(["id", "name", "type", "bits", "endian", "bitOrder", "charset", "length", "alignTo", "fillBit"]);
    $("field-extra").value = JSON.stringify(Object.fromEntries(Object.entries(field).filter(([key]) => !common.has(key))), null, 2);
    $("field-error").hidden = true;
    $("field-dialog").showModal();
}
function saveField(event) {
    event.preventDefault();
    try {
        const list = listAt(editing);
        const old = editing.at(-1) < 0 ? {} : list[editing.at(-1)];
        const extra = JSON.parse($("field-extra").value || "{}");
        // 先合并附加属性，再由可视化表单覆盖已显示的属性。
        const field = {...old, ...extra, id: $("field-id").value.trim(), type: $("field-type").value};
        const optional = {name: $("field-name").value.trim(), bits: $("field-bits").value, endian: $("field-endian").value,
            bitOrder: $("field-order").value, charset: $("field-charset").value, alignTo: $("field-align").value, fillBit: $("field-fill").value};
        for (const [key, value] of Object.entries(optional)) {
            if (value === "") delete field[key];
            else field[key] = ["bits", "alignTo", "fillBit"].includes(key) ? Number(value) : value;
        }
        const kind = $("field-length-kind").value;
        if (kind) {
            field.length = {unit: $("field-unit").value, [kind]: kind === "remaining" ? true : kind === "ref" ? $("field-length-value").value : Number($("field-length-value").value)};
        } else delete field.length;
        if (field.type === "struct" && !field.fields) field.fields = [];
        if (field.type === "array" && !field.item) field.item = {id: "item", type: "u8"};
        if (["tlv", "map", "pair", "choice"].includes(field.type) && !field.variants) field.variants = {};
        // 同一标识允许提供不同版本的布局，具体版本重叠由后端编译器给出路径化诊断。
        if (/^[ui][1-9][0-9]?$/.test(field.type) && field.bits !== undefined) throw new Error("整数位宽已包含在类型中，请清空位宽输入");
        if (editing.at(-1) < 0) list.push(field);
        else list[editing.at(-1)] = field;
        $("field-dialog").close();
        renderFields();
    } catch (error) {
        $("field-error").textContent = error.message;
        $("field-error").hidden = false;
    }
}

function switchMode(next) {
    // 模式切换仅切换草稿和专属面板，不改变已经编好的动态结构。
    drafts[mode] = {values: $("values").value, hex: $("hex").value, version: $("version").value};
    mode = next;
    $("dynamic-mode").classList.toggle("active", mode === "dynamic");
    $("entity-mode").classList.toggle("active", mode === "entity");
    $("schema-panel").hidden = mode !== "dynamic";
    $("entity-panel").hidden = mode !== "entity";
    $("values").value = drafts[mode].values;
    $("hex").value = drafts[mode].hex;
    $("version").value = drafts[mode].version || "";
    localStorage.setItem("xtream-debugger-mode", mode);
}
function persist() {
    drafts[mode] = {values: $("values").value, hex: $("hex").value, version: $("version").value};
    localStorage.setItem("xtream-debugger-drafts", JSON.stringify(drafts));
    localStorage.setItem("xtream-debugger-schema", JSON.stringify(schema));
}
function renderEntities() {
    const query = $("entity-search").value.toLowerCase();
    const selected = $("entity-select").value;
    $("entity-select").replaceChildren();
    for (const entity of entities.filter(item => (item.id + item.name).toLowerCase().includes(query))) {
        const option = document.createElement("option");
        option.value = entity.id;
        option.textContent = `${entity.name} · ${entity.id}`;
        $("entity-select").append(option);
    }
    if ([...$("entity-select").options].some(option => option.value === selected)) $("entity-select").value = selected;
}
async function api(path, options) {
    const response = await fetch(path, options);
    const body = await response.json();
    // 编解码失败仍可能带部分 Trace，应交给结果面板展示，而非当成传输失败丢弃。
    if (!response.ok && !body.trace) throw new Error(body.error || `HTTP ${response.status}`);
    return body;
}
async function run(direction) {
    // 编码提交结构和值；解码提交同一结构与原始 HEX，两种模式复用响应视图。
    persist();
    try {
        if (mode === "dynamic") readSchemaJson();
        const values = direction === "encode" ? JSON.parse($("values").value) : null;
        const version = $("version").value === "" ? null : Number($("version").value);
        const request = {mode, direction, entityId: mode === "entity" ? $("entity-select").value : null,
            version, schema: mode === "dynamic" ? schema : null, values, hex: direction === "decode" ? $("hex").value : null};
        status("正在执行…");
        const result = await api("/api/v1/codec", {method: "POST", headers: {"Content-Type": "application/json"}, body: JSON.stringify(request)});
        renderResult(result);
        status(result.success ? "完成" : "执行失败", !result.success);
        if (result.success && direction === "encode") $("hex").value = result.hex;
        if (result.success && direction === "decode") $("values").value = JSON.stringify(result.values, null, 2);
        persist();
    } catch (error) {
        status(error.message, true);
        $("result-panel").hidden = false;
        $("error").textContent = error.message;
        $("error").hidden = false;
    }
}
function renderResult(result) {
    // 即使值尚未完整生成，已完成节点、错误路径与剩余输入仍可单独查看。
    latest = result;
    selectedNode = null;
    $("result-panel").hidden = false;
    $("metrics").replaceChildren();
    for (const message of [`已消费 ${result.consumedBits} bit`, `${result.consumedBytes} byte`, `剩余 ${result.remainingHex.length / 2} byte`]) {
        const item = document.createElement("span"); item.textContent = message; $("metrics").append(item);
    }
    // 校验不匹配是可查看的结果，保留完整字段和 Trace；只有执行失败才展示错误摘要。
    for (const check of result.checksums || []) {
        const item = document.createElement("span");
        item.textContent = `${check.fieldPath} · ${check.matched ? "校验通过" : "校验不匹配"} · 实际 ${check.actual} / 计算 ${check.expected}`;
        $("metrics").append(item);
    }
    $("error").hidden = !result.error;
    $("error").textContent = result.error ? `${result.error.fieldPath || "$"} · ${result.error.message}${result.error.bitOffset == null ? "" : ` · bit ${result.error.bitOffset}`}` : "";
    $("result-values").textContent = result.values == null ? "暂无完整值；可查看已完成的 Trace 字段。" : JSON.stringify(result.values, null, 2);
    $("remaining").textContent = result.remainingHex ? `未消费的原始字节：${result.remainingHex}` : "无剩余输入";
    renderBytes(result);
    renderTrace(result.trace);
}
function renderBytes(result) {
    // 点击物理字节时优先定位覆盖范围最小的节点，使共享字节中的位字段更容易选中。
    $("bytes").replaceChildren(); $("bits").replaceChildren();
    const hex = result.hex || "";
    for (let offset = 0; offset < hex.length / 2; offset++) {
        const button = document.createElement("button");
        button.textContent = hex.slice(offset * 2, offset * 2 + 2);
        button.title = `byte ${offset}`;
        button.addEventListener("click", () => {
            const ids = result.trace?.nodeIdsByByteOffset?.[offset] || [];
            const matches = result.trace?.nodes?.filter(node => ids.includes(node.id) && node.kind !== "ROOT") || [];
            const best = matches.sort((a, b) => ((a.bitEnd ?? a.byteEnd * 8) - (a.bitStart ?? a.byteStart * 8)) -
                ((b.bitEnd ?? b.byteEnd * 8) - (b.bitStart ?? b.byteStart * 8)))[0];
            if (best) selectNode(best);
        });
        $("bytes").append(button);
    }
}
function renderTrace(trace) {
    // Trace 的树顺序来自后端执行顺序，保留嵌套层级及每个节点的位/字节范围。
    $("trace").replaceChildren();
    if (!trace?.root) return;
    const visit = (node, depth) => {
        if (depth) {
            const row = document.createElement("div");
            row.className = "trace-row" + (node.status === "ERROR" ? " error-node" : "");
            row.dataset.nodeId = node.id;
            row.style.marginLeft = `${(depth - 1) * 18}px`;
            const name = document.createElement("strong"); name.textContent = node.name;
            const range = document.createElement("span"); range.className = "range";
            range.textContent = `byte [${node.byteStart ?? "?"}, ${node.byteEnd ?? "?"}) · bit [${node.bitStart ?? "?"}, ${node.bitEnd ?? "?"})`;
            const value = document.createElement("span"); value.className = "value";
            value.textContent = node.valueSummary ?? (node.value == null ? "" : JSON.stringify(node.value));
            row.append(name, range, value);
            row.addEventListener("click", () => selectNode(node));
            $("trace").append(row);
        }
        (node.children || []).forEach(child => visit(child, depth + 1));
    };
    visit(trace.root, 0);
}
function selectNode(node) {
    // 字节范围用于整体高亮，bit 范围用于同一字节内的精确定位。
    selectedNode = node;
    for (const row of $("trace").querySelectorAll(".trace-row")) row.classList.toggle("active", row.dataset.nodeId === node.id);
    for (const [offset, button] of [...$("bytes").children].entries()) {
        button.classList.toggle("active", offset >= (node.byteStart ?? Infinity) && offset < (node.byteEnd ?? -1));
    }
    $("bits").replaceChildren();
    if (node.bitStart != null && node.bitEnd != null) {
        for (let bit = Math.floor(node.bitStart / 8) * 8; bit < Math.ceil(node.bitEnd / 8) * 8; bit++) {
            const cell = document.createElement("span");
            cell.className = "bit" + (bit >= node.bitStart && bit < node.bitEnd ? " active" : "");
            const byte = parseInt(latest.hex.slice(Math.floor(bit / 8) * 2, Math.floor(bit / 8) * 2 + 2), 16);
            const lsb = node.attributes?.bitOrder === "lsb0";
            // 位序决定当前字节从高位还是低位显示，不改变后端返回的物理 bit 偏移。
            cell.textContent = Number.isNaN(byte) ? "·" : (byte >> (lsb ? bit % 8 : 7 - bit % 8)) & 1;
            cell.title = `bit ${bit}`;
            $("bits").append(cell);
        }
    }
    $("node-detail").textContent = JSON.stringify({path: node.path, value: node.value, hex: node.hex, diagnostics: node.diagnostics}, null, 2);
}

$("field-types").replaceChildren(...TYPES.map(type => {const option = document.createElement("option"); option.value = type; return option;}));
// 注册页面交互后恢复本地草稿；草稿损坏时仍能回退到可运行示例。
$("dynamic-mode").addEventListener("click", () => switchMode("dynamic"));
$("entity-mode").addEventListener("click", () => switchMode("entity"));
$("add-field").addEventListener("click", () => openField());
$("field-form").addEventListener("submit", saveField);
$("cancel-field").addEventListener("click", () => $("field-dialog").close());
$("example").addEventListener("click", () => {schema = structuredClone(EXAMPLE); renderFields(); $("values").value = JSON.stringify(EXAMPLE_VALUES, null, 2); persist();});
$("apply-json").addEventListener("click", () => {try {readSchemaJson(); status("定义已应用"); persist();} catch (error) {status(error.message, true);}});
$("import").addEventListener("click", () => $("import-file").click());
$("import-file").addEventListener("change", async event => {
    const file = event.target.files[0]; if (!file) return;
    try {$("schema-json").value = await file.text(); readSchemaJson(); persist(); status("定义已导入");}
    catch (error) {status(error.message, true);}
    event.target.value = "";
});
$("export").addEventListener("click", () => {
    try {readSchemaJson(); const url = URL.createObjectURL(new Blob([JSON.stringify(schema, null, 2)], {type: "application/json"}));
        const link = document.createElement("a"); link.href = url; link.download = "codec-schema.json"; link.click(); URL.revokeObjectURL(url);}
    catch (error) {status(error.message, true);}
});
$("entity-search").addEventListener("input", renderEntities);
$("entity-template").addEventListener("click", async () => {
    try {const id = $("entity-select").value; const value = await api(`/api/v1/entities/${encodeURIComponent(id)}/template`);
        $("values").value = JSON.stringify(value, null, 2); persist(); status("已载入实体初始值");}
    catch (error) {status(error.message, true);}
});
$("encode").addEventListener("click", () => run("encode"));
$("decode").addEventListener("click", () => run("decode"));
$("copy-hex").addEventListener("click", async () => {if (latest) {await navigator.clipboard.writeText(latest.hex); status("已复制原始 HEX");}});
$("use-result").addEventListener("click", () => {if (latest?.values != null) {$("values").value = JSON.stringify(latest.values, null, 2); persist();}});
for (const id of ["values", "hex", "version"]) $(id).addEventListener("input", persist);
try {
    const saved = JSON.parse(localStorage.getItem("xtream-debugger-drafts") || "{}");
    Object.assign(drafts.dynamic, saved.dynamic || {}); Object.assign(drafts.entity, saved.entity || {});
    schema = JSON.parse(localStorage.getItem("xtream-debugger-schema") || "null") || schema;
} catch {status("本地草稿无效，已载入示例", true);}
renderFields();
fetch("/dynamic-codec-schema.json").then(response => {
    if (!response.ok) throw new Error("无法读取定义补全资源");
    return response.json();
}).then(schema => installCompletion($("schema-json"), schema, $("schema-completions"), $("schema-complete")))
    .catch(error => status(error.message, true));
$("values").value = drafts.dynamic.values; $("hex").value = drafts.dynamic.hex;
$("version").value = drafts.dynamic.version || "";
if (localStorage.getItem("xtream-debugger-mode") === "entity") switchMode("entity");
Promise.all([api("/api/v1/entities"), api("/api/v1/extensions")]).then(([items, extensions]) => {
    entities = items; renderEntities(); $("available-extensions").textContent = extensions.join("、") || "无"; status("服务已连接");
}).catch(error => status(error.message, true));
