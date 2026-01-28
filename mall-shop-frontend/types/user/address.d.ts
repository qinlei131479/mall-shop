import type { BaseResponseListWrap } from "~/types/api";
export interface AddressFilterParams {
    page: number;
    size: number;
}

// 获取列表返回参数类型
export interface AddressFilterState {
    consignee: string; // 收货人姓名
    regionName: string; // 地区名
    addressId: number; //
    address: string; // 详细地址
    email: string; // 电子邮箱
    telephone: string; // 电话号码
    mobile: string; // 手机号码
}
//详情参数
export interface AddressFormState {
    consignee: string; // 收货人姓名
    regionIds: number[]; // 地区id
    regionName?: string; // 地区名
    addressId?: number; //
    address: string; // 详细地址
    email: string; // 电子邮箱
    telephone: string; // 电话号码
    mobile: string; // 手机号码
    userId?: number;
    isSelected?: number;
    isDefault?: number;
}
export interface AddressFilterResult extends BaseResponseListWrap<AddressFilterState[]>{
    filter: AddressFilterParams;
    optimizeCountSql: AddressFilterResult;
    searchCount: AddressFilterResult;
}

export interface AddressFormResult {
    item: AddressFormState;
}

export interface RegionFilterState {
    errcode: number;
    records: Array<RegionList[]>;
    message: string;
}

export interface RegionList {
    regionId: number;
    regionName: string;
    parentId: number;
    level: number;
    isHot: number;
    firstWord: string;
}
