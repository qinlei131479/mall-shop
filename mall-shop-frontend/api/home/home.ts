import { request, asyncRequest } from "@/utils/request";
import type { HomeDataResult, CouponResponse } from "@/types/home/home.d";
import type { SeckillFilterResult } from "~/types/home/seckill";
import type { FriendLinksFilterState } from "~/types/home/friendLinks";
import type { ProductFilterResult } from "~/types/product/product";
// 获取示例模板列表
export const getHomeData = (params: {}) => {
    return request<HomeDataResult>({
        url: "home/home/pcIndex",
        method: "get",
        params
    });
};
export const getHomeRecommendProductData = (params: any) => {
    return asyncRequest<ProductFilterResult>({
        url: "home/home/getRecommend",
        method: "get",
        params
    });
};
// 首页秒杀
export const getHomeSeckill = (params?: object) => {
    return asyncRequest<SeckillFilterResult>({
        url: "home/home/getSeckill",
        method: "get",
        params
    });
};

// 首页优惠券
export const getHomeCoupon = () => {
    return asyncRequest<CouponResponse[]>({
        url: "home/home/getCoupon",
        method: "get"
    });
};
// 首页友情链
export const getHomeFriendLinks = () => {
    return request<FriendLinksFilterState[]>({
        url: "home/home/friendLinks",
        method: "get"
    });
};
// 首页客服
export const getHomeCustomerServiceConfig = () => {
    return request<any>({
        url: "home/home/getCustomerServiceConfig",
        method: "get"
    });
};
//登陆背景图片
export const getMobileNav = (params: any) => {
    return asyncRequest<any>({
        url: "home/home/mobileNav",
        method: "get",
        params
    });
};
//商品列表
export const getProductList = (params: any) => {
    return asyncRequest<any>({
        url: "home/home/getRecommend",
        method: "get",
        params
    });
};

