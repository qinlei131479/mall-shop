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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.merchant.MerchantAdminDataDTO;
import com.tigshop.bean.dto.merchant.MerchantCreateDTO;
import com.tigshop.bean.query.merchant.MerchantListPageQuery;
import com.tigshop.bean.dto.merchant.MerchantUpdateDTO;
import com.tigshop.bean.enums.merchant.MerchantStatus;
import com.tigshop.bean.enums.merchant.MerchantType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.model.merchant.MerchantUser;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.merchant.MerchantVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.merchant.MerchantMapper;
import com.tigshop.mapper.merchant.MerchantUserMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.authority.AdminRoleService;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.merchant.MerchantService;
import com.tigshop.service.merchant.MerchantUserService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.shop.ShopFundsService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.authority.AdminUserConstants.PASSWORD;
import static com.tigshop.common.constant.merchant.MerchantConstants.MERCHANT_USER_NOT_EXIST;
import static com.tigshop.common.constant.merchant.MerchantConstants.MERCHANT_USER_NOT_FOUND;

/**
 * 商家表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class MerchantServiceImpl extends BaseServiceImpl<MerchantMapper, Merchant> implements MerchantService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private MerchantUserService merchantUserService;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    ShopFundsService shopFundsService;
    @Resource
    ConfigService configService;
    @Resource
    private MerchantUserMapper merchantUserMapper;
    @Resource
    TranslatePackageImpl translatePackage;
    @Resource
    AdminRoleService adminRoleService;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<MerchantVO> list(MerchantListPageQuery pageQuery) {
        Page<Merchant> page = buildSortOrder(pageQuery);

        Page<Merchant> merchantPage = this.page(page, Wrappers.lambdaQuery(Merchant.class)
                .eq(null != pageQuery.getMerchantId(), Merchant::getMerchantId, pageQuery.getMerchantId())
                .and(StrUtil.isNotBlank(pageQuery.getKeyword()), wrapper -> wrapper
                        .like(Merchant::getCompanyName, pageQuery.getKeyword())
                        .or()
                        .like(Merchant::getCorporateName, pageQuery.getKeyword())
                )
        );

        List<Merchant> merchantRecords = merchantPage.getRecords();
        if (CollUtil.isEmpty(merchantRecords)) {
            return PageUtil.convertPage(merchantPage, Collections.emptyList());
        }

        // 查询出所有用户
        List<Integer> userIds = merchantRecords.stream().map(Merchant::getUserId).distinct().toList();
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getUserId, Function.identity()));

        // 查询出所有商户关联
        List<Integer> merchantIds = merchantRecords.stream().map(Merchant::getMerchantId).distinct().toList();
        List<MerchantUser> merchantUserList = merchantUserMapper.selectList(
                Wrappers.lambdaQuery(MerchantUser.class)
                        .in(MerchantUser::getMerchantId, merchantIds)
                        .eq(MerchantUser::getIsAdmin, 1)
        );
        Map<Integer, MerchantUser> merchantUserMap = merchantUserList.stream().collect(Collectors.toMap(MerchantUser::getMerchantId, Function.identity()));

        // 然后去AdminUser表中查出数据
        List<Integer> adminUserIds = merchantUserList.stream().map(MerchantUser::getAdminUserId).distinct().toList();
        List<AdminUser> adminUsers = adminUserService.getAdminUserByIds(adminUserIds);
        Map<Integer, AdminUser> adminUserMap = adminUsers.stream().collect(Collectors.toMap(AdminUser::getAdminId, Function.identity()));

        // 转换为VO
        List<MerchantVO> merchantVOList = merchantRecords.stream()
                .map(merchant -> {
                    MerchantVO merchantVO = new MerchantVO();
                    BeanUtils.copyProperties(merchant, merchantVO);
                    long timestampInSeconds = merchant.getAddTime();
                    Date date = new Date(timestampInSeconds * 1000L);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    merchantVO.setAddTime(sdf.format(date));
                    MerchantAdminDataDTO merchantUserData = new MerchantAdminDataDTO();
                    MerchantUser merchantUser = merchantUserMap.get(merchant.getMerchantId());
                    if (merchantUser != null) {
                        AdminUser adminUser = adminUserMap.get(merchantUser.getAdminUserId());
                        BeanUtils.copyProperties(merchantUser, merchantUserData);
                        if (adminUser != null) {
                            merchantUserData.setUsername(adminUser.getUsername());
                            merchantUserData.setAdminId(adminUser.getAdminId());
                        }
                        merchantVO.setAdmin(merchantUserData);
                    }
                    //存储uer数据
                    User user = userMap.get(merchant.getUserId());
                    merchantVO.setUser(user);
                    //从shop表中查出关联个数
                    Long shopCount = shopFundsService.getCountByMerchantId(merchant.getMerchantId());
                    merchantVO.setShopCount(shopCount);
                    merchantVO.setTypeText(MerchantType.getTypeName(merchant.getType()));
                    merchantVO.setStatusText(MerchantStatus.getStatusName(merchant.getStatus()));
                    User user1 = new User();

                    if (merchantVO.getAdmin() != null) {
                        user1.setUsername(merchantVO.getAdmin().getUsername());
                    }
                    merchantVO.setUser(user1);
                    return merchantVO;
                }).toList();
        return PageUtil.convertPage(merchantPage, merchantVOList);
    }

    @Override
    public MerchantVO detail(Integer id) {
        if (id != null) {
            Merchant merchant = this.getById(id);
            MerchantVO merchantVO = new MerchantVO();
            BeanUtils.copyProperties(merchant, merchantVO);
            merchantVO.setMerchantData(JSON.parseObject(merchant.getMerchantData()));
            merchantVO.setBaseData(JSON.parseObject(merchant.getBaseData()));
            merchantVO.setShopData(JSON.parseObject(merchant.getShopData()));
            User user = userMapper.selectById(merchant.getUserId());
            merchantVO.setUser(user);
            long timestampInSeconds = merchant.getAddTime();
            Date date = new Date(timestampInSeconds * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            merchantVO.setAddTime(sdf.format(date));
            merchantVO.setTypeText(MerchantType.getTypeName(merchant.getType()));
            merchantVO.setStatusText(MerchantStatus.getStatusName(merchant.getStatus()));

            return merchantVO;
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(MerchantCreateDTO createDTO) {
        if (createDTO != null) {
            Merchant merchant = new Merchant();
            //获取数据
            MerchantCreateDTO.AdminInfo adminInfo = createDTO.getAdmin();
            MerchantCreateDTO.MerchantData merchantData = createDTO.getMerchantData();
            MerchantCreateDTO.BaseData baseData = createDTO.getBaseData();

            //赋值
            merchant.setAddTime(StringUtils.getCurrentTime());
            merchant.setMerchantData(JsonUtil.toJson(merchantData));
            merchant.setBaseData(JsonUtil.toJson(baseData));
            merchant.setType(baseData.getType());
            merchant.setCompanyName(baseData.getCompanyName());
            merchant.setCorporateName(baseData.getCorporateName());
            merchant.setMerchantApplyId(0);
            //将数据存入merchant表
            this.save(merchant);
            MerchantUser merchantUser = new MerchantUser();
            if (adminInfo.getType() == 1) {
                //获取用户数据
                User user = userMapper.selectById(adminInfo.getUserId());
                if (user == null) {
                    throw new GlobalException(translatePackage.translate(MERCHANT_USER_NOT_FOUND));
                }
                String mobile = ObjectUtil.isEmpty(user.getMobile()) ? createDTO.getMerchantData().getContactPhone() : user.getMobile();
                if (ObjectUtil.isEmpty(mobile)) {
                    throw new GlobalException("关联会员或者商户手机号为空");
                }
                AdminUser adminUser = adminUserService.getAdminUserByUseMobile(mobile);
                //判断adminUser是否为空
                Integer adminId;
                if (adminUser == null) {
                    AdminUser adminUserCreate = getAdminUser(user);
                    adminUserCreate.setMobile(mobile);
                    adminUserService.save(adminUserCreate);
                    adminId = adminUserCreate.getAdminId();
                } else {
                    adminId = adminUser.getAdminId();
                }
                //操作merchant_user表
                merchantUser.setMerchantId(merchant.getMerchantId());
                merchantUser.setIsAdmin(1);
                merchantUser.setUserId(user.getUserId());
                merchantUserService.updateMerchantUser(merchantUser, adminId);
            } else {
                //更新admin_user表的merchant_id字段
                AdminUser adminUser = adminUserService.getById(adminInfo.getAdminId());
                if (adminUser == null) {
                    throw new GlobalException(translatePackage.translate(MERCHANT_USER_NOT_EXIST));
                }
                adminUser.setMerchantId(merchant.getMerchantId());
                adminUserService.updateById(adminUser);
                //更新merchant_user表的merchant_id
                merchantUser.setMerchantId(merchant.getMerchantId());
                merchantUser.setIsAdmin(1);
                merchantUserService.updateMerchantUser(merchantUser, adminInfo.getAdminId());
            }
            return true;
        }
        return false;
    }

    private AdminUser getAdminUser(User user) {
        AdminUser adminUserCreate = new AdminUser();
        adminUserCreate.setUsername(user.getUsername());
        adminUserCreate.setUserId(user.getUserId());
        adminUserCreate.setMobile(user.getMobile());
        adminUserCreate.setEmail(user.getEmail());
        adminUserCreate.setPassword(PASSWORD);
        adminUserCreate.setAdminType("shop");
        adminUserCreate.setAvatar("");
        adminUserCreate.setRoleId(1);

        adminUserCreate.setMerchantId(0);
        adminUserCreate.setShopId(0);
        adminUserCreate.setIsUsing(0);
        adminUserCreate.setSuppliersId(0);
        adminUserCreate.setAddTime(StringUtils.getCurrentTime());

        if (ObjectUtil.isEmpty(adminUserCreate.getAvatar())) {
            int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
            String format = StrUtil.format("../assets/avatar/{}.jpeg", randomNumber);
            adminUserCreate.setAvatar(format);
        }
        if (ObjectUtil.isNotEmpty(adminUserCreate.getPassword())) {
            adminUserCreate.setPassword(passwordEncoder.encode(adminUserCreate.getPassword()));
        }
        if (adminUserCreate.getRoleId() > 0) {
            adminUserCreate.setAuthList(adminRoleService.getOne(new LambdaQueryWrapper<AdminRole>()
                            .eq(AdminRole::getRoleId, adminUserCreate.getRoleId()))
                    .getAuthorityList()
            );
        }
        return adminUserCreate;
    }

    @Override
    public boolean update(MerchantUpdateDTO updateDTO) {
        if (updateDTO != null) {
            Merchant merchant = new Merchant();
            BeanUtils.copyProperties(updateDTO, merchant);
            return this.updateById(merchant);
        }
        return false;
    }

    @Override
    public MerchantVO getMerchantVO(Integer id) {
        Merchant merchant = this.getById(id);
        if (merchant == null) {
            return new MerchantVO();
        }
        MerchantVO merchantVO = new MerchantVO();
        BeanUtils.copyProperties(merchant, merchantVO);
        long meTimestampInSeconds = merchant.getAddTime();
        Date meDate = new Date(meTimestampInSeconds * 1000L);
        SimpleDateFormat meSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        merchantVO.setAddTime(meSdf.format(meDate));
        merchantVO.setTypeText(MerchantType.getTypeName(merchant.getType()));
        merchantVO.setStatusText(MerchantStatus.getStatusName(merchant.getStatus()));
        MerchantUser merchantUser = merchantUserService.getDetailByMerchantId(merchant.getMerchantId(), false);
        MerchantAdminDataDTO merchantUserData = new MerchantAdminDataDTO();
        if (merchantUser != null) {
            BeanUtils.copyProperties(merchantUser, merchantUserData);
            AdminUser adminUser = adminUserService.getById(merchantUser.getAdminUserId());
            if (adminUser != null) {
                merchantUserData.setUsername(adminUser.getUsername());
                merchantUserData.setAdminId(adminUser.getAdminId());
            } else {
                merchantUserData.setUsername(null);
                merchantUserData.setAdminId(null);
            }
            merchantVO.setAdmin(merchantUserData);
        } else {
            merchantVO.setAdmin(null);
        }
        merchantVO.setUser(null);
        merchantVO.setShopCount(null);

        merchantVO.setMerchantData(JSON.parseObject(merchant.getMerchantData()));
        merchantVO.setBaseData(JSON.parseObject(merchant.getBaseData()));
        merchantVO.setShopData(JSON.parseObject(merchant.getShopData()));
        return merchantVO;
    }

    @Override
    public String applyShopAgreement() {
        return configService.getConfigByCode(SettingsEnum.SHOP_AGREEMENT.getBizCode()).getBizVal();
    }

}
