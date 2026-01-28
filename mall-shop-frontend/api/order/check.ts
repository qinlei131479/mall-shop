import { request, asyncRequest } from "@/utils/request";
import type { PaymentTypeItem, ShippingTypeItem, CheckItem } from "@/types/order/check";

export const getOrderCheck = (data: object) => {
    return asyncRequest<CheckItem>({
        url: "order/check/index",
        method: "post",
        data
    });
};

export const updateOrderCheck = (data: object) => {
    return asyncRequest<CheckItem>({
        url: "order/check/update",
        method: "post",
        data
    });
};

export const updateCouponData = (data: any) => {
    return asyncRequest<CheckItem>({
        url: "order/check/updateCoupon",
        method: "post",
        data: data
    });
};

export const orderSubmit = (data: any) => {
    return asyncRequest<any>({
        url: "order/check/submit",
        method: "post",
        data: data
    });
};

export const getCheckInvoice = (params: object) => {
    return asyncRequest<any>({
        url: "order/check/getInvoice",
        method: "get",
        params
    });
};

export const getPaymentType = () => {
    return asyncRequest<PaymentTypeItem[]>({
        url: "order/check/getAvailablePaymentType",
        method: "get"
    });
};

export const getShippingType = (params: object) => {
    return asyncRequest<{ [key: string]: ShippingTypeItem[] }>({
        url: "order/check/getStoreShippingType",
        method: "get",
        params
    });
};
