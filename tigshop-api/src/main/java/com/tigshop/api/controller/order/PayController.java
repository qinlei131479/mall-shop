package com.tigshop.api.controller.order;

import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.bean.vo.order.pay.PayIndexVO;
import com.tigshop.service.finance.PaylogService;
import com.tigshop.service.order.OrderPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付管理控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/api/order/pay")
@Validated
@Tag(name = "订单支付", description = "订单支付")
public class PayController {

    @Resource
    OrderPayService orderPayService;

    @Resource
    PaylogService paylogService;

    @GetMapping("/index")
    @Operation(summary = "订单支付")
    public PayIndexVO index(@RequestParam(value = "id") Integer id) {
        return orderPayService.getPayOrderInfo(id);
    }

    @GetMapping("/getPayLog")
    @Operation(summary = "检测订单支付状态")
    public Paylog getPayLog(@RequestParam(value = "id", required = false) Integer orderId) {
        if (orderId == null || orderId == 0) {
            throw new RuntimeException("参数缺失");
        }
        Paylog one = paylogService.lambdaQuery()
                .eq(Paylog::getOrderId, orderId)
                .eq(Paylog::getPayStatus, 1)
                .last("limit 1")
                .one();
        return one == null ? new Paylog() : one;
    }

    @GetMapping("/checkStatus")
    @Operation(summary = "检测订单支付状态")
    public Integer checkStatus(@RequestParam(value = "id") Integer id,
                               @RequestParam(value = "paylogId", required = false) Integer payLogId) {
        return orderPayService.getCheckStatus(id, payLogId);
    }

    @GetMapping("/create")
    @Operation(summary = "支付订单创建")
    public PayCreateVO create(@RequestParam(value = "id") Integer id,
                              @RequestParam(value = "type") String payType,
                              @RequestParam(value = "code", required = false) String code) {
        return orderPayService.create(id, payType, code);
    }

    @PostMapping(value = "/notify/{payType}", consumes = "application/json")
    @Operation(summary = "支付回调")
    public String notify(@RequestBody Map<String, Object> resMap,
                         @PathVariable("payType") String payType) {
        return orderPayService.payNotify(resMap, payType);
    }

    @PostMapping(value = "/notify/{payType}", consumes = "application/x-www-form-urlencoded")
    @Operation(summary = "支付回调")
    public String notify(HttpServletRequest request,
                         @PathVariable("payType") String payType) {
        Map<String, Object> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = String.join(",", values);
            params.put(name, valueStr);
        }
        return orderPayService.payNotify(params, payType);
    }

    @PostMapping(value = "/refundNotify/{payType}", consumes = "application/json")
    @Operation(summary = "退款回调地址")
    public String refundNotify(@RequestBody Map<String, Object> resMap,
                               @PathVariable("payType") String payType) {
        return orderPayService.payRefundNotify(resMap, payType);
    }
}
