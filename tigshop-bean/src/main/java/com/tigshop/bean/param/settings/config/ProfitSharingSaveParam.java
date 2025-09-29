package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 */
@Schema(description = "结算与分账参数")
@Data
public class ProfitSharingSaveParam implements ConfigSaveParam {

    @ConfigItemField(SettingsEnum.BILLING_NODE)
    @Schema(description = "出账节点，1-下单成功，2-售后申请")
    private Integer billingNode;

    @ConfigItemField(SettingsEnum.COLLECTION_NODE)
    @Schema(description = "入账节点，1-订单完成，2-确认收货")
    private Integer collectionNode;

    @ConfigItemField(SettingsEnum.COLLECTION_TIME_SETTING)
    @Schema(description = "入账时间（单位：天）")
    private Integer collectionTimeSetting;

    @ConfigItemField(SettingsEnum.COLLECTION_METHOD)
    @Schema(description = "入账方式，1-自动入账，2-手动入账")
    private Integer collectionMethod;

    @ConfigItemField(SettingsEnum.COLLECTION_ACCOUNT_TYPE)
    @Schema(description = "入账账户类型，1-余额")
    private Integer collectionAccountType;

    @ConfigItemField(SettingsEnum.SPLIT_PAYMENT_METHOD)
    @Schema(description = "分账方式，1-线下分账，2-线上分账")
    private Integer splitPaymentMethod;

    @ConfigItemField(SettingsEnum.STORE_GENERAL_SERVICE_FEE_RATE)
    @Schema(description = "店铺通用平台服务费率（%）")
    private BigDecimal storeGeneralServiceFeeRate;

    @ConfigItemField(SettingsEnum.STORE_WITHDRAWAL_FEE_RATE)
    @Schema(description = "店铺提现手续费率（%）")
    private BigDecimal storeWithdrawalFeeRate;

    @ConfigItemField(SettingsEnum.STOREFRONT_GENERAL_SERVICE_FEE_RATE)
    @Schema(description = "门店通用平台服务费率（%）")
    private BigDecimal storefrontGeneralServiceFeeRate;

    @ConfigItemField(SettingsEnum.STOREFRONT_WITHDRAWAL_FEE_RATE)
    @Schema(description = "门店提现手续费率（%）")
    private BigDecimal storefrontWithdrawalFeeRate;

    @ConfigItemField(SettingsEnum.SUPPLIER_GENERAL_SERVICE_FEE_RATE)
    @Schema(description = "供应商通用平台服务费率（%）")
    private BigDecimal supplierGeneralServiceFeeRate;

    @ConfigItemField(SettingsEnum.SUPPLIER_WITHDRAWAL_FEE_RATE)
    @Schema(description = "供应商提现手续费率（%）")
    private BigDecimal supplierWithdrawalFeeRate;
}
