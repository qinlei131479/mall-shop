import { asyncRequest, request } from "@/utils/request";
import type { OrderFilterParams, OrderFilterResult, OrderFormResult, OrderNumResult, ShippingInfoResponse } from "~/types/user/order.d";

// 获取商品订单列表
export const getOrderList = (params: OrderFilterParams) => {
    return asyncRequest<OrderFilterResult>({
        url: "user/order/list",
        method: "get",
        params
    });
};

export const getOrderNum = () => {
    return asyncRequest<OrderNumResult>({
        url: "user/order/orderNum",
        method: "get"
    });
};

// 删除
export const delOrder = (data: object) => {
    return request({
        url: "user/order/delOrder",
        method: "post",
        data
    });
};
// 取消订单
export const cancelOrder = (data: object) => {
    return request({
        url: "user/order/cancelOrder",
        method: "post",
        data
    });
};
export const confirmReceipt = (data: object) => {
    return request({
        url: "user/order/confirmReceipt",
        method: "post",
        data
    });
};

export const reBuy = (data: object) => {
    return request({
        url: "user/order/buyAgain",
        method: "post",
        data
    });
};
//获取订单详情
export const getOrder = (params: object) => {
    return request<OrderFormResult>({
        url: "user/order/detail",
        method: "get",
        params
    });
};

// 更新订单
export const updateOrder = (operation: string, data: object) => {
    return request({
        url: "user/order/" + operation,
        method: "post",
        data
    });
};

export const getShippingInfo = (params: object) => {
    return asyncRequest<ShippingInfoResponse>({
        url: "user/order/shippingInfo",
        method: "get",
        params
    });
};
