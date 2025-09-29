package com.tigshop.bean.enums.setting;

import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础配置枚举
 *
 * @author kidd
 * @since 2025/4/1 15:45
 */
@Getter
@AllArgsConstructor
public enum SettingsEnum {

    // *** 商城设置-基础信息 ***

    // *** 商城信息 ***
    SHOP_LOGO("shopLogo", "商城LOGO", ValueTypeEnum.STRING, null),
    LIGHT_SHOP_LOGO("lightShopLogo", "lightShopLogo", ValueTypeEnum.STRING, null),
    SHOP_NAME("shopName", "商城简称", ValueTypeEnum.STRING, null),
    SHOP_COMPANY("shopCompany", "版权信息; 1-系统默认，0-自定义", ValueTypeEnum.INTEGER, "1"),
    DEFAULT_COPYRIGHT("defaultCopyright", "默认版权", ValueTypeEnum.STRING, "© 2018 江西佰商科技版权所有"),
    SHOP_COMPANY_TXT("shopCompanyTxt", "版权信息自定义值", ValueTypeEnum.STRING, null),
    POWERED_BY_STATUS("poweredByStatus", "标题栏版权隐藏；1-隐藏，0-显示", ValueTypeEnum.INTEGER, null),
    POWERED_BY("poweredBy", "技术支持标识；1-系统默认，0-自定义", ValueTypeEnum.INTEGER, "1"),
    POWERED_BY_LOGO("poweredByLogo", "技术支持标识logo", ValueTypeEnum.STRING, null),
    KEFU_ADDRESS("kefuAddress", "企业地址", ValueTypeEnum.STRING, null),

    // *** 备案信息 ***
    SHOP_ICP_NO("shopIcpNo", "ICP备案号", ValueTypeEnum.STRING, null),
    SHOP_ICP_NO_URL("shopIcpNoUrl", "ICP备案链接", ValueTypeEnum.STRING, null),
    SHOP_110_NO("shop110No", "公安备案号", ValueTypeEnum.STRING, null),
    SHOP_110_LINK("shop110Link", "公安备案链接", ValueTypeEnum.STRING, null),

    // *** 商城状态 ***
    CLOSE_ORDER("closeOrder", "关闭下单；0-否，1-是", ValueTypeEnum.INTEGER, null),
    SHOP_REG_CLOSED("shopRegClosed", "关闭注册；0-否，1-是", ValueTypeEnum.INTEGER, null),

    // *** 商城设置-商品设置 ***
    ENABLE_ATTRIBUTE_FILTER("enableAttributeFilter", "属性筛选：0表示关闭属性筛选，1表示开启属性筛选", ValueTypeEnum.INTEGER, "1"),

    // *** 商品货币设置 ***
    DOLLAR_SIGN("dollarSign", "货币符号", ValueTypeEnum.STRING, null),
    DOLLAR_SIGN_CN("dollarSignCn", "商品货币中文", ValueTypeEnum.STRING, null),

    // *** 商品录入设置 ***
    SN_PREFIX("snPrefix", "商品货号前缀", ValueTypeEnum.STRING, null),
    MARKET_PRICE_RATE("marketPriceRate", "市场价换算", ValueTypeEnum.DECIMAL, null),

    // *** 商品信息显示设置 ***
    SHOW_SELLED_COUNT("showSelledCount", "显示销量; 0-否，1-是", ValueTypeEnum.INTEGER, null),
    SHOW_MARKETPRICE("showMarketprice", "显示市场价; 0-否，1-是", ValueTypeEnum.INTEGER, null),

    // *** 商城设置-交易设置 ***

    // *** 购物全局设置 ***
    CHILD_AREA_NEED_REGION("childAreaNeedRegion", "运费模板地区设置机制；0-未设置的地区皆可配送（使用默认运费设置），1-仅设置的地区可配送", ValueTypeEnum.INTEGER, null),

    // *** 积分设置 ***
    INTEGRAL_NAME("integralName", "积分名称", ValueTypeEnum.STRING, null),
    INTEGRAL_SCALE("integralScale", "积分换算比例", ValueTypeEnum.STRING, null),
    ORDER_SEND_POINT("orderSendPoint", "下单送积分", ValueTypeEnum.STRING, null),
    INTEGRAL_PERCENT("integralPercent", "积分支付比例", ValueTypeEnum.STRING, null),
    COMMENT_SEND_POINT("commentSendPoint", "评论商品送积分", ValueTypeEnum.STRING, null),
    SHOW_SEND_POINT("showSendPoint", "晒单商品送积分", ValueTypeEnum.STRING, null),
    USE_QIANDAO_POINT("useQiandaoPoint", "签到赠送积分; 0-否，1-是", ValueTypeEnum.INTEGER, null),

    // *** 发票设置 ***
    CAN_INVOICE("canInvoice", "是否能开发票; 0-否，1-是", ValueTypeEnum.INTEGER, null),
    INVOICE_ADDED("invoiceAdded", "是否支持增值税专用发票; 0-否，1-是", ValueTypeEnum.INTEGER, null),

    // *** 退换货设置 ***
    RETURN_CONSIGNEE("returnConsignee", "回寄联系人设置", ValueTypeEnum.STRING, null),
    RETURN_MOBILE("returnMobile", "回寄电话设置", ValueTypeEnum.STRING, null),
    RETURN_ADDRESS("returnAddress", "回寄地址设置", ValueTypeEnum.STRING, null),

    // *** 商城设置-订单设置 ***

    AUTO_DELIVERY_DAYS("autoDeliveryDays", "自动收货天数", ValueTypeEnum.DECIMAL, null),
    AUTO_RETURN_GOODS("autoReturnGoods", "自动同意退货选项; 1-开启，0-关闭", ValueTypeEnum.INTEGER, null),
    AUTO_RETURN_GOODS_DAYS("autoReturnGoodsDays", "自动同意退天数", ValueTypeEnum.DECIMAL, null),
    AFTER_SALES_LIMIT_DAYS("afterSalesLimitDays", "售后限制天数", ValueTypeEnum.DECIMAL, null),
    AUTO_CANCEL_ORDER_MINUTE("autoCancelOrderMinute", "未付款订单超时时间", ValueTypeEnum.INTEGER, null),
    IS_PLATFORM_CANCEL_PAID_ORDER("isPlatformCancelPaidOrder", "平台已支付订单可取消订单", ValueTypeEnum.INTEGER, "0"),
    IS_PLATFORM_CANCEL_DELIVER_ORDER("isPlatformCancelDeliverOrder", "平台已发货订单可取消发货", ValueTypeEnum.INTEGER, "0"),
    IS_SHOP_CANCEL_DELIVER_ORDER("isShopCancelDeliverOrder", "店铺已发货订单可取消发货", ValueTypeEnum.INTEGER, "0"),


    // *** 商城设置-客服设置 ***

    // *** 客服设置 ***
    KEFU_TYPE("kefuType", "客服选择；0-无客服，1-腾讯云客服，2-企业微信客服，3-自定义客服，4-TigChart客服", ValueTypeEnum.INTEGER, null),
    KEFU_YZF_TYPE("kefuYzfType", "腾讯云客服打开方式；0-打开链接地址，1-小弹窗打开", ValueTypeEnum.INTEGER, null),
    KEFU_YZF_SIGN("kefuYzfSign", "腾讯云客服sign", ValueTypeEnum.STRING, null),
    KEFU_WORKWX_ID("kefuWorkwxId", "企业微信客服ID", ValueTypeEnum.STRING, null),
    CORP_ID("corpId", "企业微信企业ID", ValueTypeEnum.STRING, null),
    KEFU_CODE("kefuCode", "客服链接", ValueTypeEnum.STRING, null),
    KEFU_CODE_BLANK("kefuCodeBlank", "客服链接打开方式；0-打开链接地址，1-小弹窗打开", ValueTypeEnum.INTEGER, null),

    // *** 客服信息 ***
    KEFU_PHONE("kefuPhone", "客服热线电话", ValueTypeEnum.STRING, null),
    KEFU_TIME("kefuTime", "服务时间", ValueTypeEnum.STRING, null),

    // *** 个性化设置 ***
    // 猜你喜欢
    GUEST_LIKE_GOODS_NAME("guestLikeGoodsName", "猜你喜欢自定义名称", ValueTypeEnum.STRING, null),
    IS_GUEST_LIKE_GOODS("isGuestLikeGoods", "是否启用猜你喜欢", ValueTypeEnum.INTEGER, "1"),

    // *** 配送设置-配送设置 ***

    // *** 快递发货设置 ***
    DEFAULT_LOGISTICS_NAME("defaultLogisticsName", "默认配送名称", ValueTypeEnum.STRING, null),

    // *** 快递接口设置 ***
    KDNIAO_API_KEY("kdniaoApiKey", "快递鸟API Key", ValueTypeEnum.STRING, null),
    KDNIAO_BUSINESS_ID("kdniaoBusinessId", "快递鸟服务ID", ValueTypeEnum.STRING, null),

    // *** 发货信息设置 ***
    SENDER("sender", "发货人", ValueTypeEnum.STRING, null),
    MOBILE("mobile", "发货人联系方式", ValueTypeEnum.STRING, null),
    PROVINCE_NAME("provinceName", "发货地省份", ValueTypeEnum.STRING, null),
    CITY_NAME("cityName", "发货城市", ValueTypeEnum.STRING, null),
    AREA_NAME("areaName", "发货地区", ValueTypeEnum.STRING, null),
    ADDRESS("address", "发货详细地址", ValueTypeEnum.STRING, null),

    // *** 系统设置-全局设置 ***

    // *** 后台风格 ***
    NAV_THEME("navTheme", "外观", ValueTypeEnum.STRING, null),
    PRIMARY_COLOR("primaryColor", "主题色", ValueTypeEnum.STRING, null),
    LAYOUT("layout", "导航模式", ValueTypeEnum.STRING, null),
    ADMIN_LIGHT_LOGO("adminLightLogo", "后台LOGO", ValueTypeEnum.STRING, null),
    VERSION_INFO_HIDDEN("versionInfoHidden", "是否隐藏版本信息; 0-否，1-是", ValueTypeEnum.INTEGER, null),

    // *** SEO 设置 ***
    SHOP_TITLE_SUFFIX("shopTitleSuffix", "SEO标题后缀", ValueTypeEnum.STRING, null),
    SHOP_TITLE("shopTitle", "首页SEO标题", ValueTypeEnum.STRING, null),
    SHOP_KEYWORDS("shopKeywords", "首页SEO关键词", ValueTypeEnum.STRING, null),
    SHOP_DESC("shopDesc", "首页SEO描述", ValueTypeEnum.STRING, null),
    ICO_IMG("icoImg", "商城ico", ValueTypeEnum.STRING, null),
    DEFAULT_AVATAR("defaultAvatar", "会员默认头像", ValueTypeEnum.STRING, null),

    // *** 域名设置 ***
    CLIENT_DEFAULT_USE("clientDefaultUse", "前台默认跳转", ValueTypeEnum.INTEGER, null),
    PC_DOMAIN("pcDomain", "PC端域名", ValueTypeEnum.STRING, null),
    H5_DOMAIN("h5Domain", "H5端域名", ValueTypeEnum.STRING, null),
    ADMIN_DOMAIN("adminDomain", "后台域名", ValueTypeEnum.STRING, null),
    AUTO_REDIRECT("autoRedirect", "自动跳转H5；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),

    // *** 上传设置 ***
    UPLOAD_MAX_SIZE("uploadMaxSize", "上传文件最大尺寸，单位：MB", ValueTypeEnum.INTEGER, null),

    // *** 搜索设置 ***
    SEARCH_KEYWORDS("searchKeywords", "搜索热门关键字", ValueTypeEnum.STRING, null),
    MSG_HACK_WORD("msgHackWord", "敏感词屏蔽", ValueTypeEnum.STRING, null),
    IS_OPEN_PSCWS("isOpenPscws", "搜索关键词分词；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    SELECT_SEARCH_TYPE("selectSearchType", "搜索方式；es 或 mysql", ValueTypeEnum.STRING, "mysql"),

    // *** 地区设置 ***
    SHOP_DEFAULT_REGIONS("shopDefaultRegions", "商城默认地区", ValueTypeEnum.STRING, null),
    DEFAULT_COUNTRY("defaultCountry", "默认国家", ValueTypeEnum.INTEGER, null),

    // *** ICO 图标库 ***
    ICO_DEFINED_CSS("icoDefinedCss", "自定义ico图标库", ValueTypeEnum.STRING, null),

    // *** 存储设置 ***
    STORAGE_TYPE("storageType", "存储类型；0-本地存储，1-阿里云 OSS 存储，2-腾讯云 COS 存储", ValueTypeEnum.INTEGER, null),
    STORAGE_LOCAL_URL("storageLocalUrl", "本地 图片访问域名", ValueTypeEnum.STRING, null),
    STORAGE_OSS_URL("storageOssUrl", "OSS 图片访问域名", ValueTypeEnum.STRING, null),
    STORAGE_OSS_ACCESS_KEY_ID("storageOssAccessKeyId", "OSS AccessKeyId", ValueTypeEnum.STRING, null),
    STORAGE_OSS_ACCESS_KEY_SECRET("storageOssAccessKeySecret", "OSS AccessKeySecret", ValueTypeEnum.STRING, null),
    STORAGE_OSS_BUCKET("storageOssBucket", "OSS 空间名称", ValueTypeEnum.STRING, null),
    STORAGE_OSS_REGION("storageOssRegion", "OSS 空间地域节点", ValueTypeEnum.STRING, null),
    STORAGE_COS_URL("storageCosUrl", "COS 图片访问域名", ValueTypeEnum.STRING, null),
    STORAGE_COS_SECRET_ID("storageCosSecretId", "COS SecretId", ValueTypeEnum.STRING, null),
    STORAGE_COS_SECRET_KEY("storageCosSecretKey", "COS KeySecret", ValueTypeEnum.STRING, null),
    STORAGE_COS_BUCKET("storageCosBucket", "COS 空间名称", ValueTypeEnum.STRING, null),
    STORAGE_COS_REGION("storageCosRegion", "COS 空间地域节点", ValueTypeEnum.STRING, null),

    // *** 多语言设置 ***
    LANG_ON("langOn", "是否开启多语言；0-否，1-是", ValueTypeEnum.INTEGER, null),
    LANG_TYPE("langType", "接口翻译", ValueTypeEnum.INTEGER, null),
    LANG_VOLCENGINE_ACCESS_KEY("langVolcengineAccessKey", "火山翻译AssessKey", ValueTypeEnum.STRING, null),
    LANG_VOLCENGINE_SECRET("langVolcengineSecret", "火山翻译SecretKey", ValueTypeEnum.STRING, null),

    // *** 系统设置-登录设置 ***

    // *** 微信登录 ***
    OPEN_WECHAT_OAUTH("openWechatOauth", "开启公众号授权登录；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    WECHAT_APP_ID("wechatAppId", "微信APPID", ValueTypeEnum.STRING, null),
    WECHAT_APP_SECRET("wechatAppSecret", "微信AppSecret", ValueTypeEnum.STRING, null),
    WECHAT_SERVER_TOKEN("wechatServerToken", "微信服务器Token", ValueTypeEnum.STRING, null),
    WECHAT_SERVER_SECRET("wechatServerSecret", "微信服务器消息加密密钥", ValueTypeEnum.STRING, null),
    OPEN_WECHAT_REGISTER("openWechatRegister", "是否开启小程序快捷登录；0-否，1-是", ValueTypeEnum.INTEGER, null),
    WECHAT_REGISTER_BIND_PHONE("wechatRegisterBindPhone", "微信注册是否绑定手机；0-否，1-是", ValueTypeEnum.INTEGER, null),
    OPEN_EMAIL_REGISTER("openEmailRegister", "开启邮箱注册；0-否，1-是", ValueTypeEnum.INTEGER, null),
    OPEN_WECHAT_PC_LOGIN("openWechatPcLogin", "是否开启微信注册；0-否，1-是", ValueTypeEnum.INTEGER, null),

    // *** Facebook 登录 ***
    FACEBOOK_LOGIN_ON("facebookLoginOn", "是否开启Facebook登陆；0-否，1-是", ValueTypeEnum.INTEGER, null),
    FACEBOOK_CLIENT_ID("facebookClientId", "应用APP ID", ValueTypeEnum.STRING, null),
    FACEBOOK_CLIENT_SECRET("facebookClientSecret", "应用APP SECRET", ValueTypeEnum.STRING, null),

    // *** Google 登录 ***
    GOOGLE_LOGIN_ON("googleLoginOn", "是否开启Google登陆；0-否，1-是", ValueTypeEnum.INTEGER, null),
    GOOGLE_CLIENT_ID("googleClientId", "应用APP ID", ValueTypeEnum.STRING, null),
    GOOGLE_CLIENT_SECRET("googleClientSecret", "应用APP SECRET", ValueTypeEnum.STRING, null),

    // *** 基础设置 ***
    IS_OPEN_MOBILE_AREA_CODE("isOpenMobileAreaCode", "是否开启手机区号选择；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    USERNAME_PREFIX("usernamePrefix", "自动注册用户名前缀", ValueTypeEnum.STRING, null),

    // *** 系统设置-支付设置 ***

    // *** 基础设置 ***
    USE_SURPLUS("useSurplus", "是否启用余额支付；0-不支持，1-支持", ValueTypeEnum.INTEGER, null),
    USE_POINTS("usePoints", "是否启用积分支付；0-不支持，1-支持", ValueTypeEnum.INTEGER, null),
    USE_COUPON("useCoupon", "是否启用优惠券；0-不支持，1-支持", ValueTypeEnum.INTEGER, null),

    // *** 微信支付设置 ***
    USE_WECHAT("useWechat", "是否启用微信支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    WECHAT_MCHID_TYPE("wechatMchidType", "微信商户号类型；1-普通商户模式，2-服务商模式", ValueTypeEnum.INTEGER, null),
    WECHAT_PAY_MCHID("wechatPayMchid", "微信商户号", ValueTypeEnum.STRING, null),
    WECHAT_PAY_SUB_MCHID("wechatPaySubMchid", "微信子商户号", ValueTypeEnum.INTEGER, null),
    WECHAT_PAY_KEY("wechatPayKey", "商户API密钥", ValueTypeEnum.STRING, null),
    WECHAT_PAY_SERIAL_NO("wechatPaySerialNo", "商户证书序列号", ValueTypeEnum.STRING, null),
    WECHAT_PAY_CERTIFICATE("wechatPayCertificate", "商户API证书", ValueTypeEnum.INTEGER, null),
    WECHAT_PAY_PRIVATE_KEY("wechatPayPrivateKey", "商户API证书密钥", ValueTypeEnum.INTEGER, null),
    WECHAT_PAY_CHECK_TYPE("wechatPayCheckType", "验证微信支付方式；1-平台证书，2-微信支付公钥", ValueTypeEnum.INTEGER, null),
    WECHAT_PAY_PLATFORM_CERTIFICATE("wechatPayPlatformCertificate", "平台证书", ValueTypeEnum.INTEGER, null),
    WECHAT_PAY_PUBLIC_KEY_ID("wechatPayPublicKeyId", "微信支付公钥ID", ValueTypeEnum.STRING, null),
    WECHAT_PAY_PUBLIC_KEY("wechatPayPublicKey", "微信支付公钥文件", ValueTypeEnum.INTEGER, null),

    // *** 微信小程序支付 ***
    WECHAT_MINI_PROGRAM_APP_ID("wechatMiniProgramAppId", "微信小程序AppID", ValueTypeEnum.STRING, null),
    WECHAT_MINI_PROGRAM_SECRET("wechatMiniProgramSecret", "微信小程序AppSecret", ValueTypeEnum.STRING, null),

    // *** 微信 APP 支付 ***
    WECHAT_PAY_APP_ID("wechatPayAppId", "应用APP ID", ValueTypeEnum.STRING, null),
    WECHAT_PAY_APP_SECRET("wechatPayAppSecret", "应用APP SECRET", ValueTypeEnum.STRING, null),

    // *** 支付宝支付设置 ***
    USE_ALIPAY("useAlipay", "是否启用支付宝支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    ALIPAY_APPID("alipayAppid", "支付宝APPID", ValueTypeEnum.STRING, null),
    ALIPAY_MOBILE_APPID("alipayMobileAppid", "支付宝移动端AppID", ValueTypeEnum.STRING, null),
    ALIPAY_RSA_PRIVATE_KEY("alipayRsaPrivateKey", "应用私钥", ValueTypeEnum.STRING, null),
    ALIPAY_RSA_PUBLIC_KEY("alipayRsaPublicKey", "支付宝公钥", ValueTypeEnum.STRING, null),

    // *** 易班支付设置 ***
    USE_YABANPAY("useYabanpay", "是否启用YaBand支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    USE_YABANPAY_WECHAT("useYabanpayWechat", "是否启用YaBand微信支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    USE_YABANPAY_ALIPAY("useYabanpayAlipay", "是否启用YaBand支付宝支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    YABANPAY_CURRENCY("yabanpayCurrency", "YaBand支付货币类型", ValueTypeEnum.STRING, null),
    YABANPAY_UID("yabandpayUid", "YaBand支付UID", ValueTypeEnum.STRING, null),
    YABANPAY_SECRET_KEY("yabandpaySecretKey", "YaBand支付密钥", ValueTypeEnum.STRING, null),

    // *** 线下支付设置 ***
    USE_OFFLINE("useOffline", "是否启用线下支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    OFFLINE_PAY_BANK("offlinePayBank", "银行汇款", ValueTypeEnum.STRING, null),
    OFFLINE_PAY_COMPANY("offlinePayCompany", "企业汇款", ValueTypeEnum.STRING, null),

    // *** Paypal 支付设置 ***
    USE_PAYPAL("usePaypal", "是否启用PayPal支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    PAYPAL_CURRENCY("paypalCurrency", "PayPal货币类型", ValueTypeEnum.STRING, null),
    PAYPAL_CLIENT_ID("paypalClientId", "PayPal客户端ID", ValueTypeEnum.STRING, null),
    PAYPAL_SECRET("paypalClientSecret", "PayPal密钥", ValueTypeEnum.STRING, null),

    // *** 云支付设置 ***
    USE_YUNPAY("useYunpay", "是否启用云支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    YUNPAY_UID("yunpayUid", "商户号", ValueTypeEnum.STRING, null),
    YUNPAY_SECRET_KEY("yunpaySecretKey", "商户秘钥", ValueTypeEnum.STRING, null),

    // *** 系统设置-邮箱服务器 ***

    MAIL_SERVICE("mailService", "邮件服务；0-本地邮件服务器，1-SMTP服务", ValueTypeEnum.INTEGER, null),
    SMTP_SSL("smtpSsl", "是否启用SSL；0-否，1-是", ValueTypeEnum.INTEGER, null),
    SMTP_HOST("smtpHost", "发送邮件服务器地址(SMTP)", ValueTypeEnum.STRING, null),
    SMTP_PORT("smtpPort", "SMTP服务器端口", ValueTypeEnum.STRING, null),
    SMTP_USER("smtpUser", "邮件发送帐号", ValueTypeEnum.STRING, null),
    SMTP_PASS("smtpPass", "帐号密码", ValueTypeEnum.STRING, null),
    SMTP_MAIL("smtpMail", "邮件回复地址", ValueTypeEnum.STRING, null),
    MAIL_CHARSET("mailCharset", "邮件编码；UTF8-国际编码(UTF8)，GB2312-简体中文(GB2312)，BIG5-繁体中文(BIG5)", ValueTypeEnum.STRING, null),
    TEST_MAIL_ADDRESS("testMailAddress", "测试邮件地址", ValueTypeEnum.STRING, null),

    // *** 消息设置-通知设置 ***

    // *** 短信设置 ***
    SMS_KEY_ID("smsKeyId", "短信帐户用户名", ValueTypeEnum.STRING, null),
    SMS_KEY_SECRET("smsKeySecret", "短信帐户密码", ValueTypeEnum.STRING, null),
    SMS_SIGN_NAME("smsSignName", "短信签名", ValueTypeEnum.STRING, null),
    SMS_SHOP_MOBILE("smsShopMobile", "商家短信号码", ValueTypeEnum.STRING, null),

    // *** 邮箱通知 ***
    SERVICE_EMAIL("serviceEmail", "客服收件地址", ValueTypeEnum.STRING, null),
    SEND_CONFIRM_EMAIL("sendConfirmEmail", "提交订单发送邮件; 0-否，1-是", ValueTypeEnum.INTEGER, null),
    ORDER_PAY_EMAIL("orderPayEmail", "订单支付发送邮件; 0-否，1-是", ValueTypeEnum.INTEGER, null),
    SEND_SERVICE_EMAIL("sendServiceEmail", "下单给客服发邮件; 0-否，1-是", ValueTypeEnum.INTEGER, null),
    SEND_SHIP_EMAIL("sendShipEmail", "发货时发送邮件; 0-否，1-是", ValueTypeEnum.INTEGER, null),

    // *** 会员认证 ***

    CLOSE_AUTH("closeAuth", "是否关闭；1-否，0-是", ValueTypeEnum.INTEGER, "0"),
    TYPE("type", "认证类型；0-个人认证 + 企业认证，1-企业认证", ValueTypeEnum.INTEGER, null),
    IS_IDENTITY("isIdentity", "是否实名后才能查看价格；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    IS_ENQUIRY("isEnquiry", "是否开启询价功能；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    SMS_NOTE("smsNote", "是否短信通知；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    TIPS("tips", "审核周期", ValueTypeEnum.STRING, null),
    BULK_PURCHASE("bulkPurchase", "是否批量采购；0-隐藏，1-显示", ValueTypeEnum.INTEGER, "0"),

    // *** 售后设置 ***

    TEMPLATE_CONTENT("templateContent", "售后服务", ValueTypeEnum.STRING, null),

    // *** 授权信息 ***

    LICENSE("license", "授权码", ValueTypeEnum.STRING, null),
    LICENSED_TYPE("licensedType", "授权类型", ValueTypeEnum.STRING, null),
    LICENSED_TYPE_NAME("licensedTypeName", "授权类型名称", ValueTypeEnum.STRING, null),
    IS_ENTERPRISE("isEnterprise", "是否企业用户", ValueTypeEnum.INTEGER, null),
    DE_COPYRIGHT("deCopyright", "去版权", ValueTypeEnum.INTEGER, null),
    AUTHORIZED_DOMAIN("authorizedDomain", "授权域名", ValueTypeEnum.STRING, null),

    ORDER_ID("orderId", "订单 ID", ValueTypeEnum.STRING, null),
    HOLDER("holder", "持有者", ValueTypeEnum.STRING, null),
    LICENSED_ID("licensedId", "授权 ID 列表", ValueTypeEnum.STRING, null),
    RELEASE_TIME("releaseTime", "发布时间", ValueTypeEnum.STRING, null),
    EXPIRATION_TIME("expirationTime", "过期时间", ValueTypeEnum.STRING, null),

    // *** 主题风格设置 ***

    THEME_STYLE("themeStyle", "主题类型", ValueTypeEnum.STRING, null),

    // *** 分页页装修设置 ***

    PRODUCT_CATEGORY_DECORATE_TYPE("productCategoryDecorateType", "分类页装修类型；1-样式一，2-样式二，3-样式三", ValueTypeEnum.INTEGER, null),

    // *** 店铺设置 ***

    SHOP_PRODUCT_NEED_CHECK("shopProductNeedCheck", "店铺商品是否需要审核；0-否，1-是", ValueTypeEnum.INTEGER, null),
    MAX_RECOMMEND_PRODUCT_COUNT("maxRecommendProductCount", "最多推荐商品数量", ValueTypeEnum.STRING, null),
    MAX_SUB_ADMINISTRATOR("maxSubAdministrator", "最大管理员数", ValueTypeEnum.STRING, null),
    DEFAULT_SHOP_NAME("defaultShopName", "店铺名称", ValueTypeEnum.STRING, null),

    // *** 供应商设置 ***

    VENDOR_PRODUCT_NEED_CHECK("vendorProductNeedCheck", "供应商商品是否需要审核；0-否，1-是", ValueTypeEnum.INTEGER, null),
    VENDOR_MAX_SUB_ADMINISTRATOR("vendorMaxSubAdministrator", "最大管理员数", ValueTypeEnum.INTEGER, null),
    VENDOR_SET_PRICE_TYPE("vendorSetPriceType", "供应商设价方式(1按比例，2-按固定数值加价，3-默认售价)", ValueTypeEnum.INTEGER, null),
    VENDOR_SET_PRICE_AUTO_VALUE("vendorSetPriceAutoValue", "智能设价（百分比或固定数值）", ValueTypeEnum.STRING, null),
    VENDOR_AGREEMENT("vendorAgreement", "供应商协议", ValueTypeEnum.STRING, null),

    // *** 商户设置 ***

    PERSON_APPLY_ENABLED("personApplyEnabled", "是否开启个人申请；0-否，1-是", ValueTypeEnum.INTEGER, null),
    MERCHANT_APPLY_NEED_CHECK("merchantApplyNeedCheck", "商户申请是否需要审核；0-否，1-是", ValueTypeEnum.INTEGER, null),
    MAX_SHOP_COUNT("maxShopCount", "最大店铺数量", ValueTypeEnum.STRING, null),
    SHOP_AGREEMENT("shopAgreement", "店铺协议", ValueTypeEnum.STRING, null),

    // *** 默认数据 ***
    DEFAULT_TECH_SUPPORT("defaultTechSupport", "默认技术支持", ValueTypeEnum.STRING, "/static/mini/images/common/default_tech_support.png"),
    PAYPAL_CURRENCY_LIST("paypalCurrencyList", "paypal货币列表", ValueTypeEnum.STRING, ""),


    // *** app version（菜单关闭） ***
    ANDROID_VERSION("androidVersion", "Android版本号", ValueTypeEnum.STRING, null),
    IOS_VERSION("iosVersion", "iOS版本号", ValueTypeEnum.STRING, null),
    IOS_LINK("iosLink", "iOS下载链接", ValueTypeEnum.STRING, null),
    ANDROID_LINK("androidLink", "Android下载链接", ValueTypeEnum.STRING, null),
    HOT_UPDATE_LINK("hotUpdateLink", "热更新包链接", ValueTypeEnum.STRING, null),
    HOT_UPDATE_TYPE("hotUpdateType", "热更新类型（0：关闭，1：开启）", ValueTypeEnum.STRING, null),


    // *** 登录协议 ***
    TERMS_OF_SERVICE_SHOW("termsOfServiceShow", "服务协议展示", ValueTypeEnum.INTEGER, null),
    TERMS_OF_SERVICE("termsOfService", "服务协议", ValueTypeEnum.STRING, null),
    PRIVACY_POLICY_SHOW("privacyPolicyShow", "隐私政策展示", ValueTypeEnum.INTEGER, null),
    PRIVACY_POLICY("privacyPolicy", "隐私政策", ValueTypeEnum.STRING, null),
    AFTER_SALES_SERVICE_SHOW("afterSalesServiceShow", "售后服务展示", ValueTypeEnum.INTEGER, null),
    AFTER_SALES_SERVICE("afterSalesService", "售后服务", ValueTypeEnum.STRING, null),

    // *** 提现设置 ***

    // *** 分账设置 ***
    // *** 对账与结算设置 ***
    BILLING_NODE("billingNode", "出账节点，1-下单成功，2-售后申请", ValueTypeEnum.INTEGER, "1"),
    COLLECTION_NODE("collectionNode", "入账节点，1-订单完成，2-确认收货", ValueTypeEnum.INTEGER, "1"),
    COLLECTION_TIME_SETTING("collectionTimeSetting", "入账时间（单位：天）", ValueTypeEnum.INTEGER, "0"),
    COLLECTION_METHOD("collectionMethod", "入账方式，1-自动入账，2-手动入账", ValueTypeEnum.INTEGER, "1"),
    COLLECTION_ACCOUNT_TYPE("collectionAccountType", "入账账户类型，1-余额", ValueTypeEnum.INTEGER, "1"),
    SPLIT_PAYMENT_METHOD("splitPaymentMethod", "分账方式，1-线下分账，2-线上分账", ValueTypeEnum.INTEGER, "1"),

    // *** 服务费与提现费率设置 ***
    STORE_GENERAL_SERVICE_FEE_RATE("storeGeneralServiceFeeRate", "店铺通用平台服务费率（%）", ValueTypeEnum.DECIMAL, "0"),
    STORE_WITHDRAWAL_FEE_RATE("storeWithdrawalFeeRate", "店铺提现手续费率（%）", ValueTypeEnum.DECIMAL, "0"),
    STOREFRONT_GENERAL_SERVICE_FEE_RATE("storefrontGeneralServiceFeeRate", "门店通用平台服务费率（%）", ValueTypeEnum.DECIMAL, "0"),
    STOREFRONT_WITHDRAWAL_FEE_RATE("storefrontWithdrawalFeeRate", "门店提现手续费率（%）", ValueTypeEnum.DECIMAL, "0"),
    SUPPLIER_GENERAL_SERVICE_FEE_RATE("supplierGeneralServiceFeeRate", "供应商通用平台服务费率（%）", ValueTypeEnum.DECIMAL, "0"),
    SUPPLIER_WITHDRAWAL_FEE_RATE("supplierWithdrawalFeeRate", "供应商提现手续费率（%）", ValueTypeEnum.DECIMAL, "0"),

    // *** 提现设置 ***
    WITHDRAWAL_RECEIPT_METHOD("withdrawalReceiptMethod", "线下收款方式，数组：1-银行卡，2-支付宝，3-微信", ValueTypeEnum.JSON, JSON.toJSONString(Arrays.asList(1, 2, 3))),
    WITHDRAWAL_ENABLED("withdrawalEnabled", "是否开通提现，0-不开通，1-开通", ValueTypeEnum.INTEGER, "0"),
    WITHDRAWAL_MIN_AMOUNT("minAmount", "提现限额-最小金额", ValueTypeEnum.DECIMAL, "0"),
    WITHDRAWAL_MAX_AMOUNT("maxAmount", "提现限额-最大金额", ValueTypeEnum.DECIMAL, "0"),
    WITHDRAWAL_FREQUENCY_UNIT("withdrawalFrequencyUnit", "提现频次单位，1-每日，2-每月，3-每年", ValueTypeEnum.INTEGER, "1"),
    WITHDRAWAL_FREQUENCY_COUNT("withdrawalFrequencyCount", "提现频次数量", ValueTypeEnum.INTEGER, "1"),
    WITHDRAWAL_REVIEW_METHOD("withdrawalReviewMethod", "提现审核方式，1-人工审核", ValueTypeEnum.INTEGER, "1"),
    WITHDRAWAL_DESCRIPTION("withdrawalDescription", "提现说明", ValueTypeEnum.STRING, ""),

    // *** 门店设置 ***
    STORE_POST_ALLOCATION_STATUS("storePostAllocationStatus", "商品分配后状态", ValueTypeEnum.INTEGER, null),
    STORE_INDEPENDENT_GOODS("storeIndependentGoods", "门店独立商品", ValueTypeEnum.INTEGER, null),
    STORE_ASSIGN_PRODUCT_NAME("storeAssignProductName", "分配商品名称", ValueTypeEnum.INTEGER, null),
    STORE_ASSIGN_PRODUCT_PRICE("storeAssignProductPrice", "分配商品价格", ValueTypeEnum.INTEGER, null),
    STORE_USE_SOLO_PRODUCT_STOCK("storeUseSoloProductStock", "使用门店独立库存", ValueTypeEnum.INTEGER, null),
    STORE_USE_TOTAL_PRODUCT_STOCK("storeUseTotalProductStock", "使用总库存", ValueTypeEnum.INTEGER, null),
    STORE_CUSTOM_SUBMIT_SHIPPING_TYPE("storeCustomSubmitShippingType", "客户下单配送方式", ValueTypeEnum.INTEGER, null),
    STORE_SHOW_OTHER_CITY_STORE("storeShowOtherCityStore", "显示地区其他门店", ValueTypeEnum.INTEGER, null),

    AMAP_WEB_KEY("amapWebKey", "web 高德地图key", ValueTypeEnum.STRING, null),
    AMAP_WEB_SECRET("amapWebSecret", "web 高德地图secret", ValueTypeEnum.STRING, null),
    AMAP_WEB_JS_KEY("amapWebJsKey", "webJs 高德地图key", ValueTypeEnum.STRING, null),
    AMAP_WEB_JS_SECRET("amapWebJsSecret", "webJs 高德地图secret", ValueTypeEnum.STRING, null),
    AMAP_WEB_MINI_KEY("amapWebMiniKey", "webMini 高德地图key", ValueTypeEnum.STRING, null),
    AMAP_WEB_MINI_SECRET("amapWebMiniSecret", "webMini 高德地图secret", ValueTypeEnum.STRING, null),

    // *** 删除的设置 ***
    SELF_STORE_NAME("selfStoreName", "自营名称", ValueTypeEnum.STRING, null),
    SHOW_CAT_LEVEL("showCatLevel", "商城分类级数；0-三级或以上，1-二级", ValueTypeEnum.STRING, null),
    UNIQUE_OPENED("uniqueOpened", "菜单是否手风琴展开；0-否，1-是", ValueTypeEnum.STRING, null),
    IS_MULTI_LABEL("isMultiLabel", "是否多标签；0-否，1-是", ValueTypeEnum.STRING, null),
    WECHAT_SERVER_URL("wechatServerUrl", "微信服务器接口地址", ValueTypeEnum.STRING, null),
    IS_CHANGE_ORDER_STATUS("isChangeOrderStatus", "订单流转设置；0-关闭，1-开启", ValueTypeEnum.INTEGER, "0"),


    // *** 未知 ***
    SHOP_COMPANY_DUPLICATE("shopCompanyDuplicate", "shopCompanyDuplicate", ValueTypeEnum.STRING, null),
    ADMIN_DARK_LOGO("adminDarkLogo", "adminDarkLogo", ValueTypeEnum.STRING, null),
    DEFAULT_COMPANY("defaultCompany", "默认公司", ValueTypeEnum.STRING, null),
    COMPANY_ADDRESS("companyAddress", "公司地址", ValueTypeEnum.STRING, null),
    COMMENT_CHECK("commentCheck", "commentCheck", ValueTypeEnum.STRING, null),
    COMPANY_DATA_TYPE("companyDataType", "企业信息收集类型", ValueTypeEnum.STRING, null),
    COMPANY_DATA_TIPS("companyDataTips", "企业信息提示内容", ValueTypeEnum.STRING, null),
    ICO_TIG_CSS("icoTigCss", "官方图标库地址", ValueTypeEnum.STRING, null),
    AFTER_SALES_SETTING("afterSalesSetting", "售后设置", ValueTypeEnum.STRING, null),
    SHOP_RANK_DATE_RAGE("shopRankDateRage", "店铺排名时间范围（天）", ValueTypeEnum.STRING, null),
    ENABLED_COMMISSION_ORDER("enabledCommissionOrder", "是否开启分佣订单；0-否，1-是", ValueTypeEnum.STRING, null),
    DEFAULT_ADMIN_PREFIX("defaultAdminPrefix", "默认管理员前缀", ValueTypeEnum.STRING, null),
    GROW_UP_SETTING("growUpSetting", "growUpSetting", ValueTypeEnum.STRING, null),
    ;

    @Schema(description = "业务代码")
    private final String bizCode;

    @Schema(description = "业务描述")
    private final String bizDesc;

    @Schema(description = "值类型")
    private final ValueTypeEnum valueType;

    @Schema(description = "默认值")
    private final String defaultValue;

    private static final Map<String, SettingsEnum> SETTINGS_MAP = new HashMap<>();

    static {
        for (SettingsEnum setting : values()) {
            SETTINGS_MAP.put(setting.bizCode, setting);
        }
    }

    public static SettingsEnum fromKey(String key) {
        return SETTINGS_MAP.get(key);
    }


    public enum ValueTypeEnum {
        STRING,
        INTEGER,
        LONG,
        BOOLEAN,
        DECIMAL,
        JSON,
        JSON_ARRAY,
        JSON_OBJECT,
    }

}
