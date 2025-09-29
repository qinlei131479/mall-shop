// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldProductDTO;
import com.tigshop.bean.dto.order.CategoryParentTreeDTO;
import com.tigshop.bean.dto.product.*;
import com.tigshop.bean.model.product.Brand;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.setting.GalleryPic;
import com.tigshop.bean.param.o2o.AllocationParam;
import com.tigshop.bean.param.product.ProductEditParam;
import com.tigshop.bean.param.product.ProductSaveParam;
import com.tigshop.bean.param.product.VendorProductImportParam;
import com.tigshop.bean.query.salesman.SalesmanProductPageQuery;
import com.tigshop.bean.vo.content.ArticleVO;
import com.tigshop.bean.vo.o2o.DeliveryOptionVO;
import com.tigshop.bean.vo.product.*;
import com.tigshop.bean.vo.promotion.CouponDetailVO;
import com.tigshop.bean.vo.salesman.SalesmanProductListVO;
import com.tigshop.service.common.BaseService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品服务接口
 *
 * @author Tigshop团队
 * @create 2024年11月20日 11:09
 */
public interface ProductService extends BaseService<Product> {
    /**
     * 商品列表
     *
     * @param product 查询条件
     * @return ProductListVO
     */
    Page<ProductListResDTO> list(ProductListDTO product);


    /**
     * 商品列表
     *
     * @param product 查询条件
     * @return ProductListVO
     */
    Page<ProductListResDTO> clientList(ProductListDTO product);

    /**
     * 待审核商品数量
     * @return long
     */
    Long getWaitingCheckedCount();

    /**
     * 商品详情
     *
     * @param productId 商品ID
     * @return ItemVO<ProductDetailDTO>
     */
    ProductDetailDTO detail(Integer productId);

    /**
     * 商品配置
     */
    ProductConfigVO config(Integer shopId);

    /**
     * 新增商品
     *
     */
    String create(ProductSaveParam param);

    /**
     * 更新商品
     */
    boolean update(ProductEditParam param);

    void otherUpdates(ProductSaveParam param, String productSn, Product product, List<GalleryPic> imgList);

    /**
     * 根据商品ID查询商品
     *
     * @param productIds 商品ID
     * @return List<Product>
     */
    List<Product> getProductByIds(List<Integer> productIds);

    /**
     * 根据店铺ID初始化商品
     *
     * @param shopId 店铺id
     */
    void initProductByShopId(Integer shopId);

    /**
     * 获取店铺运费模板
     *
     * @return ShippingTplListVO
     */
    List<ProductShippingTplListVO> getShippingTplListByShopId();

    /**
     * 获取商品库存
     *
     * @param productId       商品ID
     * @param skuId           skuID
     * @param includeActivity 是否包含活动
     * @return Integer
     */
    Integer getProductStock(Integer productId, Integer skuId, boolean includeActivity);

    /**
     * 获取商品最终价格
     *
     * @param productId     商品ID
     * @param originalPrice 原价
     * @param skuId         skuID
     * @return BigDecimal
     */
    BigDecimal getProductFinalPrice(Integer productId, BigDecimal originalPrice, Integer skuId);

    /**
     * 获取商品列表
     */
    List<ProductListResDTO> getProductList(ProductListDTO dto);

    void decStock(Integer productId, Integer quantity);

    void incStock(Integer productId, Integer quantity);

    void incSales(Integer productId, Integer quantity);

    /**
     * 获取商品ID列表
     * @return String
     */
    String getProductIds();

    /**
     * 看了还看
     *
     * @param dto 相关信息
     * @return List<Category>
     */
    List<ProductLookAlsoVO> getLookAlso(ProductRelateDTO dto);

    /**
     * 获取商品相似信息（最终价）
     *
     * @param productList 商品列表
     * @return List<ProductLookAlsoVO>
     */
    List<ProductLookAlsoVO> getProductRelate(List<Product> productList, Integer shopId);

    /**
     * 相关排行
     *
     * @param dto 相关信息
     * @return Map<String, Object>
     */
    RelateRankVO getRelateRank(ProductRelateDTO dto);

    /**
     * 相关文章
     *
     * @param dto 相关信息
     * @return List<ArticleVO>
     */
    List<ArticleVO> getArticleList(ProductRelateDTO dto);

    /**
     * 相关品牌
     */
    List<Brand> getRelateBrand(ProductRelateDTO dto);

    /**
     * 相关分类
     *
     * @param dto 相关信息
     * @return List<Category>
     */
    List<Category> getRelateCategory(ProductRelateDTO dto);

    /**
     * 获取商品详情页
     *
     * @param sn 商品编号
     * @return ProductDetailVO
     */
    ProductDetailVO clientDetail(String sn, Integer shopId, Integer nearestShopId);

    /**
     * 获取浏览商品
     */
    List<ProductListResDTO> historyProduct(List<Integer> productIds);

    /**
     * 获取当前分类的父级分类
     *
     * @param categoryId 商品编号
     * @return List<CategoryParentTreeDTO>
     */
    List<CategoryParentTreeDTO> parentTree(Integer categoryId);

    Integer isBuy(int id);

    /**
     * 获取商品是否收藏
     *
     * @param productId 商品id
     * @return ProductDetailVO
     */
    Boolean isCollect(int productId, Integer shopId);

    /**
     * 获取商品优惠券列表
     *
     * @param productId 商品ID
     * @return List<CouponDetailVO>
     */
    List<CouponDetailVO> getCouponList(Integer productId);

    /**
     * 根据筛选条件查询所有商品的分类列表
     *
     * @param productListDTO 查询条件
     * @return ProductListVO
     */
    List<Category> getProductCategoryList(ProductListDTO productListDTO);

    /**
     * 根据筛选条件查询所有商品的品牌列表
     *
     * @param productListDTO 查询条件
     * @return ProductListVO
     */
    List<Brand> getProductBrandList(ProductListDTO productListDTO);

    /**
     * 根据筛选条件查询所有商品的最高价格
     *
     * @param productListDTO 查询条件
     * @return BigDecimal
     */
    BigDecimal getMaxPrice(ProductListDTO productListDTO);

    /**
     * 获取商品SKU详情
     */
    ProductAvailabilityVO getProductSkuDetail(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId);

    /**
     * 获取商品SKU详情
     */
    ProductAvailabilityVO getProductFinalPrice(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId);

    /**
     * 获取商品活动与数据
     *
     * @param products      products
     * @param shopId        shopId
     * @param promotionFrom 来源
     * @return Map<Integer, ProductDTO>
     */
    Map<Integer, ProductSaveParam> getProductsPromotion(List<ProductSaveParam> products, Integer shopId, String promotionFrom);

    /**
     * 获取相关商品
     *
     * @param productId 商品ID
     * @return List<Product>
     */
    List<Product> getRelatedList(Integer productId);

    /**
     * 获取商品询价
     *
     * @param productPriceInquiryDTO productPriceInquiryDTO
     */
    void priceInquiry(ProductPriceInquiryDTO productPriceInquiryDTO);

    /**
     * 获取商品SKU详情
     *
     * @param productId 商品ID
     */
    Page<ProductListResDTO> getRelatedProductList(Integer productId);

    /**
     * 获取商品Sn
     *
     * @param productId 商品ID
     */
    String getProductSnById(Integer productId);

    /**
     * 更新关键词
     * @param dto 参数
     * @return boolean
     */
    String getParticiple(ProductKeyDTO dto);

    /**
     * 更新字段
     * @param updateField updateField
     */
    String updateFieldProduct(UpdateFieldProductDTO updateField, String[] allowFields);

    /**
     * 批量操作
     */
    void batchOperation(ProductBatchOperationDTO dto);

    /**
     * 产品回收
     */
    boolean recycle(ProductRecycleDTO dto);

    /**
     * 产品复制
     */
    String copy(ProductCopyDTO dto);

    /**
     * 产品审核
     */
    boolean audit(ProductAuditDTO dto);

    String createNewProductSn();

    boolean auditAgain(Integer id);

    /**
     * 批量获取最终价库存信息
     * @param skuIds 字符串ids，需要拆分
     * @return map
     */
    Map<Integer, ProductAvailabilityVO> getBatchProductAvailability(String skuIds, Integer shopId);

    /**
     * 分销商品列表
     */
    Page<SalesmanProductListVO> salesmanProducts(SalesmanProductPageQuery pageQuery);

    /**
     * 根据供应商ID下架相关产品
     *
     * @param vendorId 供应商ID
     */
    void discontinueByVendorId(Integer vendorId);

    /**
     * 根据供应商产品ID下架相关产品
     *
     * @param vendorProductId 供应商产品ID
     */
    void discontinueByVendorProductId(Integer vendorProductId);

    /**
     * 根据供应商产品ID列表下架相关产品
     *
     * @param vendorProductIds 供应商产品ID列表
     */
    void discontinueByVendorProductIds(List<Integer> vendorProductIds);

    /**
     * 供应商商品导入
     */
    void vendorProductImport(VendorProductImportParam param);

    /**
     * 获取供应商最大价格
     */
    BigDecimal getVendorMaxPrice(Integer vendorProductSkuId);

    /**
     * 获取门店商品的配送方式
     */
    DeliveryOptionVO getDeliveryOption(Integer shopId);

    /**
     * 全部分配/区域分配/选择门店分配
     * @param param  param
     */
    void allocationProduct(AllocationParam param);
}
