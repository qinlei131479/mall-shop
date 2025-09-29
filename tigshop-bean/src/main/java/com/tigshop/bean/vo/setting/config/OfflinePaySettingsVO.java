package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 系统设置-线下支付设置
 *
 * @author kidd
 * @since 2025/6/7 11:46
 */
@Data
public class OfflinePaySettingsVO {

    @ConfigItemField(USE_OFFLINE)
    @Schema(description = "是否启用线下支付；0-关闭，1-开启")
    private Integer useOffline;

    @ConfigItemField(OFFLINE_PAY_BANK)
    @Schema(description = "银行汇款")
    private String offlinePayBank;

    @ConfigItemField(OFFLINE_PAY_COMPANY)
    @Schema(description = "企业汇款")
    private String offlinePayCompany;

}
