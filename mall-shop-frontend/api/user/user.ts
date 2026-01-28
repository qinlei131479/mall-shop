import { request, asyncRequest } from "@/utils/request";
import type { UserFormResult,UserFormState } from "~/types/user/user";
//
export const getUser = () => {
    return asyncRequest<UserFormState>({
        url: "user/user/detail",
        method: "get",
        noSkipLogin: true
    });
};

export const getMemberCenter = () => {
    return asyncRequest<UserFormResult>({
        url: "user/user/memberCenter",
        method: "get"
    });
};
//最近浏览
export const getHistoryProduct = (params?: any) => {
    return asyncRequest<any>({
        url: "user/user/historyProduct",
        method: "get",
        params
    });
};
