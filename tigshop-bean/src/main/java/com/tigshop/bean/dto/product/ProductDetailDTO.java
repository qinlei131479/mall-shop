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

import cn.hutool.json.JSONArray;
import com.tigshop.bean.model.o2o.ShopPickupTpl;
import com.tigshop.bean.model.product.ProductGallery;
import com.tigshop.bean.model.product.ProductVideo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情参数
 *
 * @author Tigshop团队
 * @create 2024年11月26日 16:38
 */
@Data
@Schema(description = "商品详情参数")
public class ProductDetailDTO {
    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品货号")
    private String productSn;

    @Schema(description = "商品条形码")
    private String productTsn;

    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "商品市场价")
    private BigDecimal marketPrice;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "商品销量")
    private Long shippingTplId;

    @Schema(description = "商品状态")
    private Integer productStatus;

    @Schema(description = "商品类型")
    private Integer productType;

    @Schema(description = "商品分类ID")
    private Integer categoryId;

    @Schema(description = "商品品牌ID")
    private Integer brandId;

    @Schema(description = "商品店铺ID")
    private Integer shopId;

    @Schema(description = "商品关键词")
    private String keywords;

    @Schema(description = "商品店铺分类ID")
    private Integer shopCategoryId;

    @Schema(description = "商品审核状态")
    private Integer checkStatus;

    @Schema(description = "商品审核原因")
    private String checkReason;

    @Schema(description = "商品点击量")
    private Integer clickCount;

    @Schema(description = "商品重量")
    private BigDecimal productWeight;

    @Schema(description = "商品是否促销")
    private Integer isPromote;

    @Schema(description = "商品是否活动")
    private Integer isPromoteActivity;

    @Schema(description = "商品促销价格")
    private BigDecimal promotePrice;

    @Schema(description = "商品促销开始时间")
    private String promoteStartDate;

    @Schema(description = "商品促销结束时间")
    private String promoteEndDate;

    @Schema(description = "商品秒杀商品数量")
    private Integer seckillMaxNum;

    @Schema(description = "商品秒杀库存")
    private String productBrief;

    @Schema(description = "商品描述")
    private String productDesc;

    @Schema(description = "商品图片")
    private String picUrl;

    @Schema(description = "商品缩略图")
    private String picThumb;

    @Schema(description = "商品原图")
    private String picOriginal;

    @Schema(description = "商品服务")
    private List<Integer> productServiceIds;

    @Schema(description = "商品是否包邮")
    private Integer freeShipping;

    @Schema(description = "商品积分")
    private Integer integral;

    @Schema(description = "商品添加时间")
    private long addTime;

    @Schema(description = "商品排序")
    private Integer sortOrder;

    @Schema(description = "商品店铺排序")
    private Integer storeSortOrder;

    @Schema(description = "商品是否删除")
    private Integer isDelete;

    @Schema(description = "商品是否精品")
    private Integer isBest;

    @Schema(description = "商品是否新品")
    private Integer isNew;

    @Schema(description = "商品是否热销")
    private Integer isHot;

    @Schema(description = "商品最后更新时间")
    private Integer lastUpdate;

    @Schema(description = "商品备注")
    private String remark;

    @Schema(description = "商品赠送积分")
    private Integer giveIntegral;

    @Schema(description = "商品评价积分")
    private Integer rankIntegral;

    @Schema(description = "商品供应商ID")
    private Integer suppliersId;

    @Schema(description = "商品虚拟销量")
    private Integer virtualSales;

    @Schema(description = "商品限购数量")
    private Integer limitNumber;

    @Schema(description = "商品收藏数")
    private String productCare;

    @Schema(description = "商品关联商品")
    private List<Integer> productRelated;

    @Schema(description = "商品是否支持退货")
    private Integer isSupportReturn;

    @Schema(description = "商品是否支持货到付款")
    private Integer isSupportCod;

    @Schema(description = "商品视频")
    private String productVideo;

    @Schema(description = "商品预存款支付价格")
    private BigDecimal prepayPrice;

    @Schema(description = "商品描述")
    private List<ProductDescDTO> productDescArr;

    @Schema(description = "商品相册")
    private List<ProductGallery> imgList;

    @Schema(description = "视频信息")
    private List<ProductVideo> productVideoInfo;

    @Schema(description = "商品文章")
    private List<Integer> productArticleList;

    @Schema(description = "属性模板数据")
    private ProductAttrsDTO attrList;

    @Schema(description = "商品SKU")
    private List<ProductSkuDTO> productList;

    @Schema(description = "卡密组")
    private Integer cardGroupId;

    @Schema(description = "虚拟商品")
    private String virtualSample;

    @Schema(description = "付费内容")
    private JSONArray paidContent;

    @Schema(description = "是否无需配送；0-否，1-是")
    private Integer noShipping;

    @Schema(description = "运费类型")
    private Integer fixedShippingType;

    @Schema(description = "固定运费金额")
    private BigDecimal fixedShippingFee;

    @Schema(description = "是否到店自提；0-否，1-是")
    private Integer isShopPickup;

    @Schema(description = "到店自提模板")
    private ShopPickupTpl shopPickupTpl;

    @Schema(description = "供应商产品ID")
    private Integer vendorProductId;

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Schema(description = "是否物流配送0-否，1-是")
    private Integer isLogistics;

    @Schema(description = "是否门店配送0-否，1-是")
    private Integer isShopDelivery;
}
