package com.tigshop.service.order;

import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.bean.vo.order.pay.PayIndexVO;

import java.util.Map;

/**
 * 订单支付（前端业务调用）
 *
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
public interface OrderPayService {

    /**
     * 获取支付订单信息
     *
     * @param id 订单ID
     * @return PayIndexVO
     */
    PayIndexVO getPayOrderInfo(Integer id);

    /**
     * 获取支付订单状态
     *
     * @param id      订单ID
     * @param payLogId 支付日志ID
     * @return Map<String, Integer>
     */
    Integer getCheckStatus(Integer id, Integer payLogId);

    /**
     * 创建支付订单
     *
     * @param id      订单ID
     * @param payType    支付类型
     * @param code    支付码
     * @return PayCreateVO
     */
    PayCreateVO create(Integer id, String payType, String code);

    /**
     * 支付退款回调
     *
     * @param resMap 支付退款回调参数
     * @return Object
     */
    String payRefundNotify(Map<String, Object> resMap, String payType);

    /**
     * 支付回调
     *
     * @param resMap 支付回调参数
     * @return String
     */
    String payNotify(Map<String, Object> resMap, String payType);
}