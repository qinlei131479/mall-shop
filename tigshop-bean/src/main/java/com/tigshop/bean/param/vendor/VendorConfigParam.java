// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.param.vendor;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop团队
 * @create 2025/7/7 17:19
 */
@Data
@Schema(description = "供应商配置")
public class VendorConfigParam {
    @Schema(description = "供应商状态 1开启 2关闭")
    private Integer status;
    @Schema(description = "供应商客服电话")
    private String kefuPhone;
}
