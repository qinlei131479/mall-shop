export interface LangListResponse {
    data: LangListItem[];
    errcode: number;
    message: string;
}

export interface LangListItem {
    id: number;
    localeCode: string;
    language: string;
    flagPicture: string;
    lastUpdated: string;
    isEnabled: number;
    isDefault: number;
}

export interface DefaultLangResponse {
    item: DefaultLangItem;
    errcode: number;
    message: string;
}
export interface DefaultLangItem {
    currencyId: number;
    flagPicture: string;
    id: number;
    isDefault: number;
    isEnabled: number;
    language: string;
    lastUpdated: string;
    localeCode: string;
    sort: number;
}

export interface CurrencyResponse {
    item: CurrencyItem[];
    errcode: number;
    message: string;
}

export interface CurrencyItem {
    id: number;
    isDefault: number;
    name: string;
    rate: string;
    symbol: string;
}

export interface MobileAreaCodeResponse {
    list: MobileAreaCodeList[];
    errcode: number;
    message: string;
}

export interface MobileAreaCodeList {
    code: string;
    id?: number;
    isAvailable?: number;
    isDefault?: number;
    label?: string;
    name?: string;
}

export interface ProductsListResponse {
    records: ProductsListResult[];
    filter: Filter;
    total: number;
    waitingCheckedCount: number;
    errcode: number;
    message: string;
}

export interface Filter {
    isShow: number;
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    productId: number;
    isDelete: number;
    categoryId: number;
    brandId: number;
    ids: null;
    shopId: number;
    introType: string;
    couponId: number;
    shopCategoryId: number;
    withCartSum: number;
    productStatus: number;
}

export interface ProductsListResult {
    categoryId: number;
    brandId: number;
    productTsn: string;
    marketPrice: string;
    shippingTplId: number;
    freeShipping: number;
    productId: number;
    picUrl: string;
    picThumb: string;
    productName: string;
    checkStatus: number;
    checkReason: string;
    shopId: number;
    suppliersId: number;
    productType: number;
    productSn: string;
    productPrice: string;
    productStatus: number;
    isBest: number;
    isNew: number;
    isHot: number;
    productStock: number;
    sortOrder: number;
    productSku: ProductSku[];
    shop: ShopSimple | null;
}

export interface ProductSku {
    skuId: number;
    productId: number;
    skuValue: string;
    skuData: SkuDatum[];
    skuSn: string;
    skuStock: number;
    skuTsn: string;
    skuPrice: string;
}

export interface SkuDatum {
    name: string;
    value: string;
}

export interface ShopSimple {
    statusText: string;
    shopId: number;
    shopTitle: string;
}

export interface BaseConfigResponse {
    shopName: string;
    shopTitle: string;
    shopTitleSuffix: string;
    shopLogo: string;
    shopKeywords: string;
    shopDesc: string;
    storageUrl: string;
    dollarSign: string;
    dollarSignCn: string;
    icoImg: string;
    autoRedirect: number;
    openWechatOauth: number;
    personApplyEnabled: number;
    h5Domain: string;
    pcDomain: string;
    adminDomain: string;
    showService: number;
    versionType: string;
    shopIcpNo: string;
    shopIcpNoUrl: string;
    shop110No: string;
    shop110Link: string;
    shopCompanyTxt: string;
    companyAddress: string;
    kefuPhone: string;
    kefuTime: string;
    kefuAddress: string;
    isEnterprise: number;
    deCopyright: number;
    poweredByStatus: number;
    poweredBy: number;
    categoryDecorateType: number;
    canInvoice: number;
    invoiceAdded: number;
    defaultShopName: string;
    isOpenMobileAreaCode: number;
    showSelledCount: number;
    showMarketprice: number;
    useSurplus: number;
    usePoints: number;
    useCoupon: number;
    closeOrder: number;
    shopRegClosed: number;
    companyDataType: number;
    companyDataTips: string;
    isIdentity: number;
    isEnquiry: number;
    growUpSetting: GrowUpSetting;
    decoratePageConfig: DecoratePageConfig;
    defaultHeaderStyle: number;
    errcode: number;
    message: string;
    openWechatPcLogin: number;
    wechatRegisterBindPhone: number;
    googleLoginOn: number;
    facebookLoginOn: number;
    defaultTechSupport: string;
    poweredByLogo: string;
    openEmailRegister: number;
    integralName: string;
    closeAuth: number;
    bulkPurchase: number;
}

export interface ThemeSettingsResponse {
    themeStyle: string;
}

export interface DecoratePageConfig {
    type: string;
    module: any[];
    backgroundRepeat: string;
    backgroundSize: string;
    style: number;
    title: string;
    titleColor: string;
    headerStyle: number;
    titleBackgroundColor: string;
    backgroundImage: BackgroundImage;
    backgroundColor: string;
}

export interface BackgroundImage {
    picUrl: string;
    picThumb: string;
}

export interface GrowUpSetting {
    buyOrder: number;
    buyOrderNumber: number;
    buyOrderGrowth: string;
    evpi: number;
    evpiGrowth: string;
    bindPhone: number;
    bindPhoneGrowth: string;
}

export interface ThemeStyle {
    themeId: number;
    "--general": string;
    "--main-bg": string;
    "--main-bg-gradient": string;
    "--main-text": string;
    "--vice-bg": string;
    "--vice-text": string;
    "--icon": string;
    "--price": string;
    "--tag-text": string;
    "--tag-bg": string;
    "--primary-light-3": string;
    "--primary-light-5": string;
    "--primary-light-7": string;
    "--primary-light-8": string;
    "--primary-light-9": string;
    "--primary-dark-2": string;
    "--main-btn-hover-bg": string;
    "--ump-main-bg": string;
    "--ump-main-text": string;
    "--ump-vice-bg": string;
    "--ump-vice-text": string;
    "--ump-icon": string;
    "--ump-price": string;
    "--ump-tag-text": string;
    "--ump-tag-bg": string;
    "--ump-coupon-bg": string;
    "--ump-border": string;
    "--ump-start-bg": string;
    "--ump-end-bg": string;
}
