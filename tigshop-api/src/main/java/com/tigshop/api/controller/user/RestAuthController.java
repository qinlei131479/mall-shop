package com.tigshop.api.controller.user;

import com.tigshop.bean.vo.login.LoginWechatEventVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.user.RestAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.web.bind.annotation.*;


/**
 * @author Tigshop项目组
 */
@RestController
@RequestMapping("/api/user/oauth")
@Tag(name = "第三方登录", description = "第三方登录功能")
public class RestAuthController {

    @Resource
    private RestAuthService authService;

    /**
     * 获取授权链接
     *
     * @param source 平台来源
     * @param redirectUri 回调地址
     * @return String
     */
    @GetMapping("/render/{source}")
    @Operation(summary = "获取授权链接")
    public String renderAuth(@PathVariable("source") String source, @RequestParam("redirectUri") String redirectUri) {
        // 根据source从config表中获取对应的配置
        AuthRequest authRequest = authService.getAuthRequest(source, redirectUri);
        return authRequest.authorize(AuthStateUtils.createState());
    }

    /**
     * 授权回调地址
     *
     * @param source 平台来源
     * @param redirectUri 回调地址
     * @param callback 回调对象
     * @return Object
     */
    @PostMapping("/callback/{source}")
    @Operation(summary = "校验授权回调地址并登录")
    public LoginWechatEventVO login(@PathVariable("source") String source, @RequestParam("redirectUri") String redirectUri, @RequestBody AuthCallback callback) {
        // 获取授权对象
        AuthRequest authRequest = authService.getAuthRequest(source, redirectUri);
        AuthResponse<AuthUser> login = authRequest.login(callback);
        if (login.getCode() != 2000){
            throw new GlobalException("登录已过期");
        }
        AuthUser authUser = login.getData();
        // 登录
        return authService.authLogin(authUser);
    }

    @GetMapping("/callback/{source}")
    @Operation(summary = "校验授权回调地址并登录（测试版）")
    public Object loginTest(@PathVariable("source") String source, AuthCallback callback) {
        // 获取授权对象
        AuthRequest authRequest = authService.getAuthRequest(source, "https://17372ribz030.vicp.fun/api/user/oauth/callback/google");
        // 登录
        AuthResponse<AuthUser> login = authRequest.login(callback);
        if (login.getCode() != 2000){
            throw new GlobalException("登录已过期");
        }
        return authService.authLogin(login.getData());
    }

}
