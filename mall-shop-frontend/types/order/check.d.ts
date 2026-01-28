export interface CheckResponse {
    errcode: number;
    message: string;
    data: CheckItem;
}

export interface CheckItem {
    cartList: CheckCartList[];
    total: CheckTotal;
    couponList: CheckCouponList;
    item: CheckItemItem;
    useCouponIds: null | number[];
    selectUserCouponIds: null | number[];
    tmplIds: string[];
    flowType: number;
    availablePoints: number;
    message: null;
}

export interface CheckCouponList {
    enableCoupons: EnableCoupon[];
    disableCoupons: any[];
}

export interface EnableCoupon {
    id: number;
    couponName: string;
    couponType: number;
    minOrderAmount: string;
    couponDesc: string;
    couponMoney: string;
    isGlobal: number;
    couponDiscount: string;
    shopId: number;
    endDate: string;
    couponId: number;
    userCouponId: number;
    selected?: boolean;
}

export interface CheckCartList {
    total: CartListTotal;
    carts: Cart[];
    usedPromotions: any[];
    enableUsePromotion: any[];
    gift: any[];
    shopId: number;
    shopTitle: string;
    noShipping: number;
}

export interface Cart {
    cartId: number;
    userId: number;
    productId: number;
    productSn: string;
    picThumb: string;
    marketPrice: null;
    originalPrice: null;
    quantity: number;
    skuId: number;
    skuData: any[];
    productType: number;
    isChecked: boolean;
    shopId: number;
    type: number;
    updateTime: number;
    salesmanId: number;
    extraSkuData: null | any[];
    productWeight: null;
    shippingTplId: null;
    freeShipping: number;
    productStatus: number;
    productName: string;
    productPrice: number;
    categoryId: number;
    brandId: number;
    productStock: number;
    cardGroupId: number;
    virtualSample: string;
    suppliersId: number;
    shop: Shop;
    sku: null;
    price: number;
    stock: number;
    hasSku: boolean;
    subtotal: number;
    originPrice: number;
    isDisabled: boolean;
    activityInfo: null;
    serviceFee: null;
    isSeckill: null;
}

export interface Shop {
    description: string;
    kefuInlet: null | string;
    kefuLink: string;
    kefuPhone: string;
    kefuWeixin: string;
    merchantId: number;
    shopId: number;
    shopLogo: string;
    shopTitle: string;
    status: number;
}

export interface CartListTotal {
    couponIds: any[];
    discountCouponAmount: number;
    discountProductPromotionAmount: number;
    discountSeckillAmount: number;
    discountTimeDiscountAmount: number;
    discountDiscountAmount: number;
    discounts: number;
}

export interface CheckItemItem {
    addressId: number;
    shippingType: ShippingType[];
    payTypeId: number;
    usePoint: number;
    useBalance: number;
    useCouponIds: null;
    selectUserCouponIds: null;
    flowType: number;
}

export interface ShippingType {
    typeId: null;
    shopId: null;
    typeName: null;
}

export interface CheckTotal {
    productAmount: number;
    checkedCount: number;
    discounts: number;
    discountAfter: null;
    totalCount: number;
    discountCouponAmount: number;
    discountDiscountAmount: number;
    discountSeckillAmount: number;
    discountProductPromotionAmount: number;
    discountTimeDiscountAmount: number;
    serviceFee: number;
    paidAmount: number;
    couponAmount: number;
    discountAmount: number;
    exchangePoints: number;
    pointsAmount: number;
    shippingFee: number;
    storeShippingFee: StoreShippingFee;
    totalAmount: number;
    orderSendPoint: number;
    balance: number;
    unpaidAmount: number;
}

export interface StoreShippingFee {
    [key: string]: number;
}

export interface PaymentTypeResponse {
    errcode: number;
    message: string;
    item: PaymentTypeItem[];
}

export interface PaymentTypeItem {
    typeId: number;
    typeName: string;
    disabled: boolean;
    disabledDesc: string;
    isShow: boolean;
}

export interface ShippingTypeResponse {
    errcode: number;
    message: string;
    item: { [key: string]: ShippingTypeItem[] };
}

export interface ShippingTypeItem {
    shippingTypeId: number;
    shippingTypeName: string;
    shippingDefaultId: number;
    shippingTypeDesc: string;
    shippingTimeDesc: string;
    isSupportCod: number;
    sortOrder: number;
    shopId: number;
}
