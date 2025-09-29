package com.tigshop.service.o2o.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.model.o2o.AreaStoreManagerShop;
import com.tigshop.bean.model.o2o.ProductOrg;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.o2o.StoreProductSku;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.param.o2o.ProductOrgParam;
import com.tigshop.bean.vo.o2o.ProductOrgVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.o2o.ProductOrgMapper;
import com.tigshop.service.o2o.AreaStoreManagerShopService;
import com.tigshop.service.o2o.ProductOrgService;
import com.tigshop.service.o2o.StoreProductService;
import com.tigshop.service.o2o.StoreSkuService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author kidd
 */
@RequiredArgsConstructor
@Service
public class ProductOrgServiceImpl extends ServiceImpl<ProductOrgMapper, ProductOrg> implements ProductOrgService {

    private final AreaStoreManagerShopService areaStoreManagerShopService;
    private final ProductService productService;
    private final ProductSkuService productSkuService;
    private final StoreProductService storeProductService;
    private final StoreSkuService storeSkuService;

    @Transactional
    @Override
    public void distribution(ProductOrgParam param) {
        Product product = productService.getById(param.getProductId());
        Assert.notNull(product, () -> new GlobalException("商品不存在"));

        List<ProductSku> skus = productSkuService.lambdaQuery().eq(ProductSku::getProductId, param.getProductId()).list();

        /*List<ProductOrg> productOrgs = param.buildProductDistributions();
        this.saveBatch(productOrgs);*/

        List<Long> regionOrgIds = param.getRegionOrgIds();
        List<AreaStoreManagerShop> areaStores = areaStoreManagerShopService.lambdaQuery()
                .in(AreaStoreManagerShop::getAreaStoreManagerId, regionOrgIds)
                .list();
        Set<Integer> shopIds = areaStores.stream().map(AreaStoreManagerShop::getShopId).collect(Collectors.toSet());
        shopIds.addAll(param.getShopOrgIds());

        for (Integer shopId : shopIds) {
            StoreProduct storeProduct = StoreProduct.builder()
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .productPrice(product.getProductPrice())
                    .productStatus(Constants.NO)
                    .productStock(product.getProductStock())
                    .shopId(shopId.longValue())
                    .build();
            storeProductService.save(storeProduct);

            List<StoreProductSku> storeProductSkus = skus.stream()
                    .map(sku -> StoreProductSku.builder()
                            .productId(product.getProductId().longValue())
                            .productSkuId(sku.getSkuId().longValue())
                            .skuPrice(sku.getSkuPrice())
                            .skuStock(sku.getSkuStock())
                            .storeProductId(storeProduct.getStoreProductId())
                            .build())
                    .toList();
            storeSkuService.saveBatch(storeProductSkus);
        }
    }

    @Override
    public ProductOrgVO info(Long productId) {
        List<ProductOrg> productOrgs = this.lambdaQuery().eq(ProductOrg::getProductId, productId).list();

        return new ProductOrgVO(productId, productOrgs);
    }
}




