package com.tigshop.bean.enums.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 供应商商品规格库存业务
 *
 * @author kidd
 * @since 2025/7/14 10:34
 */
@Getter
@AllArgsConstructor
public enum VendorProductSkuStockBizEnum {

    INSERT(1, "插入"),
    UPDATE(2, "更新"),
    ORDER_CREATE(3, "下单"),
    ORDER_CANCEL(4, "取消订单"),
    ;

    private final int code;
    private final String desc;
}
