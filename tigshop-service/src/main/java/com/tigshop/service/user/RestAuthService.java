package com.tigshop.service.user;

import com.tigshop.bean.vo.login.LoginWechatEventVO;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;

/**
 * 第三方登录接口
 *
 * @author Tigshop项目组
 * @create 2025年04月29日 16:05
 */
public interface RestAuthService {
    /**
     * 根据具体地授权来源，获取授权对象
     *
     * @param source 授权来源
     * @param redirectUri 回调地址
     * @return AuthRequest
     */
    AuthRequest getAuthRequest(String source, String redirectUri);

    /**
     * 第三方登录
     *
     * @param authUser 用户信息
     * @return String
     */
    LoginWechatEventVO authLogin(AuthUser authUser);
}
