package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;
import static com.tigshop.bean.enums.setting.SettingsEnum.ALIPAY_RSA_PUBLIC_KEY;

/**
 * 系统设置-支付宝支付设置
 *
 * @author kidd
 * @since 2025/6/7 11:09
 */
@Data
public class AliPaySettingsVO {

    @ConfigItemField(USE_ALIPAY)
    @Schema(description = "是否启用支付宝支付；0-关闭，1-开启")
    private Integer useAlipay;

    @ConfigItemField(ALIPAY_APPID)
    @Schema(description = "支付宝APPID")
    private String alipayAppid;

    @ConfigItemField(ALIPAY_MOBILE_APPID)
    @Schema(description = "支付宝移动端APPID")
    private String alipayMobileAppid;

    @ConfigItemField(ALIPAY_RSA_PRIVATE_KEY)
    @Schema(description = "应用私钥")
    private String alipayRsaPrivateKey;

    @ConfigItemField(ALIPAY_RSA_PUBLIC_KEY)
    @Schema(description = "支付宝公钥")
    private String alipayRsaPublicKey;

    public void encryptData() {
        this.alipayAppid = StringUtils.maskMiddleHalf(this.alipayAppid);
        this.alipayMobileAppid = StringUtils.maskMiddleHalf(this.alipayMobileAppid);
        this.alipayRsaPrivateKey = StringUtils.maskMiddleHalf(this.alipayRsaPrivateKey);
        this.alipayRsaPublicKey = StringUtils.maskMiddleHalf(this.alipayRsaPublicKey);
    }
}
