import type { shopListResponse } from "~/types/shop/shop";

//获得相关分类
export const getShopDecorate = (params: object) => {
    return request({
        url: "shop/shop/decorate",
        method: "get",
        params
    });
};

// 获得店铺列表
export const getShopList = (params: object) => {
    return asyncRequest<shopListResponse>({
        url: "shop/shop/list",
        method: "get",
        params
    });
};
