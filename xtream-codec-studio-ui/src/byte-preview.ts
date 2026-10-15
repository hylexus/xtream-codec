import type { TraceNode } from './types';

export function previewFieldValue(
  node: TraceNode | undefined,
  dataType: string,
  range: [number, number] | null,
) {
  if (!node) return null;
  const bits = range ? range[1] - range[0] : null;
  const preview = {
    dataType,
    bits,
    value: '',
    hex: null as string | null,
    binary: null as string | null,
  };
  if (node.status !== 'SUCCESS') {
    preview.value = ['ERROR', 'FAILED'].includes(node.status) ? '字段执行失败' : '字段尚未完成';
    return preview;
  }
  const value = node.value === undefined ? node.valueSummary : node.value;
  preview.value =
    value === '' ? '""' : value === null ? 'null' : String(node.valueSummary ?? value ?? '—');
  const integerType = /^([ui])(\d+)$/.exec(dataType);
  const javaInteger = /^(?:byte|short|int|long|Byte|Short|Integer|Long|BigInteger)$/.test(dataType);
  const width = integerType ? Number(integerType[2]) : bits;
  if (
    (!integerType && !javaInteger) ||
    !width ||
    width > 64 ||
    (typeof value !== 'string' && typeof value !== 'number') ||
    (typeof value === 'number' && !Number.isSafeInteger(value)) ||
    !/^-?\d+$/.test(String(value))
  )
    return preview;
  const integer = BigInt(value);
  preview.value = integer.toString();
  const signed = integerType ? integerType[1] === 'i' : integer < 0n;
  const limit = 1n << BigInt(width);
  if (integer < (signed ? -limit / 2n : 0n) || integer >= (signed ? limit / 2n : limit))
    return preview;
  // 基数显示采用字段位宽的补码，字段主值仍保留后端返回的符号与字节序语义。
  const encoded = integer < 0n ? limit + integer : integer;
  preview.hex = `0x${encoded.toString(16).toUpperCase()}`;
  preview.binary = encoded.toString(2).padStart(width, '0');
  return preview;
}

export function previewBytes(hexBytes: string[]) {
  const values = hexBytes.map((hex) => parseInt(hex, 16));
  const bytes = new Uint8Array(values);
  const integer = (littleEndian: boolean, signed: boolean) => {
    if (!values.length || values.length > 8) return '—';
    const ordered = littleEndian ? [...values].reverse() : values;
    let value = ordered.reduce((current, byte) => (current << 8n) | BigInt(byte), 0n);
    const bits = BigInt(values.length * 8);
    if (signed && value & (1n << (bits - 1n))) value -= 1n << bits;
    return value.toString();
  };
  const text = (encoding: string) => {
    if (!values.length) return '—';
    try {
      // 控制字符用点显示，非法编码明确标识，避免静默替换成看似有效的文本。
      return (
        new TextDecoder(encoding, { fatal: true })
          .decode(bytes)
          .replace(/[\u0000-\u001f\u007f-\u009f]/g, '·') || '—'
      );
    } catch {
      return '无法解码';
    }
  };
  const bcd = values.every((value) => value >> 4 <= 9 && (value & 15) <= 9)
    ? hexBytes.join('')
    : '非 BCD';
  return {
    encodings: [
      [
        'ASCII',
        values
          .map((value) => (value >= 0x20 && value <= 0x7e ? String.fromCharCode(value) : '·'))
          .join('') || '—',
      ],
      ['UTF-8', text('utf-8')],
      ['GBK', text('gbk')],
      ['GB2312', text('gb2312')],
      ['BCD', bcd || '—'],
    ],
    numbers: [
      ['HEX', hexBytes.join(' ').toUpperCase() || '—'],
      ['BIN', values.map((value) => value.toString(2).padStart(8, '0')).join(' ') || '—'],
      ['UInt BE', integer(false, false)],
      ['UInt LE', integer(true, false)],
      ['Int BE', integer(false, true)],
      ['Int LE', integer(true, true)],
    ],
  };
}
