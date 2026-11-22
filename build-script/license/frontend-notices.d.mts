export declare function frontendNotices(options?: { additionalLicenseFiles?: string[] }): {
  name: string;
  apply: 'build';
  configResolved(config: { root: string }): void;
  generateBundle(this: {
    getModuleIds(): IterableIterator<string>;
    emitFile(asset: { type: 'asset'; fileName: string; source: string }): string;
  }, options: unknown, bundle: Record<string, {
    type: string;
    modules?: Record<string, { renderedLength: number }>;
  }>): void;
};
