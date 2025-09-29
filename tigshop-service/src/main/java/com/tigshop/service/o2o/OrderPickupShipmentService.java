package com.tigshop.service.o2o;

import com.tigshop.bean.model.o2o.OrderPickupShipment;
import com.tigshop.bean.vo.o2o.OrderPickupShipmentVO;
import com.tigshop.bean.vo.o2o.OrderPickupTimeVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 订单提货
 *
 * @author Tigshop项目组
 * @create 2025年08月27日 09:53
 */
public interface OrderPickupShipmentService extends BaseService<OrderPickupShipment> {
    /**
     * 保存订单提货信息(初步)
     *
     * @param orderPickupShipment 订单提货信息
     */
    void saveOrderPickupShipment(OrderPickupShipment orderPickupShipment);

    /**
     * 订单备货
     *
     * @param orderId 订单id
     */
    void stockUp(Integer orderId);

    /**
     * 订单核销
     *
     * @param orderId 单号
     */
    void writeOff(Integer orderId);

    /**
     * 获取订单提货信息
     *
     * @param orderId 订单id
     * @return 订单提货信息
     */
    OrderPickupShipmentVO getOrderPickupShipment(Integer orderId);

    /**
     * 获取门店提货时间段
     *
     * @param shopId 门店id
     * @return 提货时间段
     */
    List<OrderPickupTimeVO> getShopPickupTpl(Integer shopId);
}
