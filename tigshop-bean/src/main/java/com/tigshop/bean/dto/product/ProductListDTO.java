package com.tigshop.bean.dto.product;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品列表入参
 *
 * @author Jayce
 * @create 2024年11月20日 15:10
 */
@Getter
@Setter
public class ProductListDTO extends BasePage {
    @Schema(description = "介绍类型")
    private String introType;

    @Schema(description = "是否删除，0-未删除，1-已删除，-1-全部")
    private Integer isDelete;

    @Schema(description = "店铺ID，-1表示所有店铺，-2表示其他特定店铺")
    private Integer shopId;

    @Schema(description = "产品状态，1-正常，0-下架")
    private Integer productStatus;

    @Schema(description = "审核状态，1-审核通过，0-未审核")
    private Integer checkStatus;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "是否查所有店铺商品")
    private Integer searchShop;

    @Schema(description = "商品分类ID")
    private Integer categoryId;

    @Schema(description = "品牌ID")
    private Integer brandId;

    @Schema(description = "商品组ID")
    private Integer productGroupId;

    @Schema(description = "店铺分类ID")
    private Integer shopCategoryId;

    @Schema(description = "商品id组 (默认为 null)")
    private String ids;

    @Schema(description = "商品id list组 (默认为 null)")
    private List<Integer> idList;

    @Schema(description = "不包含的商品ids")
    private List<Integer> extraProductIds;

    @Schema(description = "品牌ids")
    private String brandIds;

    @Schema(description = "优惠券id")
    private Integer couponId;

    @Schema(description = "价格区间")
    private BigDecimal minPrice;

    @Schema(description = "价格区间")
    private BigDecimal maxPrice;

    @Schema(description = "是否最新")
    private Integer isNew;

    @Schema(description = "供应商id")
    private Integer suppliersId;

    @Schema(description = "是否显示，-1-全部，0-不显示，1-显示")
    private Integer isShow;

    @Schema(description = "排序字段")
    private String sortField;

    @Schema(description = "排序顺序，asc-升序，desc-降序")
    private String sortOrder;

    @Schema(description = "是否包含购物车数量，0-不包含，1-包含")
    private Integer withCartSum;

    @Schema(description = "用于前台搜索，sale|price|time")
    private String sort;

    @Schema(description = "用于后台端还是客户端（0：后台，1：客户端）")
    private Integer isClient;


}
