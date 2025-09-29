// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.order;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 售后商品VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "售后商品参数")
public class AftersalesItemVO {
    @Schema(description = "售后项目ID")
    private Integer aftersalesItemId;

    @Schema(description = "订单项ID")
    private Integer orderItemId;

    @Schema(description = "数量")
    private Integer number;

    @Schema(description = "售后ID")
    private Integer aftersaleId;

    @Schema(description = "订单号")
    private String orderSn;

    @JsonTranslate
    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "产品缩略图")
    private String picThumb;

    @Schema(description = "产品编号")
    private String productSn;

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "价格")
    private BigDecimal price;
}