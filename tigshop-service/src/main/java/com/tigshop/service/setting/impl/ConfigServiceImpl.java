// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.annotations.settings.ConfigIgnoreField;
import com.tigshop.bean.annotations.settings.ConfigInnerParam;
import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.dto.login.RegisterEmailDTO;
import com.tigshop.bean.dto.login.RegisterSmsDTO;
import com.tigshop.bean.dto.setting.ConfigCustomerDTO;
import com.tigshop.bean.dto.setting.ConfigStorageDTO;
import com.tigshop.bean.dto.setting.SmsSendDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.common.StorageType;
import com.tigshop.bean.enums.decorate.DecorateType;
import com.tigshop.bean.enums.setting.MailTemplateType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.feign.wechat.CreateQrcodeResult;
import com.tigshop.bean.feign.wechat.GetTokenResult;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.decorate.Decorate;
import com.tigshop.bean.model.o2o.ShopPickupConfig;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.setting.MailTemplates;
import com.tigshop.bean.model.setting.MessageTemplate;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.model.user.UserRankConfig;
import com.tigshop.bean.param.settings.config.*;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import com.tigshop.bean.param.user.CreateQrcodeParam;
import com.tigshop.bean.vo.config.*;
import com.tigshop.bean.vo.setting.config.*;
import com.tigshop.bean.vo.setting.SearchSettingsVO;
import com.tigshop.bean.dto.setting.SearchSaveParam;
import com.tigshop.bean.vo.user.WechatLoginUrlVO;
import com.tigshop.service.product.ProductEsService;
import com.tigshop.common.constant.setting.ConfigConstants;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.helper.SettingsHelper;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.setting.ConfigMapper;
import com.tigshop.mapper.setting.RegionMapper;
import com.tigshop.mapper.user.UserRankConfigMapper;
import com.tigshop.security.common.handler.CaptchaHandler;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.decorate.DecorateService;
import com.tigshop.service.o2o.ShopPickupConfigService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.MailTemplatesService;
import com.tigshop.service.setting.MessageTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.setting.MessageTemplateType.MSG_CODE;
import static com.tigshop.bean.enums.setting.MessageTypeEnum.MESSAGE_TYPE_MSG;
import static com.tigshop.bean.enums.setting.SettingsEnum.*;
import static com.tigshop.bean.enums.setting.SettingsEnum.AFTER_SALES_SERVICE;
import static com.tigshop.bean.enums.setting.SettingsEnum.DEFAULT_COMPANY;
import static com.tigshop.bean.enums.setting.SettingsEnum.SHOP_DESC;
import static com.tigshop.bean.enums.setting.SettingsEnum.SHOP_ICP_NO;
import static com.tigshop.bean.enums.setting.SettingsEnum.SHOP_KEYWORDS;
import static com.tigshop.bean.enums.setting.SettingsEnum.SHOP_NAME;
import static com.tigshop.bean.enums.setting.SettingsEnum.THEME_STYLE;
import static com.tigshop.common.constant.Constants.*;
import static com.tigshop.common.constant.ExceptionConstants.CODE_PARAM_ERROR;
import static com.tigshop.common.constant.ExceptionConstants.PARAM_ERROR;
import static com.tigshop.common.constant.setting.ConfigConstants.*;
import static com.tigshop.common.enums.DataType.SETTING;

/**
 * 设置配置服务实现
 *
 * @author kidd
 * @since 2025/4/1 10:40
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ConfigServiceImpl extends BaseServiceImpl<ConfigMapper, ConfigPO> implements ConfigService {

    private final MessageTemplateService messageTemplateService;

    private final CaptchaHandler captchaHandler;

    private final SettingsHelper settingsHelper;

    private final RedisTemplate<String, String> redisTemplate;

    private final RegionMapper regionMapper;

    private final DecorateService decorateService;

    private final RabbitTemplate rabbitTemplate;

    private final UserRankConfigMapper userRankConfigMapper;

    private final TranslatePackageImpl translatePackage;

    private final WechatApiClient wechatApiClient;

    private final AdminUserMapper adminUserMapper;

    private final MailTemplatesService mailTemplatesService;

    private final TigshopProperties tigshopProperties;

    private final ProductMapper productMapper;

    private final ProductEsService productEsService;

    @Value("${icoTigCss}")
    private String icoTigCss;

    private final ShopPickupConfigService shopPickupConfigService;

    private final ApplicationContext applicationContext;

    @Override
    public BasicSettingsVO getBasicSettings() {
        return getSettings(BasicSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveBasic(BasicSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public ProductSettingsVO getProductSettings() {
        return getSettings(ProductSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveProduct(ProductSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public ShoppingSettingsVO getShoppingSettings() {
        return getSettings(ShoppingSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveShoppingSettings(ShoppingSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public OrderSettingsVO getOrderSettings() {
        return getSettings(OrderSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveOrderSettings(OrderSettingsSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public KefuSettingsVO getKefuSettings() {
        KefuSettingsVO settings = this.getSettings(KefuSettingsVO.class);

        // 数据加密
        settings.encryptData();

        return settings;
    }

    @Transactional
    @Override
    public void saveKefuSettings(KefuSaveParam param) {
        KefuSettingsVO settings = this.getKefuSettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    @Override
    public ShippingSettingsVO getShippingSettings() {
        ShippingSettingsVO settings = this.getSettings(ShippingSettingsVO.class);

        // 数据加密
        settings.encryptData();

        return settings;
    }

    @Transactional
    @Override
    public void saveShippingSettings(ShippingSaveParam param) {
        ShippingSettingsVO settings = this.getShippingSettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    @Override
    public GlobalSettingsVO getGlobalSettings() {
        GlobalSettingsVO settings = getSettings(GlobalSettingsVO.class);

        // 数据加密
        settings.encryptData();

        // 处理商城默认区域 [ 110000, 110100, 110102 ]
        settings.setShopDefaultRegions(settings.getShopDefaultRegions());

        // 查询国家
        List<Region> countries = regionMapper.selectList(new LambdaQueryWrapper<Region>().eq(Region::getParentId, 0));
        settings.setCountries(countries);

        // 处理登录账户是商户的情况，配置返回默认值
        Integer adminId = SecurityUtils.getCurrentAdminId();
        Long shopCount = adminUserMapper.selectCount(
                Wrappers.lambdaQuery(AdminUser.class)
                        .eq(AdminUser::getAdminId, adminId)
                        .eq(AdminUser::getAdminType, AdminTypeEnum.SHOP.getCode())
        );
        if (shopCount > 0) {
            settings.setNavTheme("dark");
            settings.setLayout("topMenu");
        }

        return settings;
    }

    @Transactional
    @Override
    public void saveGlobalSettings(GlobalSettingsSaveParam param) {
        GlobalSettingsVO settings = this.getGlobalSettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    @Override
    public LoginSettingsVO getLoginSettings() {
        LoginSettingsVO settings = getSettings(LoginSettingsVO.class);

        // 数据加密
        settings.encryptData();

        return settings;
    }

    @Transactional
    @Override
    public void saveLoginSettings(LoginSettingsSaveParam param) {
        LoginSettingsVO settings = this.getLoginSettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    @Override
    public BasicPaySettingsVO getBasicPaySettings() {
        return this.getSettings(BasicPaySettingsVO.class);
    }

    @Transactional
    @Override
    public void saveBasicPaySettings(BasicPaySettingsSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public WechatPaySettingsVO getWechatPaySettings() {
        WechatPaySettingsVO settings = this.getSettings(WechatPaySettingsVO.class);

        // 数据加密
        settings.encryptData();

        return settings;
    }

    @Transactional
    @Override
    public void saveWechatPaySettings(WechatPaySettingsSaveParam param) {
        WechatPaySettingsVO settings = this.getWechatPaySettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    @Override
    public AliPaySettingsVO getAliPaySettings() {
        AliPaySettingsVO settings = this.getSettings(AliPaySettingsVO.class);

        // 数据加密
        settings.encryptData();

        return settings;
    }

    @Transactional
    @Override
    public void saveAliPaySettings(AliPaySettingsSaveParam param) {
        AliPaySettingsVO settings = this.getAliPaySettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    @Override
    public YaBandPaySettingsVO getYaBandPaySettings() {
        YaBandPaySettingsVO settings = this.getSettings(YaBandPaySettingsVO.class);

        // 数据加密
        settings.encryptData();

        return settings;
    }

    @Transactional
    @Override
    public void saveYaBandPaySettings(YaBandPaySettingsSaveParam param) {
        YaBandPaySettingsVO settings = this.getYaBandPaySettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    @Override
    public OfflinePaySettingsVO getOfflinePaySettings() {
        return this.getSettings(OfflinePaySettingsVO.class);
    }

    @Transactional
    @Override
    public void saveOfflinePaySettings(OfflinePaySettingsSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public PayPalSettingsVO getPayPalSettings() {
        PayPalSettingsVO settings = this.getSettings(PayPalSettingsVO.class);

        // 数据加密
        settings.encryptData();

        settings.setPaypalCurrencyList(JSON.parseArray(settings.getPaypalCurrencyListStr()));

        return settings;
    }

    @Transactional
    @Override
    public void savePayPalSettings(PayPalSettingsSaveParam param) {
        PayPalSettingsVO settings = this.getPayPalSettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    @Override
    public YunPaySettingsVO getYunPaySettings() {
        YunPaySettingsVO settings = this.getSettings(YunPaySettingsVO.class);

        // 数据加密
        settings.encryptData();

        return settings;
    }

    @Transactional
    @Override
    public void saveYunPaySettings(YunPaySettingsSaveParam param) {
        YunPaySettingsVO settings = this.getYunPaySettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    @Override
    public MailSettingsVO getMailSettings() {
        return this.getSettings(MailSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveMail(MailSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public NotifySettingsVO getNotifySettings() {
        NotifySettingsVO settings = this.getSettings(NotifySettingsVO.class);

        // 数据加密
        settings.encryptData();

        return settings;
    }

    @Transactional
    @Override
    public void saveNotify(NotifySaveParam param) {
        NotifySettingsVO settings = this.getNotifySettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    @Override
    public AuthSettingsVO getAuthSettings() {
        return this.getSettings(AuthSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveAuth(AuthSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public AfterSalesSettingsVO getAfterSalesSettings() {
        return getSettings(AfterSalesSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveAfterSales(AfterSaleSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public ThemeStyleSettingsVO getThemeStyleSettings() {
        return this.getSettings(ThemeStyleSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveThemeStyle(ThemeStyleSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public CategoryDecorateSettingsVO getCategoryDecorateSettings() {
        return this.getSettings(CategoryDecorateSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveCategoryDecorate(CategoryDecorateSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public ShopSettingsVO getShopSettings() {
        return this.getSettings(ShopSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveShop(ShopSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public MerchantSettingsVO getMerchantSettings() {
        return this.getSettings(MerchantSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveMerchant(MerchantSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public GetAdminVO getAdmin() {
        GetAdminVO settings = this.getSettings(GetAdminVO.class);

        // 判断是否设置了商城LOGO，如果设置了需要加上图片访问域名
        boolean hasUrl = StrUtil.startWith(settings.getShopLogo(), "https://") || StrUtil.startWith(settings.getShopLogo(), "http://");
        if (StringUtils.isNotBlank(settings.getShopLogo()) && !hasUrl) {
            String storageUrl = getStorageUrl();
            settings.setShopLogo(storageUrl + settings.getShopLogo());
        }

        return settings;
    }

    @Override
    public GetAdminBaseVO getAdminBase() {
        GetAdminBaseVO settings = this.getSettings(GetAdminBaseVO.class);

        String storageUrl = this.getStorageUrl();
        settings.setStorageUrl(storageUrl);

        settings.setVersionType(tigshopProperties.getVersion());
        settings.setVersion(tigshopProperties.getVersionNum());

        // 处理登录账户是商户的情况，配置返回默认值
        Integer shopId = HeaderUtils.getShopId();
        if (shopId != null && shopId > 0) {
            settings.setNavTheme("dark");
            settings.setLayout("topMenu");
        }
        Integer vendorId = HeaderUtils.getVendorId();
        if (vendorId != null && vendorId > 0) {
            settings.setNavTheme("dark");
            settings.setLayout("topMenu");
        }
        if (List.of(AdminTypeEnum.STORE.getCode(), AdminTypeEnum.PICKUP.getCode()).contains(HeaderUtils.getAdminType())) {
            settings.setNavTheme("dark");
            settings.setLayout("topMenu");
            settings.setPrimaryColor("blue");
        }

        // 获取店铺自提配置
        ShopPickupConfig shopPickupConfig = shopPickupConfigService.lambdaQuery().eq(ShopPickupConfig::getShopId, shopId).one();
        settings.setIsOpenShopPickup(shopPickupConfig != null ? shopPickupConfig.getStatus() : NO);

        return settings;
    }

    @Override
    public <T> T getSettings(Class<T> clazz) {
        return instantiateAndPopulate(clazz);
    }

    private <T> T instantiateAndPopulate(Class<T> clazz) {
        T instance = createInstance(clazz);

        ReflectionUtils.doWithFields(clazz, field -> {
            ReflectionUtils.makeAccessible(field);

            // 内嵌处理嵌套配置
            if (field.isAnnotationPresent(ConfigInnerParam.class)) {
                Object innerValue = instantiateAndPopulate(field.getType());
                setFieldValue(instance, field, innerValue, "无法设置内部参数");
                return;
            }

            // 处理普通配置项
            ConfigItemField annotation = field.getAnnotation(ConfigItemField.class);
            if (annotation != null) {
                Object value = settingsHelper.get(annotation.value());
                setFieldValue(instance, field, value, "设置字段值失败");
            }
        });

        return instance;
    }

    private <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new GlobalException("无法创建对象实例: " + clazz.getSimpleName());
        }
    }

    private void setFieldValue(Object target, Field field, Object value, String errorMsg) {
        try {
            ReflectionUtils.setField(field, target, value);
        } catch (Exception e) {
            throw new GlobalException(field.getName() + "，" + errorMsg);
        }
    }

    @Transactional
    @Override
    public void saveConfigSettings(ConfigSaveParam param) {
        List<ConfigPO> configs = extractConfigs(param);

        configs.stream()
                .filter(config -> config.getBizVal() != null)
                .forEach(settingsHelper::set);
    }

    @Override
    public void sendEmail(RegisterEmailDTO dto) {
        if (!Validator.isEmail(dto.getEmail())) {
            throw new GlobalException("邮箱地址不正确");
        }
        // 验证验证码是否正确
        if (!captchaHandler.validateCaptcha(dto.getVerifyToken())) {
            // 如果验证码错误，抛出异常
            throw new GlobalException("获取验证码失败，需要先进行行为验证");
        }
        // 随机生成验证码
        int code = RandomUtil.randomInt(100000, 999999);
        // 初始化短信模板
        MailTemplates mailTemplates = mailTemplatesService.getOne(new LambdaQueryWrapper<MailTemplates>()
                .eq(MailTemplates::getTemplateCode, MailTemplateType.REGISTER_CODE.getCode()));

        String templateContent = mailTemplates.getTemplateContent();
        // 发送短信
        MailUtil.send(loadEmailConfig(), dto.getEmail(), mailTemplates.getTemplateSubject(), templateContent.replace("{$code}", String.valueOf(code)), true);
        //存入redis
        redisTemplate.opsForValue().set(dto.getEvent() + dto.getEmail(), String.valueOf(code), 5, TimeUnit.MINUTES);
    }

    @Override
    public VendorSettingsVO getVendorSettings() {
        return this.getSettings(VendorSettingsVO.class);
    }

    @Override
    @Transactional
    public void saveVendor(VendorSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public GetLoginProtocolVO getLoginProtocol() {
        return this.getSettings(GetLoginProtocolVO.class);
    }

    @Override
    @Transactional
    public void saveLoginProtocol(LoginProtocolParam param) {
        switch (SettingsEnum.fromKey(param.getCode())) {
            case TERMS_OF_SERVICE:
                settingsHelper.set(TERMS_OF_SERVICE_SHOW, param.getShow());
                settingsHelper.set(TERMS_OF_SERVICE, param.getContent());
                break;
            case PRIVACY_POLICY:
                settingsHelper.set(PRIVACY_POLICY_SHOW, param.getShow());
                settingsHelper.set(PRIVACY_POLICY, param.getContent());
                break;
            case AFTER_SALES_SERVICE:
                settingsHelper.set(AFTER_SALES_SERVICE_SHOW, param.getShow());
                settingsHelper.set(AFTER_SALES_SERVICE, param.getContent());
                break;
        }
    }

    @Override
    public GetLoginProtocolContentVO getLoginProtocolContentVO(String configCode) {
        switch (SettingsEnum.fromKey(configCode)) {
            case TERMS_OF_SERVICE:
                Integer termsOfServiceShow = settingsHelper.get(TERMS_OF_SERVICE_SHOW);
                String termsOfService = settingsHelper.get(TERMS_OF_SERVICE);
                return new GetLoginProtocolContentVO(termsOfServiceShow, termsOfService);
            case PRIVACY_POLICY:
                Integer privacyPolicyShow = settingsHelper.get(PRIVACY_POLICY_SHOW);
                String privacyPolicy = settingsHelper.get(PRIVACY_POLICY);
                return new GetLoginProtocolContentVO(privacyPolicyShow, privacyPolicy);
            case AFTER_SALES_SERVICE:
                Integer afterSalesServiceShow = settingsHelper.get(AFTER_SALES_SERVICE_SHOW);
                String afterSalesService = settingsHelper.get(AFTER_SALES_SERVICE);
                return new GetLoginProtocolContentVO(afterSalesServiceShow, afterSalesService);
            default:
                return null;
        }
    }

    @Override
    public ProfitSharingSettingsVO getProfitSharingSettings() {
        return this.getSettings(ProfitSharingSettingsVO.class);
    }

    @Override
    @Transactional
    public void saveProfitSharing(ProfitSharingSaveParam param) {
        applicationContext.getBean(ConfigService.class).saveConfigSettings(param);
    }

    @Override
    public WithdrawalSettingsVO getWithdrawalSettings() {
        return this.getSettings(WithdrawalSettingsVO.class);
    }

    @Override
    @Transactional
    public void saveWithdrawal(WithdrawalSaveParam param) {
        applicationContext.getBean(ConfigService.class).saveConfigSettings(param);
    }

    @Override
    public SearchSettingsVO getSearchSettings() {
        SearchSettingsVO searchSettings = new SearchSettingsVO();

        // 获取搜索类型配置
        String searchType = getConfigValue(ConfigConstants.SEARCH, "selectSearchType");
        if (StrUtil.isNotBlank(searchType)) {
            searchSettings.setSelectSearchType(searchType);
        }

        // 检查ES连接状态
        try {
            Map<String, Object> response = productEsService.checkConnection();
            searchSettings.setEsConnected((Boolean) response.get("connected"));
        } catch (Exception e) {
            searchSettings.setEsConnected(false);
        }

        return searchSettings;
    }

    @Override
    public void saveSearch(SearchSaveParam param) {
        Map<String, Object> searchConfig = new HashMap<>();
        searchConfig.put("selectSearchType", param.getSelectSearchType());

        // 直接保存配置
        LambdaQueryWrapper<ConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConfigPO::getBizCode, ConfigConstants.SEARCH);
        ConfigPO config = this.getOne(queryWrapper);

        if (config == null) {
            config = new ConfigPO();
            config.setBizCode(ConfigConstants.SEARCH);
        }
        config.setBizVal(JsonUtil.toJson(searchConfig));
        this.saveOrUpdate(config);
    }

    @Override
    public Map<String, Object> checkEsConnection() {
        try {
            Map<String, Object> response = productEsService.checkConnection();
            return response;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("connected", false);
            result.put("message", "ES连接检查失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public GetStoreVO getStoreSettings() {
        return this.getSettings(GetStoreVO.class);
    }

    @Transactional
    @Override
    public void saveStore(StoreSaveParam param) {
        GetStoreVO settings = this.getSettings(GetStoreVO.class);
        this.saveConfigSettings(param);
        if (settings.getStoreIndependentGoods() == 1 && param.getStoreIndependentGoods() == 0) {
            // 下架所有自建商品
            productMapper.update(
                    new LambdaUpdateWrapper<Product>()
                            .set(Product::getProductStatus, NO)
                            .ne(Product::getShopId, 0)
            );
        }
    }

    @Override
    public AmapVO getAmapSettings(String type) {
        AmapVO amapVO = new AmapVO();
        if (type.equals("web")) {
            GetAmapVO settings = this.getSettings(GetAmapVO.class);
            BeanUtil.copyProperties(settings, amapVO);
            return amapVO;
        }
        if (type.equals("webjs")) {
            GetAmapJSVO settings = this.getSettings(GetAmapJSVO.class);
            BeanUtil.copyProperties(settings, amapVO);
            return amapVO;
        }
        if (type.equals("mini")) {
            GetAmapMiniVO settings = this.getSettings(GetAmapMiniVO.class);
            BeanUtil.copyProperties(settings, amapVO);
            return amapVO;
        }
        return null;
    }

    @Override
    @Transactional
    public void saveAmapSettings(AmapParam param) {
        String type = param.getType();
        if (type.equals("web")) {
            AmapSaveParam saveParam = new AmapSaveParam();
            BeanUtil.copyProperties(param, saveParam);
            this.saveConfigSettings(saveParam);
        }
        if (type.equals("webjs")) {
            AmapJsSaveParam saveParam = new AmapJsSaveParam();
            BeanUtil.copyProperties(param, saveParam);
            this.saveConfigSettings(saveParam);
        }
        if (type.equals("mini")) {
            AmapMiniSaveParam saveParam = new AmapMiniSaveParam();
            BeanUtil.copyProperties(param, saveParam);
            this.saveConfigSettings(saveParam);
        }
    }

    @Override
    public AmapAllVO getAllAmapSettings(Integer use) {
        AmapAllVO settings = this.getSettings(AmapAllVO.class);
        if (use == null || use == 0) {
            settings.encryptData();
        }
        return settings;
    }

    @Override
    @Transactional
    public void saveAllAmapSettings(AmapAllParam param) {
        AmapAllVO amapAllVO = this.getSettings(AmapAllVO.class);
        param.noUpdate(amapAllVO);
        this.saveConfigSettings(param);
    }

    @Override
    public void savePersonalizedSetting(PersonalizedSettingSaveParam param) {
        applicationContext.getBean(ConfigService.class).saveConfigSettings(param);
    }

    @Override
    public PersonalizedSettingVO getPersonalizedSetting() {
        return this.getSettings(PersonalizedSettingVO.class);
    }

    private List<ConfigPO> extractConfigs(Object obj) {
        List<ConfigPO> configs = new ArrayList<>();

        ReflectionUtils.doWithFields(obj.getClass(), field -> {
            ReflectionUtils.makeAccessible(field);
            Object fieldValue = ReflectionUtils.getField(field, obj);

            // 嵌套字段递归处理
            if (fieldValue != null && field.isAnnotationPresent(ConfigInnerParam.class)) {
                configs.addAll(extractConfigs(fieldValue));
                return;
            }

            // 忽略字段
            if (field.isAnnotationPresent(ConfigIgnoreField.class)) {
                return;
            }

            // 获取字段名或枚举定义的 bizCode
            String bizCode = field.getName();
            if (field.isAnnotationPresent(ConfigItemField.class)) {
                ConfigItemField annotation = field.getAnnotation(ConfigItemField.class);
                bizCode = annotation.value().getBizCode();
            }

            configs.add(new ConfigPO(bizCode, fieldValue != null ? String.valueOf(fieldValue) : null));

        });

        return configs;
    }


    @Override
    public void createPlatformCertificate() {
        // ConfigPO wechatPayMchidConfig = this.getConfigByCode(WECHAT_PAY_MCHID.getBizCode());
        // ConfigPO wechatPaySerialNoConfig = this.getConfigByCode(WECHAT_PAY_SERIAL_NO.getBizCode());
        // ConfigPO wechatPayKeyConfig = this.getConfigByCode(WECHAT_PAY_KEY.getBizCode());

        // SignUtils.sign()
        //
        // // 初始化商户配置
        // Config config =
        //         new RSAAutoCertificateConfig.Builder()
        //                 .merchantId(wechatPayMchidConfig.getBizVal())
        //                 // 使用 com.wechat.pay.java.core.util
        //                 // 中的函数从本地文件中加载商户私钥，商户私钥会用来生成请求的签名
        //                 .privateKeyFromPath(wechatPayKeyConfig.getBizVal())
        //                 .merchantSerialNumber(wechatPaySerialNoConfig.getBizVal())
        //                 .apiV3Key(apiV3Key)
        //                 .build();
        //
        // // 初始化证书服务
        // CertificateService certificateService = new CertificateService.Builder().config(config).build();
        // // 设置商户apiV3密钥，apiV3密钥用于解密下载证书
        // String certificate = certificateService.downloadCertificate(apiV3Key.getBytes(StandardCharsets.UTF_8));
        // // 将证书写入文件
        // Files.write(Paths.get(CERT_PATH), certificate.getBytes());
    }


    @Override
    public ConfigBasicVO getConfigBasic() {
        // 1. 获取父ID为0的所有地区
        List<Region> regionList = regionMapper.selectList(new LambdaQueryWrapper<Region>().eq(Region::getParentId, 0));
        // 2. 查询所有code中包含base的配置
        Map<String, Map<String, Object>> configMap = getConfigsByCode(BASE);

        // 3. 存储设置
        List<SettingsEnum> settings = List.of(
                STORAGE_TYPE, STORAGE_LOCAL_URL, STORAGE_OSS_URL,
                STORAGE_OSS_ACCESS_KEY_ID, STORAGE_OSS_ACCESS_KEY_SECRET,
                STORAGE_OSS_BUCKET, STORAGE_OSS_REGION,
                STORAGE_COS_URL, STORAGE_COS_SECRET_ID,
                STORAGE_COS_SECRET_KEY, STORAGE_COS_BUCKET,
                STORAGE_COS_REGION
        );
        Map<String, String> storageConfigMap = this.getConfigMapBySettings(settings);
        if (storageConfigMap != null) {
            ConfigStorageDTO configStorage = new ConfigStorageDTO(storageConfigMap);
            // 将密钥写回 storageConfigMap
            processStorageSecrets(configStorage, storageConfigMap);
        }

        // 4. 敏感信息混淆的配置处理
        Map<String, String> sensitiveKeys = Map.of(
                BASE_SMS, "sms_key_secret",
                BASE_API_WECHAT, "wechat_appSecret",
                BASE_API_MINI_PROGRAM, "wechat_miniProgram_secret",
                BASE_API_APP_PAY, "wechat_pay_app_secret",
                BASE_API_LANG, "lang_volcengine_secret"
        );

        // 遍历配置项，批量混淆敏感数据
        sensitiveKeys.forEach((configKey, secretKey) -> {
            Map<String, Object> config = configMap.get(configKey);
            if (config != null) {
                hideSecret(config, secretKey);
            }
        });

        return new ConfigBasicVO(regionList, configMap);
    }

    /**
     * 处理存储配置中的敏感信息
     *
     * @param configStorage    存储配置对象
     * @param storageConfigMap 原始的 map，用于将混淆后的值放回 map
     */
    private void processStorageSecrets(ConfigStorageDTO configStorage, Map<String, String> storageConfigMap) {
        if (configStorage == null || configStorage.getStorageType() == null) {
            return; // 避免空指针异常
        }
        // oss密钥混淆处理
        String ossSecret = configStorage.getStorageOssAccessKeySecret();
        String hiddenOssSecret = StringUtils.hide(ossSecret, 12, 18);
        storageConfigMap.put(STORAGE_OSS_ACCESS_KEY_SECRET.getBizCode(), hiddenOssSecret);

        // cos密钥混淆处理
        String cosSecret = configStorage.getStorageCosSecretKey();
        String hiddenCosSecret = StringUtils.hide(cosSecret, 12, 18);
        storageConfigMap.put(STORAGE_COS_SECRET_KEY.getBizCode(), hiddenCosSecret);
    }

    /**
     * 隐藏配置中的敏感信息
     *
     * @param map         配置数据
     * @param propertyKey 配置项的key
     */
    private void hideSecret(Map<String, Object> map, String propertyKey) {
        if (map == null || !map.containsKey(propertyKey)) {
            return; // 防止空指针异常
        }
        Object secretValue = map.get(propertyKey);
        if (secretValue instanceof String secret) {
            map.put(propertyKey, StringUtils.hide(secret, 12, 18));
        }
    }


    @Override
    public ConfigPO getConfigByCode(String code) {
        if (StrUtil.isEmpty(code)) {
            throw new GlobalException(CODE_PARAM_ERROR);
        }

        ConfigPO configPO = this.lambdaQuery().eq(ConfigPO::getBizCode, code).eq(ConfigPO::getIsDel, 0).one();
        if (configPO == null) {
            throw new GlobalException(CONFIG_NOT_EXIST);
        }

        return configPO;
    }

    @Override
    public void sendTestMail(String testMailAddress) {
        MailUtil.send(loadEmailConfig(), testMailAddress, "测试邮件", "这是一封测试邮件，收到此邮件代表着您的邮箱服务器设置正确！", false);
    }

    /**
     * 加载邮件配置
     */
    private MailAccount loadEmailConfig() {

        // 创建邮件账户对象
        MailAccount account = new MailAccount();
        account.setHost(this.getConfigVal(SMTP_HOST));
        account.setPort(Integer.parseInt(this.getConfigVal(SMTP_PORT)));
        account.setAuth(true);
        account.setFrom(this.getConfigVal(SMTP_MAIL));
        account.setUser(this.getConfigVal(SMTP_USER));
        account.setPass(this.getConfigVal(SMTP_PASS));
        account.setSslEnable(true);
        return account;
    }

    @Override
    public Map<String, Object> getBase(Integer previewId) {

        // 主题id
        JSONObject themeStyleJsonObject = JSONUtil.parseObj(this.getConfigVal(THEME_STYLE));
        int themeId = themeStyleJsonObject.getInt("themeId");
        String shopName = translatePackage.translate(this.getConfigVal(SHOP_NAME), SETTING.getCode());
        String shopTitle = translatePackage.translate(this.getConfigVal(SHOP_TITLE), SETTING.getCode());
        String shopTitleSuffix = translatePackage.translate(this.getConfigVal(SHOP_TITLE_SUFFIX), SETTING.getCode());
        String shopLogo = this.getConfigVal(SHOP_LOGO);
        String shopKeywords = translatePackage.translate(this.getConfigVal(SHOP_KEYWORDS), SETTING.getCode());
        String shopDesc = translatePackage.translate(this.getConfigVal(SHOP_DESC), SETTING.getCode());
        String icoImg = this.getConfigVal(ICO_IMG);

        String autoRedirectVal = this.getConfigVal(AUTO_REDIRECT);
        int autoRedirect = StrUtil.isBlank(autoRedirectVal) ? 1 : Integer.parseInt(autoRedirectVal);

        String h5Domain = this.getConfigVal(H5_DOMAIN);
        String pcDomain = this.getConfigVal(PC_DOMAIN);
        String adminDomain = this.getConfigVal(ADMIN_DOMAIN);
        String shopIcpNo = translatePackage.translate(this.getConfigVal(SHOP_ICP_NO), SETTING.getCode());

        String shopIcpNoUrlVal = this.getConfigVal(SHOP_ICP_NO_URL);
        String shopIcpNoUrl = StrUtil.isBlank(shopIcpNoUrlVal) ? "" : "https://beian.miit.gov.cn";

        String shop110No = this.getConfigVal(SHOP_110_NO);

        String shopIcpNoLinkVal = this.getConfigVal(SHOP_110_LINK);
        String shop110Link = StrUtil.isBlank(shopIcpNoLinkVal) ? "" : "https://beian.mps.gov.cn/#/query/webSearch";

        String companyAddress = this.getConfigVal(COMPANY_ADDRESS);

        String isOpenMobileAreaCodeVal = this.getConfigVal(IS_OPEN_MOBILE_AREA_CODE);
        int isOpenMobileAreaCode = StrUtil.isBlank(isOpenMobileAreaCodeVal) ? 0 : Integer.parseInt(isOpenMobileAreaCodeVal);

        String closeOrderVal = this.getConfigVal(CLOSE_ORDER);
        int closeOrder = StrUtil.isBlank(closeOrderVal) ? 0 : Integer.parseInt(closeOrderVal);

        String shopRegClosedVal = this.getConfigVal(SHOP_REG_CLOSED);
        int shopRegClosed = StrUtil.isBlank(shopRegClosedVal) ? 0 : Integer.parseInt(shopRegClosedVal);

        String isIdentityVal = this.getConfigVal(IS_IDENTITY);
        int isIdentity = StrUtil.isBlank(isIdentityVal) ? 0 : Integer.parseInt(isIdentityVal);

        String isEnquiryVal = this.getConfigVal(IS_ENQUIRY);
        int isEnquiry = StrUtil.isBlank(isEnquiryVal) ? 0 : Integer.parseInt(isEnquiryVal);

        // 获取存储地址
        String storageUrl = getStorageUrl();

        String dollarSignVal = this.getConfigVal(DOLLAR_SIGN);
        String dollarSign = StrUtil.isBlank(dollarSignVal) ? "¥" : dollarSignVal;

        String dollarSignCnVal = this.getConfigVal(DOLLAR_SIGN_CN);
        String dollarSignCn = StrUtil.isBlank(dollarSignCnVal) ? "元" : dollarSignCnVal;

        String showSelledCountVal = this.getConfigVal(SHOW_SELLED_COUNT);
        int showSelledCount = StrUtil.isBlank(showSelledCountVal) ? 1 : Integer.parseInt(showSelledCountVal);

        String showMarketpriceVal = this.getConfigVal(SHOW_MARKETPRICE);
        int showMarketprice = StrUtil.isBlank(showMarketpriceVal) ? 1 : Integer.parseInt(showMarketpriceVal);

        // 微信配置
        String openWechatOauth = this.getConfigVal(OPEN_WECHAT_OAUTH);
        // 商户设置
        String personApplyEnabled = this.getConfigVal(PERSON_APPLY_ENABLED);

        // 客服设置
        int show_service = Integer.parseInt(this.getConfigVal(KEFU_TYPE)) > 0 ? 1 : 0;
        String kefuPhone = this.getConfigVal(KEFU_PHONE);
        String kefuTime = this.getConfigVal(KEFU_TIME);

        // 声明
        String shopCompany = this.getConfigVal(SHOP_COMPANY);
        String poweredByStatus = this.getConfigVal(POWERED_BY_STATUS);
        String poweredBy = this.getConfigVal(POWERED_BY);
        // 自动生成许可数据
        String isEnterprise = this.getConfigVal(IS_ENTERPRISE);
        String deCopyright = this.getConfigVal(DE_COPYRIGHT);

        // 基础产品类别装修
        Integer categoryDecorateType = Integer.parseInt(this.getConfigVal(PRODUCT_CATEGORY_DECORATE_TYPE));

        // 购物信息
        String canInvoice = this.getConfigVal(CAN_INVOICE);
        String invoiceAdded = this.getConfigVal(INVOICE_ADDED);

        // 店铺信息
        String defaultShopName = this.getConfigVal(DEFAULT_SHOP_NAME);
        // 支付信息
        String useSurplus = this.getConfigVal(USE_SURPLUS);
        String usePoints = this.getConfigVal(USE_POINTS);
        String useCoupon = this.getConfigVal(USE_COUPON);

        // 基础API公司数据
        String typeVal = this.getConfigVal(TYPE);
        int companyDataType = StrUtil.isBlank(typeVal) ? 2 : Integer.parseInt(typeVal);
        String companyDataTips = this.getConfigVal(TIPS);
        String version_type = tigshopProperties.getVersion();

        // 商品搜索设置
        String enableAttributeFilter = this.getConfigVal(ENABLE_ATTRIBUTE_FILTER);

        // 存进来
        Map<String, Object> propertiesMap = new HashMap<>();

        // 添加 themeId
        propertiesMap.put("themeStyle", themeStyleJsonObject);
        propertiesMap.put("themeId", themeId);

        // 添加 商城配置
        propertiesMap.put("shopName", shopName);
        propertiesMap.put("shopTitle", shopTitle);
        propertiesMap.put("shopTitleSuffix", shopTitleSuffix);
        propertiesMap.put("shopLogo", shopLogo);
        propertiesMap.put("shopKeywords", shopKeywords);
        propertiesMap.put("shopDesc", shopDesc);
        propertiesMap.put("icoImg", icoImg);
        propertiesMap.put("autoRedirect", autoRedirect);
        propertiesMap.put("h5Domain", h5Domain);
        propertiesMap.put("pcDomain", pcDomain);
        propertiesMap.put("adminDomain", adminDomain);
        propertiesMap.put("shopIcpNo", shopIcpNo);
        propertiesMap.put("shopIcpNoUrl", shopIcpNoUrl);
        propertiesMap.put("shop110No", shop110No);
        propertiesMap.put("shop110Link", shop110Link);
        propertiesMap.put("companyAddress", companyAddress);
        propertiesMap.put("isOpenMobileAreaCode", isOpenMobileAreaCode);
        propertiesMap.put("closeOrder", closeOrder);
        propertiesMap.put("shopRegClosed", shopRegClosed);
        propertiesMap.put("isIdentity", isIdentity);
        propertiesMap.put("isEnquiry", isEnquiry);

        // 获取存储地址
        propertiesMap.put("storageUrl", storageUrl);

        // 商品配置
        propertiesMap.put("dollarSign", dollarSign);
        propertiesMap.put("dollarSignCn", dollarSignCn);
        propertiesMap.put("showSelledCount", showSelledCount);
        propertiesMap.put("showMarketprice", showMarketprice);

        // 微信配置
        propertiesMap.put("openWechatOauth", openWechatOauth);

        // 商户设置
        propertiesMap.put("personApplyEnabled", personApplyEnabled);

        // 客服设置
        propertiesMap.put("showService", show_service);
        propertiesMap.put("kefuPhone", kefuPhone);
        propertiesMap.put("kefuTime", kefuTime);

        // 声明
        propertiesMap.put("shopCompany", shopCompany);
        propertiesMap.put("poweredByStatus", poweredByStatus);
        propertiesMap.put("poweredBy", poweredBy);

        // 自动生成许可数据
        propertiesMap.put("isEnterprise", isEnterprise);
        propertiesMap.put("deCopyright", deCopyright);

        // 基础产品类别装修
        propertiesMap.put("categoryDecorateType", categoryDecorateType);

        // 购物信息
        UserRankConfig growConfig = userRankConfigMapper.selectOne(new LambdaQueryWrapper<UserRankConfig>().eq(UserRankConfig::getCode, "grow_config"));
        propertiesMap.put("growUpSetting", JSONUtil.parse(growConfig.getData()));
        propertiesMap.put("canInvoice", canInvoice);
        propertiesMap.put("invoiceAdded", invoiceAdded);

        // 店铺信息
        propertiesMap.put("defaultShopName", defaultShopName);

        // 支付信息
        propertiesMap.put("useSurplus", useSurplus);
        propertiesMap.put("usePoints", usePoints);
        propertiesMap.put("useCoupon", useCoupon);

        // 基础API公司数据
        propertiesMap.put("companyDataType", companyDataType);
        propertiesMap.put("companyDataTips", companyDataTips);
        propertiesMap.put("versionType", version_type);

        shopCompany = propertiesMap.containsKey("deCopyright") ? propertiesMap.get("shopCompany").toString() : DEFAULT_COMPANY.getBizDesc();
        propertiesMap.put("shopCompany", shopCompany);
        propertiesMap.put("decoratePageConfig", decorateService.getPcIndexDecoratePageConfig(previewId));

        propertiesMap.put("enableAttributeFilter", enableAttributeFilter);
        return propertiesMap;
    }

    @Override
    public boolean sendSms(RegisterSmsDTO dto) {
        // 验证验证码是否正确
        if (!captchaHandler.validateCaptcha(dto.getVerifyToken())) {
            // 如果验证码错误，抛出异常
            throw new GlobalException("获取验证码失败，需要先进行行为验证");
        }
        // 随机生成验证码
        int code = RandomUtil.randomInt(100000, 999999);
        // 初始化短信模板
        MessageTemplate messageTemplate = messageTemplateService.buildMessageTemplate(MSG_CODE.getMessageId(), MESSAGE_TYPE_MSG.getCode());

        // 构造短信模板参数
        String templateParam = "{\"code\":\"" + code + "\"}";
        // 发送短信
        boolean isSend = sendSms(dto, messageTemplate.getTemplateId(), templateParam);
        if (isSend) {
            //存入redis
            redisTemplate.opsForValue().set(dto.getEvent() + dto.getMobile(), String.valueOf(code), 5, TimeUnit.MINUTES);
        }
        return isSend;
    }

    @Override
    public String sendCode(RegisterSmsDTO dto) {
        // 随机生成验证码
        int code = RandomUtil.randomInt(100000, 999999);
        // 初始化短信模板
        MessageTemplate messageTemplate = messageTemplateService.buildMessageTemplate(MSG_CODE.getMessageId(), MESSAGE_TYPE_MSG.getCode());

        String templateParam = "{\"code\":\"" + code + "\"}";
        boolean isSend = sendSms(dto, messageTemplate.getTemplateId(), templateParam);
        if (isSend) {
            //存入redis
            redisTemplate.opsForValue().set(dto.getEvent() + dto.getMobile(), String.valueOf(code), 5, TimeUnit.MINUTES);
        }

        return String.valueOf(code);
    }

    @Override
    public boolean sendSms(RegisterSmsDTO dto, String templateId, Map<String, Object> templateParam) {
        String templateParamStr = JSON.toJSONString(templateParam);
        return this.sendSms(dto, templateId, templateParamStr);
    }

    @Override
    public boolean sendSms(RegisterSmsDTO dto, String templateId, String templateParam) {
        List<SettingsEnum> settings = List.of(SMS_KEY_ID, SMS_KEY_SECRET, SMS_SIGN_NAME);
        Map<String, String> configMap = this.getConfigMapBySettings(settings);


        // 获取相关配置
        SmsSendDTO smsSend = new SmsSendDTO(configMap.get(SMS_KEY_ID.getBizCode()),
                configMap.get(SMS_KEY_SECRET.getBizCode()),
                dto.getMobile(),
                configMap.get(SMS_SIGN_NAME.getBizCode()),
                templateId,
                templateParam);
        System.out.println("发送短信消息到 RabbitMQ：" + smsSend);
        // 转json
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.SEND_SMS_ROUTING_KEY, smsSend);
        System.out.println("发送短信消息到 RabbitMQ：" + new Date());
        return true;
    }

    @Override
    public Map<String, String> getConfigMapBySettings(List<SettingsEnum> settings) {
        if (CollUtil.isEmpty(settings)) {
            return Collections.emptyMap();
        }

        List<String> bizCods = settings.stream().map(SettingsEnum::getBizCode).toList();
        List<ConfigPO> configs = this.lambdaQuery().in(ConfigPO::getBizCode, bizCods).list();

        return configs.stream().collect(Collectors.toMap(ConfigPO::getBizCode, ConfigPO::getBizVal));
    }

    @Override
    public WechatLoginUrlVO getWechatLoginUrl(String redirectUrl) {
        String clientType = HeaderUtils.getClientType();

        switch (clientType) {
            case "wechat" -> {
                log.error(redirectUrl);
                WxMpDefaultConfigImpl configStorage = new WxMpDefaultConfigImpl();
                configStorage.setAppId(this.getConfigVal(SettingsEnum.WECHAT_APP_ID));
                configStorage.setSecret(this.getConfigVal(SettingsEnum.WECHAT_APP_SECRET));
                configStorage.setToken(this.getConfigVal(SettingsEnum.WECHAT_SERVER_TOKEN));
                configStorage.setAesKey(this.getConfigVal(SettingsEnum.WECHAT_SERVER_SECRET));
                WxMpService service = new WxMpServiceImpl();
                service.setWxMpConfigStorage(configStorage);
                // 1. 生成随机 state
                String state = UUID.randomUUID().toString().replace("-", "");
                redisTemplate.opsForValue().set("wechat::state:" + state, "valid", 300);
                String url = service.getOAuth2Service().buildAuthorizationUrl(
                        redirectUrl,
                        WxConsts.OAuth2Scope.SNSAPI_USERINFO,
                        state
                );
                return new WechatLoginUrlVO(url);
            }
            case "pc" -> {
                // 获取微信配置
                String appId = this.getConfigVal(SettingsEnum.WECHAT_APP_ID);
                String appSecret = this.getConfigVal(SettingsEnum.WECHAT_APP_SECRET);
                // 获取token
                GetTokenResult tokenResult = wechatApiClient.getToken("client_credential", appId, appSecret);
                String accessToken = tokenResult.getAccessToken();
                // 获取小程序二维码
                CreateQrcodeParam createQrcodeParam = CreateQrcodeParam.builder()
                        .expireSeconds(300)
                        .actionName("QR_STR_SCENE")
                        .actionInfo(
                                CreateQrcodeParam.ActionInfo.builder()
                                        .scene(Map.of("scene_id", RandomUtil.randomInt(0, 10000), "scene_str", RandomUtil.randomString(12)))
                                        .build()
                        )
                        .build();
                CreateQrcodeResult createQrcodeResult = wechatApiClient.qrcodeCreate(accessToken, createQrcodeParam);
                return new WechatLoginUrlVO(createQrcodeResult.getUrl(), createQrcodeResult.getTicket());
            }
            default -> throw new GlobalException("不支持的终端类型");
        }
    }

    @Override
    public InitConfigSettingsVO getInitConfigSettings(Integer previewId) {
        InitConfigSettingsVO settings = this.getSettings(InitConfigSettingsVO.class);

        String storageUrl = getStorageUrl();

        UserRankConfig growConfig = userRankConfigMapper.selectOne(
                Wrappers.lambdaQuery(UserRankConfig.class).eq(UserRankConfig::getCode, "grow_config")
        );

        String decorateData = "";
        Integer headerStyle = 1;
        if (previewId != null) {
            Decorate decorate = decorateService.lambdaQuery()
                    .eq(Decorate::getDecorateId, previewId)
                    .one();
            if (decorate != null) {
                decorateData = decorate.getData();
            }
        } else {
            Decorate decorate = decorateService.lambdaQuery()
                    .eq(Decorate::getShopId, 0)
                    .eq(Decorate::getDecorateType, DecorateType.PC.getCode())
                    .eq(Decorate::getIsHome, 1)
                    .eq(Decorate::getStatus, 1)
                    .one();
            if (decorate != null) {
                decorateData = decorate.getData();
            }
            try {
                headerStyle = JSONUtil.parseObj(decorateData).getJSONObject("pageModule").getInt("headerStyle");
            } catch (Exception e) {
                log.debug(e.getMessage());
            }
        }

        settings.setVersion(tigshopProperties.getVersionNum());
        settings.assembleData(storageUrl, growConfig, JSONUtil.parseObj(decorateData).get("pageModule").toString(), tigshopProperties.getVersion(), headerStyle);
        return settings;
    }

    @Override
    public ThemeSettingsVO getThemeSettings() {
        return this.getSettings(ThemeSettingsVO.class);
    }

    @Override
    public IconSettingsVO getIcon() {
        List<String> icoTig = new ArrayList<>();
        String tigClass = "";
        List<String> icoDefined = new ArrayList<>();
        String definedClass = "";

        String data = redisTemplate.opsForValue().get(icoTigCss);

        if (data == null) {
            String content = HttpUtil.get(icoTigCss);
            if (content != null) {
                icoTig = extractIcons(content);
                tigClass = extractFontFamily(content);
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("ico_tig", icoTig);
                dataMap.put("tig_class", tigClass);
                redisTemplate.opsForValue().set(icoTigCss, JSON.toJSONString(dataMap));
            }
        } else {
            Map<String, Object> dataMap = (Map<String, Object>) JSON.parse(data);
            icoTig = (List<String>) dataMap.get("ico_tig");
            tigClass = (String) dataMap.get("tig_class");
        }

        // 自定义 ico
        ConfigPO icoDefinedCssConfig = this.getConfigByCode(ICO_DEFINED_CSS.getBizCode());
        if (icoDefinedCssConfig != null && icoDefinedCssConfig.getBizVal().startsWith("http") && icoDefinedCssConfig.getBizVal().endsWith(".css")) {
            String definedData = redisTemplate.opsForValue().get(icoDefinedCssConfig.getBizVal());
            if (definedData == null) {
                String content = HttpUtil.get(icoDefinedCssConfig.getBizVal());
                if (content != null) {
                    icoDefined = extractIcons(content);
                    definedClass = extractFontFamily(content);
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("ico_defined", icoDefined);
                    dataMap.put("defined_class", definedClass);
                    redisTemplate.opsForValue().set(icoDefinedCssConfig.getBizVal(), JSON.toJSONString(dataMap));
                }
            } else {
                Map<String, Object> dataMap = (Map<String, Object>) JSON.parse(definedData);
                icoDefined = (List<String>) dataMap.get("ico_defined");
                definedClass = (String) dataMap.get("defined_class");
            }
        }

        return new IconSettingsVO(icoTig, tigClass, icoDefined, definedClass);
    }

    private List<String> extractIcons(String content) {
        List<String> icons = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\.(.*?)\\:before").matcher(content);
        while (matcher.find()) {
            icons.add(matcher.group(1));
        }
        return icons;
    }

    private String extractFontFamily(String content) {
        Matcher matcher = Pattern.compile("font-family:\\s*\"([^\"]+)\";").matcher(content);
        return matcher.find() ? matcher.group(1) : "";
    }

    @Override
    public String getConfigVal(SettingsEnum setting) {

        ConfigPO config = this.lambdaQuery().eq(ConfigPO::getBizCode, setting.getBizCode()).one();

        return config != null ? config.getBizVal() : "";
    }

    @Override
    public Map<String, Object> getQuickLoginSetting() {
        int wechatLogin = 0;
        String headerValue = HeaderUtils.getHeaderValue(X_CLIENT_TYPE);
        if (StrUtil.isNotBlank(headerValue)) {
            switch (headerValue) {
                case "wechat", "pc" -> wechatLogin = Integer.parseInt(getConfigVal(OPEN_WECHAT_OAUTH));
                case "miniProgram" -> wechatLogin = 1;
            }
        }
        int showOauth = wechatLogin;
        return Map.of(
                "wechatLogin", wechatLogin,
                "showOauth", showOauth
        );
    }

    @Override
    public ConfigCustomerDTO getConfigCustomer() {
        ConfigCustomerDTO configCustomerDTO = new ConfigCustomerDTO();

        String h5Domain = this.getConfigVal(H5_DOMAIN);
        String corpId = "";
        String url = "";
        // 服务类型
        String serviceType = this.getConfigVal(KEFU_TYPE);
        // 公开类型
        String openType = this.getConfigVal(KEFU_YZF_TYPE);
        switch (serviceType) {
            case "1" -> {
                String cmsSign = this.getConfigVal(KEFU_YZF_SIGN);
                corpId = this.getConfigVal(CORP_ID);
                url = StrUtil.format("{}{}", TENCENT_CUSTOMER_SERVICE_URL, cmsSign);
            }
            case "2" -> {
                String cmsWorkWechatId = this.getConfigVal(KEFU_WORKWX_ID);
                corpId = this.getConfigVal(CORP_ID);
                url = StrUtil.format("{}{}", QY_CUSTOMER_SERVICE_URL, cmsWorkWechatId);
            }
            case "3" -> url = this.getConfigVal(KEFU_CODE);
        }
        configCustomerDTO.setCorpId(corpId);
        configCustomerDTO.setUrl(url);
        configCustomerDTO.setOpenType(Integer.parseInt(openType));
        int serviceTypeValue = Integer.parseInt(serviceType);
        configCustomerDTO.setServiceType(serviceTypeValue);
        configCustomerDTO.setShow(serviceTypeValue > 0 ? 1 : 0);
        configCustomerDTO.setH5Domain(h5Domain);
        return configCustomerDTO;
    }

    /**
     * 根据code获取多个配置（主要用于返回多个base配置）
     *
     * @param code code
     * @return Map
     */
    public Map<String, Map<String, Object>> getConfigsByCode(String code) {
        LambdaQueryWrapper<ConfigPO> queryWrapper = Wrappers.lambdaQuery();
        if (code == null) {
            throw new GlobalException(PARAM_ERROR);
        }
        queryWrapper.like(ConfigPO::getBizCode, code);
        List<ConfigPO> configPOList = this.list(queryWrapper);
        return getConfigMap(configPOList);
    }

    /**
     * 将配置数据转换为Map
     *
     * @param configPOList 配置数据列表
     * @return Map
     */
    public Map<String, Map<String, Object>> getConfigMap(List<ConfigPO> configPOList) {
        Map<String, Map<String, Object>> configMap = new HashMap<>();

        // 遍历并处理每条记录
        for (ConfigPO configPO : configPOList) {
            String code = configPO.getBizCode();
            String data = configPO.getBizVal();

            // 将 JSON 字符串 data 转换为 Map<String, Object>
            Map<String, Object> dataMap = JsonUtil.jsonToMap(data);
            configMap.put(code, dataMap);
        }
        return configMap;
    }

    /**
     * 获取存储URL
     */
    public String getStorageUrl() {
        // 获取里面的存储类型，以确定前端需要哪些储存配置
        String storageTypeVal = this.getConfigVal(STORAGE_TYPE);
        // 获取存储类型对应的URL
        StorageType storageTypeEnum = StorageType.fromTypeCode(storageTypeVal);
        // 获取存储URL
        return this.getConfigVal(storageTypeEnum.getSettingsEnum());
    }



    public String getConfigValue(String configCode, String key) {
        try {
            Map<String, Object> configData = getConfigData(configCode);
            if (configData != null && configData.containsKey(key)) {
                return configData.get(key).toString();
            }
            return null;
        } catch (Exception e) {
            log.error("获取配置值失败: configCode={}, key={}", configCode, key, e);
            return null;
        }
    }

    /**
     * 获取配置数据
     */
    private Map<String, Object> getConfigData(String configCode) {
        LambdaQueryWrapper<ConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConfigPO::getBizCode, configCode);
        ConfigPO config = this.getOne(queryWrapper);

        if (config != null && StrUtil.isNotBlank(config.getBizVal())) {
            return JsonUtil.jsonToMap(config.getBizVal());
        }
        return null;
    }

}
