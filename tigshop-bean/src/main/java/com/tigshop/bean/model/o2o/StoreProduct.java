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
 * 分配店铺商品
 */
@TableName(value = "store_product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreProduct implements Serializable {

    @Schema(description = "ID")
    @TableId(type = IdType.AUTO)
    private Long storeProductId;

    @Schema(description = "商品id")
    private Integer productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "商品状态；1-上架，0-下架")
    private Integer productStatus;

    @Schema(description = "产品库存")
    private Integer productStock;

    @Schema(description = "门店id")
    private Long shopId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "卡组ID")
    private Integer cardGroupId;

    @Schema(description = "店铺分类ID")
    private Integer shopCategoryId;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}