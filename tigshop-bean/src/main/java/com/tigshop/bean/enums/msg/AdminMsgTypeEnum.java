package com.tigshop.bean.enums.msg;

import lombok.Getter;

/**
 * 管理员消息分类
 *
 * @author tigshop
 * @create 2024-12-25
 */
@Getter
public enum AdminMsgTypeEnum {
    TRANSACTION(1, "交易消息", 0, true, true),
    PRODUCT(2, "商品消息", 0, true, true),
    AFTER_SALE(3, "售后服务", 0, true, true),
    SHOP_SERVICE(4, "店铺服务", 0, true, false),
    OTHER(5, "其它消息", 0, true, true),

    NEW_ORDER(11, "新订单", 1, true, true),
    PAID_ORDER(12, "已付款订单", 1, true, true),
    ORDER_COMPLETED(13, "订单完成", 1, true, true),
    STOCK_WARNING(21, "商品库存预警", 2, true, true),
    OUT_OF_STOCK(22, "商品无货", 2, true, true),
    PRODUCT_OUT_OF_STOCK(23, "商品下架", 2, true, true),
    PRODUCT_AUDIT_NOTIFICATION(24, "商品审核通知", 2, true, true),
    ORDER_CANCELED(31, "订单取消", 3, true, true),
    AFTER_SALE_REQUEST(32, "售后申请", 3, true, true),
    WITHDRAWAL_REQUEST(33, "提现申请", 3, true, true),
    INVOICE_QUALIFICATION_AUDIT(34, "发票资质审核", 3, true, true),
    INVOICE_REQUEST(35, "发票申请", 3, true, true),
    SHOP_ENTRY_APPLICATION(41, "店铺入驻申请", 4, true, false),
    SHOP_QUALIFICATION_MODIFICATION(42, "店铺资质修改", 4, true, false),
    SHOP_VIOLATION(43, "店铺违规", 4, true, false),
    SYSTEM_MESSAGE(51, "系统消息", 5, true, true),
    PENDING_TASK(52, "待办任务", 5, true, true),
    FEEDBACK(53, "意见反馈", 5, true, true),
    QUICK_DELIVERY(54, "发货提醒", 5, false, true);

    private final int catId;
    private final String catName;
    private final int parentId;
    private final boolean shopShow;
    private final boolean vendorShow;

    AdminMsgTypeEnum(int catId, String catName, int parentId, boolean shopShow, boolean vendorShow) {
        this.catId = catId;
        this.catName = catName;
        this.parentId = parentId;
        this.shopShow = shopShow;
        this.vendorShow = vendorShow;
    }

}
