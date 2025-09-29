package com.tigshop.common.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * Tigshop 版本
 *
 * @author kidd
 * @since 2025/7/3 11:13
 */
@ConfigurationProperties(prefix = "tigshop")
@AllArgsConstructor
@Getter
public class TigshopProperties {

    /** 系统名称 */
    private final String name;

    /** 系统版本名 */
    private final String version;

    /** 系统版本号 */
    private final String versionNum;

    /** 是否商户端（1 是，0 否） */
    private final Integer isMerchant;

    /** 是否启用 B2B 模式（1 是，0 否） */
    private final Integer isB2b;

    /** 是否为 Pro 版本（1 是，0 否） */
    private final Integer isPro;

    /** 是否海外版（1 是，0 否） */
    private final Integer isOverseas;

    /** 是否供应商端（1 是，0 否） */
    private final Integer isVendor;

    /** 是否多门店（1 是，0 否） */
    private final Integer isO2o;
}
