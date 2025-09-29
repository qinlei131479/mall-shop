package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 物流配置
 *
 * @author kidd
 * @since 2025/4/3 15:20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingSettingsVO {

    // *** 快递发货设置 ***

    @ConfigItemField(SettingsEnum.DEFAULT_LOGISTICS_NAME)
    @Schema(description = "默认配送名称")
    private String defaultLogisticsName;

    // *** 快递接口设置 ***

    @ConfigItemField(KDNIAO_API_KEY)
    @Schema(description = "快递鸟API Key")
    private String kdniaoApiKey;

    @ConfigItemField(KDNIAO_BUSINESS_ID)
    @Schema(description = "快递鸟服务ID")
    private String kdniaoBusinessId;

    // *** 发货信息设置 ***

    @ConfigItemField(SENDER)
    @Schema(description = "发货人")
    private String sender;

    @ConfigItemField(MOBILE)
    @Schema(description = "发货人联系方式")
    private String mobile;

    @ConfigItemField(PROVINCE_NAME)
    @Schema(description = "发货地省份")
    private String provinceName;

    @ConfigItemField(CITY_NAME)
    @Schema(description = "发货城市")
    private String cityName;

    @ConfigItemField(AREA_NAME)
    @Schema(description = "发货地区")
    private String areaName;

    @ConfigItemField(ADDRESS)
    @Schema(description = "发货详细地址")
    private String address;

    public void encryptData() {
        this.kdniaoApiKey = StringUtils.maskMiddleHalf(this.kdniaoApiKey);
        this.kdniaoBusinessId = StringUtils.maskMiddleHalf(this.kdniaoBusinessId);
    }
}
