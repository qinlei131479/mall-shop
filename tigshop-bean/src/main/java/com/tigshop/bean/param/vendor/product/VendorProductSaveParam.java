package com.tigshop.bean.param.vendor.product;

import com.tigshop.bean.dto.product.ProductDescDTO;
import com.tigshop.bean.enums.vendor.VendorProductAuditStateEnum;
import com.tigshop.bean.model.vendor.product.*;
import com.tigshop.common.annotation.ConditionalNotEmpty;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.utils.HeaderUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 供应商商品新增参数
 *
 * @author kidd
 * @since 2025/7/10 08:53
 */
@ConditionalNotEmpty(field = "skuAttrs", conditionField = "skuType", conditionValue = "2", message = "多规格商品时，规格属性不能为空")
@Data
public class VendorProductSaveParam {

    // *** VendorProduct ***

    @NotBlank(message = "商品名称不能为空")
    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品品牌ID")
    private Integer brandId;

    @NotNull(message = "商品类目ID不能为空")
    @Schema(description = "商品类目ID")
    private Integer productCategoryId;

    @NotNull(message = "商品编码生成类型能为空")
    @Schema(description = "商品编码生成类型；1-系统生成，2-手动输入")
    private Integer productSnGenerateType;

    @Schema(description = "商品描述")
    private String productBrief;

    @NotNull(message = "商品状态不能为空")
    @Schema(description = "商品状态；0-断供，1-在售")
    private Integer productState;

    @NotNull(message = "规格类型不能为空")
    @Schema(description = "规格类型，1-单规格，2-多规格")
    private Integer skuType;

    // *** Other ***

    @Schema(description = "商品详情")
    private List<ProductDescDTO> productDesc;

    @Valid
    @NotEmpty(message = "商品相册不能为空")
    @Schema(description = "商品相册")
    private List<VendorProductGalleryParam> galleries;

    @Valid
    @Schema(description = "商品视频")
    private List<VendorProductVideoParam> video;

    @Valid
    @NotEmpty(message = "商品规格不能为空")
    @Schema(description = "商品规格")
    private List<VendorProductSkuParam> skus;

    @Valid
    @Schema(description = "商品规格属性")
    private List<VendorProductSkuAttrParam> skuAttrs;

    public VendorProduct createVendorProduct(String size400) {

        String productDesc = ProductDescDTO.getProductDesc(this.productDesc);

        VendorProduct vendorProduct = VendorProduct.builder()
                .productName(this.productName)
                .productBrandId(this.brandId)
                .productCategoryId(this.productCategoryId)
                .productSnGenerateType(this.productSnGenerateType)
                .productBrief(this.productBrief)
                .productState(this.productState)
                .skuType(this.skuType)
                .productDesc(productDesc)
                .auditState(VendorProductAuditStateEnum.WAIT_AUDIT.getCode())
                .isRecycle(Constants.NO)
                .vendorId(HeaderUtils.getVendorId())
                .build();

        VendorProductGalleryParam gallery = galleries.getFirst();
        if (gallery.getId() == null) {
            vendorProduct.setPicUrl(gallery.getPicUrl() + size400);
            vendorProduct.setPicOriginal(gallery.getPicUrl());
            vendorProduct.setPicThumb(gallery.getPicThumb());
        } else {
            vendorProduct.setPicUrl(gallery.getPicUrl());
            vendorProduct.setPicOriginal(gallery.getPicOriginal());
            vendorProduct.setPicThumb(gallery.getPicThumb());
        }

        return vendorProduct;
    }

    public List<VendorProductGallery> createVendorProductGalleries(Long vendorProductId) {
        return IntStream.range(0, galleries.size())
                .mapToObj(i -> {
                    VendorProductGalleryParam gallery = galleries.get(i);
                    return gallery.createVendorProductGallery(vendorProductId, i);
                })
                .filter(gallery -> gallery.getId() == null)
                .toList();
    }

    public VendorProductVideo createVendorProductVideo(Long vendorProductId) {
        VendorProductVideoParam video = this.video.getFirst();
        return video.createVendorProductVideo(vendorProductId);
    }

    public List<VendorProductSkuAttr> createVendorProductSkuAttrs(Long vendorProductId) {
        return this.skuAttrs.stream()
                .flatMap(skuAttr -> skuAttr.createVendorProductSkuAttr(vendorProductId).stream())
                .collect(Collectors.toList());
    }

    public List<VendorProductSku> createVendorProductSkus(Long vendorProductId) {
        return this.skus.stream()
                .map(skuParam -> skuParam.createVendorProductSku(vendorProductId))
                .collect(Collectors.toList());
    }

}
