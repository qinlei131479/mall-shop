package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知配置
 *
 * @author kidd
 * @since 2025/4/3 16:23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifySettingsVO {

    // *** 短信设置 ***

    @ConfigItemField(SettingsEnum.SMS_KEY_ID)
    @Schema(description = "短信帐户用户名")
    private String smsKeyId;

    @ConfigItemField(SettingsEnum.SMS_KEY_SECRET)
    @Schema(description = "短信帐户密码")
    private String smsKeySecret;

    @ConfigItemField(SettingsEnum.SMS_SIGN_NAME)
    @Schema(description = "短信签名")
    private String smsSignName;

    @ConfigItemField(SettingsEnum.SMS_SHOP_MOBILE)
    @Schema(description = "商家短信号码")
    private String smsShopMobile;

    // *** 邮箱通知 ***

    @ConfigItemField(SettingsEnum.SERVICE_EMAIL)
    @Schema(description = "客服收件地址")
    private String serviceEmail;

    @ConfigItemField(SettingsEnum.SEND_CONFIRM_EMAIL)
    @Schema(description = "提交订单发送邮件; 0-否，1-是")
    private Integer sendConfirmEmail;

    @ConfigItemField(SettingsEnum.ORDER_PAY_EMAIL)
    @Schema(description = "订单支付发送邮件; 0-否，1-是")
    private Integer orderPayEmail;

    @ConfigItemField(SettingsEnum.SEND_SERVICE_EMAIL)
    @Schema(description = "下单给客服发邮件; 0-否，1-是")
    private Integer sendServiceEmail;

    @ConfigItemField(SettingsEnum.SEND_SHIP_EMAIL)
    @Schema(description = "发货时发送邮件; 0-否，1-是")
    private Integer sendShipEmail;

    public void encryptData() {
        this.smsKeyId = StringUtils.maskMiddleHalf(this.smsKeyId);
        this.smsKeySecret = StringUtils.maskMiddleHalf(this.smsKeySecret);
    }
}
