package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import com.tigshop.bean.vo.setting.config.ShippingSettingsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;
import static com.tigshop.bean.enums.setting.SettingsEnum.ADDRESS;

/**
 * 物流设置新增参数
 *
 * @author kidd
 * @since 2025/4/1 17:39
 */
@Schema(description = "物流设置新增参数")
@Data
public class ShippingSaveParam implements ConfigSaveParam {

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

    public void noUpdate(ShippingSettingsVO settings) {
        if (Objects.equals(this.kdniaoApiKey, settings.getKdniaoApiKey())) {
            this.kdniaoApiKey = null;
        }
        if (Objects.equals(this.kdniaoBusinessId, settings.getKdniaoBusinessId())) {
            this.kdniaoBusinessId = null;
        }
    }

}
