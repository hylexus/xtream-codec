export function getKeyValue<T extends object>(
  obj: T,
  key: PropertyKey,
): unknown {
  return obj[key as keyof T];
}
