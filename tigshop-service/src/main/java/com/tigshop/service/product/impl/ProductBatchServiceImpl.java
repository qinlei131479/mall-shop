package com.tigshop.service.product.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.dto.product.ProductBatchDealDTO;
import com.tigshop.bean.dto.product.ProductBatchEditDTO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductGallery;
import com.tigshop.bean.vo.product.ProductBatchModifyVO;
import com.tigshop.bean.vo.product.ProductImportExcelPrueVO;
import com.tigshop.bean.vo.product.ProductImportExcelVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.ExcelUtils;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.*;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 产品批量操作 服务实现
 *
 * @author kidd
 * @since 2025/3/26 16:17
 */
@RequiredArgsConstructor
@Service
public class ProductBatchServiceImpl extends BaseServiceImpl<ProductMapper, Product> implements ProductBatchService {

    private final CategoryService categoryService;

    private final ProductService productService;

    private final BrandService brandService;

    private final ConfigService configService;

    private final ProductGalleryService productGalleryService;

    @Override
    public List<ProductImportExcelVO> downloadTemplate() {
        List<ProductImportExcelVO> result = new ArrayList<>();

        ProductImportExcelVO item = ProductImportExcelVO.builder()
                .productName("Xiaomi/小米 小米电视4A 55英寸4k高清智能网络平板液晶电视40 49")
                .productSn("SN00011")
                .categoryName("家用电器|电视|平板电视")
                .productPrice("2000")
                .marketPrice("2200")
                .productStatus("1")
                .brandName("小米")
                .picUrl("img/item/202009/1599117139VhqZQYsACs5awn4N6S!!pic.jpeg")
                .keywords("电视 小米 4k")
                .productBrief("官方授权 全国联保 现货销售")
                .productWeight("10")
                .productStock("9999")
                .build();
        result.add(item);

        return result;
    }

    @Override
    public List<ProductImportExcelVO> productBatchDeal(ProductBatchDealDTO dto) {
        List<Integer> rangeIds = StrUtil.isBlank(dto.getRangeIds()) ? List.of() : Arrays.stream(dto.getRangeIds().split(",")).map(Integer::valueOf).toList();
        Integer shopId = getShopId();
        List<Product> products = new ArrayList<>();

        if (dto.getDealRange() == 1) {
            // 分类
            List<Integer> allCateGoryIds = new ArrayList<>();
            if (CollUtil.isNotEmpty(rangeIds)) {
                rangeIds.forEach(rangeId -> {
                    List<Integer> categoryAllChildIds = categoryService.getCategoryAllChildIds(rangeId);
                    allCateGoryIds.addAll(categoryAllChildIds);
                });
            }
            if (CollUtil.isNotEmpty(allCateGoryIds)) {
                List<Integer> categoryIds = allCateGoryIds.stream().distinct().toList();
                products = this.lambdaQuery()
                        .in(Product::getCategoryId, categoryIds)
                        .eq(shopId > 0, Product::getShopId, shopId)
                        .eq(Product::getIsDelete, Constants.NO)
                        .list();
            }

        } else if (dto.getDealRange() == 2) {
            // 品牌
            products = this.lambdaQuery()
                    .in(CollUtil.isNotEmpty(rangeIds), Product::getBrandId, rangeIds)
                    .eq(shopId > 0, Product::getShopId, shopId)
                    .eq(Product::getIsDelete, Constants.NO)
                    .list();
        } else if (dto.getDealRange() == 3) {
            // 商品
            products = this.lambdaQuery()
                    .in(CollUtil.isNotEmpty(rangeIds), Product::getProductId, rangeIds)
                    .eq(shopId > 0, Product::getShopId, shopId)
                    .eq(Product::getIsDelete, Constants.NO)
                    .list();
        } else {
            products = this.lambdaQuery()
                    .notIn(CollUtil.isNotEmpty(rangeIds), Product::getProductId, rangeIds)
                    .eq(shopId > 0, Product::getShopId, shopId)
                    .eq(Product::getIsDelete, Constants.NO)
                    .list();
        }

        List<Integer> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .distinct()
                .toList();

        Map<Integer, String> categoryMap = Map.of();
        if (CollUtil.isNotEmpty(categoryIds)) {
            List<Category> categoryList = categoryService.lambdaQuery()
                    .in(Category::getCategoryId, categoryIds)
                    .list();
            categoryMap = categoryList.stream()
                    .collect(Collectors.toMap(Category::getCategoryId, Category::getCategoryName));
        }
        Map<Integer, String> finalCategoryMap = categoryMap;
        return products.stream().map(p -> new ProductImportExcelVO(p, finalCategoryMap.get(p.getCategoryId()))).toList();
    }

    @Transactional
    @Override
    public ProductBatchModifyVO productBatchModify(Integer isAutoCat, Integer isAutoBrand, Integer isChangePic, Integer isUpload, MultipartFile file) {
        Assert.notNull(file, () -> new GlobalException("请上传文件"));

        ExcelUtils<ProductImportExcelPrueVO> excelUtils = new ExcelUtils<>(file, ProductImportExcelPrueVO.class);
        List<ProductImportExcelPrueVO> list = excelUtils.importExcel();

        int count = 0;
        StringBuilder msg = new StringBuilder();

        String marketPriceRate = configService.getConfigByCode(SettingsEnum.MARKET_PRICE_RATE.getBizCode()).getBizVal();

        Integer shopId = getShopId();

        if (CollUtil.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                int index = i + 1;
                ProductImportExcelPrueVO prueItem = list.get(i);
                ProductImportExcelVO item = new ProductImportExcelVO();
                BeanUtil.copyProperties(prueItem, item);
                if (StrUtil.isBlank(item.getProductName()) || StrUtil.isBlank(item.getCategoryName())) {
                    msg.append("LINE ").append(index).append("存在商品名称为空或分类为空的数据，已忽略此数据");
                    continue;
                }

                if (StrUtil.isBlank(item.getProductSn())) {
                    String newProductSn = productService.createNewProductSn();
                    item.setProductSn(newProductSn);
                } else {
                    Long existCount = this.lambdaQuery().eq(Product::getProductSn, item.getProductSn()).count();
                    if (existCount > 0) {
                        msg.append("LINE ").append(index).append("错误：存在商品编号重复的数据，已忽略此数据");
                        continue;
                    }
                }

                String[] categoryNames = item.getCategoryName().trim().split("\\|");
                Integer categoryId = categoryService.getCategoryIds(categoryNames, isAutoCat);
                if (categoryId == null || categoryId == 0) {
                    msg.append("LINE ").append(index).append("错误：存在分类不存在的数据，已忽略此数据");
                    continue;
                }
                item.setCategoryId(categoryId);

                if (StrUtil.isNotBlank(item.getBrandName())) {
                    Integer brandId = brandService.getBrandId(item.getBrandName(), isAutoBrand);
                    item.setBrandId(brandId);
                }

                BigDecimal productPrice = item.getProductPrice() == null ? BigDecimal.ZERO : new BigDecimal(item.getProductPrice());

                BigDecimal marketPrice = item.getMarketPrice() != null ? new BigDecimal(item.getMarketPrice()) : productPrice.multiply(new BigDecimal(marketPriceRate));
                int itemShopId = shopId > 0 ? shopId : 0;

                String picUrl = item.getPicUrl();
                String[] picUrlArr = {};
                if (StrUtil.isNotBlank(picUrl)) {
                    picUrlArr = picUrl.trim().split("\\|");
                }


                String firstPicUrl = picUrlArr.length > 0 ? picUrlArr[0] : "";
                Product product = Product.builder()
                        .productName(item.getProductName())
                        .productSn(item.getProductSn())
                        .categoryId(item.getCategoryId())
                        .productPrice(productPrice)
                        .marketPrice(marketPrice)
                        .productStatus(StrUtil.isNotBlank(item.getProductStatus()) ? Integer.parseInt(item.getProductStatus()) : 1)
                        .brandId(brandService.getBrandId(item.getBrandName(), isAutoBrand))
                        .picUrl(firstPicUrl)
                        .picThumb(firstPicUrl)
                        .picOriginal(firstPicUrl)
                        .keywords(item.getKeywords())
                        .productBrief(item.getProductBrief())
                        .productDesc(item.getProductDesc())
                        .productWeight(StrUtil.isNotBlank(item.getProductWeight()) ? new BigDecimal(item.getProductWeight()) : BigDecimal.ZERO)
                        .productStock(StrUtil.isNotBlank(item.getProductStock()) ? Integer.parseInt(item.getProductStock()) : 0)
                        .shopId(itemShopId)
                        .fixedShippingType(1)
                        .build();

                this.save(product);

                for (int j = 0; j < picUrlArr.length; j++) {
                    String imageUrl = picUrlArr[j];
                    ProductGallery productGallery = ProductGallery.builder()
                            .productId(product.getProductId())
                            .picUrl(imageUrl)
                            .picThumb(imageUrl)
                            .picLarge(imageUrl)
                            .picOriginal(imageUrl)
                            .sortOrder(1)
                            .build();
                    productGalleryService.save(productGallery);
                }


                count++;
            }

            String resultMsg = msg.isEmpty() ? "上传完成" : msg.toString();
            return new ProductBatchModifyVO(count, resultMsg);
        }

        return new ProductBatchModifyVO(count, "请上传有数据的文件");
    }

    @Transactional
    @Override
    public boolean productBatchEdit(ProductBatchEditDTO dto) {
        if (CollUtil.isNotEmpty(dto.getEditItems())) {
            List<Product> products = dto.buildProducts();
            this.updateBatchById(products);
        }
        return true;
    }
}
