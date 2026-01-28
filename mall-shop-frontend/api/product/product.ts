import { request, asyncRequest } from "~/utils/request";
import type {
    ProductFilterResult,
    SkuDetail,
    ProductCouponItem,
    CommentFilterResult,
    CommentDetail,
    ProductItem,
    ProductConsultationFormState,
    ProductConsultationList,
    PromotionList,
    ProductAmountItem,
    SkuAvailabilitys
} from "~/types/product/product.d";

// 获取示例模板列表
export const getProductDetail = (sn: any, skuSn: any) => {
    return request<ProductFilterResult>({
    // return asyncRequest<ProductFilterResult>({
        url: "product/product/detail",
        method: "get",
        params: { sn, skuSn }
    });
};

export const getComment = (id: number) => {
    return request<CommentDetail>({
        url: "product/product/getComment",
        method: "get",
        params: { id }
    });
};

export const getCommentList = (id: number, params: object) => {
    return request<CommentFilterResult>({
        url: "product/product/getCommentList",
        method: "get",
        params: { id, ...params }
    });
};

export const getProductSkuDetail = (id: number, params: object) => {
    return request<SkuDetail>({
    // return asyncRequest<SkuDetail>({
        url: "product/product/getProductAvailability",
        method: "get",
        params: { id, ...params }
    });
};
export const addToCart = (params: { id: number; number: number; skuId?: number; isQuick?: number }) => {
    return asyncRequest({
        url: "cart/cart/addToCart",
        method: "post",
        data: params
    });
};

export const addExchangeToCart = (params: { id: number; number: number; skuId?: number; isQuick?: number }) => {
    return asyncRequest({
        url: "product/exchange/addToCart",
        method: "post",
        data: params
    });
};

//获取猜您喜欢列表
export const getProductLikeList = (params: {}) => {
    return asyncRequest<ProductItem[]>({
        url: "common/recommend/guessLike",
        method: "get",
        params
    });
};

//获取猜您喜欢列表-Ids
export const getProductLikeListIds = () => {
    return asyncRequest<any>({
        url: "common/recommend/getProductIds",
        method: "get"
    })
}

//商品咨询列表
export const getProductConsultationList = (params: object) => {
    return request<ProductConsultationList>({
        url: "product/product/getFeedbackList",
        method: "get",
        params
    });
};
//提交商品咨询
export const addConsultation = (data: ProductConsultationFormState) => {
    return request({
        url: "user/feedback/submit",
        method: "post",
        data
    });
};

//售后服务内容
export const getAfterSaleService = (id: number) => {
    return request<any>({
        url: "common/config/afterSalesService",
        method: "get"
    });
};

//收藏商品
export const getCollectProduct = (params: object) => {
    return request<any>({
        url: "product/product/isCollect",
        method: "get",
        params
    });
};
//优惠券列表
export const getProductCouponList = (id: number) => {
    return asyncRequest<ProductCouponItem[]>({
        url: "product/product/getCoupon",
        method: "get",
        params: { id }
    });
};

//获取店铺信息
export const getShopDetail = (shopId: number) => {
    return asyncRequest<any>({
        url: "shop/shop/detail",
        method: "get",
        params: { shopId }
    });
};
export const getShopHead = (shopId: number) => {
    return asyncRequest<any>({
        url: "shop/shop/head",
        method: "get",
        params: { shopId }
    });
};
export const getShopTree = (shopId: number) => {
    return asyncRequest<any>({
        url: "shop/category/tree",
        method: "get",
        params: { shopId }
    });
};

//收藏取消店铺
export const getShopCollect = (shopId: number) => {
    return asyncRequest<any>({
        url: "shop/shop/collect",
        method: "post",
        data: { shopId }
    });
};

// 获取商品活动信息
export const getPromotion = (data: object) => {
    return request<{ [key: string]: PromotionList }>({
        url: "product/product/promotion",
        method: "post",
        data
    });
};

export const getExchangeDetail = (id: any) => {
    return request<ProductFilterResult>({
        url: "product/exchange/detail",
        method: "get",
        params: { id }
    });
};

export const getProductAmount = (data: object) => {
    return request<ProductAmountItem>({
        url: "product/product/getProductAmount",
        method: "post",
        data
    });
};

export const enquiry = (data: object) => {
    return asyncRequest({
        url: "product/product/priceInquiry",
        method: "post",
        data
    });
};

export const getBatchProductAvailability = (params: object) => {
    return asyncRequest<{ [key: string]: SkuAvailabilitys }>({
        url: "product/product/getBatchProductAvailability",
        method: "get",
        params
    });
};
