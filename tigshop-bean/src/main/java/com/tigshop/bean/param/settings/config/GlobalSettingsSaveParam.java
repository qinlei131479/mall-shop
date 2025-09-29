package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import com.tigshop.bean.vo.setting.config.GlobalSettingsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

/**
 * 系统设置-全局设置新增参数
 *
 * @author kidd
 * @since 2025/6/6 09:07
 */
@Data
public class GlobalSettingsSaveParam implements ConfigSaveParam {

    // *** 后台风格 ***

    @ConfigItemField(SettingsEnum.NAV_THEME)
    @Schema(description = "外观")
    private String navTheme;

    @ConfigItemField(SettingsEnum.PRIMARY_COLOR)
    @Schema(description = "主题色")
    private String primaryColor;

    @ConfigItemField(SettingsEnum.LAYOUT)
    @Schema(description = "导航模式")
    private String layout;

    @ConfigItemField(SettingsEnum.ADMIN_LIGHT_LOGO)
    @Schema(description = "后台LOGO")
    private String adminLightLogo;

    @ConfigItemField(SettingsEnum.VERSION_INFO_HIDDEN)
    @Schema(description = "是否隐藏版本信息; 0-否，1-是")
    private Integer versionInfoHidden;

    // *** SEO 设置 ***

    @ConfigItemField(SettingsEnum.SHOP_TITLE_SUFFIX)
    @Schema(description = "SEO标题后缀")
    private String shopTitleSuffix;

    @ConfigItemField(SettingsEnum.SHOP_TITLE)
    @Schema(description = "首页SEO标题")
    private String shopTitle;

    @ConfigItemField(SettingsEnum.SHOP_KEYWORDS)
    @Schema(description = "首页SEO关键词")
    private String shopKeywords;

    @ConfigItemField(SettingsEnum.SHOP_DESC)
    @Schema(description = "首页SEO描述")
    private String shopDesc;

    @ConfigItemField(SettingsEnum.ICO_IMG)
    @Schema(description = "商城ico")
    private String icoImg;

    @ConfigItemField(SettingsEnum.DEFAULT_AVATAR)
    @Schema(description = "会员默认头像")
    private String defaultAvatar;

    // *** 域名设置 ***

    @ConfigItemField(SettingsEnum.CLIENT_DEFAULT_USE)
    @Schema(description = "前台默认跳转 0 pc ,1 h5")
    private Integer clientDefaultUse;

    @ConfigItemField(SettingsEnum.PC_DOMAIN)
    @Schema(description = "PC端域名")
    private String pcDomain;

    @ConfigItemField(SettingsEnum.H5_DOMAIN)
    @Schema(description = "H5端域名")
    private String h5Domain;

    @ConfigItemField(SettingsEnum.ADMIN_DOMAIN)
    @Schema(description = "后台域名")
    private String adminDomain;

    @ConfigItemField(SettingsEnum.AUTO_REDIRECT)
    @Schema(description = "自动跳转H5；0-关闭，1-开启")
    private Integer autoRedirect;

    // *** 上传设置 ***

    @ConfigItemField(SettingsEnum.UPLOAD_MAX_SIZE)
    @Schema(description = "视频上传大小设置，单位：MB")
    private Integer uploadMaxSize;

    // *** 搜索设置 ***

    @ConfigItemField(SettingsEnum.SEARCH_KEYWORDS)
    @Schema(description = "搜索热门关键字")
    private String searchKeywords;

    @ConfigItemField(SettingsEnum.MSG_HACK_WORD)
    @Schema(description = "敏感词屏蔽")
    private String msgHackWord;

    @ConfigItemField(SettingsEnum.IS_OPEN_PSCWS)
    @Schema(description = "搜索关键词分词；0-关闭，1-开启")
    private Integer isOpenPscws;

    //selectSearchType
    @ConfigItemField(SettingsEnum.SELECT_SEARCH_TYPE)
    @Schema(description = "搜索方式")
    private String selectSearchType;

    // *** 地区设置 ***

    @ConfigItemField(SettingsEnum.SHOP_DEFAULT_REGIONS)
    @Schema(description = "商城默认地区")
    private String shopDefaultRegions;

    @ConfigItemField(SettingsEnum.DEFAULT_COUNTRY)
    @Schema(description = "默认国家")
    private String defaultCountry;

    // *** ICO 图标库 ***

    @ConfigItemField(SettingsEnum.ICO_DEFINED_CSS)
    @Schema(description = "自定义ico图标库")
    private String icoDefinedCss;

    // *** 存储设置 ***

    @ConfigItemField(SettingsEnum.STORAGE_TYPE)
    @Schema(description = "存储类型；0-本地存储，1-阿里云 OSS 存储，2-腾讯云 COS 存储")
    private Integer storageType;

    @ConfigItemField(SettingsEnum.STORAGE_LOCAL_URL)
    @Schema(description = "本地 图片访问域名")
    private String storageLocalUrl;

    @ConfigItemField(SettingsEnum.STORAGE_OSS_URL)
    @Schema(description = "OSS 图片访问域名")
    private String storageOssUrl;

    @ConfigItemField(SettingsEnum.STORAGE_OSS_ACCESS_KEY_ID)
    @Schema(description = "OSS AccessKeyId")
    private String storageOssAccessKeyId;

    @ConfigItemField(SettingsEnum.STORAGE_OSS_ACCESS_KEY_SECRET)
    @Schema(description = "OSS AccessKeySecret")
    private String storageOssAccessKeySecret;

    @ConfigItemField(SettingsEnum.STORAGE_OSS_BUCKET)
    @Schema(description = "OSS 空间名称")
    private String storageOssBucket;

    @ConfigItemField(SettingsEnum.STORAGE_OSS_REGION)
    @Schema(description = "OSS 空间地域节点")
    private String storageOssRegion;

    @ConfigItemField(SettingsEnum.STORAGE_COS_URL)
    @Schema(description = "COS 图片访问域名")
    private String storageCosUrl;

    @ConfigItemField(SettingsEnum.STORAGE_COS_SECRET_ID)
    @Schema(description = "COS SecretId")
    private String storageCosSecretId;

    @ConfigItemField(SettingsEnum.STORAGE_COS_SECRET_KEY)
    @Schema(description = "COS KeySecret")
    private String storageCosSecretKey;

    @ConfigItemField(SettingsEnum.STORAGE_COS_BUCKET)
    @Schema(description = "COS 空间名称")
    private String storageCosBucket;

    @ConfigItemField(SettingsEnum.STORAGE_COS_REGION)
    @Schema(description = "COS 空间地域节点")
    private String storageCosRegion;

    // *** 多语言设置 ***

    @ConfigItemField(SettingsEnum.LANG_ON)
    @Schema(description = "是否开启多语言；0-否，1-是")
    private Integer langOn;

    @ConfigItemField(SettingsEnum.LANG_TYPE)
    @Schema(description = "接口翻译")
    private Integer langType;

    @ConfigItemField(SettingsEnum.LANG_VOLCENGINE_ACCESS_KEY)
    @Schema(description = "火山翻译AssessKey")
    private String langVolcengineAccessKey;

    @ConfigItemField(SettingsEnum.LANG_VOLCENGINE_SECRET)
    @Schema(description = "火山翻译SecretKey")
    private String langVolcengineSecret;

    public void noUpdate(GlobalSettingsVO settings) {
        if (Objects.equals(this.storageOssAccessKeyId, settings.getStorageOssAccessKeyId())) {
            this.storageOssAccessKeyId = null;
        }
        if (Objects.equals(this.storageOssAccessKeySecret, settings.getStorageOssAccessKeySecret())) {
            this.storageOssAccessKeySecret = null;
        }

        if (Objects.equals(this.storageCosSecretId, settings.getStorageCosSecretId())) {
            this.storageCosSecretId = null;
        }
        if (Objects.equals(this.storageCosSecretKey, settings.getStorageCosSecretKey())) {
            this.storageCosSecretKey = null;
        }
    }
}
