package com.tigshop.bean.vo.user;

import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品收藏VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "商品收藏参数")
public class CollectProductVO {
    @Schema(description = "商品收藏ID")
    private Integer collectId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "商品收藏名称")
    @JsonTranslate
    private String productName;

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "商品图片")
    private String picThumb;

    @Schema(description = "商品大图")
    private String picUrl;

    @Schema(description = "市场价（划线价）")
    private BigDecimal marketPrice;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "是否促销")
    private Integer isPromote;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "等级ID")
    private Integer rankId;

    @Schema(description = "折扣")
    private BigDecimal discount;

    @Schema(description = "SKU价格")
    private BigDecimal skuPrice;

    @Schema(description = "商品SKU")
    private List<ProductSkuDTO> productSku;

    @Schema(description = "价格")
    private BigDecimal price;
}
