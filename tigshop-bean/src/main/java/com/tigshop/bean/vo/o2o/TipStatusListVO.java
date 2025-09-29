// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.o2o;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 区域门店管理VO
 *
 * @author Tigshop团队
 * @create 2025年01月15日
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "标签")
public class TipStatusListVO {

    @Schema(description = "id")
    private Integer status;

    @Schema(description = "描述")
    private String description;

}