package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品组织分配表
 */
@TableName(value = "product_org")
@Data
public class ProductOrg implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long productOrgId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "分配组织类型；1-门店，2-区域")
    private Integer orgType;

    @Schema(description = "分配组织 ID")
    private Long orgId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Long addTime;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}