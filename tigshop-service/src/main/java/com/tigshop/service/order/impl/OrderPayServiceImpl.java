package com.tigshop.service.order.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.feign.wechat.Code2SessionResult;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.user.UserAuthorize;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.bean.vo.order.pay.PayIndexVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.service.finance.PaylogService;
import com.tigshop.service.order.OrderPayService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.pay.PayService;
import com.tigshop.service.pay.PaymentService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.user.UserAuthorizeService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单支付
 *
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class OrderPayServiceImpl implements OrderPayService {

    @Resource
    private OrderService orderService;

    @Resource
    private PaymentService paymentService;

    @Resource
    private ConfigService configService;

    @Resource
    private PaylogService paylogService;

    @Resource
    TranslatePackageImpl translatePackage;

    @Resource
    @Qualifier("WechatPayService")
    private PayService wechatPayService;

    @Resource
    @Qualifier("AliPayService")
    private PayService aliPayService;

    @Resource
    @Qualifier("PaypalService")
    private PayService paypalService;

    private final WechatApiClient wechatApiClient;
    @Autowired
    private UserAuthorizeService userAuthorizeService;

    @Override
    public PayIndexVO getPayOrderInfo(Integer id) {
        PayIndexVO vo = new PayIndexVO();
        OrderVO detail = orderService.detail(id);
        vo.setOrder(detail);
        if (!detail.getAvailableActions().getToPay()) {
            throw new GlobalException(translatePackage.translate("订单已支付"));
        }

        List<String> paymentList = paymentService.getAvailablePayment("order");

        if (detail.getPayTypeId() == 1) {
            paymentList.remove("offline");
        } else if (detail.getPayTypeId() == 3) {
            paymentList = paymentList.stream()
                    .filter("offline"::equals)
                    .toList();
        }
        vo.setPaymentList(paymentList);
        Map<String, String> offlinePaymentList = new HashMap<>();

        if (paymentList.contains("offline")) {
            ConfigPO offlinePayBankConfig = configService.getConfigByCode(SettingsEnum.OFFLINE_PAY_BANK.getBizCode());
            offlinePaymentList.put("offlinePayBank", offlinePayBankConfig.getBizVal().replace("{$order_sn}", detail.getOrderSn()));

            ConfigPO offlinePayCompanyConfig = configService.getConfigByCode(SettingsEnum.OFFLINE_PAY_COMPANY.getBizCode());
            offlinePaymentList.put("offlinePayCompany", offlinePayCompanyConfig.getBizVal().replace("{$order_sn}", detail.getOrderSn()));
        }
        vo.setOfflinePaymentList(offlinePaymentList);

        return vo;

    }

    @Override
    public Integer getCheckStatus(Integer id, Integer payLogId) {
        if (id == null && payLogId == null) {
            throw new GlobalException(translatePackage.translate("参数错误"));
        }
        Map<String, Integer> map = new HashMap<>();
        Integer payStatus;
        if (id != null) {
            payStatus = orderService.getById(id).getPayStatus();
        } else {
            payStatus = paylogService.getById(payLogId).getPayStatus();
        }

        log.info("微信支付状态监测：id :{}, payLogId:{}, payStatus:{}", id, payLogId, payStatus);
        return payStatus > 0 ? 1 : 0;
    }

    @Override
    public PayCreateVO create(Integer id, String payType, String code) {
        PayCreateVO payCreateVO = new PayCreateVO();
        if (payType == null) {
            throw new GlobalException(translatePackage.translate("请选择支付方式"));
        }
        String clientType = HeaderUtils.getClientType();
        String openid = null;
        if (code != null) {
            // 获取微信配置
            String appId = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID.getBizCode()).getBizVal();
            String appSecret = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_SECRET.getBizCode()).getBizVal();
            String result = wechatApiClient.code2Session(appId, appSecret, code, "authorization_code");
            Code2SessionResult code2SessionResult = JsonUtil.fromJson(result, Code2SessionResult.class);
            // 错误code -1,40029，45011,40226
            if (code2SessionResult.getErrcode() != null && code2SessionResult.getErrcode() != 0) {
                throw new GlobalException(code2SessionResult.getErrmsg());
            }

            openid = code2SessionResult.getOpenid();
        }
        if (ObjectUtil.equals("wechat", clientType) && "wechat".equals(payType)) {
            // 微信内支付
            UserAuthorize userAuthorize = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>().eq(UserAuthorize::getUserId, SecurityUtils.getCurrentUserId()));
            openid = userAuthorize.getOpenId();
        }
        OrderVO detail = orderService.detail(id);
        if (!detail.getAvailableActions().getToPay()) {
            if (detail.getOrderStatus().equals(OrderStatusEnum.CANCELLED.getCode())) {
                throw new GlobalException(translatePackage.translate("订单已取消"));
            } else {
                throw new GlobalException(translatePackage.translate("订单已支付"));
            }
        }
        Paylog paylog = paylogService.creatPayLog(detail, payType);
        detail.setPaySn(paylog.getPaySn());
        PayCreateVO.PayInfo payResult = new PayCreateVO.PayInfo();
        if ("wechat".equals(payType)) {
            payResult = wechatPayService.pay(detail, openid, clientType);
        }
        if ("alipay".equals(payType)) {
            payResult = aliPayService.pay(detail, openid, clientType);
        }
        if ("paypal".equals(payType)) {
            payResult = paypalService.pay(detail, openid, clientType);
        }
        payCreateVO.setPayInfo(payResult);
        payCreateVO.setOrderId(detail.getOrderId());
        payCreateVO.setOrderSn(detail.getOrderSn());
        payCreateVO.setOrderAmount(detail.getUnpaidAmount());
        return payCreateVO;
    }

    @Override
    public String payRefundNotify(Map<String, Object> resMap, String payType) {
        if ("wechat".equals(payType)) {
            return wechatPayService.refundNotify(resMap);
        }
        if ("ali".equals(payType)) {
            return aliPayService.refundNotify(resMap);
        }
        if ("paypal".equals(payType)) {
            return paypalService.refundNotify(resMap);
        }
        return null;
    }

    @Override
    public String payNotify(Map<String, Object> resMap, String payType) {
        if ("wechat".equals(payType)) {
            return wechatPayService.payNotify(resMap);
        }
        if ("ali".equals(payType)) {
            return aliPayService.payNotify(resMap);
        }
        if ("paypal".equals(payType)) {
            return paypalService.payNotify(resMap);
        }
        return null;
    }
}