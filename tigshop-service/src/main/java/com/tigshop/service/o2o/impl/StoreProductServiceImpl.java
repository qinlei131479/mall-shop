package com.tigshop.service.o2o.impl;

import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.product.ProductInventoryLog;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.o2o.StoreProductMapper;
import com.tigshop.mapper.product.ProductInventoryLogMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.o2o.StoreProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author kidd
 * @description 针对表【store_product(分配店铺商品)】的数据库操作Service实现
 * @createDate 2025-08-25 13:08:31
 */

@Service
@RequiredArgsConstructor
public class StoreProductServiceImpl extends BaseServiceImpl<StoreProductMapper, StoreProduct> implements StoreProductService {


    private final ProductInventoryLogMapper productInventoryLogMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public StoreProduct decStock(Integer shopId, Integer productId, Integer quantity) {
        // 1. 查询商品属性
        StoreProduct storeProduct = this.lambdaQuery()
                .eq(StoreProduct::getProductId, productId)
                .eq(StoreProduct::getShopId, shopId)
                .last("limit 1")
                .one();
        // 如果商品已被删除
        if (storeProduct == null) {
            return new StoreProduct();
        }

        // 2. 扣减商品库存
        this.lambdaUpdate()
                .eq(StoreProduct::getProductId, productId)
                .eq(StoreProduct::getShopId, shopId)
                .setDecrBy(StoreProduct::getProductStock, quantity)
                .update();

        // 3. 增加库存扣减日志
        int number = storeProduct.getProductStock() - quantity;
        ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                .productId(storeProduct.getProductId())
                .specId(0)
                .number(number)
                .addTime(StringUtils.getCurrentTime())
                .oldNumber(storeProduct.getProductStock())
                .type(2)
                .changeNumber(quantity)
                .desc("下单扣减库存")
                .shopId(Math.toIntExact(storeProduct.getShopId()))
                .build();
        productInventoryLogMapper.insert(productInventoryLog);
        return storeProduct;
    }

    @Override
    public StoreProduct incStock(Integer shopId, Integer productId, Integer quantity) {
        // 1. 查询商品属性
        StoreProduct storeProduct = this.lambdaQuery()
                .eq(StoreProduct::getProductId, productId)
                .eq(StoreProduct::getShopId, shopId)
                .last("limit 1")
                .one();

        // 如果商品已被删除
        if (storeProduct == null) {
            return new StoreProduct();
        }

        // 2. 增加商品库存
        this.lambdaUpdate()
                .eq(StoreProduct::getProductId, productId)
                .eq(StoreProduct::getShopId, shopId)
                .setIncrBy(StoreProduct::getProductStock, quantity)
                .update();

        // 3. 增加库存增加日志
        int number = storeProduct.getProductStock() - quantity;
        ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                .productId(storeProduct.getProductId())
                .specId(0)
                .number(number)
                .addTime(StringUtils.getCurrentTime())
                .oldNumber(storeProduct.getProductStock())
                .type(1)
                .changeNumber(quantity)
                .desc("取消订单返库存")
                .shopId(Math.toIntExact(storeProduct.getShopId()))
                .build();
        productInventoryLogMapper.insert(productInventoryLog);
        return storeProduct;
    }
}




