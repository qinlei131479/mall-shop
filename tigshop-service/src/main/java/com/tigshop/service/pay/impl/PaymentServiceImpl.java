package com.tigshop.service.pay.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.PaylogRefund;
import com.tigshop.bean.model.finance.RefundApply;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.mapper.finance.RefundApplyMapper;
import com.tigshop.service.finance.PaylogRefundService;
import com.tigshop.service.pay.PaymentService;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单支付
 *
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private ConfigService configService;

    private final PaylogRefundService paylogRefundService;

    private final RefundApplyMapper refundApplyMapper;

    @Override
    public List<String> getAvailablePayment(String type) {

        String clientType = HeaderUtils.getClientType();

        List<String> paymentList = new ArrayList<>();

        String useWechatVal = configService.getConfigByCode(SettingsEnum.USE_WECHAT.getBizCode()).getBizVal();
        if (StrUtil.isNotBlank(useWechatVal) && Integer.parseInt(useWechatVal) == 1) {
            paymentList.add("wechat");
        }

        String useAlipayVal = configService.getConfigByCode(SettingsEnum.USE_ALIPAY.getBizCode()).getBizVal();
        if (StrUtil.isNotBlank(useAlipayVal) && Integer.parseInt(useAlipayVal) == 1) {
            if (!"miniProgram".equals(clientType) && !"wechat".equals(clientType)) {
                paymentList.add("alipay");
            }
        }

        String usePaypalVal = configService.getConfigByCode(SettingsEnum.USE_PAYPAL.getBizCode()).getBizVal();
        if (StrUtil.isNotBlank(usePaypalVal) && Integer.parseInt(usePaypalVal) == 1) {
            paymentList.add("paypal");
        }

        String useYabanpayVal = configService.getConfigByCode(SettingsEnum.USE_YABANPAY.getBizCode()).getBizVal();
        if (StrUtil.isNotBlank(useYabanpayVal) && Integer.parseInt(useYabanpayVal) == 1) {
            //检测是否开启微信/支付宝支付
            String useYabanpayWechatVal = configService.getConfigByCode(SettingsEnum.USE_YABANPAY_WECHAT.getBizCode()).getBizVal();
            if (StrUtil.isNotBlank(useYabanpayWechatVal) && Integer.parseInt(useYabanpayWechatVal) == 1) {
                paymentList.add("yabanpay_wechat");
            }
            String useYabanpayAlipayVal = configService.getConfigByCode(SettingsEnum.USE_YABANPAY_ALIPAY.getBizCode()).getBizVal();
            if (StrUtil.isNotBlank(useYabanpayAlipayVal) && Integer.parseInt(useYabanpayAlipayVal) == 1 && !"miniProgram".equals(clientType) && !"wechat".equals(clientType)) {
                paymentList.add("yabanpay_alipay");
            }
        }

        String useYunpayVal = configService.getConfigByCode(SettingsEnum.USE_YUNPAY.getBizCode()).getBizVal();
        if (StrUtil.isNotBlank(useYunpayVal) && Integer.parseInt(useYunpayVal) == 1) {
            paymentList.add("yunpay_wechat");
            if (!"miniProgram".equals(clientType) && !"wechat".equals(clientType)) {
                paymentList.add("yunpay_alipay");
            }
//            paymentList.add("yunpay_yunshanfu");
        }

        String useOfflineVal = configService.getConfigByCode(SettingsEnum.USE_OFFLINE.getBizCode()).getBizVal();
        if (StrUtil.isNotBlank(useOfflineVal) && Integer.parseInt(useOfflineVal) == 1 && "order".equals(type)) {
            paymentList.add("offline");
        }
        return paymentList;
    }

    @Transactional
    @Override
    public void refundSuccess(String refundSn) {
        PaylogRefund paylogRefund = paylogRefundService.lambdaQuery()
                .eq(PaylogRefund::getRefundSn, refundSn)
                .one();
        if (paylogRefund == null) {
            return;
        }

        if (paylogRefund.getStatus() == 1) {
            return;
        }

        if (paylogRefund.getOrderId() == null) {
            return;
        }

        try {
            paylogRefund.setStatus(1);
            paylogRefund.setNotifyTime(System.currentTimeMillis()/1000);
            paylogRefundService.updateById(paylogRefund);

            RefundApply refundApply = refundApplyMapper.selectOne(
                    Wrappers.lambdaQuery(RefundApply.class)
                            .eq(RefundApply::getPaylogRefundId, paylogRefund.getPaylogRefundId())
            );
            if (refundApply == null) {
                return;
            }

            refundApply.setIsOnline(2);

            if (refundApply.getIsOffline() != 1 && refundApply.getIsOnline() != 1 && refundApply.getIsReceive() != 1) {
                refundApply.setRefundStatus(2);
            }
            refundApplyMapper.updateById(refundApply);
        } catch (Exception e) {
            throw new GlobalException(e.getMessage());
        }
    }
}