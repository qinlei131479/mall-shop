package com.tigshop.service.vendor.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.bo.vendort.VendorProductBO;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.vendor.VendorDiscontinueDTO;
import com.tigshop.bean.enums.product.ProductStatus;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.vendor.VendorProductAuditStateEnum;
import com.tigshop.bean.model.product.Brand;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.model.vendor.product.*;
import com.tigshop.bean.param.vendor.product.VendorProductAuditParam;
import com.tigshop.bean.param.vendor.product.VendorProductEditParam;
import com.tigshop.bean.param.vendor.product.VendorProductSaveParam;
import com.tigshop.bean.query.vendor.VendorProductClientPageQuery;
import com.tigshop.bean.query.vendor.VendorProductPageQuery;
import com.tigshop.bean.query.vendor.VendorProductPlatformPageQuery;
import com.tigshop.bean.vo.vendor.product.*;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.vendor.product.VendorProductMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.BrandService;
import com.tigshop.service.product.CategoryService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.GalleryPicService;
import com.tigshop.service.vendor.*;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author kidd
 */
@RequiredArgsConstructor
@Service
public class VendorProductServiceImpl extends BaseServiceImpl<VendorProductMapper, VendorProduct> implements VendorProductService {

    private final VendorProductGalleryService vendorProductGalleryService;
    private final VendorProductVideoService vendorProductVideoService;
    private final VendorProductSkuService vendorProductSkuService;
    private final VendorProductSkuAttrService vendorProductSkuAttrService;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final VendorProductAuditLogService vendorProductAuditLogService;
    private final GalleryPicService galleryPicService;
    private final ConfigService configService;
    private final ProductMapper productMapper;
    private final VendorService vendorService;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    @Override
    public String create(VendorProductSaveParam param) {
        // 1. 新增供应商商品
        String size400 = galleryPicService.getImageSize().get("size_400");
        VendorProduct vendorProduct = param.createVendorProduct(size400);

        String vendorProductNeedCheckStr = configService.getConfigVal(SettingsEnum.VENDOR_PRODUCT_NEED_CHECK);
        Integer vendorProductNeedCheck = Integer.valueOf(vendorProductNeedCheckStr);
        if (Constants.NO.equals(vendorProductNeedCheck)) {
            vendorProduct.setAuditState(VendorProductAuditStateEnum.PASS_AUDIT.getCode());
        }

        this.save(vendorProduct);

        // 2. 新增供应商商品图片
        vendorProductGalleryService.saveBatchGalleries(param, vendorProduct.getId());

        // 3. 新增供应商商品视频
        if (CollUtil.isNotEmpty(param.getVideo())) {
            vendorProductVideoService.create(param, vendorProduct.getId());
        }

        // 4. 新增供应商商品规格
        vendorProductSkuService.saveBatchSku(param, vendorProduct.getId());

        // 5. 新增供应商商品规格属性
        if (CollUtil.isNotEmpty(param.getSkuAttrs())) {
            vendorProductSkuAttrService.saveBatchSkuAttr(param, vendorProduct.getId());
        }

        return Constants.YES.equals(vendorProductNeedCheck) ? "商品审核中，请耐心等待" : "新增成功";
    }

    @Override
    public String edit(VendorProductEditParam param) {
        // 校验供应商商品状态
        VendorProduct dbVendorProduct = this.getById(param.getId());
        Assert.notNull(dbVendorProduct, () -> new GlobalException("供应商商品不存在"));
        Assert.isTrue(dbVendorProduct.isNotSales(), () -> new GlobalException("供应商商品状态异常，不能编辑"));

        // 1. 编辑供应商商品
        String size400 = galleryPicService.getImageSize().get("size_400");
        VendorProduct vendorProduct = param.createVendorProduct(size400);
        // 1.1. 判断是否需要再次审核
        String vendorProductNeedCheckStr = configService.getConfigVal(SettingsEnum.VENDOR_PRODUCT_NEED_CHECK);
        Integer vendorProductNeedCheck = Integer.valueOf(vendorProductNeedCheckStr);
        if (Constants.YES.equals(vendorProductNeedCheck)) {
            vendorProduct.setAuditState(VendorProductAuditStateEnum.WAIT_AUDIT.getCode());
        }
        this.updateById(vendorProduct);

        // 2. 编辑供应商商品图片
        vendorProductGalleryService.updateBatchGalleries(param, vendorProduct.getId());

        // 3. 编辑供应商商品视频
        if (CollUtil.isNotEmpty(param.getVideo())) {
            vendorProductVideoService.update(param, vendorProduct.getId());
        }

        // 4. 编辑供应商商品规格
        vendorProductSkuService.updateBatchSku(param, vendorProduct.getId());

        // 5. 编辑供应商商品规格属性
        if (CollUtil.isNotEmpty(param.getSkuAttrs())) {
            vendorProductSkuAttrService.updateBatchSkuAttr(param, vendorProduct.getId());
        }

        // 6. 下架商户的商品
        this.outageProduct(Collections.singletonList(vendorProduct));

        return Constants.YES.equals(vendorProductNeedCheck) ? "商品审核中，请耐心等待" : "编辑成功";
    }

    private void outageProduct(List<VendorProduct> vendorProducts) {
        List<VendorProduct> salesProducts = vendorProducts.stream().filter(VendorProduct::isSales).toList();
        if (CollUtil.isEmpty(salesProducts)) {
            return;
        }

        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

        VendorDiscontinueDTO dto = new VendorDiscontinueDTO();

        List<Integer> vendorProductIds = salesProducts.stream().map(VendorProduct::getId).map(Long::intValue).toList();
        dto.setVendorProductIds(vendorProductIds);

        rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.VENDOR_DISCONTINUE_ROUTING_KEY, dto);
    }

    @Transactional
    @Override
    public void changeState(OperateDTO operate) {
        VendorProduct vendorProduct = this.getById(operate.getId());
        Assert.notNull(vendorProduct, () -> new GlobalException("供应商商品不存在"));
        Assert.isTrue(vendorProduct.isPassAudit(), () -> new GlobalException("供应商商品审核未通过"));

//        Vendor vendor = vendorService.getById(vendorProduct.getVendorId());
//        Assert.isTrue(vendor.getStatus() == 1 && vendorProduct.isNotSales(), () -> new GlobalException("供应商禁用，不可起售"));

        Integer productState = vendorProduct.isSales() ? Constants.NO : Constants.YES;
        vendorProduct.setProductState(productState);
        this.updateById(vendorProduct);

        if (productState.equals(Constants.NO)) {
            // 下架商户的商品
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            VendorDiscontinueDTO dto = new VendorDiscontinueDTO();
            dto.setVendorProductId(vendorProduct.getId().intValue());
            rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.VENDOR_DISCONTINUE_ROUTING_KEY, dto);
        }

        if (productState.equals(Constants.YES)) {
            String vendorProductNeedCheckStr = configService.getConfigVal(SettingsEnum.VENDOR_PRODUCT_NEED_CHECK);
            Integer vendorProductNeedCheck = Integer.valueOf(vendorProductNeedCheckStr);
            if (Constants.YES.equals(vendorProductNeedCheck)) {
                vendorProduct.setAuditState(VendorProductAuditStateEnum.WAIT_AUDIT.getCode());
            }
        }
    }

    @Transactional
    @Override
    public boolean batch(BatchDTO batch) {
        List<VendorProduct> vendorProducts = this.listByIds(batch.getIds());
        if (CollUtil.isEmpty(vendorProducts)) {
            return true;
        }

        if ("recycle".equals(batch.getType())) {
            this.recycle(vendorProducts);
        }
        if ("outage".equals(batch.getType())) {
            this.outage(vendorProducts);
        }
        if ("restore".equals(batch.getType())) {
            this.restore(vendorProducts);
        }
        return true;
    }

    @Transactional
    public void restore(List<VendorProduct> vendorProducts) {
        vendorProducts.forEach(vendorProduct -> {
            vendorProduct.setIsRecycle(Constants.NO);
        });
        this.updateBatchById(vendorProducts);
    }

    @Transactional
    public void recycle(List<VendorProduct> vendorProducts) {
        // 下架商户的商品
        this.outageProduct(vendorProducts);

        vendorProducts.forEach(vendorProduct -> {
            vendorProduct.setProductState(Constants.NO);
            vendorProduct.setIsRecycle(Constants.YES);
        });
        this.updateBatchById(vendorProducts);

    }

    @Transactional
    public void outage(List<VendorProduct> vendorProducts) {
        // 下架商户的商品
        this.outageProduct(vendorProducts);

        vendorProducts.forEach(vendorProduct -> vendorProduct.setProductState(Constants.NO));
        this.updateBatchById(vendorProducts);
    }

    @Override
    public Page<VendorProductPageVO> pageList(VendorProductPageQuery pageQuery) {
        if (ObjectUtil.isNotEmpty(pageQuery.getSearchStartTime())) {
            DateTime startDate = DateUtil.parse(pageQuery.getSearchStartTime(), "yyyy-MM-dd");
            pageQuery.setStartCreateTime(DateUtil.beginOfDay(startDate).getTime() / 1000);
        }
        if (ObjectUtil.isNotEmpty(pageQuery.getSearchEndTime())) {
            DateTime endDate = DateUtil.parse(pageQuery.getSearchEndTime(), "yyyy-MM-dd");
            pageQuery.setEndCreateTime(DateUtil.beginOfDay(endDate).getTime() / 1000);
        }
        Page<VendorProduct> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        ;
//        Integer vendorId = HeaderUtils.getVendorId();
//        Page<VendorProduct> vendorProductPage = this.lambdaQuery()
//                .eq(VendorProduct::getVendorId, vendorId)
//                .like(StrUtil.isNotBlank(pageQuery.getKeyword()), VendorProduct::getProductName, pageQuery.getKeyword())
//                .eq(pageQuery.getBrandId() != null, VendorProduct::getProductBrandId, pageQuery.getBrandId())
//                .eq(pageQuery.getCategoryId() != null, VendorProduct::getProductCategoryId, pageQuery.getCategoryId())
//                .eq(pageQuery.getProductState() != null, VendorProduct::getProductState, pageQuery.getProductState())
//                .eq(pageQuery.getAuditState() != null, VendorProduct::getAuditState, pageQuery.getAuditState())
//                .eq(pageQuery.getIsRecycle() != null, VendorProduct::getIsRecycle, pageQuery.getIsRecycle())
//                .ge(pageQuery.getStartCreateTime() != null, VendorProduct::getCreateTime, pageQuery.getStartCreateTime())
//                .le(pageQuery.getEndCreateTime() != null, VendorProduct::getCreateTime, pageQuery.getEndCreateTime())
//                .page(page);
        // 是否开启审核，开启审核查看在售需要审核通过
        String configVal = configService.getConfigVal(SettingsEnum.VENDOR_PRODUCT_NEED_CHECK);
        if (ObjectUtil.isNotEmpty(configVal) && configVal.equals("1")
                && pageQuery.getProductState() != null && pageQuery.getProductState().equals(1)
                && pageQuery.getAuditState() == null) {
            pageQuery.setAuditState(1);
        }
        Page<VendorProduct> vendorProductPage = getBaseMapper().pageList(page, HeaderUtils.getVendorId(), pageQuery);
        List<VendorProduct> records = vendorProductPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        List<Long> vendorProductIds = records.stream().map(VendorProduct::getId).toList();
        List<VendorProductSku> vendorProductSkus = vendorProductSkuService.lambdaQuery().in(VendorProductSku::getVendorProductId, vendorProductIds).list();
        Map<Long, List<VendorProductSku>> vendorProductSkuMap = vendorProductSkus.stream().collect(Collectors.groupingBy(VendorProductSku::getVendorProductId));

        List<Integer> brandIds = records.stream().map(VendorProduct::getProductBrandId).toList();
        List<Brand> brands = brandService.listByIds(brandIds);
        Map<Integer, Brand> brandMap = brands.stream().collect(Collectors.toMap(Brand::getBrandId, brand -> brand));

        List<Integer> categoryIds = records.stream().map(VendorProduct::getProductCategoryId).toList();
        List<Category> categories = categoryService.listByIds(categoryIds);
        Map<Integer, Category> categoryMap = categories.stream().collect(Collectors.toMap(Category::getCategoryId, Function.identity()));

        List<VendorProductAuditLog> productAuditLogs = vendorProductAuditLogService.lambdaQuery()
                .in(VendorProductAuditLog::getVendorProductId, vendorProductIds)
                .orderByDesc(VendorProductAuditLog::getCreateTime)
                .list();
        Map<Long, List<VendorProductAuditLog>> productAuditLogMap = productAuditLogs.stream().collect(Collectors.groupingBy(VendorProductAuditLog::getVendorProductId));

        List<VendorProductPageVO> resultRecords = records.stream()
                .map(vendorProduct -> {
                    List<VendorProductSku> currProductSkus = vendorProductSkuMap.get(vendorProduct.getId());
                    Brand brand = brandMap.get(vendorProduct.getProductBrandId());
                    Category category = categoryMap.get(vendorProduct.getProductCategoryId());
                    List<VendorProductAuditLog> currProductAuditLogs = productAuditLogMap.get(vendorProduct.getId());
                    return new VendorProductPageVO(vendorProduct, currProductSkus, brand, category, currProductAuditLogs);
                })
                .toList();

        return PageUtil.convertPage(page, resultRecords);
    }

    @Override
    public VendorProductDetailVO detail(Integer id) {
        VendorProduct vendorProduct = this.getById(id);
        Assert.notNull(vendorProduct, () -> new GlobalException("供应商商品不存在"));

        List<VendorProductGallery> galleries = vendorProductGalleryService.lambdaQuery()
                .eq(VendorProductGallery::getVendorProductId, vendorProduct.getId())
                .orderByAsc(VendorProductGallery::getSortOrder)
                .list();
        List<VendorProductVideo> video = vendorProductVideoService.lambdaQuery().eq(VendorProductVideo::getVendorProductId, vendorProduct.getId()).list();
        List<VendorProductSku> skus = vendorProductSkuService.lambdaQuery().eq(VendorProductSku::getVendorProductId, vendorProduct.getId()).list();
        List<VendorProductSkuAttr> skuAttrs = vendorProductSkuAttrService.lambdaQuery().eq(VendorProductSkuAttr::getVendorProductId, vendorProduct.getId()).list();

        List<VendorProductAuditLog> productAuditLogs = vendorProductAuditLogService.lambdaQuery()
                .eq(VendorProductAuditLog::getVendorProductId, vendorProduct.getId())
                .orderByDesc(VendorProductAuditLog::getCreateTime)
                .list();

        return new VendorProductDetailVO(vendorProduct, galleries, video, skus, skuAttrs, productAuditLogs);
    }

    @Override
    public void discontinueByVendorId(Integer vendorId) {
        if (vendorId == null) {
            return;
        }
        // 更新与供应商关联的所有产品状态为下架
        this.lambdaUpdate()
                .eq(VendorProduct::getVendorId, vendorId)
                .set(VendorProduct::getProductState, ProductStatus.OFF_SALE.getCode())
                .update();
    }

    @Override
    public void discontinueByVendorProductId(Integer vendorProductId) {
        if (vendorProductId == null) {
            return;
        }
        // 更新指定供应商产品的状态为下架
        this.lambdaUpdate()
                .eq(VendorProduct::getId, vendorProductId)
                .set(VendorProduct::getProductState, ProductStatus.OFF_SALE.getCode())
                .update();
    }

    @Override
    public void discontinueByVendorProductIds(List<Integer> vendorProductIds) {
        if (ObjectUtil.isEmpty(vendorProductIds)) {
            return;
        }
        // 更新指定供应商产品的状态为下架
        this.lambdaUpdate()
                .in(VendorProduct::getId, vendorProductIds)
                .set(VendorProduct::getProductState, ProductStatus.OFF_SALE.getCode())
                .update();
    }

    @Override
    public Page<VendorProductPlatformPageVO> platformPageList(VendorProductPlatformPageQuery pageQuery) {
        Page<VendorProductPlatformPageVO> page = buildSortOrder(pageQuery);
        // 是否开启审核，开启审核查看在售需要审核通过
        String configVal = configService.getConfigVal(SettingsEnum.VENDOR_PRODUCT_NEED_CHECK);
        if (ObjectUtil.isNotEmpty(configVal) && configVal.equals("1")
                && pageQuery.getProductState() != null && pageQuery.getProductState().equals(1)
                && pageQuery.getAuditState() == null) {
            pageQuery.setAuditState(1);
        }
        Page<VendorProductPlatformPageVO> vendorProductPage = this.baseMapper.platformPageList(page, pageQuery);

        List<VendorProductPlatformPageVO> records = vendorProductPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        List<Integer> brandIds = records.stream().map(VendorProductPlatformPageVO::getProductBrandId).toList();
        List<Brand> brands = brandService.listByIds(brandIds);
        Map<Integer, Brand> brandMap = brands.stream().collect(Collectors.toMap(Brand::getBrandId, brand -> brand));

        List<Integer> categoryIds = records.stream().map(VendorProductPlatformPageVO::getProductCategoryId).toList();
        List<Category> categories = categoryService.listByIds(categoryIds);
        Map<Integer, Category> categoryMap = categories.stream().collect(Collectors.toMap(Category::getCategoryId, Function.identity()));

        records.forEach(vendorProduct -> {
            Brand brand = brandMap.get(vendorProduct.getProductBrandId());
            Category category = categoryMap.get(vendorProduct.getProductCategoryId());
            Vendor vendor = vendorService.getById(vendorProduct.getVendorId());
            vendorProduct.assembleData(brand, category, vendor);
        });
        return vendorProductPage;
    }

    @Override
    public VendorProductPlatformDetailVO platformDetail(Integer id) {
        VendorProduct vendorProduct = this.getById(id);
        Assert.notNull(vendorProduct, () -> new GlobalException("供应商商品不存在"));

        List<VendorProductGallery> galleries = vendorProductGalleryService.lambdaQuery()
                .eq(VendorProductGallery::getVendorProductId, vendorProduct.getId())
                .orderByAsc(VendorProductGallery::getSortOrder)
                .list();
        VendorProductVideo video = vendorProductVideoService.lambdaQuery().eq(VendorProductVideo::getVendorProductId, vendorProduct.getId()).one();
        List<VendorProductSku> skus = vendorProductSkuService.lambdaQuery().eq(VendorProductSku::getVendorProductId, vendorProduct.getId()).list();
        List<VendorProductSkuAttr> skuAttrs = vendorProductSkuAttrService.lambdaQuery().eq(VendorProductSkuAttr::getVendorProductId, vendorProduct.getId()).list();

        return new VendorProductPlatformDetailVO(vendorProduct, galleries, video, skus, skuAttrs);
    }

    @Transactional
    @Override
    public void audit(VendorProductAuditParam param) {
        // 参数校验
        param.validate();

        VendorProduct vendorProduct = this.getById(param.getVendorProductId());
        Assert.notNull(vendorProduct, () -> new GlobalException("供应商商品不存在"));

        Assert.isTrue(vendorProduct.getAuditState() == 0, () -> new GlobalException("供应商商品已审核"));

        vendorProduct.setAuditState(param.getAuditState());

        this.updateById(vendorProduct);

        // 创建供应商商品审核日志
        VendorProductAuditLog vendorProductAuditLog = param.createVendorProductAuditLog(vendorProduct.getVendorId());
        vendorProductAuditLogService.save(vendorProductAuditLog);
    }

    @Override
    public Page<VendorProductClientPageVO> clientPage(VendorProductClientPageQuery pageQuery) {
        Page<VendorProduct> page = this.buildSortOrder(pageQuery);

        Integer shopId = HeaderUtils.getShopId();
        List<Product> products = productMapper.selectList(
                Wrappers.lambdaQuery(Product.class).gt(Product::getVendorId, 0).eq(Product::getShopId, shopId)
        );
        Map<Integer, Product> productMap = products.stream().collect(Collectors.toMap(Product::getVendorProductId, product -> product));
        List<Long> vendorProductIds = products.stream().map(Product::getVendorProductId).map(Integer::longValue).toList();
        Page<VendorProduct> vendorProductPage = this.lambdaQuery()
                .eq(VendorProduct::getProductState, Constants.YES)
                .like(StrUtil.isNotBlank(pageQuery.getKeyword()), VendorProduct::getProductName, pageQuery.getKeyword())
                .eq(pageQuery.getVendorId() != null, VendorProduct::getVendorId, pageQuery.getVendorId())
                .eq(pageQuery.getProductBrandId() != null, VendorProduct::getProductBrandId, pageQuery.getProductBrandId())
                .eq(pageQuery.getProductCategoryId() != null, VendorProduct::getProductCategoryId, pageQuery.getProductCategoryId())
                .eq(VendorProduct::getAuditState, 1)
                .in(pageQuery.getIsCanImport() != null && Constants.NO.equals(pageQuery.getIsCanImport()), VendorProduct::getId, vendorProductIds)
                .notIn(pageQuery.getIsCanImport() != null && Constants.YES.equals(pageQuery.getIsCanImport()), VendorProduct::getId, vendorProductIds)
                .page(page);

        List<VendorProduct> records = vendorProductPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        List<Long> resultProductIds = records.stream().map(VendorProduct::getId).toList();
        List<VendorProductSku> vendorProductSkus = vendorProductSkuService.lambdaQuery().in(VendorProductSku::getVendorProductId, resultProductIds).list();
        Map<Long, List<VendorProductSku>> vendorProductSkuMap = vendorProductSkus.stream().collect(Collectors.groupingBy(VendorProductSku::getVendorProductId));

        List<Integer> categoryIds = records.stream().map(VendorProduct::getProductCategoryId).toList();
        List<Category> categories = categoryService.listByIds(categoryIds);
        Map<Integer, Category> categoryMap = categories.stream().collect(Collectors.toMap(Category::getCategoryId, Function.identity()));

        List<Integer> vendorIds = records.stream().map(VendorProduct::getVendorId).distinct().toList();
        List<Vendor> vendors = vendorService.lambdaQuery().in(Vendor::getVendorId, vendorIds).list();
        Map<Integer, Vendor> vendorMap = vendors.stream().collect(Collectors.toMap(Vendor::getVendorId, vendor -> vendor));

        List<VendorProductClientPageVO> resultRecords = records.stream()
                .map(vendorProduct -> {
                    List<VendorProductSku> currProductSkus = vendorProductSkuMap.get(vendorProduct.getId());
                    Vendor vendor = vendorMap.get(vendorProduct.getVendorId());
                    Category category = categoryMap.get(vendorProduct.getProductCategoryId());
                    Product product = productMap.get(vendorProduct.getId().intValue());
                    return new VendorProductClientPageVO(vendorProduct, currProductSkus, vendor, category, vendorProductIds, product);
                })
                .toList();

        return PageUtil.convertPage(page, resultRecords);
    }

    @Override
    public List<VendorProductBO> selectVendorProducts(List<Long> vendorProductIds) {
        if (CollUtil.isEmpty(vendorProductIds)) {
            return Collections.emptyList();
        }

        // 1. 查询供应商商品
        List<VendorProduct> vendorProducts = this.listByIds(vendorProductIds);
        if (CollUtil.isEmpty(vendorProducts)) {
            return Collections.emptyList();
        }

        List<Long> ids = vendorProducts.stream().map(VendorProduct::getId).toList();
        List<VendorProductVideo> videos = vendorProductVideoService.lambdaQuery().in(VendorProductVideo::getVendorProductId, ids).list();
        Map<Long, VendorProductVideo> videoMap = videos.stream().collect(Collectors.toMap(VendorProductVideo::getVendorProductId, Function.identity()));

        List<VendorProductGallery> allGalleries = vendorProductGalleryService.lambdaQuery().in(VendorProductGallery::getVendorProductId, ids).list();
        Map<Long, List<VendorProductGallery>> galleryMap = allGalleries.stream().collect(Collectors.groupingBy(VendorProductGallery::getVendorProductId));

        List<VendorProductSkuAttr> allSkuAttrs = vendorProductSkuAttrService.lambdaQuery().in(VendorProductSkuAttr::getVendorProductId, ids).list();
        Map<Long, List<VendorProductSkuAttr>> skuAttrMap = allSkuAttrs.stream().collect(Collectors.groupingBy(VendorProductSkuAttr::getVendorProductId));

        List<VendorProductSku> allSkus = vendorProductSkuService.lambdaQuery().in(VendorProductSku::getVendorProductId, ids).list();
        Map<Long, List<VendorProductSku>> skuMap = allSkus.stream().collect(Collectors.groupingBy(VendorProductSku::getVendorProductId));

        return vendorProducts.stream()
                .map(product -> {
                    VendorProductVideo video = videoMap.get(product.getId());
                    List<VendorProductGallery> galleries = galleryMap.get(product.getId());
                    List<VendorProductSkuAttr> skuAttrs = skuAttrMap.get(product.getId());
                    List<VendorProductSku> skus = skuMap.get(product.getId());

                    return new VendorProductBO(product, video, galleries, skuAttrs, skus);
                })
                .toList();
    }
}




