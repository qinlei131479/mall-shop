package com.tigshop.service.o2o.impl;

import com.tigshop.bean.model.o2o.ShopPickupConfig;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.o2o.ShopPickupConfigMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.o2o.ShopPickupConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tigshop.common.constant.Constants.NO;

/**
* @author Admin
* @description 针对表【shop_pickup_config(门店自提配置表)】的数据库操作Service实现
* @createDate 2025-08-21 10:07:35
*/
@Service
public class ShopPickupConfigServiceImpl extends BaseServiceImpl<ShopPickupConfigMapper, ShopPickupConfig>
    implements ShopPickupConfigService {

    static final String DEFAULT_PICKUP_NAME = "门店自提";
    static final String PICKUP_CONFIG_FIELD = "[{\"fieldKey\":\"pickupPoint\",\"fieldName\":\"自提点\",\"required\":true,\"visible\":true},{\"fieldKey\":\"pickupTime\",\"fieldName\":\"提货时间\",\"required\":true,\"visible\":true},{\"fieldKey\":\"pickuperPhone\",\"fieldName\":\"提货人手机号\",\"required\":true,\"visible\":true},{\"fieldKey\":\"pickuperName\",\"fieldName\":\"提货人姓名\",\"required\":false,\"visible\":false}]";

    @Override
    @Transactional
    public void initPickUpConfig(Integer shopId) {
        ShopPickupConfig pickupConfig = new ShopPickupConfig();
        pickupConfig.setShopId(Long.valueOf(shopId));
        pickupConfig.setPickupName(DEFAULT_PICKUP_NAME);
        pickupConfig.setStatus(NO);
        pickupConfig.setPickupFiled(PICKUP_CONFIG_FIELD);
        pickupConfig.setCreateTime(StringUtils.getCurrentTime());
        pickupConfig.setUpdateTime(StringUtils.getCurrentTime());
        this.save(pickupConfig);
    }

//    @Override
//    public void updatePickUpConfig(ShopPickupConfig param) {
//        ShopPickupConfig pickupConfig = new ShopPickupConfig();
//        pickupConfig.setShopId(param.getShopId());
//        pickupConfig.setPickupName(param.getPickupName());
//        pickupConfig.setStatus(param.getStatus());
//        pickupConfig.setPickupFiled(param.getPickupFiled());
//        pickupConfig.setUpdateTime(StringUtils.getCurrentTime());
//        this.save(pickupConfig);
//    }
}




