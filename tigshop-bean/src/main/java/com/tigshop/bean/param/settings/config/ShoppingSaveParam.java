package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 购物配置新增参数
 *
 * @author kidd
 * @since 2025/4/3 16:26
 */
@Data
public class ShoppingSaveParam implements ConfigSaveParam {

    // *** 购物全局设置 ***

    @ConfigItemField(SettingsEnum.CHILD_AREA_NEED_REGION)
    @Schema(description = "运费模板地区设置机制；0-未设置的地区皆可配送（使用默认运费设置），1-仅设置的地区可配送")
    private Integer childAreaNeedRegion;

    // *** 积分设置 ***

    @ConfigItemField(SettingsEnum.INTEGRAL_NAME)
    @Schema(description = "积分名称")
    private String integralName;

    @ConfigItemField(SettingsEnum.INTEGRAL_SCALE)
    @Schema(description = "积分换算比例")
    private String integralScale;

    @ConfigItemField(SettingsEnum.ORDER_SEND_POINT)
    @Schema(description = "下单送积分")
    private String orderSendPoint;

    @ConfigItemField(SettingsEnum.INTEGRAL_PERCENT)
    @Schema(description = "积分支付比例")
    private String integralPercent;

    @ConfigItemField(SettingsEnum.COMMENT_SEND_POINT)
    @Schema(description = "评论商品送积分")
    private String commentSendPoint;

    @ConfigItemField(SettingsEnum.SHOW_SEND_POINT)
    @Schema(description = "晒单商品送积分")
    private String showSendPoint;

    @ConfigItemField(SettingsEnum.USE_QIANDAO_POINT)
    @Schema(description = "签到赠送积分; 0-否，1-是")
    private Integer useQiandaoPoint;

    // *** 发票设置 ***

    @ConfigItemField(SettingsEnum.CAN_INVOICE)
    @Schema(description = "是否能开发票; 0-否，1-是")
    private Integer canInvoice;

    @ConfigItemField(SettingsEnum.INVOICE_ADDED)
    @Schema(description = "是否支持增值税专用发票; 0-否，1-是")
    private Integer invoiceAdded;

    // *** 退换货设置 ***

    @ConfigItemField(SettingsEnum.RETURN_CONSIGNEE)
    @Schema(description = "回寄联系人设置")
    private String returnConsignee;

    @ConfigItemField(SettingsEnum.RETURN_MOBILE)
    @Schema(description = "回寄电话设置")
    private String returnMobile;

    @ConfigItemField(SettingsEnum.RETURN_ADDRESS)
    @Schema(description = "回寄地址设置")
    private String returnAddress;

}
