package com.tigshop.service.authority.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.authority.AdminRoleDTO;
import com.tigshop.bean.dto.authority.AuthorityChildDTO;
import com.tigshop.bean.dto.authority.AuthorityDTO;
import com.tigshop.bean.dto.authority.AuthorityListDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.authority.Authority;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.model.vendor.AdminUserVendor;
import com.tigshop.bean.vo.authority.AuthorityVO;
import com.tigshop.bean.vo.authority.ChildAuthVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.authority.AuthorityMapper;
import com.tigshop.mapper.shop.AdminUserShopMapper;
import com.tigshop.mapper.vendor.AdminUserVendorMapper;
import com.tigshop.service.authority.AdminRoleService;
import com.tigshop.service.authority.AuthorityService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.tigshop.common.constant.Constants.ADMIN_USER;
import static com.tigshop.common.constant.ResultTextConstants.USER_NOT_LOGGED_IN;
import static com.tigshop.common.constant.authority.AuthorityConstants.AUTHORITY_NOT_FOUND;
import static com.tigshop.common.constant.authority.AuthorityConstants.AUTHORITY_SN_EXISTS;

/**
 * 权限服务实现类
 *
 * @author kidd
 * @create 2025/7/3
 */
@RequiredArgsConstructor
@Service
public class AuthorityServiceImpl extends BaseServiceImpl<AuthorityMapper, Authority> implements AuthorityService {

    private final RedisCache redisCache;

    private final AdminRoleService adminRoleService;

    private final TigshopProperties tigshopProperties;

    private final AdminUserShopMapper adminUserShopMapper;

    private final AdminUserVendorMapper adminUserVendorMapper;

    @Override
    public boolean create(AuthorityDTO authorityDTO) {
        return this.save(buildChildAuth(authorityDTO));
    }

    @Override
    public boolean update(AuthorityDTO authorityDTO) {
        return this.updateById(buildChildAuth(authorityDTO));
    }

    /**
     * 构建子权限(create/update)
     *
     * @param authorityDTO 权限信息
     * @return Authority
     */
    public Authority buildChildAuth(AuthorityDTO authorityDTO) {
        // 检查权限编号是否已存在，如果存在则抛出异常
        if (this.count(new LambdaQueryWrapper<Authority>()
                .ne(authorityDTO.getAuthorityId() != null, Authority::getAuthorityId, authorityDTO.getAuthorityId())
                .eq(Authority::getAuthoritySn, authorityDTO.getAuthoritySn())
                .eq(Authority::getAdminType, authorityDTO.getAdminType())
                .select(Authority::getAuthorityName)) > 0) {
            throw new GlobalException(AUTHORITY_SN_EXISTS);
        }

        Authority authority = new Authority();
        BeanUtils.copyProperties(authorityDTO, authority);

        // 处理子权限，若为空则设置为 "[]"
        authority.setChildAuth(
                Optional.ofNullable(authorityDTO.getChildAuth())
                        .filter(list -> !list.isEmpty())
                        .map(JSONUtil::toJsonStr)
                        .orElse("[]")
        );

        return authority;
    }

    /**
     * 构建子权限(list/detail)
     *
     * @param authority 权限信息
     * @return AuthorityDTO
     */
    public AuthorityDTO buildChildAuth(Authority authority) {
        AuthorityDTO authorityDTO = new AuthorityDTO();
        String childAuth = authority.getChildAuth();
        // authority -> authorityDTO
        BeanUtils.copyProperties(authority, authorityDTO);
        // 处理子权限
        if (StringUtils.isNotBlank(childAuth)) {
            List<AuthorityChildDTO> list = JSONUtil.toList(JSONUtil.parseArray(childAuth), AuthorityChildDTO.class);
            authorityDTO.setChildAuth(list);
        }
        return authorityDTO;
    }

    @Override
    public AuthorityDTO detail(Integer id) {
        Authority authority = this.getById(id);
        if (authority == null) {
            throw new GlobalException(AUTHORITY_NOT_FOUND);
        }
        return buildChildAuth(authority);
    }

    @Override
    public List<AuthorityVO> getAuthorityByName(String name) {
        LambdaQueryWrapper<Authority> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Authority::getAuthorityName, name);
        queryWrapper.eq(Authority::getIsShow, Constants.YES);
        JSONObject userDetails = SecurityUtils.getCurrentUserDetails();
        assert userDetails != null;
        if ("shop".equals(userDetails.get("adminType"))) {
            queryWrapper.eq(Authority::getAdminType, "shop");
        } else if ("admin".equals(userDetails.get("adminType"))) {
            queryWrapper.eq(Authority::getAdminType, "admin");
        } else if ("vendor".equals(userDetails.get("adminType"))) {
            queryWrapper.eq(Authority::getAdminType, "vendor");
        } else if ("supplier".equals(userDetails.get("adminType"))) {
            AdminRoleDTO detail = adminRoleService.detail(2);
            if (detail == null) {
                return new ArrayList<>();
            } else {
                queryWrapper.in(Authority::getAuthoritySn, detail.getAuthorityList());
            }
        }

        List<Authority> authorities = this.list(queryWrapper.select(Authority::getAuthoritySn, Authority::getRouteLink, Authority::getAuthorityName, Authority::getParentId));

        if (CollUtil.isEmpty(authorities)) {
            return new ArrayList<>();
        }

        return authorities.stream()
                .map(authority -> {
                    AuthorityVO item = BeanUtil.copyProperties(authority, AuthorityVO.class);

                    List<String> authorityNames = new ArrayList<>();
                    authorityNames.add(authority.getAuthorityName());
                    List<String> nameUntilRoot = getParentNameUntilRoot(authorityNames, authority.getParentId());
                    item.setAuthorityNames(nameUntilRoot);

                    return item;
                })
                .toList();
    }

    public List<String> getParentNameUntilRoot(List<String> authorityNames, int parentId) {
        if (parentId == 0) {
            return authorityNames;
        }
        Authority authority = this.getById(parentId);
        if (authority == null) {
            return authorityNames;
        }

        authorityNames.addFirst(authority.getAuthorityName());

        if (authority.getParentId() != 0) {
            return this.getParentNameUntilRoot(authorityNames, authority.getParentId());
        }

        return authorityNames;
    }

    @Override
    public List<Tree<Integer>> getAllAuthority(Integer type, String requestAdminType) {
        // 获取当前用户的权限列表
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.notNull(authentication, () -> new GlobalException(USER_NOT_LOGGED_IN));

        // 从缓存中获取用户信息
        String adminUserCacheKey = ADMIN_USER + authentication.getName();
        AdminUser adminUser = redisCache.getCacheObject(adminUserCacheKey);
        Assert.notNull(adminUser, () -> new GlobalException(USER_NOT_LOGGED_IN));

        // 获取当前用户的权限
        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        // 设置当前账户是后台账户或者前台账户的查询条件
        String adminType = HeaderUtils.getAdminType();
        if (AdminTypeEnum.fromCode(adminType) == AdminTypeEnum.ADMIN && ObjectUtil.isNotEmpty(requestAdminType)) {
            adminType = requestAdminType;
        }
        if (ObjectUtil.equals(adminType, "suppliers")) {
            adminType = "admin";
        } else if (ObjectUtil.equals(adminType, "shop")) {
            Integer shopId = HeaderUtils.getShopId();
            if (shopId != null && shopId != 0) {
                AdminUserShop adminUserShop = adminUserShopMapper.selectOne(
                        Wrappers.lambdaQuery(AdminUserShop.class).eq(AdminUserShop::getAdminId, adminUser.getAdminId()).eq(AdminUserShop::getShopId, shopId)
                );
                if (adminUserShop != null) {
                    authorities = JSONArray.parseArray(adminUserShop.getAuthList(), String.class);
                }
            }
        } else if (ObjectUtil.equals(adminType, "vendor")) {
            Integer vendorId = HeaderUtils.getVendorId();
            if (vendorId != null && vendorId != 0) {
                AdminUserVendor adminUserVendor = adminUserVendorMapper.selectOne(
                        Wrappers.lambdaQuery(AdminUserVendor.class).eq(AdminUserVendor::getAdminId, adminUser.getAdminId()).eq(AdminUserVendor::getVendorId, vendorId)
                );
                if (adminUserVendor != null) {
                    authorities = JSONArray.parseArray(adminUserVendor.getAuthList(), String.class);
                }
            }
        }


        // 检查是否包含 "all" 权限
        boolean hasAllAuthority = CollUtil.isEmpty(authorities) || authorities.stream().anyMatch("all"::equals);

        List<Authority> authorityList = this.lambdaQuery()
                .eq(Authority::getAdminType, adminType)
                .in(!hasAllAuthority, Authority::getAuthoritySn, authorities)
                .eq(Authority::getIsShow, 1)
                .orderByAsc(Authority::getParentId, Authority::getSortOrder, Authority::getAuthorityId)
                .list();

        if (tigshopProperties.getIsOverseas() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "multilingual")).findFirst().ifPresent(authorityList::remove);
            authorityList.removeIf(item -> "mobileuserUserOverseasManage".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsOverseas() == 1) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userInvoice")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userInvoiceManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "orderInvoiceManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.removeIf(item -> "mobileUserManage".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsMerchant() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shop")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shopManagement")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shopManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shopSettingsManage")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsPro() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "salesman")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "eCardManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userLevelProManage")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsPro() == 1) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "levelManageManage")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsB2b() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userCertificationManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "enquiryManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "realNameAuthentication")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userAuthenticationManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "quotationManage")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsVendor() == 0) {
            authorityList.removeIf(item -> item.getAuthoritySn().contains("vendor"));
        }
        if (tigshopProperties.getIsO2o() == 0) {
            authorityList.removeIf(item -> item.getAuthoritySn().contains("store"));
        }
        if (tigshopProperties.getIsO2o() == 1) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "pcDecorate")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0 && tigshopProperties.getIsVendor() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "organize")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "adminMerchant")).findFirst().ifPresent(authorityList::remove);

            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "profitsharingManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.removeIf(item -> "shopUserfundsManagement".equals(item.getAuthoritySn()));
        }

        return convertTreeList(authorityList, type);
    }

    @Override
    public List<Authority> getAuthority() {
        // 获取当前用户的权限列表
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.notNull(authentication, () -> new GlobalException(USER_NOT_LOGGED_IN));

        // 从缓存中获取用户信息
        String adminUserCacheKey = ADMIN_USER + authentication.getName();
        AdminUser adminUser = redisCache.getCacheObject(adminUserCacheKey);
        Assert.notNull(adminUser, () -> new GlobalException(USER_NOT_LOGGED_IN));

        // 获取当前用户的权限
        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        // 设置当前账户是后台账户或者前台账户的查询条件
        String adminType = adminUser.getAdminType();
        if (ObjectUtil.equals(adminType, "suppliers")) {
            adminType = "admin";
        }
        Integer shopId = HeaderUtils.getShopId();
        if (shopId != null && shopId != 0) {
            adminType = "shop";
            AdminUserShop adminUserShop = adminUserShopMapper.selectOne(
                    Wrappers.lambdaQuery(AdminUserShop.class).eq(AdminUserShop::getAdminId, adminUser.getAdminId()).eq(AdminUserShop::getShopId, shopId)
            );
            if (adminUserShop != null) {
                authorities = JSONArray.parseArray(adminUserShop.getAuthList(), String.class);
            }
        }

        Integer vendorId = HeaderUtils.getVendorId();
        if (vendorId != null && vendorId != 0) {
            adminType = "vendor";
            AdminUserVendor adminUserVendor = adminUserVendorMapper.selectOne(
                    Wrappers.lambdaQuery(AdminUserVendor.class).eq(AdminUserVendor::getAdminId, adminUser.getAdminId()).eq(AdminUserVendor::getVendorId, vendorId)
            );
            if (adminUserVendor != null) {
                authorities = JSONArray.parseArray(adminUserVendor.getAuthList(), String.class);
            }
        }

        // 检查是否包含 "all" 权限
        boolean hasAllAuthority = authorities.stream().anyMatch("all"::equals);

        if (ObjectUtil.isEmpty(authorities)) {
            return new ArrayList<>();
        }

        List<Authority> authorityList = this.lambdaQuery()
                .eq(Authority::getAdminType, getAdminType())
                .in(!hasAllAuthority, Authority::getAuthoritySn, authorities)
                .eq(Authority::getIsShow, 1)
                .orderBy(true, true, Authority::getParentId)
                .orderBy(true, false, Authority::getSortOrder)
                .orderBy(true, true, Authority::getAuthorityId)
                .list();

        if (tigshopProperties.getIsOverseas() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "multilingual")).findFirst().ifPresent(authorityList::remove);
            authorityList.removeIf(item -> "mobileuserUserOverseasManage".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsOverseas() == 1) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userInvoice")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userInvoiceManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "orderInvoiceManage")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsMerchant() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shop")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shopManagement")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shopManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shopSettingsManage")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsPro() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "salesman")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "eCardManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userLevelProManage")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsPro() == 1) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "levelManageManage")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsB2b() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userCertificationManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "enquiryManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "realNameAuthentication")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userAuthenticationManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "quotationManage")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsVendor() == 0) {
            authorityList.removeIf(item -> item.getAuthoritySn().contains("vendor"));
        }
        if (tigshopProperties.getIsO2o() == 0) {
            authorityList.removeIf(item -> item.getAuthoritySn().contains("store"));
        }
        if (tigshopProperties.getIsO2o() == 1) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "pcDecorate")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0 && tigshopProperties.getIsVendor() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "organize")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "adminMerchant")).findFirst().ifPresent(authorityList::remove);

            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "profitsharingManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.removeIf(item -> "shopUserfundsManagement".equals(item.getAuthoritySn()));
        }

        return authorityList;
    }

    @Override
    public Page<AuthorityDTO> list(AuthorityListDTO dto) {
        //分页参数
        Page<Authority> page = buildSortOrder(dto);

        // 分页查询
        Page<Authority> authorityPage = this.lambdaQuery()
                .eq(StrUtil.isNotBlank(dto.getAdminType()), Authority::getAdminType, dto.getAdminType())
                .eq(dto.getParentId() != null && dto.getParentId() >= 0, Authority::getParentId, dto.getParentId())
                .page(page);

        // 查询子菜单信息
        List<Authority> records = authorityPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        List<Integer> authorityIds = records.stream().map(Authority::getAuthorityId).toList();
        List<Authority> childrenAuthorities = this.lambdaQuery().in(Authority::getParentId, authorityIds).list();

        List<AuthorityDTO> authorityList = new ArrayList<>();
        if (tigshopProperties.getIsOverseas() == 0) {
            records.removeIf(item -> "multilingual".equals(item.getAuthoritySn()));
            records.removeIf(item -> "mobileuserUserOverseasManage".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsOverseas() == 1) {
            records.removeIf(item -> "userInvoice".equals(item.getAuthoritySn()));
            records.removeIf(item -> "userInvoiceManage".equals(item.getAuthoritySn()));
            records.removeIf(item -> "orderInvoiceManage".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsMerchant() == 0) {
            records.removeIf(item -> "shop".equals(item.getAuthoritySn()));
            records.removeIf(item -> "shopManagement".equals(item.getAuthoritySn()));
            records.removeIf(item -> "shopManage".equals(item.getAuthoritySn()));
            records.removeIf(item -> "shopSettingsManage".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsPro() == 0) {
            records.removeIf(item -> "salesman".equals(item.getAuthoritySn()));
            records.removeIf(item -> "eCardManage".equals(item.getAuthoritySn()));
            records.removeIf(item -> "userLevelProManage".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsPro() == 1) {
            records.removeIf(item -> "levelManageManage".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsB2b() == 0) {
            records.removeIf(item -> "userCertificationManage".equals(item.getAuthoritySn()));
            records.removeIf(item -> "enquiryManage".equals(item.getAuthoritySn()));
            records.removeIf(item -> "realNameAuthentication".equals(item.getAuthoritySn()));
            records.removeIf(item -> "userAuthenticationManage".equals(item.getAuthoritySn()));
            records.removeIf(item -> "quotationManage".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsVendor() == 0) {
            records.removeIf(item -> item.getAuthoritySn().contains("vendor"));
        }
        if (tigshopProperties.getIsO2o() == 0) {
            records.removeIf(item -> item.getAuthoritySn().contains("store"));
        }
        if (tigshopProperties.getIsO2o() == 1) {
            records.removeIf(item -> "pcDecorate".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0 && tigshopProperties.getIsVendor() == 0) {
            records.removeIf(item -> "organize".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0) {
            records.removeIf(item -> "adminMerchant".equals(item.getAuthoritySn()));

            records.removeIf(item -> "profitsharingManage".equals(item.getAuthoritySn()));
            records.removeIf(item -> "shopUserfundsManagement".equals(item.getAuthoritySn()));
        }
        for (Authority authority : records) {
            AuthorityDTO authorityDTO = buildChildAuth(authority);
            long hasChildren = childrenAuthorities.stream().filter(children -> authority.getAuthorityId().equals(children.getParentId())).count();
            authorityDTO.setHasChildren(hasChildren);
            authorityList.add(authorityDTO);
        }
        // 构建返回结果
        return PageUtil.convertPage(authorityPage, authorityList);
    }

    @Override
    public String getAuthorityParentName(Integer id) {
        // 获取父级id
        Authority authority = this.getOne(new LambdaQueryWrapper<Authority>()
                .eq(Authority::getAuthorityId, id)
                .select(Authority::getAuthorityName)
        );
        Integer parentId = authority.getParentId();
        String parentName = null;
        if (parentId != null && parentId > 0) {
            parentName = getParentName(parentId);
        }
        return parentName;
    }

    /**
     * 获取父级名称
     *
     * @param parentId 父级id
     * @return 父级名称
     */
    public String getParentName(int parentId) {
        Authority authority = this.getOne(new LambdaQueryWrapper<Authority>()
                .eq(Authority::getAuthorityId, parentId)
                .select(Authority::getAuthorityName)
        );
        return authority != null ? authority.getAuthorityName() : null;
    }

    /**
     * 获取所有权限sn
     *
     * @return List<String>
     */
    public List<String> getAuthoritySnList(List<String> authorities) {
        LambdaQueryWrapper<Authority> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        // 指定要查询的字段
        lambdaQueryWrapper.select(Authority::getAuthoritySn);

        //查询出所有的权限标识
        List<String> list = this.list(lambdaQueryWrapper)
                .stream()
                .map(Authority::getAuthoritySn)
                .toList();

        // 筛出权限并返回
        return authorities.stream()
                .filter(list::contains)
                .toList();
    }

    /**
     * 转换为树形结构
     *
     * @param authorityList 权限列表
     * @return List<Tree < String>>
     */
    public List<Tree<Integer>> convertTreeList(List<Authority> authorityList, Integer type) {
        if (authorityList.isEmpty()) {
            return null;
        }
        // 构建树形结构
        TreeNodeConfig config = new TreeNodeConfig();
        //默认id，可以不设置
        config.setIdKey("authorityId");
        //父id
        config.setParentIdKey("parentId");
        //分类名称
        config.setNameKey("authorityName");
        //排序
        config.setWeightKey("sortOrder");
        //最大递归深度
        config.setDeep(5);

        // 转树
        List<Tree<Integer>> result = TreeUtil.build(authorityList, 0, config, ((object, treeNode) -> {
            String childAuth = object.getChildAuth();
            List<ChildAuthVO> childAuthList = new ArrayList<>();
            if (!StrUtil.isEmpty(childAuth) && !"[]".equals(childAuth)) {
                childAuthList = JSONArray.parseArray(childAuth, ChildAuthVO.class);
            }
            treeNode.putExtra("authorityId", object.getAuthorityId());
            treeNode.putExtra("parentId", object.getParentId());
            treeNode.putExtra("authorityName", object.getAuthorityName());
            treeNode.putExtra("sortOrder", object.getSortOrder());
            treeNode.putExtra("isShow", object.getIsShow());
            treeNode.putExtra("childAuth", childAuthList);
            treeNode.putExtra("routeLink", object.getRouteLink());
            treeNode.putExtra("authorityIco", object.getAuthorityIco());
            treeNode.putExtra("authoritySn", object.getAuthoritySn());

        }));

        /*if (type != null && type == 1) {
            for (Tree<Integer> treeNode : result) {
                List<Tree<Integer>> allChildren = treeNode.getChildren();
                List<Tree<Integer>> deepChildren = this.deepChildren(treeNode.getChildren());
                allChildren.addAll(deepChildren);
                treeNode.setChildren(allChildren);
            }
        }*/

        return result;
    }

    private List<Tree<Integer>> deepChildren(List<Tree<Integer>> treeNodes) {
        List<Tree<Integer>> allChildren = new ArrayList<>();

        for (Tree<Integer> treeNode : treeNodes) {
            List<Tree<Integer>> children = treeNode.getChildren();
            if (CollUtil.isNotEmpty(children)) {
                // 先加入当前节点的直接子节点
                allChildren.addAll(children);
                // 再递归加入子节点的所有子孙节点
                allChildren.addAll(deepChildren(children));
            }
        }

        return allChildren;
    }
}