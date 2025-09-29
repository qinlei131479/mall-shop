// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.vendor;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop团队
 * @create 2025/7/4 15:04
 */
@Data
@Schema(description = "供应商配置")
public class VendorDetailConfigVO extends VendorDetailVO{

    @Schema(description = "供应商客服电话")
    private String kefuPhone;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "创建时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String addTime;
}
