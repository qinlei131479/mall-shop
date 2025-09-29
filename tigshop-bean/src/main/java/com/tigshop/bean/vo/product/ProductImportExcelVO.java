package com.tigshop.bean.vo.product;

import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import com.tigshop.bean.model.product.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品导入 excel
 *
 * @author kidd
 * @since 2025/3/26 16:25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(30)
public class ProductImportExcelVO {

    @ExcelProperty("商品名称（必须）")
    @Schema(description = "商品名称")
    private String productName;

    @ExcelProperty("商品编号（为空则自动生成）")
    @Schema(description = "商品编号")
    private String productSn;

    @ExcelProperty("分类（必须）")
    @Schema(description = "分类")
    private String categoryName;

    @ExcelProperty("商品售价")
    @Schema(description = "商品价格")
    private String productPrice;

    @ExcelProperty("市场价（为空则自动生成）")
    @Schema(description = "市场价（划线价）")
    private String marketPrice;

    @Schema(description = "成本价")
    private String costPrice;

    @ExcelProperty("是否上架（默认1）")
    @Schema(description = "商品状态")
    private String productStatus;

    @ExcelProperty("品牌")
    @Schema(description = "品牌")
    private String brandName;

    @ExcelProperty("商品相册")
    @Schema(description = "图片URL")
    private String picUrl;

    @ExcelProperty("关键词")
    @Schema(description = "关键词")
    private String keywords;

    @ExcelProperty("商品描述")
    @Schema(description = "商品描述")
    private String productBrief;

    @ExcelProperty("详细描述")
    @Schema(description = "详细描述")
    private String productDesc;

    @ExcelProperty("重量（kg）")
    @Schema(description = "商品重量")
    private String productWeight;

    @ExcelProperty("库存（默认0）")
    @Schema(description = "商品库存")
    private String productStock;

    @ExcelIgnore
    @Schema(description = "分类ID")
    private Integer categoryId;

    @ExcelIgnore
    @Schema(description = "品牌ID")
    private Integer brandId;

    @ExcelIgnore
    @Schema(description = "店铺ID")
    private Integer shopId;

    public ProductImportExcelVO(Product product, String categoryName) {
        this.productName = product.getProductName();
        this.productSn = product.getProductSn();
        this.categoryName = categoryName;
        this.productPrice = product.getProductPrice().toString();
        this.marketPrice = product.getMarketPrice().toString();
        this.productStatus = product.getProductStatus().toString();
        this.brandName = product.getBrandId().toString();
        this.picUrl = product.getPicUrl();
        this.keywords = product.getKeywords();
        this.productBrief = product.getProductBrief();
        this.productDesc = product.getProductDesc();
        this.productWeight = product.getProductWeight().toString();
        this.productStock = product.getProductStock().toString();
        this.shopId = product.getShopId();
    }
}
