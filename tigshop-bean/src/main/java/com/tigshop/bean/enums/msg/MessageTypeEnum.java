// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.enums.msg;


import lombok.Getter;

/**
 * @author Tigshop团队
 * @create 2025/5/26 10:31
 * 对应模板消息模块
 */
@Getter
public enum MessageTypeEnum {
    NEW_ORDER(1, "会员下单"),
    ORDER_PAY(2, "支付成功"),
    ORDER_SHIPPING(3, "订单发货"),
    ORDER_REFUND(4, "订单退款"),
    NEW_ORDER_SHOP(5, "下单给商家发送信息"),
    ORDER_PAY_SHOP(6, "支付订单给商家发送信息"),
    ORDER_INVOICE(7, "发票开具成功"),
    USER_COMPANY_AUTH(9, "会员实名认证通知"),
    ;

    private final int id;
    private final String name;

    MessageTypeEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
