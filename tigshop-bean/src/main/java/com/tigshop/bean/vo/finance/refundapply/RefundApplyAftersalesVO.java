package com.tigshop.bean.vo.finance.refundapply;

import cn.hutool.core.collection.CollUtil;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.order.ShippingStatusEnum;
import com.tigshop.bean.model.finance.RefundApply;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 退货申请售后
 *
 * @author kidd
 * @since 2025/4/27 14:28
 */
@Schema(description = "退货申请售后")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundApplyAftersalesVO {

    // *** Aftersales ***

    @Schema(description = "售后申请表ID")
    private Integer aftersaleId;

    @Schema(description = "申请类型：1、退货退款，2、换货，3、维修，4、其它，5，仅退款")
    private Integer aftersaleType;

    @Schema(description = "状态：1、正在审核处理，2、售后审核通过,待处理，3、售后审核未通过，4、售后要求寄回，5、快递已寄回，6、已完成，7、已取消")
    private Integer status;

    @Schema(description = "用户凭证")
    private String pics;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "回复")
    private String reply;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "物流编号")
    private String trackingNo;

    @Schema(description = "物流公司")
    private String logisticsName;

    @Schema(description = "退款地址和联系电话")
    private String returnAddress;

    @Schema(description = "售后原因")
    private String aftersaleReason;

    @Schema(description = "售后编号")
    private String aftersalesSn;

    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "可退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "审核时间")
    private Long auditTime;

    @Schema(description = "处理时间")
    private Long dealTime;

    @Schema(description = "完结时间")
    private Long finalTime;

    // *** Order ***

    @Schema(description = "订单信息")
    private Order orders;

    public RefundApplyAftersalesVO(Aftersales aftersales, Order order, List<AftersalesItem> aftersalesItems, List<OrderItem> orderItems, List<RefundApply> refundApplies, List<AftersalesItem> orderAllAftersalesItems) {
        this.aftersaleId = aftersales.getAftersaleId();
        this.aftersaleType = aftersales.getAftersaleType();
        this.status = aftersales.getStatus();
        this.pics = aftersales.getPics();
        this.description = aftersales.getDescription();
        this.reply = aftersales.getReply();
        this.addTime = aftersales.getAddTime();
        this.trackingNo = aftersales.getTrackingNo();
        this.logisticsName = aftersales.getLogisticsName();
        this.returnAddress = aftersales.getReturnAddress();
        this.aftersaleReason = aftersales.getAftersaleReason();
        this.aftersalesSn = aftersales.getAftersalesSn();
        this.orderId = aftersales.getOrderId();
        this.userId = aftersales.getUserId();

        if (ShippingStatusEnum.PENDING.getCode() == order.getShippingStatus()) {
            // 判断是不是全部商品都售后了
            //  获取全部的售后商品
            Map<Integer, List<AftersalesItem>> aftersalesItemMap = orderAllAftersalesItems.stream().collect(Collectors.groupingBy(AftersalesItem::getOrderItemId));
            boolean allAftersalesFlag = orderItems.stream().allMatch(item -> {
                List<AftersalesItem> curr = aftersalesItemMap.get(item.getItemId());
                if (CollUtil.isEmpty(curr)) {
                    return false;
                }
                Integer itemNum = curr.stream().map(AftersalesItem::getNumber).reduce(0, Integer::sum);
                return Objects.equals(item.getQuantity(), itemNum);
            });

            // 还要加一个判断，防止同时有多个待处理的退款申请，只有最后一个需要加上运费，根据待处理的情况判断
            long isReviewCount = refundApplies.stream().filter(item -> item.getRefundStatus() == 0).count();
            if (isReviewCount == 1) {
                this.refundAmount = allAftersalesFlag ? aftersales.getRefundAmount().add(order.getShippingFee()) : aftersales.getRefundAmount();
            } else {
                this.refundAmount = aftersales.getRefundAmount();
            }

        } else if (ShippingStatusEnum.SENT.getCode() == order.getShippingStatus()) {
            this.refundAmount = aftersales.getRefundAmount();
        } else {
            this.refundAmount = aftersales.getRefundAmount();
        }

        // 如果订单状态为已完成，发货状态为已收货
        if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus() && ShippingStatusEnum.SHIPPED.getCode() == order.getShippingStatus()) {
            if (order.getShippingTime() != null && order.getShippingTime() > 0) {
                this.refundAmount = aftersales.getRefundAmount();
            } else {
                // 判断是不是全部商品都售后了
                Map<Integer, List<AftersalesItem>> aftersalesItemMap = aftersalesItems.stream().collect(Collectors.groupingBy(AftersalesItem::getOrderItemId));
                boolean allAftersalesFlag = orderItems.stream().allMatch(item -> {
                    List<AftersalesItem> curr = aftersalesItemMap.get(item.getItemId());
                    if (CollUtil.isEmpty(curr)) {
                        return false;
                    }
                    Integer itemNum = curr.stream().map(AftersalesItem::getNumber).reduce(0, Integer::sum);
                    return Objects.equals(item.getQuantity(), itemNum);
                });

                this.refundAmount = allAftersalesFlag ? aftersales.getRefundAmount().add(order.getShippingFee()) : aftersales.getRefundAmount();
            }
        }

        this.shopId = aftersales.getShopId();
        this.auditTime = aftersales.getAuditTime();
        this.dealTime = aftersales.getDealTime();
        this.finalTime = aftersales.getFinalTime();

        this.orders = order;
    }
}
