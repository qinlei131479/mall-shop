/*
 * ---------------------------------------------------------------------+
 *  文件 --
 * ---------------------------------------------------------------------+
 *  版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
 * ---------------------------------------------------------------------+
 *  作者：Tigshop团队，yq@tigshop.com
 * ---------------------------------------------------------------------+
 *  提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
 * ---------------------------------------------------------------------+
 */
package com.tigshop.security.admin.filter;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.security.admin.service.UserDetailsServiceImpl;
import com.tigshop.security.common.utils.JwtUtil;
import com.tigshop.service.shop.ShopService;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.tigshop.security.admin.config.SecurityConfig.URL_WHITELIST;

/**
 * @author Tigshop团队
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private UserDetailsServiceImpl userDetailsService;

    @Resource
    private PathMatcher pathMatcher;

    @Resource
    private RedisCache redisCache;

    @Resource
    private ShopService shopService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        if (Arrays.stream(URL_WHITELIST).anyMatch(pattern -> pathMatcher.match(pattern, requestURI))) {
            chain.doFilter(request, response);
            return;
        }
        final String authorization = request.getHeader(JwtUtil.HEADER);

        // 如果请求头中没有token，不能访问需要权限的数据
        if (StrUtil.isBlankOrUndefined(authorization)) {
            chain.doFilter(request, response);
            return;
        }
        // 去掉 Bearer
        final String token = authorization.replaceAll(Constants.TOKEN_PREFIX, "");
        if (StrUtil.isBlankOrUndefined(token)) {
            chain.doFilter(request, response);
            return;
        }
        // 验证token
        Claims claims = jwtUtil.getClaimsByToken(token);
        if (claims == null) {
            throw new GlobalException("token异常");
        }
        if (jwtUtil.isTokenExpired(claims.getExpiration())) {
            throw new GlobalException("token已过期");
        }

        String username = claims.getSubject();
        // 判断redis中是否存在token
        Object redisToken = redisCache.getCacheObject(Constants.ADMIN_TOKEN + username);
        Assert.notNull(redisToken, () -> new GlobalException("token已过期"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        // 构建UsernamePasswordAuthenticationToken
        Integer userId = claims.get("userId", Integer.class);
        String shopOrVendorId = null;
        if (ObjectUtil.equals(HeaderUtils.getAdminType(), "shop")) {
            shopOrVendorId = Optional.ofNullable(HeaderUtils.getShopId())
                    .map(String::valueOf)
                    .orElse(null);;
        } else if (ObjectUtil.equals(HeaderUtils.getAdminType(), "vendor")) {
            shopOrVendorId = Optional.ofNullable(HeaderUtils.getVendorId())
                    .map(String::valueOf)
                    .orElse(null);;
        }
        List<String> authorityList = List.of();
        if (!requestURI.equals("/adminapi/merchant/shop/myShop")) {
            authorityList = shopService.checkAndReturnAuthList(userId, shopOrVendorId, HeaderUtils.getAdminType());
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userDetails, userDetails.getPassword(),
                        AuthorityUtils.createAuthorityList(authorityList)
                )
        );
        chain.doFilter(request, response);
    }
}