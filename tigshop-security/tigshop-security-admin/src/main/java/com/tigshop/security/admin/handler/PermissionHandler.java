// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.security.admin.handler;

import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Objects;

/**
 * 自定义默认权限
 *
 * @author Jayce
 * @create 2024年10月16日 13:04
 */
public class PermissionHandler {

    /**
     * 判断接口是否有任意xxx，xxx权限
     *
     * @param permissions 权限
     * @return {boolean}
     */
    public boolean hasPermission(String... permissions) {

        if (permissions == null || permissions.length == 0) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // 检查是否有 "all" 权限
        if (authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("all"::equals)) {
            return true;
        }

        // 检查是否有传入的权限
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(StringUtils::hasText)
                .anyMatch(auth -> PatternMatchUtils.simpleMatch(permissions, auth));
    }


    public boolean hasPermissionOrSelf(String permission, Integer targetUserId) {
        Integer currentUserId = SecurityUtils.getCurrentAdminId();
        return targetUserId.equals(currentUserId) || hasPermission(permission);
    }

}
