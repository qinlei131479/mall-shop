import { request, asyncRequest } from "@/utils/request";
import type { CartFilterResult } from "@/types/cart/cart.d";
// 获取购物车
export const getCart = () => {
    return asyncRequest<CartFilterResult>({
        url: "cart/cart/list",
        method: "get",
        noSkipLogin: true
    });
};
// 获取示例模板列表
export const updateCartCheck = (data: any) => {
    return asyncRequest({
        url: "cart/cart/updateCheck",
        method: "post",
        data: data
    });
};
export const updateCartItemData = (cartId: any, data: { quantity?: number }) => {
    return asyncRequest({
        url: "cart/cart/updateItem",
        method: "post",
        data: { cartId, data }
    });
};
export const removeCartItemData = (cartIds: number[]) => {
    return asyncRequest({
        url: "cart/cart/removeItem",
        method: "post",
        data: { cartIds }
    });
};

export const clearCart = () => {
    return asyncRequest({
        url: "cart/cart/clear",
        method: "post"
    });
};
export const asyncGetCartCount = () => {
    return asyncRequest<any>({
        url: "cart/cart/getCount",
        method: "get",
        noSkipLogin: true
    });
};
