// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.product;

import com.tigshop.bean.dto.product.ProductAttrsDTO;
import com.tigshop.bean.dto.product.ProductDescDTO;
import com.tigshop.bean.dto.product.ProductInfoDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.dto.promotion.SeckillItemInfoDTO;
import com.tigshop.bean.model.product.ProductGallery;
import com.tigshop.bean.model.product.ProductVideo;
import com.tigshop.bean.model.product.Services;
import com.tigshop.bean.vo.promotion.PointsExchangeVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 前台商品详情
 *
 * @author Tigshop团队
 * @create 2025年02月12日 13:43
 */
@Setter
@Getter
public class ProductDetailVO {
    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "商品详情")
    private ProductInfoDTO item;

    @Schema(description = "商品图文详情")
    private List<ProductDescDTO> descArr;

    @Schema(description = "sku列表")
    private List<ProductSkuDTO> skuList;

    @Schema(description = "相册列表")
    private List<ProductGallery> picList;

    @Schema(description = "视频信息")
    private List<ProductVideo> videoList;

    @Schema(description = "商品属性列表")
    private ProductAttrsDTO attrList;

    @Schema(description = "商品评论评分详情")
    private ProductCommentStatisticVO rankDetail;

    @Schema(description = "秒杀活动详情")
    private List<SeckillItemInfoDTO> seckillDetail;

    @Schema(description = "服务列表")
    private List<Services> serviceList;

    @Schema(description = "默认选择的属性")
    private List<String> checkedValue;

    @Schema(description = "商品咨询总数")
    private Integer consultationTotal;

    @Schema(description = "积分商品信息")
    private PointsExchangeVO exchangeInfo;
}