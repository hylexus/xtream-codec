// 仅为定义编辑提供容错解析和候选生成；跨字段约束仍由后端编译器检查。
function parseContext(text, caret) {
    const tokens = /"(?:\\[\s\S]|[^"\\])*"?|[{}[\]:,]|-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?|true|false|null/g;
    const stack = [];
    let root, context;
    const attach = value => {
        const parent = stack.at(-1);
        if (!parent) {root = value; return [];}
        const key = Array.isArray(parent.value) ? parent.value.length : parent.key;
        const path = [...parent.path, key];
        if (key !== undefined) parent.value[key] = value;
        parent.state = "comma";
        return path;
    };
    for (const match of text.matchAll(tokens)) {
        const raw = match[0], from = match.index, to = from + raw.length;
        const parent = stack.at(-1);
        const unfinished = raw.startsWith('"') && (raw.length === 1 || !raw.endsWith('"'));
        if (!context && (caret <= from || caret < to || unfinished && caret === to)) {
            const quoted = raw.startsWith('"') && caret > from && (caret < to || unfinished && caret === to);
            const key = parent?.state === "key";
            const path = parent ? (key ? parent.path : [...parent.path, parent.key ?? parent.value.length]) : [];
            const colon = key && quoted ? text.slice(to).match(/^\s*:\s*/)?.[0].length ?? 0 : 0;
            context = {path, key, from: quoted ? from : caret, to: quoted ? to + colon : caret,
                prefix: quoted ? text.slice(from + 1, caret) : "", quoted, parent};
        }
        if (raw === "{" || raw === "[") {
            const value = raw === "{" ? {} : [];
            const path = attach(value);
            stack.push({value, path, state: raw === "{" ? "key" : "value", key: undefined});
        } else if (raw === "}" || raw === "]") {
            stack.pop();
        } else if (raw === ":") {
            if (parent) parent.state = "value";
        } else if (raw === ",") {
            if (parent) {parent.state = Array.isArray(parent.value) ? "value" : "key"; parent.key = undefined;}
        } else {
            let value;
            try {value = JSON.parse(raw);} catch {value = raw.slice(1);}
            if (parent?.state === "key") {parent.key = value; parent.state = "colon";}
            else attach(value);
        }
    }
    if (!context) {
        const parent = stack.at(-1), key = parent?.state === "key";
        context = {path: parent ? (key ? parent.path : [...parent.path, parent.key ?? parent.value.length]) : [],
            key, from: caret, to: caret, prefix: "", quoted: false, parent};
    }
    return {root, context};
}

function resolve(schema, node) {
    return node?.$ref?.startsWith("#/$defs/") ? schema.$defs[node.$ref.slice(8)] : node;
}

function schemaAt(schema, path) {
    let node = schema;
    for (const part of path) {
        node = resolve(schema, node);
        node = typeof part === "number" ? node?.items : node?.properties?.[part] ?? node?.additionalProperties;
    }
    return resolve(schema, node);
}

function valueAt(value, path) {
    for (const part of path) value = value?.[part];
    return value;
}

// 只解释补全所需的条件形状，不在页面中重复实现 JSON Schema 校验器。
function matches(rule, value) {
    if (rule.required?.some(key => value?.[key] === undefined)) return false;
    if (rule.enum && !rule.enum.includes(value)) return false;
    if (rule.const !== undefined && value !== rule.const) return false;
    if (rule.not && matches(rule.not, value)) return false;
    if (rule.anyOf && !rule.anyOf.some(item => matches(item, value))) return false;
    return !rule.properties || Object.entries(rule.properties).every(([key, item]) =>
        value?.[key] === undefined || matches(item, value[key]));
}

function properties(node, value) {
    const result = {...node?.properties};
    for (const rule of node?.allOf ?? []) {
        if (!rule.if) continue;
        const branch = matches(rule.if, value) ? rule.then : rule.else;
        for (const key of branch?.not?.required ?? []) delete result[key];
    }
    for (const branch of node?.oneOf ?? []) {
        const present = branch.required?.filter(key => value?.[key] !== undefined) ?? [];
        if (present.length) {
            for (const rule of branch.not?.anyOf ?? []) {
                for (const key of rule.required ?? []) delete result[key];
            }
        }
    }
    return result;
}

function references(root, path, property) {
    const scopes = [];
    for (let index = 0; index < path.length - 1; index++) {
        if (path[index] === "fields" && typeof path[index + 1] === "number") {
            scopes.push({fields: valueAt(root, path.slice(0, index + 1)) ?? [], index: path[index + 1]});
        } else if (["item", "defaultVariant"].includes(path[index]) || path[index - 1] === "variants") {
            scopes.push({fields: [valueAt(root, path.slice(0, index + 1))], index: 0});
        }
    }
    const current = scopes.at(-1);
    if (!current) return [];
    if (["countOf", "range", "computedLengthOf"].includes(property)) {
        const direct = path.length >= 2 && path.includes("generated") || property === "computedLengthOf";
        if (!direct) return [];
        return current.fields.filter((_, index) => property === "computedLengthOf" ? index > current.index : index !== current.index)
            .filter(field => property !== "countOf" || ["array", "tlv", "map", "pair"].includes(field.type))
            .map(field => field.id).filter(Boolean);
    }
    const names = new Set();
    const add = (field, prefix = "") => {
        if (!field?.id) return;
        const name = prefix + field.id;
        names.add(name);
        for (const child of field.fields ?? []) add(child, name + ".");
    };
    for (const scope of scopes) scope.fields.slice(0, scope.index).forEach(field => add(field));
    return [...names];
}

function enumValues(schema, node) {
    node = resolve(schema, node);
    return [...(node?.enum ?? []), ...(node?.const === undefined ? [] : [node.const]), ...(node?.examples ?? []),
        ...(node?.anyOf ?? []).flatMap(item => enumValues(schema, item))];
}

export function completeDefinition(text, caret, schema) {
    const {root, context} = parseContext(text, caret);
    const node = schemaAt(schema, context.path);
    let values;
    if (context.key) {
        values = Object.keys(properties(node, valueAt(root, context.path)))
            .filter(key => key === context.prefix || valueAt(root, context.path)?.[key] === undefined);
    } else {
        values = enumValues(schema, node);
        const property = context.path.at(-1);
        if (["ref", "countOf", "range", "computedLengthOf"].includes(property) || context.path.includes("bindings")) {
            values.push(...references(root, context.path, property));
        }
    }
    return [...new Set(values)].filter(value => String(value).startsWith(context.prefix)).map(value => ({
        label: String(value), text: JSON.stringify(value) + (context.key ? ": " : ""), from: context.from, to: context.to
    }));
}
