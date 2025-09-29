// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.shop.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.shop.AdminUserShopCreateDTO;
import com.tigshop.bean.query.shop.AdminUserShopPageQuery;
import com.tigshop.bean.dto.shop.AdminUserShopModifyDTO;
import com.tigshop.bean.dto.shop.AdminUserShopUpdateDTO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.merchant.MerchantUser;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.shop.AdminUserShopVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.authority.AdminRoleMapper;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.shop.AdminUserShopMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.merchant.MerchantUserService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.shop.AdminUserShopService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.authority.AdminUserConstants.*;
import static com.tigshop.common.constant.shop.AdminUserShopConstants.ADMIN_USER_SHOP_NOT_DELETE_SELF;
import static com.tigshop.common.constant.shop.AdminUserShopConstants.ADMIN_USER_SHOP_NOT_EXIST;

/**
 * 店铺表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class AdminUserShopImpl extends BaseServiceImpl<AdminUserShopMapper, AdminUserShop> implements AdminUserShopService {
    @Resource
    private ConfigService configService;
    @Resource
    private AdminUserMapper adminUserMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private AdminRoleMapper adminRoleMapper;
    @Resource
    private MerchantUserService merchantUserService;
    @Resource
    private AdminLogService adminLogService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createAdminUserShop(AdminUserShopCreateDTO createDTO) {
        // 判断用户名是否重复
        long usernameCount = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, createDTO.getUsername())
                .ne(AdminUser::getMobile, createDTO.getMobile())
                .ne(createDTO.getAdminId() != null, AdminUser::getAdminId, createDTO.getAdminId())
        );

        if (usernameCount > 0) {
            throw new GlobalException(USERNAME_IS_EXIST);
        }

        if (createDTO.getShopId() > 0) {
            //获取admin_user_shop员工数量
            long count = this.count(new LambdaQueryWrapper<AdminUserShop>()
                    .eq(AdminUserShop::getShopId, createDTO.getShopId())
                    .eq(AdminUserShop::getIsAdmin, 0));
            ConfigPO maxSubAdministratorConfig = configService.getConfigByCode(SettingsEnum.MAX_SUB_ADMINISTRATOR.getBizCode());
            long maxAdminUserShopCount = StrUtil.isNotBlank(maxSubAdministratorConfig.getBizVal()) ? Long.parseLong(maxSubAdministratorConfig.getBizVal()) : 0L;
            if (count >= maxAdminUserShopCount) {
                throw new GlobalException("店铺员工数已达上限,上限为" + maxAdminUserShopCount + "个,如需修改,请前往配置");
            }
        }

        if (createDTO.getAvatar() == null) {
            int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
            String format = StrUtil.format("../assets/avatar/{}.jpeg", randomNumber);
            createDTO.setAvatar(format);
        }

        if (createDTO.getAdminId() != null && createDTO.getAdminId() > 0) {
            AdminUser adminUser = adminUserMapper.selectById(createDTO.getAdminId());
            if (adminUser != null) {
                createDTO.setMerchantId(adminUser.getMerchantId());
            }
        } else {
            LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AdminUser::getMobile, createDTO.getMobile());
            AdminUser adminUser = adminUserMapper.selectOne(queryWrapper);
            if (adminUser == null) {
                AdminUser adminUserSave = new AdminUser();
                BeanUtils.copyProperties(createDTO, adminUserSave);
                adminUserSave.setAdminType("shop");
                adminUserSave.setAddTime(StringUtils.getCurrentTime());
                if (ObjectUtil.isEmpty(adminUserSave.getPassword())) {
                    adminUserSave.setPassword(passwordEncoder.encode(PASSWORD));
                }
                adminUserSave.setAuthList("[]");
                int isSave = adminUserMapper.insert(adminUserSave);
                if (isSave > 0) {
                    createDTO.setAdminId(adminUserSave.getAdminId());
                    MerchantUser merchantUser = new MerchantUser();
                    BeanUtils.copyProperties(createDTO, merchantUser);
                    merchantUserService.updateMerchantUser(merchantUser, adminUserSave.getAdminId());
                }
            } else {
                createDTO.setAdminId(adminUser.getAdminId());
            }
        }


        AdminUserShop adminUsershop = new AdminUserShop();
        BeanUtils.copyProperties(createDTO, adminUsershop);
        if (createDTO.getRoleId() != null && createDTO.getRoleId() > 0) {
            AdminRole adminRole = adminRoleMapper.selectById(createDTO.getRoleId());
            if (adminRole != null) {
                adminUsershop.setAuthList(adminRole.getAuthorityList());
            }
            adminUsershop.setRoleId(createDTO.getRoleId());
        } else {
            adminUsershop.setAuthList(Optional.ofNullable(createDTO.getAuthList())
                    .map(JSONUtil::toJsonStr)
                    .orElse("[]"));
            adminUsershop.setRoleId(0);
        }
        //获取用户名
        if (createDTO.getUsername() == null) {
            AdminUser adminUserInfo = adminUserMapper.selectById(createDTO.getAdminId());
            adminUsershop.setUsername(adminUserInfo.getUsername());
        }
        adminUsershop.setAddTime(StringUtils.getCurrentTime());
        this.save(adminUsershop);
    }

    @Override
    public void updateAdminUserShop(AdminUserShopUpdateDTO updateDTO) {
        // 判断用户名是否重复
        long usernameCount = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, updateDTO.getUsername())
                .ne(updateDTO.getAdminId() != null, AdminUser::getAdminId, updateDTO.getAdminId())
        );

        if (usernameCount > 0) {
            throw new GlobalException(USERNAME_IS_EXIST);
        }

        long mobileCount = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getMobile, updateDTO.getMobile())
                .ne(updateDTO.getAdminId() != null, AdminUser::getAdminId, updateDTO.getAdminId())
        );

        if (mobileCount > 0) {
            throw new GlobalException(MOBILE_IS_EXIST);
        }
        AdminUserShop adminUserShop = this.getById(updateDTO.getId());
        if (adminUserShop == null) {
            throw new GlobalException(ADMIN_USER_SHOP_NOT_EXIST);
        }
        AdminUser adminUser = adminUserMapper.selectById(adminUserShop.getAdminId());
        if (adminUser != null && !Objects.equals(updateDTO.getMobile(), adminUser.getMobile())) {
            LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AdminUser::getMobile, updateDTO.getMobile());
            AdminUser adminUserInfo = adminUserMapper.selectOne(queryWrapper);
            if (adminUserInfo == null) {
                AdminUser adminUserSave = new AdminUser();
                BeanUtils.copyProperties(updateDTO, adminUserSave);
                adminUserSave.setAdminType("shop");
                adminUserSave.setAddTime(StringUtils.getCurrentTime());
                int isSave = adminUserMapper.insert(adminUserSave);
                if (isSave > 0) {
                    updateDTO.setAdminId(adminUserSave.getAdminId());
                    MerchantUser merchantUser = new MerchantUser();
                    BeanUtils.copyProperties(updateDTO, merchantUser);
                    merchantUserService.updateMerchantUser(merchantUser, adminUserSave.getAdminId());
                }
            } else {
                updateDTO.setAdminId(adminUser.getAdminId());
            }
        }

        if (updateDTO.getAvatar() == null) {
            int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
            String format = StrUtil.format("../assets/avatar/{}.jpeg", randomNumber);
            updateDTO.setAvatar(format);
        }

        // 更新用户权限
        if (updateDTO.getRoleId() != null && updateDTO.getRoleId() > 0) {
            AdminRole adminRole = adminRoleMapper.selectById(updateDTO.getRoleId());
            if (adminRole != null) {
                adminUserShop.setAuthList(adminRole.getAuthorityList());
            }
        } else {
            adminUserShop.setAuthList(Optional.ofNullable(updateDTO.getAuthList())
                    .map(JSONUtil::toJsonStr)
                    .orElse("[]"));
        }
        // 更新 admin_user_shop
        BeanUtils.copyProperties(updateDTO, adminUserShop);
        this.updateById(adminUserShop);
    }

    @Override
    public void delShop(Integer id, Integer adminId) {
        AdminUserShop adminUserShop = this.getById(id);
        if (adminUserShop == null) {
            throw new GlobalException(ADMIN_USER_SHOP_NOT_EXIST);
        }
        if (Objects.equals(adminId, adminUserShop.getAdminId())) {
            throw new GlobalException(ADMIN_USER_SHOP_NOT_DELETE_SELF);
        }
        adminLogService.createByLogInfo("删除员工信息：" + adminUserShop.getUsername());
        this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        Integer adminId = SecurityUtils.getCurrentAdminId();
        if ("del".equals(batchDTO.getType())) {
            batchDTO.getIds().forEach(id -> delShop(id, adminId));
        }
        return false;
    }

    @Override
    public void modifyUser(AdminUserShopModifyDTO modifyDTO) {
        AdminUserShop adminUserShop = this.getById(modifyDTO.getId());
        if (adminUserShop == null) {
            throw new GlobalException(ADMIN_USER_SHOP_NOT_EXIST);
        }
        if (ObjectUtil.isEmpty(modifyDTO.getAvatar())) {
            int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
            String format = StrUtil.format("../assets/avatar/{}.jpeg", randomNumber);
            modifyDTO.setAvatar(format);
        }
        BeanUtils.copyProperties(modifyDTO, adminUserShop);
        this.updateById(adminUserShop);
    }

    @Override
    public boolean hasShop(Integer userId) {
        LambdaQueryWrapper<AdminUserShop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminUserShop::getUserId, userId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper) != null;
    }

    @Override
    public Page<AdminUserShopVO> list(AdminUserShopPageQuery pageQuery) {

        Integer shopId = HeaderUtils.getShopId();

        Page<AdminUserShop> page = buildSortOrder(pageQuery);
        Page<AdminUserShop> shopPage = this.lambdaQuery()
                .eq(shopId != null && shopId > 0, AdminUserShop::getShopId, shopId)
                .like(StrUtil.isNotBlank(pageQuery.getKeyword()), AdminUserShop::getUsername, pageQuery.getKeyword())
                .page(page);

        List<AdminUserShop> adminShopRecords = shopPage.getRecords();
        if (CollUtil.isEmpty(adminShopRecords)) {
            return new Page<>();
        }
        List<Integer> userIds = adminShopRecords.stream().map(AdminUserShop::getUserId).toList();


        // 查出用户信息
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getUserId, Function.identity()));

        // 查出admin_user信息
        List<Integer> adminIds = adminShopRecords.stream().map(AdminUserShop::getAdminId).toList();
        List<AdminUser> adminUsers = adminUserMapper.selectBatchIds(adminIds);
        Map<Integer, AdminUser> adminUserMap = adminUsers.stream().collect(Collectors.toMap(AdminUser::getAdminId, Function.identity()));

        // 查出角色
        List<Integer> roleIds = adminShopRecords.stream().map(AdminUserShop::getRoleId).toList();
        List<AdminRole> roles = adminRoleMapper.selectBatchIds(roleIds);
        Map<Integer, AdminRole> roleMap = roles.stream().collect(Collectors.toMap(AdminRole::getRoleId, Function.identity()));

        // 转换为VO
        List<AdminUserShopVO> adminUserShopVOList = adminShopRecords.stream()
                .map(adminUserShop -> {
                    AdminUserShopVO adminUserShopVO = new AdminUserShopVO(adminUserShop);

                    User user = userMap.get(adminUserShop.getUserId());
                    AdminUser adminUser = adminUserMap.get(adminUserShop.getAdminId());
                    AdminRole adminRole = roleMap.get(adminUserShop.getRoleId());
                    adminUserShopVO.assembleData(user, adminUser, adminRole);

                    return adminUserShopVO;
                })
                .toList();
        return PageUtil.convertPage(shopPage, adminUserShopVOList);
    }

    @Override
    public AdminUserShopVO detail(Integer id) {
        AdminUserShop adminUserShop = this.getById(id);
        if (adminUserShop == null) {
            return null;
        }
        User user = userMapper.selectById(adminUserShop.getUserId());
        AdminUser adminUser = adminUserMapper.selectById(adminUserShop.getAdminId());
        AdminUserShopVO adminUserShopVO = handleRes(adminUserShop);
        if (user != null) {
            AdminUserShopVO.UserVO userVO = new AdminUserShopVO.UserVO();
            BeanUtils.copyProperties(user, userVO);
            adminUserShopVO.setUser(userVO);
        } else {
            adminUserShopVO.setUser(null);
        }
        return getAdminUserShopItem(adminUser, adminUserShopVO);
    }

    @Override
    public AdminUserShopVO info(Integer id, Integer shopId) {
        LambdaQueryWrapper<AdminUserShop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminUserShop::getAdminId, id)
                .eq(AdminUserShop::getShopId, shopId);
        AdminUserShop adminUserShop = this.getOne(queryWrapper);
        if (adminUserShop == null) {
            throw new GlobalException(ADMIN_USER_SHOP_NOT_EXIST);
        }
        AdminUser adminUser = adminUserMapper.selectById(adminUserShop.getAdminId());
        AdminUserShopVO adminUserShopVO = handleRes(adminUserShop);
        return getAdminUserShopItem(adminUser, adminUserShopVO);
    }

    /**
     * 获取adminUserShopVOItemVO
     *
     * @param adminUser       adminUser
     * @param adminUserShopVO 店铺员工管理VO
     * @return AdminUserShopVO
     */
    private AdminUserShopVO getAdminUserShopItem(AdminUser adminUser, AdminUserShopVO adminUserShopVO) {
        if (adminUser != null) {
            AdminUserShopVO.AdminUserVO adminUserVO = new AdminUserShopVO.AdminUserVO();
            BeanUtils.copyProperties(adminUser, adminUserVO);
            adminUserShopVO.setAdminUser(adminUserVO);
        } else {
            adminUserShopVO.setAdminUser(null);
        }
        return adminUserShopVO;
    }

    /**
     * 处理返回结果
     *
     * @param adminUserShop 店铺员工管理
     * @return AdminUserShopVO
     */
    private AdminUserShopVO handleRes(AdminUserShop adminUserShop) {
        AdminUserShopVO adminUserShopVO = new AdminUserShopVO();
        BeanUtils.copyProperties(adminUserShop, adminUserShopVO);
        adminUserShopVO.setAddTime(TigUtils.handelTime(adminUserShop.getAddTime()));
        if (adminUserShop.getAuthList() != null && !"[]".equals(adminUserShop.getAuthList())) {
            adminUserShopVO.setAuthList(JsonUtil.fromJson(adminUserShop.getAuthList(), JSONArray.class));
        } else {
            adminUserShopVO.setAuthList(new JSONArray());
        }
        return adminUserShopVO;
    }

    @Override
    public List<Integer> getShopIds(Integer adminId) {
        List<Integer> shopIds = this.list(new LambdaQueryWrapper<AdminUserShop>()
                        .eq(AdminUserShop::getAdminId, adminId)
                        .select(AdminUserShop::getShopId))
                .stream()
                .map(AdminUserShop::getShopId)
                .toList();
        if (shopIds.isEmpty()) {
            return new ArrayList<>();
        }
        return shopIds;

    }

}
