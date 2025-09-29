package com.tigshop.service.finance.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.enums.finance.RefundLogTypeEnum;
import com.tigshop.bean.enums.msg.MessageTypeEnum;
import com.tigshop.bean.enums.order.AftersalesStatusEnum;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.order.ShippingStatusEnum;
import com.tigshop.bean.model.finance.*;
import com.tigshop.bean.model.order.*;
import com.tigshop.bean.model.product.ECard;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.user.UserPointsLog;
import com.tigshop.bean.param.finance.refundapply.RefundApplyAuditParam;
import com.tigshop.bean.param.finance.refundapply.RefundApplyEditParam;
import com.tigshop.bean.param.finance.refundapply.RefundApplySaveParam;
import com.tigshop.bean.param.merchant.OfflineAuditParam;
import com.tigshop.bean.query.finance.RefundApplyPageQuery;
import com.tigshop.bean.vo.finance.refundapply.RefundApplyDetailVO;
import com.tigshop.bean.vo.finance.refundapply.RefundApplyVO;
import com.tigshop.bean.vo.order.pay.PayNotifyVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.finance.RefundApplyMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.product.ECardMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.product.ProductSkuMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.*;
import com.tigshop.service.msgTemplate.MessageService;
import com.tigshop.service.order.AftersalesItemService;
import com.tigshop.service.order.AftersalesService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.pay.PaymentService;
import com.tigshop.service.pay.impl.AliPayServiceImpl;
import com.tigshop.service.pay.impl.PaypalServiceImpl;
import com.tigshop.service.pay.impl.WechatPayServiceImpl;
import com.tigshop.service.user.UserBalanceService;
import com.tigshop.service.user.UserPointsLogService;
import com.tigshop.service.user.UserRankService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.finance.RefundApplyConstants.*;

/**
 * 退款申请服务实现类
 *
 * @author kidd
 * @create 2025/7/7
 */
@RequiredArgsConstructor
@Service
public class RefundApplyServiceImpl extends BaseServiceImpl<RefundApplyMapper, RefundApply> implements RefundApplyService {

    private final OrderFinanceAftersalesService orderFinanceAftersalesService;
    private final AftersalesService aftersalesService;
    private final OrderService orderService;
    private final OrderItemMapper orderItemMapper;
    private final PaylogService paylogService;
    private final PaylogRefundService paylogRefundService;
    private final WechatPayServiceImpl wechatPayService;
    private final AliPayServiceImpl aliPayService;
    private final PaypalServiceImpl paypalService;
    private final PaymentService paymentService;
    private final RefundLogService refundLogService;
    private final UserBalanceService userBalanceService;
    private final AftersalesItemService aftersalesItemService;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ECardMapper eCardMapper;
    private final UserRankService userRankService;
    private final MessageService messageService;
    private final UserPointsLogService userPointsLogService;

    @Override
    public Map<Integer, String> config() {
        return REFUND_STATUS_NAME;
    }

    @Override
    public Page<RefundApplyVO> list(RefundApplyPageQuery pageQuery) {
        Page<RefundApply> page = buildSortOrder(pageQuery);
        Page<RefundApplyVO> resultPage = this.baseMapper.page(page, pageQuery);

        List<RefundApplyVO> records = resultPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        List<Integer> aftersalesIds = records.stream().map(RefundApplyVO::getAftersaleId).toList();
        List<OrderFinanceAftersales> orderFinanceAftersalesList = orderFinanceAftersalesService.listByIds(aftersalesIds);

        records.forEach(record -> record.assembleData(orderFinanceAftersalesList));
        return resultPage;
    }

    @Override
    public RefundApplyDetailVO detail(Integer id) {
        RefundApply refundApply = this.getById(id);
        Assert.notNull(refundApply, () -> new GlobalException("该申请不存在"));

        Order order = orderService.lambdaQuery().eq(Order::getOrderId, refundApply.getOrderId()).one();
        List<OrderItem> orderItems = orderItemMapper.selectList(
                Wrappers.lambdaQuery(OrderItem.class).eq(OrderItem::getOrderId, order.getOrderId())
        );

        // 排除退款成功的支付金额
        List<RefundApply> completedRefundApplies = this.lambdaQuery()
                .eq(RefundApply::getOrderId, refundApply.getOrderId())
                .in(RefundApply::getRefundStatus, REFUND_STATUS_PROCESSING, REFUND_STATUS_PROCESSED)
                .list();
        BigDecimal completeOnlineBalance = completedRefundApplies.stream()
                .map(RefundApply::getOnlineBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // 已完成的总金额
        BigDecimal totalCompleteAmount = completedRefundApplies.stream()
                .map(item -> item.getRefundBalance().add(item.getOnlineBalance()).add(item.getOfflineBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 售后协商可退金额必填 refund_amount
        Aftersales aftersales = aftersalesService.lambdaQuery().eq(Aftersales::getAftersaleId, refundApply.getAftersaleId()).one();
        BigDecimal refundAmount = aftersales.getRefundAmount();

        // 真实的线上剩余可退金额
        BigDecimal effectiveOnlineBalance = order.getOnlinePaidAmount().subtract(completeOnlineBalance);
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            effectiveOnlineBalance = effectiveOnlineBalance.compareTo(refundAmount) > 0 ? refundAmount : effectiveOnlineBalance;
        }

        List<Integer> productIds = orderItems.stream().map(OrderItem::getProductId).toList();
        List<Product> products = productMapper.selectBatchIds(productIds);

        List<Integer> skuIds = orderItems.stream().map(OrderItem::getSkuId).toList();
        List<ProductSku> productSkus = productSkuMapper.selectBatchIds(skuIds);

        List<Integer> orderItemIds = orderItems.stream().map(OrderItem::getItemId).toList();
        List<ECard> eCards = eCardMapper.selectList(
                Wrappers.lambdaQuery(ECard.class).in(ECard::getOrderItemId, orderItemIds)
        );

        List<AftersalesItem> aftersalesItems = aftersalesItemService.lambdaQuery().eq(AftersalesItem::getAftersaleId, refundApply.getAftersaleId()).list();
        List<RefundApply> refundApplies = this.lambdaQuery().eq(RefundApply::getOrderId, refundApply.getOrderId()).list();

        List<Aftersales> orderCompleteAftersales = aftersalesService.lambdaQuery().eq(Aftersales::getOrderId, refundApply.getOrderId()).eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode()).list();
        List<Integer> aftersalesIds = orderCompleteAftersales.stream().map(Aftersales::getAftersaleId).toList();
        List<AftersalesItem> orderAllAftersalesItems = aftersalesItemService.lambdaQuery().in(AftersalesItem::getAftersaleId, aftersalesIds).list();

        return new RefundApplyDetailVO(refundApply, order, orderItems, products, productSkus, eCards, aftersales, aftersalesItems, effectiveOnlineBalance, totalCompleteAmount, refundApplies, orderAllAftersalesItems);
    }

    @Transactional
    @Override
    public void audit(RefundApplyAuditParam param) {
        param.initParam();

        RefundApply refundApply = this.getById(param.getRefundId());
        Assert.notNull(refundApply, () -> new GlobalException("该申请不存在"));
        Assert.isTrue(refundApply.getRefundStatus() == 0, () -> new GlobalException("申请状态值错误"));

        Order order = orderService.getById(refundApply.getOrderId());

        // 1. 校验是否可以通过申请
        BigDecimal totalRefundAmount = this.checkAudit(param, refundApply, order);

        // 2. 创建退款流水
        if (param.getRefundStatus() == 1) {
            this.createRefundFlow(param, refundApply, order);
        }

        this.updateById(refundApply);

        // 退回库存
        aftersalesItemService.returnStock(refundApply.getAftersaleId());

        // 发送退款消息
        if (refundApply.getRefundStatus() == 2) {
            messageService.sendUserMessage(refundApply.getUserId(), refundApply.getOrderId(), MessageTypeEnum.ORDER_REFUND, totalRefundAmount);
        }
    }

    private BigDecimal checkAudit(RefundApplyAuditParam param, RefundApply refundApply, Order order) {
        // 排除退款成功的支付金额
        List<RefundApply> refundApplies = this.lambdaQuery()
                .eq(RefundApply::getOrderId, refundApply.getOrderId())
                .in(RefundApply::getRefundStatus, 1, 2).list();
        BigDecimal completeOnlineBalance = refundApplies.stream().map(RefundApply::getOnlineBalance).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 售后协商可退金额
        Aftersales aftersales = aftersalesService.getById(refundApply.getAftersaleId());

        BigDecimal refundAmount;

        List<OrderItem> orderItems = orderItemMapper.selectList(
                Wrappers.lambdaQuery(OrderItem.class).eq(OrderItem::getOrderId, order.getOrderId())
        );

        List<Aftersales> orderCompleteAftersales = aftersalesService.lambdaQuery().eq(Aftersales::getOrderId, refundApply.getOrderId()).eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode()).list();
        List<Integer> aftersalesIds = orderCompleteAftersales.stream().map(Aftersales::getAftersaleId).toList();
        List<AftersalesItem> orderAllAftersalesItems = aftersalesItemService.lambdaQuery().in(AftersalesItem::getAftersaleId, aftersalesIds).list();

        List<RefundApply> allRefundApplies = this.lambdaQuery().eq(RefundApply::getOrderId, refundApply.getOrderId()).list();
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
            long isReviewCount = allRefundApplies.stream().filter(item -> item.getRefundStatus() == 0).count();
            if (isReviewCount == 1) {
                refundAmount = allAftersalesFlag ? aftersales.getRefundAmount().add(order.getShippingFee()) : aftersales.getRefundAmount();
            } else {
                refundAmount = aftersales.getRefundAmount();
            }

        } else if (ShippingStatusEnum.SENT.getCode() == order.getShippingStatus()) {
            refundAmount = aftersales.getRefundAmount();
        } else {
            refundAmount = aftersales.getRefundAmount();
        }

        // 如果订单状态为已完成，发货状态为已收货
        List<AftersalesItem> aftersalesItems = aftersalesItemService.lambdaQuery().eq(AftersalesItem::getAftersaleId, refundApply.getAftersaleId()).list();
        if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus() && ShippingStatusEnum.SHIPPED.getCode() == order.getShippingStatus()) {
            if (order.getShippingTime() != null && order.getShippingTime() > 0) {
                refundAmount = aftersales.getRefundAmount();
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

                refundAmount = allAftersalesFlag ? aftersales.getRefundAmount().add(order.getShippingFee()) : aftersales.getRefundAmount();
            }
        }

        BigDecimal effectiveOnlineBalance = order.getOnlinePaidAmount().subtract(completeOnlineBalance);
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            effectiveOnlineBalance = effectiveOnlineBalance.compareTo(refundAmount) > 0 ? refundAmount : effectiveOnlineBalance;
        }
        // 真实的线上剩余可退金额
        Assert.isTrue(new BigDecimal(param.getOnlineBalance()).compareTo(effectiveOnlineBalance) <= 0, () -> new GlobalException("填写的线上金额不能超过可退的在线支付金额"));
        Assert.isTrue(new BigDecimal(param.getRefundBalance()).compareTo(refundAmount) <= 0, () -> new GlobalException("填写的余额不能超过可退款总金额"));
        Assert.isTrue(new BigDecimal(param.getOfflineBalance()).compareTo(refundAmount) <= 0, () -> new GlobalException("填写的线下金额不能超过可退款总金额"));

        BigDecimal totalRefundAmount = new BigDecimal(param.getOnlineBalance()).add(new BigDecimal(param.getOfflineBalance())).add(new BigDecimal(param.getRefundBalance()));
        if (param.getRefundStatus() == 1) {
            Assert.isTrue(totalRefundAmount.compareTo(BigDecimal.ZERO) != 0, () -> new GlobalException("退款总金额不能为0"));
        }

        BigDecimal finalRefundAmount = refundAmount;
        Assert.isTrue(totalRefundAmount.compareTo(refundAmount) == 0, () -> new GlobalException("填写的退款总金额要等于售后可退款金额:" + finalRefundAmount));

        return totalRefundAmount;
    }

    private void createRefundFlow(RefundApplyAuditParam param, RefundApply refundApply, Order order) {
        RefundLog refundLog = RefundLog.builder()
                .orderId(refundApply.getOrderId())
                .refundApplyId(refundApply.getRefundId())
                .userId(refundApply.getUserId())
                .build();

        // 线上退款
        BigDecimal onlineBalance = new BigDecimal(param.getOnlineBalance());
        if (onlineBalance.compareTo(BigDecimal.ZERO) > 0) {
            refundLog.setLogId(null);
            refundLog.setRefundType(RefundLogTypeEnum.OFFLINE_REFUND.getCode());
            refundLog.setRefundAmount(onlineBalance);
            refundLog.setAddTime(StringUtils.getCurrentTime());
            refundLogService.save(refundLog);

            this.refundFlow(order, onlineBalance, param.getRefundNote(), refundApply);
            refundApply.setIsOnline(Constants.YES);
            refundApply.setOnlineBalance(onlineBalance);
        }

        // 余额退款
        BigDecimal refundBalance = new BigDecimal(param.getRefundBalance());
        if (refundBalance.compareTo(BigDecimal.ZERO) > 0) {
            refundLog.setLogId(null);
            refundLog.setRefundType(RefundLogTypeEnum.BALANCE_REFUND.getCode());
            refundLog.setRefundAmount(refundBalance);
            refundLog.setAddTime(StringUtils.getCurrentTime());
            refundLogService.save(refundLog);

            userBalanceService.incBalance(refundBalance, refundApply.getUserId(), "加余额");
            refundApply.setIsReceive(2);
            refundApply.setRefundBalance(refundBalance);
        }

        // 线下退款
        BigDecimal offlineBalance = new BigDecimal(param.getOfflineBalance());
        if (offlineBalance.compareTo(BigDecimal.ZERO) > 0) {
            refundLog.setLogId(null);
            refundLog.setRefundType(RefundLogTypeEnum.OFFLINE_REFUND.getCode());
            refundLog.setRefundAmount(offlineBalance);
            refundLog.setAddTime(StringUtils.getCurrentTime());
            refundLogService.save(refundLog);

            refundApply.setIsOffline(1);
            refundApply.setOfflineBalance(offlineBalance);
            refundApply.setPaymentVoucher(param.getPaymentVoucher());
        }

        UserPointsLog userPointsLog = userPointsLogService.lambdaQuery()
                .eq(UserPointsLog::getRelationId, order.getOrderId())
                .last("limit 1")
                .one();

        if (userPointsLog != null && userPointsLog.getPoints() > 0) {
            List<RefundApply> allRefundApplies = this.lambdaQuery().eq(RefundApply::getOrderId, refundApply.getOrderId()).list();
            long isReviewCount = allRefundApplies.stream().filter(item -> item.getRefundStatus() == 0).count();
            if (isReviewCount == 1) {
                userPointsLogService.subPoints(userPointsLog.getPoints(), refundApply.getUserId(), "退货扣积分", order.getOrderId());
            }
        }

        if (refundApply.getIsOffline() != 1 && refundApply.getIsOnline() != 1 && refundApply.getIsReceive() != 1) {
            refundApply.setRefundStatus(2);
        }
    }

    @Transactional
    @Override
    public void offlineAudit(OfflineAuditParam param) {
        RefundApply refundApply = this.getById(param.getRefundId());
        Assert.notNull(refundApply, () -> new GlobalException("该申请不存在"));
        Assert.isTrue(refundApply.getIsOffline() == 1, () -> new GlobalException("该状态下不能确认线下已退款"));

        refundApply.setIsOffline(2);

        if (refundApply.getIsOffline() != 1 && refundApply.getIsOnline() != 1 && refundApply.getIsReceive() != 1) {
            refundApply.setRefundStatus(2);

            // 扣减成长值
            userRankService.reduceGrowth(param.getRefundId());
        }

        this.updateById(refundApply);

        BigDecimal totalRefundAmount = refundApply.getOfflineBalance().add(refundApply.getOnlineBalance()).add(refundApply.getRefundBalance());

        messageService.sendUserMessage(refundApply.getUserId(), refundApply.getOrderId(), MessageTypeEnum.ORDER_REFUND, totalRefundAmount);
    }

    @Override
    public void create(RefundApplySaveParam param) {
        RefundApply refundApply = new RefundApply();
        BeanUtils.copyProperties(param, refundApply);
        refundApply.setAddTime(StringUtils.getCurrentTime());
        this.save(refundApply);
    }

    @Override
    public void update(RefundApplyEditParam param) {
        RefundApply refundApply = new RefundApply();
        BeanUtils.copyProperties(param, refundApply);
        this.updateById(refundApply);
    }

    private void refundFlow(Order order, BigDecimal refundAmount, String paylogDesc, RefundApply refundApply) {
        Paylog paylog = paylogService.lambdaQuery()
                .eq(Paylog::getOrderSn, order.getOrderSn())
                .eq(Paylog::getPayStatus, 1)
                .isNotNull(Paylog::getTransactionId)
                .one();
        Assert.notNull(paylog, () -> new GlobalException("订单支付记录不存在"));

        String refundSn = createRefundSn();

        Integer adminId = SecurityUtils.getCurrentAdminId();
        PaylogRefund paylogRefund = PaylogRefund.builder()
                .paylogId(paylog.getPaylogId())
                .refundSn(refundSn)
                .paylogDesc(paylogDesc)
                .orderId(order.getOrderId())
                .refundAmount(refundAmount)
                .addTime(System.currentTimeMillis() / 1000)
                .adminId(adminId)
                .payCode(paylog.getPayCode())
                .build();
        paylogRefundService.save(paylogRefund);
        // 防止支付宝支付时状态被修改
        refundApply.setPaylogRefundId(paylogRefund.getPaylogRefundId());
        refundApply.setRefundStatus(null);
        // refund_apply 绑定
        RefundApply update = new RefundApply();
        update.setRefundId(refundApply.getRefundId());
        update.setPaylogRefundId(paylogRefund.getPaylogRefundId());
        updateById(update);
        // 创建退款接口日志
        PayNotifyVO payNotifyVO;
        try {
            payNotifyVO = switch (paylog.getPayCode()) {
                case "wechat" ->
                        wechatPayService.payRefund(paylog.getPaySn(), refundSn, refundAmount, paylog.getOrderAmount(), paylog.getPayCode());
                case "alipay" ->
                        aliPayService.payRefund(paylog.getPaySn(), refundSn, refundAmount, paylog.getOrderAmount(), paylog.getPayCode());
                case "paypal" ->
                        paypalService.payRefund(paylog.getPaySn(), refundSn, refundAmount, paylog.getOrderAmount(), paylog.getPayCode());
                default -> throw new GlobalException("该支付方式不存在");
            };
        } catch (Exception e) {
            throw new GlobalException(e.getMessage());
        }

        if (payNotifyVO != null && "SUCCESS".equals(payNotifyVO.getCode())) {
            // 退款成功
            paymentService.refundSuccess(refundSn);
        }

    }

    private String createRefundSn() {
        String refundSn;
        int payLogRefundId;

        do {
            refundSn = RandomStringUtils.randomAlphanumeric(16);
            payLogRefundId = this.getPayLogRefundId(refundSn);
        } while (payLogRefundId > 0);

        return refundSn;
    }

    public int getPayLogRefundId(String refundSn) {
        if (StrUtil.isBlank(refundSn)) {
            return 0;
        }

        PaylogRefund paylogRefund = paylogRefundService.lambdaQuery()
                .eq(PaylogRefund::getRefundSn, refundSn)
                .one();

        return paylogRefund != null ? paylogRefund.getPaylogRefundId() : 0;
    }
}
