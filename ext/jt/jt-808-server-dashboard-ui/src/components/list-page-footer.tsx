import { PagePagination } from "@/components/page-pagination.tsx";

type ListPageFooterProps = {
  total: number;
  page: number;
  pages: number;
  onPageChange: (page: number) => void;
};

export function ListPageFooter({
  total,
  page,
  pages,
  onPageChange,
}: ListPageFooterProps) {
  return (
    <div className="mt-4 flex w-full shrink-0 items-center justify-center gap-3">
      <p className="text-small text-muted">总数：{total}</p>
      {pages > 0 ? (
        <PagePagination page={page} total={pages} onChange={onPageChange} />
      ) : null}
    </div>
  );
}
