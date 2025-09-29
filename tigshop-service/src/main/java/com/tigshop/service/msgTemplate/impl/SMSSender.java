package com.tigshop.service.msgTemplate.impl;

import cn.hutool.json.JSONUtil;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.tigshop.bean.dto.msg.Message;
import com.tigshop.bean.dto.setting.SmsSendDTO;
import com.tigshop.bean.enums.msg.MessageTemplateType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.utils.SmsUtils;
import com.tigshop.service.msgTemplate.MessageSender;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 短信发送
 */
@Slf4j
@Component
public class SMSSender implements MessageSender {

    @Resource
    private ConfigService configService;

    @Override
    public boolean supports(MessageTemplateType type) {
        return type == MessageTemplateType.SMS;
    }

    @Override
    public void send(Message message) {
        try {
            List<SettingsEnum> settings = List.of(SMS_KEY_ID, SMS_KEY_SECRET, SMS_SIGN_NAME);
            Map<String, String> configMap = configService.getConfigMapBySettings(settings);

            // 获取相关配置
            SmsSendDTO smsSend = new SmsSendDTO(
                    configMap.get(SMS_KEY_ID.getBizCode()),
                    configMap.get(SMS_KEY_SECRET.getBizCode()),
                    (String) message.getMessage().get("mobile"),
                    configMap.get(SMS_SIGN_NAME.getBizCode()),
                    (String) message.getMessage().get("template_code"),
                    JSONUtil.toJsonStr(message.getMessage().get("content")));

            // 模拟调用阿里云短信服务
            SendSmsResponse sendSmsResponse = SmsUtils.sendSms(
                    smsSend.getAccessKeyId(),
                    smsSend.getAccessKeySecret(),
                    smsSend.getPhoneNumbers(),
                    smsSend.getSignName(),
                    smsSend.getTemplateCode(),
                    smsSend.getTemplateParam());
            if (!"OK".equals(sendSmsResponse.getBody().getCode())) {
                log.info("短信发送失败！{}", JSONUtil.toJsonStr(smsSend));
            }
            log.info("短信发送成功！{}", JSONUtil.toJsonStr(smsSend));
        } catch (Exception e) {
            log.error("短信发送失败！{}", e.getMessage());
        }
    }
}
