package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分配店铺 sku
 */
@TableName(value ="store_product_sku")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreProductSku implements Serializable {

    @Schema(description = "ID")
    @TableId(type = IdType.AUTO)
    private Long storeProductSkuId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品 sku ID")
    private Long productSkuId;

    @Schema(description = "商品价格")
    private BigDecimal skuPrice;

    @Schema(description = "sku库存")
    private Integer skuStock;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "门店商品id")
    private Long storeProductId;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}