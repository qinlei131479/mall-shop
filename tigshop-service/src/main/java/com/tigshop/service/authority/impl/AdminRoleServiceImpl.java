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

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.authority.AdminRoleDTO;
import com.tigshop.bean.dto.authority.AdminRoleListDTO;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.vendor.AdminUserVendor;
import com.tigshop.common.constant.authority.AdminUserConstants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.authority.AdminRoleMapper;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.merchant.MerchantMapper;
import com.tigshop.mapper.shop.AdminUserShopMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.vendor.AdminUserVendorMapper;
import com.tigshop.service.authority.AdminRoleService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.tigshop.common.constant.authority.AdminRoleConstants.ROLE_NOT_EXIST;

/**
 * 管理员角色服务实现类
 *
 * @author Tigshop团队
 * @create 2024年11月04日 17:16
 */
@Service
public class AdminRoleServiceImpl extends BaseServiceImpl<AdminRoleMapper, AdminRole> implements AdminRoleService {
    @Resource
    private ShopMapper shopMapper;
    @Resource
    private MerchantMapper merchantMapper;
    @Autowired
    private AdminUserMapper adminUserMapper;
    @Autowired
    private AdminUserShopMapper adminUserShopMapper;
    @Autowired
    private AdminUserVendorMapper adminUserVendorMapper;

    @Override
    public Page<AdminRoleDTO> list(AdminRoleListDTO adminRoleListDTO) {
        // 初始化分页对象，用于MyBatis-Plus的分页查询。
        Page<AdminRole> page = new Page<>(adminRoleListDTO.getPage(), adminRoleListDTO.getSize());

        // 创建查询条件构造器。
        LambdaQueryWrapper<AdminRole> queryWrapper = new LambdaQueryWrapper<>();

        AdminTypeEnum adminTypeEnum = AdminTypeEnum.fromCode(getAdminType());
        switch (adminTypeEnum) {
            case AdminTypeEnum.ADMIN:
                queryWrapper.and(qw -> qw
                        .eq(AdminRole::getShopId, getShopId())
                        .eq(AdminRole::getAdminType, "admin")
                );
                break;
            case AdminTypeEnum.SHOP:
            case AdminTypeEnum.STORE:
            case AdminTypeEnum.PICKUP:
                Shop shop = shopMapper.selectById(getShopId());
                if (shop == null) {
                    throw new GlobalException("店铺不存在");
                }
                Merchant merchant;
                if (shop.getMerchantId() > 0) {
                    merchant = merchantMapper.selectById(shop.getMerchantId());
                    if (merchant == null) {
                        throw new GlobalException("商户不存在");
                    }
                } else {
                    merchant = null;
                }
                queryWrapper.and(qw -> {
                            if (merchant != null && merchant.getMerchantId() > 0) {
                                qw.eq(AdminRole::getMerchantId, merchant.getMerchantId())
                                        .or();
                            }
                            qw.eq(AdminRole::getShopId, shop.getShopId());
                        }
                );
                break;
            case AdminTypeEnum.VENDOR:
                queryWrapper.and(qw -> qw
                        .eq(AdminRole::getVendorId, getVendorId())
                        .eq(AdminRole::getAdminType, "vendor")
                );
                break;
            default:
                throw new GlobalException("管理员类型错误");
        }


        // 获取搜索关键字，并检查是否为空。
        String keyword = adminRoleListDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            // 如果关键字不为空，构建模糊查询条件。
            queryWrapper
                    .like(AdminRole::getRoleName, keyword)
                    .or()
                    .like(AdminRole::getRoleDesc, keyword);
        }
        // 排序字段
        buildSortOrder(page, adminRoleListDTO.getSortField(), adminRoleListDTO.getSortOrder());

        Page<AdminRole> adminRolePage = this.page(page, queryWrapper);
        List<AdminRoleDTO> adminRoles = adminRolePage.getRecords().stream().map(this::convertToDTO).toList();
        // 批量查询 username 并返回分页结果
        return PageUtil.convertPage(adminRolePage, adminRoles);
    }

    @Override
    public AdminRoleDTO detail(Integer id) {
        AdminRole adminRole = this.getById(id);
        if (adminRole == null) {
            throw new GlobalException(ROLE_NOT_EXIST);
        }
        return convertToDTO(adminRole);
    }

    @Override
    public boolean create(AdminRoleDTO adminRoleDTO) {
        return this.save(convertToBean(adminRoleDTO));
    }

    @Override
    public boolean update(AdminRoleDTO adminRoleDTO) {
        return this.updateById(convertToBean(adminRoleDTO));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initAdminRole(Integer merchantId, Integer shopId) {
        AdminRole adminRole = this.lambdaQuery()
                .eq(AdminRole::getAdminType, "shop")
                .eq(AdminRole::getMerchantId, merchantId)
                .eq(AdminRole::getShopId, shopId)
                .one();

        if (adminRole != null) {
            return;
        }

        adminRole = AdminRole.builder()
                .roleName("超级管理员")
                .roleDesc("系统内置角色")
                .authorityList("[\"all\"]")
                .adminType("shop")
                .merchantId(merchantId)
                .shopId(shopId)
                .build();
        this.save(adminRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminRole initVendorRole(Integer vendorId) {
        AdminRole adminRole = this.lambdaQuery().eq(AdminRole::getVendorId, vendorId).one();

        if (adminRole != null) {
            return adminRole;
        }

        adminRole = AdminRole.builder()
                .roleName("超级管理员")
                .roleDesc("系统内置角色")
                .authorityList("[\"all\"]")
                .adminType(AdminUserConstants.VENDOR_TYPE)
                .vendorId(vendorId)
                .build();
        this.save(adminRole);
        return adminRole;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delRole(Integer id) {
        if (id == null) {
            throw new GlobalException(ROLE_NOT_EXIST);
        }
        boolean del = del(id);
        if (del) {
            //角色组删除后对应的管理员角色组也删除
            adminUserMapper.update(new LambdaUpdateWrapper<AdminUser>().set(AdminUser::getRoleId, 0).eq(AdminUser::getRoleId, id));
            adminUserShopMapper.update(new LambdaUpdateWrapper<AdminUserShop>().set(AdminUserShop::getRoleId, 0).eq(AdminUserShop::getRoleId, id));
            adminUserVendorMapper.update(new LambdaUpdateWrapper<AdminUserVendor>().set(AdminUserVendor::getRoleId, 0).eq(AdminUserVendor::getRoleId, id));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        if ("del".equals(batchDTO.getType())) {
            batchDTO.getIds().forEach(this::delRole);
        }
        return false;
    }

    @Override
    public boolean del(Integer id) {
        return this.removeById(id);
    }

    /**
     * 转换为 DTO
     *
     * @param adminRole 属性模板实体
     * @return 属性模板 DTO
     */
    private AdminRoleDTO convertToDTO(AdminRole adminRole) {
        AdminRoleDTO dto = new AdminRoleDTO();
        BeanUtils.copyProperties(adminRole, dto);

        dto.setAuthorityList(
                JSONUtil.toList(adminRole.getAuthorityList(), String.class)
        );

        return dto;
    }

    /**
     * 转换为实体
     *
     * @param dto 属性模板 DTO
     * @return 属性模板实体
     */
    private AdminRole convertToBean(AdminRoleDTO dto) {
        AdminRole adminRole = new AdminRole();
        BeanUtils.copyProperties(dto, adminRole);
        adminRole.setShopId(getShopId());
        adminRole.setVendorId(getVendorId());
        adminRole.setAdminType(getAdminType());
        adminRole.setAuthorityList(
                JSONUtil.toJsonStr(dto.getAuthorityList())
        );
        return adminRole;
    }
}