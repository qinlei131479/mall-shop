export interface ProductItem {
    productId: number;
    productName: string;
    description: string;
    productSn?: string;
    productTsn?: string;
    productStock: number;
    productPrice?: string;
    marketPrice?: string;
    shippingTplId?: number;
    productStatus?: number;
    productType?: number;
    categoryId?: number;
    brandId?: number;
    shopId?: number;
    keywords: string;
    storeCategoryId?: number;
    checkStatus?: number;
    checkReason?: string;
    clickCount?: number;
    productWeight?: string;
    isPromote?: number;
    isPromoteActivity?: number;
    promotePrice?: string;
    promoteStartDate?: number;
    promoteEndDate?: number;
    seckillMaxNum?: number;
    productBrief?: string;
    productDesc?: string;
    picUrl?: string;
    picThumb?: string;
    picOriginal?: string;
    commentTag?: string;
    freeShipping?: number;
    integral?: number;
    addTime?: number;
    sortOrder?: number;
    storeSortOrder?: number;
    isDelete?: number;
    isBest?: number;
    isNew?: number;
    isHot?: number;
    lastUpdate?: number;
    remark?: string;
    giveIntegral?: number;
    rankIntegral?: number;
    suppliersId?: number;
    virtualSales?: number;
    limitNumber?: number;
    productCare?: string;
    productRelated?: any;
    productServiceIds?: any;
    isSupportReturn?: number;
    isSupportCod?: number;
    productVideo?: string;
    prepayPrice?: string;
    number?: number;
    discountsPrice?: string;
    exchangeIntegral?: number;
    isSeckill?: number;
    shopId?: number;
    productSku?: SkuList[];
    shop?: any;
    isBuy?: number;
    paidContent?: DescArr[];
    virtualSample?: string;
    videoList?: VideoList[];
}

export interface VideoList {
    id: number;
    videoId: number;
    productId: number;
    videoUrl: string;
    videoCover: string;
    format: string;
}

export interface ProductFilterParams {
    page?: number;
    size?: number;
}
export interface ProductFilterResult {
    item: ProductItem;
    descArr: DescArr[];
    skuList: SkuList[];
    picList: PicList[];
    serviceList: ServiceList[];
    attrList: AttrList;
    rankDetail: RankDetail;
    checkedValue: string[];
    consultationTotal: number;
}
export interface RankDetail {
    total?: number;
    averageRank?: number;
    goodPercent?: number;
}
export interface AttrList {
    normal?: Normal[];
    spe?: Spe[];
    extra?: Extra[];
}
export interface AttrList {
    normal?: Normal[];
    spe?: Spe[];
    extra?: Extra[];
}
export interface Normal {
    attrName: string;
    attrList: AttrList[];
}

export interface GuessLikeListResult {
    productList: ProductItem[];
}
export interface Spe {
    attrName: number;
    attrList: AttrList[];
}

export interface Extra {
    attrName: string;
    attrList: AttrList[];
}

export interface AttrList {
    attributesId: number;
    productId: number;
    attrType: number;
    attrName: string;
    attrValue: string;
    attrPrice: string;
    attrColor: string;
    attrPic: string;
    attrPicThumb: string;
}

export interface SkuList {
    skuId: number;
    productId: number;
    skuValue: string;
    skuData: SkuData[];
    skuSn: string;
    skuStock: number;
    skuTsn: string;
    skuPrice: string;
    price?: string;
    stock?: number;
    title?: string;
    num?: number;
}

export interface SkuData {
    name: string;
    value: string;
}

export interface PicList {
    picId?: number;
    productId?: number;
    picUrl?: string;
    picDesc?: string;
    picThumb?: string;
    picLarge?: string;
    sortOrder?: number;
    videoUrl?: string;
    videoCover?: string;
}
export interface DescArr {
    type?: string;
    pic?: string;
    html?: string;
}

export interface SkuDetail {
    price: string;
    originPrice: string | null;
    stock: number;
    promotion: SkuPromotion[];
    errcode: number;
    message: string;
}

export interface SkuPromotion {
    promotionId: number;
    promotionName: string;
    startTime: number;
    endTime: number;
    type: number;
    shopId: number;
    relationId: number;
    range: number;
    rangeData: number[];
    skuIds: number[] | null;
    data: anyObject;
}

export interface CommentDetail {
    total?: number;
    goodCount?: number;
    moderateCount?: number;
    badCount?: number;
    showCount?: number;
    goodPercent?: number;
    moderatePercent?: number;
    badPercent?: number;
}

export interface CommentFilterResult {
    records: CommentItem[];
    total: number;
}

export interface CommentItem {
    commentId?: number;
    userId?: number;
    username?: string;
    nickname?: string;
    avatar?: string;
    productId?: number;
    orderId?: number;
    orderItemId?: number;
    commentRank?: number;
    content?: string;
    addTime?: string;
    status?: number;
    parentId?: number;
    usefull?: number;
    useless?: number;
    commentTag?: any;
    showPics?: any;
    isRecommend?: number;
    isTop?: number;
    isShowed?: number;
    isDefault?: number;
    sortOrder?: number;
    shopId?: number;
}

//商品优惠券
export interface ProductCouponItem {
    couponId?: number;
    couponName?: string;
    couponMoney?: string;
    couponDiscount?: number;
    couponDesc?: string;
    couponType?: number;
    sendRange?: number;
    sendRangeData?: any[];
    minOrderAmount?: string;
    sendStartDate?: string;
    sendEndDate?: string;
    sendType?: number;
    useDay?: number;
    useStartDate?: string;
    useEndDate?: string;
    isShow?: number;
    isGlobal?: number;
    isNewUser?: number;
    enabledClickGet?: number;
    limitUserRank?: number[];
    shopId?: number;
    isReceive?: number;
    limitNum: number;
    receiveNum: number;
    delayDay: number;
}
//商品咨询
export interface ProductConsultationItem {
    addTime?: string;
    username?: string;
    nickname?: string;
    content?: string;
    reply?: any;
}
export interface ProductConsultationList {
    records: ProductConsultationItem[];
    total: number;
}

export interface ProductConsultationFormState {
    productId?: number;
    content?: string;
    email?: string;
    type: number;
}

export interface ServiceList {
    productServiceId: number;
    productServiceName: string;
    icoImg: string;
}

export interface BrandInfo {
    brandId: number;
    brandName: string;
    brandLogo: string;
    siteUrl: string;
    firstWord: string;
}

interface PromotionFilterParams {
    products: Products[];
    shopId?: number;
    from?: string;
}
interface Products {
    productId: number;
    skuId?: number;
}

export interface PromotionList {
    shopId: number;
    productId: number;
    activityInfo: AnyObject;
}

export interface PromotionData {
    promotionDesc: string;
    couponId: number;
    couponName: string;
    couponMoney: string;
    couponDiscount: string;
    couponDesc: string;
    couponType: number;
    sendRange: number;
    sendRangeData: any[];
    minOrderAmount: string;
    sendStartDate: string;
    sendEndDate: string;
    sendType: number;
    useDay: number;
    useStartDate: string;
    useEndDate: string;
    isShow: number;
    isGlobal: number;
    isNewUser: number;
    enabledClickGet: number;
    limitUserRank: any[];
    shopId: number;
    isDelete: number;
    limitNum: number;
    delayDay: number;
    sendNum: number;
    maxOrderAmount: string;
    couponUnit: number;
    reduceType: number;
}

export interface ExchangeDetail {
    id?: number;
    pointsDeductedAmount?: string;
    isEnabled?: number;
    isHot?: number;
    exchangeIntegral?: number;
    discountsPrice?: string;
    productStock?: number;
}

export interface ProductAmountItem {
    count: number;
    total: string;
}

export interface SkuAvailabilitysResponse {
    data: { [key: string]: SkuAvailabilitys };
    code: number;
    message: string;
}

export interface SkuAvailabilitys {
    originPrice: null;
    price: string;
    stock: number;
    promotion: any[];
}
