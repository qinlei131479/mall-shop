package com.tigshop.bean.vo.o2o;

import com.tigshop.bean.model.order.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Tigshop项目组
 * @create 2025年08月28日 19:39
 */
@Schema(description = "订单自提信息")
@Data
public class OrderPickupShipmentVO {
    @Schema(description = "自提手机号")
    private String mobile;
    @Schema(description = "核销二维码")
    private String pickupQrCode;
    @Schema(description = "核销sn")
    private String pickupSn;
    @Schema(description = "提货状态")
    private Integer shipmentStatus;
    @Schema(description = "提货状态名称")
    private String shipmentStatusName;
    @Schema(description = "订单商品信息")
    private List<OrderItem> orderItems;
    @Schema(description = "自提地址")
    private String shopDetailedAddress;
    @Schema(description = "自提点名称")
    private String pickupName;
}