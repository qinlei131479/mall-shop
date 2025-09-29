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

package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 短信发送参数
 * @author Tigshop团队
 * @create 2025年03月05日 17:52
 */
@Data
@Schema(description = "短信发送参数")
public class SmsSendDTO implements Serializable {
    @Schema(description = "accessKeyId")
    private String accessKeyId;
    @Schema(description = "accessKeySecret")
    private String accessKeySecret;
    @Schema(description = "手机号码")
    private String phoneNumbers;
    @Schema(description = "短信签名")
    private String signName;
    @Schema(description = "短信模板")
    private String templateCode;
    @Schema(description = "短信模板参数")
    private String templateParam;

    public SmsSendDTO(String accessKeyId, String accessKeySecret, String phoneNumbers, String signName, String templateCode, String templateParam) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.phoneNumbers = phoneNumbers;
        this.signName = signName;
        this.templateCode = templateCode;
        this.templateParam = templateParam;
    }
}