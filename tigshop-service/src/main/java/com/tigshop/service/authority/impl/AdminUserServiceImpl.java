// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.authority.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.authority.AdminUserDTO;
import com.tigshop.bean.dto.authority.AdminUserModifyManageAccountsDTO;
import com.tigshop.bean.dto.login.RegisterSmsDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.vendor.AdminUserVendor;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.query.authority.adminuser.AdminUserPageQuery;
import com.tigshop.bean.vo.shop.ShopVO;
import com.tigshop.bean.vo.vendor.VendorVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.merchant.MerchantMapper;
import com.tigshop.mapper.shop.AdminUserShopMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.vendor.AdminUserVendorMapper;
import com.tigshop.mapper.vendor.VendorMapper;
import com.tigshop.service.authority.AdminRoleService;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.Constants.ADMIN_TOKEN;
import static com.tigshop.common.constant.Constants.ADMIN_USER;
import static com.tigshop.common.constant.authority.AdminUserConstants.*;
import static com.tigshop.common.constant.login.LoginConstants.*;

/**
 * 后台用户
 *
 * @author Jayce
 * @create 2024-09-27 10:34:44
 */
@RequiredArgsConstructor
@Service
public class AdminUserServiceImpl extends BaseServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {

    @Resource
    private RedisTemplate<String, AdminUser> redisTemplate;

    private final RedisCache redisCache;

    private final StringRedisTemplate stringRedisTemplate;

    private final PasswordEncoder passwordEncoder;

    private final AdminRoleService adminRoleService;

    private final ConfigService configService;

    private final ShopMapper shopMapper;

    private final MerchantMapper merchantMapper;

    private final AdminUserShopMapper adminUserShopMapper;

    private final AdminUserVendorMapper adminUserVendorMapper;

    private final VendorMapper vendorMapper;


    @Override
    public boolean create(AdminUserDTO adminUserDTO) {
        // 如果手机号原来的账号不为管理员类型，则将其改为管理员类型，并保留原来的店铺和供应商
        AdminUser adminUser = this.getOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getMobile, adminUserDTO.getMobile()));
        if (adminUser != null) {
            if (AdminTypeEnum.fromCode(adminUser.getAdminType()) == AdminTypeEnum.ADMIN) {
                throw new GlobalException(MOBILE_IS_EXIST);
            } else {
                adminUser.setAdminType(AdminTypeEnum.ADMIN.getCode());
                adminUser.setRoleId(adminUserDTO.getRoleId());
                // 设置权限
                if (adminUserDTO.getRoleId() != null && adminUserDTO.getRoleId() > 0) {
                    adminUser.setAuthList(adminRoleService.getOne(new LambdaQueryWrapper<AdminRole>()
                                    .eq(AdminRole::getRoleId, adminUserDTO.getRoleId()))
                            .getAuthorityList()
                    );
                } else {
                    adminUser.setAuthList(
                            Optional.ofNullable(adminUserDTO.getAuthList())
                                    .map(JSONUtil::toJsonStr)
                                    .orElse("[]")
                    );
                }
                this.updateById(adminUser);
                return true;
            }
        }

        return this.save(buildAdminUser(adminUserDTO));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUser providerCreate(AdminUserDTO adminUserDTO) {
        AdminUser adminUser = buildAdminUser(adminUserDTO);
        boolean save = this.save(adminUser);
        if (!save) {
            return null;
        }
        return adminUser;
    }

    @Override
    public boolean modifyManageAccounts(AdminUserModifyManageAccountsDTO adminUserModifyManageAccountsDTO) {
        // 验证验证码
        if (ObjectUtil.isNotEmpty(adminUserModifyManageAccountsDTO.getCode())) {
            // 验证验证码 需要加上之前发送验证码时带的 event
            if (!adminUserModifyManageAccountsDTO.getCode().equals(String.valueOf(redisTemplate.opsForValue().get("login" + adminUserModifyManageAccountsDTO.getMobile())))) {
                throw new GlobalException("短信验证码错误或已过期，请重试");
            }
        }

        Integer currentAdminId = SecurityUtils.getCurrentAdminId();
        AdminUser adminUser = this.getById(currentAdminId);
        AdminUser adminUserUpdate = new AdminUser();
        adminUserUpdate.setAdminId(currentAdminId);
        switch (adminUserModifyManageAccountsDTO.getModifyType()) {
            case 1 -> {
                //修改个人信息
                adminUserUpdate.setAvatar(adminUserModifyManageAccountsDTO.getAvatar());
                adminUserUpdate.setEmail(adminUserModifyManageAccountsDTO.getEmail());
            }
            case 2 -> {
                //修改密码
                if (adminUserModifyManageAccountsDTO.getOldPassword() != null) {
                    if (!passwordEncoder.matches(adminUserModifyManageAccountsDTO.getOldPassword(), adminUser.getPassword())) {
                        throw new GlobalException(OLD_PASSWORD_ERROR);
                    }
                }
                if (adminUserModifyManageAccountsDTO.getPassword() != null) {
                    adminUserUpdate.setPassword(passwordEncoder.encode(adminUserModifyManageAccountsDTO.getPassword()));
                    // 清空 redis 中 admin_user 信息
                    redisTemplate.delete(StrUtil.format("{}{}", ADMIN_USER, adminUser.getUsername()));
                    redisTemplate.delete(StrUtil.format("{}{}", ADMIN_TOKEN, adminUser.getUsername()));
                }
                if (!Objects.equals(adminUserModifyManageAccountsDTO.getPassword(), adminUserModifyManageAccountsDTO.getPwdConfirm())) {
                    throw new GlobalException(PASSWORD_NOT_CONSISTENT);
                }
                redisCache.deleteObject(Constants.PASSWORD_TOO_SIMPLE + currentAdminId);
            }
            case 3 -> {
                // 修改手机号
                if (this.count(new LambdaQueryWrapper<AdminUser>()
                        .ne(AdminUser::getAdminId, currentAdminId)
                        .eq(AdminUser::getMobile, adminUserModifyManageAccountsDTO.getMobile())) > 0) {
                    // 判断手机号是否被其他人使用
                    throw new GlobalException(MOBILE_USED);
                }
                adminUserUpdate.setMobile(adminUserModifyManageAccountsDTO.getMobile());
                if (adminUserModifyManageAccountsDTO.getCode() == null) {
                    throw new GlobalException(USER_NOT_CODE);
                }
                String mobileCode = adminUserModifyManageAccountsDTO.getCode();

                if (StrUtil.isEmpty(adminUserModifyManageAccountsDTO.getCode())) {
                    throw new GlobalException("验证码为空");
                }
                String redisCode = stringRedisTemplate.opsForValue().get("login" + adminUserModifyManageAccountsDTO.getMobile());
                if (!mobileCode.equals(redisCode)) {
                    throw new GlobalException("短信验证码错误或已过期，请重试");
                }
            }
        }
        return updateById(adminUserUpdate);
    }

    @Override
    public Integer getMerchantId(Integer adminId) {
        AdminUser adminUser = this.getById(adminId);

        if (adminUser != null) {
            return adminUser.getMerchantId();
        } else {
            throw new GlobalException("未登录");
        }
    }

    @Override
    public List<AdminRole> getConfig(String adminType) {
        AdminTypeEnum adminTypeEnum = AdminTypeEnum.fromCode(getAdminType());
        switch (adminTypeEnum) {
            case AdminTypeEnum.ADMIN:
                return adminRoleService.lambdaQuery()
                        .eq(AdminRole::getShopId, getShopId())
                        .eq(AdminRole::getMerchantId, 0)
                        .eq(AdminRole::getVendorId, 0)
                        .list();
            case AdminTypeEnum.SHOP:
            case AdminTypeEnum.STORE:
            case AdminTypeEnum.PICKUP:
                Shop shop = shopMapper.selectById(getShopId());
                if (shop == null) {
                    throw new GlobalException("店铺不存在");
                }

                Merchant merchant = merchantMapper.selectById(shop.getMerchantId());
                if (merchant == null) {
                    throw new GlobalException("商户不存在");
                }
                return adminRoleService.lambdaQuery()
                        .eq(AdminRole::getMerchantId, merchant.getMerchantId())
                        .or()
                        .eq(AdminRole::getShopId, getShopId())
                        .list();
            case AdminTypeEnum.VENDOR:
                return adminRoleService.lambdaQuery()
                        .eq(AdminRole::getVendorId, getVendorId())
                        .list();
            default:
                throw new GlobalException("管理员类型错误");
        }
    }

    @Override
    public void getCode(String type, String mobile, String verifyToken) {
        if (StrUtil.isEmpty(mobile)) {
            AdminUser adminUser = this.getById(SecurityUtils.getCurrentAdminId());
            if (adminUser == null) {
                throw new GlobalException("手机号不能为空");
            }
            mobile = adminUser.getMobile();
        }
        RegisterSmsDTO dto = new RegisterSmsDTO();
        dto.setEvent("login");
        dto.setMobile(mobile);
        dto.setVerifyToken(verifyToken);
        configService.sendSms(dto);
    }

    @Override
    public List<Integer> getBindMainShopIds(String mainAccount, Boolean onlyAdmin) {
        return getBaseMapper().getBindMainShopIds(mainAccount, onlyAdmin);
    }

    @Override
    public List<Integer> getBindMainVendorIds(String mainAccount, Boolean onlyAdmin) {
        return getBaseMapper().getBindMainVendorIds(mainAccount, onlyAdmin);
    }

    @Override
    public AdminUserDTO detail(Integer id) {
        // 获取用户信息，若不存在则抛出异常
        AdminUser adminUser = this.getById(id);
        Assert.notNull(adminUser, () -> new GlobalException(USER_NOT_EXIST));

        // 复制属性
        AdminUserDTO adminUserDTO = new AdminUserDTO();
        BeanUtils.copyProperties(adminUser, adminUserDTO);

        // 解析权限列表，若为空则设置为空列表
        adminUserDTO.setAuthList(
                "[]".equals(adminUser.getAuthList())
                        ? Collections.emptyList()
                        : Arrays.asList(StringUtils.str2Array(adminUser.getAuthList()))
        );

        List<AdminUserShop> adminUserShops = adminUserShopMapper.selectList(
                Wrappers.lambdaQuery(AdminUserShop.class).eq(AdminUserShop::getAdminId, adminUser.getAdminId())
        );
        if (ObjectUtil.isNotEmpty(adminUserShops)) {
            List<AdminUserDTO.UserShopDTO> userShops = adminUserShops.stream().map(adminUserShop -> {
                AdminUserDTO.UserShopDTO userShop = new AdminUserDTO.UserShopDTO();
                Shop shop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>().eq(Shop::getShopId, adminUserShop.getShopId()));
                ShopVO shopVO = new ShopVO();
                long timestampInSeconds = shop.getAddTime();
                Date date = new Date(timestampInSeconds * 1000L);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                shopVO.setAddTime(sdf.format(date));
                shopVO.setContactMobile(shop.getContactMobile());
                shopVO.setShopId(shop.getShopId());
                shopVO.setShopLogo(shop.getShopLogo());
                shopVO.setShopTitle(shop.getShopTitle());
                shopVO.setStatus(shop.getStatus());
                shopVO.setStatusText(ShopType.getTypeName(shop.getStatus()));
                userShop.setShop(shopVO);
                return userShop;
            }).toList();
            adminUserDTO.setUserShop(userShops);
        }

        List<AdminUserVendor> adminUserVendors = adminUserVendorMapper.selectList(
                Wrappers.lambdaQuery(AdminUserVendor.class).eq(AdminUserVendor::getAdminId, adminUser.getAdminId())
        );
        if (ObjectUtil.isNotEmpty(adminUserVendors)) {
            List<AdminUserDTO.UserVendorDTO> userVendors = adminUserVendors.stream().map(adminUserVendor -> {
                AdminUserDTO.UserVendorDTO userVendor = new AdminUserDTO.UserVendorDTO();
                Vendor vendor = vendorMapper.selectOne(new LambdaQueryWrapper<Vendor>().eq(Vendor::getVendorId, adminUserVendor.getVendorId()));
                VendorVO vendorVO = new VendorVO();
                long timestampInSeconds = vendor.getAddTime();
                Date date = new Date(timestampInSeconds * 1000L);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                vendorVO.setAddTime(sdf.format(date));
                vendorVO.setContactMobile(vendor.getContactMobile());
                vendorVO.setVendorId(vendor.getVendorId());
                vendorVO.setVendorLogo(vendor.getVendorLogo());
                vendorVO.setVendorName(vendor.getVendorName());
                vendorVO.setStatus(vendor.getStatus());
                userVendor.setVendor(vendorVO);
                return userVendor;
            }).toList();
            adminUserDTO.setUserVendor(userVendors);
        }

        return adminUserDTO;
    }

    @Override
    public AdminUser getAdminUserByUsername(String username, Boolean cache) {
        AdminUser adminUser = null;
        if (cache) {
            adminUser = redisTemplate.opsForValue().get(StrUtil.format("{}{}", ADMIN_USER, username));
            if (adminUser != null) {
                return adminUser;
            }
        }
        Wrapper<AdminUser> wrapper = Wrappers.<AdminUser>lambdaQuery()
                .eq(AdminUser::getUsername, username);
        adminUser = this.getOne(wrapper);

        if (adminUser != null) {
            redisTemplate.opsForValue().set(StrUtil.format("{}{}", ADMIN_USER, adminUser.getUsername()), adminUser);
            return adminUser;
        }
        return null;
    }

    @Override
    public AdminUser getAdminUserByUseMobile(String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            return null;
        }
        return this.lambdaQuery().eq(AdminUser::getMobile, mobile).one();
    }

    @Override
    public List<AdminUser> getAdminUserByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AdminUser::getAdminId, ids);
        return this.list(queryWrapper);
    }

    @Override
    public boolean adminUserDel(Integer id) {
        if (id == null) {
            return false;
        }

        if (id.equals(SecurityUtils.getCurrentAdminId())) {
            throw new GlobalException("不能删除当前登录用户");
        }

        // 获取用户信息，若不存在则抛出异常
        AdminUser adminUser = Optional.ofNullable(this.getById(id))
                .orElseThrow(() -> new GlobalException(USER_NOT_EXIST));
        // 删除改后台用户
        boolean isDeleted = this.removeById(id);
        // 删除成功后需要清除后台的缓存
        if (isDeleted) {
            redisTemplate.delete(StrUtil.format("{}{}", ADMIN_TOKEN, adminUser.getUsername()));
        }
        return isDeleted;
    }

    @Override
    public Page<AdminUserDTO> list(AdminUserPageQuery pageQuery) {
        String adminUserType = "admin,suppliers";
        if ("shop".equals(pageQuery.getAdminUserType())) {
            adminUserType = "shop";
        }
        if ("vendor".equals(pageQuery.getAdminUserType())) {
            adminUserType = "vendor";
        }

        List<String> types = StrUtil.split(adminUserType, StrUtil.COMMA);

        // 分页参数
        Page<AdminUser> adminUserPage = buildSortOrder(pageQuery);

        adminUserPage = this.lambdaQuery()
                .and(StrUtil.isNotBlank(pageQuery.getKeyword()), query ->
                        query.like(AdminUser::getUsername, pageQuery.getKeyword())
                                .or()
                                .like(AdminUser::getMobile, pageQuery.getKeyword()))
                .in(ObjectUtil.isNotEmpty(pageQuery.getAdminUserType()), AdminUser::getAdminType, types)
                .eq(StrUtil.isNotBlank(pageQuery.getSuppliersId()), AdminUser::getSuppliersId, pageQuery.getSuppliersId())
                .page(adminUserPage);

        List<AdminUser> records = adminUserPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageUtil.convertPage(adminUserPage, new ArrayList<>());
        }
        List<Integer> roleIds = records.stream()
                .map(AdminUser::getRoleId)
                .distinct()
                .toList();
        List<AdminRole> adminRoleList = adminRoleService.lambdaQuery()
                .in(AdminRole::getRoleId, roleIds)
                .list();
        Map<Integer, AdminRole> adminRoleMap = adminRoleList.stream()
                .collect(Collectors.toMap(AdminRole::getRoleId, Function.identity()));

        // 将 AdminUser 对象列表转换为 AdminUserDTO 对象列表
        List<AdminUserDTO> recordsDTO = records.stream()
                .map(adminUser -> {
                    AdminUserDTO userDTO = new AdminUserDTO();
                    // 复制基本属性
                    BeanUtils.copyProperties(adminUser, userDTO);
                    userDTO.setAddTime(adminUser.getAddTime().toString());
                    AdminRole adminRole = adminRoleMap.get(adminUser.getRoleId());
                    if (adminRole != null) {
                        userDTO.setRoleName(adminRole.getRoleName());
                    }
                    // 将 String 类型的 authList 转换为 List<String>
                    if (adminUser.getAuthList() != null) {
                        userDTO.setAuthList(Arrays.asList(adminUser.getAuthList().split(",")));
                    }
                    return userDTO;
                })
                .toList();
        return PageUtil.convertPage(adminUserPage, recordsDTO);
    }

    @Override
    @CacheEvict(value = "admin_user", key = "#adminUserDTO.getUsername()")
    public boolean update(AdminUserDTO adminUserDTO) {
        return this.updateById(buildAdminUser(adminUserDTO));
    }

    /**
     * 构建 AdminUser 对象
     *
     * @param adminUserDTO 前端提交的数据
     * @return AdminUser
     */
    private AdminUser buildAdminUser(AdminUserDTO adminUserDTO) {
        // 判断用户名是否重复
        long usernameCount = this.count(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, adminUserDTO.getUsername())
                .ne(adminUserDTO.getAdminId() != null, AdminUser::getAdminId, adminUserDTO.getAdminId())
        );

        if (usernameCount > 0) {
            throw new GlobalException(USERNAME_IS_EXIST);
        }

        long mobileCount = this.count(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getMobile, adminUserDTO.getMobile())
                .ne(adminUserDTO.getAdminId() != null, AdminUser::getAdminId, adminUserDTO.getAdminId())
        );

        if (mobileCount > 0) {
            throw new GlobalException(MOBILE_IS_EXIST);
        }
        if (StrUtil.isNotBlank(adminUserDTO.getEmail())) {
            boolean match = ReUtil.isMatch("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", adminUserDTO.getEmail());
            if (!match) {
                throw new GlobalException("邮箱格式不正确");
            }

            long emailCount = this.count(new LambdaQueryWrapper<AdminUser>()
                    .eq(AdminUser::getEmail, adminUserDTO.getEmail())
                    .ne(adminUserDTO.getAdminId() != null, AdminUser::getAdminId, adminUserDTO.getAdminId())
            );

            if (emailCount > 0) {
                throw new GlobalException(EMAIL_IS_EXIST);
            }
        }


        // 设置默认 roleId
        Integer roleId = adminUserDTO.getRoleId();
        adminUserDTO.setRoleId(Optional.ofNullable(roleId).orElse(0));

        AdminUser adminUser = new AdminUser();
        BeanUtils.copyProperties(adminUserDTO, adminUser);

        String avatar = adminUserDTO.getAvatar();
        if (ObjectUtil.isEmpty(avatar)) {
            int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
            String format = StrUtil.format("../assets/avatar/{}.jpeg", randomNumber);
            adminUser.setAvatar(format);
        }

        Integer suppliersId = adminUserDTO.getSuppliersId();
        if (suppliersId != null && suppliersId != 0) {
            adminUser.setAdminType("suppliers");
        }
        String oldPassword = adminUserDTO.getOldPassword();
        if (StrUtil.isNotEmpty(oldPassword)) {
            AdminUser adminUserById = this.getById(adminUserDTO.getAdminId());
            boolean matches = passwordEncoder.matches(adminUserById.getPassword(), oldPassword);
            if (!matches) {
                throw new GlobalException(OLD_PASSWORD_ERROR);
            }

        }
        String password = adminUserDTO.getPassword();
        if (StrUtil.isNotEmpty(password)) {
            adminUser.setPassword(passwordEncoder.encode(password));
        }

        String pwdConfirm = adminUserDTO.getPwdConfirm();
        if (StrUtil.isNotEmpty(pwdConfirm) && StrUtil.isNotEmpty(password) && !password.equals(pwdConfirm)) {
            throw new GlobalException(PASSWORD_NOT_CONSISTENT);
        }
        // 设置初始密码
        if (StrUtil.isNotEmpty(adminUserDTO.getInitialPassword())) {
            adminUser.setPassword(passwordEncoder.encode(adminUserDTO.getInitialPassword()));
        }
        // 设置权限
        if (adminUserDTO.getRoleId() > 0) {
            adminUser.setAuthList(adminRoleService.getOne(new LambdaQueryWrapper<AdminRole>()
                            .eq(AdminRole::getRoleId, adminUserDTO.getRoleId()))
                    .getAuthorityList()
            );
        } else {
            adminUser.setAuthList(
                    Optional.ofNullable(adminUserDTO.getAuthList())
                            .map(JSONUtil::toJsonStr)
                            .orElse("[]")
            );
        }
        adminUser.setAddTime(StringUtils.getCurrentTime());
        return adminUser;
    }
}