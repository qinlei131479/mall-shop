import { request, asyncRequest } from "~/utils/request";
import type { CategoryNavigation } from "@/types/product/category.d";
import type { ProductConsultationItem } from "~/types/product/product";

export const getParentCategoryTree = (id: number) => {
    return request<CategoryNavigation[]>({
        url: "category/category/parentTree",
        method: "get",
        params: { id }
    });
};

//获得相关分类
export const getProductRelateCategory = (params: object) => {
    return asyncRequest<ProductConsultationItem[]>({
        url: "category/category/relateCategory",
        method: "get",
        params
    });
};

//获得相关品牌
export const getProductRelateBrand = (params: object) => {
    return asyncRequest<ProductConsultationItem[]>({
        url: "category/category/relateBrand",
        method: "get",
        params
    });
};
//获得相关排行
export const getProductRelateRank = (params: object) => {
    return asyncRequest<any>({
        url: "category/category/relateRank",
        method: "get",
        params
    });
};

//获得相关文章
export const getProductRelateArticle = (params: object) => {
    return asyncRequest<any>({
        url: "category/category/relateArticle",
        method: "get",
        params
    });
};
//获得看了又看
export const getProductRelateLookAlso = (params: object) => {
    return asyncRequest<any>({
        url: "category/category/relateLookAlso",
        method: "get",
        params
    });
};
//获取关联商品
export const getProductRelateProductList = (params: object) => {
    return asyncRequest<any>({
        url: "product/product/getRelated",
        method: "get",
        params
    });
};
