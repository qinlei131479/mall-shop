package com.tigshop.config;

import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static me.chanjar.weixin.common.api.WxConsts.EventType.*;
import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType.EVENT;

/**
 * @author wzh
 */
@Configuration
public class WxMpConfig {

    @Resource
    private ConfigService configService;

    /**
     * 创建并配置一个用于微信公众号操作的 WxMpService 实例。
     * 该方法读取配置属性并为每个公众号配置创建一个 WxMpDefaultConfigImpl 实例，
     * 将这些实例放入 MultiConfigStorages 中，并返回最终的 WxMpService 实例。
     *
     * @return WxMpService 实例，用于微信公众号操作
     * @throws RuntimeException 如果 wxService 配置异常或 Application 相关配置对象缺失
     */
    @Bean
    public WxMpService wxService() {
        ConfigPO wechatAppId = configService.getConfigByCode(SettingsEnum.WECHAT_APP_ID.getBizCode());
        ConfigPO wechatAppSecret = configService.getConfigByCode(SettingsEnum.WECHAT_APP_SECRET.getBizCode());
        ConfigPO wechatServerToken = configService.getConfigByCode(SettingsEnum.WECHAT_SERVER_TOKEN.getBizCode());
        ConfigPO wechatServerSecret = configService.getConfigByCode(SettingsEnum.WECHAT_SERVER_SECRET.getBizCode());
        // 检查配置是否存在
        List<ConfigPO> configs = Arrays.asList(wechatAppId, wechatAppSecret, wechatServerToken, wechatServerSecret);
        if (configs.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("微信公众号配置缺失，请检查配置项");
        }
        // 创建 WxMpService 实例
        WxMpService service = new WxMpServiceImpl();
        WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
        config.setAppId(wechatAppId.getBizVal());
        config.setSecret(wechatAppSecret.getBizVal());
        config.setToken(wechatServerToken.getBizVal());
        config.setAesKey(wechatServerSecret.getBizVal());
        service.setWxMpConfigStorage(config);
        return service;
    }

    @Bean
    public WxMpMessageRouter messageRouter(WxMpService wxMpService) {
        final WxMpMessageRouter newRouter = new WxMpMessageRouter(wxMpService);

        // 关注事件
        newRouter.rule().async(false).msgType(EVENT).event(SUBSCRIBE).handler(subscribeHandler()).end();

        // 取消关注事件
        newRouter.rule().async(false).msgType(EVENT).event(UNSUBSCRIBE).handler(unsubscribeHandler()).end();

        // 扫码事件
        newRouter.rule().async(false).msgType(EVENT).event(SCAN).handler(scanHandler()).end();

        // 文本消息
        newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.TEXT).handler(textMsgHandler()).end();

        // 默认
        newRouter.rule().async(false).handler(defaultHandler()).end();
        return newRouter;
    }

    // 处理用户扫码关注事件
    // 关注事件处理器
    @Bean
    public WxMpMessageHandler subscribeHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            // 获取二维码参数（如果是扫码关注）
            String eventKey = wxMessage.getEventKey();
            String sceneId = null;
            if (eventKey != null && eventKey.startsWith("qrscene_")) {
                // 去掉 "qrscene_" 前缀
                sceneId = eventKey.substring(8);
            }

            // 获取用户OpenID
            String openId = wxMessage.getFromUser();

            // TODO: 处理关注逻辑，如记录用户信息、发送欢迎消息
            System.out.println("用户关注，OpenID: " + openId + "，场景值: " + sceneId);

            // 返回欢迎消息
            return WxMpXmlOutMessage.TEXT()
                    .content("感谢关注我们的公众号！")
                    .fromUser(wxMessage.getToUser())
                    .toUser(wxMessage.getFromUser())
                    .build();
        };
    }

    // 取消关注事件处理器
    @Bean
    public WxMpMessageHandler unsubscribeHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            // 获取用户OpenID
            String openId = wxMessage.getFromUser();

            // TODO: 处理取消关注逻辑，如标记用户状态
            System.out.println("用户取消关注，OpenID: " + openId);

            // 取消关注事件不需要回复消息，返回null
            return null;
        };
    }

    // 扫码事件处理器（已关注用户）
    @Bean
    public WxMpMessageHandler scanHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            // 获取二维码场景值
            String sceneId = wxMessage.getEventKey();

            // 获取用户OpenID
            String openId = wxMessage.getFromUser();

            // TODO: 处理扫码逻辑，如根据场景值执行不同操作
            System.out.println("用户扫码，OpenID: " + openId + "，场景值: " + sceneId);

            // 返回扫码响应消息
            return WxMpXmlOutMessage.TEXT()
                    .content("您扫描了场景: " + sceneId)
                    .fromUser(wxMessage.getToUser())
                    .toUser(wxMessage.getFromUser())
                    .build();
        };
    }

    // 文本消息处理器
    @Bean
    public WxMpMessageHandler textMsgHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            // 获取用户发送的文本内容
            String content = wxMessage.getContent();

            // 获取用户OpenID
            String openId = wxMessage.getFromUser();

            // TODO: 处理文本消息，如关键词回复、智能客服等
            System.out.println("收到文本消息，OpenID: " + openId + "，内容: " + content);

            // 根据内容返回不同回复
            if ("帮助".equals(content)) {
                return WxMpXmlOutMessage.TEXT()
                        .content("您可以发送以下关键词获取帮助：\n1. 商品查询\n2. 订单状态\n3. 联系客服")
                        .fromUser(wxMessage.getToUser())
                        .toUser(wxMessage.getFromUser())
                        .build();
            } else {
                return WxMpXmlOutMessage.TEXT()
                        .content("收到您的消息：" + content)
                        .fromUser(wxMessage.getToUser())
                        .toUser(wxMessage.getFromUser())
                        .build();
            }
        };
    }

    // 默认处理器
    @Bean
    public WxMpMessageHandler defaultHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            // 处理其他类型的消息
            System.out.println("收到未知类型消息，MsgType: " + wxMessage.getMsgType());

            return WxMpXmlOutMessage.TEXT()
                    .content("暂不支持此类消息")
                    .fromUser(wxMessage.getToUser())
                    .toUser(wxMessage.getFromUser())
                    .build();
        };
    }
}
