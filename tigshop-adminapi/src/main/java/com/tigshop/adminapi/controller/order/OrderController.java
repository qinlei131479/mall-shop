package com.tigshop.adminapi.controller.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.*;
import com.tigshop.bean.dto.order.deliver.DeliverOrderParam;
import com.tigshop.bean.feign.shipping.ShippingResult;
import com.tigshop.bean.model.order.LogisticsApiLog;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.param.order.ExportItemSaveParam;
import com.tigshop.bean.param.order.OrderAdminNoteParam;
import com.tigshop.bean.param.order.OrderChangeStatusParam;
import com.tigshop.bean.query.order.OrderListPageQuery;
import com.tigshop.bean.vo.order.OrderPageConfigVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.o2o.OrderPickupShipmentService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.order.OrderSpitLogService;
import com.tigshop.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 订单管理控制器
 *
 * @author kidd
 * @create 2025/7/7
 */
@Tag(name = "订单管理", description = "订单管理功能")
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/order/order")
public class OrderController {

    private final OrderService orderService;
    private final OrderSpitLogService orderSplitLogService;
    private final ProductService productService;
    private final OrderPickupShipmentService orderPickupShipmentService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<OrderVO> list(OrderListPageQuery pageQuery) {
        return orderService.list(pageQuery);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public OrderVO detail(@RequestParam Integer id) {
        return orderService.detail(id);
    }

    @PostMapping("/setAdminNote")
    @Operation(summary = "设置管理员备注")
    @PreAuthorize("@pms.hasPermission('setAdminNoteManage')")
    public void setAdminNote(@RequestBody OrderAdminNoteParam param) {
        orderService.setAdminNote(param);
    }

    @GetMapping("/parentDetail")
    @Operation(summary = "获取父订单详情")
    public JSONObject parentOrderDetail(@RequestParam("id") Integer id) {
        return orderSplitLogService.getParentDataByOrderId(id);
    }

    @PostMapping("/setConfirm")
    @Operation(summary = "设置订单确认")
    @PreAuthorize("@pms.hasPermission('setConfirmManage')")
    public void setConfirm(@RequestBody OrderIdDTO orderIddto) {
        orderService.setOrderConfirm(orderIddto.getId());
    }

    @PostMapping("/splitStoreOrder")
    @Operation(summary = "订单拆分")
    @PreAuthorize("@pms.hasPermission('splitStoreOrderManage')")
    public void splitStoreOrder(@RequestBody OrderIdDTO orderIddto) {
        orderService.splitStoreOrder(orderIddto.getId());
    }

    @PostMapping("/setPaid")
    @Operation(summary = "设置已付款")
    @PreAuthorize("@pms.hasPermission('setPaidManage')")
    public void setPaid(@RequestBody OrderIdDTO orderIddto) {
        orderService.setOrderPaid(orderIddto.getId());
    }

    @PostMapping("cancelOrder")
    @Operation(summary = "取消订单")
    @PreAuthorize("@pms.hasPermission('cancelOrderManage')")
    public void cancelOrder(@RequestBody OrderIdDTO orderIddto) {
        orderService.cancelOrder(orderIddto.getId());
    }

    @PostMapping("/delOrder")
    @Operation(summary = "删除订单")
    @PreAuthorize("@pms.hasPermission('delOrderManage')")
    public void delOrder(@RequestBody OrderIdDTO orderIddto) {
        orderService.delOrder(orderIddto.getId());
    }

    @PostMapping("/modifyMoney")
    @Operation(summary = "修改订单金额")
    @PreAuthorize("@pms.hasPermission('orderModifyMoneyManage')")
    public void modifyMoney(@RequestBody OrderModifyMoneyDTO feeData) {
        orderService.modifyOrderMoney(feeData);
    }

    @PostMapping("/modifyConsignee")
    @Operation(summary = "修改收货人信息")
    @PreAuthorize("@pms.hasPermission('orderModifyConsigneeManage')")
    public void modifyConsignee(@RequestBody OrderModifyConsigneeDTO modifyConsigneeDTO) {
        orderService.modifyOrderConsignee(modifyConsigneeDTO);
    }

    @PostMapping("/confirmReceipt")
    @Operation(summary = "确认收货")
    @PreAuthorize("@pms.hasPermission('orderConfirmReceiptManage')")
    public void confirmReceipt(@RequestBody OrderIdDTO orderIddto) {
        orderService.confirmReceipt(orderIddto.getId());
        orderPickupShipmentService.writeOff(orderIddto.getId());
    }

    @PostMapping("/modifyShipping")
    @Operation(summary = "修改配送信息")
    @PreAuthorize("@pms.hasPermission('orderModifyShippingManage')")
    public void modifyShipping(@RequestBody OrderModifyShippingDTO modifyShippingDTO) {
        orderService.modifyOrderShipping(modifyShippingDTO);
    }

    @PostMapping("/modifyProduct")
    @Operation(summary = "修改商品信息")
    @PreAuthorize("@pms.hasPermission('modifyProductManage')")
    public void modifyProduct(@RequestBody OrderModifyProductDTO modifyProductDTO) {
        orderService.modifyOrderProduct(modifyProductDTO);
    }

    @PostMapping("/getAddProductInfo")
    @Operation(summary = "添加商品时获取商品信息")
    public List<Product> getAddProductInfo(@RequestBody Map<String, List<Integer>> params) {
        return productService.getProductByIds(params.get("ids"));
    }

    @Operation(summary = "发货")
    @PreAuthorize("@pms.hasPermission('orderDeliverManage')")
    @PostMapping("/deliver")
    public void deliver(@RequestBody @Validated DeliverOrderParam param) {
        orderPickupShipmentService.stockUp(param.getId());
        orderService.deliverOrder(param);
    }

    @GetMapping("/orderPrint")
    @Operation(summary = "打印订单")
    public OrderVO orderPrint(@RequestParam Integer id) {
        return orderService.getOrderPrintInfo(id);
    }

    @GetMapping("/orderPrintBill")
    @Operation(summary = "打印电子面单")
    public LogisticsApiLog orderPrintWaybill(@RequestParam Integer id) {
        return orderService.getOrderPrintWaybillInfo(id);
    }

    @GetMapping("/severalDetail")
    @Operation(summary = "批量详情")
    public List<OrderVO> severalDetail(@RequestParam String ids) {
        List<Integer> list = Arrays.stream(ids.split(",")).map(Integer::parseInt).toList();
        if (CollUtil.isNotEmpty(list)) {
            return orderService.getSeveralDetail(list, null);
        } else {
            throw new GlobalException("#ids 错误");
        }
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('splitStoreOrderManage')")
    public void batch(@RequestBody com.tigshop.bean.dto.order.OrderBatchDTO dto) {
        if ("deliver".equals(dto.getType())) {
            try {
                for (Integer id : dto.getIds()) {
                    orderService.batchOperation(id, dto.getType(), dto.getData());
                }
            } catch (Exception e) {
                throw new GlobalException(e.getMessage());
            }
        } else {
            throw new GlobalException("#type 错误");
        }
    }

    @GetMapping("/printSeveral")
    @Operation(summary = "批量打印")
    public List<OrderVO> printSeveral(@RequestParam List<Integer> ids) {
        return orderService.printSeveral(ids);
    }

    @GetMapping("/getExportItemList")
    @Operation(summary = "订单导出标签列表")
    public Map<String, String> getExportItemList() {
        return orderService.getExportItemList();
    }

    @GetMapping("/exportItemInfo")
    @Operation(summary = "标签详情")
    public Map<String, String> exportItemInfo() {
        return orderService.exportItemInfo();
    }

    @PostMapping("/saveExportItem")
    @Operation(summary = "保存导出标签信息")
    public void saveExportItem(@RequestBody @Validated ExportItemSaveParam param) {
        orderService.saveExportItem(param);
    }

    @GetMapping("/orderExport")
    @Operation(summary = "订单导出")
    public void exportItemInfo(HttpServletResponse response, @Validated OrderListPageQuery pageQuery) {
        orderService.orderExport(pageQuery, response);
    }

    @GetMapping("/shippingInfo")
    @Operation(summary = "物流信息")
    public ShippingResult shippingInfo(@RequestParam Integer id) {
        return orderService.getShippingInfo(id);
    }

    @Operation(summary = "获取订单列表配置")
    @GetMapping("/getOrderPageConfig")
    public OrderPageConfigVO getOrderPageConfig() {
        return orderService.getOrderPageConfig();
    }

    @Operation(summary = "修改订单状态")
    @PostMapping("/changeOrderStatus")
    public void changeOrderStatus(@RequestBody @Validated OrderChangeStatusParam param) {
        orderService.changeOrderStatus(param);
    }


    @Operation(summary = "催发货")
    @PostMapping("/remindDeliver")
    @PreAuthorize("@pms.hasPermission('remindDeliver')")
    public void remindDeliver(@RequestParam Integer id) {
        orderService.remindDeliver(id);
    }

}
