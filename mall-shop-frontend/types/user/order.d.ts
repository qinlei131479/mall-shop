import type { BaseResponseListWrap } from "~/types/api";
// 列表查询时筛选参数类型
export interface OrderFilterParams {
    page: number;
    size?: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    payStatus?: number;
    orderStatus?: number;
    addTime?: string[];
    shippingStatus?: number;
    commentStatus?: number;
    dateType?: number;
}

// 获取列表返回参数类型
export interface OrderFilterState {
    showPopover: boolean;
    orderId: number;
    addTime: string;
    balance: string;
    availableActions: AvailableActionsInfo;
    shippingFee: string;
    orderStatusName: string;
    trackingNo: string;
    logisticsName: string;
    payStatusName: string;
    totalAmount?: string;
    unpaidAmount?: string;
    orderStatus?: number;
    shippingMethod?: number;
    payTypeId?: number;
    orderSn?: string;
    isShow?: boolean;
    items?: ProductItems[];
    invoiceData?: InvoiceData;
}
export interface InvoiceData {
    status: number;
}

export interface ProductItems {
    productPicThumb: string;
    picThumb: string;
    aftersalesItem: object;
    productName: string;
    skuData: string;
    productSn: string;
    productId: number;
    quantity: number;
    itemId: number;
    price: string;
    subtotal: string;
    isGift: number;
}

export interface OrderFilterResult extends BaseResponseListWrap<OrderFilterState[]> {
    filter: OrderFilterParams;
}

// 获取详情返回参数类型
export interface OrderFormState {
    orderId?: number;
    orderStatus?: number;
    shopId?: number;
    payTypeId?: number;
    orderSn?: string;
    addTime?: string;
    shopTitle?: string;
    consignee?: string;
    mobile?: string;
    payStatus?: number;
    orderStatusName?: string;
    detailedAddress?: string;
    items?: ProductItems[];
    userName?: string;
    address?: OrderAddress;
    shippingTypeId?: number;
    shippingTypeName?: string;
    logisticsId?: number;
    logisticsName?: string;
    payType?: number;
    isStoreSplited?: number;
    payTime?: string;
    productAmount?: number;
    totalAmount?: number;
    couponAmount?: number;
    pointsAmount?: number;
    invoiceFee?: number;
    serviceFee?: number;
    shippingFee?: number;
    paidAmount?: number;
    balance?: number;
    discountAmount?: number;
    updateOrderSn?: boolean;
    unpaidAmount?: number;
    totalWeight?: number;
    buyerNote?: string;
    adminNote?: string;
    remark?: string;
    availableActions: AvailableActionsInfo;
    shippingTime?: string;
    trackingNo?: string;
    shipping?: number;
    shippingMethod?: number;
    shippingList?: ShippingListType[];
    stepStatus?: StepStatus;
    shop: any;
    autoDeliveryDays?: number | null;
}

export interface StepStatus {
    current: number;
    status: any;
    steps: {
        title?: string;
        description?: string;
    }[];
}

export interface OrderFormResult {
    item: OrderFormState;
}

export interface AvailableActionsInfo {
    cancelOrder?: boolean;
    toPay?: boolean;
    toAftersales?: boolean;
    setConfirm?: boolean;
    confirmReceipt?: boolean;
    modifyOrder?: boolean;
    modifyOrderMoney?: boolean;
    modifyShippingInfo?: boolean;
    setPaid?: boolean;
    setUnpaid?: boolean;
    deliver?: boolean;
    splitOrder?: boolean;
    delOrder?: boolean;
    rebuy?: boolean;
    toComment?: boolean;
}

export interface OrderNumResult {
    item: OrderNumState;
}

export interface OrderNumState {
    awaitPay: number;
    awaitShipping: number;
    awaitComment: number;
    orderCompleted: number;
}

export interface ShippingInfoResponse {
    shipperCode: string;
    logisticCode: string;
    state: string;
    stateEx: string;
    location: string;
    traces: ShippingInfoTrace[];
    success: string;
    ebusinessID: string;
}

export interface ShippingInfoTrace {
    action: string;
    acceptTime: string;
    acceptStation: string;
    location: string;
}
