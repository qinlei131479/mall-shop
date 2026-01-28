import { ElMessage } from "element-plus";
import { aesEncrypt } from "~/components/verifition/utils/ase";
import { getCurrentRoute, formatArguments } from "~/utils/util";
import { useUserStore } from "~/store/user";

interface ApiConfig {
    url: string;
    method: "get" | "post" | "put" | "delete"; // 限制为具体的HTTP方法
    params?: any;
    data?: any;
    noSkipLogin?: boolean;
    headers?: Record<string, string>; // 可选的自定义请求头
}

type ApiResponse<T> = {
    code: number;
    msg: string;
    message: string;
    data: T;
};

type ApiResponseData<T> = T & {
    code: number;
    message: string;
    data?: T;
};

export const X_CLIENT_TYPE = "pc";
const { VITE_API_URL, VITE_API_PREFIX } = import.meta.env;
type ApiResponseSuccess<T> = ApiResponse<ApiResponseData<T>>;
type ApiResponseError<T> = ApiResponse<ApiResponseData<T>>;
export const requestPrefix = VITE_API_URL + VITE_API_PREFIX;

//接口加密密钥--请自行修改
const key = "TigShopApiSecret";

const requestSecret = () => {
    const currentTimeStamp = Math.floor(Date.now() / 1000);
    const secret = aesEncrypt(currentTimeStamp, key);
    return {
        Secret: secret
    };
};

export const requestHeader = () => {
    const userStore = useUserStore();

    const localeCode = useCookie("X-Locale-Code").value;

    return {
        "X-Client-Type": X_CLIENT_TYPE,
        Authorization: "Bearer " + (userStore.isAuthenticated ? userStore.token : null),
        ...requestSecret(),
        "X-Locale-Code": localeCode
    };
};

const requestConfig = (config: ApiConfig) => {
    const data = formatArguments(config.data || config.params);

    const headers = {
        "Content-Type": "application/json",
        ...requestHeader()
    };

    if (config.headers) {
        Object.assign(headers, config.headers);
    }

    // 设置请求头
    return {
        body: config.method === "post" ? JSON.stringify(config.data) : null,
        params: config.method === "post" ? "" : data,
        headers,
        baseURL: requestPrefix + "/",
        method: config.method,
        noSkipLogin: config.noSkipLogin || false
    };
};

export const toLogin = () => {
    const router = getCurrentRoute();
    useUserStore().clearToken();
    navigateTo(`/member/login?returnUrl=${router.path ? router.path : ""}`);
    return Promise.reject({ message: "请登录" });
};

let errdata = {
    errcode: 1,
    message: "服务器连接失败，请检查网络"
};

export async function request<T>(config: ApiConfig): Promise<T> {
    type UseFetchResult = { data: Ref<ApiResponseSuccess<T>>; error: Ref<ApiResponseError<T> | null> };
    const userStore = useUserStore();
    try {
        const result: UseFetchResult = await useFetch(config.url, {
            ...requestConfig(config)
        });
        if (result.error.value) {
            // 检查错误代码，并根据需要处理
            return Promise.reject(result.error.value.data.data);
        }
        if (result.data.value) {
            if (result.data.value.code === 0) {
                const responseData = result.data.value.data;
                return responseData as ApiResponseSuccess<T>["data"];
            } else {
                if (result.data.value.code === 401 && (userStore.token || !config.noSkipLogin)) {
                    return toLogin();
                }
                return Promise.reject(result.data.value);
            }
        }

        return Promise.reject(errdata);
    } catch (error) {
        console.error(error);
        return Promise.reject(error);
    }
}

export async function asyncRequest<T>(config: ApiConfig): Promise<T> {
    const userStore = useUserStore();
    try {
        let result: ApiResponseData<T> = await $fetch(config.url, {
            ...requestConfig(config),
            lazy: true,
            server: false,
            onResponseError: (e) => {
                if (e.response._data.code === 403) {
                    // 禁止访问、无效Token
                } else if (e.response._data.code === 401) {
                    //登录已过期
                } else if (e.response._data.code === 400) {
                    // 服务器错误
                    ElMessage.error(e.response._data.msg);
                } else if (e.response._data.code === 500) {
                    ElMessage.error(e.response._data.msg);
                }
            }
        });

        if (typeof result === "string") result = JSON.parse(result);

        if (result.code === 0) {
            const responseData = result.data;
            return responseData as ApiResponseSuccess<T>["data"];
        }

        if (result.code === 401 && (userStore.token || !config.noSkipLogin)) {
            return toLogin();
        }
        return Promise.reject(result);
    } catch (error) {
        console.error(error);
        return Promise.reject(error);
    }
}

export default request;
