// **---------------------------------------------------------------------+
// ** 文件 -- ${DESCRIPTION}
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 购物车列表dto
 *
 * @author Jayce
 */
@Data
@Schema(description = "批量操作")
public class ListDTO {
    @Schema(description = "选中操作")
    private Boolean isChecked;

    @Schema(description = "商品Id")
    private Integer productId;

    @Schema(description = "店铺ID")
    private Integer shopId;

}
