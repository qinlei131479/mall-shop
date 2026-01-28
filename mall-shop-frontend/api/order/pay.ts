import { request, asyncRequest } from "@/utils/request";
export const orderPayInfo = (params: {}) => {
    return request({
        url: "order/pay/index",
        method: "get",
        params
    });
};
export const creatPay = (params: {}) => {
    return asyncRequest({
        url: "order/pay/create",
        method: "get",
        params
    });
};

export const checkPayStatus = (params: {}) => {
    return asyncRequest({
        url: "order/pay/checkStatus",
        method: "get",
        params
    });
};
