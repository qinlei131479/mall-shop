import type { CouponFilterParams, CouponFilterResult,MyCouponFilterState } from "~/types/user/coupon.d";
export const getCouponList = (params: CouponFilterParams) => {
    return asyncRequest<CouponFilterResult>({
        url: "user/coupon/getList",
        method: "get",
        params
    });
};
// 获取我的优惠券列表
export const getMyCouponList = (params: CouponFilterParams) => {
    return asyncRequest<CouponFilterResult>({
        url: "user/coupon/list",
        method: "get",
        params
    });
};
//领取优惠券
export const addCoupon = (data: object) => {
    return asyncRequest({
        url: "user/coupon/claim",
        method: "post",
        data
    });
};
//删除我的优惠券
export const delCoupon = (data: object) => {
    return asyncRequest({
        url: "user/coupon/del",
        method: "post",
        data
    });
};
// 获取我的优惠券详情
export const getMyCouponInfo = (id: any) => {
    return asyncRequest<any>({
        url: "user/coupon/detail",
        method: "get",
        params: { id }
    });
};
