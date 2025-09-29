package com.tigshop.api.controller.user;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.AfterSalesCancelDTO;
import com.tigshop.bean.dto.order.AfterSalesFeedbackDTO;
import com.tigshop.bean.dto.order.ClientAfterSalesUpdateDTO;
import com.tigshop.bean.model.o2o.OrderPickupItem;
import com.tigshop.bean.param.order.AftersalesApplyParam;
import com.tigshop.bean.query.order.AftersalesListPageQuery;
import com.tigshop.bean.vo.order.*;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.service.o2o.OrderPickupItemService;
import com.tigshop.service.order.AftersalesLogService;
import com.tigshop.service.order.AftersalesService;
import com.tigshop.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 用户售后控制器
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/aftersales"))
@Tag(name = "前台用户售后")
public class AfterSalesController {
    @Resource
    private OrderService orderService;
    @Resource
    private AftersalesService aftersalesService;
    @Resource
    private AftersalesLogService aftersalesLogService;
    @Resource
    TranslatePackageImpl translatePackage;
    @Resource
    private OrderPickupItemService orderPickupItemService;

    @GetMapping("/list")
    @Operation(summary = "售后列表")
    public Page<AfterSalesClientVO> list(AftersalesListPageQuery listDTO) {
        Integer userId = getCurrentUserId();
        listDTO.setUserId(userId);
        return orderService.afterSalesList(listDTO);
    }

    @GetMapping("/config")
    @Operation(summary = "售后配置")
    public Map<String, Object> config(@RequestParam(value = "orderId", required = false) Integer orderId) {
        Map<Integer, String> type = new HashMap<>();
        type.put(1, translatePackage.translate("退货/退款"));
        type.put(2, translatePackage.translate("仅退款"));
        if (orderId != null) {
            long existCount = orderPickupItemService.count(Wrappers.lambdaQuery(OrderPickupItem.class)
                    .eq(OrderPickupItem::getOrderId, orderId));
            if (existCount > 0) {
                type.remove(1);
            }
        }
        List<String> reason = new ArrayList<>();
        reason.add(translatePackage.translate("多拍/拍错/不喜欢"));
        reason.add(translatePackage.translate("未按约定时间发货"));
        reason.add(translatePackage.translate("协商一致退款"));
        reason.add(translatePackage.translate("地址/电话填错了"));
        reason.add(translatePackage.translate("其他"));

        return Map.of("aftersaleType", type, "aftersaleReason", reason);
    }

    @GetMapping("/applyData")
    @Operation(summary = "售后详情")
    public AfterSalesApplyDataVO applyData(@RequestParam(value = "itemId", required = false) Integer itemId,
                                           @RequestParam(value = "orderId", required = false) Integer orderId) {
        Integer userId = getCurrentUserId();
        return AfterSalesApplyDataVO.builder()
                .list(orderService.applyData(itemId, orderId))
                .order(orderService.detail(orderId, userId))
                .build();
    }

    @PostMapping("/create")
    @Operation(summary = "售后申请")
    public void create(@RequestBody @Validated AftersalesApplyParam param) {
        aftersalesService.afterSalesApply(param);
    }

    @PostMapping("/update")
    @Operation(summary = "售后申请修改")
    public void update(@RequestBody ClientAfterSalesUpdateDTO updateDTO) {
        Integer userId = getCurrentUserId();
        updateDTO.setUserId(userId);
        aftersalesService.afterSalesApplyUpdate(updateDTO);
    }

    @GetMapping("/getRecord")
    @Operation(summary = "售后申请记录")
    public Page<AftersalesVO> getRecord(AftersalesListPageQuery listDTO) {
        Integer userId = getCurrentUserId();
        listDTO.setUserId(userId);
        return aftersalesService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "查看售后记录")
    public AfterSalesClientDetailVO detail(@RequestParam Integer id) {
        Integer userId = getCurrentUserId();
        AfterSalesClientDetailVO afterSalesClientDetailVO = aftersalesService.clientDetail(id);
        OrderVO orderVO = orderService.detail(afterSalesClientDetailVO.getOrderId(), userId);
        afterSalesClientDetailVO.setOrders(orderVO);
        return afterSalesClientDetailVO;
    }

    @GetMapping("/detailLog")
    @Operation(summary = "查看售后log记录")
    public List<AftersalesLogVO> detailLog(@RequestParam Integer id) {
        return aftersalesLogService.getDetailLog(id);
    }

    @PostMapping("/feedback")
    @Operation(summary = "提交售后反馈记录")
    public void feedback(@RequestBody AfterSalesFeedbackDTO dto) {
        Integer userId = getCurrentUserId();
        dto.setUserId(userId);
        aftersalesService.submitFeedbackRecord(dto);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消售后申请")
    public void cancel(@RequestBody AfterSalesCancelDTO dto) {
        Integer userId = getCurrentUserId();
        dto.setUserId(userId);
        aftersalesService.cancel(dto);
    }
}
