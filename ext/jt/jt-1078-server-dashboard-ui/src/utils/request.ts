import axios, {
  AxiosInstance,
  AxiosRequestConfig,
  AxiosError,
  CancelTokenSource,
  InternalAxiosRequestConfig,
} from "axios";

interface ExtendedAxiosRequestConfig extends AxiosRequestConfig {
  retry?: number;
  retryDelay?: number;
}

interface LoadingHandler {
  start: () => void;
  end: () => void;
}

const config = {
  BASE_URL: import.meta.env.VITE_API_DASHBOARD_V1,
  TIMEOUT: parseInt(import.meta.env.VITE_BASE_API_TIMEOUT || "30000", 10),
};

const instance: AxiosInstance = axios.create({
  baseURL: config.BASE_URL,
  timeout: config.TIMEOUT,
  withCredentials: true,
});

const noop = () => {};

const defaultLoadingHandler: LoadingHandler = {
  start: noop,
  end: noop,
};

let loadingHandler: LoadingHandler = defaultLoadingHandler;

const setGlobalLoadingHandler = (handler: LoadingHandler) => {
  loadingHandler = handler;
};

instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    loadingHandler.start();

    return config;
  },
  (error) => {
    loadingHandler.end();

    return Promise.reject(error);
  },
);

instance.interceptors.response.use(
  (response) => {
    loadingHandler.end();

    return response;
  },
  async (error: AxiosError) => {
    loadingHandler.end();

    const originalRequest = error.config as ExtendedAxiosRequestConfig;

    if (originalRequest?.retry && originalRequest.retry > 0) {
      originalRequest.retry -= 1;
      const retryDelay = originalRequest.retryDelay || 1000;

      await new Promise((resolve) => setTimeout(resolve, retryDelay));

      return instance(originalRequest);
    }

    return Promise.reject(error);
  },
);

const request = async <T>(
  config: {
    path: string;
    method: string;
    headers?: Record<string, string>;
    data?: unknown;
    params?: Record<string, unknown>;
    cancelToken?: CancelTokenSource;
  } & AxiosRequestConfig,
): Promise<T> => {
  const { path, method, headers, data, params, ...rest } = config;

  try {
    const response = await instance({
      url: path,
      method,
      headers,
      data,
      params,
      ...rest,
    });

    return response.data;
  } catch (error) {
    throw error;
  }
};

const createCancelToken = (): CancelTokenSource => {
  return axios.CancelToken.source();
};

export { request, createCancelToken, setGlobalLoadingHandler };
