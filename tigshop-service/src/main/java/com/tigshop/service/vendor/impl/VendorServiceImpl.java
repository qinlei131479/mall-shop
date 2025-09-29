package com.tigshop.service.vendor.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.vendor.VendorDiscontinueDTO;
import com.tigshop.bean.enums.finance.StatementType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.vendor.AdminInfoEnum;
import com.tigshop.bean.enums.vendor.VendorIsAdminEnum;
import com.tigshop.bean.enums.vendor.VendorStatusEnum;
import com.tigshop.bean.enums.vendor.VendorUsingEnum;
import com.tigshop.bean.event.VendorMainAccountBindEvent;
import com.tigshop.bean.model.authority.AdminLog;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.finance.StatementOutput;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.vendor.AdminUserVendor;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.param.account.ShopOrVendorBindParam;
import com.tigshop.bean.param.vendor.*;
import com.tigshop.bean.vo.shop.StaffShowVO;
import com.tigshop.bean.vo.vendor.VendorAccountFundVO;
import com.tigshop.bean.vo.vendor.VendorDetailConfigVO;
import com.tigshop.bean.vo.vendor.VendorDetailVO;
import com.tigshop.bean.vo.vendor.VendorVO;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.finance.StatementOutputMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.vendor.AdminUserVendorMapper;
import com.tigshop.mapper.vendor.VendorMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.authority.AdminRoleService;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.RegionService;
import com.tigshop.service.vendor.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static com.tigshop.common.constant.Constants.NO;
import static com.tigshop.common.constant.authority.AdminUserConstants.PASSWORD;
import static com.tigshop.common.constant.authority.AdminUserConstants.VENDOR_TYPE;
import static com.tigshop.common.constant.vendor.VendorConstants.*;

/**
 * @author Admin
 * @description 针对表【vendor(供应商表)】的数据库操作Service实现
 * @createDate 2025-07-04 10:14:10
 */
@Service
@RequiredArgsConstructor
public class VendorServiceImpl extends BaseServiceImpl<VendorMapper, Vendor> implements VendorService {

    private final AdminUserService adminUserService;
    private final AdminRoleService adminRoleService;
    private final AdminUserVendorMapper adminUserVendorMapper;
    private final RegionService regionService;
    private final RabbitTemplate rabbitTemplate;
    private final AdminLogService adminLogService;
    private final ConfigService configService;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher publisher;
    private final StatementOutputMapper statementOutputMapper;

    private final ApplicationContext applicationContext;


    @Override
    public Page<VendorVO> list(VendorListPageParam pageQuery) {
        // 分页
        Page<Vendor> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Vendor> queryWrapper = new LambdaQueryWrapper<>();

        if (ObjectUtil.isNotEmpty(pageQuery.getMainAccount())) {
            List<Integer> vendorIds = adminUserService.getBindMainVendorIds(pageQuery.getMainAccount(), true);
            if (ObjectUtil.isEmpty(vendorIds)) {
                return new Page<>(0, pageQuery.getSize());
            } else {
                queryWrapper.in(Vendor::getVendorId, vendorIds);
            }
        }

        if (ObjectUtil.isNotEmpty(pageQuery.getAccount())) {
            List<Integer> vendorIds = adminUserService.getBindMainVendorIds(pageQuery.getAccount(), false);
            if (ObjectUtil.isEmpty(vendorIds)) {
                return new Page<>(0, pageQuery.getSize());
            } else {
                queryWrapper.in(Vendor::getVendorId, vendorIds);
            }
        }

        // 状态筛选
        queryWrapper.eq(pageQuery.getStatus() != null && pageQuery.getStatus() > 0, Vendor::getStatus, pageQuery.getStatus());
        // 搜索关键字
        if (StringUtils.isNotEmpty(pageQuery.getKeyword())) {
            queryWrapper.nested(qw -> {
                qw.like(Vendor::getVendorName, pageQuery.getKeyword())
                        .or()
                        .like(Vendor::getContactMobile, pageQuery.getKeyword());
            });
        }

        if (pageQuery.getVendorIds() != null && !pageQuery.getVendorIds().isEmpty()) {
            queryWrapper.in(Vendor::getVendorId, pageQuery.getVendorIds());
            queryWrapper.orderByDesc(Vendor::getLastLoginTime);
        } else {
            Integer vendorId = getVendorId();
            if (vendorId != null && vendorId > 0) {
                queryWrapper.eq(Vendor::getVendorId, vendorId);
            }
        }
        queryWrapper.orderByDesc(Vendor::getVendorId);
        // 执行查询
        Page<Vendor> vendorPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Vendor> vendorPageRecords = vendorPage.getRecords();
        // 转换为VO
        List<VendorVO> voList = vendorPageRecords.stream().filter(Objects::nonNull).map(vendor -> {
            VendorVO vendorVO = new VendorVO();
            BeanUtil.copyProperties(vendor, vendorVO);
            // 设置联系人姓名
            Optional.of(vendor)
                    .map(Vendor::getVendorData)
                    .filter(StrUtil::isNotBlank)
                    .map(data -> JSONUtil.parse(data).toBean(VendorCreateParam.VendorData.class))
                    .map(VendorCreateParam.VendorData::getContactName)
                    .ifPresent(vendorVO::setContactName);

            Optional<AdminUserVendor> first = adminUserVendorMapper.selectList(new LambdaQueryWrapper<AdminUserVendor>()
                    .eq(AdminUserVendor::getVendorId, vendor.getVendorId())
                    .eq(AdminUserVendor::getIsAdmin, VendorIsAdminEnum.YES.getCode())
            ).stream().findFirst();

            if (first.isPresent()) {
                AdminUser adminUser = adminUserService.getById(first.get().getAdminId());
                vendorVO.setLoginAccount(adminUser.getUsername());
            }
            return vendorVO;
        }).toList();
        return PageUtil.convertPage(vendorPage, voList);
    }

    /**
     * 创建供应商
     * admin_user
     *
     * @param createParam
     */
    @Override
    @Transactional
    public void create(VendorCreateParam createParam) {
        // 新增 vendor
        Vendor vendor = new Vendor();
        vendor.setVendorLogo(createParam.getVendorLogo());
        vendor.setContactMobile(createParam.getVendorData().getContactPhone());
        vendor.setAddTime(StringUtils.getCurrentTime());
        vendor.setVendorData(JSONUtil.toJsonStr(createParam.getVendorData()));
        vendor.setPersonData(JSONUtil.toJsonStr(createParam.getPersonData()));
        vendor.setStatus(VendorStatusEnum.OPEN.getCode());
        vendor.setType(createParam.getType());
        vendor.setVendorName(createParam.getVendorName());
        vendor.setKefuPhone(createParam.getVendorData().getCustomerServicePhone());
        this.save(vendor);
        // 初始化数据（角色）
        AdminRole adminRole = adminRoleService.initVendorRole(vendor.getVendorId());
        // 新增 admin_user 并获取 admin_id
        AdminUser admin = applicationContext.getBean(this.getClass()).getAdmin(createParam);
        // 新增 admin_user_vendor
        AdminUserVendor adminUserVendor = new AdminUserVendor();
        adminUserVendor.setAdminId(admin.getAdminId());
        adminUserVendor.setUserId(createParam.getAdminData().getUserId());
        adminUserVendor.setVendorId(vendor.getVendorId());
        adminUserVendor.setUsername(admin.getUsername());
        adminUserVendor.setAvatar(admin.getAvatar());
        adminUserVendor.setEmail(admin.getEmail());
        adminUserVendor.setAuthList(adminRole.getAuthorityList());
        adminUserVendor.setIsUsing(VendorUsingEnum.YES.getCode());
        adminUserVendor.setIsAdmin(VendorIsAdminEnum.YES.getCode());
        adminUserVendor.setAddTime(StringUtils.getCurrentTime());
        adminUserVendor.setRoleId(adminRole.getRoleId());
        adminUserVendorMapper.insert(adminUserVendor);
    }

    /**
     * 根据用户类型使用原来的管理员或创建新的管理员
     *
     * @param createParam
     * @return AdminUser
     */
    @Transactional
    @Override
    public AdminUser getAdmin(VendorCreateParam createParam) {
        AdminUser admin = null;
        if (ObjectUtil.equals(createParam.getAdminData().getType(), AdminInfoEnum.USER.getCode())) {
            //会员类型
            User user = userMapper.selectById(createParam.getAdminData().getUserId());
            if (user == null) {
                throw new GlobalException(VENDOR_USER_NOT_EXIST);
            }
            String mobile = ObjectUtil.isEmpty(user.getMobile()) ? createParam.getVendorData().getContactPhone() : user.getMobile();
            if (ObjectUtil.isEmpty(mobile)) {
                throw new GlobalException("关联会员或者供应商手机号为空");
            }
            //通过手机查询是否有对应管理员
            admin = adminUserService.getAdminUserByUseMobile(mobile);
            if (admin == null) {
                AdminUser adminUserCreate = applicationContext.getBean(this.getClass()).getAdminUser(user, createParam.getVendorData());
                adminUserService.save(adminUserCreate);
                admin = adminUserCreate;
            }
        } else {
            //管理员类型
            admin = adminUserService.getById(createParam.getAdminData().getAdminId());
            if (admin == null) {
                throw new GlobalException(VENDOR_ADMIN_USER_NOT_EXIST);
            }
        }
        return admin;
    }

    @Transactional
    @Override
    public AdminUser getAdminUser(User user, VendorCreateParam.VendorData vendorData) {
        AdminUser adminUserCreate = new AdminUser();
        adminUserCreate.setUsername(user.getUsername());
        adminUserCreate.setUserId(user.getUserId());
        adminUserCreate.setMobile(user.getMobile() == null ? vendorData.getContactPhone() : user.getMobile());
        adminUserCreate.setEmail(user.getEmail());
        adminUserCreate.setPassword(passwordEncoder.encode(PASSWORD));
        adminUserCreate.setAdminType(VENDOR_TYPE);
        adminUserCreate.setIsUsing(0);
        adminUserCreate.setAuthList("[]");
        adminUserCreate.setRoleId(1);
        // 设置默认头像
        int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
        String format = StrUtil.format("../assets/avatar/{}.jpeg", randomNumber);
        adminUserCreate.setAvatar(format);
        adminUserCreate.setAddTime(StringUtils.getCurrentTime());
        return adminUserCreate;
    }

    @Override
    public void update(VendorUpdateParam updateParam) {
        if (updateParam.getId() == null) {
            throw new GlobalException(VENDOR_NOT_EXIST);
        }
        if (this.getById(updateParam.getId()) == null) {
            throw new GlobalException(VENDOR_NOT_EXIST);
        }
        if (updateParam.getAdminData() != null) {
            // 查询供应商主账号
            ShopOrVendorBindParam shopOrVendorBindParam = new ShopOrVendorBindParam();
            shopOrVendorBindParam.setId(updateParam.getId());
            shopOrVendorBindParam.setType("vendor");
            ShopOrVendorBindParam.AdminInfo adminData = new ShopOrVendorBindParam.AdminInfo();
            BeanUtil.copyProperties(updateParam.getAdminData(), adminData);
            shopOrVendorBindParam.setAdminData(adminData);
            // 立即触发监听器逻辑
            publisher.publishEvent(new VendorMainAccountBindEvent(shopOrVendorBindParam));
        }
        // 新增 vendor
        Vendor vendor = new Vendor();
        vendor.setVendorId(updateParam.getId());
        vendor.setVendorLogo(updateParam.getVendorLogo());
        vendor.setContactMobile(updateParam.getVendorData().getContactPhone());
        vendor.setVendorData(JSONUtil.toJsonStr(updateParam.getVendorData()));
        vendor.setPersonData(JSONUtil.toJsonStr(updateParam.getPersonData()));
        vendor.setType(updateParam.getType());
        vendor.setVendorName(updateParam.getVendorName());
        vendor.setKefuPhone(updateParam.getVendorData().getCustomerServicePhone());
        this.updateById(vendor);
    }

    @Override
    public VendorDetailVO detail(Integer id) {
        Vendor vendor = this.getById(id);
        if (vendor == null) {
            throw new GlobalException(VENDOR_NOT_EXIST);
        }
        VendorDetailVO vendorDetailVO = new VendorDetailVO();
        vendorDetailVO.setType(vendor.getType());
        vendorDetailVO.setVendorLogo(vendor.getVendorLogo());
        // 查询供应商主账号
        LambdaQueryWrapper<AdminUserVendor> vendorQueryWrapper = new LambdaQueryWrapper<>();
        vendorQueryWrapper.eq(AdminUserVendor::getVendorId, id)
                .eq(AdminUserVendor::getIsAdmin, 1);
        AdminUserVendor adminUserVendor = adminUserVendorMapper.selectOne(vendorQueryWrapper);
        if (adminUserVendor != null) {
            VendorDetailVO.AdminInfo adminInfo = new VendorDetailVO.AdminInfo();
            adminInfo.setType(adminUserVendor.getUserId() == 0 ? 2 : 1);
            adminInfo.setAdminId(adminUserVendor.getAdminId());
            adminInfo.setUserId(adminUserVendor.getUserId());
            if (adminInfo.getType() == 2) {
                adminInfo.setAccountName(Optional.ofNullable(adminUserService.getById(adminUserVendor.getAdminId())).map(AdminUser::getUsername).orElse(""));
            } else {
                adminInfo.setAccountName(Optional.ofNullable(userMapper.selectById(adminUserVendor.getUserId())).map(User::getUsername).orElse(""));
            }
            vendorDetailVO.setAdminData(adminInfo);
        }
        vendorDetailVO.setVendorData(JSONUtil.parse(vendor.getVendorData()).toBean(VendorDetailVO.VendorData.class));
        vendorDetailVO.setPersonData(JSONUtil.parse(vendor.getPersonData()).toBean(VendorDetailVO.PersonData.class));
        vendorDetailVO.setVendorName(vendor.getVendorName());
        vendorDetailVO.setStatus(vendor.getStatus());

        VendorDetailVO.VendorData vendorData = vendorDetailVO.getVendorData();
        List<String> vendorAddressRegions = regionService.getRegionNamesByRegionIds(vendorData.getVendorAddress());
        List<String> licenseAddrProvinces = regionService.getRegionNamesByRegionIds(vendorData.getLicenseAddrProvince());
        if (ObjectUtil.isNotEmpty(vendorAddressRegions)) {
            vendorData.setVendorAddressStr(String.join("", vendorAddressRegions));
        }
        if (ObjectUtil.isNotEmpty(licenseAddrProvinces)) {
            vendorData.setLicenseAddrProvinceStr(String.join("", licenseAddrProvinces));
        }
        return vendorDetailVO;
    }

    @Override
    public VendorDetailConfigVO currentDetail() {
        Vendor vendor = this.getById(getVendorId());
        if (vendor == null) {
            throw new GlobalException(VENDOR_NOT_EXIST);
        }
        VendorDetailVO vendorDetailVO = this.detail(getVendorId());
        VendorDetailConfigVO vendorConfigVO = new VendorDetailConfigVO();
        BeanUtil.copyProperties(vendorDetailVO, vendorConfigVO);

        vendorConfigVO.setKefuPhone(vendor.getKefuPhone());
        vendorConfigVO.setDescription(vendor.getDescription());
        vendorConfigVO.setAddTime(vendor.getAddTime().toString());
        return vendorConfigVO;
    }

    @Override
    public void setting(VendorConfigParam param) {
        if (param == null || param.getStatus() == null) {
            throw new GlobalException("参数错误");
        }
        if (!(param.getStatus() == VendorStatusEnum.OPEN.getCode() || param.getStatus() == VendorStatusEnum.CLOSE.getCode())) {
            throw new GlobalException("参数错误");
        }
        Vendor vendor = this.getById(getVendorId());
        if (vendor == null) {
            throw new GlobalException(VENDOR_NOT_EXIST);
        }
        vendor.setStatus(param.getStatus());
        vendor.setKefuPhone(param.getKefuPhone());
        VendorDetailVO.VendorData bean = JSONUtil.parseObj(vendor.getVendorData()).toBean(VendorDetailVO.VendorData.class);
        bean.setCustomerServicePhone(param.getKefuPhone());
        vendor.setVendorData(JSONUtil.toJsonStr(bean));
        this.updateById(vendor);

        if (param.getStatus().equals(VendorStatusEnum.CLOSE.getCode())) {
            // 如果是修改为关闭，则下架所有关联商品
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            VendorDiscontinueDTO dto = new VendorDiscontinueDTO();
            dto.setVendorId(getVendorId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.VENDOR_DISCONTINUE_ROUTING_KEY, dto);
        }
    }

    @Override
    public StaffShowVO staffShow() {
        // 已有员工数
        long totalUsingUser = adminUserVendorMapper.selectCount(new LambdaQueryWrapper<AdminUserVendor>().eq(AdminUserVendor::getIsUsing, 0).eq(AdminUserVendor::getVendorId, getVendorId()));
        // 子管理员数
        long subUsingUser = adminUserVendorMapper.selectCount(new LambdaQueryWrapper<AdminUserVendor>().eq(AdminUserVendor::getIsUsing, 0).eq(AdminUserVendor::getVendorId, getVendorId()).eq(AdminUserVendor::getIsAdmin, 0));
        // 停用员工数
        long stopUsingUser = adminUserVendorMapper.selectCount(new LambdaQueryWrapper<AdminUserVendor>().eq(AdminUserVendor::getIsUsing, 1).eq(AdminUserVendor::getVendorId, getVendorId()));

        ConfigPO maxSubAdministratorConfig = configService.getConfigByCode(SettingsEnum.VENDOR_MAX_SUB_ADMINISTRATOR.getBizCode());
        long maxAdminUserVendorCount = StrUtil.isNotBlank(maxSubAdministratorConfig.getBizVal()) ? Long.parseLong(maxSubAdministratorConfig.getBizVal()) : 0L;
        long residue = maxAdminUserVendorCount > 0 ? maxAdminUserVendorCount - (subUsingUser + stopUsingUser) : 0;

        List<Integer> adminIds = adminUserVendorMapper.selectList(new LambdaQueryWrapper<AdminUserVendor>().eq(AdminUserVendor::getVendorId, getVendorId()))
                .stream()
                .map(AdminUserVendor::getAdminId)
                .toList();

        List<AdminLog> list = adminLogService.list(new LambdaQueryWrapper<AdminLog>()
                .in(ObjectUtil.isNotEmpty(adminIds), AdminLog::getUserId, adminIds)
                .orderByDesc(AdminLog::getLogId)
                .last("limit 5")
        );

        StaffShowVO staffShowVO = new StaffShowVO();
        staffShowVO.setUsingUser((int) totalUsingUser);
        staffShowVO.setStopUsingUser((int) stopUsingUser);
        staffShowVO.setResidue((int) residue);
        staffShowVO.setAdminLog(list.stream().map(item -> {
            StaffShowVO.AdminLogVO adminLogVO = new StaffShowVO.AdminLogVO();
            adminLogVO.setLogId(item.getLogId());
            DateTime date = DateUtil.date(item.getLogTime() * 1000);
            adminLogVO.setLogTime(DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));
            adminLogVO.setLogInfo(item.getLogInfo());
            adminLogVO.setIpAddress(item.getIpAddress());
            adminLogVO.setUserId(item.getUserId());
            AdminUser adminUser = adminUserService.getById(item.getUserId());
            if (adminUser != null) {
                adminLogVO.setUsername(adminUser.getUsername());
            }
            return adminLogVO;
        }).toList());
        return staffShowVO;
    }

    @Override
    public void updateInfo(VendorUpdateInfoParam param) {
        Vendor vendor = getById(getVendorId());
        vendor.setVendorLogo(param.getVendorLogo());
        vendor.setVendorName(param.getVendorName());
        vendor.setContactMobile(param.getContactPhone());

        VendorDetailVO.VendorData bean = JSONUtil.parse(vendor.getVendorData()).toBean(VendorDetailVO.VendorData.class);
        bean.setVendorAddress(param.getVendorAddress());
        bean.setDetailedAddress(param.getDetailedAddress());
        bean.setContactPhone(param.getContactPhone());
        bean.setVendorName(param.getVendorName());
        vendor.setVendorData(JSONUtil.toJsonStr(bean));

        vendor.setDescription(param.getDescription());
        updateById(vendor);
    }

    @Override
    public BigDecimal getSumVendorMoney() {
        QueryWrapper<Vendor> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sum(vendor_money) as vendor_money");
        Vendor vendor = this.getOne(queryWrapper);
        return null != vendor ? vendor.getVendorMoney() : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getSumFrozenMoney() {
        QueryWrapper<Vendor> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sum(frozen_money) as frozen_money");
        Vendor vendor = this.getOne(queryWrapper);
        return null != vendor ? vendor.getFrozenMoney() : BigDecimal.ZERO;
    }

    @Override
    public Page<VendorAccountFundVO> listFund(VendorListPageParam pageQuery) {
        // 分页
        Page<Vendor> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Vendor> queryWrapper = new LambdaQueryWrapper<>();
        // 排序字段
        buildSortOrder(page, pageQuery.getSortField(), pageQuery.getSortOrder());
        // 状态筛选
        queryWrapper.eq(pageQuery.getStatus() != null && pageQuery.getStatus() > 0, Vendor::getStatus, pageQuery.getStatus());
        // 搜索关键字
        if (StringUtils.isNotEmpty(pageQuery.getKeyword())) {
            queryWrapper.nested(qw -> {
                qw.like(Vendor::getVendorName, pageQuery.getKeyword());
            });
        }
        // 执行查询
        Page<Vendor> vendorPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Vendor> vendorPageRecords = vendorPage.getRecords();
        // 转换为VO
        List<VendorAccountFundVO> voList = vendorPageRecords.stream().filter(Objects::nonNull).map(vendor -> {
            VendorAccountFundVO vendorVO = new VendorAccountFundVO();
            BeanUtil.copyProperties(vendor, vendorVO);
            vendorVO.setUnSettlementMoney(getOrderUnSettlementAmountByVendor(vendor.getVendorId()));
            return vendorVO;
        }).toList();
        return PageUtil.convertPage(vendorPage, voList);
    }

    // orderService getOrderUnSettlementAmountByVendor 这个循环依赖问题
    public BigDecimal getOrderUnSettlementAmountByVendor(Integer vendorId) {
        LambdaQueryWrapper<StatementOutput> eq = new LambdaQueryWrapper<StatementOutput>()
                .in(StatementOutput::getSettlementStatus, StatementType.SERVICE_FEE.getCode(), StatementType.ORDER.getCode(), StatementType.ORDER_REFUND.getCode())
                .eq(StatementOutput::getRealSettlement, NO)
                .eq(StatementOutput::getVendorId, vendorId);
        List<StatementOutput> statementOutputs = statementOutputMapper.selectList(eq);
        if (ObjectUtil.isEmpty(statementOutputs)) {
            return BigDecimal.ZERO;
        }
        BigDecimal in = statementOutputs.stream().map(StatementOutput::getIncome).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        BigDecimal out = statementOutputs.stream().map(StatementOutput::getExpenditure).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        return in.subtract(out);
    }

    @Override
    public boolean updateField(UpdateFieldDTO updateField, String[] allowFields) {
        boolean updated = super.updateField(updateField, allowFields);
        if ("status".equals(updateField.getField()) &&
                Integer.parseInt(updateField.getVal()) == VendorStatusEnum.CLOSE.getCode() &&
                updated) {
            log.error("供应商已关闭");
            // 如果是修改为关闭，则下架所有关联商品
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            VendorDiscontinueDTO dto = new VendorDiscontinueDTO();
            dto.setVendorId(updateField.getId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.VENDOR_DISCONTINUE_ROUTING_KEY, dto);
        }
        return updated;
    }
}




