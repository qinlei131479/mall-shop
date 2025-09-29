package com.tigshop.service.o2o.impl;

import com.tigshop.bean.model.o2o.OrderPickupItem;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.mapper.o2o.OrderPickupItemMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.o2o.OrderPickupItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Tigshop项目组
 * @create 2025年08月28日 15:15
 */
@Service
@RequiredArgsConstructor
public class OrderPickupItemServiceImpl extends BaseServiceImpl<OrderPickupItemMapper, OrderPickupItem> implements OrderPickupItemService {

    private final TigshopProperties tigshopProperties;

    @Override
    public void saveOrderPickupItem(OrderPickupItem orderPickupItem) {
        boolean isO2o = tigshopProperties.getIsO2o() == 1;
        if (!isO2o) {
            return;
        }
        this.save(orderPickupItem);
    }
}