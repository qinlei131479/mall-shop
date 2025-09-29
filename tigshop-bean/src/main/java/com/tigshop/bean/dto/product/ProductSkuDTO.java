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

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.TableField;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品SKU数据参数
 *
 * @author Jayce
 * @create 2024年11月21日 11:26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSkuDTO {
    @Schema(description = "规格ID")
    private Integer skuId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品规格值")
    private String skuValue;

    @Schema(description = "商品规格值（JSON）")
    private List<SkuData> skuData;

    @Schema(description = "货品编码")
    private String skuSn;

    @Schema(description = "货品库存")
    private Integer skuStock;

    @Schema(description = "条形码")
    private String skuTsn;

    @Schema(description = "货品价格")
    private String skuPrice;

    @TableField("market_price")
    @Schema(description = "市场价（划线价）")
    private BigDecimal marketPrice;

    @TableField("cost_price")
    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "供应商产品skuID")
    private Integer vendorProductSkuId;

    // *** Other ***

    @Schema(description = "供货价")
    private BigDecimal supplyPrice;

    @Data
    public static class SkuData {

        @Schema(description = "规格值名称")
        @JsonTranslate(dataType = 8)
        private String name;

        @Schema(description = "规格值名称")
        @JsonTranslate(dataType = 8)
        private String value;

        public SkuData(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    public ProductSkuDTO(ProductSku productSku) {
        this.skuId = productSku.getSkuId();
        this.productId = productSku.getProductId();
        this.skuValue = productSku.getSkuValue();
        if (StrUtil.isNotBlank(productSku.getSkuData())) {
            this.skuData = JSON.parseArray(productSku.getSkuData(), SkuData.class);
        }
        this.skuSn = productSku.getSkuSn();
        this.skuStock = productSku.getSkuStock();
        this.skuTsn = productSku.getSkuTsn();
        this.skuPrice = productSku.getSkuPrice().toString();
    }
}
