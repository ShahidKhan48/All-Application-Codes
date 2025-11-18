import { getAuthData, getUserIds } from "@/lib/storage";
import axios, {
  AxiosError,
  InternalAxiosRequestConfig,
  AxiosResponse,
} from "axios";

/**
 * use apiClient to enable interceptors defined in useAxiosInterceptor.ts
 */
export const apiClient = axios.create({
  headers: {
    "Content-Type": "application/json",
  },
});

const TOOL_ID = import.meta.env.VITE_PUBLIC_TOOL_ID;

apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig<unknown>) => {
    const noAuthRoute = config?.url?.includes("runWithNoAuth");

    const userIdsStorage = getUserIds();
    const authData = getAuthData(); // could be null
    const token = authData?.token;

    if (!config.headers["x-nc-tool-id"]) {
      config.headers["x-nc-tool-id"] = TOOL_ID;
    }

    if (!config.headers["x-nc-system-user-id"]) {
      config.headers["x-nc-system-user-id"] = userIdsStorage?.userId;
    }

    if (!noAuthRoute && token && !config.headers.Authorization) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// Remove extra data from request
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    if (response?.data?.error) {
      return Promise.reject(response.data);
    }
    return Promise.resolve(response.data);
  },
  async (error) => {
    // logout if session expired
    if (error?.response?.status === 401) {
      localStorage.clear();
      window.history.pushState(null, "", "/");
      window.location.reload();
    }

    // Reject promise if usual erro
    return Promise.reject(error?.response?.data);
  }
);
