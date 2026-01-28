import { asyncRequest } from "~/utils/request";
import type { AfterSalesFilterParams, AfterSalesFilterResult, AfterSalesFormResult } from "~/types/user/afterSales";

// 可售后的订单列表
export const getAfterSalesList = (params: AfterSalesFilterParams) => {
    return asyncRequest<AfterSalesFilterResult>({
        url: "user/aftersales/list",
        method: "get",
        params
    });
};
//售后配置
export const getAfterSalesConfig = () => {
    return asyncRequest<any>({
        url: "user/aftersales/config",
        method: "get"
    });
};
export const getAfterSales = (params: object) => {
    return asyncRequest<AfterSalesFormResult>({
        url: "user/aftersales/applyData",
        method: "get",
        params
    });
};
export const updateAfterSales = (data: object) => {
    return asyncRequest({
        url: "user/aftersales/create",
        method: "post",
        data
    });
};
