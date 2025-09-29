// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.pay.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.enums.msg.MessageTypeEnum;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.user.ChangTypeEnum;
import com.tigshop.bean.model.finance.*;
import com.tigshop.bean.model.order.Order;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.ServletUtils;
import com.tigshop.mapper.finance.RefundApplyMapper;
import com.tigshop.mapper.finance.UserRechargeOrderMapper;
import com.tigshop.service.finance.PaylogRefundService;
import com.tigshop.service.finance.PaylogService;
import com.tigshop.service.finance.UserBalanceLogService;
import com.tigshop.service.msgTemplate.MessageService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.pay.PayBaseService;
import com.tigshop.service.product.ECardService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.user.UserRankService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 订单支付基础实现类
 *
 * @author Tigshop团队
 * @create 2025年02月18日 17:26
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class PayBaseServiceImpl implements PayBaseService {

    @Resource
    private ConfigService configService;
    @Resource
    private PaylogService paylogService;
    @Resource
    private OrderService orderService;
    @Resource
    private ECardService eCardService;
    @Resource
    private UserRankService userRankService;
    @Resource
    private UserRechargeOrderMapper userRechargeOrderMapper;
    @Resource
    private UserBalanceLogService userBalanceLogService;
    @Resource
    private PaylogRefundService payLogRefundService;
    @Resource
    private RefundApplyMapper refundApplyMapper;
    @Resource
    private MessageService messageService;

    /**
     * 获取回调地址
     *
     * @param payType 支付渠道
     * @param path    回调地址的路径
     * @return String
     */
    private String getNotifyUrl(String payType, String path) {
        String url = configService.getConfigByCode(SettingsEnum.PC_DOMAIN.getBizCode()).getBizVal();
        // 获取pc_domain并生成基础URL
        if (url.isEmpty()) {
            url = ServletUtils.getRequestSchemeAndHost() + path;
        } else {
            url = url + path;
        }

        // 如果支付渠道不为空，附加pay_code参数
        if (payType != null) {
            url = StrUtil.format("{}/{}", url, payType);
            System.out.println(url);
        }

        return url;
    }

    /**
     * 获取退款回调地址
     *
     * @param payType 支付渠道
     * @return String
     */
    @Override
    public String getRefundNotifyUrl(String payType) {
        return getNotifyUrl(payType, "/api/order/pay/refundNotify");
    }

    @Override
    public String getPayNotifyUrl(String payType) {
        return getNotifyUrl(payType, "/api/order/pay/notify");
    }

    @Override
    public String getReturnUrl(Integer orderId) {
        String url = configService.getConfigByCode(SettingsEnum.PC_DOMAIN.getBizCode()).getBizVal();
        // 获取pc_domain并生成基础URL
        if (url.isEmpty()) {
            url = ServletUtils.getRequestSchemeAndHost();
        }

        // 如果支付渠道不为空，附加pay_code参数
        if (orderId != null && orderId > 0) {
            url += "/member/order/info?id=" + orderId;
        }

        return url;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkOrderPayStatus(String paySn) {
        Paylog one = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, paySn));
        Order order = orderService.getById(one.getOrderId());
        if (order != null && Objects.equals(order.getOrderStatus(), OrderStatusEnum.PENDING.getCode())) {
            // 未支付
            return false;
        } else {
            // 不能支付状态
            return true;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(String paySn, String tranceNo) {
        Paylog one = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, paySn));
        Order order = orderService.getById(one.getOrderId());
        if (order != null && Objects.equals(order.getOrderStatus(), OrderStatusEnum.PENDING.getCode())) {
            // 未支付 处理逻辑
            one.setPayStatus(1);
            one.setTransactionId(tranceNo);
            paylogService.updateById(one);
            if (one.getOrderType() == 0) {
                // 订单
                order.setOutTradeNo(one.getPaySn());
                orderService.updateById(order);
                order.setOnlinePaidAmount(one.getPayAmount());
                orderService.updateOrderMoney(order);
                // 卡券分配
                eCardService.getCardByOrderId(order.getOrderId());
                // 订单交易成功获取成长值
                try {
                    userRankService.getRankGrowth(order.getUserId());
                } catch (Exception e) {
                    log.error("订单交易成功获取成长值", e);
                }
            }
            if (one.getOrderType() == 1) {
                // 充值
                setRechargePaid(one.getOrderId());
            }
        }

        messageService.sendUserMessage(order.getUserId(), order.getOrderId(), MessageTypeEnum.ORDER_PAY);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refundSuccess(String refundSn) {
        // 获取 PayLogRefund 对象
        PaylogRefund payLogRefund = payLogRefundService.getOne(new LambdaQueryWrapper<PaylogRefund>().eq(PaylogRefund::getRefundSn, refundSn));
        if (payLogRefund == null || payLogRefund.getStatus() == 1) {
            return;
        }
        if (payLogRefund.getOrderId() == null) {
            return;
        }
        try {
            // 修改通知状态
            PaylogRefund update = new PaylogRefund();
            update.setPaylogId(payLogRefund.getPaylogId());
            update.setStatus(1);
            update.setNotifyTime(System.currentTimeMillis()/1000);
            payLogRefundService.updateById(update);
            // 通知退款成功
            int successFlag = onlineRefundSuccess(payLogRefund.getPaylogRefundId());
            if (successFlag > 0){
                System.out.println("通知发送成功");
            }
        } catch (Exception e) {
            throw new GlobalException(e.getMessage());
        }

        Order order = orderService.getById(payLogRefund.getOrderId());
        messageService.sendUserMessage(order.getUserId(), order.getOrderId(), MessageTypeEnum.ORDER_REFUND);
    }

    /**
     * 设置充值支付成功
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    protected void setRechargePaid(Integer orderId) {
        // 获取订单详情
        UserRechargeOrder order = userRechargeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new GlobalException("充值订单不存在");
        }

        if (order.getStatus() != 0) {
            throw new GlobalException("充值订单状态错误");
        }

        // 更新订单状态为已支付
        UserRechargeOrder updateOrder = new UserRechargeOrder();
        updateOrder.setOrderId(orderId);
        updateOrder.setStatus(1);
        updateOrder.setPaidTime(System.currentTimeMillis() / 1000);
        userRechargeOrderMapper.updateById(updateOrder);

        // 修改用户余额
        UserBalanceLog log = new UserBalanceLog();
        log.setBalance(order.getAmount().add(order.getDiscountMoney()));
        log.setChangeDesc(order.getPostscript());
        log.setChangeType(ChangTypeEnum.INC_POINTS.getValue());

        userBalanceLogService.accountChange(order.getUserId(), log);
    }

    @Transactional
    protected int onlineRefundSuccess(Integer paylogRefundId) {
        RefundApply refundApply = refundApplyMapper.selectOne(new LambdaQueryWrapper<RefundApply>().eq(RefundApply::getPaylogRefundId, paylogRefundId));
        if (refundApply == null) {
            return 0;
        }
        refundApply.setIsOnline(2);
        if (refundApply.getIsOffline() != 1 && refundApply.getIsReceive() != 1) {
            refundApply.setRefundStatus(2);
        }

        return refundApplyMapper.updateById(refundApply);
    }
}