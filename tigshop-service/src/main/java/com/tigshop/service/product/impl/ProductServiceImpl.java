// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.product.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.EscapeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.bo.vendort.VendorProductBO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.common.UpdateFieldProductDTO;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.dto.order.CategoryParentTreeDTO;
import com.tigshop.bean.dto.product.*;
import com.tigshop.bean.dto.promotion.PromotionTypeDTO;
import com.tigshop.bean.dto.promotion.SeckillItemInfoDTO;
import com.tigshop.bean.dto.promotion.TimeDiscountDTO;
import com.tigshop.bean.dto.user.UserCouponListDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.o2o.ShopDeliveryTypeEnum;
import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import com.tigshop.bean.enums.product.*;
import com.tigshop.bean.enums.promotion.SendRangeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.authority.Suppliers;
import com.tigshop.bean.model.cart.Cart;
import com.tigshop.bean.model.msg.AdminMsg;
import com.tigshop.bean.model.o2o.AreaStoreManagerShop;
import com.tigshop.bean.model.o2o.ShopPickupTpl;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.o2o.StoreProductSku;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.product.*;
import com.tigshop.bean.model.promotion.*;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.setting.GalleryPic;
import com.tigshop.bean.model.setting.ShippingTpl;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.shop.ShopProductCategory;
import com.tigshop.bean.model.user.CollectProduct;
import com.tigshop.bean.model.user.Feedback;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.model.vendor.VendorShopBind;
import com.tigshop.bean.model.vendor.product.VendorProduct;
import com.tigshop.bean.model.vendor.product.VendorProductSku;
import com.tigshop.bean.param.o2o.AllocationParam;
import com.tigshop.bean.param.product.ProductEditParam;
import com.tigshop.bean.param.product.ProductSaveParam;
import com.tigshop.bean.param.product.VendorProductImportParam;
import com.tigshop.bean.query.content.ArticlePageQuery;
import com.tigshop.bean.query.salesman.SalesmanProductPageQuery;
import com.tigshop.bean.vo.config.GetStoreVO;
import com.tigshop.bean.vo.content.ArticleVO;
import com.tigshop.bean.vo.o2o.DeliveryOptionVO;
import com.tigshop.bean.vo.product.*;
import com.tigshop.bean.vo.promotion.CouponDetailVO;
import com.tigshop.bean.vo.promotion.CouponVO;
import com.tigshop.bean.vo.promotion.ProductGiftVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.bean.vo.salesman.SalesmanProductListVO;
import com.tigshop.bean.vo.user.UserCouponVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.*;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.cart.CartMapper;
import com.tigshop.mapper.o2o.AreaStoreManagerShopMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.product.PriceInquiryMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.promotion.*;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.user.CollectProductMapper;
import com.tigshop.mapper.user.FeedbackMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.vendor.VendorShopBindMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.authority.SuppliersService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.content.ArticleService;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.o2o.ShopPickupTplService;
import com.tigshop.service.o2o.StoreProductService;
import com.tigshop.service.o2o.StoreSkuService;
import com.tigshop.service.product.*;
import com.tigshop.service.promotion.*;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.GalleryPicService;
import com.tigshop.service.setting.ShippingTplService;
import com.tigshop.service.shop.ShopProductCategoryService;
import com.tigshop.service.user.UserCouponService;
import com.tigshop.service.user.UserRankService;
import com.tigshop.service.vendor.VendorProductService;
import com.tigshop.service.vendor.VendorProductSkuService;
import com.tigshop.utils.WrapperConditionUtils;
import com.tigshop.service.product.ProductEsSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.tigshop.bean.dto.product.ProductDescDTO.getProductDescArr;
import static com.tigshop.bean.enums.order.PaymentStatus.PAID;
import static com.tigshop.bean.enums.product.DeleteType.NOT_DELETE;
import static com.tigshop.bean.enums.product.IntroType.*;
import static com.tigshop.bean.enums.product.ProductStatus.ON_SALE;
import static com.tigshop.bean.enums.product.ProductType.PRODUCT_TYPE_CARD;
import static com.tigshop.bean.enums.promotion.PromotionRangeEnum.PROMOTION_RANGE_EXCLUDE_PRODUCT;
import static com.tigshop.bean.enums.promotion.PromotionRangeEnum.PROMOTION_RANGE_PRODUCT;
import static com.tigshop.bean.enums.setting.SettingsEnum.STORE_INDEPENDENT_GOODS;
import static com.tigshop.bean.enums.setting.SettingsEnum.STORE_POST_ALLOCATION_STATUS;
import static com.tigshop.common.constant.Constants.NO;
import static com.tigshop.common.constant.ExceptionConstants.SERVICE_ERROR;
import static com.tigshop.common.constant.product.ProductConstants.*;

/**
 * 商品服务接口实现
 *
 * @author Tigshop团队
 * @create 2024年11月20日 11:11
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ProductServiceImpl extends BaseServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductSkuService productSkuService;
    private final CategoryService categoryService;
    private final ProductGalleryService productGalleryService;
    private final ProductAttributesService productAttributesService;
    private final AttributesTplService attributesTplService;
    private final ShippingTplService shippingTplService;
    private final SuppliersService suppliersService;
    private final ProductPromotionService productPromotionService;
    private final ServicesService servicesService;
    private final ProductVideoService productVideoService;
    private final UserRankService userRankService;
    private final ProductArticleService productArticleService;
    private final ConfigService configService;
    private final ProductGroupService productGroupService;
    private final GalleryPicService galleryPicService;
    private final BrandService brandService;
    private final ArticleService articleService;
    private final OrderMapper orderMapper;
    private final SeckillService seckillService;
    private final SeckillItemMapper seckillItemMapper;
    private final UserMapper userMapper;
    private final CommentService commentService;
    private final FeedbackMapper feedbackMapper;
    private final CollectProductMapper collectProductMapper;
    private final CouponService couponService;
    private final UserCouponService userCouponService;
    private final PromotionService promotionService;
    private final ProductPromotionMapper productPromotionMapper;
    private final TimeDiscountMapper timeDiscountMapper;
    private final TimeDiscountItemMapper timeoutDiscountItemMapper;
    private final ProductGiftService productGiftService;
    private final TimeDiscountService timeDiscountService;
    private final PriceInquiryMapper priceInquiryMapper;
    private final AdminMsgService adminMsgService;
    private final ShopMapper shopMapper;
    private final ECardGroupService ecardGroupService;
    private final AdminLogService adminLogService;
    private final TranslatePackageImpl translatePackage;
    private final ShopProductCategoryService shopProductCategoryService;
    private final ECardService eCardService;
    private final ProductInventoryLogService productInventoryLogService;
    private final PointsExchangeMapper pointsExchangeMapper;
    private final VendorProductService vendorProductService;
    private final VendorProductSkuService vendorProductSkuService;
    private final VendorShopBindMapper vendorShopBindMapper;
    private final ShopPickupTplService shopPickupTplService;
    private final ProductEsSyncService productEsSyncService;

    private final StoreProductService storeProductService;
    private final StoreSkuService storeProductSkuService;
    private final TigshopProperties tigshopProperties;
    private final ApplicationContext applicationContext;
    private final CartMapper cartMapper;
    private final AreaStoreManagerShopMapper areaStoreManagerShopMapper;

    @Override
    public Page<ProductListResDTO> list(ProductListDTO listDTO) {
        // 分页查询
        Page<Product> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 创建查询构造器
        MPJLambdaWrapper<Product> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(Product.class);
        if (tigshopProperties.getIsO2o() == 1 && listDTO.getShopId() != null && listDTO.getShopId() > 0) {
            queryWrapper
                    .leftJoin(StoreProduct.class, "sp2", StoreProduct::getProductId, Product::getProductId)
                    .select("sp2.product_price as storeProductPrice")
                    .select("sp2.product_name as storeProductName")
                    .select("CASE WHEN sp2.product_status = 1 AND t.product_status = 1 THEN 1 ELSE 0 END AS storeProductStatus")
                    .select("sp2.product_stock as storeProductStock")
                    .select("sp2.card_group_id as storeCardGroupId")
            ;

        }
        // 排序
        if (ObjectUtil.isNotEmpty(listDTO.getIds())) {
            // ids 不为空按ids排序
            String orderSql = "FIELD(t." + getKeyColumn() + ", " + listDTO.getIds() + ")";
            queryWrapper.last("ORDER BY " + orderSql);
        } else {
            if (listDTO.getIsClient() != null && listDTO.getIsClient() == 1 && listDTO.getSortField() == null) {
                queryWrapper.orderByAsc(Product::getSortOrder);
                queryWrapper.orderByDesc(Product::getProductId);
            } else {
                buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());
            }
        }

        // 构造查询条件
        buildQueryWrapper(queryWrapper, listDTO);
        if (listDTO.getShopId() != null && listDTO.getShopId() != 0) {
            WrapperConditionUtils.copyConditionsWithOr(queryWrapper);
            queryWrapper.groupBy(Product::getProductId);
        }
        // 查询后的分页数据
        Page<Product> queryPage = this.page(page, queryWrapper);

        if (CollUtil.isEmpty(queryPage.getRecords())) {
            return PageUtil.convertPage(page, List.of());
        }

        queryPage.getRecords().forEach(product -> {
            if (tigshopProperties.getIsO2o() == 1 && listDTO.getShopId() != null && listDTO.getShopId() != 0 && product.getShopId() == 0) {
                ConfigPO storeAssignProductName = configService.getConfigByCode(SettingsEnum.STORE_ASSIGN_PRODUCT_NAME.getBizCode());
                if ("1".equals(storeAssignProductName.getBizVal())) {
                    product.setProductName(product.getStoreProductName());
                }
                ConfigPO storeAssignProductPrice = configService.getConfigByCode(SettingsEnum.STORE_ASSIGN_PRODUCT_PRICE.getBizCode());
                if ("1".equals(storeAssignProductPrice.getBizVal())) {
                    product.setProductPrice(product.getStoreProductPrice());
                }
                ConfigPO storeUseSoloProductStock = configService.getConfigByCode(SettingsEnum.STORE_USE_SOLO_PRODUCT_STOCK.getBizCode());
                if ("1".equals(storeUseSoloProductStock.getBizVal())) {
                    product.setProductStock(product.getStoreProductStock());
                }
                product.setProductStatus(product.getStoreProductStatus());
                product.setCardGroupId(product.getStoreCardGroupId());
            }
            if (product.getProductType() == PRODUCT_TYPE_CARD.getCode()) {
                product.setProductStock((int) eCardService.count(new LambdaQueryWrapper<ECard>()
                        .eq(ECard::getIsUse, NO)
                        .eq(ECard::getGroupId, product.getCardGroupId())));
            }
        });
        // 获取商品 ID 列表
        List<Integer> productIds = queryPage.getRecords().stream()
                .map(Product::getProductId)
                .toList();
        // 查询商品规格
        List<ProductSkuDTO> productSkus = productSkuService.getProductSkusByProductIds(productIds);

        // 将商品规格按商品 ID 分组
        Map<Integer, List<ProductSkuDTO>> productSkuMap = productSkus.stream()
                .collect(Collectors.groupingBy(ProductSkuDTO::getProductId));

        List<UserRank> userRankList = userRankService.getUserRank();
        User user = userMapper.selectById(SecurityUtils.getCurrentUserId());
        Integer rankId = user != null ? user.getRankId() : 0;

        // 转换为DTO
        List<ProductListResDTO> productListRes = queryPage.getRecords().stream()
                .map(product -> {
                    ProductListResDTO dto = convertToDTO(product);
                    List<ProductSkuDTO> skus = productSkuMap.get(product.getProductId());
                    dto.setProductSku(skus);
                    if (CollUtil.isNotEmpty(skus)) {
                        Integer productStock = skus.stream().map(ProductSkuDTO::getSkuStock).reduce(0, Integer::sum);
                        if (tigshopProperties.getIsO2o() == 1 && listDTO.getShopId() != null && listDTO.getShopId() != 0 && product.getShopId() == 0) {
                            // 不处理
                        } else {
                            dto.setProductStock(productStock);
                        }
                    }
                    int skuId = 0;
                    if (skus != null && !skus.isEmpty()) {
                        skuId = skus.getFirst().getSkuId();
                    }
                    dto.setPrice(this.getProductPrice(product.getProductId(), skuId, userRankList, rankId, listDTO.getShopId()));
                    return dto;
                })
                .toList();
        productListRes.forEach(product -> product.setProductName(translatePackage.translate(product.getProductName())));
        //循环获得店铺IDS
        List<Integer> shopIds = queryPage.getRecords().stream()
                .map(Product::getShopId)
                .toList();
        //如果不为空则查询shop获得shopMap
        if (!shopIds.isEmpty()) {
            List<Shop> shops = shopMapper.selectBatchIds(shopIds);
            //组装成shopId对应shop的shopMap
            if (shops != null && !shops.isEmpty()) {
                Map<Integer, Shop> shopMap = shops.stream()
                        .collect(Collectors.toMap(Shop::getShopId, Function.identity()));
                productListRes.forEach(product -> {
                    Integer productShopId = product.getShopId();
                    if (productShopId != null && productShopId > 0 && shopMap.containsKey(productShopId)) {
                        Shop shop = shopMap.get(productShopId);
                        product.setShop(shop);
                    }
                });
            }
        }
        return PageUtil.convertPage(page, productListRes);
    }

    @Override
    public Page<ProductListResDTO> clientList(ProductListDTO listDTO) {
        // 分页查询
        Page<Product> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 创建查询构造器
        MPJLambdaWrapper<Product> queryWrapper = new MPJLambdaWrapper<>();

        if (tigshopProperties.getIsO2o() == 1 && listDTO.getShopId() != null && listDTO.getShopId() > 0) {
            queryWrapper.selectAll(Product.class);
            queryWrapper
                    .leftJoin(StoreProduct.class, "sp2", StoreProduct::getProductId, Product::getProductId)
                    .select("sp2.product_price as storeProductPrice")
                    .select("sp2.product_name as storeProductName")
                    .select("sp2.product_status as storeProductStatus")
                    .select("sp2.product_stock as storeProductStock")
                    .select("sp2.card_group_id as storeCardGroupId");
        }

        // 排序
        if (ObjectUtil.isNotEmpty(listDTO.getIds())) {
            // ids 不为空按ids排序
            String orderSql = "FIELD(t." + getKeyColumn() + ", " + listDTO.getIds() + ")";
            queryWrapper.last("ORDER BY " + orderSql);
        } else {
            if (listDTO.getIsClient() != null && listDTO.getIsClient() == 1 && listDTO.getSortField() == null) {
                queryWrapper.orderByAsc(Product::getSortOrder);
                queryWrapper.orderByDesc(Product::getProductId);
            } else {
                buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());
            }
        }
        // 构造查询条件
        buildQueryWrapper(queryWrapper, listDTO);
        if (tigshopProperties.getIsO2o() == 1) {
            WrapperConditionUtils.copyConditionsWithOr(queryWrapper);
            queryWrapper.groupBy(Product::getProductId);
        }
        // 查询后的分页数据
        Page<Product> queryPage = this.page(page, queryWrapper);

        if (CollUtil.isEmpty(queryPage.getRecords())) {
            return PageUtil.convertPage(page, List.of());
        }

        queryPage.getRecords().forEach(product -> {
            if (tigshopProperties.getIsO2o() == 1 && listDTO.getShopId() != null && listDTO.getShopId() != 0 && product.getShopId() == 0) {
                ConfigPO storeAssignProductName = configService.getConfigByCode(SettingsEnum.STORE_ASSIGN_PRODUCT_NAME.getBizCode());
                if ("1".equals(storeAssignProductName.getBizVal())) {
                    product.setProductName(product.getStoreProductName());
                }
                ConfigPO storeAssignProductPrice = configService.getConfigByCode(SettingsEnum.STORE_ASSIGN_PRODUCT_PRICE.getBizCode());
                if ("1".equals(storeAssignProductPrice.getBizVal())) {
                    product.setProductPrice(product.getStoreProductPrice());
                }
                ConfigPO storeUseSoloProductStock = configService.getConfigByCode(SettingsEnum.STORE_USE_SOLO_PRODUCT_STOCK.getBizCode());
                if ("1".equals(storeUseSoloProductStock.getBizVal())) {
                    product.setProductStock(product.getStoreProductStock());
                }
                product.setProductStatus(product.getStoreProductStatus());
                product.setCardGroupId(product.getStoreCardGroupId());
            }
            if (product.getProductType() == PRODUCT_TYPE_CARD.getCode()) {
                product.setProductStock((int) eCardService.count(new LambdaQueryWrapper<ECard>()
                        .eq(ECard::getGroupId, product.getCardGroupId())));
            }
        });
        // 获取商品 ID 列表
        List<Integer> productIds = queryPage.getRecords().stream()
                .map(Product::getProductId)
                .toList();
        // 查询商品规格
        List<ProductSkuDTO> productSkus = productSkuService.getProductSkusByProductIds(productIds);

        // 将商品规格按商品 ID 分组
        Map<Integer, List<ProductSkuDTO>> productSkuMap = productSkus.stream()
                .collect(Collectors.groupingBy(ProductSkuDTO::getProductId));

        // 转换为DTO
        List<ProductListResDTO> productListRes = queryPage.getRecords().stream()
                .map(product -> {
                    ProductListResDTO dto = convertToDTO(product);
                    List<ProductSkuDTO> skus = productSkuMap.get(product.getProductId());
                    dto.setProductSku(skus);
                    int skuId = 0;
                    if (skus != null && !skus.isEmpty()) {
                        skuId = skus.getFirst().getSkuId();
                    }
                    dto.setPrice(this.getProductSkuDetail(product.getProductId(), skuId, 0, "", listDTO.getShopId()).getPrice());
                    return dto;
                })
                .toList();
        productListRes.forEach(product -> product.setProductName(translatePackage.translate(product.getProductName())));
        if (CollUtil.isNotEmpty(productIds)) {
            Long now = StringUtils.getCurrentTime();
            List<SeckillItem> seckillItems = seckillItemMapper.selectList(
                    Wrappers.lambdaQuery(SeckillItem.class)
                            .in(SeckillItem::getProductId, productIds)
                            .le(SeckillItem::getSeckillStartTime, now)
                            .ge(SeckillItem::getSeckillEndTime, now)
            );

            Map<Integer, SeckillItem> seckillItemMap = Optional.ofNullable(seckillItems)
                    .orElse(new ArrayList<>())
                    .stream()
                    .collect(Collectors.toMap(SeckillItem::getProductId
                            , Function.identity()
                            , (replacement, existing) -> replacement));
            productListRes.forEach(product -> {
                SeckillItem seckillItem = seckillItemMap.get(product.getProductId());
                if (seckillItem != null) {
                    product.setProductPrice(seckillItem.getSeckillPrice());
                }
            });
        }
        //循环获得店铺IDS
        List<Integer> shopIds = queryPage.getRecords().stream()
                .map(Product::getShopId)
                .toList();
        //如果不为空则查询shop获得shopMap
        if (!shopIds.isEmpty()) {
            List<Shop> shops = shopMapper.selectBatchIds(shopIds);
            //组装成shopId对应shop的shopMap
            if (shops != null && !shops.isEmpty()) {
                Map<Integer, Shop> shopMap = shops.stream()
                        .collect(Collectors.toMap(Shop::getShopId, Function.identity()));
                productListRes.forEach(product -> {
                    Integer shopId = product.getShopId();
                    if (shopId != null && shopId > 0 && shopMap.containsKey(shopId)) {
                        Shop shop = shopMap.get(shopId);
                        product.setShop(shop);
                    }
                });
            }
        }
        return PageUtil.convertPage(page, productListRes);
    }

    @Override
    public ProductDetailDTO detail(Integer productId) {
        ProductDetailDTO productDetailDTO = new ProductDetailDTO();
        Product product = this.getById(productId);
        if (product == null) {
            return null;
        }
        BeanUtils.copyProperties(product, productDetailDTO);
        if (StrUtil.isNotBlank(product.getPaidContent())) {
            productDetailDTO.setPaidContent(JsonUtil.checkJsonType(product.getPaidContent()));
        }
        // 商品详情，需要拆分并获取里面的图片
        String productDesc = product.getProductDesc();
        productDetailDTO.setProductDescArr(getProductDescArr(productDesc));

        // 商品关联商品，需要转成集合
        String productRelated = product.getProductRelated();
        productDetailDTO.setProductRelated(StringUtils.str2IntList(productRelated));

        // 商品服务，需要转成集合
        String productServiceIds = product.getProductServiceIds();
        productDetailDTO.setProductServiceIds(StringUtils.str2IntList(productServiceIds));

        // 商品相册
        List<ProductGallery> productGalleryList = productGalleryService.getProductGalleryList(productId);
        productDetailDTO.setImgList(productGalleryList);

        List<ProductVideo> productVideo = productVideoService.lambdaQuery().eq(ProductVideo::getProductId, productId).list();
        productDetailDTO.setProductVideoInfo(productVideo);
        // 商品规格
        ProductAttrsDTO productAttributes = productAttributesService.getProductAttributes(productId);
        productDetailDTO.setAttrList(productAttributes);

        // 商品文章
        List<ProductArticle> productArticleByProductId = productArticleService.getProductArticleByProductId(productId);
        productDetailDTO.setProductArticleList(productArticleByProductId.stream()
                .map(ProductArticle::getArticleId)
                .toList());

        // 构建商品列表属性
        productDetailDTO.setProductList(productSkuService.getProductSkusByPid(productId));


        // 查询到店自提模版
        if (Constants.YES.equals(product.getIsShopPickup())) {
            ShopPickupTpl shopPickupTpl = shopPickupTplService.getById(product.getShopPickupTplId());
            productDetailDTO.setShopPickupTpl(shopPickupTpl);
        }
        Integer shopId = getShopId();
        if (tigshopProperties.getIsO2o() == 1 && shopId != null && shopId > 0 && !Objects.equals(product.getShopId(), shopId)) {
            StoreProduct storeProduct = storeProductService.lambdaQuery().eq(StoreProduct::getShopId, shopId)
                    .eq(StoreProduct::getProductId, productId)
                    .last("limit 1")
                    .one();
            if (storeProduct != null) {
                ConfigPO storeAssignProductName = configService.getConfigByCode(SettingsEnum.STORE_ASSIGN_PRODUCT_NAME.getBizCode());
                if ("1".equals(storeAssignProductName.getBizVal())) {
                    productDetailDTO.setProductName(storeProduct.getProductName());
                }
                ConfigPO storeAssignProductPrice = configService.getConfigByCode(SettingsEnum.STORE_ASSIGN_PRODUCT_PRICE.getBizCode());
                if ("1".equals(storeAssignProductPrice.getBizVal())) {
                    productDetailDTO.setProductPrice(storeProduct.getProductPrice());
                    // 2. product_sku
                    if (ObjectUtil.isNotEmpty(productDetailDTO.getProductList())) {
                        List<StoreProductSku> list1 = storeProductSkuService.list(new LambdaQueryWrapper<StoreProductSku>().eq(StoreProductSku::getStoreProductId, storeProduct.getStoreProductId()));
                        if (ObjectUtil.isNotEmpty(list1)) {
                            Map<Long, StoreProductSku> skuMap = list1.stream()
                                    .collect(Collectors.toMap(StoreProductSku::getProductSkuId, Function.identity()));

                            productDetailDTO.getProductList().forEach(productSkuDTO -> {
                                StoreProductSku storeProductSku = skuMap.get(Long.valueOf(productSkuDTO.getSkuId()));
                                if (storeProductSku != null) {
                                    productSkuDTO.setSkuPrice(String.valueOf(storeProductSku.getSkuPrice()));
                                }
                            });
                        }
                    }
                }
                ConfigPO storeUseSoloProductStock = configService.getConfigByCode(SettingsEnum.STORE_USE_SOLO_PRODUCT_STOCK.getBizCode());
                if ("1".equals(storeUseSoloProductStock.getBizVal())) {
                    productDetailDTO.setProductStock(storeProduct.getProductStock());
                    // 2. product_sku
                    if (ObjectUtil.isNotEmpty(productDetailDTO.getProductList())) {
                        List<StoreProductSku> list1 = storeProductSkuService.list(new LambdaQueryWrapper<StoreProductSku>().eq(StoreProductSku::getStoreProductId, storeProduct.getStoreProductId()));
                        if (ObjectUtil.isNotEmpty(list1)) {
                            Map<Long, StoreProductSku> skuMap = list1.stream()
                                    .collect(Collectors.toMap(StoreProductSku::getProductSkuId, Function.identity()));

                            productDetailDTO.getProductList().forEach(productSkuDTO -> {
                                StoreProductSku storeProductSku = skuMap.get(Long.valueOf(productSkuDTO.getSkuId()));
                                if (storeProductSku != null) {
                                    productSkuDTO.setSkuStock(storeProductSku.getSkuStock());
                                }
                            });
                        }
                    }
                }
                productDetailDTO.setProductStatus(storeProduct.getProductStatus());
            }
        }

        return productDetailDTO;
    }

    @Override
    public ProductConfigVO config(Integer shopId) {
        List<ShippingTpl> shippingTplList;
        if (shopId == null || shopId == 0) {
            shippingTplList = shippingTplService.list();
        } else {
            shopId = HeaderUtils.getShopId();
            shippingTplList = shippingTplService.lambdaQuery().eq(ShippingTpl::getShopId, shopId).list();
        }
        List<Suppliers> suppliersList = suppliersService.list();
        List<Services> servicesList = servicesService.list();
        List<Integer> servicesIdList = servicesList.stream()
                .map(Services::getProductServiceId)
                .toList();
        List<AttributesTpl> attrTplList = attributesTplService.list();
        List<UserRank> userRankList = userRankService.list();
        List<ECardGroup> eCardGroups = ecardGroupService.lambdaQuery().eq(ECardGroup::getIsUse, Constants.YES).list();

        return new ProductConfigVO(shippingTplList, suppliersList, servicesList, attrTplList, userRankList, servicesIdList, eCardGroups);
    }

    /**
     * 将 ProductSkuDTO 转换为 ProductSku
     */
    public List<ProductSku> convertToProductSku(List<ProductSkuDTO> productSkusDTO) {
        return productSkusDTO.stream()
                .map(this::mapToProductSku)
                .collect(Collectors.toList());
    }

    /**
     * 将 ProductListAttributeDTO 转换为 ProductSkuDTO
     */
    private List<ProductSkuDTO> convertToProductSkuDTO(List<ProductListAttributeDTO> productList, Integer productId) {
        return productList.stream()
                .map(productListAttributeDTO -> {
                    ProductSkuDTO productSkuDTO = new ProductSkuDTO();
                    BeanUtils.copyProperties(productListAttributeDTO, productSkuDTO);

                    List<ProductListAttributeDTO.Attribute> attrs = productListAttributeDTO.getAttrs();
                    if (attrs != null && !attrs.isEmpty()) {
                        // 转换 attrs -> skuData
                        List<ProductSkuDTO.SkuData> skuDataList = attrs.stream()
                                .map(attr -> new ProductSkuDTO.SkuData(attr.getAttrName(), attr.getAttrValue()))
                                .collect(Collectors.toList());

                        productSkuDTO.setSkuData(skuDataList);
                    }
                    productSkuDTO.setProductId(productId);
                    return productSkuDTO;
                })
                .collect(Collectors.toList());
    }

    /**
     * 将单个 ProductSkuDTO 转换为 ProductSku
     */
    private ProductSku mapToProductSku(ProductSkuDTO dto) {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductSku productSku = new ProductSku();
        BeanUtils.copyProperties(dto, productSku);
        String skuPrice = dto.getSkuPrice();
        if (StrUtil.isNotEmpty(skuPrice)) {
            productSku.setSkuPrice(new BigDecimal(skuPrice));
        }

        // 将 SkuData 转换为 JSON 字符串
        try {
            if (dto.getSkuData() != null) {
                productSku.setSkuData(objectMapper.writeValueAsString(dto.getSkuData()));
            }
        } catch (JsonProcessingException e) {
            throw new GlobalException(SERVICE_ERROR);
        }

        return productSku;
    }

    /**
     * 保存商品规格信息
     */
    @Transactional(rollbackFor = Exception.class)
    protected void saveProductSku(List<ProductListAttributeDTO> productList, Product product) {
        Integer productId = product.getProductId();

        if (CollUtil.isEmpty(productList)) {
            productSkuService.lambdaUpdate()
                    .eq(ProductSku::getProductId, productId)
                    .remove();
            return;
        }

        // 转换为 ProductSkuDTO 列表
        List<ProductSkuDTO> productSkusDTO = convertToProductSkuDTO(productList, productId);

        // 转换为 ProductSku 列表
        List<ProductSku> productSkus = convertToProductSku(productSkusDTO);
        List<Integer> currentSkuIds = productSkus.stream().map(ProductSku::getSkuId).toList();

        List<ProductSku> dbProductSkus = productSkuService.lambdaQuery().eq(ProductSku::getProductId, productId).list();
        Map<Integer, ProductSku> dbSkuMap = dbProductSkus.stream().collect(Collectors.toMap(ProductSku::getSkuId, Function.identity()));

        List<ProductInventoryLog> operationLogs = new ArrayList<>();

        // 新增 sku
        List<ProductSku> addSkus = productSkus.stream()
                .filter(item -> item.getSkuId() == null)
                .map(item -> BeanUtil.copyProperties(item, ProductSku.class))
                .toList();
        if (CollUtil.isNotEmpty(addSkus)) {
            productSkuService.saveBatch(addSkus);
            // 创建 sku 库存日志
            List<ProductInventoryLog> addLogs = addSkus.stream()
                    .map(item -> ProductInventoryLog.builder()
                            .productId(item.getProductId())
                            .specId(item.getSkuId())
                            .number(item.getSkuStock())
                            .addTime(StringUtils.getCurrentTime())
                            .oldNumber(0)
                            .type(1)
                            .changeNumber(item.getSkuStock())
                            .desc("新增产品规格")
                            .shopId(product.getShopId())
                            .build()
                    ).toList();
            operationLogs.addAll(addLogs);
        }

        // 删除 sku
        List<ProductSku> delSkus = dbProductSkus.stream().filter(item -> !currentSkuIds.contains(item.getSkuId())).toList();
        if (CollUtil.isNotEmpty(delSkus)) {
            productSkuService.removeByIds(delSkus);
            // 创建 sku 库存日志
            List<ProductInventoryLog> delLogs = delSkus.stream()
                    .map(item -> ProductInventoryLog.builder()
                            .productId(item.getProductId())
                            .specId(item.getSkuId())
                            .number(0)
                            .addTime(StringUtils.getCurrentTime())
                            .oldNumber(item.getSkuStock())
                            .type(1)
                            .changeNumber(Math.negateExact(item.getSkuStock()))
                            .desc("删除产品规格")
                            .shopId(product.getShopId())
                            .build()
                    ).toList();
            operationLogs.addAll(delLogs);
        }

        // 编辑 sku
        List<ProductSku> editSkus = productSkus.stream().filter(item -> item.getSkuId() != null).toList();
        if (CollUtil.isNotEmpty(editSkus)) {
            productSkuService.updateBatchById(editSkus);
            // 创建 sku 库存日志
            List<ProductInventoryLog> editLogs = editSkus.stream()
                    .map(item -> {
                                ProductSku dbSku = dbSkuMap.get(item.getSkuId());
                                int changeNumber = item.getSkuStock() - dbSku.getSkuStock();

                                return ProductInventoryLog.builder()
                                        .productId(item.getProductId())
                                        .specId(item.getSkuId())
                                        .number(item.getSkuStock())
                                        .addTime(StringUtils.getCurrentTime())
                                        .oldNumber(dbSku.getSkuStock())
                                        .type(1)
                                        .changeNumber(changeNumber)
                                        .desc("删除产品规格")
                                        .shopId(product.getShopId())
                                        .build();
                            }
                    ).toList();
            operationLogs.addAll(editLogs);
        }

        // 新增库存日志
        if (CollUtil.isNotEmpty(operationLogs)) {
            productInventoryLogService.saveBatch(operationLogs);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(ProductSaveParam param) {
        Product product = new Product();
        BeanUtils.copyProperties(param, product);

        if (CollUtil.isNotEmpty(param.getPaidContent())) {
            product.setPaidContent(param.getPaidContent().toJSONString(0));
        }

        // 校验商品价格
        checkProductPrice(param.getProductPrice());

        // 校验商品货号唯一
        checkProductSn(param.getProductSn());

        // 构建商品对象
        buildProduct(param, product);
        // 保存商品
        boolean isSaved = this.save(product);

        if (isSaved) {
            // 其他相关更新
            applicationContext.getBean(ProductService.class).otherUpdates(param, product.getProductSn(), product, param.getImgList());

            // 增加库存编辑日志
            if (CollUtil.isEmpty(param.getProductList())) {
                ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                        .productId(product.getProductId())
                        .specId(0)
                        .number(product.getProductStock())
                        .addTime(StringUtils.getCurrentTime())
                        .oldNumber(0)
                        .type(1)
                        .changeNumber(product.getProductStock())
                        .desc("库存新增")
                        .shopId(product.getShopId())
                        .build();
                productInventoryLogService.save(productInventoryLog);
            }
        }
        if (!isSaved) {
            return "商品更新失败";
        }

        // 异步同步到ES
        try {
            productEsSyncService.syncProductAsync(Long.valueOf(product.getProductId()));
        } catch (Exception e) {
            log.error("商品创建后ES同步失败: {}", product.getProductId(), e);
        }

        Integer shopId = HeaderUtils.getShopId();
        if (shopId != null && shopId > 0) {
            String shopProductNeedCheck = configService.getConfigByCode("shopProductNeedCheck").getBizVal();
            if ("1".equals(shopProductNeedCheck)) {
                return "商品已添加，等待管理员审核";
            }
        }
        return "商品已添加";
    }

    /**
     * 构建商品对象
     */
    private void buildProduct(ProductSaveParam param, Product product) {
        List<ProductListAttributeDTO> productList = param.getProductList();
        if (CollUtil.isNotEmpty(productList)) {
            int sum = productList.stream().mapToInt(ProductListAttributeDTO::getSkuStock).sum();
            product.setProductStock(sum);
        }

        // 商品关键字
        String keywords = param.getKeywords();
        if (StrUtil.isNotEmpty(keywords)) {
            // 分词
            keywords = StringUtils.cutForSearch(keywords);
            product.setKeywords(keywords);
        }

        // 关联产品
        List<Integer> productRelated = param.getProductRelated();
        if (productRelated != null && !productRelated.isEmpty()) {
            product.setProductRelated(productRelated.toString());
        }

        // 商品服务
        List<Integer> productServiceIds = param.getProductServiceIds();
        if (productServiceIds != null && !productServiceIds.isEmpty()) {
            product.setProductServiceIds(productServiceIds.toString());
        }

        // 商品描述
        String productDesc = ProductDescDTO.getProductDesc(param.getProductDescArr());
        if (StrUtil.isNotEmpty(productDesc)) {
            product.setProductDesc(productDesc);
        }

        // 商品图片
        List<GalleryPic> imgList = param.getImgList();
        if (imgList != null && !imgList.isEmpty()) {
            setImgList(imgList, product);
        }

        // 商家后台情况下,新增情况下才需要审核
        if (product.getProductId() == null) {
            checkShopId(product);
        }

        // 商品序号
        product.setProductSn(param.getProductSn());
        // 添加时间
        product.setAddTime(StringUtils.getCurrentTime());
    }

    /**
     * 设置商品图片
     */
    private void setImgList(List<GalleryPic> imgList, Product product) {
        if (imgList != null && !imgList.isEmpty()) {
            String picUrl = imgList.getFirst().getPicUrl();
            product.setPicOriginal(picUrl);
            product.setPicThumb(imgList.getFirst().getPicThumb());
            if (product.getProductId() != null) {
                product.setPicUrl(imgList.getFirst().getPicUrl());
            } else {
                product.setPicUrl(StrUtil.format("{}{}", picUrl, galleryPicService.getImageSize().get("size_400")));

            }

        }
    }

    /**
     * 商家后台情况下，设置商品状态和审核状态
     */
    private void checkShopId(Product product) {
        if (getShopId() != null && getShopId() > 0) {
            String shopProductNeedCheckVal = configService.getConfigByCode(SettingsEnum.SHOP_PRODUCT_NEED_CHECK.getBizCode()).getBizVal();
            int shopProductNeedCheck = Integer.parseInt(shopProductNeedCheckVal);
            // 商家后台需要审核
            if (shopProductNeedCheck == 1) {
                product.setProductStatus(ProductStatus.OFF_SALE.getCode());
                product.setCheckStatus(CheckStatus.PENDING.getCode());
            } else {
                product.setProductStatus(ON_SALE.getCode());
                product.setCheckStatus(CheckStatus.APPROVED.getCode());
            }
        }
    }

    /**
     * 保存商品属性
     *
     * @param attrList  属性参数
     * @param productId 商品id
     */
    @Transactional(rollbackFor = Exception.class)
    protected void saveProductAttr(ProductAttrsDTO attrList, Integer productId) {
        if (attrList == null) {
            return;
        }
        List<ProductAttributeDTO> attributesDTO = Stream.of(attrList.getExtra(), attrList.getSpe(), attrList.getNormal())
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .flatMap(attribute -> attribute.getAttrList().stream())
                .peek(attr -> attr.setProductId(productId))
                .toList();

        List<ProductAttribute> attrs = attributesDTO.stream().map(attr -> {
            ProductAttribute productAttribute = new ProductAttribute();
            BeanUtils.copyProperties(attr, productAttribute);
            productAttribute.setAttrPrice(attr.getAttrPrice());
            return productAttribute;
        }).toList();

        // 批量保存（如果当前商品没有任何的规格参数传过来，就删除绑定的所有规格）
        if (CollUtil.isEmpty(attrs)) {
            productAttributesService.remove(new LambdaQueryWrapper<ProductAttribute>()
                    .eq(ProductAttribute::getProductId, productId)
            );
            return;
        }

        List<ProductAttribute> saveAttrs = attrs.stream().filter(attr -> attr.getAttributesId() == null).toList();
        List<ProductAttribute> editAttrs = attrs.stream().filter(attr -> attr.getAttributesId() != null).toList();
        productAttributesService.remove(new LambdaQueryWrapper<ProductAttribute>()
                .eq(ProductAttribute::getProductId, productId)
                .notIn(ObjectUtil.isNotEmpty(editAttrs.stream().map(ProductAttribute::getAttributesId).toList()),
                        ProductAttribute::getAttributesId, editAttrs.stream().map(ProductAttribute::getAttributesId).toList())
        );
        if (CollUtil.isNotEmpty(saveAttrs)) {
            productAttributesService.saveBatch(saveAttrs);
        }
        if (CollUtil.isNotEmpty(editAttrs)) {
            productAttributesService.updateBatchById(editAttrs);
        }
    }

    /**
     * 保存商品图片
     *
     * @param imgList   图片列表
     * @param productId 商品ID
     */
    @Transactional(rollbackFor = Exception.class)
    protected void saveProductGallery(List<GalleryPic> imgList, Integer productId) {
        if (CollUtil.isEmpty(imgList)) {
            return;
        }

        // 1. 删除图片
        productGalleryService.lambdaUpdate().eq(ProductGallery::getProductId, productId).remove();

        // 2. 新增图片
        List<ProductGallery> productGalleryList = IntStream.range(0, imgList.size())
                .mapToObj(i -> {
                    GalleryPic galleryPic = imgList.get(i);
                    ProductGallery productGallery = new ProductGallery();
                    productGallery.setProductId(productId);
                    String picUrl = galleryPic.getPicUrl();
                    productGallery.setPicOriginal(picUrl);
                    productGallery.setPicThumb(galleryPic.getPicThumb());
                    if (productId == null) {
                        productGallery.setPicUrl(StrUtil.format("{}{}", picUrl, galleryPicService.getImageSize().get("size_400")));
                        productGallery.setPicLarge(StrUtil.format("{}{}", picUrl, galleryPicService.getImageSize().get("size_800")));
                    } else {
                        productGallery.setPicUrl(picUrl);
                        productGallery.setPicLarge(picUrl);
                    }

                    productGallery.setSortOrder(i);
                    return productGallery;
                }).toList();
        productGalleryService.saveBatch(productGalleryList);
    }

    /**
     * 新增商品关联文章
     *
     * @param productArticleList 文章IDs
     * @param productId          商品ID
     */
    @Transactional(rollbackFor = Exception.class)
    protected void saveProductArticle(List<Integer> productArticleList, Integer productId) {
        if (productArticleList.isEmpty()) {
            return;
        }
        // 需要保存的实体对象
        List<ProductArticle> productArticles = productArticleList.stream().map(articleId -> {
            ProductArticle productArticle = new ProductArticle();
            productArticle.setArticleId(articleId);
            productArticle.setGoodsId(productId);
            return productArticle;
        }).collect(Collectors.toList());

        boolean isSuccess;
        // 保存关联文章
        long count = productArticleService.count(new LambdaQueryWrapper<ProductArticle>()
                .eq(ProductArticle::getGoodsId, productId));
        if (count > 0) {
            productArticleService.remove(new LambdaQueryWrapper<ProductArticle>()
                    .in(ProductArticle::getArticleId, productArticleList));
        }
        isSuccess = productArticleService.saveBatch(productArticles);
        if (!isSuccess) {
            throw new GlobalException(SERVICE_ERROR);
        }
    }

    /**
     * 更新商品货号
     *
     * @param productId 商品ID
     */
    @Transactional(rollbackFor = Exception.class)
    protected void updateSn(Integer productId) {
        String snName = configService.getConfigByCode(SettingsEnum.SN_PREFIX.getBizCode()).getBizVal();
        String productSn = StrUtil.format("{}{}", snName, String.format("%07d", productId));
        Product product = new Product();
        product.setProductId(productId);
        product.setProductSn(productSn);
        boolean isUpdated = this.updateById(product);
        if (!isUpdated) {
            throw new GlobalException(SERVICE_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(ProductEditParam param) {
        // 如果为门店商品，非平台不能操作
        Product dbProduct = this.getById(param.getProductId());
        Assert.isTrue(dbProduct != null, () -> new GlobalException("商品不存在"));

        if (dbProduct.getVendorProductId() != null && param.getProductStatus() == 1) {
            VendorProduct byId = vendorProductService.getById(dbProduct.getVendorProductId());
            Assert.notNull(byId, () -> new GlobalException("供应商商品不存在"));
            Assert.isTrue(byId.isSales(), () -> new GlobalException("供应商商品未上架"));
        }
        if (tigshopProperties.getIsO2o() == 1) {
            // 是分配商品，且店铺在修改为上线时
            if (dbProduct.getShopId() != 0 && AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.STORE) {
                String storeIndependentGoods = configService.getConfigVal(STORE_INDEPENDENT_GOODS);
                if ("0".equals(storeIndependentGoods)) {
                    throw new GlobalException("平台已设置无法自建商品");
                }
            }
            if (dbProduct.getShopId() == 0
                    && AdminTypeEnum.fromCode(HeaderUtils.getAdminType()) != AdminTypeEnum.ADMIN) {
                // 修改根据配置项进行修改
                GetStoreVO settings = configService.getSettings(GetStoreVO.class);
                // 可以修改商品价格

                // 1. product
                if (ObjectUtil.isNotEmpty(param.getProductList())) {
                    param.setProductStock(param.getProductList().stream().mapToInt(ProductSkuDTO::getSkuStock).sum());
                }
                storeProductService.lambdaUpdate()
                        .eq(StoreProduct::getProductId, param.getProductId())
                        .eq(StoreProduct::getShopId, getShopId())
                        .set(settings.getStoreAssignProductPrice() == 1, StoreProduct::getProductPrice, param.getProductPrice())
                        .set(settings.getStoreAssignProductName() == 1, StoreProduct::getProductName, param.getProductName())
                        .set(StoreProduct::getProductStock, param.getProductStock())
                        .update();
                StoreProduct storeProduct = storeProductService.getOne(new LambdaQueryWrapper<StoreProduct>()
                        .eq(StoreProduct::getProductId, param.getProductId())
                        .eq(StoreProduct::getShopId, getShopId()));
                // 2. product_sku
                if (ObjectUtil.isNotEmpty(param.getProductList())) {
                    storeProductSkuService.remove(new LambdaQueryWrapper<StoreProductSku>().eq(StoreProductSku::getStoreProductId, storeProduct.getStoreProductId()));
                    List<ProductSku> list = productSkuService.list(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, param.getProductId()));

                    StoreProduct one = storeProductService.lambdaQuery()
                            .eq(StoreProduct::getProductId, param.getProductId())
                            .eq(StoreProduct::getShopId, getShopId())
                            .last("limit 1").one();
                    storeProductSkuService.saveBatch(list.stream().map(product -> {
                        StoreProductSku storeProductSku = new StoreProductSku();
                        storeProductSku.setProductId(Long.valueOf(product.getProductId()));
                        storeProductSku.setProductSkuId(Long.valueOf(product.getSkuId()));

                        param.getProductList().stream().filter(sku -> sku.getSkuValue().equals(product.getSkuValue()))
                                .findFirst()
                                .ifPresent(sku -> {
                                    storeProductSku.setSkuPrice(new BigDecimal(sku.getSkuPrice()));
                                    storeProductSku.setSkuStock(sku.getSkuStock());
                                });

                        storeProductSku.setAddTime(StringUtils.getCurrentTime());
                        storeProductSku.setStoreProductId(one.getStoreProductId());
                        return storeProductSku;
                    }).collect(Collectors.toList()));
                }

                return true;
            }
        }
        Integer shopId = HeaderUtils.getShopId();
        if (shopId != null && shopId > 0) {
            String shopProductNeedCheck = configService.getConfigByCode("shopProductNeedCheck").getBizVal();
            if ("1".equals(shopProductNeedCheck)) {
                // 待审核，未上架
                param.setCheckStatus(0);
                param.setProductStatus(0);
            }
        }

        Product product = new Product();
        BeanUtils.copyProperties(param, product);
        if (product.getBrandId() == null) {
            product.setBrandId(0);
        }
        // 判断是否是卡密商品，并且状态改为上架。需要判断卡密是否可以使用
        if (product.getProductType() == PRODUCT_TYPE_CARD.getCode()
                && param.getProductStatus() == 1) {
            ECardGroup byId = ecardGroupService.getById(product.getCardGroupId());
            if (byId.getIsUse() == 0) {
                throw new GlobalException("电子卡卷组【" + byId.getGroupName() + "】未启用，请先启用电子卡卷组！");
            }
        }
        if (CollUtil.isNotEmpty(param.getPaidContent())) {
            product.setPaidContent(param.getPaidContent().toJSONString(0));
        }

        // 校验商品价格
        checkProductPrice(param.getProductPrice());

        // 校验商品货号唯一
        long count = this.count(new LambdaQueryWrapper<Product>()
                .eq(Product::getProductSn, param.getProductSn())
                .eq(Product::getProductId, param.getProductId()));
        if (count < 1) {
            checkProductSn(param.getProductSn());
        }

        // 构建商品对象
        buildProduct(param, product);

        if (CollUtil.isNotEmpty(param.getProductList())) {
            List<BigDecimal> skuPrices = param.getProductList().stream().map(item -> new BigDecimal(item.getSkuPrice())).sorted().toList();
            product.setProductPrice(skuPrices.getFirst());
        }

        // 执行更新操作
        boolean isUpdated = this.updateById(product);

        if (isUpdated) {
            // 其他相关更新
            applicationContext.getBean(ProductService.class).otherUpdates(param, product.getProductSn(), product, param.getImgList());

            // 增加库存编辑日志
            int diff = product.getProductStock() - dbProduct.getProductStock();
            if (CollUtil.isEmpty(param.getProductList()) && diff != 0) {
                ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                        .productId(product.getProductId())
                        .specId(0)
                        .number(product.getProductStock())
                        .addTime(StringUtils.getCurrentTime())
                        .oldNumber(dbProduct.getProductStock())
                        .type(diff > 0 ? 1 : 2)
                        .changeNumber(diff)
                        .desc("库存编辑")
                        .shopId(product.getShopId())
                        .build();
                productInventoryLogService.save(productInventoryLog);
            }

            // 异步同步到ES
            try {
                productEsSyncService.syncProductAsync(Long.valueOf(product.getProductId()));
            } catch (Exception e) {
                log.error("商品编辑后ES同步失败: {}", product.getProductId(), e);
            }
        }
        return isUpdated;
    }

    /**
     * 其他相关更新
     *
     * @param param     商品DTO
     * @param productSn 商品货号
     * @param product   商品
     * @param imgList   图片列表
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void otherUpdates(ProductSaveParam param, String productSn, Product product, List<GalleryPic> imgList) {
        Integer productId = product.getProductId();
        if (StrUtil.isEmpty(productSn)) {
            // 更新商品序号
            updateSn(productId);
        }
        String adminType = getAdminType();
        if (adminType.equals(AdminTypeEnum.ADMIN.getCode()) && tigshopProperties.getIsO2o() == 1) {
            StoreProduct oldStoreProduct = storeProductService.getOne(new LambdaQueryWrapper<StoreProduct>()
                    .eq(StoreProduct::getProductId, productId)
                    .eq(StoreProduct::getShopId, 0L));
            StoreProduct storeProduct = StoreProduct.builder()
                    .productId(productId)
                    .productName(product.getProductName())
                    .productPrice(product.getProductPrice())
                    .productStatus(product.getProductStatus())
                    .productStock(product.getProductStock())
                    .shopId(0L)
                    .addTime(StringUtils.getCurrentTime())
                    .isDelete(0)
                    .cardGroupId(product.getCardGroupId())
                    .shopCategoryId(product.getShopCategoryId())
                    .build();
            if (oldStoreProduct == null) {
                storeProductService.save(storeProduct);
            } else {
                storeProduct.setStoreProductId(oldStoreProduct.getStoreProductId());
                storeProductService.updateById(storeProduct);
            }

        }

        if (CollUtil.isNotEmpty(param.getProductVideoInfo())) {
            // 更新 商品视频
            productVideoService.lambdaUpdate().eq(ProductVideo::getProductId, productId).remove();
            for (ProductVideo productVideo : param.getProductVideoInfo()) {
                productVideo.setProductId(productId);
                productVideo.setId(null);
                productVideoService.save(productVideo);
            }
        } else {
            productVideoService.lambdaUpdate().eq(ProductVideo::getProductId, productId).remove();
        }

        // 更新 商品图片
        saveProductGallery(imgList, productId);

        // 更新 关联文章
        List<Integer> productArticleList = param.getProductArticleList();
        if (productArticleList != null && !productArticleList.isEmpty()) {
            // 保存商品关联文章
            saveProductArticle(productArticleList, productId);
        }

        // 更新 商品属性
        ProductAttrsDTO attrList = param.getAttrList();
        if (attrList != null) {
            // 保存商品属性
            saveProductAttr(attrList, productId);
        }
        // 更新 商品分组
        Integer isHot = param.getIsHot();
        if (isHot != null && isHot == 1) {
            productGroupService.updateGroupProductByName(productId, IS_HOT.getDesc());
        }
        Integer isNew = param.getIsNew();
        if (isNew != null && isNew == 1) {
            productGroupService.updateGroupProductByName(productId, IS_NEW.getDesc());
        }

        // 更新sku
        List<ProductListAttributeDTO> productList = param.getProductList();

        // 保存商品sku
        saveProductSku(productList, product);


        if (param.getProductStatus() != null && param.getProductStatus() == 0) {
            AdminMsgCreateDTO createDTO = new AdminMsgCreateDTO();
            createDTO.setMsgType(AdminMsgTypeEnum.PRODUCT_OUT_OF_STOCK.getCatId());
            createDTO.setShopId(param.getShopId());
            createDTO.setProductId(productId);
            createDTO.setTitle("商品下架:" + param.getProductName());
            createDTO.setContent("您的商品【" + param.getProductName() + "】已经下架，请及时处理！");
            Map<String, Object> relatedData = new HashMap<>();
            relatedData.put("productId", productId);
            createDTO.setRelatedData(relatedData);
            adminMsgService.createMessage(createDTO);
        }
    }

    @Override
    public List<Product> getProductByIds(List<Integer> productIds) {
        if (productIds.isEmpty()) {
            return List.of();
        }
        return this.list(new LambdaQueryWrapper<Product>().in(Product::getProductId, productIds));
    }

    /**
     * 转换为DTO
     *
     * @param product 商品实体
     * @return DTO
     */
    public ProductListResDTO convertToDTO(Product product) {
        ProductListResDTO dto = new ProductListResDTO();
        BeanUtils.copyProperties(product, dto);
        return dto;
    }

    /**
     * 构建商品列表查询条件
     *
     * @param queryWrapper 查询构造器
     * @param listDTO      查询参数
     */
    private void buildQueryWrapper(MPJLambdaWrapper<Product> queryWrapper, ProductListDTO listDTO) {

        // 关键词查询条件
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.nested(wrapper -> wrapper.like(Product::getProductName, keyword)
                    .or()
                    .like(Product::getProductSn, keyword));
        }
        if (listDTO.getSearchShop() != null && listDTO.getSearchShop() == 1) {
            //则查shop_id >0 的
            queryWrapper.gt(Product::getShopId, 0);
        }
        if (listDTO.getShopId() != null && listDTO.getShopId() > 0) {
            queryWrapper.eq(Product::getShopId, listDTO.getShopId());
        }
        // 商品类型查询条件
        String introType = listDTO.getIntroType();
        introType = StrUtil.toUnderlineCase(introType);
        if (StrUtil.isNotEmpty(introType)) {
            IntroType type = fromString(introType);
            if (type != null) {
                // 这里introType是is_best/is_hot/is_new中的一个，1代表是，0代表否
                if (IntroType.IS_BEST.equals(type)) {
                    queryWrapper.eq(Product::getIsBest, 1);
                } else if (IntroType.IS_HOT.equals(type)) {
                    queryWrapper.eq(Product::getIsHot, 1);
                } else {
                    queryWrapper.eq(Product::getIsNew, 1);
                }

            }
        }
        // 优惠券
        Integer couponId = listDTO.getCouponId();
        if (couponId != null && couponId > 0) {
            //获取优惠券详情
            CouponVO coupon = couponService.detail(couponId);
            if (coupon != null) {
                if (coupon.getSendRange() == SendRangeEnum.CATEGORY.getValue()) {
                    queryWrapper.nested(wrapper -> {
                        for (Integer itemCategoryId : coupon.getSendRangeData()) {
                            wrapper.in(Product::getCategoryId, categoryService.getCategoryAllChildIds(itemCategoryId), "OR");
                        }
                    });
                } else if (coupon.getSendRange() == SendRangeEnum.BRAND.getValue()) {
                    queryWrapper.in(Product::getBrandId, coupon.getSendRangeData());
                } else if (coupon.getSendRange() == SendRangeEnum.PRODUCT.getValue()) {
                    queryWrapper.in(Product::getProductId, coupon.getSendRangeData());
                } else if (coupon.getSendRange() == SendRangeEnum.NOT_PRODUCT.getValue()) {
                    queryWrapper.notIn(Product::getProductId, coupon.getSendRangeData());
                }
                queryWrapper.eq(Product::getShopId, coupon.getShopId());

            }
        }

        // 品牌ID
        Integer brandId = listDTO.getBrandId();
        if (brandId != null && brandId >= 0) {
            queryWrapper.eq(Product::getBrandId, brandId);
        }

        // 品牌IDs
        if (StrUtil.isNotBlank(listDTO.getBrandIds())) {
            List<String> brandIds = StrUtil.split(listDTO.getBrandIds(), ",");
            if (!brandIds.isEmpty()) {
                queryWrapper.in(Product::getBrandId, brandIds);
            }
        }

        //处理数组的ids，如果是必要条件，传入时请确保不为空 product_ids与idList一致了
        if (listDTO.getIdList() != null && !listDTO.getIdList().isEmpty()) {
            queryWrapper.or(wrapper -> wrapper.in(Product::getProductId, listDTO.getIdList()));
        }

        // 不包含的id
        if (listDTO.getExtraProductIds() != null && !listDTO.getExtraProductIds().isEmpty()) {
            queryWrapper.notIn(Product::getProductId, listDTO.getExtraProductIds());
        }

        // 商品分类ID查询条件
        Integer categoryId = listDTO.getCategoryId();
        if (categoryId != null && categoryId > 0) {
            List<Integer> ids = categoryService.getCategoryAllChildIds(listDTO.getCategoryId());
            queryWrapper.in(Product::getCategoryId, ids);
        }

        // 商品分类ID查询条件
        Integer shopCategoryId = listDTO.getShopCategoryId();
        if (shopCategoryId != null && shopCategoryId > 0 && listDTO.getShopId() > 0) {
            ShopProductCategory byId = shopProductCategoryService.getById(shopCategoryId);
            // 选择的分类不是店铺类别时添加筛选
            if (byId.getParentId() != 0) {
                List<Integer> ids = shopProductCategoryService.getCategoryAllChildIds(shopCategoryId, listDTO.getShopId());
                queryWrapper.in(Product::getShopCategoryId, ids);
            } else {
                queryWrapper.eq(Product::getShopCategoryId, shopCategoryId);
            }
        }


        //处理字符串的ids
        if (StrUtil.isNotBlank(listDTO.getIds())) {
            List<String> ids = StrUtil.split(listDTO.getIds(), ",");
            if (!ids.isEmpty()) {
                queryWrapper.in(Product::getProductId, ids);
            }
        }
        //maxPrice
        if (listDTO.getMaxPrice() != null) {
            queryWrapper.le(Product::getProductPrice, listDTO.getMaxPrice().doubleValue());
        }
        // minPrice
        if (listDTO.getMinPrice() != null) {
            queryWrapper.ge(Product::getProductPrice, listDTO.getMinPrice().doubleValue());
        }
        // 店铺ID查询条件
        Integer shopId = listDTO.getShopId();
        if (shopId != null && shopId != -1) {
            queryWrapper.eq(Product::getShopId, shopId);
        }
        // 搜索店铺
        if (listDTO.getSearchShop() != null && listDTO.getSearchShop() != 0) {
            //则查shop_id >0 的
            queryWrapper.gt(Product::getShopId, 0);
        }

        if (shopId != null && shopId == 0 && listDTO.getSearchShop() != null && listDTO.getSearchShop() == 0) {
            queryWrapper.eq(Product::getShopId, 0);
        }
        //分组id
        if (listDTO.getProductGroupId() != null && listDTO.getProductGroupId() > 0) {
            // 通过product_group表先获取product_group_id对应的product_ids
            List<Integer> groupProductIds = productGroupService.detail(listDTO.getProductGroupId()).getProductIds();
            if (groupProductIds.isEmpty()) {
                queryWrapper.eq(Product::getProductId, -1);
            } else {
                queryWrapper.in(Product::getProductId, groupProductIds);
            }
        }
        // 商品状态查询条件
        Integer productStatus = listDTO.getProductStatus();
        if (productStatus != null && ProductStatus.isValidCode(productStatus)) {
            queryWrapper.eq(Product::getProductStatus, productStatus);
        }
        // 是否删除
        Integer isDelete = listDTO.getIsDelete();
        if (isDelete != null && DeleteType.isValidCode(isDelete)) {
            queryWrapper.eq(Product::getIsDelete, isDelete);
        }
        // 是否最新
        if (listDTO.getIsNew() != null && listDTO.getIsNew() > -1) {
            queryWrapper.eq(Product::getIsNew, listDTO.getIsNew());
        }

        // 审核状态查询条件
        Integer checkStatus = listDTO.getCheckStatus();
        if (checkStatus != null && CheckStatus.isValidCode(checkStatus)) {
            queryWrapper.eq(Product::getCheckStatus, checkStatus);
        }

        //供应商
        Integer suppliersId = listDTO.getSuppliersId();
        if (suppliersId != null && suppliersId > 0) {
            queryWrapper.eq(Product::getSuppliersId, suppliersId);
        }
        //排除不运营的店铺
        List<Shop> shops = shopMapper.selectList(new LambdaQueryWrapper<Shop>().eq(Shop::getStatus, 4));
        queryWrapper.notIn(ObjectUtil.isNotEmpty(shops), Product::getShopId, shops.stream().map(Shop::getShopId).toList());
    }

    /**
     * 获取待审核商品数量
     *
     * @return 待审核商品数量
     */
    @Override
    public Long getWaitingCheckedCount() {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getCheckStatus, CheckStatus.PENDING.getCode());
        queryWrapper.eq(Product::getIsDelete, NOT_DELETE.getCode());

        //排除不运营的店铺
        List<Shop> shops = shopMapper.selectList(new LambdaQueryWrapper<Shop>().eq(Shop::getStatus, 4));
        queryWrapper.notIn(ObjectUtil.isNotEmpty(shops), Product::getShopId, shops.stream().map(Shop::getShopId).toList());
        return this.count(queryWrapper);
    }

    /**
     * 校验商品货号是否重复
     *
     * @param productSn 商品货号
     */
    public void checkProductSn(String productSn) {
        if (StrUtil.isNotEmpty(productSn)) {
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Product::getProductSn, productSn);
            if (this.count(queryWrapper) > 0) {
                throw new GlobalException(PRODUCT_SN_EXIST);
            }
        }
    }

    /**
     * 校验商品价格是否大于0
     *
     * @param productPrice 商品价格
     */
    public void checkProductPrice(BigDecimal productPrice) {
        if (productPrice == null) {
            throw new GlobalException(PRODUCT_PRICE_LE_0);
        }
        if (productPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException(PRODUCT_PRICE_LE_0);
        }
    }

    /**
     * 获取商品详情描述
     *
     * @param productDescList 商品详情描述列表
     * @return 商品详情描述字符串
     */
    public String getProductDesc(List<ProductDescDTO> productDescList) {
        if (productDescList == null || productDescList.isEmpty()) {
            return "";
        }

        // 生成结果列表
        List<String> result = productDescList.stream()
                .map(item -> {
                    String type = item.getType();
                    if ("pic".equals(type)) {
                        String pic = item.getPic();
                        return "<div class=\"desc-pic-item\"><img src=\"" + pic + "\"></div>";
                    } else if ("text".equals(type)) {
                        return item.getHtml();
                    }
                    return "";
                })
                // 过滤空字符串
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // 使用分隔符拼接结果
        return String.join("<div data-division=1></div>", result);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void initProductByShopId(Integer shopId) {
        for (int i = 0; i < 4; i++) {
            //获取运费模板内容
            ProductSaveParam productSaveParam = new ProductSaveParam();
            productSaveParam.setProductType(ProductType.PRODUCT_TYPE_NORMAL.getCode());
            productSaveParam.setProductName("实例商品" + i);
            productSaveParam.setProductStock(1000);
            productSaveParam.setProductPrice(new BigDecimal("0.01"));
            productSaveParam.setProductDescArr(List.of());
            productSaveParam.setProductTsn(String.valueOf(i));
            productSaveParam.setShopId(getShopId());
            productSaveParam.setProductStatus(0);

            try {
                create(productSaveParam);
            } catch (Exception e) {
                throw new GlobalException("初始化商品失败");
            }
        }
    }

    @Override
    public List<ProductShippingTplListVO> getShippingTplListByShopId() {
        return shippingTplService.getShippingTplListByShopId();
    }

    @Override
    public Integer getProductStock(Integer productId, Integer skuId, boolean includeActivity) {
        Long now = StringUtils.getCurrentTime();
        if (includeActivity) {
            SeckillItem seckillItem = seckillItemMapper.selectOne(new QueryWrapper<SeckillItem>().eq("product_id", productId).eq("sku_id", skuId).ge("seckill_start_time", now).le("seckill_end_time", now));
            if (seckillItem != null) {
                return seckillItem.getSeckillStock();
            }
        }
        if (skuId != null && skuId > 0) {
            return productSkuService.getProductStock(productId, skuId);
        } else {
            Product product = this.getById(productId);
            return product.getProductStock();
        }
    }

    @Override
    public BigDecimal getProductFinalPrice(Integer productId, BigDecimal originalPrice, Integer skuId) {
        BigDecimal price = originalPrice;
        Long now = StringUtils.getCurrentTime();
        SeckillItem seckillItem = seckillItemMapper.selectOne(new QueryWrapper<SeckillItem>().eq("product_id", productId).eq("sku_id", skuId).ge("seckill_start_time", now).le("seckill_end_time", now));
        if (seckillItem != null) {
            return seckillItem.getSeckillPrice();
        }
        if (skuId > 0) {
            ProductSku productSku = productSkuService.getById(skuId);
            if (productSku != null) {
                price = productSku.getSkuPrice();
            }
        }
//        // 会员等级优惠价
//        User user = userMapper.selectById(SecurityUtils.getCurrentUserId());
//        //会员等级列表
//        List<UserRank> userRankList = userRankService.list();
//        for (UserRank userRank : userRankList) {
//            assert user != null;
//            if (Objects.equals(user.getRankId(), userRank.getRankId()) && userRank.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
//                price = originalPrice.multiply(userRank.getDiscount());
//                price = price.divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP);
//            }
//        }
        return price;
    }

    @Override
    public List<ProductListResDTO> getProductList(ProductListDTO dto) {
        // dto.setIds(getProductIds());
        return list(dto).getRecords();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void decStock(Integer productId, Integer quantity) {
        // 1. 查询商品属性
        Product product = this.getById(productId);

        // 2. 扣减商品库存
        this.lambdaUpdate().eq(Product::getProductId, productId).setDecrBy(Product::getProductStock, quantity).update();

        // 3. 增加库存扣减日志
        int number = product.getProductStock() - quantity;
        ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                .productId(product.getProductId())
                .specId(0)
                .number(number)
                .addTime(StringUtils.getCurrentTime())
                .oldNumber(product.getProductStock())
                .type(2)
                .changeNumber(quantity)
                .desc("下单扣减库存")
                .shopId(product.getShopId())
                .build();
        productInventoryLogService.save(productInventoryLog);
    }

    @Override
    public void incStock(Integer productId, Integer quantity) {
        // 1. 查询商品属性
        Product product = this.getById(productId);

        // 2. 增加商品库存
        this.lambdaUpdate().eq(Product::getProductId, productId).setIncrBy(Product::getProductStock, quantity).update();

        // 3. 增加库存增加日志
        int number = product.getProductStock() + quantity;
        ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                .productId(product.getProductId())
                .specId(0)
                .number(number)
                .addTime(StringUtils.getCurrentTime())
                .oldNumber(product.getProductStock())
                .type(1)
                .changeNumber(quantity)
                .desc("取消订单返库存")
                .shopId(product.getShopId())
                .build();
        productInventoryLogService.save(productInventoryLog);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void incSales(Integer productId, Integer quantity) {
        LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Product::getProductId, productId).setIncrBy(Product::getVirtualSales, quantity);
        this.update(updateWrapper);
    }

    /**
     * 获取随机商品
     *
     * @return String
     */
    @Override
    public String getProductIds() {
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        List<Integer> recommendedProductIds = getRecommendedProductIds(currentUserId);
        return recommendedProductIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(StrUtil.COMMA));
    }

    public List<Integer> getRecommendedProductIds(int userId) {
        Set<Integer> resultSet = new LinkedHashSet<>();

        // 1. 取 is_best = 1 的商品
        List<Integer> bestIds = this.baseMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getIsBest, 1)
                        .eq(Product::getProductStatus, ON_SALE.getCode())
                        .eq(Product::getIsDelete, 0)
                        .select(Product::getProductId)
                        .last("LIMIT 10")
        ).stream().map(Product::getProductId).toList();
        resultSet.addAll(bestIds);

        // 2. 取 is_hot = 1 的商品
        List<Integer> hotIds = this.baseMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getIsHot, 1)
                        .eq(Product::getProductStatus, ON_SALE.getCode())
                        .eq(Product::getIsDelete, 0)
                        .select(Product::getProductId)
                        .last("LIMIT 10")
        ).stream().map(Product::getProductId).toList();
        resultSet.addAll(hotIds);

        // 3. 获取用户浏览记录
        if (userId > 0) {
            User user = userMapper.selectById(userId);
            if (user != null && StrUtil.isNotBlank(user.getHistoryProductIds())) {
                List<Integer> historyIds = JSONUtil.toList(user.getHistoryProductIds(), Integer.class);
                resultSet.addAll(historyIds.stream().limit(20).toList());
            }
        }

        // 4. 如果不足 40 个，随机补全
        int need = 40 - resultSet.size();
        if (need > 0) {
            List<Integer> excludeIds = new ArrayList<>(resultSet);
            /*String idListStr = excludeIds.isEmpty() ? "" :
                    excludeIds.stream().map(String::valueOf).collect(Collectors.joining(","));*/

            // 查询随机补全
            List<Integer> randomIds = this.list(Wrappers.lambdaQuery(Product.class).eq(Product::getIsHot, 1)
                    .eq(Product::getProductStatus, ON_SALE.getCode())
                    .eq(Product::getIsDelete, 0)
                    .select(Product::getProductId)
                    .notIn(Product::getProductId, excludeIds)
                    .last("ORDER BY RAND() LIMIT " + need)).stream().map(Product::getProductId).toList();
            resultSet.addAll(randomIds);
        }

        return new ArrayList<>(resultSet).stream().limit(40).collect(Collectors.toList());
    }

    @Override
    public List<ProductLookAlsoVO> getLookAlso(ProductRelateDTO dto) {
        // 获取商品详情
        Product product = this.getById(dto.getProductId());
        // 获取商品分类下所有子分类
        // List<Integer> categoryAllChildIds = categoryService.getCategoryAllChildIds(product.getCategoryId());
        List<Integer> categoryAllChildIds = categoryService.getRelatedCategoryIds(product.getCategoryId());
        if (categoryAllChildIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 查询构造器
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Product::getProductId, Product::getProductName, Product::getProductSn, Product::getMarketPrice, Product::getPicThumb)
                .in(Product::getCategoryId, categoryAllChildIds)
                .eq(Product::getIsDelete, 0)
                .eq(Product::getProductStatus, 1);
        // 判断是什么类型
        String introType = dto.getIntro();
        if (IS_NEW.getDesc().equals(introType)) {
            queryWrapper.eq(Product::getIsNew, 1);
        } else if (IS_BEST.getDesc().equals(introType)) {
            queryWrapper.eq(Product::getIsBest, 1);
        } else {
            // 默认使用热销
            queryWrapper.eq(Product::getIsHot, 1);
        }
        queryWrapper.orderByAsc(Product::getSortOrder)
                .last("limit " + dto.getSize());

        // 获取商品信息
        List<Product> productList = this.list(queryWrapper);

        return this.getProductRelate(productList, dto.getShopId());
    }

    /**
     * 获取商品相似信息（最终价）
     *
     * @param productList 商品列表
     * @return List<ProductLookAlsoVO>
     */
    @Override
    public List<ProductLookAlsoVO> getProductRelate(List<Product> productList, Integer shopId) {
        if (CollUtil.isEmpty(productList)) {
            return Collections.emptyList();
        }
        // 获取 productId 列表
        List<Integer> list = productList.stream().map(Product::getProductId).toList();

        // 获取对应的 SKU 列表
        List<ProductSku> productSkuList = productSkuService.lambdaQuery().in(ProductSku::getProductId, list).list();

        // 将 SKU 按 productId 分组（方便快速查找第一个 SKU）
        Map<Integer, List<ProductSku>> skuMap = productSkuList.stream()
                .collect(Collectors.groupingBy(ProductSku::getProductId));

        List<ProductLookAlsoVO> result = new ArrayList<>();
        // 遍历 productList，替换 price
        for (Product p : productList) {
            int productId = p.getProductId();
            List<ProductSku> skus = skuMap.get(productId);

            int skuId = 0;
            if (skus != null && !skus.isEmpty()) {
                skuId = skus.getFirst().getSkuId();
            }

            ProductAvailabilityVO detail = this.getProductSkuDetail(productId, skuId, 0, "", shopId);
            if (detail != null) {
                ProductLookAlsoVO vo = ProductLookAlsoVO.builder()
                        .marketPrice(p.getMarketPrice().toString())
                        .productName(p.getProductName())
                        .picThumb(p.getPicThumb())
                        .price(detail.getPrice().toString())
                        .productId(p.getProductId())
                        .productSn(p.getProductSn())
                        .build();
                result.add(vo);
            }
        }

        return result;
    }

    @Override
    public RelateRankVO getRelateRank(ProductRelateDTO dto) {
        // 获取商品详情
        Product product = this.getById(dto.getProductId());
        // 判断是否为空
        if (product == null) {
            throw new GlobalException(PRODUCT_NOT_EXIST);
        }
        // 获取商品分类下所有子分类
        // List<Integer> categoryAllChildIds = categoryService.getCategoryAllChildIds(product.getCategoryId());
        List<Integer> categoryAllChildIds = categoryService.getRelatedCategoryIds(product.getCategoryId());
        // 相同价位
        List<Product> priceProductRankData = getProductRankData(dto, product, 1, categoryAllChildIds);
        // 相同品牌
        List<Product> brandProductRankData = getProductRankData(dto, product, 2, categoryAllChildIds);
        // 相同类别
        List<Product> cateProductRankData = getProductRankData(dto, product, 3, categoryAllChildIds);

        return RelateRankVO.builder()
                .price(this.getProductRelate(priceProductRankData, dto.getShopId()))
                .brand(this.getProductRelate(brandProductRankData, dto.getShopId()))
                .cate(this.getProductRelate(cateProductRankData, dto.getShopId()))
                .build();
    }

    /**
     * 获取相关数据
     *
     * @param dto                 查询构造器
     * @param product             商品详情
     * @param type                相关类型
     * @param categoryAllChildIds 分类下所有子分类
     * @return List<Product>
     */
    public List<Product> getProductRankData(ProductRelateDTO dto, Product product, int type, List<Integer> categoryAllChildIds) {
        // 查询构造器
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("product_id", dto.getProductId())
                .eq("is_delete", 0)
                .eq("product_status", 1)
                .last("limit " + dto.getRankNum());

        switch (type) {
            case 1 -> {
                if (categoryAllChildIds.isEmpty()) {
                    return Collections.emptyList();
                }
                queryWrapper.in("category_id", categoryAllChildIds)
                        .select("product_id", "product_name", "product_sn", "market_price", "pic_thumb", StrUtil.format("ABS(market_price - {}) ", product.getMarketPrice()));
                return this.list(queryWrapper);
            }
            case 2 -> {
                queryWrapper.select("product_id", "product_name", "product_sn", "market_price", "pic_thumb")
                        .eq("brand_id", product.getBrandId())
                        .orderByAsc("sort_order");
                return this.list(queryWrapper);
            }
            case 3 -> {
                queryWrapper.select("product_id", "product_name", "product_sn", "market_price", "pic_thumb")
                        .eq("category_id", product.getCategoryId())
                        .orderByAsc("sort_order");
                return this.list(queryWrapper);
            }
        }
        return Collections.emptyList();
    }

    /**
     * 获取相关品牌
     *
     * @param dto 商品相关参数
     * @return List<Brand>
     */
    @Override
    public List<Brand> getRelateBrand(ProductRelateDTO dto) {
        // 获取商品详情
        Product product = this.getById(dto.getProductId());
        // 获取商品分类下所有子分类
        // List<Integer> categoryAllChildIds = categoryService.getCategoryAllChildIds(product.getCategoryId());
        List<Integer> categoryAllChildIds = categoryService.getRelatedCategoryIds(product.getCategoryId());
        if (categoryAllChildIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询构造器
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(
                        "MAX(product_id) as product_id",
                        "MAX(product_name) as product_name",
                        "MAX(product_sn) as product_sn",
                        "MAX(market_price) as market_price",
                        "MAX(pic_thumb) as pic_thumb",
                        "brand_id"
                )
                .in("category_id", categoryAllChildIds)
                .eq("is_delete", 0)
                .eq("product_status", 1)
                .groupBy("brand_id")
                .last("limit " + dto.getSize());
        // 获取相关的品牌ID
        List<Integer> brandIds = this.list(queryWrapper).stream().map(Product::getBrandId).toList();
        if (brandIds.isEmpty()) {
            return List.of();
        }
        return brandService.list(new LambdaQueryWrapper<Brand>()
                .select(Brand::getBrandId, Brand::getBrandName, Brand::getBrandLogo, Brand::getSiteUrl, Brand::getFirstWord)
                .in(Brand::getBrandId, brandIds)
                .eq(Brand::getIsShow, 1)
                .last("limit " + dto.getSize())
                .orderByAsc(Brand::getSortOrder));
    }

    @Override
    public List<ArticleVO> getArticleList(ProductRelateDTO dto) {
        List<ProductArticle> articleList = productArticleService.getProductArticleByProductId(dto.getProductId());
        if (articleList.isEmpty()) {
            return List.of();
        }
        String articleIds = articleList.stream().map(ProductArticle::getArticleId).map(String::valueOf).collect(Collectors.joining(","));

        ArticlePageQuery articlePageQuery = new ArticlePageQuery();
        articlePageQuery.setArticleIds(articleIds);
        articlePageQuery.setPage(1);
        articlePageQuery.setSize(dto.getSize());
        articlePageQuery.setIsShow(1);
        return articleService.list(articlePageQuery).getRecords();
    }

    @Override
    public List<Category> getRelateCategory(ProductRelateDTO dto) {
        // 获取商品详情
        Product product = this.getById(dto.getProductId());
        // 查询构造器
        /*LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getIsShow, 1)
                .eq(Category::getCategoryId, product.getCategoryId())
                .select(Category::getCategoryId, Category::getCategoryName, Category::getParentId);
        List<Category> categoryList = categoryService.list(queryWrapper);
        if (categoryList.isEmpty()) {
            return List.of();
        }
        List<Category> categoryByParentId = categoryService.getCategoryByParentId(categoryList.getFirst().getParentId(), dto.getSize());*/
        List<Integer> categoryAllChildIds = categoryService.getRelatedCategoryIds(product.getCategoryId());
        if (categoryAllChildIds.isEmpty()) {
            return List.of();
        }
        List<Category> categoryList = categoryService.lambdaQuery()
                .in(Category::getCategoryId, categoryAllChildIds)
                .eq(Category::getIsShow, 1)
                .last("limit " + dto.getSize())
                .orderByAsc(Category::getSortOrder)
                .list();
        // 包一层多语言
        categoryList.forEach(category -> category.setCategoryName(translatePackage.translate(category.getCategoryName(), DataType.CATEGORY.getCode())));
        return categoryList;
    }

    @Override
    public ProductDetailVO clientDetail(String sn, Integer shopId, Integer nearestShopId) {
        int productId = 0, skuId = 0;
        if (StrUtil.isNotBlank(sn)) {
            Map<String, Integer> productBySn = getProductBySn(sn);
            productId = productBySn.get("productId");
            skuId = productBySn.get("skuId");
        }
        ProductDetailVO productDetailVO = new ProductDetailVO();
        // 获取商品详情(各种商品类型信息)
        ProductECardGroupDTO productDetail = getProductInfo(productId, true, shopId, nearestShopId);
        productDetailVO.setShopId(productDetail.getShopId());
        ProductInfoDTO productInfoDTO = new ProductInfoDTO();
        BeanUtils.copyProperties(productDetail, productInfoDTO);
        productInfoDTO.setPaidContent(JsonUtil.checkJsonType(productDetail.getPaidContent()));

        productDetailVO.setItem(productInfoDTO);

        // 判断商品是否存在
        if (productDetail.getIsDelete() == 1 || productDetail.getProductStatus() != 1) {
            throw new GlobalException(PRODUCT_NOT_EXIST);
        }
        // 获取商品图文详情
        List<ProductDescDTO> productDescArr = getProductDescArr(productDetail.getProductDesc());
        productDetailVO.setDescArr(productDescArr);
        // 获取sku列表
        List<ProductSkuDTO> productSkusByPid = productSkuService.getProductSkusByPid(productId);
        productDetailVO.setSkuList(productSkusByPid);
        // 获取相册列表
        List<ProductGallery> productGalleryList = productGalleryService.getProductGalleryList(productId);
        productDetailVO.setPicList(productGalleryList);
        List<ProductVideo> productVideo = productVideoService.lambdaQuery().eq(ProductVideo::getProductId, productId).list();
        productDetailVO.setVideoList(productVideo);
        // 获取属性列表
        ProductAttrsDTO productAttributes = productAttributesService.getProductAttributes(productId);
        productDetailVO.setAttrList(productAttributes);
        // 获取商品评论评分详情
        ProductCommentStatisticVO productCommentDetail = commentService.getProductCommentDetail(productId, shopId);
        productDetailVO.setRankDetail(productCommentDetail);
        // 获取商品秒杀信息
        List<SeckillItemInfoDTO> seckillInfo = seckillService.getSeckillInfo(productId);
        productDetailVO.setSeckillDetail(seckillInfo);
        // 获取商品服务信息
        List<Services> serviceList = getServiceList(productId);
        productDetailVO.setServiceList(serviceList);
        // 获取默认选择的属性
        List<String> selectValue = getSelectValue(skuId);
        productDetailVO.setCheckedValue(selectValue);
        // 获取商品咨询量
        int productFeedbackCount = getProductFeedbackCount(productId);
        productDetailVO.setConsultationTotal(productFeedbackCount);
        return productDetailVO;
    }

    @Override
    public List<ProductListResDTO> historyProduct(List<Integer> historyProductIds) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Product::getProductId, historyProductIds);
        //根据historyProductIds顺序排序
        queryWrapper.last("ORDER BY field(product_id, " + historyProductIds.stream().map(String::valueOf).
                collect(Collectors.joining(",")) + ") limit 20");
        //根据 historyProductIds 排序
        List<Product> productList = this.list(queryWrapper);
        // 查询商品规格
        List<ProductSkuDTO> productSkus = productSkuService.getProductSkusByProductIds(historyProductIds);
        // 将商品规格按商品 ID 分组
        Map<Integer, List<ProductSkuDTO>> productSkuMap = productSkus.stream()
                .collect(Collectors.groupingBy(ProductSkuDTO::getProductId));
        return productList.stream()
                .map(product -> {
                    ProductListResDTO dto = convertToDTO(product);
                    List<ProductSkuDTO> skus = productSkuMap.get(product.getProductId());
                    dto.setProductSku(skus);

                    // 获取最终价格
                    int skuId = 0;
                    if (skus != null && !skus.isEmpty()) {
                        skuId = skus.getFirst().getSkuId();
                    }
                    ProductAvailabilityVO detail = this.getProductSkuDetail(product.getProductId(), skuId, 0, "", null);
                    dto.setPrice(detail.getPrice());
                    return dto;
                })
                .toList();
    }


    public ProductECardGroupDTO getProductInfo(int id, boolean isDetail, Integer shopId, Integer nearestShopId) {
        // 连表查询
        MPJLambdaWrapper<Product> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.eq(Product::getProductId, id)
                .selectAll(Product.class)
                .selectAll(ECardGroup.class)
                .leftJoin(ECardGroup.class, ECardGroup::getGroupId, Product::getCardGroupId);
        ProductECardGroupDTO dto = this.selectJoinOne(ProductECardGroupDTO.class, queryWrapper);
        if (tigshopProperties.getIsO2o() == 1 && dto.getShopId() == 0) {

            ProductECardGroupDTO storeDto = new ProductECardGroupDTO();
            // O2O 模式下的优先级门店查询
            Integer[] shopIds = {shopId == null ? 0 : shopId, nearestShopId, null}; // null 表示查任意门店（limit 1）
            for (Integer sid : shopIds) {
                MPJLambdaWrapper<StoreProduct> wrapper = buildStoreProductQuery(id, sid);
                wrapper.leftJoin(Product.class, Product::getProductId, StoreProduct::getProductId);
                wrapper.selectAll(Product.class);
                storeDto = storeProductService.selectJoinOne(ProductECardGroupDTO.class, wrapper);
                if (storeDto != null) {
                    storeDto.setStoreProductStatus(1);
                    dto = storeDto;
                    dto.setShopId(storeDto.getShopId());
                    ConfigPO storeAssignProductName = configService.getConfigByCode(SettingsEnum.STORE_ASSIGN_PRODUCT_NAME.getBizCode());
                    if ("1".equals(storeAssignProductName.getBizVal())) {
                        dto.setProductName(storeDto.getProductName());
                    }
                    ConfigPO storeAssignProductPrice = configService.getConfigByCode(SettingsEnum.STORE_ASSIGN_PRODUCT_PRICE.getBizCode());
                    if ("1".equals(storeAssignProductPrice.getBizVal())) {
                        dto.setProductPrice(storeDto.getProductPrice());
                        dto.setStoreProductId(storeDto.getStoreProductId());
                    }
                    ConfigPO storeUseSoloProductStock = configService.getConfigByCode(SettingsEnum.STORE_USE_SOLO_PRODUCT_STOCK.getBizCode());
                    if ("1".equals(storeUseSoloProductStock.getBizVal())) {
                        dto.setProductStock(storeDto.getProductStock());
                    }
                    break;
                }
            }
            // 如果所有门店都查不到，再查 Product 表
            if (storeDto == null && dto != null) {
                dto.setStoreProductStatus(0); // 标记下架或无效
            }
        }

        if (dto == null) {
            return new ProductECardGroupDTO(); // 确保返回非 null
        }

        // 补充业务逻辑
        isSeckill(id, dto);        // 判断是否秒杀
        isBuy(id, dto);            // 判断付费内容
        updateStockByECard(dto);   // 更新卡密库存

        // 浏览记录
        int currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId > 0 && isDetail) {
            historyProductRecord(id, currentUserId);
        }

        return dto;
    }

    /**
     * 构建连表查询条件
     */
    private MPJLambdaWrapper<StoreProduct> buildStoreProductQuery(int productId, Integer shopId) {
        MPJLambdaWrapper<StoreProduct> wrapper = new MPJLambdaWrapper<>();
        wrapper.eq(StoreProduct::getProductId, productId)
                .selectAll(StoreProduct.class)
                .selectAll(ECardGroup.class)
                .leftJoin(ECardGroup.class, ECardGroup::getGroupId, StoreProduct::getCardGroupId);

        if (shopId != null) {
            wrapper.eq(StoreProduct::getShopId, shopId);
        }
        wrapper.eq(StoreProduct::getProductStatus, Constants.YES);
        wrapper.last("limit 1"); // 没有 shopId 时只取一个
        return wrapper;
    }


    /**
     * 获取商品详情（前台详情用法）
     *
     * @param id 商品ID
     * @return ProductECardGroupDTO
     */
    public ProductECardGroupDTO getProductDetail(int id, boolean isDetail, Integer shopId) {

        // 连表查询
        MPJLambdaWrapper<Product> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.eq(Product::getProductId, id)
                .selectAll(Product.class)
                .selectAll(ECardGroup.class)
                .leftJoin(ECardGroup.class, ECardGroup::getGroupId, Product::getCardGroupId);
        ProductECardGroupDTO dto = this.selectJoinOne(ProductECardGroupDTO.class, queryWrapper);
        if (dto == null) {
            return new ProductECardGroupDTO();
        }
        if (tigshopProperties.getIsO2o() == 1 && shopId != null && shopId != -1 && !Objects.equals(dto.getShopId(), shopId)) {
            // 连表查询
            MPJLambdaWrapper<StoreProduct> storeQueryWrapper = new MPJLambdaWrapper<>();
            storeQueryWrapper.eq(StoreProduct::getProductId, id)
                    .eq(StoreProduct::getShopId, shopId)
                    .selectAll(StoreProduct.class)
                    .selectAll(ECardGroup.class)
                    .leftJoin(ECardGroup.class, ECardGroup::getGroupId, StoreProduct::getCardGroupId);
            storeQueryWrapper.leftJoin(Product.class, Product::getProductId, StoreProduct::getProductId);
            storeQueryWrapper.selectAll(Product.class);
            ProductECardGroupDTO storeDto = storeProductService.selectJoinOne(ProductECardGroupDTO.class, storeQueryWrapper);

            if (storeDto != null) {
                ConfigPO storeAssignProductName = configService.getConfigByCode(SettingsEnum.STORE_ASSIGN_PRODUCT_NAME.getBizCode());
                if ("1".equals(storeAssignProductName.getBizVal())) {
                    dto.setProductName(storeDto.getProductName());
                }
                ConfigPO storeAssignProductPrice = configService.getConfigByCode(SettingsEnum.STORE_ASSIGN_PRODUCT_PRICE.getBizCode());
                if ("1".equals(storeAssignProductPrice.getBizVal())) {
                    dto.setProductPrice(storeDto.getProductPrice());
                    dto.setStoreProductId(storeDto.getStoreProductId());
                }
                ConfigPO storeUseSoloProductStock = configService.getConfigByCode(SettingsEnum.STORE_USE_SOLO_PRODUCT_STOCK.getBizCode());
                if ("1".equals(storeUseSoloProductStock.getBizVal())) {
                    dto.setProductStock(storeDto.getProductStock());
                }
            }
        }

        // 判断商品是否参与秒杀
        isSeckill(id, dto);
        // 付费内容商品在用户购买过之后才可以显示
        isBuy(id, dto);
        // 卡密商品根据卡券修改库存
        updateStockByECard(dto);
        // 添加浏览记录
        int currentUserId = SecurityUtils.getCurrentUserId();
        if (SecurityUtils.getCurrentUserId() > 0 && isDetail) {
            historyProductRecord(id, currentUserId);
        }
        return dto;
    }

    /**
     * 添加浏览记录
     *
     * @param productId 商品id
     * @param userId    用户id
     */
    public void historyProductRecord(int productId, int userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 使用 LinkedList 提升头部操作性能
        LinkedList<Integer> historyProductIds = new LinkedList<>();

        // 如果数据库字段不为空则填充数据
        if (StrUtil.isNotBlank(user.getHistoryProductIds())) {
            List<Integer> temp = StringUtils.str2IntList(user.getHistoryProductIds());
            if (temp != null) {
                historyProductIds.addAll(temp);
            }
        }

        Integer target = productId;
        historyProductIds.remove(target);
        historyProductIds.addFirst(target);

        // 优化去重方式（保持顺序）
        LinkedHashSet<Integer> uniqueSet = new LinkedHashSet<>(historyProductIds);
        historyProductIds.clear();
        historyProductIds.addAll(uniqueSet);

        // 限制历史记录数量（可选）
        while (historyProductIds.size() > 20) {
            historyProductIds.removeLast();
        }

        user.setHistoryProductIds(JsonUtil.toJson(historyProductIds));
        userMapper.updateById(user);
    }

    /**
     * 卡密商品根据卡券修改库存
     *
     * @param dto 商品详情
     */
    public void updateStockByECard(ProductECardGroupDTO dto) {
        Integer productType = dto.getProductType();
        if (productType != null && productType == PRODUCT_TYPE_CARD.getCode()) {
            LambdaQueryWrapper<ECard> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ECard::getGroupId, dto.getCardGroupId())
                    .eq(ECard::getIsUse, 0);
            long count = eCardService.count(queryWrapper);
            dto.setProductStock((int) count);
        }
    }

    /**
     * 判断商品是否付费
     *
     * @param id  商品ID
     * @param dto 商品详情
     */
    public void isBuy(int id, ProductECardGroupDTO dto) {
        // 付费内容商品在用户购买过之后才可以显示
        int isBuy = 0;
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId > 0) {
            MPJLambdaWrapper<Order> queryWrapper = new MPJLambdaWrapper<>();
            queryWrapper.leftJoin(OrderItem.class, OrderItem::getOrderId, Order::getOrderId)
                    .eq(Order::getUserId, currentUserId)
                    .eq(OrderItem::getProductId, id)
                    .eq(Order::getPayStatus, PAID.getCode())
                    .eq(Order::getIsDel, 0);

            Long count = orderMapper.selectJoinCount(queryWrapper);
            if (count > 0) {
                isBuy = 1;
            }
        }

        if (isBuy == 0) {
            dto.setPaidContent("");
        }
        dto.setIsBuy(isBuy);
    }

    /**
     * 判断商品是否付费
     */
    @Override
    public Integer isBuy(int id) {
        // 付费内容商品在用户购买过之后才可以显示
        int isBuy = 0;
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId > 0) {
            MPJLambdaWrapper<Order> queryWrapper = new MPJLambdaWrapper<>();
            queryWrapper.leftJoin(OrderItem.class, OrderItem::getOrderId, Order::getOrderId)
                    .eq(Order::getUserId, currentUserId)
                    .eq(OrderItem::getProductId, id)
                    .eq(Order::getPayStatus, PAID.getCode())
                    .eq(Order::getIsDel, 0);

            Long count = orderMapper.selectJoinCount(queryWrapper);
            if (count > 0) {
                isBuy = 1;
            }
        }
        return isBuy;
    }

    /**
     * 判断商品是否参与秒杀
     *
     * @param id  商品ID
     * @param dto 商品信息
     */
    public void isSeckill(int id, ProductECardGroupDTO dto) {
        LambdaQueryWrapper<Seckill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Seckill::getProductId, id)
                .le(Seckill::getSeckillStartTime, StringUtils.getCurrentTime())
                .ge(Seckill::getSeckillEndTime, StringUtils.getCurrentTime());
        long count = seckillService.count(queryWrapper);
        if (count > 0) {
            dto.setIsSeckill(1);
        } else {
            dto.setIsSeckill(0);
        }
    }

    /**
     * 根据商品编号获取商品信息
     *
     * @param sn 商品编号
     * @return Map<String, Integer>
     */
    public Map<String, Integer> getProductBySn(String sn) {
        // 定义变量
        int productId = 0, skuId = 0;
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getProductSn, sn);
        // 获取商品信息
        Product product = this.getOne(queryWrapper);
        if (product != null) {
            productId = product.getProductId();
            return Map.of("productId", productId, "skuId", skuId);
        }
        // 没有商品信息的话就从sku获得
        ProductSku productSkusBySn = productSkuService.getProductSkusBySn(sn);
        if (productSkusBySn != null) {
            productId = productSkusBySn.getProductId();
            skuId = productSkusBySn.getSkuId();
            return Map.of("productId", productId, "skuId", skuId);
        }
        return Map.of("productId", productId, "skuId", skuId);
    }

    /**
     * 获取商品服务
     *
     * @param id 商品ID
     * @return List<Services>
     */
    public List<Services> getServiceList(int id) {
        Product product = this.getById(id);
        if (product == null) {
            throw new GlobalException(PRODUCT_NOT_EXIST);
        }
        // 商品服务，需要转成集合
        String productServiceIds = product.getProductServiceIds();
        if (StrUtil.isEmpty(productServiceIds)) {
            return List.of();
        }
        LambdaQueryWrapper<Services> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(!StringUtils.str2IntList(productServiceIds).isEmpty(),
                Services::getProductServiceId, StringUtils.str2IntList(productServiceIds));
        queryWrapper
                .eq(Services::getDefaultOn, 1)
                .orderByAsc(Services::getSortOrder);
        return servicesService.list(queryWrapper);
    }

    /**
     * 获取默认选择的属性
     *
     * @param skuId 商品规格ID
     * @return List<String>
     */
    public List<String> getSelectValue(int skuId) {
        if (skuId == 0) {
            return List.of();
        }
        ProductSkuDTO productSkuDTO = productSkuService.convertToDTO(productSkuService.getById(skuId));
        if (productSkuDTO == null) {
            return List.of();
        }
        List<ProductSkuDTO.SkuData> skuDataList = productSkuDTO.getSkuData();
        if (skuDataList.isEmpty()) {
            return List.of();
        }
        // 循环拼接字符串
        List<String> list = new ArrayList<>();
        for (ProductSkuDTO.SkuData skuData : skuDataList) {
            list.add(StrUtil.format("{}:{}", skuData.getName(), skuData.getValue()));
        }
        return list;
    }

    /**
     * 获取商品反馈总数
     *
     * @param productId 商品ID
     * @return int
     */
    public int getProductFeedbackCount(int productId) {
        LambdaQueryWrapper<Feedback> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Feedback::getProductId, productId);
        return Math.toIntExact(feedbackMapper.selectCount(queryWrapper));
    }

    @Override
    public Boolean isCollect(int productId, Integer shopId) {
        Product product = this.getById(productId);
        if (product == null) {
            return false;
        }
        LambdaQueryWrapper<CollectProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CollectProduct::getProductId, productId)
                .eq(shopId != null, CollectProduct::getShopId, shopId)
                .eq(CollectProduct::getUserId, SecurityUtils.getCurrentUserId());
        long count = collectProductMapper.selectCount(queryWrapper);
        return count > 0;
    }

    @Override
    public List<CategoryParentTreeDTO> parentTree(Integer categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        List<CategoryParentTreeDTO> categoryTrees = new ArrayList<>();

        // 防循环引用
        Set<Integer> visitedIds = new HashSet<>();
        // 最大循环次数限制为4次
        final int MAX_LOOPS = 4;
        // 当前循环计数
        int loopCount = 0;

        while (categoryId != 0 && loopCount < MAX_LOOPS) {
            // 检测循环引用（即使没到4次，出现循环也立即终止）
            if (visitedIds.contains(categoryId)) {
                log.error("检测到分类循环引用，提前终止。当前ID: {}", categoryId);
                break;
            }
            visitedIds.add(categoryId);

            Category category = categoryService.getById(categoryId);
            if (category == null) {
                break;
            }
            Integer parentId = category.getParentId();
            List<Category> alikeParentList = categoryService.list(
                    new LambdaQueryWrapper<Category>()
                            .select(Category::getCategoryId, Category::getCategoryName, Category::getParentId)
                            .eq(Category::getParentId, parentId)
                            .eq(Category::getIsShow, 1)
            );
            categoryTrees.add(new CategoryParentTreeDTO(category, alikeParentList));
            categoryId = parentId;
            // 每次循环计数+1
            loopCount++;
        }
        // 记录超过最大次数的情况（便于排查是否有过深层级）
        if (loopCount >= MAX_LOOPS) {
            log.error("已达到最大循环次数({}次)，终止遍历。最后处理的分类ID: {}", MAX_LOOPS, categoryId);
        }
        categoryTrees.sort(Comparator.comparingInt(CategoryParentTreeDTO::getParentId));
        return categoryTrees;
    }

    @Override
    public List<CouponDetailVO> getCouponList(Integer productId) {
        // 获取优惠券详情
        ProductDetailDTO productDetail = detail(productId);

        // 获取商品优惠券列表
        List<CouponDetailVO> productCouponList = couponService.getProductCouponList(productId, productDetail.getShopId());

        // 获取用户优惠券列表
        UserCouponListDTO userCouponListDTO = new UserCouponListDTO();
        userCouponListDTO.setPage(1);
        userCouponListDTO.setSize(10000);
        userCouponListDTO.setUsedTime(0L);
        List<UserCouponVO> userCouponList = userCouponService.list(userCouponListDTO).getRecords();
        if (userCouponList == null) {
            return productCouponList;
        }
        // 使用Set提升查找效率，避免List.contains的低效操作
        Set<Integer> userCouponIdSet = userCouponList.stream()
                .map(UserCouponVO::getCouponId)
                .collect(Collectors.toSet());

        // 分开存储需要更新的优惠券和剩余的优惠券
        List<CouponDetailVO> couponList = new ArrayList<>();
        Iterator<CouponDetailVO> iterator = productCouponList.iterator();
        while (iterator.hasNext()) {
            CouponDetailVO productCoupon = iterator.next();
            if (userCouponIdSet.contains(productCoupon.getCouponId()) && productCoupon.getLimitNum() > 0) {
                // 如果用户已拥有该优惠券，设置为已领取
                productCoupon.setIsReceive(1);
                couponList.add(productCoupon);
                // 移除已处理的优惠券
                iterator.remove();
            } else {
                // 如果用户未拥有该优惠券，设置为未领取
                productCoupon.setIsReceive(0);
            }
        }

        // 将已领取的优惠券加入到原列表中
        productCouponList.addAll(couponList);

        return productCouponList;
    }

    private BigDecimal getProductPrice(Integer productId, Integer skuId, List<UserRank> userRankList, Integer rankId, Integer shopId) {
        ProductECardGroupDTO productDetail = this.getProductDetail(productId, false, shopId);
        BigDecimal productPrice = productDetail.getProductPrice();

        ProductSkuDTO productSkuDetail = skuId != null ? productSkuService.getDetail(skuId, productDetail.getStoreProductId()) : null;
        if (productSkuDetail != null) {
            productPrice = new BigDecimal(productSkuDetail.getSkuPrice());
        }
        Long count = pointsExchangeMapper.selectCount(Wrappers.lambdaQuery(PointsExchange.class).eq(PointsExchange::getProductId, productId));
        if (count < 1) {
            if (SecurityUtils.getCurrentUserId() > 0) {
                for (UserRank userRank : userRankList) {
                    if (userRank.getRankId().equals(rankId) && userRank.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                        productPrice = productPrice.multiply(userRank.getDiscount()).divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
                    }
                }
            }
        }
        BigDecimal newPrice = null;

        List<ProductSaveParam> products = new ArrayList<>();

        ProductSaveParam productSaveParam = new ProductSaveParam();
        productSaveParam.setProductId(productId);
        skuId = skuId != null ? skuId : 0;
        productSaveParam.setSkuId(skuId);
        products.add(productSaveParam);

        Map<Integer, ProductSaveParam> promotions = getProductsPromotion(products, productDetail.getShopId(), "detail");

        List<PromotionVO> promotionListVo = promotions.get(skuId).getActivityInfo();
        Iterator<PromotionVO> iterator = promotionListVo.iterator();
        while (iterator.hasNext()) {
            PromotionVO promotionVO = iterator.next();
            if (promotionVO.getType() == 1) {
                SeckillItem seckillItem = seckillItemMapper.selectOne(new LambdaQueryWrapper<SeckillItem>()
                        .eq(SeckillItem::getSeckillId, promotionVO.getRelationId())
                        .eq(SeckillItem::getSkuId, skuId)
                        .last("limit 1"));
                newPrice = seckillItem.getSeckillPrice();
                promotionVO.getData().put("item", seckillItem);
                // 这里暂时先这样处理，后续应该把所有的驼峰字段改为下划线字段
                promotionVO.getData().put("seckillStartTime", TigUtils.handelTime(seckillItem.getSeckillStartTime()));
                promotionVO.getData().put("seckillEndTime", TigUtils.handelTime(seckillItem.getSeckillEndTime()));
            } else if (promotionVO.getType() == 6) {
                TimeDiscountItem discountItem = timeoutDiscountItemMapper.selectOne(new LambdaQueryWrapper<TimeDiscountItem>()
                        .eq(TimeDiscountItem::getDiscountId, promotionVO.getData().get("discountId"))
                        .eq(TimeDiscountItem::getProductId, productId)
                        .last("limit 1"));
                newPrice = timeDiscountService.getTimeDiscountPrice(productPrice, skuId, discountItem);
                TimeDiscountDTO value = new TimeDiscountDTO(discountItem);
                value.setDiscountPrice(productPrice.subtract(newPrice).setScale(2, RoundingMode.HALF_UP));
                promotionVO.getData().put("item", value);
            } else if (promotionVO.getType() == 2 && promotionVO.getIsDelete() == 1) {
                // 使用 Iterator 的 remove 方法安全删除
                iterator.remove();
            }
        }

        productPrice = newPrice == null ? productPrice : newPrice;

        return productPrice.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public ProductAvailabilityVO getProductSkuDetail(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId) {
        ProductAvailabilityVO productAvailabilityVO = new ProductAvailabilityVO();
        ProductECardGroupDTO productDetail = this.getProductDetail(productId, false, shopId);
        Integer productStock = productDetail.getProductStock();
        BigDecimal productPrice = productDetail.getProductPrice();
        ProductSkuDTO productSkuDetail = skuId != null ? productSkuService.getDetail(skuId, productDetail.getStoreProductId()) : null;
        List<ProductSkuDTO.SkuData> skuData = new ArrayList<>();
        if (productSkuDetail != null) {
            productStock = productSkuDetail.getSkuStock();
            productPrice = new BigDecimal(productSkuDetail.getSkuPrice());
            skuData = productSkuDetail.getSkuData();
//            String attrPicThumb = getProductAttribute(productSkuDetail.getProductId(), skuData.getFirst().getName(), skuData.getFirst().getValue());
        }
        Long count = pointsExchangeMapper.selectCount(Wrappers.lambdaQuery(PointsExchange.class).eq(PointsExchange::getProductId, productId));
        if (count < 1) {
            if (SecurityUtils.getCurrentUserId() > 0) {
                List<UserRank> userRankList = userRankService.getUserRank();
                Integer rankId = userMapper.selectById(SecurityUtils.getCurrentUserId()).getRankId();
                for (UserRank userRank : userRankList) {
                    if (userRank.getRankId().equals(rankId) && userRank.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                        productPrice = productPrice.multiply(userRank.getDiscount()).divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
                    }
                }
            }
        }

        BigDecimal attrPrice = BigDecimal.ZERO;
        BigDecimal newPrice = null;
        if (StrUtil.isNotBlank(extraAttrIds)) {
            List<ProductAttribute> productAttributeList = getProductAttributeList(StringUtils.str2IntList(extraAttrIds));
            attrPrice = productAttributeList.stream().map(ProductAttribute::getAttrPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        List<PromotionVO> promotionListVo = new ArrayList<>();
        if (excludeActivity == 0) {
            List<ProductSaveParam> products = new ArrayList<>();

            ProductSaveParam productSaveParam = new ProductSaveParam();
            productSaveParam.setProductId(productId);
            skuId = skuId != null ? skuId : 0;
            productSaveParam.setSkuId(skuId);
            products.add(productSaveParam);

            Map<Integer, ProductSaveParam> promotions = getProductsPromotion(products, productDetail.getShopId(), "detail");

            promotionListVo = promotions.get(skuId).getActivityInfo();
            Iterator<PromotionVO> iterator = promotionListVo.iterator();
            while (iterator.hasNext()) {
                PromotionVO promotionVO = iterator.next();
                if (promotionVO.getType() == 1) {
                    SeckillItem seckillItem = seckillItemMapper.selectOne(new LambdaQueryWrapper<SeckillItem>()
                            .eq(SeckillItem::getSeckillId, promotionVO.getRelationId())
                            .eq(SeckillItem::getSkuId, skuId)
                            .last("limit 1"));
                    newPrice = seckillItem.getSeckillPrice();
                    productStock = seckillItem.getSeckillStock();
                    promotionVO.getData().put("item", seckillItem);
                    // 这里暂时先这样处理，后续应该把所有的驼峰字段改为下划线字段
                    promotionVO.getData().put("seckillStartTime", TigUtils.handelTime(seckillItem.getSeckillStartTime()));
                    promotionVO.getData().put("seckillEndTime", TigUtils.handelTime(seckillItem.getSeckillEndTime()));
                } else if (promotionVO.getType() == 6) {
                    TimeDiscountItem discountItem = timeoutDiscountItemMapper.selectOne(new LambdaQueryWrapper<TimeDiscountItem>()
                            .eq(TimeDiscountItem::getDiscountId, promotionVO.getData().get("discountId"))
                            .eq(TimeDiscountItem::getProductId, productId)
                            .last("limit 1"));
                    newPrice = timeDiscountService.getTimeDiscountPrice(productPrice, skuId, discountItem);
                    TimeDiscountDTO value = new TimeDiscountDTO(discountItem);
                    value.setDiscountPrice(productPrice.subtract(newPrice).setScale(2, RoundingMode.HALF_UP));
                    promotionVO.getData().put("item", value);
                } else if (promotionVO.getType() == 2 && promotionVO.getIsDelete() == 1) {
                    // 使用 Iterator 的 remove 方法安全删除
                    iterator.remove();
                }
            }

        }
        productPrice = productPrice.add(attrPrice);
        BigDecimal originPrice = newPrice == null ? productPrice : null;
        productPrice = newPrice == null ? productPrice : newPrice;

        productAvailabilityVO.setId(productId);
        productAvailabilityVO.setPromotion(promotionListVo);
        productAvailabilityVO.setSku(productSkuDetail);
        productAvailabilityVO.setData(skuData);
        productAvailabilityVO.setPrice(productPrice.setScale(2, RoundingMode.HALF_UP));
        productAvailabilityVO.setStock(productStock);
        productAvailabilityVO.setProductId(productId);
        productAvailabilityVO.setOriginPrice(originPrice);
        return productAvailabilityVO;
    }

    @Override
    public ProductAvailabilityVO getProductFinalPrice(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId) {
        ProductAvailabilityVO productAvailabilityVO = new ProductAvailabilityVO();
        ProductECardGroupDTO productDetail = this.getProductDetail(productId, false, shopId);
        Integer productStock = productDetail.getProductStock();
        BigDecimal productPrice = productDetail.getProductPrice();
        ProductSkuDTO productSkuDetail = skuId != null ? productSkuService.getDetail(skuId, productDetail.getStoreProductId()) : null;
        List<ProductSkuDTO.SkuData> skuData = new ArrayList<>();
        if (productSkuDetail != null) {
            productStock = productSkuDetail.getSkuStock();
            productPrice = new BigDecimal(productSkuDetail.getSkuPrice());
            skuData = productSkuDetail.getSkuData();
//            String attrPicThumb = getProductAttribute(productSkuDetail.getProductId(), skuData.getFirst().getName(), skuData.getFirst().getValue());
        }
        Long count = pointsExchangeMapper.selectCount(Wrappers.lambdaQuery(PointsExchange.class).eq(PointsExchange::getProductId, productId));
        if (count < 1) {
            if (SecurityUtils.getCurrentUserId() > 0) {
                List<UserRank> userRankList = userRankService.getUserRank();
                Integer rankId = userMapper.selectById(SecurityUtils.getCurrentUserId()).getRankId();
                for (UserRank userRank : userRankList) {
                    if (userRank.getRankId().equals(rankId) && userRank.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                        productPrice = productPrice.multiply(userRank.getDiscount()).divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
                    }
                }
            }
        }

        BigDecimal attrPrice = BigDecimal.ZERO;
        BigDecimal newPrice = null;
        if (StrUtil.isNotBlank(extraAttrIds)) {
            List<ProductAttribute> productAttributeList = getProductAttributeList(StringUtils.str2IntList(extraAttrIds));
            attrPrice = productAttributeList.stream().map(ProductAttribute::getAttrPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        List<PromotionVO> promotionListVo = new ArrayList<>();
        if (excludeActivity == 0) {
            List<ProductSaveParam> products = new ArrayList<>();

            ProductSaveParam productSaveParam = new ProductSaveParam();
            productSaveParam.setProductId(productId);
            skuId = skuId != null ? skuId : 0;
            productSaveParam.setSkuId(skuId);
            products.add(productSaveParam);

            Map<Integer, ProductSaveParam> promotions = getProductsPromotion(products, productDetail.getShopId(), "detail");

            promotionListVo = promotions.get(skuId).getActivityInfo();
            Iterator<PromotionVO> iterator = promotionListVo.iterator();
            while (iterator.hasNext()) {
                PromotionVO promotionVO = iterator.next();
                if (promotionVO.getType() == 1) {
                    SeckillItem seckillItem = seckillItemMapper.selectOne(new LambdaQueryWrapper<SeckillItem>()
                            .eq(SeckillItem::getSeckillId, promotionVO.getRelationId())
                            .eq(SeckillItem::getSkuId, skuId)
                            .last("limit 1"));
                    if (seckillItem != null) {
                        newPrice = seckillItem.getSeckillPrice();
                        productStock = seckillItem.getSeckillStock();
                        promotionVO.getData().put("item", seckillItem);
                        // 这里暂时先这样处理，后续应该把所有的驼峰字段改为下划线字段
                        promotionVO.getData().put("seckillStartTime", TigUtils.handelTime(seckillItem.getSeckillStartTime()));
                        promotionVO.getData().put("seckillEndTime", TigUtils.handelTime(seckillItem.getSeckillEndTime()));
                    }
                } else if (promotionVO.getType() == 2 && promotionVO.getIsDelete() == 1) {
                    // 使用 Iterator 的 remove 方法安全删除
                    iterator.remove();
                }
            }

        }
        productPrice = productPrice.add(attrPrice);
        BigDecimal originPrice = newPrice == null ? productPrice : null;
        productPrice = newPrice == null ? productPrice : newPrice;

        productAvailabilityVO.setId(productId);
        productAvailabilityVO.setPromotion(promotionListVo);
        productAvailabilityVO.setSku(productSkuDetail);
        productAvailabilityVO.setData(skuData);
        productAvailabilityVO.setPrice(productPrice.setScale(2, RoundingMode.HALF_UP));
        productAvailabilityVO.setStock(productStock);
        productAvailabilityVO.setProductId(productId);
        productAvailabilityVO.setOriginPrice(originPrice);
        return productAvailabilityVO;
    }

    /**
     * 获取商品属性的图片
     *
     * @param productId 商品ID
     * @param attrName  属性名
     * @param attrValue 属性值
     * @return String
     */
    public String getProductAttribute(int productId, String attrName, String attrValue) {
        LambdaQueryWrapper<ProductAttribute> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductAttribute::getProductId, productId)
                .eq(ProductAttribute::getAttrName, attrName)
                .eq(ProductAttribute::getAttrValue, attrValue);
        List<ProductAttribute> list = productAttributesService.list(queryWrapper);
        return list.getFirst().getAttrPicThumb();
    }

    /**
     * 获取商品附加规格
     *
     * @param productIds 商品ID集合
     * @return List<ProductAttribute>
     */
    public List<ProductAttribute> getProductAttributeList(List<Integer> productIds) {
        LambdaQueryWrapper<ProductAttribute> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProductAttribute::getAttributesId, productIds)
                .select(ProductAttribute::getAttributesId, ProductAttribute::getAttrName, ProductAttribute::getAttrValue, ProductAttribute::getAttrPrice);
        return productAttributesService.list(queryWrapper);
    }

    /**
     * 获得商品的优惠信息,列表使用简化优惠版本
     *
     * @param products      商品信息
     * @param shopId        店铺ID
     * @param promotionFrom 获取促销信息的来源
     * @return List<ProductDTO>
     */
    @Override
    public Map<Integer, ProductSaveParam> getProductsPromotion(List<ProductSaveParam> products, Integer shopId, String promotionFrom) {
        promotionFrom = (promotionFrom == null) ? "list" : promotionFrom;
        Map<Integer, ProductSaveParam> productPromotion = new HashMap<>();

        // 获取所有可用的促销活动
        List<PromotionVO> allAvailablePromotion = promotionService.getAllAvailablePromotion(shopId);

        // 预加载所有赠品数据，避免重复查询数据库
        Map<Integer, ProductGift> giftCache = new HashMap<>();

        for (ProductSaveParam product : products) {
            int filedId = switch (promotionFrom) {
                case "list" -> product.getProductId();
                case "cart" -> product.getCartId();
                case "detail" -> product.getSkuId();
                default -> 0;
            };

            product.setShopId(shopId);
            productPromotion.put(filedId, product);

            List<PromotionVO> promotionList = new ArrayList<>();

            for (PromotionVO promotion : allAvailablePromotion) {
                if (checkPromotionIsAvailable(promotion, product)) {
                    promotion.setData(realPromotion(promotion.getType(), promotion.getRelationId()));
                    // 处理时间
                    promotion.getData().put("startTime", promotion.getStartTime());
                    promotion.getData().put("endTime", promotion.getEndTime());
                    if ("list".equals(promotionFrom)) {
                        if (promotion.getIsDelete() == 1) {
                            continue;
                        }
                    }

                    promotionList.add(promotion);

                    if ("list".equals(promotionFrom) && promotion.getType() != 6) {
                        break;
                    }

                    if (promotion.getType() == 5) {
                        Map<String, Object> data = promotion.getData();
                        String string = data.get("promotionTypeData").toString();
                        List<PromotionTypeDTO> gitDataList = JsonUtil.fromJson(string, new TypeReference<>() {
                        });

                        // 处理赠品
                        gitDataList.forEach(promotionTypeDTO -> {
                            int giftId = promotionTypeDTO.getGiftId();
                            ProductGift productGift = giftCache.computeIfAbsent(giftId, productGiftService::getById);
                            ProductGiftVO productGiftVO = new ProductGiftVO();
                            BeanUtils.copyProperties(productGift, productGiftVO);
                            productGiftVO.setProductInfo(getById(productGift.getProductId()));
                            promotionTypeDTO.setGift(productGiftVO);
                        });

                        promotion.setData(Map.of("promotionTypeData", gitDataList));
                        promotion.setData(Map.of("promotionDesc", gitDataList));
                    }
                }
            }

            product.setActivityInfo(promotionList);

        }
        return productPromotion;
    }

    @Override
    public List<Product> getRelatedList(Integer productId) {
        Product product = getById(productId);
        if (product == null) {
            return List.of();
        }
        List<Integer> productIds = JsonUtil.jsonToList(product.getProductRelated(), Integer.class);
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<Product>()
                .in(Product::getProductId, productIds)
                .select(Product::getProductId, Product::getProductName, Product::getProductSn, Product::getProductTsn, Product::getProductPrice, Product::getProductStock, Product::getProductWeight, Product::getProductDesc, Product::getPicUrl, Product::getPicThumb, Product::getPicOriginal)
                .last("limit 10"));
    }

    @Override
    public void priceInquiry(ProductPriceInquiryDTO productPriceInquiryDTO) {
        Product product = getById(productPriceInquiryDTO.getProductId());
        if (product == null) {
            throw new GlobalException(translatePackage.translate("商品不存在"));
        }
        PriceInquiry priceInquiry = new PriceInquiry();
        priceInquiry.setProductId(productPriceInquiryDTO.getProductId());
        priceInquiry.setMobile(productPriceInquiryDTO.getMobile());
        priceInquiry.setContent(productPriceInquiryDTO.getContent());
        priceInquiry.setShopId(product.getShopId());
        priceInquiry.setCreateTime(StringUtils.getCurrentTime());
        priceInquiryMapper.insert(priceInquiry);
    }

    /**
     * 检查活动是否可用
     *
     * @param promotion 活动
     * @param dto       商品
     * @return Boolean
     */
    public Boolean checkPromotionIsAvailable(PromotionVO promotion, ProductSaveParam dto) {

        Integer userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);
        ProductPromotion productPromotion = productPromotionService.getById(promotion.getRelationId());
        if (user != null && productPromotion != null) {
            String limitUserRank = productPromotion.getLimitUserRank();
            List<Integer> rankIds = StrUtil.isNotBlank(limitUserRank)
                    ? JsonUtil.jsonToList(limitUserRank, Integer.class)
                    : List.of();

            if (CollUtil.isNotEmpty(rankIds) && !rankIds.contains(user.getRankId())) {
                return false;
            }
        }

        Integer productId = dto.getProductId();

        if (productId != null) {
            if (promotion.getRange().equals(PROMOTION_RANGE_PRODUCT.getCode()) && !promotion.getRangeData().contains(productId)) {
                return false;
            } else if (promotion.getRange().equals(PROMOTION_RANGE_EXCLUDE_PRODUCT.getCode()) && promotion.getRangeData().contains(productId)) {
                return false;
            }
            if (dto.getShopId() == null) {
                dto.setShopId(this.getById(dto.getProductId()).getShopId());
            }
        }

        Integer skuId = dto.getSkuId();
        if (skuId != null && skuId > 0) {
            List<Integer> skuIds = promotion.getSkuIds();
            if (CollUtil.isNotEmpty(skuIds) && !skuIds.contains(skuId)) {
                return false;
            }
        }

        return Objects.equals(dto.getShopId(), promotion.getShopId());
    }

    /**
     * 获取真实活动详情
     *
     * @param type       活动类型
     * @param relationId 关联ID
     * @return Map<Integer, Object>
     */
    public Map<String, Object> realPromotion(Integer type, int relationId) {
        switch (type.toString()) {
            case "1":
                return JsonUtil.fromJson(JsonUtil.toJson(seckillService.getById(relationId)), JSONObject.class);
            case "2":
                Coupon coupon = couponService.getById(relationId);
                JSONObject couponJson = JsonUtil.fromJson(JsonUtil.toJson(coupon), JSONObject.class);
                couponJson.set("promotionDesc", couponService.getCouponPromotionDesc(coupon));
                return couponJson;
            case "3":
            case "4":
            case "5":
                return JsonUtil.fromJson(JsonUtil.toJson(productPromotionMapper.selectById(relationId)), JSONObject.class);
            case "6":
                return JsonUtil.fromJson(JsonUtil.toJson(timeDiscountMapper.selectById(relationId)), JSONObject.class);
            default:
                return null;
        }
    }

    @Override
    public List<Category> getProductCategoryList(ProductListDTO listDTO) {
        // 创建查询构造器
        MPJLambdaWrapper<Product> queryWrapper = new MPJLambdaWrapper<>();
        // 构造查询条件
        buildQueryWrapper(queryWrapper, listDTO);
        // 查询所有结果
        List<Product> list = this.list(queryWrapper);
        // 获取商品 categoryId 列表，去掉重复的
        List<Integer> categoryIdList = list.stream()
                .map(Product::getCategoryId)
                .distinct()
                .toList();
        if (ObjectUtil.isEmpty(categoryIdList)) {
            return Collections.emptyList();
        }
        return categoryService.listByIds(categoryIdList);
    }

    @Override
    public List<Brand> getProductBrandList(ProductListDTO listDTO) {
        // 创建查询构造器
        MPJLambdaWrapper<Product> queryWrapper = new MPJLambdaWrapper<>();
        // 构造查询条件
        buildQueryWrapper(queryWrapper, listDTO);
        // 查询所有结果
        List<Product> list = this.list(queryWrapper);
        // 获取商品 categoryId 列表，去掉重复的
        List<Integer> brandIdList = list.stream()
                .map(Product::getBrandId)
                .distinct()
                .toList();
        if (ObjectUtil.isEmpty(brandIdList)) {
            return Collections.emptyList();
        }

        return brandService.lambdaQuery()
                .in(Brand::getBrandId, brandIdList)
                .eq(Brand::getStatus, BrandStatusEnum.APPROVED.getCode())
                .eq(Brand::getIsShow, 1)
                .list();
    }

    @Override
    public BigDecimal getMaxPrice(ProductListDTO listDTO) {
        // 创建查询构造器
        MPJLambdaWrapper<Product> queryWrapper = new MPJLambdaWrapper<>();
        listDTO.setMaxPrice(null);
        listDTO.setMinPrice(null);
        // 构造查询条件，例如根据关键词、分类、品牌等筛选商品
        buildQueryWrapper(queryWrapper, listDTO);

        // 查询并获取符合条件的商品中价格最高的一个
        // 使用 orderByDesc 按商品价格降序排序，并通过 last("limit 1") 限制只取一条记录
        Product product = this.getOne(queryWrapper.orderByDesc(Product::getProductPrice).last("limit 1"));

        // 如果查询结果不为空，返回其商品价格；否则返回 BigDecimal.ZERO
        return product != null ? product.getProductPrice() : BigDecimal.ZERO;

    }

    @Override
    public Page<ProductListResDTO> getRelatedProductList(Integer productId) {
        // 获取商品信息
        Product product = this.getById(productId);

        List<Integer> productIdList = StringUtils.str2IntList(product.getProductRelated());
        if (productIdList.isEmpty()) {
            return null;
        }
        ProductListDTO productListDTO = new ProductListDTO();
        productListDTO.setIdList(productIdList);
        return list(productListDTO);
    }

    @Override
    public String getProductSnById(Integer productId) {
        if (productId == null) {
            return null;
        }
        Product product = this.getById(productId);
        if (product == null) {
            return null;
        }
        return product.getProductSn();

    }

    @Override
    public String getParticiple(ProductKeyDTO dto) {
        // 去除首尾空格
        String trimmed = StrUtil.trim(dto.getProductName());
        // 移除所有空格
        String withoutSpaces = StrUtil.removeAll(trimmed, " ");
        // 对 HTML 特殊字符进行转义
        String keyword = EscapeUtil.escapeHtml4(withoutSpaces);
        return StringUtils.cutForSearch(keyword);
    }

    @Override
    @Transactional
    public String updateFieldProduct(UpdateFieldProductDTO updateField, String[] allowFields) {
        Product product = this.getById(updateField.getId());
        Assert.notNull(product, () -> new GlobalException("商品不存在"));

        if (product.getVendorProductId() == null) {
            if (tigshopProperties.getIsO2o() == 1) {
                // 是分配商品，且店铺在修改为上线时
                if (product.getShopId() == 0
                        && ObjectUtil.equals(updateField.getField(), "productStatus")
                        && ObjectUtil.equals(updateField.getVal(), 1)) {
                    if (AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.STORE
                            && ObjectUtil.equals(product.getProductStatus(), NO)) {
                        throw new GlobalException("平台已下架商品，你无法上架");
                    }
                }
                if (product.getShopId() == 0 && ObjectUtil.equals(updateField.getField(), "productName")) {
                    ConfigPO storeAssignProductName = configService.getConfigByCode(SettingsEnum.STORE_ASSIGN_PRODUCT_NAME.getBizCode());
                    storeProductService.lambdaUpdate()
                            .eq(StoreProduct::getProductId, product.getProductId())
                            .eq(StoreProduct::getShopId, getShopId())
                            .set(Objects.equals(storeAssignProductName.getBizVal(), "1"), StoreProduct::getProductName, updateField.getVal())
                            .update();
                }
                if (product.getShopId() != 0 && AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.STORE) {
                    String configVal = configService.getConfigVal(STORE_INDEPENDENT_GOODS);
                    if ("0".equals(configVal)) {
                        throw new GlobalException("平台已设置无法自建商品");
                    }
                }
            }
            return this.updateProduct(updateField, allowFields);
        } else {
            return this.updateVendorProduct(updateField, allowFields);
        }
    }

    private String updateProduct(UpdateFieldProductDTO updateField, String[] allowFields) {
        if ("del".equals(updateField.getField())) {
            this.removeById(updateField.getId());
            return "操作成功";
        }

        Product product = this.getById(updateField.getId());
        if (product == null) {
            throw new GlobalException("商品不存在");
        }
        if (ObjectUtil.equals(updateField.getField(), "productSku")) {
            String json = JSONUtil.toJsonStr(updateField.getVal());
            List<ProductSkuInfoDTO> productSkuList = JSONUtil.toList(json, ProductSkuInfoDTO.class);
            if (CollUtil.isNotEmpty(productSkuList)) {
                for (ProductSkuInfoDTO dto : productSkuList) {

                    ProductSku productSku = productSkuService.getById(dto.getSkuId());

                    productSkuService.lambdaUpdate()
                            .eq(ProductSku::getSkuId, dto.getSkuId())
                            .set(ProductSku::getSkuStock, dto.getSkuStock())
                            .update();

                    int number = dto.getSkuStock() - productSku.getSkuStock();
                    ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                            .productId(product.getProductId())
                            .specId(productSku.getSkuId())
                            .number(dto.getSkuStock())
                            .addTime(StringUtils.getCurrentTime())
                            .oldNumber(productSku.getSkuStock())
                            .type(number > 0 ? 1 : 2)
                            .changeNumber(Math.abs(number))
                            .desc(number > 0 ? "增加库存" : "扣减库存")
                            .shopId(product.getShopId())
                            .build();
                    productInventoryLogService.save(productInventoryLog);
                }
                int sum = productSkuList.stream().mapToInt(ProductSkuInfoDTO::getSkuStock).sum();
                product.setProductStock(sum);
                this.updateById(product);

                int diff = sum - product.getProductStock();
                ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                        .productId(product.getProductId())
                        .specId(0)
                        .number(sum)
                        .addTime(StringUtils.getCurrentTime())
                        .oldNumber(product.getProductStock())
                        .type(diff > 0 ? 1 : 2)
                        .changeNumber(diff)
                        .desc(diff > 0 ? "增加库存" : "扣减库存")
                        .shopId(product.getShopId())
                        .build();
                productInventoryLogService.save(productInventoryLog);
            }
            return "操作成功";
        }
        // 判断是否是卡密商品，并且状态改为上架。需要判断卡密是否可以使用
        if (product.getProductType() == PRODUCT_TYPE_CARD.getCode()
                && ObjectUtil.equals(updateField.getField(), "productStatus")
                && ObjectUtil.equals(updateField.getVal(), "1")) {
            ECardGroup byId = ecardGroupService.getById(product.getCardGroupId());
            if (byId.getIsUse() == 0) {
                throw new GlobalException("电子卡卷组【" + byId.getGroupName() + "】未启用，请先启用电子卡卷组！");
            }
        }

        //判断是否是门店商品，并修改上下架状态
        if (tigshopProperties.getIsO2o() == 1
                && ObjectUtil.equals(updateField.getField(), "productStatus")
                && !ObjectUtil.equals(getShopId(), product.getShopId())) {
            storeProductService.lambdaUpdate()
                    .eq(StoreProduct::getProductId, product.getProductId())
                    .eq(StoreProduct::getShopId, getShopId())
                    .set(StoreProduct::getProductStatus, updateField.getVal())
                    .update();
            return "操作成功";
        }

        UpdateFieldDTO updateFieldDTO = new UpdateFieldDTO();
        updateFieldDTO.setId(updateField.getId());
        updateFieldDTO.setField(updateField.getField());
        updateFieldDTO.setVal(updateField.getVal().toString());
        // 修改状态
        updateField(updateFieldDTO, allowFields);

        if ("productStock".equals(updateField.getField())) {
            int newStock = Integer.parseInt(updateField.getVal().toString());
            int number = newStock - product.getProductStock();
            ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                    .productId(product.getProductId())
                    .specId(0)
                    .number(newStock)
                    .addTime(StringUtils.getCurrentTime())
                    .oldNumber(product.getProductStock())
                    .type(number > 0 ? 1 : 2)
                    .changeNumber(Math.abs(number))
                    .desc(number > 0 ? "增加库存" : "扣减库存")
                    .shopId(product.getShopId())
                    .build();
            productInventoryLogService.save(productInventoryLog);
        }

        if ("productSn".equals(updateField.getField())) {
            Long count = this.lambdaQuery()
                    .eq(Product::getProductSn, updateField.getVal())
                    .ne(Product::getProductId, updateField.getId())
                    .count();
            if (count > 0) {
                throw new GlobalException("商品编号重复，请重新输入");
            }
        }

        //发送后台消息
        if (Objects.equals(updateField.getField(), "productStatus") && Objects.equals(updateField.getVal(), 0)) {
            AdminMsgCreateDTO createDTO = new AdminMsgCreateDTO();
            createDTO.setMsgType(AdminMsgTypeEnum.PRODUCT_OUT_OF_STOCK.getCatId());
            createDTO.setShopId(product.getShopId());
            createDTO.setProductId(product.getProductId());
            createDTO.setTitle("商品下架:" + product.getProductName());
            createDTO.setContent("您的商品【" + product.getProductName() + "】已经下架，请及时处理！");
            Map<String, Object> relatedData = new HashMap<>();
            relatedData.put("productId", product.getProductId());
            createDTO.setRelatedData(relatedData);
            adminMsgService.createMessage(createDTO);
        }

        // 异步同步到ES
        try {
            productEsSyncService.syncProductAsync(Long.valueOf(product.getProductId()));
        } catch (Exception e) {
            log.error("商品状态变更后ES同步失败: {}", product.getProductId(), e);
        }

        return "操作成功";
    }

    private String updateVendorProduct(UpdateFieldProductDTO updateField, String[] allowFields) {
        if ("del".equals(updateField.getField())) {
            this.removeById(updateField.getId());
            return "操作成功";
        }

        Product product = this.getById(updateField.getId());

        // 判断供应商商品是否在售
        if (Objects.equals(updateField.getField(), "productStatus") && Objects.equals(updateField.getVal(), 1)) {
            VendorProduct vendorProduct = vendorProductService.getById(product.getVendorProductId());
            Assert.isTrue(vendorProduct.isSales(), () -> new GlobalException("供应商商品未上架"));
        }

        UpdateFieldDTO updateFieldDTO = new UpdateFieldDTO();
        updateFieldDTO.setId(updateField.getId());
        updateFieldDTO.setField(updateField.getField());
        updateFieldDTO.setVal(updateField.getVal().toString());
        // 修改状态
        updateField(updateFieldDTO, allowFields);


        //发送后台消息
        if (Objects.equals(updateField.getField(), "productStatus") && Objects.equals(updateField.getVal(), "0")) {
            AdminMsgCreateDTO createDTO = new AdminMsgCreateDTO();
            createDTO.setMsgType(AdminMsgTypeEnum.PRODUCT_OUT_OF_STOCK.getCatId());
            createDTO.setShopId(product.getShopId());
            createDTO.setProductId(product.getProductId());
            createDTO.setTitle("商品下架:" + product.getProductName());
            createDTO.setContent("您的商品【" + product.getProductName() + "】已经下架，请及时处理！");
            Map<String, Object> relatedData = new HashMap<>();
            relatedData.put("productId", product.getProductId());
            createDTO.setRelatedData(relatedData);
            adminMsgService.createMessage(createDTO);
        }

        return "操作成功";
    }

    @Transactional
    @Override
    public void batchOperation(ProductBatchOperationDTO dto) {
        if (ProductBatchOperationEnum.DEL.getCode().equals(dto.getType())) {
            this.removeByIds(dto.getIds());
            return;
        }

        // 查询产品信息
        List<Product> products = this.lambdaQuery().in(Product::getProductId, dto.getIds()).list();
        if (CollUtil.isEmpty(products)) {
            return;
        }

        if (ProductBatchOperationEnum.UP.getCode().equals(dto.getType())) {
            products.forEach(product -> product.setProductStatus(1));
        }

        if (ProductBatchOperationEnum.DOWN.getCode().equals(dto.getType())) {
            products.forEach(product -> product.setProductStatus(0));
        }

        if (ProductBatchOperationEnum.RECYCLE.getCode().equals(dto.getType())) {
            products.forEach(product -> {
                product.setProductStatus(0);
                product.setIsDelete(1);
            });
        }

        if (ProductBatchOperationEnum.RESTORE.getCode().equals(dto.getType())) {
            products.forEach(product -> product.setIsDelete(0));
        }

        if (ProductBatchOperationEnum.AUDIT.getCode().equals(dto.getType())) {
            products.forEach(product -> product.setCheckStatus(1));
        }

        this.updateBatchById(products);

        // 异步同步到ES
        try {
            List<Long> productIds = products.stream()
                    .map(product -> Long.valueOf(product.getProductId()))
                    .collect(Collectors.toList());
            productEsSyncService.batchSyncProductsAsync(productIds);
        } catch (Exception e) {
            log.error("批量操作后ES同步失败: {}", dto.getType(), e);
        }
    }

    @Override
    public boolean recycle(ProductRecycleDTO dto) {
        StoreProduct storeProduct = storeProductService.lambdaQuery()
                .eq(StoreProduct::getProductId, dto.getId())
                .eq(StoreProduct::getShopId, getShopId())
                .last("limit 1").one();
        if (storeProduct != null) {
            storeProduct.setProductStatus(0);
            storeProduct.setIsDelete(1);
            return storeProductService.updateById(storeProduct);
        }

        Product product = this.getById(dto.getId());
        Assert.notNull(product, () -> new GlobalException("未知的产品"));

        product.setProductStatus(0);
        product.setIsDelete(1);
        this.updateById(product);

        // 异步从ES删除
        try {
            productEsSyncService.deleteProductAsync(Long.valueOf(product.getProductId()));
        } catch (Exception e) {
            log.error("商品移入回收站后ES删除失败: {}", product.getProductId(), e);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String copy(ProductCopyDTO dto) {
        Product product = this.lambdaQuery().eq(Product::getProductId, dto.getProductId()).one();
        Assert.notNull(product, () -> new GlobalException("商品不存在"));

        // 1. 新增产品
        product.setProductId(null);
        String newProductSn = this.createNewProductSn();
        product.setProductSn(newProductSn);
        this.save(product);
        Integer newProductId = product.getProductId();

        // 2. 关联产品文章
        List<ProductArticle> productArticles = productArticleService.lambdaQuery().eq(ProductArticle::getGoodsId, dto.getProductId()).list();
        if (CollUtil.isNotEmpty(productArticles)) {
            productArticles.forEach(item -> item.setGoodsId(newProductId));
            productArticleService.saveBatch(productArticles);
        }

        // 3. 保存产品属性
        List<ProductAttribute> productAttributes = productAttributesService.lambdaQuery().eq(ProductAttribute::getProductId, dto.getProductId()).list();
        if (CollUtil.isNotEmpty(productAttributes)) {
            productAttributes.forEach(item -> {
                item.setAttributesId(null);
                item.setProductId(newProductId);
            });
            productAttributesService.saveBatch(productAttributes);
        }

        // 4. 保存产品规格
        List<ProductSku> productSkus = productSkuService.lambdaQuery().eq(ProductSku::getProductId, dto.getProductId()).list();
        List<ProductSku> newProductSkus = new ArrayList<>();
        if (CollUtil.isNotEmpty(productSkus)) {
            for (int i = 0; i < productSkus.size(); i++) {
                ProductSku productSku = productSkus.get(i);
                productSku.setSkuId(null);
                productSku.setProductId(newProductId);
                String skuSn = newProductSn + "-" + (i + 1);
                productSku.setSkuSn(skuSn);
                newProductSkus.add(productSku);
            }
            productSkuService.saveBatch(newProductSkus);
        }

        // 5. 保存产品相册
        List<ProductGallery> productGalleries = productGalleryService.lambdaQuery()
                .eq(ProductGallery::getProductId, dto.getProductId())
                .orderByAsc(ProductGallery::getSortOrder)
                .list();
        if (CollUtil.isNotEmpty(productGalleries)) {
            productGalleries.forEach(item -> {
                item.setProductId(newProductId);
                item.setPicId(null);
            });
            productGalleryService.saveBatch(productGalleries);
        }

        //6. 保存产品视频
        List<ProductVideo> productVideos = productVideoService.lambdaQuery()
                .eq(ProductVideo::getProductId, dto.getProductId())
                .list();
        if (CollUtil.isNotEmpty(productVideos)) {
            productVideos.forEach(item -> {
                item.setProductId(newProductId);
                item.setId(null);
            });
            productVideoService.saveBatch(productVideos);
        }
        // 产品新增日志
        adminLogService.createByLogInfo(StrUtil.format("创建商品:{}", product.getProductName()));
        return "复制成功";
    }

    @Override
    public boolean audit(ProductAuditDTO dto) {
        dto.validParam();

        Product product = this.getById(dto.getId());
        Assert.notNull(product, () -> new GlobalException("商品不存在"));
        Assert.isTrue(CheckStatus.PENDING.getCode() == product.getCheckStatus(), () -> new GlobalException("商品审核状态错误"));

        product.setCheckStatus(dto.getCheckStatus());
        product.setCheckReason(dto.getCheckReason());
        // 审核通过直接上架
        product.setProductStatus(CheckStatus.APPROVED.getCode() == dto.getCheckStatus() ? 1 : 0);
        this.updateById(product);

        // 发送审核通知消息
        String isPass = CheckStatus.APPROVED.getCode() == dto.getCheckStatus() ? "通过" : "不通过";
        String checkReason = CheckStatus.APPROVED.getCode() == dto.getCheckStatus() ? "" : ",未通过的原因为：" + dto.getCheckReason();
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("productId", product.getProductId());
        AdminMsg adminMsg = AdminMsg.builder()
                .msgType(AdminMsgTypeEnum.PRODUCT_AUDIT_NOTIFICATION.getCatId())
                .title("商品审核通知：" + product.getProductName())
                .shopId(product.getShopId())
                .productId(product.getProductId())
                .content("商品" + product.getProductName() + "审核结果为：" + isPass + checkReason)
                .relatedData(JSON.toJSONString(relatedData))
                .sendTime(StringUtils.getCurrentTime())
                .build();
        adminMsgService.save(adminMsg);

        // 审核通过 -- 更新商品分组
        if (CheckStatus.APPROVED.getCode() == dto.getCheckStatus()) {
            List<ProductGroup> productGroups = productGroupService.lambdaQuery()
                    .eq(ProductGroup::getShopId, product.getShopId())
                    .in(ProductGroup::getProductGroupName, "新品", "热卖")
                    .list();

            if (CollUtil.isNotEmpty(productGroups)) {
                productGroups.forEach(group -> {
                    List<Integer> productIds = StringUtils.str2IntList(group.getProductIds());
                    if ("新品".equals(group.getProductGroupName()) && product.getIsNew() == 1) {
                        productIds.add(product.getProductId());
                    }
                    if ("热卖".equals(group.getProductGroupName()) && product.getIsHot() == 1) {
                        productIds.add(product.getProductId());
                    }
                    group.setProductIds(productIds.toString());
                });
                productGroupService.updateBatchById(productGroups);
            }
        }
        return true;
    }

    @Override
    public String createNewProductSn() {
        Product product = this.lambdaQuery().orderByDesc(Product::getProductId).last("LIMIT 1").one();

        int num = product != null ? product.getProductId() + 1 : 1;
        String snPrefix = configService.getConfigByCode(SettingsEnum.SN_PREFIX.getBizCode()).getBizVal();

        String leadingZeros = String.format("%07d", num).substring(0, 7 - java.lang.String.valueOf(num).length());

        return snPrefix + leadingZeros + num;
    }

    @Override
    public boolean auditAgain(Integer id) {
        Product product = this.getById(id);
        Assert.notNull(product, () -> new GlobalException("商品不存在"));
//        Assert.isTrue(CheckStatus.REJECTED.getCode() == product.getCheckStatus(), () -> new GlobalException("商品审核状态错误"));
        product.setCheckStatus(CheckStatus.PENDING.getCode());
        product.setCheckReason("");
        return this.updateById(product);
    }

    // 批量获取最终价库存信息
    @Override
    public Map<Integer, ProductAvailabilityVO> getBatchProductAvailability(String skuIds, Integer shopId) {
        if (StrUtil.isBlank(skuIds)) {
            return Collections.emptyMap();
        }
        String[] skuIdArray = skuIds.split(",");
        if (skuIdArray.length == 0) {
            return Collections.emptyMap();
        }
        List<String> skuIdList = Arrays.stream(skuIdArray).toList();
        Map<Integer, ProductAvailabilityVO> map = new HashMap<>();
        for (String skuId : skuIdList) {
            // 判断是否为数字
            if (StrUtil.isBlank(skuId) || !skuId.chars().allMatch(Character::isDigit)) {
                continue;
            }
            int parseIntSkuId = Integer.parseInt(skuId);
            // 获取商品sku详情
            ProductSkuDTO productSkuServiceDetail = productSkuService.getDetail(parseIntSkuId, null);
            if (productSkuServiceDetail == null) {
                continue;
            }
            ProductAvailabilityVO productSkuDetail = this.getProductSkuDetail(productSkuServiceDetail.getProductId(), parseIntSkuId, 0, null, shopId);
            map.put(parseIntSkuId, productSkuDetail);
        }
        return map;
    }

    @Override
    public Page<SalesmanProductListVO> salesmanProducts(SalesmanProductPageQuery pageQuery) {
        Page<SalesmanProductListVO> page = buildSortOrder(pageQuery);

        pageQuery.setShopId(HeaderUtils.getShopId());

        page.orders().forEach(orderItem -> orderItem.setColumn("p." + orderItem.getColumn()));

        return this.baseMapper.salesmanProducts(page, pageQuery);
    }

    @Override
    public void discontinueByVendorId(Integer vendorId) {
        if (vendorId == null) {
            return;
        }
        // 更新与供应商关联的所有产品状态为下架
        this.lambdaUpdate()
                .eq(Product::getVendorId, vendorId)
                .set(Product::getProductStatus, ProductStatus.OFF_SALE.getCode())
                .update();
    }

    @Override
    public void discontinueByVendorProductId(Integer vendorProductId) {
        if (vendorProductId == null) {
            return;
        }
        // 更新指定供应商产品的状态为下架
        this.lambdaUpdate()
                .eq(Product::getVendorProductId, vendorProductId)
                .set(Product::getProductStatus, ProductStatus.OFF_SALE.getCode())
                .update();
    }

    @Override
    public void discontinueByVendorProductIds(List<Integer> vendorProductIds) {
        if (ObjectUtil.isEmpty(vendorProductIds)) {
            return;
        }
        // 更新指定供应商产品的状态为下架
        this.lambdaUpdate()
                .in(Product::getVendorProductId, vendorProductIds)
                .set(Product::getProductStatus, ProductStatus.OFF_SALE.getCode())
                .update();
    }

    @Transactional
    @Override
    public void vendorProductImport(VendorProductImportParam param) {
        // 查询供应商产品
        List<VendorProductBO> vendorProducts = vendorProductService.selectVendorProducts(param.getVendorProductIds());

        // 判断导入过的不可再次导入
        Integer shopId = HeaderUtils.getShopId();
        List<Product> products = this.lambdaQuery().gt(Product::getVendorProductId, 0).eq(Product::getShopId, shopId).list();
        List<Integer> vendorProductIds = products.stream().map(Product::getVendorProductId).toList();

        boolean imported = vendorProducts.stream()
                .anyMatch(vendorProduct -> {
                    int vendorProductId = vendorProduct.getVendorProduct().getId().intValue();
                    return vendorProductIds.contains(vendorProductId);
                });
        Assert.isFalse(imported, "商品已导入过，请勿重复导入");

        // 获取店铺价格设置
        Integer vendorSetPriceType;
        String vendorSetPriceAutoValue;
        if (shopId != null && shopId > 0) {
            Shop shop = shopMapper.selectById(shopId);
            vendorSetPriceType = shop.getVendorSetPriceType();
            vendorSetPriceAutoValue = shop.getVendorSetPriceAutoValue();
        } else {
            String vendorSetPriceTypeStr = configService.getConfigVal(SettingsEnum.VENDOR_SET_PRICE_TYPE);
            vendorSetPriceType = vendorSetPriceTypeStr == null ? null : Integer.valueOf(vendorSetPriceTypeStr);
            vendorSetPriceAutoValue = configService.getConfigVal(SettingsEnum.VENDOR_SET_PRICE_AUTO_VALUE);
        }

        Integer finalVendorSetPriceType = vendorSetPriceType;
        String finalVendorSetPriceAutoValue = vendorSetPriceAutoValue;
        vendorProducts.forEach(vendorProduct -> {
            vendorProduct.transferInit();

            // 1. 保存商品信息
            Product product = vendorProduct.getProduct();
            // 1.1 重新生成编号
            String productSn = createNewProductSn();
            product.setProductSn(productSn);
            this.save(product);

            // 2. 保存商品视频
            ProductVideo productVideo = vendorProduct.getProductVideo();
            if (productVideo != null) {
                productVideo.setProductId(product.getProductId());
                productVideoService.save(productVideo);
            }

            // 3. 保存商品图片
            List<ProductGallery> productGalleries = vendorProduct.getProductGalleries();
            productGalleries.forEach(productGallery -> productGallery.setProductId(product.getProductId()));
            productGalleryService.saveBatch(productGalleries);

            // 4. 保存商品属性
            List<ProductAttribute> productAttributes = vendorProduct.getProductAttributes();
            if (CollUtil.isNotEmpty(productAttributes)) {
                productAttributes.forEach(productAttribute -> productAttribute.setProductId(product.getProductId()));
                productAttributesService.saveBatch(productAttributes);
            }

            // 5. 保存商品规格
            List<ProductSku> productSkus = vendorProduct.getProductSkus();
            List<VendorProductSku> vendorProductSkus = vendorProduct.getSkus();
            Map<Long, VendorProductSku> vendorProductSkuMap = vendorProductSkus.stream().collect(Collectors.toMap(VendorProductSku::getId, Function.identity()));

            IntStream.range(0, productSkus.size())
                    .forEach(idx -> {
                        ProductSku productSku = productSkus.get(idx);

                        productSku.setProductId(product.getProductId());
                        productSku.setSkuSn(product.getProductSn() + "-" + (idx + 1));

                        if (finalVendorSetPriceType != null) {
                            BigDecimal skuPrice = productSku.getSkuPrice();
                            if (finalVendorSetPriceType == 3) {
                                skuPrice = skuPrice;
                            }
                            if (finalVendorSetPriceType == 2) {
                                BigDecimal vendorSetPrice = new BigDecimal(finalVendorSetPriceAutoValue);
                                skuPrice = productSku.getSkuPrice().add(vendorSetPrice);
                            }
                            if (finalVendorSetPriceType == 1) {
                                BigDecimal vendorSetPriceRate = new BigDecimal(finalVendorSetPriceAutoValue).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                                BigDecimal addPrice = productSku.getSkuPrice().multiply(vendorSetPriceRate);
                                skuPrice = skuPrice.add(addPrice);
                            }

                            VendorProductSku vendorProductSku = vendorProductSkuMap.get(productSku.getVendorProductSkuId().longValue());
                            skuPrice = skuPrice.compareTo(vendorProductSku.getMaxPrice()) > 0 ? vendorProductSku.getMaxPrice() : skuPrice;
                            productSku.setSkuPrice(skuPrice);
                        }
                    });
            productSkuService.saveBatch(productSkus);

            BigDecimal minSkuPrice = productSkus.stream().map(ProductSku::getSkuPrice).min(BigDecimal::compareTo).get();
            product.setProductPrice(minSkuPrice);
            this.updateById(product);

            Integer vendorId = vendorProduct.getVendorProduct().getVendorId();
            // 查询是否已存在记录
            VendorShopBind exist = vendorShopBindMapper.selectOne(
                    new QueryWrapper<VendorShopBind>()
                            .eq("vendor_id", vendorId)
                            .eq("shop_id", shopId)
            );

            if (exist == null) {
                VendorShopBind vendorShopBind = new VendorShopBind();
                vendorShopBind.setVendorId(vendorId);
                vendorShopBind.setShopId(shopId);
                vendorShopBind.setAddTime(StringUtils.getCurrentTime());
                vendorShopBindMapper.insert(vendorShopBind);
            }
        });
    }

    @Override
    public BigDecimal getVendorMaxPrice(Integer vendorProductSkuId) {
        VendorProductSku vendorProductSku = vendorProductSkuService.getById(vendorProductSkuId);
        Assert.notNull(vendorProductSku, () -> new GlobalException("商品规格不存在"));

        return vendorProductSku.getMaxPrice();
    }

    @Override
    public DeliveryOptionVO getDeliveryOption(Integer shopId) {
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        List<Cart> carts = cartMapper.selectList(Wrappers.lambdaQuery(Cart.class)
                .eq(Cart::getUserId, currentUserId)
                .eq(Cart::getIsChecked, 1)
                .eq(shopId != null, Cart::getShopId, shopId));

        if (CollectionUtils.isEmpty(carts)) {
            return new DeliveryOptionVO();
        }
        List<Integer> productIds = carts.stream().map(Cart::getProductId).toList();

        Assert.notEmpty(productIds, () -> new GlobalException("商品不能为空"));
        List<Product> deliveryProductList = new ArrayList<>();
        Map<Integer, DeliveryOptionVO.PickupShopInfo> pickupMap = new HashMap<>();
        List<Product> noShippingProductList = new ArrayList<>();

        for (Cart cartProduct : carts) {
            Product product = this.getById(cartProduct.getProductId());
            if (product == null) {
                continue;
            }

            int productShopId = cartProduct.getShopId();

            boolean canPickup = product.getIsShopPickup() != null && product.getIsShopPickup() == 1;
            boolean canDelivery = product.getIsLogistics() != null && product.getIsLogistics() == 1;
            boolean isNoShipping = product.getNoShipping() != null && product.getNoShipping() == 1;

            if (isNoShipping && product.getProductType() != 1) {
                noShippingProductList.add(product);
                break;
            }

            if (canDelivery) {
                deliveryProductList.add(product);
            }

            if (canPickup) {
                // 获取自提点
                List<Shop> shops = shopMapper.selectList(Wrappers.lambdaQuery(Shop.class).eq(Shop::getStoreParentId, productShopId).eq(Shop::getStatus, 1));
                if (ObjectUtil.isNotEmpty(shops)) {
                    // 自提点不为空时添加
                    DeliveryOptionVO.PickupShopInfo info = pickupMap.computeIfAbsent(
                            productShopId, k -> new DeliveryOptionVO.PickupShopInfo(productShopId, new HashSet<>(), new ArrayList<>()));
                    info.getPickups().addAll(shops);
                    info.getProducts().add(product);
                }
            }
        }


        DeliveryOptionVO result = new DeliveryOptionVO();

        if (CollUtil.isNotEmpty(noShippingProductList)) {
            result.setDeliveryType(ShopDeliveryTypeEnum.DELIVERY.getCode());
            return result;
        }

        // 总商品数量
        int total = carts.size();
        // 配送的商品数量
        int deliveryCount = deliveryProductList.size();
        // 自提的商品数量
        int pickupCount = pickupMap.values().stream().mapToInt(i -> i.getProducts().size()).sum();

        if (pickupCount == total && deliveryCount == total) {
            result.setDeliveryType(ShopDeliveryTypeEnum.ALL_CHOOSE.getCode());
        } else if (pickupCount == total) {
            result.setDeliveryType(ShopDeliveryTypeEnum.SELF_PICK_UP.getCode());
        } else if (deliveryCount == total) {
            result.setDeliveryType(ShopDeliveryTypeEnum.DELIVERY.getCode());
        } else {
            result.setDeliveryType(ShopDeliveryTypeEnum.PART_CHOOSE.getCode());
        }

        result.setDeliveryProducts(deliveryProductList);
        result.setPickupProducts(new ArrayList<>(pickupMap.values()));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void allocationProduct(AllocationParam param) {
        List<Integer> productIds = param.getProductIds();
        Assert.notEmpty(productIds, () -> new GlobalException("商品不能为空"));

        Set<Integer> targetShops = new HashSet<>();

        switch (param.getType()) {
            case ALL -> {
                List<Shop> shops = shopMapper.selectList(Wrappers.lambdaQuery(Shop.class).eq(Shop::getShopType, ShopTypeEnum.STORE.getCode()));
                // targetShops = shops.stream().map(Shop::getShopId).toList();
                Set<Integer> shopIds = shops.stream().map(Shop::getShopId).collect(Collectors.toSet());
                shopIds.add(0);
                targetShops = shopIds;
            }

            case AREA -> {
                // if (param.getAreaId() == null) {
                //     throw new GlobalException("区域分配必须提供 areaId");
                // }
                List<AreaStoreManagerShop> areaStoreManagerShops = areaStoreManagerShopMapper.selectList(Wrappers.lambdaQuery(AreaStoreManagerShop.class));
                Set<Integer> shopId = areaStoreManagerShops.stream().map(AreaStoreManagerShop::getShopId).collect(Collectors.toSet());
                shopId.add(0);
                targetShops = shopId;
            }

            case SHOPS -> {
                if (param.getShopIds() == null || param.getShopIds().isEmpty()) {
                    throw new IllegalArgumentException("门店分配必须提供 shopIds");
                }
                targetShops = new HashSet<>(param.getShopIds());
            }
        }
        List<Integer> allTargetShops = new ArrayList<>(targetShops);
        // 过滤掉已经分配过的店铺
        if (CollUtil.isNotEmpty(targetShops)) {
            List<StoreProduct> storeProducts = storeProductService.lambdaQuery().in(StoreProduct::getProductId, productIds).list();
            List<Integer> existShopIds = storeProducts.stream().map(StoreProduct::getShopId).map(Long::intValue).toList();
            targetShops = targetShops.stream().filter(shopId -> !existShopIds.contains(shopId)).collect(Collectors.toSet());
        }

        // 遍历目标门店进行商品分配
        allocateProductsToShops(productIds, targetShops, allTargetShops);
    }

    /**
     * 商品分配到门店
     *
     * @param productIds 商品ID
     * @param shopIds    门店ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void allocateProductsToShops(List<Integer> productIds, Set<Integer> shopIds, List<Integer> allTargetShops) {
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("商品ID不能为空");
        }

        // 1. 查出所有商品信息
        List<Product> products = this.lambdaQuery()
                .in(Product::getProductId, productIds).list();
        if (products.isEmpty()) {
            throw new GlobalException("未找到对应的商品");
        }

        // 2. 查出已存在的 (productId, shopId)
        List<StoreProduct> exists = storeProductService.lambdaQuery()
                .in(StoreProduct::getProductId, productIds)
                .list(); // ⚠️ 不仅仅查 shopIds，而是查所有已分配，才能知道要删哪些

        // 3. 构建映射
        Set<String> newSet = allTargetShops.stream()
                .flatMap(shopId -> productIds.stream().map(pid -> pid + "_" + shopId))
                .collect(Collectors.toSet());

        Set<String> existsSet = exists.stream()
                .map(e -> e.getProductId() + "_" + e.getShopId())
                .collect(Collectors.toSet());

        // 4. 需要新增的记录
        List<StoreProduct> toInsert = new ArrayList<>();
        long now = System.currentTimeMillis();
        String storeProductStatus = configService.getConfigVal(STORE_POST_ALLOCATION_STATUS);
        for (Integer shopId : shopIds) {
            for (Product product : products) {
                String key = product.getProductId() + "_" + shopId;
                if (!existsSet.contains(key)) {
                    StoreProduct sp = new StoreProduct();
                    sp.setProductId(product.getProductId());
                    sp.setProductName(product.getProductName());
                    sp.setProductPrice(product.getProductPrice());
                    sp.setProductStatus(Integer.parseInt(storeProductStatus));
                    sp.setProductStock(product.getProductStock());
                    sp.setShopId(shopId.longValue());
                    sp.setAddTime(now);
                    sp.setCardGroupId(product.getCardGroupId() == null ? 0 : product.getCardGroupId());
                    sp.setIsDelete(0);
                    toInsert.add(sp);
                    existsSet.add(key);
                }
            }
        }

        // 5. 需要删除的记录
        List<Long> toDeleteIds = exists.stream()
                .filter(sp -> !newSet.contains(sp.getProductId() + "_" + sp.getShopId()))
                .map(StoreProduct::getStoreProductId)
                .toList();

        // 6. 执行批量操作
        if (!toInsert.isEmpty()) {
            storeProductService.saveBatch(toInsert);
        }
        if (!toDeleteIds.isEmpty()) {
            storeProductService.removeBatchByIds(toDeleteIds);
        }
    }
}