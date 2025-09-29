package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Tigshop团队
 */
@Schema(description = "提现配置")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalSettingsVO {

    @ConfigItemField(SettingsEnum.WITHDRAWAL_RECEIPT_METHOD)
    @Schema(description = "线下收款方式，数组：1-银行卡，2-支付宝，3-微信")
    private List<Integer> withdrawalReceiptMethod;

    @ConfigItemField(SettingsEnum.WITHDRAWAL_ENABLED)
    @Schema(description = "是否开通提现，0-不开通，1-开通")
    private Integer withdrawalEnabled;

    @ConfigItemField(SettingsEnum.WITHDRAWAL_MIN_AMOUNT)
    @Schema(description = "提现限额-最小金额")
    private BigDecimal minAmount;

    @ConfigItemField(SettingsEnum.WITHDRAWAL_MAX_AMOUNT)
    @Schema(description = "提现限额-最大金额")
    private BigDecimal maxAmount;

    @ConfigItemField(SettingsEnum.WITHDRAWAL_FREQUENCY_UNIT)
    @Schema(description = "提现频次单位，1-每日，2-每月，3-每年")
    private Integer withdrawalFrequencyUnit;

    @ConfigItemField(SettingsEnum.WITHDRAWAL_FREQUENCY_COUNT)
    @Schema(description = "提现频次数量")
    private Integer withdrawalFrequencyCount;

    @ConfigItemField(SettingsEnum.WITHDRAWAL_REVIEW_METHOD)
    @Schema(description = "提现审核方式，1-人工审核")
    private Integer withdrawalReviewMethod;

    @ConfigItemField(SettingsEnum.WITHDRAWAL_DESCRIPTION)
    @Schema(description = "提现说明")
    private String withdrawalDescription;
}
