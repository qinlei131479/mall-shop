package com.tigshop.service.user.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.user.AuthorizeTypeEnum;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserAuthorize;
import com.tigshop.bean.vo.login.LoginWechatEventVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.security.common.utils.JwtUtil;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.user.RestAuthService;
import com.tigshop.service.user.UserAuthorizeService;
import com.tigshop.service.user.UserService;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.enums.scope.AuthFacebookScope;
import me.zhyd.oauth.enums.scope.AuthGoogleScope;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthFacebookRequest;
import me.zhyd.oauth.request.AuthGoogleRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthScopeUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 第三方登录实现
 *
 * @author Tigshop项目组
 * @create 2025年04月29日 16:09
 */
@Service
public class RestAuthServiceImpl implements RestAuthService {
    private final ConfigService configService;
    private final UserAuthorizeService  userAuthorizeService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public RestAuthServiceImpl(ConfigService configService, UserAuthorizeService userAuthorizeService,
                               JwtUtil jwtUtil, UserService userService, PasswordEncoder passwordEncoder) {
        this.configService = configService;
        this.userAuthorizeService = userAuthorizeService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthRequest getAuthRequest(String source, String redirectUri) {
        AuthRequest authRequest = null;
        switch (source) {
            case "google" -> {
                // 根据source从config表中获取对应的配置
                String clientId = configService.getConfigByCode(SettingsEnum.GOOGLE_CLIENT_ID.getBizCode()).getBizVal();
                String clientSecret = configService.getConfigByCode(SettingsEnum.GOOGLE_CLIENT_SECRET.getBizCode()).getBizVal();
                // 创建授权对象
                authRequest = new AuthGoogleRequest(AuthConfig.builder()
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .redirectUri(redirectUri)
                        .scopes(AuthScopeUtils.getScopes(AuthGoogleScope.USER_EMAIL, AuthGoogleScope.USER_PROFILE, AuthGoogleScope.USER_OPENID))
                        // 针对国外平台配置代理(国内服务器需要自行配置)
                        /*.httpConfig(HttpConfig.builder()
                                .timeout(15000)
                                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10809)))
                                .build())*/
                        .build());
            }

            case "facebook" -> {
                String clientId = configService.getConfigByCode(SettingsEnum.FACEBOOK_CLIENT_ID.getBizCode()).getBizVal();
                String clientSecret = configService.getConfigByCode(SettingsEnum.FACEBOOK_CLIENT_SECRET.getBizCode()).getBizVal();
                authRequest = new AuthFacebookRequest(AuthConfig.builder()
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .redirectUri(redirectUri)
                        .scopes(AuthScopeUtils.getScopes(AuthFacebookScope.values()))
                        .build());
            }
            default -> {}
        }
        if (null == authRequest) {
            throw new GlobalException("未获取到有效的Auth配置");
        }
        return authRequest;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginWechatEventVO authLogin(AuthUser authUser) {
        // 平台
        String source = authUser.getSource();
        // 根据第三方平台获取授权类型枚举
        AuthorizeTypeEnum authorizeTypeEnum = AuthorizeTypeEnum.fromCode(source);

        return getToken(authUser, authorizeTypeEnum);
    }

    /**
     * 获取token
     * @param authUser 授权用户
     * @param authorizeTypeEnum 授权第三方枚举
     * @return LoginWechatEventVO
     */
    public LoginWechatEventVO getToken(AuthUser authUser, AuthorizeTypeEnum authorizeTypeEnum) {
        // 查询是否授权
        UserAuthorize userAuthorize = userAuthorizeService.getOne(
                new LambdaQueryWrapper<UserAuthorize>()
                        .eq(UserAuthorize::getOpenId, authUser.getUuid())
                        .eq(UserAuthorize::getAuthorizeType, authorizeTypeEnum.getCode()));
        // 没有授权，开始注册
        if (null == userAuthorize){
            // 保存用户信息
            User user = User.builder()
                    .username(authUser.getUsername())
                    .mobile("")
                    .email("")
                    .password(passwordEncoder.encode(RandomUtil.randomNumbers(8)))
                    .avatar(authUser.getAvatar())
                    .nickname("")
                    .regTime(StringUtils.getCurrentTime())
                    .referrerUserId(0)
                    .mobileValidated(1)
                    .emailValidated(0)
                    .build();
            userService.save(user);

            // 保存授权信息
            UserAuthorize saveUserAuthorize = UserAuthorize.builder()
                    .authorizeType(authorizeTypeEnum.getCode())
                    .openId(authUser.getUuid())
                    .openData(authUser.getRawUserInfo().toJSONString())
                    .openName(authUser.getUsername())
                    .openPhoto(authUser.getAvatar())
                    .unionid(authUser.getUuid())
                    .addTime(StringUtils.getCurrentTime())
                    .userId(user.getUserId())
                    .build();
            userAuthorizeService.save(saveUserAuthorize);

            String token = jwtUtil.generateToken(user.getUserId(), false, user.getUsername());
            return LoginWechatEventVO.builder().token(token).type(1).build();
        }
        User user = userService.getById(userAuthorize.getUserId());
        String token = jwtUtil.generateToken(user.getUserId(), false, user.getUsername());
        return LoginWechatEventVO.builder().token(token).type(1).build();
    }
}