package com.tigshop.service.pay.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.binarywang.wxpay.bean.notify.SignatureHeader;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Response;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyV3Result;
import com.github.binarywang.wxpay.bean.request.WxPayOrderQueryV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryV3Result;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.bean.vo.order.pay.PayNotifyVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.IpUtils;
import com.tigshop.config.PayServiceFactory;
import com.tigshop.service.pay.PayService;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import static com.tigshop.bean.enums.order.RefundNotifyType.REFUND_SUCCESS;

/**
 * 订单支付-微信
 *
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
@Service("WechatPayService")
@Slf4j
public class WechatPayServiceImpl extends PayBaseServiceImpl implements PayService {

    @Resource
    private ConfigService configService;
    @Resource
    private PayServiceFactory wxPayServiceFactory;

    /**
     * 获得统一订单请求
     *
     * @param outTradeNo   订单编号
     * @param totalFee     订单金额
     * @param notifyUrl    回调地址
     * @param openid       openid
     * @param wxPayService 支付服务
     * @return WxPayUnifiedOrderV3Request
     */
    public WxPayUnifiedOrderV3Request getUnifiedOrderV3Request(String outTradeNo, BigDecimal totalFee, String notifyUrl, String openid, WxPayService wxPayService) {
        WxPayConfig wxPayConfig = wxPayService.getConfig();
        WxPayUnifiedOrderV3Request unifiedOrderV3Request = new WxPayUnifiedOrderV3Request();
        unifiedOrderV3Request.setAppid(wxPayConfig.getAppId());
        unifiedOrderV3Request.setMchid(wxPayConfig.getMchId());
        unifiedOrderV3Request.setDescription("商品购买");

        unifiedOrderV3Request.setOutTradeNo(outTradeNo);
        unifiedOrderV3Request.setNotifyUrl(notifyUrl);
        WxPayUnifiedOrderV3Request.Amount amount = new WxPayUnifiedOrderV3Request.Amount();
        // 分为单位
        amount.setTotal(totalFee.multiply(new BigDecimal(100)).intValue());
        amount.setCurrency("CNY");
        unifiedOrderV3Request.setAmount(amount);
        String wechatPaySubMchId = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_SUB_MCHID.getBizCode()).getBizVal();
        String wechatMchidType = configService.getConfigByCode(SettingsEnum.WECHAT_MCHID_TYPE.getBizCode()).getBizVal();
        if (Objects.equals(wechatMchidType, "2") && wechatPaySubMchId != null) {
            unifiedOrderV3Request.setAppid(wxPayConfig.getAppId());
        } else {
            unifiedOrderV3Request.setMchid(wxPayConfig.getMchId());
        }
        if (StrUtil.isNotEmpty(openid)) {
            WxPayUnifiedOrderV3Request.Payer payer = new WxPayUnifiedOrderV3Request.Payer();
            payer.setOpenid(openid);
            unifiedOrderV3Request.setPayer(payer);
        }
        return unifiedOrderV3Request;
    }

    @Override
    public PayCreateVO.PayInfo pay(OrderVO orderVO, String openid, String clientType) {
        return switch (clientType) {
            case "wechat" -> jsApiPay(orderVO, openid, clientType);
            case "miniProgram" -> miniPay(orderVO, openid, clientType);
            case "app" -> {
                PayCreateVO.PayInfo payInfo = appPay(orderVO, openid, clientType);
                payInfo.setProvider("wxpay");
                yield payInfo;
            }
            case "pc" -> new PayCreateVO.PayInfo(nativePay(orderVO, openid, clientType), "");
            case "h5" -> new PayCreateVO.PayInfo(htmlPay(orderVO, openid, clientType));
            default -> null;
        };
    }

    @Override
    public PayNotifyVO payRefund(String outTradeNo, String refundNo, BigDecimal refundAmount, BigDecimal totalAmount, String clientType) {
        WxPayService wxPayService = wxPayServiceFactory.getWxPayService(clientType);
        WxPayRefundV3Request wxPayRefundRequest = new WxPayRefundV3Request();
        // 实例化退款金额
        WxPayRefundV3Request.Amount amount = new WxPayRefundV3Request.Amount();
        amount.setCurrency("CNY");
        amount.setTotal(WxPayRefundRequest.yuanToFen(String.valueOf(totalAmount)));
        amount.setRefund(WxPayRefundRequest.yuanToFen(String.valueOf(refundAmount)));
        wxPayRefundRequest.setAmount(amount);
        // 其他退款参数
        wxPayRefundRequest.setOutRefundNo(refundNo);
        wxPayRefundRequest.setOutTradeNo(outTradeNo);
        wxPayRefundRequest.setNotifyUrl(getRefundNotifyUrl(clientType));
        wxPayRefundRequest.setReason("退款");
        try {
            WxPayRefundV3Result wxPayRefundV3Result = wxPayService.refundV3(wxPayRefundRequest);
            if (StrUtil.equals(wxPayRefundV3Result.getStatus(), "SUCCESS")) {
                return new PayNotifyVO(wxPayRefundV3Result.getStatus(), "退款成功");
            }
            return new PayNotifyVO(wxPayRefundV3Result.getStatus(), "退款失败");
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String payNotify(Map<String, Object> map) {
        log.info("微信支付回调：{}", JSONUtil.toJsonStr(map));
        SignatureHeader headers = new SignatureHeader(
                HeaderUtils.getHeaderValue("Wechatpay-Timestamp"),
                HeaderUtils.getHeaderValue("Wechatpay-Nonce"),
                HeaderUtils.getHeaderValue("Wechatpay-Signature"),
                HeaderUtils.getHeaderValue("Wechatpay-Serial")
        );
        WxPayService wxPayService = wxPayServiceFactory.getWxPayService(HeaderUtils.getClientType());
        try {
            final WxPayNotifyV3Result wxPayNotifyV3Result = wxPayService.parseOrderNotifyV3Result(JSONUtil.toJsonStr(map), headers);
            // 支付成功类型
            final String eventType = "TRANSACTION.SUCCESS";
            if (wxPayNotifyV3Result.getRawData().getEventType().equals(eventType)) {
                // 3. 提取订单信息
                WxPayNotifyV3Result.DecryptNotifyResult result = wxPayNotifyV3Result.getResult();
                String outTradeNo = result.getOutTradeNo();
                String transactionId = result.getTransactionId();
                if (!checkOrderPayStatus(outTradeNo)) {
                    //修改订单状态
                    paySuccess(outTradeNo, transactionId);
                }
                return WxPayNotifyV3Response.success("成功");
            }
            return WxPayNotifyV3Response.fail("失败");
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    public WxPayOrderQueryV3Result queryOrderPay(String transactionId, String paySn) {
        WxPayService wxPayService = wxPayServiceFactory.getWxPayService(HeaderUtils.getClientType());

        String wechatPayMchId = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_MCHID.getBizCode()).getBizVal();

        WxPayOrderQueryV3Request wxPayOrderQueryV3Request = new WxPayOrderQueryV3Request();
        wxPayOrderQueryV3Request
                .setTransactionId(transactionId)
                .setOutTradeNo(paySn)
                .setMchid(wechatPayMchId);
        try {
            return wxPayService.queryOrderV3(wxPayOrderQueryV3Request);
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String refundNotify(Map<String, Object> map) {
        log.info("微信退款回调：{}", JSONUtil.toJsonStr(map));
        SignatureHeader headers = new SignatureHeader(
                HeaderUtils.getHeaderValue("Wechatpay-Timestamp"),
                HeaderUtils.getHeaderValue("Wechatpay-Nonce"),
                HeaderUtils.getHeaderValue("Wechatpay-Signature"),
                HeaderUtils.getHeaderValue("Wechatpay-Serial")
        );
        WxPayService wxPayService = wxPayServiceFactory.getWxPayService(HeaderUtils.getClientType());
        try {
            final WxPayRefundNotifyV3Result wxPayRefundNotifyV3Result = wxPayService.parseRefundNotifyV3Result(JSONUtil.toJsonStr(map), headers);
            if (wxPayRefundNotifyV3Result.getRawData().getEventType().equals(REFUND_SUCCESS.getCode())) {
                refundSuccess(wxPayRefundNotifyV3Result.getResult().getOutRefundNo());
                return WxPayNotifyV3Response.success("成功");
            }
            return WxPayNotifyV3Response.fail("失败");
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    /**
     * H5支付
     * @param orderVO 订单信息
     * @param openid openid
     * @param clientType 支付类型
     * @return Object
     */
    private String htmlPay(OrderVO orderVO, String openid, String clientType) {
        try {
            // 根据请求头中的 clientType 获取对应的 WxPayService
            WxPayService wxPayService = wxPayServiceFactory.getWxPayService(clientType);
            WxPayUnifiedOrderV3Request wechat = getUnifiedOrderV3Request(orderVO.getPaySn(), orderVO.getUnpaidAmount(), getPayNotifyUrl("wechat"), openid, wxPayService);
            WxPayUnifiedOrderV3Request.SceneInfo sceneInfo = new WxPayUnifiedOrderV3Request.SceneInfo();
            sceneInfo.setPayerClientIp(IpUtils.getIpAddr());
            WxPayUnifiedOrderV3Request.H5Info h5Info = new WxPayUnifiedOrderV3Request.H5Info();
            h5Info.setType("Wap");
            sceneInfo.setH5Info(h5Info);
            wechat.setSceneInfo(sceneInfo);
            return wxPayService.createOrderV3(TradeTypeEnum.H5, wechat);
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    /**
     * App支付
     * @param orderVO 订单信息
     * @param openid openid
     * @param clientType 支付类型
     * @return Object
     */
    private PayCreateVO.PayInfo appPay(OrderVO orderVO, String openid, String clientType) {
        try {
            // 根据请求头中的 ClientType 获取对应的 WxPayService
            WxPayService wxPayService = wxPayServiceFactory.getWxPayService(clientType);
            WxPayUnifiedOrderV3Result.AppResult appResult = wxPayService.createOrderV3(TradeTypeEnum.APP,
                    getUnifiedOrderV3Request(
                            orderVO.getPaySn(),
                            orderVO.getUnpaidAmount(),
                            getPayNotifyUrl("wechat"),
                            openid,
                            wxPayService
                    ));
            PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
            payInfo.setAppId(appResult.getAppid());
            payInfo.setPartnerId(appResult.getPartnerId());
            payInfo.setPrepayId(appResult.getPrepayId());
            payInfo.setPackageValue(appResult.getPackageValue());
            payInfo.setNonceStr(appResult.getNoncestr());
            payInfo.setTimeStamp(appResult.getTimestamp());
            payInfo.setSign(appResult.getSign());

            return payInfo;
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    /**
     * 小程序支付
     * @param orderVO 订单信息
     * @param openid openid
     * @param clientType 支付类型
     * @return Object
     */
    private PayCreateVO.PayInfo miniPay(OrderVO orderVO, String openid, String clientType) {
        try {
            WxPayService wxPayService = wxPayServiceFactory.getWxPayService(clientType);
            WxPayUnifiedOrderV3Result.JsapiResult result = wxPayService.createOrderV3(TradeTypeEnum.JSAPI, getUnifiedOrderV3Request(orderVO.getPaySn(), orderVO.getUnpaidAmount(), getPayNotifyUrl("wechat"), openid, wxPayService));
            PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
            BeanUtils.copyProperties(result, payInfo);
            return payInfo;
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    /**
     * native支付
     * @param orderVO 订单信息
     * @param openid openid
     * @param clientType 支付类型
     * @return Object
     */
    private String nativePay(OrderVO orderVO, String openid, String clientType) {
        try {
            WxPayService wxPayService = wxPayServiceFactory.getWxPayService(clientType);
            return wxPayService.createOrderV3(TradeTypeEnum.NATIVE, getUnifiedOrderV3Request(orderVO.getPaySn(), orderVO.getUnpaidAmount(), getPayNotifyUrl("wechat"), openid, wxPayService));
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    /**
     * JSAPI支付
     * @param orderVO 订单信息
     * @param openid openid
     * @param clientType 支付类型
     * @return Object
     */
    private PayCreateVO.PayInfo jsApiPay(OrderVO orderVO, String openid, String clientType) {
        try {
            WxPayService wxPayService = wxPayServiceFactory.getWxPayService(clientType);
            WxPayUnifiedOrderV3Result.JsapiResult result = wxPayService.createOrderV3(TradeTypeEnum.JSAPI, getUnifiedOrderV3Request(orderVO.getPaySn(), orderVO.getUnpaidAmount(), getPayNotifyUrl("wechat"), openid, wxPayService));
            PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
            BeanUtils.copyProperties(result, payInfo);
            return payInfo;
        } catch (WxPayException e) {
            throw new GlobalException(e.getMessage());
        }
    }
}