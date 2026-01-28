import { useCommonStore } from "@/store/common";
import { isUrl } from "@/utils/util";

// 格式化图片地址
export function imageFormat(path: string | undefined) {
    const commonStore = useCommonStore();
    if (!path) {
        return "";
    }
    return path.includes("http") ? path : commonStore.storageUrl + path;
}

export function imageFormatList(path: string[] | undefined) {
    if (path && path?.length > 0) {
        let temp: any = [];
        path?.forEach((item: any) => {
            temp.push(imageFormat(item.picUrl));
        });
        return temp;
    } else {
        return [];
    }
}

// 格式化金额
export function priceFormat(price: number | string, currencyFormat = true) {
    if (!price) {
        return "";
    }
    const commonStore = useCommonStore();
    const currency = commonStore.dollarSign ? "<span class='util'>" + commonStore.dollarSign + "</span>" : "<span class='util'>¥</span>";
    const num = typeof price === "string" ? parseFloat(price) : price;
    if (currencyFormat) {
        return currency + "<span class=decimal>" + num.toFixed(2) + "</span>";
    } else {
        return num.toFixed(2);
    }
}

// 链接格式化
export function urlFormat(params: string | undefined | { path: string; [key: string]: any }): string {
    if (params === undefined) {
        return "";
    } else if (typeof params === "string") {
        return params;
    } else {
        switch (params.path) {
            case "product":
                if (params.skuId) {
                    return "/item/" + params.sn + "?skuId=" + params.skuId;
                }
                return "/item/" + params.sn + "";
            case "exchange":
                return "/exchange/" + params.id + "";
            case "list":
                return "/list/?cat=" + params.id;
            case "article":
                // 咨询页面
                return "/article/news/list?articleId=" + params.id;
            case "brand":
                // 品牌搜索
                return "/list?brand=" + params.id;
            case "coupon":
                // 优惠券详情
                return "/coupon/list/?id=" + params.id;
            case "shop":
                // 店铺页面
                return "/shop/" + params.id + "";
            case "store":
                // 咨询页面
                return "/store/?id=" + params.id;
            case "base":
                // 基础页面
                return "/" + params.link;
            case "order":
                return `/member/order/info?id=${params.id}`;
            case "custom":
                // 自定义页面
                return isUrl(params.link) ? params.link : `${params.link}`;
            default:
                return params.path;
        }
    }
}
