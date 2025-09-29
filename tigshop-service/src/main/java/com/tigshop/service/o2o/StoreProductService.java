package com.tigshop.service.o2o;

import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.service.common.BaseService;

/**
 * @author kidd
 * @description 针对表【store_product(分配店铺商品)】的数据库操作Service
 * @createDate 2025-08-25 13:08:31
 */
public interface StoreProductService extends BaseService<StoreProduct> {


    /**
     * 减库存
     *
     * @param productId 商品ID
     * @param quantity  数量
     */
    StoreProduct decStock(Integer shopId, Integer productId, Integer quantity);

    /**
     * 增库存
     *
     * @param productId 商品ID
     * @param quantity 数量
     */
    StoreProduct incStock(Integer shopId, Integer productId, Integer quantity);
}
