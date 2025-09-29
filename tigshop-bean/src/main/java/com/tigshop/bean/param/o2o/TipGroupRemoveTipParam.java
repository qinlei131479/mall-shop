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
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 区域删除店铺参数
 *
 * @author Tigshop团队
 * @create 2025年01月15日
 */
@Data
@Schema(description = "区域删除店铺参数")
public class TipGroupRemoveTipParam {
    
    @Schema(description = "标签组ID")
    @NotNull(message = "标签组ID不能为空")
    private Integer tipGroupId;

    @Schema(description = "标签id")
    @NotNull(message = "标签id不能为空")
    private Integer tipId;
}