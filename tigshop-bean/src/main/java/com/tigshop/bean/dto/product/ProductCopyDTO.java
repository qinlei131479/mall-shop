package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 产品复制 入参
 *
 * @author kidd
 * @since 2025/3/26 09:10
 */
@Data
public class ProductCopyDTO {

    @Schema(description = "产品主键")
    private Integer productId;
}
