// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.controller.product;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.*;
import com.tigshop.bean.model.promotion.PointsExchange;
import com.tigshop.bean.param.product.ProductSaveParam;
import com.tigshop.bean.query.product.CommentListPageQuery;
import com.tigshop.bean.query.user.FeedBackListPageQuery;
import com.tigshop.bean.vo.product.*;
import com.tigshop.bean.vo.promotion.CouponDetailVO;
import com.tigshop.bean.vo.user.UserFeedbackVO;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.service.product.CommentService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.promotion.PointsExchangeService;
import com.tigshop.service.user.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 商品
 *
 * @author Tigshop团队
 * @create 2025年02月12日 14:59
 */
@RestController
@RequestMapping("/api/product/product")
@Tag(name = "商品")
public class ProductController {
    @Resource
    private ProductService productService;

    @Resource
    private PointsExchangeService pointsExchangeService;

    @Resource
    private CommentService productCommentService;

    @Resource
    private FeedbackService feedbackService;

    @GetMapping("/detail")
    @Operation(summary = "商品详情")
    public ProductDetailVO detail(@RequestParam("sn") String productSn,
                                  @RequestParam(value = "shopId", required = false) Integer shopId,
                                  @RequestParam(value = "nearestShopId", required = false) Integer nearestShopId) {
        return productService.clientDetail(productSn, shopId, nearestShopId);
    }

    @GetMapping(value = "/detail", params = {"id", "!sn"})
    @Operation(summary = "商品详情")
    public ProductDetailVO detailById(@RequestParam("id") Integer productId,
                                      @RequestParam(value = "shopId", required = false) Integer shopId,
                                      @RequestParam(value = "nearestShopId", required = false) Integer nearestShopId) {
        String productSn = productService.getProductSnById(productId);
        return productService.clientDetail(productSn, shopId, nearestShopId);
    }

    @GetMapping("/isCollect")
    @Operation(summary = "商品是否收藏")
    public boolean isCollectMethod(@RequestParam("id") int productId, @RequestParam(value = "shopId", required = false) Integer shopId) {
        //获取用户id
        //获取用户id
        String username = SecurityUtils.getCurrentUsername();
        if (ObjectUtil.isEmpty(username)) {
            return false;
        }
        return productService.isCollect(productId, shopId);
    }

    @GetMapping("/getCoupon")
    @Operation(summary = "获取商品优惠券")
    public List<CouponDetailVO> getCoupon(@RequestParam("id") int productId) {
        return productService.getCouponList(productId);
    }

    @GetMapping("/getComment")
    @Operation(summary = "获取商品评论")
    public ProductCommentStatisticVO getComment(@RequestParam("id") Integer productId, @RequestParam(value = "shopId", required = false) Integer shopId) {
        return productCommentService.getProductCommentDetail(productId, shopId);
    }

    @GetMapping("/getCommentList")
    @Operation(summary = "获取商品评论列表")
    public Page<CommentVO> getCommentList(@RequestParam("id") Integer productId,
                                          @RequestParam("page") Integer page,
                                          @RequestParam("type") Integer type,
                                          @RequestParam(value = "shopId", required = false) Integer shopId) {
        CommentListPageQuery pageQuery = new CommentListPageQuery();
        pageQuery.setPage(page);
        pageQuery.setSize(10);
        pageQuery.setProductId(productId);
        pageQuery.setShopId(shopId);
        if (type == 5) {
            pageQuery.setIsShowed(1);
        }
        pageQuery.setType(type);
        pageQuery.setSortField("isTop");
        pageQuery.setSortOrder("desc");
        return productCommentService.list(pageQuery);
    }

    @GetMapping("/getFeedbackList")
    @Operation(summary = "获取商品反馈列表")
    public Page<UserFeedbackVO> getFeedbackList(@RequestParam("productId") Integer productId,
                                                @RequestParam("page") Integer page,
                                                @RequestParam("size") Integer size,
                                                @RequestParam(value = "sortField", required = false) String sortField,
                                                @RequestParam(value = "sortOrder", required = false) String sortOrder) {
        FeedBackListPageQuery feedBackListPageQuery = new FeedBackListPageQuery();
        feedBackListPageQuery.setPage(page);
        feedBackListPageQuery.setSize(size);
        feedBackListPageQuery.setProductId(productId);
        feedBackListPageQuery.setSortField(sortField);
        feedBackListPageQuery.setSortOrder(sortOrder);
        return feedbackService.list(feedBackListPageQuery);
    }

    @GetMapping("/getProductAvailability")
    @Operation(summary = "商品可用活动与数据")
    public ProductAvailabilityVO getProductAvailability(@RequestParam("id") Integer productId,
                                                        @RequestParam(name = "skuId", required = false) Integer skuId,
                                                        @RequestParam(name = "isExchange", required = false) Integer isExchange,
                                                        @RequestParam(name = "extraAttrIds", required = false) String extraAttrIds,
                                                        @RequestParam(value = "shopId", required = false) Integer shopId) {
        if (isExchange == null) {
            isExchange = 0;
        }
        ProductAvailabilityVO productAvailabilityVo = productService.getProductSkuDetail(productId, skuId, isExchange, extraAttrIds, shopId);
        if (isExchange == 1) {
            PointsExchange infoByProductId = pointsExchangeService.getInfoByProductId(productAvailabilityVo.getProductId(), skuId);
            if (infoByProductId != null && infoByProductId.getPointsDeductedAmount().compareTo(BigDecimal.ZERO) > 0) {
                productAvailabilityVo.setPrice(TigUtils.toDecimalConvert(infoByProductId.getPointsDeductedAmount()));
            }
        }
        return productAvailabilityVo;
    }

    @PostMapping("/promotion")
    @Operation(summary = "商品活动")
    public Map<Integer, ProductSaveParam> getProductsPromotion(@RequestBody ProductsPromotionDTO productsPromotion) {
        //todo
        return productService.getProductsPromotion(productsPromotion.getProducts(), productsPromotion.getShopId(), productsPromotion.getPromotionFrom());
    }

    @PostMapping("/getProductAmount")
    @Operation(summary = "获取商品数量")
    public ProductAmountVO getProductAmount(@RequestBody ProductAmountDTO productAmountDTO) {
        Integer count = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (ProductAmountDTO.SkuItem skuItem : productAmountDTO.getSkuItem()) {
            ProductAvailabilityVO productSkuDetail = productService.getProductSkuDetail(productAmountDTO.getId(), skuItem.getSkuId(), 0, null, productAmountDTO.getShopId());
            count += skuItem.getNum();
            total = total.add(productSkuDetail.getPrice().multiply(new BigDecimal(skuItem.getNum())));
        }
        return ProductAmountVO.builder()
                .count(count)
                .total(total)
                .build();
    }

    @PostMapping("/priceInquiry")
    @Operation(summary = "商品询价")
    public void priceInquiry(@RequestBody ProductPriceInquiryDTO productPriceInquiryDTO) {
        productService.priceInquiry(productPriceInquiryDTO);
    }

    @GetMapping("/list")
    @Operation(summary = "商品列表")
    public Page<ProductListResDTO> list(ProductListDTO productListDTO) {
        productListDTO.setIsDelete(0);
        productListDTO.setIsClient(1);
        productListDTO.setProductStatus(1);
        return productService.clientList(productListDTO);
    }

    @GetMapping("/getRelated")
    @Operation(summary = "获取相关商品")
    public List<ProductListResDTO> getRelated(@RequestParam("productId") Integer productId) {
        Page<ProductListResDTO> relatedList = productService.getRelatedProductList(productId);
        return relatedList != null ? relatedList.getRecords() : new ArrayList<>();
    }

    @GetMapping("/getBatchProductAvailability")
    @Operation(summary = "批量获取商品可用活动与数据")
    public Map<Integer, ProductAvailabilityVO> getBatchProductAvailability(@RequestParam("skuIds") String skuIds, @RequestParam(name = "shopId", required = false) Integer shopId) {
        return productService.getBatchProductAvailability(skuIds, shopId);
    }
}