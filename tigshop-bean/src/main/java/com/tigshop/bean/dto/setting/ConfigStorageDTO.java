// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.setting;

import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 对象储存配置
 *
 * @author Jayce
 * @create 2024年11月07日 17:08
 */
@Data
@Schema(description = "对象储存配置")
public class ConfigStorageDTO {

    @Schema(description = "存储类型")
    private Integer storageType;

    @Schema(description = "本地存储地址")
    private String storageLocalUrl;

    @Schema(description = "阿里云存储地址")
    private String storageOssUrl;

    @Schema(description = "阿里云访问密钥ID")
    private String storageOssAccessKeyId;

    @Schema(description = "阿里云访问密钥")
    private String storageOssAccessKeySecret;

    @Schema(description = "阿里云存储空间")
    private String storageOssBucket;

    @Schema(description = "阿里云存储区域")
    private String storageOssRegion;

    @Schema(description = "腾讯云存储地址")
    private String storageCosUrl;

    @Schema(description = "腾讯云访问密钥ID")
    private String storageCosSecretId;

    @Schema(description = "腾讯云访问密钥")
    private String storageCosSecretKey;

    @Schema(description = "腾讯云存储空间")
    private String storageCosBucket;

    @Schema(description = "腾讯云存储区域")
    private String storageCosRegion;

    public ConfigStorageDTO(Map<String, String> storageConfigMap) {
        this.storageType = Integer.parseInt(storageConfigMap.get(SettingsEnum.STORAGE_TYPE.getBizCode()));

        this.storageLocalUrl = storageConfigMap.get(SettingsEnum.STORAGE_LOCAL_URL.getBizCode());

        this.storageOssUrl = storageConfigMap.get(SettingsEnum.STORAGE_OSS_URL.getBizCode());
        this.storageOssAccessKeyId = storageConfigMap.get(SettingsEnum.STORAGE_OSS_ACCESS_KEY_ID.getBizCode());
        this.storageOssAccessKeySecret = storageConfigMap.get(SettingsEnum.STORAGE_OSS_ACCESS_KEY_SECRET.getBizCode());
        this.storageOssBucket = storageConfigMap.get(SettingsEnum.STORAGE_OSS_BUCKET.getBizCode());
        this.storageOssRegion = storageConfigMap.get(SettingsEnum.STORAGE_OSS_REGION.getBizCode());

        this.storageCosUrl = storageConfigMap.get(SettingsEnum.STORAGE_COS_URL.getBizCode());
        this.storageCosSecretId = storageConfigMap.get(SettingsEnum.STORAGE_COS_SECRET_ID.getBizCode());
        this.storageCosSecretKey = storageConfigMap.get(SettingsEnum.STORAGE_COS_SECRET_KEY.getBizCode());
        this.storageCosBucket = storageConfigMap.get(SettingsEnum.STORAGE_COS_BUCKET.getBizCode());
        this.storageCosRegion = storageConfigMap.get(SettingsEnum.STORAGE_COS_REGION.getBizCode());

    }
}
