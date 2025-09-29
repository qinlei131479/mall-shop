// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.oauth.impl;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.feign.wechat.GetTokenResult;
import com.tigshop.bean.feign.wechat.GetWechatCodeParam;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.service.oauth.WechatOAuthService;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * 微信实现
 *
 * @author Tigshop团队
 * @create 2025年01月23日 17:08
 */
@Service
public class WechatOAuthServiceImpl implements WechatOAuthService {
    @Resource
    private WechatApiClient wechatApiClient;

    @Resource
    private ConfigService configService;

    @Override
    public String getMiniCode(String path, Integer productId) {
        // 获取微信配置
        String appId = configService.getConfigByCode(SettingsEnum.WECHAT_APP_ID.getBizCode()).getBizVal();
        String appSecret = configService.getConfigByCode(SettingsEnum.WECHAT_APP_SECRET.getBizCode()).getBizVal();
        // 获取token
        GetTokenResult tokenResult = wechatApiClient.getToken("client_credential", appId, appSecret);
        String accessToken = tokenResult.getAccessToken();
        // 获取小程序二维码
        ResponseEntity<byte[]> js = wechatApiClient.getWechatCode(accessToken,
                new GetWechatCodeParam(StrUtil.format("id{}", productId), path));

        byte[] imageBytes = js.getBody();
        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * 获取微信 access_token , 用于发送模板信息，
     * access_token 有效期2小时
     * @return
     */
    @Override
    public String getMpAccessToken() {
        // 获取微信配置
        String appId = configService.getConfigByCode(SettingsEnum.WECHAT_APP_ID.getBizCode()).getBizVal();
        String appSecret = configService.getConfigByCode(SettingsEnum.WECHAT_APP_SECRET.getBizCode()).getBizVal();
        // 获取token
        GetTokenResult tokenResult = wechatApiClient.getToken("client_credential", appId, appSecret);
        return tokenResult.getAccessToken();
    }

    @Override
    public String getMiniAccessToken() {
        // 获取微信配置
        String appId = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID.getBizCode()).getBizVal();
        String appSecret = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_SECRET.getBizCode()).getBizVal();
        // 获取token
        GetTokenResult tokenResult = wechatApiClient.getToken("client_credential", appId, appSecret);
        return tokenResult.getAccessToken();
    }
}