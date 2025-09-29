package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wzh
 */
@Data
public class ProductSkuInfoDTO {
    @Schema(description = "规格ID")
    private Integer skuId;

    @Schema(description = "货品库存")
    private Integer skuStock;
}
