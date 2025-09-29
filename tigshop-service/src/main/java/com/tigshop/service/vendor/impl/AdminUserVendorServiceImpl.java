package com.tigshop.service.vendor.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.vendor.AdminUserVendor;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.param.vendor.AdminUserVendorCreateParam;
import com.tigshop.bean.param.vendor.AdminUserVendorModifyParam;
import com.tigshop.bean.param.vendor.AdminUserVendorPageParam;
import com.tigshop.bean.param.vendor.AdminUserVendorUpdateParam;
import com.tigshop.bean.vo.vendor.AdminUserVendorVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.authority.AdminRoleMapper;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.vendor.AdminUserVendorMapper;
import com.tigshop.mapper.vendor.VendorMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.vendor.AdminUserVendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.authority.AdminUserConstants.*;
import static com.tigshop.common.constant.vendor.AdminUserVendorConstants.ADMIN_USER_VENDOR_NOT_DELETE_SELF;
import static com.tigshop.common.constant.vendor.AdminUserVendorConstants.ADMIN_USER_VENDOR_NOT_EXIST;
import static com.tigshop.common.constant.vendor.VendorConstants.PASSWORD_NOT_EQUAL;
import static com.tigshop.common.constant.vendor.VendorConstants.VENDOR_NOT_EXIST;


/**
 * @author Admin
 * @description 针对表【admin_user_vendor(供应商员工列表)】的数据库操作Service实现
 * @createDate 2025-07-04 13:23:56
 */
@RequiredArgsConstructor
@Service
public class AdminUserVendorServiceImpl extends BaseServiceImpl<AdminUserVendorMapper, AdminUserVendor> implements AdminUserVendorService {

    private final AdminUserMapper adminUserMapper;
    private final UserMapper userMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final ConfigService configService;
    private final PasswordEncoder passwordEncoder;
    private final AdminLogService adminLogService;
    private final VendorMapper vendorMapper;

    @Override
    public List<Integer> getVendorIds(int adminId) {
        List<Integer> vendorIds = this.list(new LambdaQueryWrapper<AdminUserVendor>()
                        .eq(AdminUserVendor::getAdminId, adminId)
                        .select(AdminUserVendor::getVendorId))
                .stream()
                .map(AdminUserVendor::getVendorId)
                .toList();
        if (vendorIds.isEmpty()) {
            return new ArrayList<>();
        }
        return vendorIds;
    }

    @Override
    public AdminUserVendorVO info(Integer id, Integer vendorId) {
        LambdaQueryWrapper<AdminUserVendor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminUserVendor::getAdminId, id)
                .eq(AdminUserVendor::getVendorId, vendorId);
        AdminUserVendor adminUserVendor = this.getOne(queryWrapper);
        if (adminUserVendor == null) {
            throw new GlobalException(ADMIN_USER_VENDOR_NOT_EXIST);
        }
        AdminUser adminUser = adminUserMapper.selectById(adminUserVendor.getAdminId());
        AdminUserVendorVO adminUserVendorVO = handleRes(adminUserVendor);
        return getAdminUserVendorItem(adminUser, adminUserVendorVO);
    }

    @Override
    @Transactional
    public void modifyUser(AdminUserVendorModifyParam modifyParam) {
        AdminUserVendor adminUserVendor = this.getById(modifyParam.getId());
        if (adminUserVendor == null) {
            throw new GlobalException(ADMIN_USER_VENDOR_NOT_EXIST);
        }
        if (ObjectUtil.isEmpty(modifyParam.getAvatar())) {
            int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
            String format = StrUtil.format("../assets/avatar/{}.jpeg", randomNumber);
            modifyParam.setAvatar(format);
        }
        BeanUtils.copyProperties(modifyParam, adminUserVendor);
        this.updateById(adminUserVendor);
    }

    @Override
    public Page<AdminUserVendorVO> list(AdminUserVendorPageParam pageQuery) {
        Page<AdminUserVendor> page = buildSortOrder(pageQuery);
        Page<AdminUserVendor> vendorPage = this.lambdaQuery()
                .eq(pageQuery.getVendorId() != null && pageQuery.getVendorId() > 0, AdminUserVendor::getVendorId, pageQuery.getVendorId())
                .like(StrUtil.isNotBlank(pageQuery.getKeyword()), AdminUserVendor::getUsername, pageQuery.getKeyword())
                .page(page);

        List<AdminUserVendor> adminVendorRecords = vendorPage.getRecords();
        if (CollUtil.isEmpty(adminVendorRecords)) {
            return new Page<>();
        }

        // 查出admin_user信息
        List<Integer> adminIds = adminVendorRecords.stream().map(AdminUserVendor::getAdminId).toList();
        List<AdminUser> adminUsers = adminUserMapper.selectBatchIds(adminIds);
        Map<Integer, AdminUser> adminUserMap = adminUsers.stream().collect(Collectors.toMap(AdminUser::getAdminId, Function.identity()));

        // 查出角色
        List<Integer> roleIds = adminVendorRecords.stream().map(AdminUserVendor::getRoleId).toList();
        List<AdminRole> roles = adminRoleMapper.selectBatchIds(roleIds);
        Map<Integer, AdminRole> roleMap = roles.stream().collect(Collectors.toMap(AdminRole::getRoleId, Function.identity()));

        // 转换为VO
        List<AdminUserVendorVO> adminUserVendorVOList = adminVendorRecords.stream()
                .map(adminUserVendor -> {
                    AdminUserVendorVO adminUserVendorVO = new AdminUserVendorVO(adminUserVendor);

                    AdminUser adminUser = adminUserMap.get(adminUserVendor.getAdminId());
                    AdminRole adminRole = roleMap.get(adminUserVendor.getRoleId());
                    adminUserVendorVO.assembleData(adminUser, adminRole);
                    Vendor vendor = vendorMapper.selectById(adminUserVendor.getVendorId());
                    if (vendor != null) {
                        adminUserVendorVO.setVendorName(vendor.getVendorName());
                    }
                    return adminUserVendorVO;
                })
                .toList();
        return PageUtil.convertPage(vendorPage, adminUserVendorVOList);
    }

    @Override
    public AdminUserVendorVO detail(Integer id) {
        AdminUserVendor adminUserVendor = this.getById(id);
        if (adminUserVendor == null) {
            return null;
        }
        AdminUser adminUser = adminUserMapper.selectById(adminUserVendor.getAdminId());
        AdminUserVendorVO adminUserVendorVO = handleRes(adminUserVendor);
        Vendor vendor = vendorMapper.selectById(adminUserVendor.getVendorId());
        if (vendor != null) {
            adminUserVendorVO.setVendorName(vendor.getVendorName());
        }
        return getAdminUserVendorItem(adminUser, adminUserVendorVO);
    }

    @Override
    @Transactional
    public void createAdminUserVendor(AdminUserVendorCreateParam createParam) {
        if (!ObjectUtil.equals(createParam.getPassword(), createParam.getPwdConfirm())) {
            throw new GlobalException(PASSWORD_NOT_EQUAL);
        }
        if (createParam.getVendorId() == null) {
            throw new GlobalException(VENDOR_NOT_EXIST);
        }
        if (vendorMapper.selectById(createParam.getVendorId()) == null) {
            throw new GlobalException(VENDOR_NOT_EXIST);
        }
        // 判断用户名是否重复
        long usernameCount = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, createParam.getUsername())
                .ne(AdminUser::getMobile, createParam.getMobile())
                .ne(createParam.getAdminId() != null, AdminUser::getAdminId, createParam.getAdminId())
        );

        if (usernameCount > 0) {
            throw new GlobalException(USERNAME_IS_EXIST);
        }

        long mobileCount = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getMobile, createParam.getMobile())
                .ne(createParam.getAdminId() != null, AdminUser::getAdminId, createParam.getAdminId())
        );

        long hasAdmin = this.count(new LambdaQueryWrapper<AdminUserVendor>()
                .eq(AdminUserVendor::getVendorId, createParam.getVendorId())
                .eq(AdminUserVendor::getIsAdmin, 1));

        if (hasAdmin == 0) {
            createParam.setIsAdmin(1);
        }

        if (createParam.getVendorId() > 0) {
            //获取admin_user_vendor员工数量
            long count = this.count(new LambdaQueryWrapper<AdminUserVendor>()
                    .eq(AdminUserVendor::getVendorId, createParam.getVendorId())
                    .eq(AdminUserVendor::getIsAdmin, 0));
            ConfigPO maxSubAdministratorConfig = configService.getConfigByCode(SettingsEnum.VENDOR_MAX_SUB_ADMINISTRATOR.getBizCode());
            long maxAdminUserVendorCount = StrUtil.isNotBlank(maxSubAdministratorConfig.getBizVal()) ? Long.parseLong(maxSubAdministratorConfig.getBizVal()) : 0L;
            if (count >= maxAdminUserVendorCount) {
                throw new GlobalException("供应商员工数已达上限,上限为" + maxAdminUserVendorCount + "个,如需修改,请前往配置");
            }
        }

        if (ObjectUtil.isEmpty(createParam.getAvatar())) {
            int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
            String format = StrUtil.format("../assets/avatar/{}.jpeg", randomNumber);
            createParam.setAvatar(format);
        }

        if (!(createParam.getAdminId() != null && createParam.getAdminId() > 0)) {
            LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AdminUser::getMobile, createParam.getMobile());
            AdminUser adminUser = adminUserMapper.selectOne(queryWrapper);
            if (adminUser == null) {
                AdminUser adminUserSave = new AdminUser();
                BeanUtils.copyProperties(createParam, adminUserSave);
                adminUserSave.setAdminType(AdminTypeEnum.VENDOR.getCode());
                adminUserSave.setAddTime(StringUtils.getCurrentTime());
                if (ObjectUtil.isEmpty(adminUserSave.getPassword())) {
                    adminUserSave.setPassword(passwordEncoder.encode(PASSWORD));
                } else {
                    adminUserSave.setPassword(passwordEncoder.encode(adminUserSave.getPassword()));
                }
                adminUserSave.setAuthList("[]");
                adminUserSave.setRoleId(0);
                if (mobileCount == 0) {
                    adminUserMapper.insert(adminUserSave);
                }
                createParam.setAdminId(adminUserSave.getAdminId());
            } else {
                createParam.setAdminId(adminUser.getAdminId());
            }
        }

        long hasExist = count(new LambdaQueryWrapper<AdminUserVendor>()
                .eq(AdminUserVendor::getAdminId, createParam.getAdminId())
                .eq(AdminUserVendor::getVendorId, createParam.getVendorId())
        );

        if (hasExist > 0) {
            throw new GlobalException("此供应商已存在此员工");
        }


        AdminUserVendor adminUservendor = new AdminUserVendor();
        BeanUtils.copyProperties(createParam, adminUservendor);
        if (createParam.getRoleId() != null && createParam.getRoleId() > 0) {
            AdminRole adminRole = adminRoleMapper.selectById(createParam.getRoleId());
            if (adminRole != null) {
                adminUservendor.setAuthList(adminRole.getAuthorityList());
            }
            adminUservendor.setRoleId(createParam.getRoleId());
        } else {
            adminUservendor.setAuthList(Optional.ofNullable(createParam.getAuthList())
                    .map(JSONUtil::toJsonStr)
                    .orElse("[]"));
            adminUservendor.setRoleId(0);
        }
        //获取用户名
        if (createParam.getUsername() == null) {
            AdminUser adminUserInfo = adminUserMapper.selectById(createParam.getAdminId());
            adminUservendor.setUsername(adminUserInfo.getUsername());
        }
        adminUservendor.setAddTime(StringUtils.getCurrentTime());

        this.save(adminUservendor);
    }

    @Override
    @Transactional
    public void updateAdminUserVendor(AdminUserVendorUpdateParam updateParam) {
        if (!ObjectUtil.equals(updateParam.getPassword(), updateParam.getPwdConfirm())) {
            throw new GlobalException(PASSWORD_NOT_EQUAL);
        }
        if (updateParam.getVendorId() == null) {
            throw new GlobalException(VENDOR_NOT_EXIST);
        }
        if (vendorMapper.selectById(updateParam.getVendorId()) == null) {
            throw new GlobalException(VENDOR_NOT_EXIST);
        }
        // 判断用户名是否重复
        long usernameCount = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, updateParam.getUsername())
                .ne(updateParam.getAdminId() != null, AdminUser::getAdminId, updateParam.getAdminId())
        );

        if (usernameCount > 0) {
            throw new GlobalException(USERNAME_IS_EXIST);
        }

        long mobileCount = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getMobile, updateParam.getMobile())
                .ne(updateParam.getAdminId() != null, AdminUser::getAdminId, updateParam.getAdminId())
        );

        if (mobileCount > 0) {
            throw new GlobalException(MOBILE_IS_EXIST);
        }
        AdminUserVendor adminUserVendor = this.getById(updateParam.getId());
        if (adminUserVendor == null) {
            throw new GlobalException(ADMIN_USER_VENDOR_NOT_EXIST);
        }


        if (ObjectUtil.isEmpty(updateParam.getAvatar())) {
            int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
            String format = StrUtil.format("../assets/avatar/{}.jpeg", randomNumber);
            updateParam.setAvatar(format);
        }

        AdminUser adminUser = adminUserMapper.selectById(adminUserVendor.getAdminId());
        if (adminUser != null) {
            adminUser.setAvatar(updateParam.getAvatar());
            adminUser.setUsername(updateParam.getUsername());
            adminUser.setEmail(updateParam.getEmail());
            if (ObjectUtil.isNotEmpty(updateParam.getPassword())) {
                adminUser.setPassword(passwordEncoder.encode(updateParam.getPassword()));
            }
            adminUserMapper.updateById(adminUser);
        }

        // 更新用户权限
        if (updateParam.getRoleId() != null && updateParam.getRoleId() > 0) {
            AdminRole adminRole = adminRoleMapper.selectById(updateParam.getRoleId());
            if (adminRole != null) {
                adminUserVendor.setAuthList(adminRole.getAuthorityList());
            }
        } else {
            adminUserVendor.setAuthList(Optional.ofNullable(updateParam.getAuthList())
                    .map(JSONUtil::toJsonStr)
                    .orElse("[]"));
        }
        // 更新 admin_user_shop
        BeanUtils.copyProperties(updateParam, adminUserVendor);
        this.updateById(adminUserVendor);
    }

    @Override
    @Transactional
    public void delVendor(Integer id, Integer adminId) {
        AdminUserVendor adminUserVendor = this.getById(id);
        if (adminUserVendor == null) {
            throw new GlobalException(ADMIN_USER_VENDOR_NOT_EXIST);
        }
        if (Objects.equals(adminId, adminUserVendor.getAdminId())) {
            throw new GlobalException(ADMIN_USER_VENDOR_NOT_DELETE_SELF);
        }
        adminLogService.createByLogInfo("删除员工信息：" + adminUserVendor.getUsername());
        this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        Integer adminId = SecurityUtils.getCurrentAdminId();
        if ("del".equals(batchDTO.getType())) {
            batchDTO.getIds().forEach(id -> delVendor(id, adminId));
        }
        return false;
    }

    /**
     * 处理返回结果
     *
     * @param adminUserVendor 店铺员工管理
     * @return AdminUserVendorVO
     */
    private AdminUserVendorVO handleRes(AdminUserVendor adminUserVendor) {
        AdminUserVendorVO adminUserVendorVO = new AdminUserVendorVO();
        BeanUtils.copyProperties(adminUserVendor, adminUserVendorVO);
        adminUserVendorVO.setAddTime(TigUtils.handelTime(adminUserVendor.getAddTime()));
        if (adminUserVendor.getAuthList() != null && !"[]".equals(adminUserVendor.getAuthList())) {
            adminUserVendorVO.setAuthList(JsonUtil.fromJson(adminUserVendor.getAuthList(), JSONArray.class));
        } else {
            adminUserVendorVO.setAuthList(new JSONArray());
        }
        return adminUserVendorVO;
    }

    /**
     * 获取adminUserVendorVOItemVO
     *
     * @param adminUser         adminUser
     * @param adminUserVendorVO 店铺员工管理VO
     * @return AdminUserVendorVO
     */
    private AdminUserVendorVO getAdminUserVendorItem(AdminUser adminUser, AdminUserVendorVO adminUserVendorVO) {
        if (adminUser != null) {
            AdminUserVendorVO.AdminUserVO adminUserVO = new AdminUserVendorVO.AdminUserVO();
            BeanUtils.copyProperties(adminUser, adminUserVO);
            adminUserVendorVO.setAdminUser(adminUserVO);
        } else {
            adminUserVendorVO.setAdminUser(null);
        }
        return adminUserVendorVO;
    }
}




