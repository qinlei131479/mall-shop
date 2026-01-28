// 获取列表返回参数类型
export interface SeckillFilterState {
    productId: number;
    seckillLimitNum: number;
    seckillStock: number;
    seckillSales: number;
    productName: string;
    picThumb: string;
    productSn: string;
    skuSn: string;
    seckillPrice: string;
}

export interface SeckillFilterResult {
    records: SeckillFilterState[];
    total: number;
}
