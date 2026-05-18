import { useMemo, useState } from "react";
import useSWR from "swr";

import { request } from "@/utils/request.ts";

export const usePageList = <T>(
  path: string,
  initPerPage?: number,
  extraParams?: Record<string, string | boolean | undefined>,
) => {
  const [page, setPage] = useState(1);
  const rowsPerPage = initPerPage || 10;
  const extraKey = extraParams ? JSON.stringify(extraParams) : "";
  const swrKey = [path, page, rowsPerPage, extraKey] as const;
  const { data, isLoading, mutate } = useSWR<{
    total: number;
    data: T[];
  }>(
    swrKey,
    () =>
      request({
        path,
        method: "GET",
        params: {
          pageNumber: page,
          pageSize: rowsPerPage,
          ...extraParams,
        },
      }),
    {
      keepPreviousData: true,
    },
  );

  const pages = useMemo(() => {
    return data?.total ? Math.ceil(data.total / rowsPerPage) : 0;
  }, [data?.total, rowsPerPage]);

  return { page, pages, tableData: data, isLoading, setPage, mutate };
};
