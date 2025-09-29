// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.cart;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.tigshop.bean.dto.cart.CarExtraSkuDataDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.model.cart.Cart;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.bean.vo.shop.ShopBaseVO;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.utils.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 购物车VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "购物车参数")
public class CartVO {

    // *** Cart ***

    @Schema(description = "购物车ID")
    private Integer cartId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品缩略图")
    private String picThumb;

    @Schema(description = "市场价")
    private BigDecimal marketPrice;

    @Schema(description = "原价")
    private String originalPrice;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "SKU ID")
    private Integer skuId;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "购物车类型")
    private Integer type;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "销售员ID")
    private Integer salesmanId;

    // *** Product ***

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "商品名称")
    @JsonTranslate
    private String productName;

    @Schema(description = "商品类型")
    private Integer productType;

    @Schema(description = "品牌ID")
    private Integer brandId;

    @Schema(description = "卡券组ID")
    private Integer cardGroupId;

    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "供应商ID")
    private Integer suppliersId;

    @Schema(description = "虚拟样品")
    private String virtualSample;

    @Schema(description = "是否包邮")
    private Integer freeShipping;

    @Schema(description = "运费模板ID")
    private Long shippingTplId;

    @Schema(description = "商品状态")
    private Integer productStatus;

    @Schema(description = "是否无需配送；0-否，1-是")
    private Integer noShipping;

    @Schema(description = "商品重量")
    private BigDecimal productWeight;

    // *** ProductSku ***

    @Schema(description = "SKU信息")
    private ProductSkuDTO sku;

    // *** Shop ***

    @Schema(description = "店铺信息")
    private ShopBaseVO shop;

    // *** Other ***

    @Schema(description = "会员价格")
    private BigDecimal memberPrice;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "原始价格")
    private BigDecimal originPrice;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "是否选中")
    private Boolean isChecked;

    @Schema(description = "额外SKU数据")
    private List<CarExtraSkuDataDTO> extraSkuData;

    @Schema(description = "额外SKU数据")
    private List<CarExtraSkuDataDTO> extraSkuAllData;

    @Schema(description = "SKU 数据")
    private JSONArray skuData;

    @Schema(description = "是否有SKU")
    private Boolean hasSku;

    @Schema(description = "小计")
    private BigDecimal subtotal;

    // *** Unknown ***

    @Schema(description = "是否禁用")
    private Boolean isDisabled;

    @Schema(description = "活动信息")
    private List<PromotionVO> activityInfo;

    @Schema(description = "服务费")
    private String serviceFee;

    @Schema(description = "是否秒杀")
    private Integer isSeckill;

    @Schema(description = "运费类型")
    private Integer fixedShippingType;

    @Schema(description = "固定运费金额")
    private BigDecimal fixedShippingFee;

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Schema(description = "供应商商品ID")
    private Integer vendorProductId;

    @Schema(description = "供应商商品skuID")
    private Integer vendorProductSkuId;

    public CartVO(Cart cart, Product product, ProductSku productSku, Shop shop, BigDecimal productPrice, BigDecimal memberPrice, Integer stock) {
        this.cartId = cart.getCartId();
        this.userId = cart.getUserId();
        this.productId = cart.getProductId();
        this.picThumb = cart.getPicThumb();
        this.marketPrice = cart.getMarketPrice();
        this.originalPrice = cart.getOriginalPrice().toString();
        this.quantity = cart.getQuantity();
        this.skuId = cart.getSkuId();
        this.shopId = cart.getShopId();
        this.type = cart.getType();
        this.updateTime = cart.getUpdateTime();
        this.salesmanId = cart.getSalesmanId();

        this.productSn = product.getProductSn();
        this.productName = product.getProductName();
        this.productType = product.getProductType();
        this.brandId = product.getBrandId();
        this.cardGroupId = product.getCardGroupId();
        this.productStock = product.getProductStock();
        this.categoryId = product.getCategoryId();
        this.suppliersId = product.getSuppliersId();
        this.virtualSample = product.getVirtualSample();
        this.freeShipping = product.getFreeShipping();
        this.shippingTplId = product.getShippingTplId();
        this.productStatus = product.getProductStatus();
        this.noShipping = product.getNoShipping();
        this.productWeight = product.getProductWeight();
        this.fixedShippingType = product.getFixedShippingType();
        this.fixedShippingFee = product.getFixedShippingFee();

        this.vendorId = product.getVendorId();
        this.vendorProductId = product.getVendorProductId();
        this.vendorProductSkuId = 0;
        if (productSku != null) {
            ProductSkuDTO productSkuDTO = BeanUtil.copyProperties(productSku, ProductSkuDTO.class, "skuData");
            List<ProductSkuDTO.SkuData> skuData = JsonUtil.jsonToList(productSku.getSkuData(), ProductSkuDTO.SkuData.class);
            productSkuDTO.setSkuData(skuData);
            productSkuDTO.setSkuPrice(productSku.getSkuPrice().toString());

            this.sku = productSkuDTO;
            this.vendorProductSkuId = productSku.getVendorProductSkuId();
        }

        if (shop != null) {
            this.shop = BeanUtil.copyProperties(shop, ShopBaseVO.class);
        }

        this.productPrice = productPrice;
        this.price = productPrice;
        this.originPrice = productPrice;
        this.stock = stock;
        this.extraSkuData = StrUtil.isNotBlank(cart.getExtraSkuData()) ? JSONUtil.parseArray(cart.getExtraSkuData()).toList(CarExtraSkuDataDTO.class) : null;
        this.isChecked = Constants.YES.equals(cart.getIsChecked());
        this.skuData = JsonUtil.fromJson(cart.getSkuData(), JSONArray.class);
        this.hasSku = this.sku != null;
        this.subtotal = productPrice.multiply(new BigDecimal(cart.getQuantity())).setScale(2, RoundingMode.HALF_UP);
        this.memberPrice = memberPrice;
    }

}
