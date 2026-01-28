import { asyncRequest } from "~/utils/request";
import type { CollectProductFilterParams, CollectProductFilterResult } from "~/types/user/collectProduct.d";

// 收藏商品列表
export const getCollectProductList = (params?: CollectProductFilterParams) => {
    return request<CollectProductFilterResult>({
        url: "user/collectProduct/list",
        method: "get",
        params
    });
};

// 取消收藏商品
export const delCollectProduct = (data: object) => {
    return asyncRequest({
        url: "user/collectProduct/cancel",
        method: "post",
        data
    });
};
// 收藏商品
export const updateCollectProduct = (data: object) => {
    return asyncRequest({
        url: "user/collectProduct/save",
        method: "post",
        data
    });
};
