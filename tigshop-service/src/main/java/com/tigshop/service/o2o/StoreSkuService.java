package com.tigshop.service.o2o;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.o2o.StoreProductSku;

/**
 * @author kidd
 * @description 针对表【store_sku(分配店铺 sku)】的数据库操作Service
 * @createDate 2025-08-25 13:08:31
 */
public interface StoreSkuService extends IService<StoreProductSku> {

    /**
     * 减库存
     *
     * @param skuId    skuId
     * @param quantity 数量
     */
    void decStock(StoreProduct storeProduct, Integer shopId, Integer skuId, Integer quantity);

    /**
     * 加库存
     *
     * @param skuId    skuId
     * @param quantity 数量
     */
    void incStock(StoreProduct storeProduct, Integer shopId, Integer skuId, Integer quantity);

}
