package com.tigshop.service.o2o.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.dto.o2o.pickup.PickupTimeJson;
import com.tigshop.bean.enums.o2o.ShipmentStatusEnum;
import com.tigshop.bean.model.cart.Cart;
import com.tigshop.bean.model.o2o.OrderPickupItem;
import com.tigshop.bean.model.o2o.OrderPickupShipment;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.vo.o2o.OrderPickupShipmentVO;
import com.tigshop.bean.vo.o2o.OrderPickupTimeVO;
import com.tigshop.bean.vo.o2o.ShopPickupConfigVO;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.cart.CartMapper;
import com.tigshop.mapper.o2o.OrderPickupItemMapper;
import com.tigshop.mapper.o2o.OrderPickupShipmentMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.o2o.OrderPickupShipmentService;
import com.tigshop.service.o2o.ShopPickupTplService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * @author Tigshop项目组
 * @create 2025年08月27日 09:55
 */
@Service
@RequiredArgsConstructor
public class OrderPickupShipmentServiceImpl extends BaseServiceImpl<OrderPickupShipmentMapper, OrderPickupShipment> implements OrderPickupShipmentService {

    private final TigshopProperties tigshopProperties;
    private final OrderPickupItemMapper orderPickupItemMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final ShopMapper shopMapper;
    private final ShopPickupTplService shopPickupTplService;
    private final CartMapper cartMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrderPickupShipment(OrderPickupShipment orderPickupShipment) {
        boolean isO2o = tigshopProperties.getIsO2o() == 1;
        if (!isO2o) {
            return;
        }

        // 查询订单相关提货信息
        List<OrderPickupItem> orderPickupItems = orderPickupItemMapper.selectList(Wrappers.lambdaQuery(OrderPickupItem.class)
                .eq(OrderPickupItem::getOrderId, orderPickupShipment.getOrderId())
                .eq(OrderPickupItem::getShopId, orderPickupShipment.getShopId()));

        // 构建自提订单信息
        List<OrderPickupShipment> orderPickupShipments = getOrderPickupShipments(orderPickupItems);
        this.saveBatch(orderPickupShipments);
    }

    /**
     * 构建自提订单信息
     */
    private List<OrderPickupShipment> getOrderPickupShipments(List<OrderPickupItem> orderPickupItems) {
        List<OrderPickupShipment> orderPickupShipments = new ArrayList<>();
        for (OrderPickupItem orderPickupItem : orderPickupItems) {
            OrderPickupShipment addOrderPickupShipment = new OrderPickupShipment();
            addOrderPickupShipment.setShipmentStatus(ShipmentStatusEnum.NOT_SHIPPED.getCode());
            addOrderPickupShipment.setPickupId(orderPickupItem.getPickupId());
            addOrderPickupShipment.setOrderId(orderPickupItem.getOrderId());
            addOrderPickupShipment.setShopId(orderPickupItem.getShopId());
            orderPickupShipments.add(addOrderPickupShipment);
        }
        return orderPickupShipments;
    }

    @Override
    public void stockUp(Integer orderId) {
        boolean isO2o = tigshopProperties.getIsO2o() == 1;
        if (!isO2o) {
            return;
        }
        Long count = orderPickupItemMapper.selectCount(Wrappers.lambdaQuery(OrderPickupItem.class).eq(OrderPickupItem::getOrderId, orderId));
        if (count == 0) {
            // 不是自提
            return;
        }

        OrderPickupShipment orderPickupShipment = this.lambdaQuery().eq(OrderPickupShipment::getOrderId, orderId)
                .last("limit 1")
                .one();

        Assert.notNull(orderPickupShipment, "自提订单不存在");

        long epochMilli = Instant.now().toEpochMilli();
        String qrCode = generateVerificationQrCode(String.valueOf(epochMilli));

        orderPickupShipment.setPickupSn(String.valueOf(epochMilli));
        orderPickupShipment.setPickupQrCode(qrCode);
        orderPickupShipment.setShipmentStatus(ShipmentStatusEnum.SHIPPED.getCode());
        this.updateById(orderPickupShipment);
    }

    @Override
    public void writeOff(Integer orderId) {
        boolean isO2o = tigshopProperties.getIsO2o() == 1;
        if (!isO2o) {
            return;
        }
        Long count = orderPickupItemMapper.selectCount(Wrappers.lambdaQuery(OrderPickupItem.class).eq(OrderPickupItem::getOrderId, orderId));
        if (count == 0) {
            // 不是自提
            return;
        }

        OrderPickupShipment orderPickupShipment = this.lambdaQuery().eq(OrderPickupShipment::getOrderId, orderId)
                .last("limit 1").one();
        Assert.notNull(orderPickupShipment, "自提订单不存在");
        Assert.isTrue(Objects.equals(orderPickupShipment.getShipmentStatus(), ShipmentStatusEnum.SHIPPED.getCode()), "自提订单已核销");
        orderPickupShipment.setShipmentStatus(ShipmentStatusEnum.RECEIVED.getCode());
        orderPickupShipment.setCheckTime(Integer.valueOf(StringUtils.getCurrentTime().toString()));
        this.updateById(orderPickupShipment);
    }

    @Override
    public OrderPickupShipmentVO getOrderPickupShipment(Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        Assert.notNull(order, "订单不存在");
        OrderPickupShipmentVO orderPickupShipmentVO = new OrderPickupShipmentVO();
        String mobile = order.getMobile();
        orderPickupShipmentVO.setMobile(mobile);
        // 查询订单商品信息
        List<OrderItem> orderItems = orderItemMapper.selectList(Wrappers.lambdaQuery(OrderItem.class).eq(OrderItem::getOrderId, orderId));
        orderPickupShipmentVO.setOrderItems(orderItems);
        // 获取核销码
        OrderPickupShipment orderPickupShipment = this.lambdaQuery().eq(OrderPickupShipment::getOrderId, orderId).last("limit 1").one();
        if (orderPickupShipment != null) {
            // 核销码
            String pickupQrCode = orderPickupShipment.getPickupQrCode();
            orderPickupShipmentVO.setPickupQrCode(pickupQrCode);
            // 核销sn
            String pickupSn = orderPickupShipment.getPickupSn();
            orderPickupShipmentVO.setPickupSn(pickupSn);
            // 核销状态
            Integer shipmentStatus = orderPickupShipment.getShipmentStatus();
            orderPickupShipmentVO.setShipmentStatus(shipmentStatus);
            String description = EnumUtil.getBy(ShipmentStatusEnum::getCode, shipmentStatus).getDescription();
            orderPickupShipmentVO.setShipmentStatusName(description);
            // 自提点地址
            Integer pickupId = orderPickupShipment.getPickupId();
            Shop shop = shopMapper.selectById(pickupId);
            String shopDetailedAddress = shop.getShopDetailedAddress();
            orderPickupShipmentVO.setShopDetailedAddress(shopDetailedAddress);
            // 自提点名称
            orderPickupShipmentVO.setPickupName(shop.getShopTitle());
        }
        return orderPickupShipmentVO;
    }

    @Override
    public List<OrderPickupTimeVO> getShopPickupTpl(Integer shopId) {
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        List<Cart> carts = cartMapper.selectList(Wrappers.lambdaQuery(Cart.class)
                .eq(Cart::getUserId, currentUserId)
                .eq(Cart::getIsChecked, 1)
                .eq(shopId != null, Cart::getShopId, shopId));

        if (CollectionUtils.isEmpty(carts)) {
            return List.of();
        }

        List<OrderPickupTimeVO> orderPickupTimeList = new ArrayList<>();
        for (Cart cart : carts) {
            // 获取店铺自提模板详情
            ShopPickupConfigVO shopPickupTpl = shopPickupTplService.getShopPickupTpl(cart.getShopId());
            Integer pickupTimeStatus = shopPickupTpl.getPickupTimeStatus();
            // 获取自提时间段
            PickupTimeJson pickupTimeJson = shopPickupTpl.getPickupTimeJson();
            List<PickupTimeJson.TimeStartAndEnd> timeList = pickupTimeJson.getTimeList();

            OrderPickupTimeVO vo = OrderPickupTimeVO.builder()
                    .pickupTimeStatus(pickupTimeStatus)
                    .timeList(timeList)
                    .shopId(cart.getShopId())
                    .build();
            orderPickupTimeList.add(vo);
        }

        return orderPickupTimeList;
    }

    /**
     * 生成核销二维码 (返回 Base64 图片字符串)
     */
    public String generateVerificationQrCode(String verificationCode) {
        // 配置二维码样式
        QrConfig config = new QrConfig(300, 300);
        config.setMargin(2);
        config.setForeColor(Color.BLACK);
        config.setBackColor(Color.WHITE);

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            QrCodeUtil.generate(verificationCode, config, "png", output);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (Exception e) {
            log.error("二维码生成失败", e);
        }
        return "";
    }
}