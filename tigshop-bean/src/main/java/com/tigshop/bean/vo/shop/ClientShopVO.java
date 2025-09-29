package com.tigshop.bean.vo.shop;

import com.tigshop.bean.model.product.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 店铺表VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "前台店铺表")
public class ClientShopVO {
    @Schema(description = "店铺状态的文本描述")
    private String statusText;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "店铺标题")
    private String shopTitle;

    @Schema(description = "店铺添加时间")
    private String addTime;

    @Schema(description = "店铺Logo URL")
    private String shopLogo;

    @Schema(description = "点击量")
    private Integer clickCount;

    @Schema(description = "店铺状态码")
    private Integer status;

    @Schema(description = "商家ID")
    private Integer merchantId;

    @Schema(description = "店铺可用资金")
    private BigDecimal shopMoney;

    @Schema(description = "店铺冻结资金")
    private BigDecimal frozenMoney;

    @Schema(description = "店铺联系手机号")
    private String contactMobile;

    @Schema(description = "店铺描述")
    private String description;

    @Schema(description = "客服电话")
    private String kefuPhone;

    @Schema(description = "客服微信")
    private String kefuWeixin;

    @Schema(description = "客服链接")
    private String kefuLink;

    @Schema(description = "是否可以联系客服")
    private Integer isContactKefu;

    @Schema(description = "客服入口")
    private String kefuInlet;

    @Schema(description = "收藏次数")
    private Long collectCount;

    @Schema(description = "上架商品数量")
    private Long listingCount;

    @Schema(description = "上架商品列表")
    private List<Product> listingProduct;
}