/**/ // 列表查询时筛选参数类型
export interface CartFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    keyword?: string;
    payStatus?: number;
    orderStatus?: number;
    addTime?: string[];
    shippingStatus?: number;
    commentStatus?: number;
}

// 获取列表返回参数类型
export interface CartFilterState {
    shopId: number;
    shopTitle: string;
    isChecked: boolean;
    carts: CartItmes[];
    gift: any[];
}

export interface CartItmes {
    activityInfo: any;
    cartId: number;
    userId?: number;
    productId?: number;
    productSn?: string;
    productName?: string;
    picThumb?: string;
    marketPrice?: string;
    originalPrice?: string;
    quantity?: number;
    skuId?: number;
    skuData?: SkuData[];
    productType?: number;
    isChecked?: boolean;
    shopId?: number;
    type?: number;
    shopTitle?: string;
    productWeight?: string;
    shippingTplId?: number;
    freeShipping?: number;
    productStatus?: number;
    price?: string;
    stock?: number;
    isDisabled?: boolean;
    subtotal?: number;
    extraSkuData: any[];
}

export interface SkuData {
    name: string;
    value: string;
}

export interface CartFilterResult {
    cartList: CartFilterState[];
    total: TotalObj;
}

export interface TotalObj {
    productAmount: number;
    checkedCount: number;
    discounts: number;
    discountAfter: number;
    totalCount: number;
    discountCouponAmount: number;
    discountSeckillAmount: number;
    discountProductPromotionAmount: number;
    discountTimeDiscountAmount: number;
    discountDiscountAmount: number;
    serviceFee?: number;
}

// 获取详情返回参数类型
export interface CartFormState {
    orderId?: number;
    orderStatus?: number;
    shopId?: number;
    payTypeId?: number;
    orderSn?: string;
    addTime?: string;
}

export interface CartFormResult {
    item: CartFormState;
}

// 编辑表单
