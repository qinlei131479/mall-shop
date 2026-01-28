import { asyncRequest } from "~/utils/request";
import type { AddressFilterParams, AddressFilterResult, AddressFormState } from "~/types/user/address.d";
// 获取收货地址列表
export const getAddressList = (params: AddressFilterParams) => {
    return asyncRequest<AddressFilterResult>({
        url: "user/address/list",
        method: "get",
        params: params
    });
};

// 删除
export const delAddress = (data: object) => {
    return asyncRequest({
        url: "user/address/del",
        method: "post",
        data
    });
};
//获取收货地址详情
export const getAddressData = (action: string, params: object) => {
    return asyncRequest<AddressFormState>({
        url: "user/address/" + action,
        method: "get",
        params
    });
};

// 更新地址详情
export const updateAddressData = (operation: string, data: object) => {
    return asyncRequest<any>({
        url: "user/address/" + operation,
        method: "post",
        data
    });
};

export const setSelectedAddress = (id: number) => {
    return request({
        url: `user/address/setSelected`,
        method: "post",
        data: { id }
    });
};
