// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.product;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.core.BigDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品列表返回参数
 *
 * @author Jayce
 * @create 2024年11月20日 14:50
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResDTO {
    @Schema(description = "商品分类ID")
    private Integer categoryId;

    @Schema(description = "品牌ID")
    private Integer brandId;

    @Schema(description = "产品TSN")
    private String productTsn;

    @Schema(description = "市场价（划线价）")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal marketPrice;

    @Schema(description = "运费模板ID")
    private Long shippingTplId;

    @Schema(description = "是否包邮")
    private Integer freeShipping;

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "图片URL")
    @JsonTranslate(dataType = 7)
    private String picUrl;

    @Schema(description = "缩略图URL")
    private String picThumb;

    @Schema(description = "商品简介")
    private String productBrief;

    @Schema(description = "产品名称")
    @JsonTranslate
    private String productName;

    @Schema(description = "审核状态")
    private Integer checkStatus;

    @Schema(description = "审核原因")
    private String checkReason;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "供应商ID")
    private Integer suppliersId;

    @Schema(description = "产品类型")
    private Integer productType;

    @Schema(description = "产品SN")
    private String productSn;

    @Schema(description = "产品价格")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal productPrice;

    @Schema(description = "产品状态")
    private Integer productStatus;

    @Schema(description = "是否推荐")
    private Integer isBest;

    @Schema(description = "是否新品")
    private Integer isNew;

    @Schema(description = "是否热销")
    private Integer isHot;

    @Schema(description = "库存数量")
    private Integer productStock;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "产品SKU列表")
    private List<ProductSkuDTO> productSku;

    @Schema(description = "店铺信息")
    private Shop shop;

    @Schema(description = "最终价")
    private BigDecimal price;

    @Schema(description = "供应商产品ID")
    private Integer vendorProductId;

    @Schema(description = "原始商品价格")
    private String orgProductPrice;

    public ProductListResDTO(Product product, List<ProductSku> productSkus) {
        BeanUtil.copyProperties(product, this);
        this.productSku = productSkus.stream().map(ProductSkuDTO::new).toList();
    }
}
