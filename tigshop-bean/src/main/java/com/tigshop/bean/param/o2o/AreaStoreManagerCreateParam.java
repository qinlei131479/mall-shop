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

import java.util.List;


/**
 * 区域门店管理创建数据对象
 *
 * @author Tigshop团队
 * @create 2025年01月15日
 */
@Data
@Schema(description = "区域门店管理创建参数")
public class AreaStoreManagerCreateParam {
    
    @Schema(description = "区域门店名称")
    @NotBlank(message = "区域名称不能为空")
    private String areaStoreName;

    @Schema(description = "排序字段")
    @NotNull(message = "排序字段不能为空")
    private Integer sortOrder;

    @Schema(description = "门店id")
    @NotNull(message = "门店id不能为空")
    private List<Integer> shopIds;
}