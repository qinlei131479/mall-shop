// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Objects;

import static com.tigshop.common.utils.TigUtils.getJarDir;

/**
 * @author Tigshop团队
 * @create 2024年11月11日 14:39
 */
@Service
public class PayServiceFactory {

    @Resource
    private ConfigService configService;

    //取配置的file_upload_dir
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 获取指定支付方式的 WxPayService
    public WxPayService getWxPayService(String clientType) {
        // 根据支付方式类型从数据库中获取不同的配置信息
        String appId = switch (clientType) {
            case "wechat", "pc", "h5" -> {
                ConfigPO wechatAppIdConfig = configService.getConfigByCode(SettingsEnum.WECHAT_APP_ID.getBizCode());
                yield wechatAppIdConfig.getBizVal();
            }
            case "miniProgram" -> {
                ConfigPO wechatMiniProgramAppIdConfig = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID.getBizCode());
                yield wechatMiniProgramAppIdConfig.getBizVal();
            }
            case "app" -> {
                ConfigPO wechatPayAppIdConfig = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_APP_ID.getBizCode());

                yield wechatPayAppIdConfig.getBizVal();
            }
            default -> null;
        };

        // 根据支付方式选择不同的配置
        WxPayConfig wxPayConfig = new WxPayConfig();
        if (StrUtil.isNotBlank(appId)){
            wxPayConfig.setAppId(appId);
        }
        ConfigPO wechatPayMchidConfig = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_MCHID.getBizCode());
        wxPayConfig.setMchId(wechatPayMchidConfig.getBizVal());
        // 获取当前 JAR 包所在目录
        String jarDir = getJarDir();

        // 在 JAR 目录下创建 uploadDir 文件夹
        File pemDir = FileUtil.mkdir(jarDir + File.separator + uploadDir);
        String absolutePath = pemDir.getAbsolutePath();

        // 打印路径
        System.out.println("PEM 目录路径: " + absolutePath);
        // 读取私钥文件
        wxPayConfig.setPrivateKeyPath(absolutePath + File.separator + "apiclient_key.pem");
        wxPayConfig.setPrivateCertPath(absolutePath + File.separator + "apiclient_cert.pem");
        ConfigPO wechatPaySerialNoConfig = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_SERIAL_NO.getBizCode());
        wxPayConfig.setCertSerialNo(wechatPaySerialNoConfig.getBizVal());
        ConfigPO wechatPayKeyConfig = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_KEY.getBizCode());
        wxPayConfig.setApiV3Key(wechatPayKeyConfig.getBizVal());

        ConfigPO wechatPayCheckTypeConfig = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_CHECK_TYPE.getBizCode());
        if (Objects.equals(wechatPayCheckTypeConfig.getBizVal(), "2")) {
            wxPayConfig.setPublicKeyPath(absolutePath + File.separator + "public_key.pem");
        }
        // 创建 WxPayService 实例并设置配置
        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(wxPayConfig);

        wxPayService.getConfig().getVerifier().getValidCertificate();

        return wxPayService;
    }

}
