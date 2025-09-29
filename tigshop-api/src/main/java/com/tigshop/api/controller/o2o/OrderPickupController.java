package com.tigshop.api.controller.o2o;

import com.tigshop.bean.vo.o2o.DeliveryOptionVO;
import com.tigshop.bean.vo.o2o.OrderPickupShipmentVO;
import com.tigshop.bean.vo.o2o.OrderPickupTimeVO;
import com.tigshop.service.o2o.OrderPickupShipmentService;
import com.tigshop.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Tigshop项目组
 * @create 2025年08月28日 20:30
 */
@Tag(name = "自提订单")
@RestController
@RequestMapping("/api/o2o/orderPickup")
@RequiredArgsConstructor
public class OrderPickupController {
    private final ProductService productService;
    private final OrderPickupShipmentService orderPickupShipmentService;

    @GetMapping("/getDeliveryOptions")
    @Operation(summary = "获取商品配送方式")
    public DeliveryOptionVO getDeliveryOptions(@RequestParam(name = "shopId", required = false) Integer shopId) {
        return productService.getDeliveryOption(shopId);
    }

    @GetMapping("/getOrderPickupShipment")
    @Operation(summary = "获取订单自提信息")
    public OrderPickupShipmentVO getOrderPickupShipment(@RequestParam Integer orderId) {
        return orderPickupShipmentService.getOrderPickupShipment(orderId);
    }

    @Operation(summary = "获取订单时间段自提信息")
    @GetMapping("/getShopPickupTpl")
    public List<OrderPickupTimeVO> getShopPickupTpl(@RequestParam(required = false) Integer shopId) {
        return orderPickupShipmentService.getShopPickupTpl(shopId);
    }
}