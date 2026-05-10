import { Pagination } from "@heroui/react";
import { FC, useMemo } from "react";

export interface PagePaginationProps {
  page: number;
  total: number;
  onChange: (page: number) => void;
}

export const PagePagination: FC<PagePaginationProps> = ({
  page,
  total,
  onChange,
}) => {
  const items = useMemo(
    () => Array.from({ length: total }, (_, i) => i + 1),
    [total],
  );

  if (total <= 0) {
    return null;
  }

  return (
    <Pagination className="mx-auto w-full max-w-md justify-center">
      <Pagination.Content className="flex flex-wrap items-center justify-center gap-1">
        <Pagination.Item>
          <Pagination.Previous
            isDisabled={page <= 1}
            onPress={() => onChange(Math.max(1, page - 1))}
          >
            <Pagination.PreviousIcon />
          </Pagination.Previous>
        </Pagination.Item>
        {items.map((p) => (
          <Pagination.Item key={p}>
            <Pagination.Link isActive={p === page} onPress={() => onChange(p)}>
              {p}
            </Pagination.Link>
          </Pagination.Item>
        ))}
        <Pagination.Item>
          <Pagination.Next
            isDisabled={page >= total}
            onPress={() => onChange(Math.min(total, page + 1))}
          >
            <Pagination.NextIcon />
          </Pagination.Next>
        </Pagination.Item>
      </Pagination.Content>
    </Pagination>
  );
};
