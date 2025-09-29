package com.tigshop.service.o2o;

import com.tigshop.bean.model.o2o.ShopPickupConfig;
import com.tigshop.service.common.BaseService;

/**
 * @author Admin
 * @description 针对表【shop_pickup_config(门店自提配置表)】的数据库操作Service
 * @createDate 2025-08-21 10:07:35
 */
public interface ShopPickupConfigService extends BaseService<ShopPickupConfig> {

    void initPickUpConfig(Integer shopId);

}
