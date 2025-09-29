package com.tigshop.service.o2o;

import com.tigshop.bean.model.o2o.OrderPickupItem;
import com.tigshop.service.common.BaseService;

/**
 * @author Tigshop项目组
 * @create 2025年08月28日 15:14
 */
public interface OrderPickupItemService extends BaseService<OrderPickupItem> {
    /**
     * 保存订单自提信息
     * @param orderPickupItem 订单自提信息
     */
    void saveOrderPickupItem(OrderPickupItem orderPickupItem);
}
