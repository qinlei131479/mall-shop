package com.tigshop.service.o2o.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.o2o.StoreProductSku;
import com.tigshop.bean.model.product.ProductInventoryLog;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.o2o.StoreSkuMapper;
import com.tigshop.mapper.product.ProductInventoryLogMapper;
import com.tigshop.service.o2o.StoreSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author kidd
 * @description 针对表【store_sku(分配店铺 sku)】的数据库操作Service实现
 * @createDate 2025-08-25 13:08:31
 */
@Service
@RequiredArgsConstructor
public class StoreSkuServiceImpl extends ServiceImpl<StoreSkuMapper, StoreProductSku> implements StoreSkuService {

    private final ProductInventoryLogMapper productInventoryLogMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void decStock(StoreProduct storeProduct, Integer shopId, Integer skuId, Integer quantity) {
        // 1. 查询商品属性
        StoreProductSku storeProductSku = this.lambdaQuery()
                .eq(StoreProductSku::getProductSkuId, skuId)
                .eq(StoreProductSku::getStoreProductId, storeProduct.getStoreProductId())
                .last("limit 1")
                .one();
        // 如果商品规格已被删除
        if (storeProductSku == null) {
            return;
        }
        // 2. 扣减商品库存
        this.lambdaUpdate()
                .eq(StoreProductSku::getProductSkuId, skuId)
                .eq(StoreProductSku::getStoreProductId, storeProduct.getStoreProductId())
                .setDecrBy(StoreProductSku::getSkuStock, quantity)
                .update();

        // 3. 增加库存扣减日志
        int number = storeProductSku.getSkuStock() - quantity;
        ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                .productId(Math.toIntExact(storeProductSku.getProductId()))
                .specId(skuId)
                .number(number)
                .addTime(StringUtils.getCurrentTime())
                .oldNumber(storeProductSku.getSkuStock())
                .type(2)
                .changeNumber(quantity)
                .desc("下单扣减库存")
                .shopId(Math.toIntExact(storeProduct.getShopId()))
                .build();
        productInventoryLogMapper.insert(productInventoryLog);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incStock(StoreProduct storeProduct, Integer shopId, Integer skuId, Integer quantity) {
        // 1. 查询商品属性
        StoreProductSku storeProductSku = this.lambdaQuery()
                .eq(StoreProductSku::getProductSkuId, skuId)
                .eq(StoreProductSku::getStoreProductId, storeProduct.getStoreProductId())
                .last("limit 1")
                .one();
        // 如果商品规格已被删除
        if (storeProductSku == null) {
            return;
        }
        // 2. 增加商品库存
        this.lambdaUpdate()
                .eq(StoreProductSku::getProductSkuId, skuId)
                .eq(StoreProductSku::getStoreProductId, storeProduct.getStoreProductId())
                .setIncrBy(StoreProductSku::getSkuStock, quantity)
                .update();

        // 3. 增加库存扣减日志
        int number = storeProductSku.getSkuStock() + quantity;
        ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                .productId(Math.toIntExact(storeProductSku.getProductId()))
                .specId(skuId)
                .number(number)
                .addTime(StringUtils.getCurrentTime())
                .oldNumber(storeProductSku.getSkuStock())
                .type(1)
                .changeNumber(quantity)
                .desc("取消订单返还")
                .shopId(Math.toIntExact(storeProduct.getShopId()))
                .build();
        productInventoryLogMapper.insert(productInventoryLog);
    }
}




