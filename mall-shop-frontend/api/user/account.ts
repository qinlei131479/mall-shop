import { asyncRequest } from "~/utils/request";
import type {
    AccountDetailFilterResult,
    AccountFilterParams,
    AccountFilterResult,
    AccountFormResult,
    AccountInfo,
    AccountInfoResult,
    DepositFilterResult,
    DepositFilterState
} from "~/types/user/account.d";

// 申请记录
export const getRechargeOrderList = (params?: AccountFilterParams) => {
    return asyncRequest<AccountFilterResult>({
        url: "user/rechargeOrder/list",
        method: "get",
        params
    });
};

// 账户变动记录
export const getAccountList = (params?: AccountFilterParams) => {
    return asyncRequest<AccountDetailFilterResult>({
        url: "user/account/list",
        method: "get",
        params
    });
};

export const getDepositList = () => {
    return asyncRequest<DepositFilterState[]>({
        url: "user/rechargeOrder/setting",
        method: "get"
    });
};

export const updateRechargeOrder = (data: object) => {
    return asyncRequest({
        url: "user/rechargeOrder/update",
        method: "post",
        data
    });
};

export const updateRechargeOrderPay = (data: object) => {
    return asyncRequest<any>({
        url: "user/rechargeOrder/pay",
        method: "post",
        data
    });
};

export const updateRechargeOrderCreate = (data: object) => {
    return asyncRequest<any>({
        url: "user/rechargeOrder/create",
        method: "post",
        data
    });
};

export const getPaymentList = () => {
    return asyncRequest<string[]>({
        url: "user/rechargeOrder/paymentList",
        method: "get"
    });
};

export const updateAccount = (data: object, url: string) => {
    return asyncRequest({
        url: "user/withdrawApply/" + url,
        method: "post",
        data
    });
};

export const getAccountNoList = (params?: AccountFilterParams) => {
    return asyncRequest<AccountInfoResult>({
        url: "user/withdrawApply/list",
        method: "get",
        params
    });
};

export const getAccount = (params?: object) => {
    return asyncRequest<AccountFormResult>({
        url: "user/withdrawApply/accountDetail",
        method: "get",
        params
    });
};
export const delAccount = (params: object) => {
    return asyncRequest({
        url: "user/withdrawApply/delAccount",
        method: "post",
        params
    });
};
// 保存
export const updateWithdrawApply = (data: object) => {
    return asyncRequest({
        url: "user/withdrawApply/apply",
        method: "post",
        data
    });
};
export const checkUserPayStatus = (params: {}) => {
    return asyncRequest({
        url: "user/rechargeOrder/checkStatus",
        method: "get",
        params
    });
};
