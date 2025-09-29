// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.utils;

import com.aliyun.teaopenapi.models.Config;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.tigshop.common.exception.GlobalException;

/**
 * 短信工具类
 *
 * @author Tigshop团队
 * @create 2025年02月28日 11:01
 */
public class SmsUtils {

    /**
     * 使用AK&SK初始化账号Client
     * @param accessKeyId AK
     * @param accessKeySecret sk
     * @return Client
     */
    public static Client createClient(String accessKeyId, String accessKeySecret) {
        // 初始化请求客户端
        Config config = new Config()
                // 配置 AccessKey ID，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID。
                .setAccessKeyId(accessKeyId)
                // 配置 AccessKey Secret，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
                .setAccessKeySecret(accessKeySecret);

        // 配置 Endpoint
        config.endpoint = "dysmsapi.aliyuncs.com";

        try {
            return new Client(config);
        } catch (Exception e) {
            throw new GlobalException("无法连接短信服务器，请稍后重试...");
        }
    }

    /**
     * 发送短信
     * @param accessKeyId AK
     * @param accessKeySecret sk
     * @param phoneNumbers 手机号
     * @param signName 短信签名
     * @param templateCode 短信模板
     * @param templateParam 短信模板参数
     * @return SendSmsResponse 发送短信响应
     */
    public static SendSmsResponse sendSms(String accessKeyId, String accessKeySecret, String phoneNumbers, String signName, String templateCode, String templateParam) {
        Client client = createClient(accessKeyId, accessKeySecret);
        try {
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phoneNumbers)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam(templateParam);
            return client.sendSms(request);
        } catch (Exception e) {
            throw new GlobalException(e.getMessage());
        }
    }
}