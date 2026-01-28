import axios, { AxiosResponse, AxiosRequestConfig, AxiosError } from "axios";
import { useUserStore } from "@/store/user";
import { message } from "ant-design-vue";
import { formatArguments } from "@/utils/util";
import { isS2b2c, isStore, isMerchant } from "@/utils/version";
const { VITE_BASE_URL, VITE_REQUEST_URL_PREFIX } = import.meta.env;

const requestUrl = {
    prefix: VITE_BASE_URL + VITE_REQUEST_URL_PREFIX, // 所有的请求地址前缀部分
    headers: () => {
        const accessToken = localStorage.getItem("accessToken");
        const headers: any = {
            "X-ADMIN-TYPE": localStorage.getItem("adminType") || 'admin',
            "X-ClIENT-TYPE": 'admin'
        };
        if (accessToken) {
            headers["Authorization"] = 'Bearer ' + accessToken;
        }
        if (isS2b2c()) {
            headers["X-Vendor-Id"] = localStorage.getItem("vendorId") || 0;
        }
        if (isStore() || isMerchant()) {
            headers["X-Shop-Id"] = localStorage.getItem("shopId") || 0;
        }

        return headers;
    }
};

// 扩展 AxiosRequestConfig 接口以包含 prefix 参数
interface CustomAxiosRequestConfig extends AxiosRequestConfig {
    prefix?: string;
    noErrorTip?: boolean;
}
// 创建一个 axios 实例
const createAxiosInstance = (baseURL: string) => {
    return axios.create({
        baseURL, // 请求前缀
        timeout: 60000, // 请求超时时间毫秒
        headers: { "Content-Type": "application/json" }
    });
};
// 添加请求拦截器
const setupInterceptors = (instance: any) => {
    instance.interceptors.request.use(
        (config: AxiosRequestConfig) => {
            const accessToken = localStorage.getItem("accessToken");
            if (accessToken) {
                config.headers!["Authorization"] = `Bearer ${accessToken}`;
            }
            if (isS2b2c()) {
                config.headers!["X-Vendor-Id"] = localStorage.getItem("vendorId") || 0;
            }
            if (isStore() || isMerchant()) {
                config.headers!["X-Shop-Id"] = localStorage.getItem("shopId") || 0;
            }
            config.headers!["X-ADMIN-TYPE"] = localStorage.getItem("adminType") || 'admin';
            config.headers!["X-ClIENT-TYPE"] = 'admin';
            config.params = formatArguments(config.params);
            return config;
        },
        (error: AxiosError) => Promise.reject(error)
    );

    // 添加响应拦截器
    instance.interceptors.response.use(
        (response: AxiosResponse) => {
            return response;
        },
        (error: any) => {
            if (error.response) {
                const store = useUserStore() as any;
                const data = error.response.data;
                const status = error.response.status;
                if (status === 403 || status === 401) {
                    // 禁止访问、无效Token
                    // 只有在确实有登录状态的情况下才提示并登出
                    const accessToken = localStorage.getItem("accessToken");
                    if (accessToken) {
                        message.error("登录已过期，请重新登录");
                        setTimeout(() => {
                            store.logout();
                        }, 200);
                    }
                } else if (data.msg) {
                    if (!error.config.noErrorTip) {
                        message.error(data.msg);
                    }
                }
            }
            return Promise.reject(error) as any;
        }
    );
};

type ApiResponse<T> = T & {
    code: number;
    message: string;
    data: T;
};
export function request<T>(config: CustomAxiosRequestConfig): Promise<ApiResponse<T>> {
    const prefix = config.prefix || VITE_REQUEST_URL_PREFIX;
    const baseURL = `${VITE_BASE_URL}${prefix}`;
    const axiosInstance = createAxiosInstance(baseURL);
    const store = useUserStore() as any;
    setupInterceptors(axiosInstance);
    return axiosInstance(config).then((response: AxiosResponse<ApiResponse<T>>) => {
        const apiResponse = response.data;
        if (config.responseType) {
            if (apiResponse.code > 0) {
                return Promise.reject(apiResponse.data);
            }
            return apiResponse;
        } else {
            if (apiResponse.code === 401) {
                // 只有在确实有登录状态的情况下才提示并登出
                const accessToken = localStorage.getItem("accessToken");
                if (accessToken) {
                    message.error("登录已过期，请重新登录");
                    setTimeout(() => {
                        store.logout();
                    }, 200);
                }
            } else if (apiResponse.code > 0 && apiResponse.code != 401) {
                return Promise.reject(apiResponse);
            } else {
                return apiResponse.data as any;
            }
        }
    }).catch(error => {
        // 处理错误，确保返回的错误也是一个 ApiResponse<T> 类型的对象
        return Promise.reject({
            code: error.code,
            message: error.message,
            data: error?.data ?? null
        } as ApiResponse<T>);
    });
}

export default request;

export { requestUrl };