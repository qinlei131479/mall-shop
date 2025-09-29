package com.tigshop.bean.vo.product;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * @author Tigshop项目组
 * @create 2025年05月20日 16:17
 */
@Data
@Builder
public class ProductLookAlsoVO {
    @Schema(description = "市场价")
    private String marketPrice;

    @Schema(description = "缩略图")
    private String picThumb;

    @Schema(description = "最终价格")
    private String price;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品名称")
    @JsonTranslate
    private String productName;

    @Schema(description = "商品编号")
    private String productSn;

}