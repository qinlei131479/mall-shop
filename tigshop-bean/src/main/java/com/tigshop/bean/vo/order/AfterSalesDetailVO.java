package com.tigshop.bean.vo.order;

import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 售后详情数据
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@Data
@Schema(description = "售后详情数据")
public class AfterSalesDetailVO {
    @Schema(description = "商品ID")
    private Integer itemId;

    @Schema(description = "商品缩略图")
    private String picThumb;

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "商品名称")
    @JsonTranslate
    private String productName;

    @Schema(description = "商品价格")
    private BigDecimal price;

    @Schema(description = "商品数量")
    private Integer quantity;

    @Schema(description = "商品小计")
    private BigDecimal subtotal;

    @Schema(description = "SKU数据")
    private List<ProductSkuDTO.SkuData> skuData;

    @Schema(description = "可申请数量")
    private Integer canApplyQuantity;
}

