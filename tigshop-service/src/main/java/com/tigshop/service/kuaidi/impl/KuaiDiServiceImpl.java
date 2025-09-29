// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.kuaidi.impl;


import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.order.LogisticsApiLog;
import com.tigshop.bean.model.setting.LogisticsCompany;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.feign.ShippingInfoClient;
import com.tigshop.mapper.order.LogisticsApiLogMapper;
import com.tigshop.mapper.setting.LogisticsCompanyMapper;
import com.tigshop.service.kuaidi.KuaiDiService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Tigshop团队
 * @create 2025/8/5 14:12
 */
@Service
@RequiredArgsConstructor
public class KuaiDiServiceImpl implements KuaiDiService {

    private final LogisticsCompanyMapper logisticsCompanyMapper;
    private final ConfigService configService;
    private final LogisticsApiLogMapper logisticsApiLogMapper;
    private final ShippingInfoClient shippingInfoClient;


    @Override
    public Map<String, Object> getElectronicWaybill(OrderVO order, String remark) {
        // 查询物流公司
        LogisticsCompany logistics = logisticsCompanyMapper.selectById(order.getLogisticsId());
        if (logistics == null) {
            throw new GlobalException("未存在物流公司信息");
        }

        // 收件人信息
        List<String> regionNames = order.getRegionNames();
        List<Integer> regionIds = order.getRegionIds();

        Map<String, Object> receiver = new HashMap<>();
        receiver.put("Name", order.getConsignee());
        receiver.put("Mobile", order.getMobile());
        receiver.put("ProvinceName", regionIds.get(0) > 1 ? regionNames.get(0) : regionNames.get(1));
        receiver.put("CityName", regionIds.get(0) > 1 ? regionNames.get(1) : regionNames.get(2));
        receiver.put("ExpAreaName", regionIds.get(0) > 1 ? regionNames.get(2) : regionNames.get(3));
        receiver.put("Address", order.getAddress());

        // 寄件人信息（读取配置）
        Map<String, Object> sender = new HashMap<>();
        sender.put("ProvinceName", configService.getConfigVal(SettingsEnum.PROVINCE_NAME));
        sender.put("CityName", configService.getConfigVal(SettingsEnum.CITY_NAME));
        sender.put("ExpAreaName", configService.getConfigVal(SettingsEnum.AREA_NAME));
        sender.put("Address", configService.getConfigVal(SettingsEnum.ADDRESS));
        sender.put("Name", configService.getConfigVal(SettingsEnum.SENDER));
        sender.put("Mobile", configService.getConfigVal(SettingsEnum.MOBILE));
        sender.put("PostCode", "000000");

        // 商品总重量
        BigDecimal weight = BigDecimal.ZERO;
        for (OrderItemVO item : order.getItems()) {
            if (item.getProductWeight() != null) {
                weight = weight.add(item.getProductWeight());
            }
        }

        // 时间设置：当前时间 + 1小时、+3小时
        String startDate = DateUtil.format(DateUtil.offsetHour(new Date(), 1), "yyyy-MM-dd HH:mm:ss");
        String endDate = DateUtil.format(DateUtil.offsetHour(new Date(), 3), "yyyy-MM-dd HH:mm:ss");

        // 组装请求数据
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("OrderCode", createOrderCode(order.getOrderSn(), order.getOrderId()));
        requestData.put("ShipperCode", logistics.getLogisticsCode());
        requestData.put("PayType", 3);
        requestData.put("ExpType", 1);
        requestData.put("Receiver", receiver);
        requestData.put("Sender", sender);
        requestData.put("Quantity", 1);
        requestData.put("Weight", weight.compareTo(BigDecimal.ONE) > 0 ? weight : BigDecimal.ONE);
        requestData.put("Volume", 1);
        requestData.put("Remark", remark);
        requestData.put("Commodity", List.of(Map.of("GoodsName", "电商产品")));
        requestData.put("CustomerName", logistics.getCustomerName());
        requestData.put("CustomerPwd", logistics.getCustomerPwd());
        requestData.put("SendSite", logistics.getMonthCode());
        requestData.put("SendStaff", logistics.getSendSite());
        requestData.put("MonthCode", logistics.getSendStaff());
        requestData.put("IsNotice", 0);
        requestData.put("StartDate", startDate);
        requestData.put("EndDate", endDate);
        requestData.put("CurrencyCode", "CNY");
        requestData.put("Dutiable", Map.of("DeclaredValue", order.getProductAmount()));
        requestData.put("IsReturnPrintTemplate", 1);

        String requestJson = JSONUtil.toJsonStr(requestData);

        // 获取系统配置
        String apiKey = configService.getConfigByCode(SettingsEnum.KDNIAO_API_KEY.getBizCode()).getBizVal();
        String kdnBusinessId = configService.getConfigByCode(SettingsEnum.KDNIAO_BUSINESS_ID.getBizCode()).getBizVal();
        String dataSign = Base64.encode(SecureUtil.md5(requestJson + apiKey), "UTF-8");

        // 调用接口
        String response = shippingInfoClient.getElectronicWaybill(
                URLEncoder.encode(requestJson, StandardCharsets.UTF_8),
                kdnBusinessId,
                "1007",
                URLEncoder.encode(dataSign, StandardCharsets.UTF_8),
                "2"
        );

        Map<String, Object> result = JSONUtil.parseObj(response).toBean(new TypeReference<>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });

        if (!Boolean.parseBoolean(String.valueOf(result.get("Success")))) {
            throw new GlobalException(String.valueOf(result.get("Reason")));
        }

        // 记录日志
        Map<String, Object> orderInfo = (Map<String, Object>) result.get("Order");
        LogisticsApiLog logisticsApiLog = new LogisticsApiLog();
        logisticsApiLog.setOrderId(order.getOrderId());
        logisticsApiLog.setOrderCode(String.valueOf(orderInfo.get("OrderCode")));
        logisticsApiLog.setLogisticCode(String.valueOf(orderInfo.get("LogisticCode")));
        logisticsApiLog.setPrintTemplate(String.valueOf(result.getOrDefault("PrintTemplate", "")));
        logisticsApiLogMapper.insert(logisticsApiLog);
        return result;
    }


    /**
     * 获取订单编号
     *
     * @param orderSn 原始订单号
     * @param orderId 订单ID
     * @return 拼接后的电子面单订单编号
     */
    private String createOrderCode(String orderSn, Integer orderId) {
        int random = ThreadLocalRandom.current().nextInt(100000, 1000000); // 100000 ~ 999999
        return orderSn + orderId + random;
    }

}
