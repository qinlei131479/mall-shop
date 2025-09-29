package com.tigshop.service.pay;

import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.bean.vo.order.pay.PayNotifyVO;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 订单支付
 *
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
public interface PayService extends PayBaseService {

    /**
     * 获取支付页面信息
     * @param orderVO 订单信息
     * @param openid openid
     * @param clientType 支付客户端类型
     * @return PayInfo
     */
    PayCreateVO.PayInfo pay(OrderVO orderVO, String openid, String clientType);

    /**
     * 支付退款
     * @param outTradeNo 商户订单号
     * @param refundNo 退款单号
     * @param refundAmount 退款金额
     * @param totalAmount 总金额
     * @param clientType 支付客户端类型
     * @return PayNotifyVO
     */
    PayNotifyVO payRefund(String outTradeNo, String refundNo, BigDecimal refundAmount, BigDecimal totalAmount, String clientType);

    /**
     * 支付回调处理
     * @param map 回调数据
     * @return String
     */
    String payNotify(Map<String, Object> map);

    /**
     * 退款回调处理
     * @param map 回调数据
     * @return String
     */
    String refundNotify(Map<String, Object> map);

}