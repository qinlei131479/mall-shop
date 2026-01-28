import { request, asyncRequest } from "@/utils/request";
import type {
    LangListItem,
    DefaultLangItem,
    CurrencyItem,
    MobileAreaCodeList,
    ProductsListResponse,
    BaseConfigResponse,
    ThemeSettingsResponse
} from "@/types/common/common.d.ts";
// 模糊搜索
export const getNav = () => {
    return request({
        url: "common/pc/getNav",
        method: "get"
    });
};
export const getCatFloor = () => {
    return request({
        url: "common/pc/getCatFloor",
        method: "get"
    });
};
export const getSearchGuessData = (keyword: string) => {
    return request<any>({
        url: "search/searchGuess/index",
        method: "get",
        params: { keyword }
    });
};

export interface TopRegionResult {
    regionList: {
        regionName: string;
        regionId: number;
    }[];
}
export const getTopRegion = () => {
    return asyncRequest<TopRegionResult>({
        url: "sys/region/getProvinceList",
        method: "get"
    });
};
export const getUserRegion = () => {
    return asyncRequest<TopRegionResult>({
        url: "sys/region/getUserRegion",
        method: "get"
    });
};

// 获取基础配置
export const getBaseConfig = (params?: any) => {
    return request<BaseConfigResponse>({
        url: "common/config/initConfigSettings",//common/config/base
        method: "get",
        params
    });
};

// 获取主题配置
export const getThemeSettings = () => {
    return request<ThemeSettingsResponse>({
        url: "common/config/themeSettings",
        method: "get"
    });
};

// 记录日志
export const commonLog = (params: object) => {
    return asyncRequest({
        url: "common/log",
        method: "get",
        params
    });
};

// 获取语言包
export const getLang = (localeCode: string) => {
    return asyncRequest({
        url: "common/i18n/getLocaleTranslations?localeCode=" + localeCode,
        method: "get"
    });
};

// 翻译
export const getTranslate = (data: object) => {
    return asyncRequest<any>({
        url: "common/translate/translate",
        method: "post",
        data
    });
};

// 获取语言列表
export const getLangList = () => {
    return request<LangListItem[]>({
        url: "common/i18n/getLocales",
        method: "get"
    });
};

// 获得默认语言
export const getDefaultLang = (code: string) => {
    return request<DefaultLangItem>({
        url: `common/i18n/getDefaultLocale?code=${code}`,
        method: "get"
    });
};

// 货币列表
export const getCurrencyList = () => {
    return request<CurrencyItem[]>({
        url: "common/currency/getCurrency",
        method: "get"
    });
};

// 获取区域code
export const getmobileAreaCode = () => {
    return request<MobileAreaCodeList[]>({
        url: "common/config/mobileAreaCode",
        method: "get"
    });
};

// 通用获取商品列表
export const getProductsList = (params: object) => {
    return asyncRequest<ProductsListResponse>({
        url: "product/product/list",
        method: "get",
        params
    });
};
