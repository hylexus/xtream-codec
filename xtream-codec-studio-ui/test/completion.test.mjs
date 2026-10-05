import {readFile} from "node:fs/promises";
import {test} from "node:test";
import assert from "node:assert/strict";
import Ajv2020 from "ajv/dist/2020.js";
import {completeDefinition} from "../src/completion.js";

const schema = JSON.parse(await readFile(new URL("../../xtream-codec-core/src/main/resources/io/github/hylexus/xtream/codec/core/dynamic/schema/dynamic-codec-schema.json", import.meta.url)));
const validate = new Ajv2020({strict: false, allErrors: true}).compile(schema);

function complete(text) {
    const caret = text.indexOf("|");
    const result = completeDefinition(text.replace("|", ""), caret, schema);
    return {result, labels: result.map(item => item.label), text: text.replace("|", "")};
}

test("补全嵌套属性、类型、算法与表达式符号", () => {
    assert.deepEqual(complete('{"schemaVersion":1,"fi|"}').labels, ["fields"]);
    assert.ok(complete('{"fields":[{"id":"n","type":"u8","gen|"}]}').labels.includes("generated"));
    assert.ok(complete('{"fields":[{"id":"n","type":"|"}]}').labels.includes("u16"));
    assert.deepEqual(complete('{"fields":[{"id":"n","type":"u32","generated":{"checksum":{"algorithm":"crc|"}}}]}').labels, ["crc32", "crc32c"]);
    assert.ok(complete('{"fields":[{"id":"n","type":"u8","generated":{"expression":{"op":"|"}}}]}').labels.includes("&&"));
    assert.ok(complete('{"fields":[{"id":"body","type":"choice","defaultVariant":{"id":"raw","type":"hex","len|"}}]}').labels.includes("length"));
});

test("引用限定到前序与同级集合，忽略字符串中的大括号", () => {
    const source = '{"fields":[{"id":"before","type":"u8","name":"\\\"{["},{"id":"n","type":"u8","generated":{"expression":{"ref":"|"}}},{"id":"later","type":"u8"}]}';
    assert.deepEqual(complete(source).labels, ["before"]);
    assert.deepEqual(complete('{"fields":[{"id":"n","type":"u8","generated":{"countOf":"|"}},{"id":"items","type":"array","item":{"id":"v","type":"u8"}}]}').labels, ["items"]);
    assert.deepEqual(complete('{"fields":[{"id":"n","type":"u8"},{"id":"s","type":"struct","fields":[{"id":"local","type":"u8"},{"id":"v","type":"derived","expression":{"ref":"|"}}]}]}').labels, ["n", "local"]);
});

test("插入属性不会重复冒号，容错处理未完成定义并过滤互斥属性", () => {
    const {result, text} = complete('{"fields":[{"id":"n","type":"u8","gen|": {}}]}');
    const candidate = result.find(item => item.label === "generated");
    const inserted = text.slice(0, candidate.from) + candidate.text + text.slice(candidate.to);
    assert.ok(JSON.parse(inserted).fields[0].generated);
    assert.ok(complete('{"fields":[{"id":"n","type":"u8","gen|').labels.includes("generated"));
    assert.ok(!complete('{"fields":[{"id":"n","type":"u8","generated":{"constant":1,"|"}}]}').labels.includes("countOf"));
    assert.ok(!complete('{"fields":[{"id":"n","type":"u8","|"}]}').labels.includes("fields"));
});

test("标准校验器接受生成字段、默认分支、长度和旧定义", () => {
    const definitions = [
        {schemaVersion: 1, fields: [{id: "n", type: "u3"}, {id: "m", type: "u5"}]},
        {schemaVersion: 1, fields: [{id: "raw", type: "hex", length: {fixed: 1, remaining: false, unit: "byte"}}]},
        {schemaVersion: 1, fields: [
            {id: "head", type: "u8", generated: {constant: 126}},
            {id: "n", type: "u8", generated: {countOf: "items"}},
            {id: "items", type: "array", length: {ref: "n", unit: "item"}, item: {id: "v", type: "u8"}},
            {id: "crc", type: "u32", generated: {checksum: {algorithm: "crc32", range: "items"}}}]},
        {schemaVersion: 1, fields: [{id: "kind", type: "u8"}, {id: "body", type: "choice", ref: "kind", variants: {},
            defaultVariant: {id: "raw", type: "hex", length: {remaining: true, unit: "byte"}}}]},
        {schemaVersion: 1, fields: [{id: "v", type: "u8", generated: {expression: {engine: "SpEL", source: "#n + 1", bindings: {n: "n"}}}}]},
        {schemaVersion: 1, fields: [{id: "v", type: "derived", expression: {op: "~", args: [{value: 1}]}}]}
    ];
    for (const definition of definitions) assert.equal(validate(definition), true, JSON.stringify(validate.errors));
});

test("标准校验器拒绝未知属性、来源冲突、错误位宽和结构组合", () => {
    for (const field of [
        {id: "n", type: "u65"},
        {id: "n", type: "u8", typo: 1},
        {id: "n", type: "u8", generated: {constant: 1, countOf: "items"}},
        {id: "n", type: "u16", generated: {checksum: {algorithm: "crc32", range: "body"}}},
        {id: "n", type: "u8", generated: {constant: 1}, computedLengthOf: "body"},
        {id: "n", type: "array", length: {fixed: 1, unit: "item"}},
        {id: "n", type: "hex", length: {fixed: 1, remaining: true, unit: "byte"}},
        {id: "n", type: "hex", length: {remaining: false, unit: "byte"}},
        {id: "n", type: "derived", expression: {op: "!", args: [{value: true}, {value: false}]}},
        {id: "n", type: "u8", defaultVariant: {id: "v", type: "u8"}}
    ]) assert.equal(validate({schemaVersion: 1, fields: [field]}), false, JSON.stringify(field));
});
