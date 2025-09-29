// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.o2o;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 标签组管理
 *
 * @author Tigshop团队
 * @create 2025年01月15日
 */
@Data
@Schema(description = "标签组管理")
public class TipUpdateParam {

    @Schema(description = "标签id 编辑时必传")
    @NotNull(message = "标签id不能为空")
    private Integer tipId;

    @Schema(description = "标签名称")
    @NotBlank(message = "标签名称不能为空")
    private String tipName;

    @Schema(description = "状态0 禁用，1 启用")
    @NotNull(message = "状态不能为空")
    private Integer status;

}