package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop项目组
 * @create 2025年09月23日 14:51
 */
@Schema(description = "个性化设置保存参数")
@Data
public class PersonalizedSettingSaveParam implements ConfigSaveParam {
    @ConfigItemField(SettingsEnum.GUEST_LIKE_GOODS_NAME)
    @Schema(description = "猜你喜欢定义名称")
    private String guestLikeGoodsName;

    @ConfigItemField(SettingsEnum.IS_GUEST_LIKE_GOODS)
    @Schema(description = "是否开启猜你喜欢，0-不开启，1-开启")
    private Integer isGuestLikeGoods;
}