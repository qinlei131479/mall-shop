// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.merchant.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.authority.AdminUserDTO;
import com.tigshop.bean.dto.login.RegisterSmsDTO;
import com.tigshop.bean.dto.merchant.MerchantApplyCreateDTO;
import com.tigshop.bean.dto.merchant.MerchantApplyUpdateDTO;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.enums.merchant.MerchantApplyStatusEnum;
import com.tigshop.bean.enums.merchant.MerchantApplyTypeEnum;
import com.tigshop.bean.enums.merchant.MerchantStatus;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import com.tigshop.bean.enums.setting.MessageTemplateType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.model.merchant.MerchantAccount;
import com.tigshop.bean.model.merchant.MerchantApply;
import com.tigshop.bean.model.merchant.MerchantUser;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.setting.MessageTemplate;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.param.merchant.MerchantApplyApplyParam;
import com.tigshop.bean.param.merchant.MerchantApplyAuditParam;
import com.tigshop.bean.query.merchant.MerchantApplyListPageQuery;
import com.tigshop.bean.vo.merchant.MerchantApplyDetailVO;
import com.tigshop.bean.vo.merchant.MerchantApplyVO;
import com.tigshop.bean.vo.merchant.MerchantMyApplyVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.merchant.MerchantApplyMapper;
import com.tigshop.mapper.shop.AdminUserShopMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.authority.AdminRoleService;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.decorate.DecorateService;
import com.tigshop.service.merchant.MerchantAccountService;
import com.tigshop.service.merchant.MerchantApplyService;
import com.tigshop.service.merchant.MerchantService;
import com.tigshop.service.merchant.MerchantUserService;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.o2o.ShopPickupConfigService;
import com.tigshop.service.o2o.ShopPickupTplService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.MessageTemplateService;
import com.tigshop.service.setting.RegionService;
import com.tigshop.service.setting.ShippingTplService;
import com.tigshop.service.shop.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.ExceptionConstants.MERCHANT_APPLY_AUDIT_STATUS_ERROR;
import static com.tigshop.common.constant.ExceptionConstants.NOT_FOUND_ERROR;
import static com.tigshop.common.constant.authority.AdminUserConstants.*;
import static com.tigshop.common.constant.merchant.MerchantApplyConstants.MERCHANT_APPLY_NOT_EXIST;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 入驻申请服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Service
public class MerchantApplyServiceImpl extends BaseServiceImpl<MerchantApplyMapper, MerchantApply> implements MerchantApplyService {

    private final UserMapper userMapper;

    private final AdminUserService adminUserService;

    private final MerchantService merchantService;

    private final AdminLogService adminLogService;

    private final MerchantUserService merchantUserService;

    private final ShopService shopService;

    private final AdminUserShopMapper adminUserShopMapper;

    private final MerchantAccountService merchantAccountService;

    private final ShippingTplService shippingTplService;

    private final DecorateService decorateService;

    private final AdminRoleService adminRoleService;

    private final ProductService productService;

    private final RegionService regionService;

    private final ConfigService configService;

    private final AdminMsgService adminMsgService;

    private final TranslatePackageImpl translatePackage;

    private final MessageTemplateService messageTemplateService;
    private final TigshopProperties tigshopProperties;
    private final ShopPickupConfigService shopPickupConfigService;
    private final ShopPickupTplService shopPickupTplService;

    @Override
    public Page<MerchantApplyVO> list(MerchantApplyListPageQuery pageQuery) {
        // 分页
        Page<MerchantApply> page = buildSortOrder(pageQuery);

        List<Integer> matchingUserIds = Collections.emptyList();
        if (StringUtils.isNotEmpty(pageQuery.getUsername())) {
            // 去user表查询会员名称
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getUsername, pageQuery.getUsername()));
            matchingUserIds = users.stream().
                    map(User::getUserId).
                    collect(Collectors.toList());
        }

        // 构造查询构造器
        LambdaQueryWrapper<MerchantApply> queryWrapper = new LambdaQueryWrapper<MerchantApply>()
                .eq(pageQuery.getStatus() != null && pageQuery.getStatus() > -1, MerchantApply::getStatus, pageQuery.getStatus())
                .in(CollUtil.isNotEmpty(matchingUserIds), MerchantApply::getUserId, matchingUserIds);

        // 执行查询
        Page<MerchantApply> merchantApplyPage = this.page(page, queryWrapper);

        // 获取查询结果
        List<MerchantApply> merchantApplyPageRecords = merchantApplyPage.getRecords();
        if (CollUtil.isEmpty(merchantApplyPageRecords)) {
            return PageUtil.convertPage(merchantApplyPage, List.of());
        }

        //获取查询出的所有userIds
        List<Integer> userIds = merchantApplyPageRecords.stream()
                .map(MerchantApply::getUserId)
                .distinct()
                .toList();

        List<User> users = userMapper.selectBatchIds(userIds);

        //将user信息存入map
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getUserId, Function.identity()));

        // 转换为VO
        List<MerchantApplyVO> merchantApplyVOList = merchantApplyPageRecords.stream()
                .map(merchantApply -> {
                    MerchantApplyVO merchantApplyVO = new MerchantApplyVO();
                    BeanUtils.copyProperties(merchantApply, merchantApplyVO);
                    merchantApplyVO.setUser(userMap.get(merchantApply.getUserId()));
                    merchantApplyVO.setStatusText(MerchantApplyStatusEnum.getStatusName(merchantApply.getStatus()));
                    merchantApplyVO.setBaseData(merchantApply.getBaseData() != null ?
                            JsonUtil.fromJson(merchantApply.getBaseData(), JSONObject.class) : null);
                    merchantApplyVO.setShopData(merchantApply.getShopData() != null ?
                            JsonUtil.fromJson(merchantApply.getShopData(), JSONObject.class) : null);
                    merchantApplyVO.setMerchantData(merchantApply.getMerchantData() != null ?
                            JsonUtil.fromJson(merchantApply.getMerchantData(), JSONObject.class) : null);
                    merchantApplyVO.setAddTime(TigUtils.handelTime(merchantApply.getAddTime()));
                    merchantApplyVO.setAuditTime(TigUtils.handelTime(merchantApply.getAuditTime()));
                    return merchantApplyVO;
                }).toList();
        return PageUtil.convertPage(merchantApplyPage, merchantApplyVOList);
    }

    @Override
    public MerchantApplyVO detail(Integer id) {
        MerchantApply merchantApply = this.getById(id);
        Assert.notNull(merchantApply, () -> new GlobalException(MERCHANT_APPLY_NOT_EXIST));

        User user = userMapper.selectById(merchantApply.getUserId());

        MerchantApplyVO merchantApplyVO = new MerchantApplyVO(merchantApply, user);
        MerchantApplyDetailVO.MerchantData merchantData = JsonUtil.fromJson(merchantApplyVO.getMerchantData().toString(),
                MerchantApplyDetailVO.MerchantData.class);
        if (merchantData.getBusinessAddress() != null && !merchantData.getBusinessAddress().isEmpty()) {
            LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Region::getRegionId, merchantData.getBusinessAddress());
            List<Region> regions = regionService.list(queryWrapper);
            Map<Integer, String> regionMap = regions.stream().collect(Collectors.toMap(Region::getRegionId,
                    Region::getRegionName));
            StringBuilder businessAddressName = new StringBuilder();
            for (Integer regionId : merchantData.getBusinessAddress()) {
                if (regionMap.get(regionId) != null) {
                    businessAddressName.append(regionMap.get(regionId));
                }
            }
            merchantData.setBusinessAddressName(businessAddressName.toString());
        }
        merchantApplyVO.setMerchantData(JSONUtil.parseObj(merchantData));
        return merchantApplyVO;
    }

    @Override
    public boolean create(MerchantApplyCreateDTO createDTO) {
        if (createDTO != null) {
            MerchantApply merchantApply = new MerchantApply();
            BeanUtils.copyProperties(createDTO, merchantApply);
            return this.save(merchantApply);
        }
        return false;
    }

    @Override
    public boolean update(MerchantApplyUpdateDTO updateDTO) {
        if (updateDTO != null) {
            MerchantApply merchantApply = new MerchantApply();
            BeanUtils.copyProperties(updateDTO, merchantApply);
            return this.updateById(merchantApply);
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void audit(MerchantApplyAuditParam param) {
        MerchantApply merchantApply = this.getById(param.getMerchantApplyId());
        Assert.notNull(merchantApply, () -> new GlobalException(translatePackage.translate(NOT_FOUND_ERROR)));

        // 判断查询审核数据状态
        Assert.isTrue(MerchantApplyStatusEnum.PENDING_REVIEW.getCode().equals(merchantApply.getStatus()), () -> new GlobalException(translatePackage.translate(MERCHANT_APPLY_AUDIT_STATUS_ERROR)));

        // 更新审核数据
        merchantApply.setStatus(param.getStatus());
        merchantApply.setAuditRemark(param.getAuditRemark());
        merchantApply.setAuditTime(StringUtils.getCurrentTime());
        this.updateById(merchantApply);

        // 审核失败
        if (MerchantApplyStatusEnum.AUDIT_NOT_PASS.getCode().equals(param.getStatus())) {
            // 查询商家审核失败短信模版
            MessageTemplate merchantApplyRefuse = messageTemplateService.lambdaQuery()
                    .eq(MessageTemplate::getMessageId, MessageTemplateType.MERCHANT_APPLY_REFUSE.getMessageId())
                    .one();

            // 发送审核失败短信
            MerchantApplyApplyParam.MerchantData merchantData = JSON.parseObject(merchantApply.getMerchantData(), MerchantApplyApplyParam.MerchantData.class);
            RegisterSmsDTO registerSmsDTO = new RegisterSmsDTO(merchantData.getContactPhone());
            configService.sendSms(registerSmsDTO, merchantApplyRefuse.getTemplateId(), "");
        }

        // 审核成功
        if (MerchantApplyStatusEnum.AUDIT_PASS.getCode().equals(param.getStatus())) {
            // 创建商户
            Merchant merchant = Merchant.builder()
                    .merchantApplyId(merchantApply.getMerchantApplyId())
                    .userId(merchantApply.getUserId())
                    .addTime(StringUtils.getCurrentTime())
                    .baseData(merchantApply.getBaseData())
                    .shopData(merchantApply.getShopData())
                    .merchantData(merchantApply.getMerchantData())
                    .status(MerchantStatus.NORMAL.getCode())
                    .type(merchantApply.getType())
                    .companyName(merchantApply.getCompanyName())
                    .corporateName(merchantApply.getCorporateName())
                    .build();
            merchantService.save(merchant);

            // 查询admin_user是否存在
            User user = userMapper.selectById(merchantApply.getUserId());
            String contactPhone = JSONUtil.parseObj(merchantApply.getMerchantData()).getStr("contactPhone");
            String contactEmail = JSONUtil.parseObj(merchantApply.getMerchantData()).getStr("contactEmail");
            AdminUser adminUser = adminUserService.getAdminUserByUseMobile(contactPhone);
            if (adminUser == null) {
                AdminUserDTO adminUserCreate = AdminUserDTO.builder()
                        .username(contactPhone)
                        .mobile(contactPhone)
                        .email(contactEmail)
                        .initialPassword(PASSWORD)
                        .adminType(SHOP_TYPE)
                        .roleId(ROLE_ID)
                        .avatar("")
                        .merchantId(merchant.getMerchantId())
                        .build();
                adminUser = adminUserService.providerCreate(adminUserCreate);

                // 记录日操作日志
                adminLogService.createByLogInfo("新增管理员:" + adminUserCreate.getUsername());
            }

            // 创建商户用户
            boolean personalFlag = MerchantApplyTypeEnum.PERSONAL.getCode().equals(merchantApply.getType());
            MerchantUser merchantUser = MerchantUser.builder()
                    .merchantId(merchant.getMerchantId())
                    .isAdmin(Constants.YES)
                    .userId(personalFlag ? merchantApply.getUserId() : null)
                    .build();
            merchantUserService.updateMerchantUser(merchantUser, adminUser.getAdminId());

            // 创建店铺数据
            String shopLogo = getShopLogoPicUrl(merchantApply.getShopData());
            Shop shop = Shop.builder()
                    .merchantId(merchant.getMerchantId())
                    .shopLogo(shopLogo)
                    .shopTitle(merchantApply.getShopTitle())
                    .addTime(StringUtils.getCurrentTime())
                    .build();
            if (tigshopProperties.getIsO2o() == 1) {
                shop.setShopType(ShopTypeEnum.STORE.getCode());
            }
            shopService.save(shop);
            if (tigshopProperties.getIsO2o() == 1) {
                // 门店创建自提配置
                Integer storeId = shop.getShopId();
                Shop shopPickup = new Shop();
                BeanUtils.copyProperties(shop, shopPickup);
                shopPickupConfigService.initPickUpConfig(storeId);
                shopPickupTplService.initPickUpTpl(storeId);

                shopPickup.setShopType(ShopTypeEnum.PICKUP.getCode());
                shopPickup.setShopTitle(shopPickup.getShopTitle() + "（自提点）");
                shopPickup.setStoreParentId(storeId);
                shopPickup.setShopId(null);
                shopService.save(shopPickup);
                // 自提点创建自提配置
                Integer pickUpId = shopPickup.getShopId();
                shopPickupConfigService.initPickUpConfig(pickUpId);
                shopPickupTplService.initPickUpTpl(pickUpId);
            }


            // TODO 创建店铺时，如果商户表里的shop_data为空，需要将店铺信息更新到shop_data字段；（暂时不补，不确定是否需要这个逻辑）

            // 初始化运费模板
            shippingTplService.initShippingTpl(shop.getShopId());
            // 初始化装修模板
            decorateService.initDecorate(shop.getShopId());
            // 初始化角色
            adminRoleService.initAdminRole(merchant.getMerchantId(), shop.getShopId());
            // 初始化商品
            // productService.initProductByShopId(shop.getShopId());

            // 创建用户店铺关联数据
            ConfigPO maxSubAdministratorConfig = configService.getConfigByCode(SettingsEnum.MAX_SUB_ADMINISTRATOR.getBizCode());
            Assert.isTrue(Integer.parseInt(maxSubAdministratorConfig.getBizVal()) > 1, () -> new GlobalException("店铺员工数已达上限,上限为" + maxSubAdministratorConfig.getBizVal() + "个,如需修改,请前往配置"));

            AdminUserShop adminUserShop = AdminUserShop.builder()
                    .adminId(adminUser.getAdminId())
                    .userId(adminUser.getUserId())
                    .shopId(shop.getShopId())
                    .username(adminUser.getUsername())
                    .isAdmin(Constants.YES)
                    .avatar(adminUser.getAvatar())
                    .authList(JsonUtil.toJson(List.of("all")))
                    .addTime(StringUtils.getCurrentTime())
                    .build();
            adminUserShopMapper.insert(adminUserShop);

            if (tigshopProperties.getIsO2o() == 1) {
                // 添加自提用户
                Shop pickUp = new LambdaQueryChainWrapper<>(shopService.getBaseMapper()).eq(Shop::getStoreParentId, shop.getShopId()).one();
                AdminUserShop adminUserShopPickUp = AdminUserShop.builder()
                        .adminId(adminUser.getAdminId())
                        .userId(adminUser.getUserId())
                        .shopId(pickUp.getShopId())
                        .username(adminUser.getUsername())
                        .isAdmin(Constants.YES)
                        .avatar(adminUser.getAvatar())
                        .authList(JsonUtil.toJson(List.of("all")))
                        .addTime(StringUtils.getCurrentTime())
                        .build();
                adminUserShopMapper.insert(adminUserShopPickUp);
            }

            // 记录日操作日志
            adminLogService.createByLogInfo("新增员工:" + adminUserShop.getUsername());

            // 创建店铺用户数据
            MerchantAccount merchantAccount = MerchantAccount.builder()
                    .accountType(1)
                    .accountName(MerchantApplyTypeEnum.PERSONAL.getCode().equals(merchantApply.getType()) ? merchantApply.getCorporateName() : merchantApply.getCompanyName())
                    .accountNo(getBankCard(merchantApply.getMerchantData()))
                    .bankName(getBankDeposit(merchantApply.getMerchantData()))
                    .bankBranch(getBankBranch(merchantApply.getMerchantData()))
                    .shopId(shop.getShopId())
                    .build();
            merchantAccountService.save(merchantAccount);

            // 发送商家审核成功短信
            MessageTemplate merchantApplySuccess = messageTemplateService.lambdaQuery()
                    .eq(MessageTemplate::getMessageId, MessageTemplateType.MERCHANT_APPLY_SUCCESS.getMessageId())
                    .one();

            // 发送审核成功短信
            MerchantApplyApplyParam.MerchantData merchantData = JSON.parseObject(merchantApply.getMerchantData(), MerchantApplyApplyParam.MerchantData.class);
            RegisterSmsDTO registerSmsDTO = new RegisterSmsDTO(merchantData.getContactPhone());

            Map<String, Object> templateParam = new HashMap<>();
            templateParam.put("username", adminUser.getUsername());
            templateParam.put("password", adminUser.getInitialPassword());

            configService.sendSms(registerSmsDTO, merchantApplySuccess.getTemplateId(), templateParam);
        }
    }

    /**
     * 创建商户
     *
     * @param merchantApply merchantApply
     * @return 商户id
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createMerchant(MerchantApply merchantApply) {
        Merchant merchant = new Merchant();
        merchant.setMerchantApplyId(merchantApply.getMerchantApplyId());
        merchant.setUserId(merchantApply.getUserId());
        merchant.setType(merchantApply.getType());
        merchant.setShopData(merchantApply.getShopData());
        merchant.setBaseData(merchantApply.getBaseData());
        merchant.setMerchantData(merchantApply.getMerchantData());
        merchant.setCompanyName(merchantApply.getCompanyName());
        merchant.setCorporateName(merchantApply.getCorporateName());
        merchant.setAddTime(StringUtils.getCurrentTime());
        merchantService.save(merchant);
        return merchant.getMerchantId();
    }

    /**
     * 创建后台用户
     *
     * @param merchantApply merchantApply
     * @param user          user
     * @param merchantId    商户Id
     * @return adminId
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminUser createAdminUser(MerchantApply merchantApply, User user, Integer merchantId) {
        // 查询admin_user是否存在
        AdminUser adminUser = adminUserService.getAdminUserByUseMobile(user.getMobile());

        if (adminUser != null) {
            return adminUser;
        }

        AdminUserDTO adminUserCreate = new AdminUserDTO();
        adminUserCreate.setUsername(user.getUsername());
        adminUserCreate.setMobile(user.getMobile());
        adminUserCreate.setPassword(PASSWORD);
        adminUserCreate.setAdminType(SHOP_TYPE);
        adminUserCreate.setRoleId(ROLE_ID);
        adminUserCreate.setAvatar("");
        adminUserCreate.setMerchantId(merchantId);
        adminUser = adminUserService.providerCreate(adminUserCreate);
        // 记录日操作日志
        adminLogService.createByLogInfo("新增管理员:" + user.getUsername());
        // merchant_user表
        MerchantUser merchantUser = new MerchantUser();
        merchantUser.setMerchantId(merchantId);
        merchantUserService.updateMerchantUser(merchantUser, adminUser.getAdminId());

        return adminUser;
    }

    /**
     * 创建商户用户
     *
     * @param merchantApply merchantApply
     * @param merchantId    商户id
     * @param adminId       后台id
     */
    @Transactional(rollbackFor = Exception.class)
    public void createMerchantUser(MerchantApply merchantApply, Integer merchantId, Integer adminId) {
        MerchantUser merchantUser = new MerchantUser();
        MerchantApplyTypeEnum merchantApplyTypeEnum = MerchantApplyTypeEnum.fromStatusCode(merchantApply.getType());
        if (merchantApplyTypeEnum == MerchantApplyTypeEnum.PERSONAL) {
            merchantUser.setMerchantId(merchantId);
            merchantUser.setIsAdmin(1);
            merchantUser.setUserId(merchantApply.getUserId());
            merchantUserService.updateMerchantUser(merchantUser, adminId);
        } else {
            merchantUser.setMerchantId(merchantId);
            merchantUser.setIsAdmin(1);
            merchantUserService.updateMerchantUser(merchantUser, adminId);
        }
    }

    /**
     * @param merchantApply merchantApply
     * @param merchantId    merchantId
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createShop(MerchantApply merchantApply, Integer merchantId) {
        //创建店铺数据
        Shop shop = new Shop();
        shop.setMerchantId(merchantId);
        shop.setShopTitle(merchantApply.getShopTitle());
        String shopData = merchantApply.getShopData();
        shop.setShopLogo(getShopLogoPicUrl(shopData));
        shop.setAddTime(StringUtils.getCurrentTime());
        shopService.save(shop);
        //初始化运费模板
        shippingTplService.initShippingTpl(shop.getShopId());
        //初始化装修模板
        decorateService.initDecorate(shop.getShopId());
        //初始化角色
        adminRoleService.initAdminRole(merchantId, shop.getShopId());
        //初始化商品
        //productService.initProductByShopId(shop.getShopId());

        return shop.getShopId();
    }

    /**
     * 创建用户店铺关联数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void createAdminUserShop(Integer shopId, AdminUser adminUser) {
        // 创建用户店铺关联数据
        AdminUserShop adminUserShop = AdminUserShop.builder()
                .adminId(adminUser.getAdminId())
                .userId(adminUser.getUserId())
                .shopId(shopId)
                .username(adminUser.getUsername())
                .email(adminUser.getEmail())
                .avatar(adminUser.getAvatar())
                .authList(adminUser.getAuthList())
                .isUsing(Constants.YES)
                .isAdmin(Constants.YES)
                .addTime(StringUtils.getCurrentTime())
                .roleId(adminUser.getRoleId())
                .build();
        adminUserShopMapper.insert(adminUserShop);
    }

    /**
     * 创建商户账号
     *
     * @param merchantApply merchantApply
     * @param shopId        店铺id
     */
    @Transactional(rollbackFor = Exception.class)
    public void createMerchantAccount(MerchantApply merchantApply, Integer shopId) {
        MerchantAccount merchantAccount = new MerchantAccount();
        merchantAccount.setAccountType(1);
        if (merchantApply.getType() == 1) {
            merchantAccount.setAccountName(merchantApply.getCorporateName());
        } else {
            merchantAccount.setAccountName(merchantApply.getCompanyName());
        }
        merchantAccount.setShopId(shopId);
        merchantAccount.setAccountNo(getBankCard(merchantApply.getMerchantData()));
        merchantAccount.setBankName(getBankDeposit(merchantApply.getMerchantData()));
        merchantAccount.setBankBranch(getBankBranch(merchantApply.getMerchantData()));
        merchantAccountService.save(merchantAccount);
    }

    @Override
    public List<Map<String, String>> config() {
        List<Map<String, String>> statusList = new ArrayList<>();
        for (MerchantApplyStatusEnum status : MerchantApplyStatusEnum.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("status", String.valueOf(status.getCode()));
            map.put("statusText", status.getDescription());
            statusList.add(map);
        }
        return statusList;
    }

    /**
     * 分析shopData中的shop_logo
     *
     * @return String
     */
    private static String getShopLogoPicUrl(String shopData) {
        //{"shop_logo":[{"pic_thumb":"img\/pc\/202412\/1734684741wf2xLMvQ5jUtzwqHfS.png?x-oss-process=image\/resize,m_pad,h_200,h_200","pic_url":"https:\/\/oss.tigshop.com\/img\/pc\/202412\/1734684741wf2xLMvQ5jUtzwqHfS.png","pic_name":"success"}],"shop_title":"111","contact_mobile":"11111111111111","description":"1111"}
        JSONObject jsonObject = JSONUtil.parseObj(shopData);
        // 检查 shop_logo 是否存在且为数组
        if (jsonObject.containsKey("shopLogo") && jsonObject.get("shopLogo") instanceof JSONArray) {
            JSONArray shopLogoArray = jsonObject.getJSONArray("shopLogo");
            // 获取第一个元素
            if (!shopLogoArray.isEmpty()) {
                JSONObject firstElement = shopLogoArray.getJSONObject(0);
                // 检查 pic_url 是否存在
                if (firstElement.containsKey("picUrl")) {
                    return firstElement.getStr("picUrl");
                }
            }
        }
        return "";
    }

    private String getBankCard(String merchantData) {
        return getMerchantDataField(merchantData, "bank_card");
    }

    private String getBankDeposit(String merchantData) {
        return getMerchantDataField(merchantData, "bank_deposit");
    }

    private String getBankBranch(String merchantData) {
        return getMerchantDataField(merchantData, "bank_branch");
    }

    private String getMerchantDataField(String merchantData, String fieldName) {
        if (StringUtils.isEmpty(merchantData)) {
            return "";
        }

        JSONObject jsonObject = JSONUtil.parseObj(merchantData);
        if (jsonObject.containsKey(fieldName)) {
            return jsonObject.getStr(fieldName);
        }
        return "";
    }

    @Override
    public MerchantMyApplyVO myApply() {
        Integer userId = getCurrentUserId();
        MerchantApply merchantApply = this.lambdaQuery()
                .eq(MerchantApply::getUserId, userId)
                .orderByDesc(MerchantApply::getMerchantApplyId)
                .last("limit 1")
                .one();

        if (merchantApply == null) {
            return null;
        }

        MerchantMyApplyVO merchantMyApplyVO = BeanUtil.copyProperties(merchantApply, MerchantMyApplyVO.class);
        merchantMyApplyVO.setStatusText(MerchantApplyStatusEnum.getStatusName(merchantApply.getStatus()));
        return merchantMyApplyVO;
    }

    @Override
    public MerchantApplyDetailVO applyDetail(Integer id) {
        Integer userId = getCurrentUserId();
        MerchantApply merchantApply = this.getById(id);
        if (merchantApply != null) {
            //如果结果id和当前用户id不一致，直接抛异常
            if (!userId.equals(merchantApply.getUserId())) {
                throw new GlobalException(translatePackage.translate(MERCHANT_APPLY_NOT_EXIST));
            }
            MerchantApplyDetailVO merchantApplyDetailVO = new MerchantApplyDetailVO();
            BeanUtils.copyProperties(merchantApply, merchantApplyDetailVO);
            //将add_time转成格式化时间
            long timestampInSecondsAdd = merchantApply.getAddTime();
            Date datesAdd = new Date(timestampInSecondsAdd * 1000L);
            SimpleDateFormat sdfAdd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            merchantApplyDetailVO.setAddTime(sdfAdd.format(datesAdd));
            merchantApplyDetailVO.setShopData(JsonUtil.fromJson(merchantApply.getShopData(), JSONObject.class));
            merchantApplyDetailVO.setStatusText(MerchantApplyStatusEnum.getStatusName(merchantApply.getStatus()));

            MerchantApplyDetailVO.BaseData baseData = JsonUtil.fromJson(merchantApply.getBaseData(),
                    MerchantApplyDetailVO.BaseData.class);
            if (baseData.getLicenseAddrProvince() != null && !baseData.getLicenseAddrProvince().isEmpty()) {
                LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(Region::getRegionId, baseData.getLicenseAddrProvince());
                List<Region> regions = regionService.list(queryWrapper);
                //将regions 列表转成 region_id 对应的map
                Map<Integer, String> regionMap = regions.stream().collect(Collectors.toMap(Region::getRegionId,
                        Region::getRegionName));
                //循环baseData.getLicenseAddrProvince()
                StringBuilder licenseAddrProvinceName = new StringBuilder();
                for (Integer regionId : baseData.getLicenseAddrProvince()) {
                    if (regionMap.get(regionId) != null) {
                        licenseAddrProvinceName.append(regionMap.get(regionId));
                    }
                }
                baseData.setLicenseAddrProvinceName(licenseAddrProvinceName.toString());
            }
            merchantApplyDetailVO.setBaseData(baseData);

            MerchantApplyDetailVO.MerchantData merchantData = JsonUtil.fromJson(merchantApply.getMerchantData(),
                    MerchantApplyDetailVO.MerchantData.class);

            if (merchantData.getBusinessAddress() != null && !merchantData.getBusinessAddress().isEmpty()) {
                LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(Region::getRegionId, merchantData.getBusinessAddress());
                List<Region> regions = regionService.list(queryWrapper);
                Map<Integer, String> regionMap = regions.stream().collect(Collectors.toMap(Region::getRegionId,
                        Region::getRegionName));
                StringBuilder businessAddressName = new StringBuilder();
                for (Integer regionId : merchantData.getBusinessAddress()) {
                    if (regionMap.get(regionId) != null) {
                        businessAddressName.append(regionMap.get(regionId));
                    }
                }
                merchantData.setBusinessAddressName(businessAddressName.toString());
            }
            merchantApplyDetailVO.setMerchantData(merchantData);

            if (merchantApply.getStatus().equals(MerchantApplyStatusEnum.AUDIT_PASS.getCode())) {
                String contactPhone = JSONUtil.parseObj(merchantApply.getMerchantData()).getStr("contactPhone");
                LambdaQueryWrapper<AdminUser> queryWrapper1 = new LambdaQueryWrapper<>();
                queryWrapper1.eq(AdminUser::getMobile, contactPhone);
                AdminUser adminUser = adminUserService.getOne(queryWrapper1);
                if (adminUser != null) {
                    MerchantApplyDetailVO.initial initialUserInfo = new MerchantApplyDetailVO.initial();
                    initialUserInfo.setUsername(adminUser.getUsername());
                    initialUserInfo.setMobile(adminUser.getMobile());
                    initialUserInfo.setInitialPassword(adminUser.getInitialPassword());
                    merchantApplyDetailVO.setInitialUserInfo(initialUserInfo);
                }
            }
            return merchantApplyDetailVO;
        }

        throw new GlobalException(translatePackage.translate(MERCHANT_APPLY_NOT_EXIST));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantApplyDetailVO apply(MerchantApplyApplyParam param) {

        // 查询商家申请是否需要审核
        ConfigPO needCheck = configService.getConfigByCode(SettingsEnum.MERCHANT_APPLY_NEED_CHECK.getBizCode());

        // 新增商户申请
        MerchantApply merchantApply = param.createMerchantApply(needCheck.getBizVal());
        this.save(merchantApply);

        // 如果审核通过，则创建商户、后台用户、商户用户、店铺、用户店铺信息、商户账户
        if (MerchantApplyStatusEnum.AUDIT_PASS.getCode().equals(merchantApply.getStatus())) {
            // 创建商户
            Integer merchantId = this.createMerchant(merchantApply);
            // 创建后台用户
            User user = userMapper.selectById(merchantApply.getUserId());
            if (ObjectUtil.isEmpty(user.getMobile())) {
                throw new GlobalException("请先绑定手机号");
            }
            // 查询admin_user是否存在
            AdminUser adminUser = this.createAdminUser(merchantApply, user, merchantId);
            // 创建商户用户
            this.createMerchantUser(merchantApply, merchantId, adminUser.getAdminId());
            // 创建店铺
            Integer shopId = this.createShop(merchantApply, merchantId);
            // 创建用户店铺信息
            this.createAdminUserShop(shopId, adminUser);
            // 创建商户账户
            this.createMerchantAccount(merchantApply, shopId);
        }

        // 发送站内信消息
        String name = param.getType() == 1 ? param.getBaseData().getCorporateName() : param.getBaseData().getCompanyName();
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("merchantApplyId", merchantApply.getMerchantApplyId());
        AdminMsgCreateDTO adminMsgCreateDTO = AdminMsgCreateDTO.builder()
                .msgType(AdminMsgTypeEnum.SHOP_ENTRY_APPLICATION.getCatId())
                .title("商户入驻通知：" + name)
                .content("【" + name + "】提交了商家入驻申请，请及时处理")
                .relatedData(relatedData)
                .build();
        adminMsgService.createMessage(adminMsgCreateDTO);

        return this.applyDetail(merchantApply.getMerchantApplyId());
    }
}
