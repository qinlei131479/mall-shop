// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

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

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ijpay.core.IJPayHttpResponse;
import com.ijpay.paypal.PayPalApi;
import com.ijpay.paypal.PayPalApiConfig;
import com.ijpay.paypal.PayPalApiConfigKit;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.bean.vo.order.pay.PayNotifyVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.service.finance.PaylogService;
import com.tigshop.service.pay.PayService;
import com.tigshop.service.setting.ConfigService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * paypal 支付服务实现类
 *
 * @author Tigshop团队
 * @create 2025年02月21日 11:21
 */
@Service("PaypalService")
@Slf4j
public class PaypalServiceImpl extends PayBaseServiceImpl implements PayService {
    private final ConfigService configService;
    private final PaylogService paylogService;
    private String currency = "CNY";
    private final RedisCache redisCache;
    private final HttpServletResponse httpServletResponse;

    public PaypalServiceImpl(ConfigService configService, RedisCache redisCache, HttpServletResponse httpServletResponse, PaylogService paylogService) {
        this.configService = configService;
        this.redisCache = redisCache;
        this.httpServletResponse = httpServletResponse;
        initConfig(configService);
        this.paylogService = paylogService;
    }

    private PayPalApiConfig initConfig(ConfigService configService) {
        // 构造 IJPay 的 PayPal 配置
        PayPalApiConfig config = new PayPalApiConfig();
        config.setClientId(configService.getConfigByCode(SettingsEnum.PAYPAL_CLIENT_ID.getBizCode()).getBizVal());
        config.setSecret(configService.getConfigByCode(SettingsEnum.PAYPAL_SECRET.getBizCode()).getBizVal());
        boolean showSandBox = StrUtil.startWith(config.getClientId(), "Aawa51bPPZzIC4yLtxc")
                && StrUtil.startWith(config.getSecret(), "EAUde2_i2jUD4M_1c-B7");
        config.setSandBox(showSandBox);
        config.setDomain(configService.getConfigByCode(SettingsEnum.PC_DOMAIN.getBizCode()).getBizVal());
        PayPalApiConfigKit.setThreadLocalApiConfig(config);
        if (ObjectUtil.isNotEmpty(configService.getConfigByCode(SettingsEnum.PAYPAL_CURRENCY.getBizCode()).getBizVal())) {
            currency = configService.getConfigByCode(SettingsEnum.PAYPAL_CURRENCY.getBizCode()).getBizVal();
        }
        return config;
    }

    @Override
    public PayCreateVO.PayInfo pay(OrderVO orderVO, String openid, String clientType) {
        try {
            PayPalApiConfig config = initConfig(configService);
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("intent", "CAPTURE");

            ArrayList<Map<String, Object>> list = new ArrayList<>();

            Map<String, Object> amount = new HashMap<>();
            amount.put("currency_code", currency);
            amount.put("value", orderVO.getUnpaidAmount());

            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("amount", amount);
            // 👇 添加这一行
            itemMap.put("invoice_id", orderVO.getPaySn());

            list.add(itemMap);

            dataMap.put("purchase_units", list);

            Map<String, String> applicationContext = new HashMap<>();
//            applicationContext.put("cancel_url", config.getDomain() + "/member/order/info?id=" + orderVO.getOrderId());
            applicationContext.put("return_url", config.getDomain() + "/member/order/info?id=" + orderVO.getOrderId());
            dataMap.put("application_context", applicationContext);

            String data = JSONUtil.toJsonStr(dataMap);
            log.info(data);
            IJPayHttpResponse resData = PayPalApi.createOrder(config, data);
            log.info(resData.toString());
            if (resData.getStatus() == 201) {
                String resultStr = resData.getBody();

                JSONObject jsonObject = JSONUtil.parseObj(resultStr);

                String id = jsonObject.getStr("id");
                redisCache.setCacheObject("paypal_order_" + id, orderVO.getPaySn(), 60, TimeUnit.MINUTES);

                JSONArray links = jsonObject.getJSONArray("links");
                for (int i = 0; i < links.size(); i++) {
                    JSONObject item = links.getJSONObject(i);
                    String rel = item.getStr("rel");
                    String href = item.getStr("href");
                    if ("approve".equalsIgnoreCase(rel)) {
                        PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
                        payInfo.setUrl(href);
                        return payInfo;
                    }
                }
            }
        } catch (Exception e) {
            log.error("paypal 创建支付出错", e);
            throw new GlobalException("请检查paypal设置项");
        }
        return null;
    }

    @Override
    public PayNotifyVO payRefund(String outTradeNo, String refundNo, BigDecimal refundAmount, BigDecimal totalAmount, String clientType) {
        PayPalApiConfig config = initConfig(configService);
        try {
            // 1. 获取 captureId（通常是支付成功后返回的）
            String captureId = paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, outTradeNo)).getTransactionId();

            // 2. 构造退款参数
            JSONObject refundParams = new JSONObject();
            JSONObject amount = new JSONObject();
            amount.set("value", refundAmount.toPlainString());
            amount.set("currency_code", currency);
            refundParams.set("amount", amount);
            refundParams.set("invoice_id", refundNo);

            // 3. 发起退款请求
            IJPayHttpResponse response = PayPalApi.refund(config, captureId, refundParams.toString());

            if (response.getStatus() == 201) {
                JSONObject result = JSONUtil.parseObj(response.getBody());
                String refundId = result.getStr("id");
                log.info("退款成功：{}", refundId);
                return new PayNotifyVO("success", "退款成功");
            } else {
                log.warn("退款失败，状态码：{}，返回：{}", response.getStatus(), response.getBody());
                return new PayNotifyVO("fail", "退款失败：" + response.getBody());
            }
        } catch (Exception e) {
            log.error("退款异常", e);
            return new PayNotifyVO("fail", "退款异常：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String payNotify(Map<String, Object> map) {
        log.info("paypal支付回调：" + JSONUtil.toJsonStr(map));
        initConfig(configService);
        String eventType = String.valueOf(map.get("event_type"));
        if (ObjectUtil.equals("CHECKOUT.ORDER.APPROVED", eventType)) {
            // 获取 paypal 订单号
            String id = String.valueOf(((HashMap<String, Object>) map.get("resource")).get("id"));
            // 获取订单号(tigShop order_id)
            Object paySn = redisCache.getCacheObject("paypal_order_" + id);
            if (!checkOrderPayStatus((String) paySn)) {
                log.info("paypal发起扣款");
                captureOrder(id);
                return "SUCCESS";
            } else {
                log.info("paypal无需扣款，订单已支付");
            }
        }
        return "FAIL";
    }

    @Override
    @Transactional
    public String refundNotify(Map<String, Object> map) {
        log.info("paypal退款回调：" + JSONUtil.toJsonStr(map));
        initConfig(configService);
        String eventType = String.valueOf(map.get("event_type"));

        if (ObjectUtil.equals("PAYMENT.CAPTURE.REFUNDED", eventType)) {
            Map<String, Object> resource = (Map<String, Object>) map.get("resource");
            String refundSn = String.valueOf(resource.get("invoice_id"));
            if (refundSn != null) {
                refundSuccess(refundSn);
                return "SUCCESS";
            } else {
                log.error("找不到对应的退款订单，refundSn: {}", refundSn);
            }
        }
        return "FAIL";
    }

    @Transactional
    public void captureOrder(String id) {
        try {
            // 1. 初始化PayPal配置
            PayPalApiConfig config = initConfig(configService);

            // 2. 使用token调用 PayPal的capture接口
            IJPayHttpResponse response = PayPalApi.captureOrder(config, id, "");
            log.info("Capture Response: {}", response);

            if (response.getStatus() == 201 || response.getStatus() == 200) {
                String body = response.getBody();
                JSONObject json = JSONUtil.parseObj(body);

                String captureId = json.getJSONArray("purchase_units").getJSONObject(0).getJSONObject("payments").getJSONArray("captures").getJSONObject(0).getStr("id");

                String status = json.getJSONArray("purchase_units").getJSONObject(0).getJSONObject("payments").getJSONArray("captures").getJSONObject(0).getStr("status");

                // 3. 判断是否支付成功
                if ("COMPLETED".equalsIgnoreCase(status)) {
                    // 4. 更新你的本地订单状态，比如根据token关联订单
                    Object paySn = redisCache.getCacheObject("paypal_order_" + id);
                    paySuccess(paySn.toString(), captureId);
                } else {
                    log.error("支付未完成，状态: {}", status);
                }
            } else {
                log.error("Capture失败，状态码: {}", response.getStatus());
            }
            httpServletResponse.sendRedirect(config.getDomain() + "/member/order/info/?id=" + id);
        } catch (Exception e) {
            log.error("PayPal支付回调异常", e);
        }
    }
}