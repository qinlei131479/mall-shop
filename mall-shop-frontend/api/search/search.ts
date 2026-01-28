import { request, asyncRequest } from "~/utils/request";
import type { filterSeleted, SearchFilter, SearchFilterResult, SearchProductListResult } from "@/types/search/search.d";

// 获取搜索筛选项
export const getSearchFilter = (data: {}) => {
    return request<SearchFilterResult>({
        url: "search/search/getFilter",
        method: "post",
        data: data
    });
};

// 获取ES搜索筛选项
export const getSearchEsFilter = (data: {}) => {
    return request<any>({
        url: "search/search/getAttributeSuggestions",
        method: "get",
        params: data
    });
};

// 获取搜索商品列表
export const getSearchProductList = (data: {}) => {
    return asyncRequest<SearchProductListResult>({
        url: "search/search/getProduct",
        method: "post",
        data: data
    });
};
