package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 系统设置-订单设置
 *
 * @author kidd
 * @since 2025/6/6 13:35
 */
@Data
public class OrderSettingsVO {

    @ConfigItemField(SettingsEnum.AUTO_DELIVERY_DAYS)
    @Schema(description = "自动收货天数")
    private BigDecimal autoDeliveryDays;

    @ConfigItemField(SettingsEnum.AUTO_RETURN_GOODS)
    @Schema(description = "自动同意退货选项; 1-开启，0-关闭")
    private Integer autoReturnGoods;

    @ConfigItemField(SettingsEnum.AUTO_RETURN_GOODS_DAYS)
    @Schema(description = "自动同意退天数")
    private BigDecimal autoReturnGoodsDays;

    @ConfigItemField(SettingsEnum.AFTER_SALES_LIMIT_DAYS)
    @Schema(description = "售后限制天数")
    private BigDecimal afterSalesLimitDays;

    @ConfigItemField(SettingsEnum.AUTO_CANCEL_ORDER_MINUTE)
    @Schema(description = "未付款订单超时时间")
    private Integer autoCancelOrderMinute;

    @ConfigItemField(SettingsEnum.IS_PLATFORM_CANCEL_PAID_ORDER)
    @Schema(description = "平台已支付订单可取消订单")
    private Integer isPlatformCancelPaidOrder;

    @ConfigItemField(SettingsEnum.IS_PLATFORM_CANCEL_DELIVER_ORDER)
    @Schema(description = "平台已发货订单可取消发货")
    private Integer isPlatformCancelDeliverOrder;

    @ConfigItemField(SettingsEnum.IS_SHOP_CANCEL_DELIVER_ORDER)
    @Schema(description = "店铺已发货订单可取消发货")
    private Integer isShopCancelDeliverOrder;

}
