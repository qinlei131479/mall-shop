package com.tigshop.service.panel.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.panel.*;
import com.tigshop.bean.enums.order.AftersalesStatusEnum;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.order.PaymentStatus;
import com.tigshop.bean.model.finance.RefundApply;
import com.tigshop.bean.model.finance.UserRechargeOrder;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.user.Feedback;
import com.tigshop.bean.model.vendor.VendorShopBind;
import com.tigshop.bean.model.vendor.product.VendorProduct;
import com.tigshop.bean.model.vendor.product.VendorProductSku;
import com.tigshop.bean.query.panel.SalesProductDetailPageQuery;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.panel.*;
import com.tigshop.bean.vo.vendor.PanelVendorIndexVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.ExcelUtils;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.finance.RefundApplyMapper;
import com.tigshop.mapper.finance.UserRechargeOrderMapper;
import com.tigshop.mapper.order.AftersalesItemMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.vendor.VendorMapper;
import com.tigshop.mapper.vendor.VendorShopBindMapper;
import com.tigshop.mapper.vendor.product.VendorProductMapper;
import com.tigshop.mapper.vendor.product.VendorProductSkuMapper;
import com.tigshop.service.log.StatisticsBaseService;
import com.tigshop.service.log.StatisticsLogService;
import com.tigshop.service.order.AftersalesService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.panel.SalesStatisticsService;
import com.tigshop.service.salesman.OverviewService;
import com.tigshop.service.user.FeedbackService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 面板服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
@Service
public class SalesStatisticsServiceImpl implements SalesStatisticsService {

    @Resource
    private OrderService orderService;

    @Resource
    private AftersalesService aftersalesService;

    @Resource
    private FeedbackService feedbackService;

    @Resource
    private StatisticsBaseService statisticsBaseService;

    @Resource
    private StatisticsLogService statisticsLogService;

    @Resource
    private OverviewService overviewService;

    @Resource
    private OrderItemMapper orderItemMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private RefundApplyMapper refundApplyMapper;

    @Resource
    private AftersalesItemMapper aftersalesItemMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private UserRechargeOrderMapper userRechargeOrderMapper;

    @Resource
    private VendorProductSkuMapper vendorProductSkuMapper;

    @Resource
    private VendorProductMapper vendorProductMapper;
    @Autowired
    private VendorShopBindMapper vendorShopBindMapper;
    @Autowired
    private VendorMapper vendorMapper;

    @Override
    public PanelIndexVO getPanelIndex() {
        return PanelIndexVO.builder()
                .consoleData(this.getConsoleData())
                .realTimeData(this.getRealTimeData())
                .panelStatisticalData(this.getPanelStatisticalData())
                .build();
    }

    @Override
    public SalesStatisticsDataVO getSalesData(SalesDataDTO salesDataDTO, HttpServletResponse response) {

        if (salesDataDTO.getStartEndTime() == null) {
            throw new GlobalException("请选择日期");
        }
        String[] startEndTime = TigUtils.getRangeDate(salesDataDTO.getDateType(), salesDataDTO.getStartEndTime(), salesDataDTO.getStartEndTime());
        // 获取环比时间区间
        String[] prevDate = StringUtils.getPrevDate(startEndTime, salesDataDTO.getDateType());

        // 商品支付金额
        Order orderPaidSum = orderMapper.selectOne(new MPJLambdaWrapper<Order>().eq(Order::getPayStatus, PaymentStatus.PAID.getCode()).eq(Order::getIsDel, 0)
                .eq(Order::getShopId, salesDataDTO.getShopId())
                .between(Order::getPayTime, TigUtils.toTimestampYmd(startEndTime[0]), TigUtils.toTimestampYmd(startEndTime[1]))
                .selectSum(Order::getTotalAmount)
        );
        BigDecimal productPayment = orderPaidSum != null ? orderPaidSum.getTotalAmount() : BigDecimal.ZERO;

        Order orderPrevPaidSum = orderMapper.selectOne(new MPJLambdaWrapper<Order>().eq(Order::getPayStatus, PaymentStatus.PAID.getCode()).eq(Order::getIsDel, 0)
                .eq(Order::getShopId, salesDataDTO.getShopId())
                .between(Order::getPayTime, TigUtils.toTimestampYmd(prevDate[0]), TigUtils.toTimestampYmd(prevDate[1]))
                .selectSum(Order::getTotalAmount)
        );
        BigDecimal prevProductPayment = orderPrevPaidSum != null ? orderPrevPaidSum.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal productPaymentGrowthRate = TigUtils.getGrowthRate(prevProductPayment, productPayment);

        //商品退款金额
        RefundApply refundApplySum = refundApplyMapper.selectOne(new MPJLambdaWrapper<RefundApply>().eq(RefundApply::getShopId, salesDataDTO.getShopId())
                .between(RefundApply::getAddTime, TigUtils.toTimestampYmd(startEndTime[0]), TigUtils.toTimestampYmd(startEndTime[1]))
                .eq(RefundApply::getRefundStatus, 2)
                .select("SUM(online_balance + offline_balance + refund_balance) AS online_balance")
        );
        BigDecimal productRefund = refundApplySum != null ? refundApplySum.getOnlineBalance() : BigDecimal.ZERO;
        RefundApply prevRefundApplySum = refundApplyMapper.selectOne(new MPJLambdaWrapper<RefundApply>().eq(RefundApply::getShopId, salesDataDTO.getShopId())
                .between(RefundApply::getAddTime, TigUtils.toTimestampYmd(prevDate[0]), TigUtils.toTimestampYmd(prevDate[1]))
                .eq(RefundApply::getRefundStatus, 2)
                .select("SUM(online_balance + offline_balance + refund_balance) AS online_balance")
        );
        BigDecimal prevProductRefund = prevRefundApplySum != null ? prevRefundApplySum.getOnlineBalance() : BigDecimal.ZERO;

        BigDecimal productRefundGrowthRate = TigUtils.getGrowthRate(prevProductPayment.subtract(prevProductRefund), productPayment.subtract(productRefund));

        //充值金额
        UserRechargeOrder userRechargeOrderSum = userRechargeOrderMapper.selectOne(new MPJLambdaWrapper<UserRechargeOrder>().eq(UserRechargeOrder::getStatus, 1)
                .between(UserRechargeOrder::getAddTime, TigUtils.toTimestampYmd(startEndTime[0]), TigUtils.toTimestampYmd(startEndTime[1]))
                .selectSum(UserRechargeOrder::getAmount)
        );
        BigDecimal rechargeAmount = userRechargeOrderSum != null && salesDataDTO.getShopId() == 0 ? userRechargeOrderSum.getAmount() : BigDecimal.ZERO;

        UserRechargeOrder prevUserRechargeOrderSum = userRechargeOrderMapper.selectOne(new MPJLambdaWrapper<UserRechargeOrder>().eq(UserRechargeOrder::getStatus, 1)
                .between(UserRechargeOrder::getAddTime, TigUtils.toTimestampYmd(prevDate[0]), TigUtils.toTimestampYmd(prevDate[1]))
                .selectSum(UserRechargeOrder::getAmount)
        );
        BigDecimal prevRechargeAmount = prevUserRechargeOrderSum != null && salesDataDTO.getShopId() == 0 ? prevUserRechargeOrderSum.getAmount() : BigDecimal.ZERO;
        BigDecimal rechargeAmountGrowthRate = TigUtils.getGrowthRate(prevRechargeAmount, rechargeAmount);

        // 营业额
        BigDecimal turnover = productPayment.add(rechargeAmount);
        BigDecimal prevTurnover = prevProductPayment.add(prevRechargeAmount);
        BigDecimal turnoverGrowthRate = TigUtils.getGrowthRate(prevTurnover, turnover);

        //余额支付金额
        Order orderBalancePaidSum = orderMapper.selectOne(new MPJLambdaWrapper<Order>().eq(Order::getPayStatus, PaymentStatus.PAID.getCode()).eq(Order::getIsDel, 0)
                .eq(Order::getShopId, salesDataDTO.getShopId())
                .between(Order::getPayTime, TigUtils.toTimestampYmd(startEndTime[0]), TigUtils.toTimestampYmd(startEndTime[1]))
                .selectSum(Order::getBalance)
        );
        BigDecimal balancePayment = orderBalancePaidSum != null ? orderBalancePaidSum.getBalance() : BigDecimal.ZERO;
        Order prevOrderBalancePaidSum = orderMapper.selectOne(new MPJLambdaWrapper<Order>().eq(Order::getPayStatus, PaymentStatus.PAID.getCode()).eq(Order::getIsDel, 0)
                .eq(Order::getShopId, salesDataDTO.getShopId())
                .between(Order::getPayTime, TigUtils.toTimestampYmd(prevDate[0]), TigUtils.toTimestampYmd(prevDate[1]))
                .selectSum(Order::getBalance)
        );
        BigDecimal prevBalancePayment = prevOrderBalancePaidSum != null ? prevOrderBalancePaidSum.getBalance() : BigDecimal.ZERO;
        BigDecimal balancePaymentGrowthRate = TigUtils.getGrowthRate(prevBalancePayment, balancePayment);
        SaleDataVO saleDataVO = new SaleDataVO();
        saleDataVO.setProductPayment(productPayment);
        saleDataVO.setProductPaymentGrowthRate(productPaymentGrowthRate);
        saleDataVO.setProductRefund(productRefund);
        saleDataVO.setPrevProductRefund(prevProductRefund);
        saleDataVO.setProductRefundGrowthRate(productRefundGrowthRate);
        saleDataVO.setRechargeAmount(rechargeAmount);
        saleDataVO.setRechargeAmountGrowthRate(rechargeAmountGrowthRate);
        saleDataVO.setTurnover(turnover);
        saleDataVO.setTurnoverGrowthRate(turnoverGrowthRate);
        saleDataVO.setBalancePayment(balancePayment);
        saleDataVO.setBalancePaymentGrowthRate(balancePaymentGrowthRate);
        AxisVO salesStatisticsData = getSalesStatisticsData(salesDataDTO.getDateType(), startEndTime, salesDataDTO.getStatisticType(), salesDataDTO.getShopId());
        if (salesDataDTO.getIsExport() != null && salesDataDTO.getIsExport() == 1) {
            if (salesDataDTO.getStatisticType() != null && salesDataDTO.getStatisticType() > 0) {
                List<String> horizontalAxis = salesStatisticsData.getHorizontalAxis();
                List<OrderAmountExportDTO> orderAmountExportDTOList = new ArrayList<>();
                for (int i = 0; i < horizontalAxis.size(); i++) {
                    OrderAmountExportDTO orderAmountExportDTO = new OrderAmountExportDTO();
                    orderAmountExportDTO.setDate(horizontalAxis.get(i));
                    orderAmountExportDTO.setAmount(salesStatisticsData.getLongitudinalAxis().get(i));
                    orderAmountExportDTOList.add(orderAmountExportDTO);
                }
                ExcelUtils<OrderAmountExportDTO> excelUtils = new ExcelUtils<>(orderAmountExportDTOList, OrderAmountExportDTO.class);
                excelUtils.exportExcelWeb(response);
            } else {
                //OrderCountExportDTO数据结构的导出
                List<OrderCountExportDTO> orderCountExportDTOList = new ArrayList<>();
                for (int i = 0; i < salesStatisticsData.getHorizontalAxis().size(); i++) {
                    OrderCountExportDTO orderCountExportDTO = new OrderCountExportDTO();
                    orderCountExportDTO.setDate(salesStatisticsData.getHorizontalAxis().get(i));
                    orderCountExportDTO.setCount(salesStatisticsData.getLongitudinalAxis().get(i));
                    orderCountExportDTOList.add(orderCountExportDTO);
                }
                ExcelUtils<OrderCountExportDTO> excelUtils = new ExcelUtils<>(orderCountExportDTOList, OrderAmountExportDTO.class);
                excelUtils.exportExcelWeb(response);
            }
        }
        return SalesStatisticsDataVO.builder()
                .salesData(saleDataVO)
                .salesStatisticsData(salesStatisticsData)
                .build();
    }

    private AxisVO getSalesStatisticsData(Integer dateType, String[] startEndTime, Integer statisticType, Integer shopId) {

        String startDate = startEndTime[0];
        String endDate = startEndTime[1];
        List<String> horizontalAxis = overviewService.getHorizontalAxis(dateType, startDate, endDate);
        List<Map<String, Object>> orderStatisticsList = orderService.listMaps(new QueryWrapper<Order>()
                .eq("shop_id", shopId)
                .eq("pay_status", PaymentStatus.PAID.getCode())
                .between("pay_time", TigUtils.toTimestampYmd(startDate), TigUtils.toTimestampYmd(endDate))
                .select("pay_time AS payTime", "total_amount AS totalAmount")
        );
        List<BigDecimal> longitudinalAxis;
        List<JSONObject> orderStatisticsListJson = orderStatisticsList.stream()
                .map(JSONObject::new)
                .toList();
        if (statisticType > 0) {
            longitudinalAxis = overviewService.getLongitudinalAxis(horizontalAxis, orderStatisticsListJson, dateType, 4);
        } else {
            longitudinalAxis = overviewService.getLongitudinalAxis(horizontalAxis, orderStatisticsListJson, dateType, 5);
        }
        AxisVO result = new AxisVO();
        result.setHorizontalAxis(horizontalAxis);
        result.setLongitudinalAxis(longitudinalAxis);
        return result;
    }

    @Override
    public SalesDetailVO getSalesDetail(SalesDetailDTO salesDetailDTO) {

        if (salesDetailDTO.getStartTime() == null || salesDetailDTO.getEndTime() == null) {
            throw new GlobalException("请选择日期");
        }
        String[] startEndTime = new String[]{salesDetailDTO.getStartTime(), salesDetailDTO.getEndTime()};
        String[] prevDate = StringUtils.getPrevDate(startEndTime, 4);
        // 商品浏览量
        Integer productView = statisticsLogService.getVisitNumByProduct(new String[]{salesDetailDTO.getStartTime(), salesDetailDTO.getEndTime()}, 1, 1, salesDetailDTO.getShopId());
        Integer prevProductView = statisticsLogService.getVisitNumByProduct(prevDate, 1, 1, salesDetailDTO.getShopId());
        BigDecimal productViewGrowthRate = TigUtils.getGrowthRate(new BigDecimal(prevProductView), new BigDecimal(productView));

        // 商品访客数
        Integer productVisitor = statisticsLogService.getVisitNumByProduct(new String[]{salesDetailDTO.getStartTime(), salesDetailDTO.getEndTime()}, 0, 1, salesDetailDTO.getShopId());
        Integer prevProductVisitor = statisticsLogService.getVisitNumByProduct(prevDate, 0, 1, salesDetailDTO.getShopId());
        BigDecimal productVisitorGrowthRate = TigUtils.getGrowthRate(new BigDecimal(prevProductVisitor), new BigDecimal(productVisitor));

        // 下单件数
        OrderItem orderIteSum = orderItemMapper.selectOne(new MPJLambdaWrapper<OrderItem>()
                .leftJoin(Order.class, Order::getOrderId, OrderItem::getOrderId).eq(Order::getPayStatus, PaymentStatus.PAID.getCode()).eq(Order::getIsDel, 0)
                .between(Order::getPayTime, TigUtils.toTimestampYmd(startEndTime[0]), TigUtils.toTimestampYmd(startEndTime[1]))
                .eq(OrderItem::getShopId, salesDetailDTO.getShopId())
                .selectSum(OrderItem::getQuantity)
        );
        int orderNum = orderIteSum != null ? orderIteSum.getQuantity() : 0;
        OrderItem prevOrderIteSum = orderItemMapper.selectOne(new MPJLambdaWrapper<OrderItem>()
                .leftJoin(Order.class, Order::getOrderId, OrderItem::getOrderId).eq(Order::getPayStatus, PaymentStatus.PAID.getCode()).eq(Order::getIsDel, 0)
                .between(Order::getPayTime, TigUtils.toTimestampYmd(prevDate[0]), TigUtils.toTimestampYmd(prevDate[1]))
                .eq(OrderItem::getShopId, salesDetailDTO.getShopId())
                .selectSum(OrderItem::getQuantity)
        );
        int prevOrderNum = prevOrderIteSum != null ? prevOrderIteSum.getQuantity() : 0;
        BigDecimal orderNumGrowthRate = TigUtils.getGrowthRate(new BigDecimal(prevOrderNum), new BigDecimal(orderNum));

        // 支付金额
        Order orderPaidSum = orderMapper.selectOne(new MPJLambdaWrapper<Order>().eq(Order::getPayStatus, PaymentStatus.PAID.getCode()).eq(Order::getIsDel, 0)
                .eq(Order::getShopId, salesDetailDTO.getShopId())
                .between(Order::getPayTime, TigUtils.toTimestampYmd(startEndTime[0]), TigUtils.toTimestampYmd(startEndTime[1]))
                .selectSum(Order::getTotalAmount)
        );
        BigDecimal paymentAmount = orderPaidSum != null ? orderPaidSum.getTotalAmount() : BigDecimal.ZERO;
        Order prevOrderPaidSum = orderMapper.selectOne(new MPJLambdaWrapper<Order>().eq(Order::getPayStatus, PaymentStatus.PAID.getCode()).eq(Order::getIsDel, 0)
                .eq(Order::getShopId, salesDetailDTO.getShopId())
                .between(Order::getPayTime, TigUtils.toTimestampYmd(prevDate[0]), TigUtils.toTimestampYmd(prevDate[1]))
                .selectSum(Order::getTotalAmount)
        );
        BigDecimal prevPaymentAmount = prevOrderPaidSum != null ? prevOrderPaidSum.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal paymentAmountGrowthRate = TigUtils.getGrowthRate(prevPaymentAmount, paymentAmount);

        // 退款金额
        RefundApply refundApplySum = refundApplyMapper.selectOne(new MPJLambdaWrapper<RefundApply>().eq(RefundApply::getShopId, salesDetailDTO.getShopId())
                .between(RefundApply::getAddTime, TigUtils.toTimestampYmd(startEndTime[0]), TigUtils.toTimestampYmd(startEndTime[1]))
                .eq(RefundApply::getRefundStatus, 2)
                .select("SUM(online_balance + offline_balance + refund_balance) AS online_balance")
        );
        BigDecimal refundAmount = refundApplySum != null ? refundApplySum.getOnlineBalance() : BigDecimal.ZERO;
        RefundApply prevRefundApplySum = refundApplyMapper.selectOne(new MPJLambdaWrapper<RefundApply>().eq(RefundApply::getShopId, salesDetailDTO.getShopId())
                .between(RefundApply::getAddTime, TigUtils.toTimestampYmd(prevDate[0]), TigUtils.toTimestampYmd(prevDate[1]))
                .eq(RefundApply::getRefundStatus, 2)
                .select("SUM(online_balance + offline_balance + refund_balance) AS online_balance")
        );
        BigDecimal prevRefundAmount = prevRefundApplySum != null ? prevRefundApplySum.getOnlineBalance() : BigDecimal.ZERO;
        BigDecimal refundAmountGrowthRate = TigUtils.getGrowthRate(prevRefundAmount, refundAmount);

        // 退款件数
        AftersalesItem afterSalesItemSum = aftersalesItemMapper.selectOne(new MPJLambdaWrapper<AftersalesItem>()
                .leftJoin(Aftersales.class, Aftersales::getAftersaleId, AftersalesItem::getAftersaleId)
                .eq(Aftersales::getShopId, salesDetailDTO.getShopId())
                .between(Aftersales::getAddTime, TigUtils.toTimestampYmd(startEndTime[0]), TigUtils.toTimestampYmd(startEndTime[1]))
                .eq(Aftersales::getStatus, 6)
                .select("SUM(number) AS number")
        );
        int refundQuantity = afterSalesItemSum != null ? afterSalesItemSum.getNumber() : 0;
        AftersalesItem prevAfterSalesItemSum = aftersalesItemMapper.selectOne(new MPJLambdaWrapper<AftersalesItem>()
                .leftJoin(Aftersales.class, Aftersales::getAftersaleId, AftersalesItem::getAftersaleId)
                .eq(Aftersales::getShopId, salesDetailDTO.getShopId())
                .between(Aftersales::getAddTime, TigUtils.toTimestampYmd(prevDate[0]), TigUtils.toTimestampYmd(prevDate[1]))
                .eq(Aftersales::getStatus, 6)
                .select("SUM(number) AS number")
        );
        int prevRefundQuantity = prevAfterSalesItemSum != null ? prevAfterSalesItemSum.getNumber() : 0;
        BigDecimal refundQuantityGrowthRate = TigUtils.getGrowthRate(new BigDecimal(prevRefundQuantity), new BigDecimal(refundQuantity));

        // 创建 SalesDetailVO 对象
        SalesDetailVO salesDetailVO = new SalesDetailVO();

        // 填充 SalesData
        SalesDetailVO.SalesData salesData = new SalesDetailVO.SalesData();
        salesData.setProductView(productView);
        salesData.setProductViewGrowthRate(productViewGrowthRate);
        salesData.setProductVisitor(productVisitor);
        salesData.setProductVisitorGrowthRate(productVisitorGrowthRate);
        salesData.setOrderNum(orderNum);
        salesData.setOrderNumGrowthRate(orderNumGrowthRate.toString());
        salesData.setPaymentAmount(paymentAmount);
        salesData.setPaymentAmountGrowthRate(paymentAmountGrowthRate);
        salesData.setRefundAmount(refundAmount);
        salesData.setRefundAmountGrowthRate(refundAmountGrowthRate.toString());
        salesData.setRefundQuantity(refundQuantity);
        salesData.setRefundQuantityGrowthRate(refundQuantityGrowthRate.toString());
        salesDetailVO.setSalesData(salesData);

        // 填充 SalesStatisticsData
        SalesDetailVO.SalesStatisticsData salesStatisticsDetail = getSalesStatisticsDetail(startEndTime, salesDetailDTO.getShopId());
        salesDetailVO.setSalesStatisticsData(salesStatisticsDetail);
        return salesDetailVO;
    }

    @Override
    public Page<SalesProductOrderItemVO> getSalesProductDetail(SalesProductDetailPageQuery pageQuery, HttpServletResponse response) {
        pageQuery.init();
        Page<SalesProductOrderItemVO> resultPage = orderService.getSalesProductDetail(pageQuery);

        resultPage.getRecords().forEach(item -> {
            OrderVO orders = new OrderVO();
            orders.setAddTime(TigUtils.handelTime(Long.parseLong(item.getAddTime())));
            item.setOrders(orders);
        });

        if (pageQuery.getIsExport() != null && pageQuery.getIsExport() == 1) {
            List<SalesProductDetailExportDTO> salesProductDetailExportDTOList = resultPage.getRecords().stream().map(orderItemVO -> {
                SalesProductDetailExportDTO salesProductDetailExportDTO = new SalesProductDetailExportDTO();
                BeanUtils.copyProperties(orderItemVO, salesProductDetailExportDTO);
                return salesProductDetailExportDTO;
            }).toList();
            ExcelUtils<SalesProductDetailExportDTO> excelUtils = new ExcelUtils<>(salesProductDetailExportDTOList, SalesProductDetailExportDTO.class);
            excelUtils.exportExcelWeb(response);
        }

        return resultPage;
    }

    @Override
    public SalesIndicatorsVO getSalesIndicators() {

        Integer shopId = HeaderUtils.getShopId();
        //订单总数
        long orderNum = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getIsDel, 0)
                .eq(Order::getShopId, shopId)
                .in(Order::getOrderStatus, List.of(OrderStatusEnum.CONFIRMED.getCode(), OrderStatusEnum.PROCESSING.getCode(), OrderStatusEnum.COMPLETED.getCode())));
        //订单商品总数
        long orderProductNum = orderItemMapper.selectJoinCount(new MPJLambdaWrapper<OrderItem>().leftJoin(Order.class, Order::getOrderId, OrderItem::getOrderId)
                .eq(OrderItem::getShopId, shopId)
                .eq(Order::getIsDel, 0)
                .eq(Order::getShopId, shopId)
                .in(Order::getOrderStatus, List.of(OrderStatusEnum.CONFIRMED.getCode(), OrderStatusEnum.PROCESSING.getCode(), OrderStatusEnum.COMPLETED.getCode())));
        //订单总金额
        Order orderTotalAmountData = orderMapper.selectOne(new QueryWrapper<Order>().eq("is_del", 0)
                .eq("shop_id", shopId)
                .in("order_status", List.of(OrderStatusEnum.CONFIRMED.getCode(), OrderStatusEnum.PROCESSING.getCode(), OrderStatusEnum.COMPLETED.getCode()))
                .select("sum(total_amount) as total_amount"));
        BigDecimal orderTotalAmount = orderTotalAmountData != null ? orderTotalAmountData.getTotalAmount() : BigDecimal.ZERO;
        //会员总数
        Long userNum = userMapper.selectCount(new LambdaQueryWrapper<>());
        //消费会员总数
        Long consumerMembershipNum = orderMapper.selectCount(new QueryWrapper<Order>()
                .eq("is_del", 0)
                .eq("shop_id", shopId)
                .in("order_status", List.of(OrderStatusEnum.CONFIRMED.getCode(), OrderStatusEnum.PROCESSING.getCode(), OrderStatusEnum.COMPLETED.getCode()))
                .select("DISTINCT user_id")
        );
        //人均消费数
        BigDecimal capitaConsumption = userNum > 0 ? orderTotalAmount.divide(new BigDecimal(userNum), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        //访问数 -- 商品点击数
        Product clickCountData = productMapper.selectOne(new QueryWrapper<Product>()
                .eq("shop_id", shopId)
                .select("sum(click_count) as click_count")
        );
        long clickCount = (clickCountData != null ? clickCountData.getClickCount() : 0);
        //访问转化率
        BigDecimal clickRate = clickCount > 0 ? new BigDecimal(orderNum).divide(new BigDecimal(clickCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        //订单转化率
        BigDecimal orderRate = clickCount > 0 ? orderTotalAmount.divide(new BigDecimal(clickCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        //消费会员比率
        BigDecimal consumerMembershipRate = userNum > 0 ? new BigDecimal(consumerMembershipNum).divide(new BigDecimal(userNum), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        //购买率
        BigDecimal purchaseRate = userNum > 0 ? new BigDecimal(orderNum).divide(new BigDecimal(userNum), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        return SalesIndicatorsVO.builder()
                .orderNum(orderNum)
                .orderProductNum(orderProductNum)
                .orderTotalAmount(orderTotalAmount)
                .userNum(userNum)
                .consumerMembershipNum(consumerMembershipNum)
                .capitaConsumption(capitaConsumption)
                .clickCount(clickCount)
                .clickRate(clickRate)
                .orderRate(orderRate)
                .consumerMembershipRate(consumerMembershipRate)
                .purchaseRate(purchaseRate)
                .build();
    }

    @Override
    public Page<SalesRankingExportDTO> getSalesRanking(SalesRankingDTO salesRankingDTO, HttpServletResponse response) {
        String[] startEndTime = new String[]{salesRankingDTO.getStartTime(), salesRankingDTO.getEndTime()};

        MPJLambdaWrapper<OrderItem> mpjLambdaWrapper = new MPJLambdaWrapper<OrderItem>().leftJoin(Order.class, Order::getOrderId, OrderItem::getOrderId)
                .eq(Order::getIsDel, 0)
                .between(Order::getAddTime, TigUtils.toTimestampYmd(startEndTime[0]), TigUtils.toTimestampYmd(startEndTime[1]))
                .in(Order::getOrderStatus, List.of(OrderStatusEnum.CONFIRMED.getCode(), OrderStatusEnum.PROCESSING.getCode(), OrderStatusEnum.COMPLETED.getCode()))
                .groupBy("t1.order_id");
        if (salesRankingDTO.getKeyword() != null) {
            mpjLambdaWrapper = mpjLambdaWrapper.and(wrapper -> {
                wrapper.like(OrderItem::getProductSn, salesRankingDTO.getKeyword())
                        .or()
                        .like(OrderItem::getProductName, salesRankingDTO.getKeyword());
            });
        }
        if (salesRankingDTO.getShopId() != null) {
            mpjLambdaWrapper = mpjLambdaWrapper.eq(OrderItem::getShopId, salesRankingDTO.getShopId());
        }
        mpjLambdaWrapper.select("SUM(quantity * price) AS totalSalesAmount,SUM(quantity) AS totalSalesNum")
                .select("MAX(product_name) AS productName")
                .select("MAX(product_sn) AS productSn")
                .select("MAX(sku_data) AS skuData")
                .orderBy(true, Objects.equals(salesRankingDTO.getSortOrder(), "asc"), salesRankingDTO.getSortField())
        ;
        Page<SalesRankingExportDTO> resultPage = orderItemMapper.selectJoinPage(new Page<>(salesRankingDTO.getPage(), salesRankingDTO.getSize()), SalesRankingExportDTO.class, mpjLambdaWrapper);
        if (salesRankingDTO.getIsExport() != null && salesRankingDTO.getIsExport() == 1) {
            List<SalesProductDetailExportDTO> salesProductDetailExportDTOList = resultPage.getRecords().stream().map(orderItemVO -> {
                SalesProductDetailExportDTO salesProductDetailExportDTO = new SalesProductDetailExportDTO();
                BeanUtils.copyProperties(orderItemVO, salesProductDetailExportDTO);
                return salesProductDetailExportDTO;
            }).toList();
            ExcelUtils<SalesProductDetailExportDTO> excelUtils = new ExcelUtils<>(salesProductDetailExportDTOList, SalesProductDetailExportDTO.class);
            excelUtils.exportExcelWeb(response);
        }
        return resultPage;
    }

    @Override
    public SalesDetailVO.SalesStatisticsData getSalesStatisticsDetail(String[] startEndTime, Integer shopId) {

        String startDate = startEndTime[0];
        String endDate = startEndTime[1];
        //横轴
        List<String> horizontalAxis = overviewService.getHorizontalAxis(0, startDate, endDate);
        //支付金额
        List<Order> paymentAmountList = orderMapper.selectList(new MPJLambdaWrapper<Order>().eq(Order::getShopId, shopId)
                .eq(Order::getPayStatus, PaymentStatus.PAID.getCode())
                .between(Order::getAddTime, TigUtils.toTimestampYmd(startDate), TigUtils.toTimestampYmd(endDate))
        );
        //转List<map>
        List<JSONObject> paymentAmountListMap = paymentAmountList.stream()
                .map(order -> JSON.parseObject(JSON.toJSONString(order)))
                .toList();
        List<BigDecimal> longitudinalAxisPaymentAmount = overviewService.getLongitudinalAxis(horizontalAxis, paymentAmountListMap, 0, 4);

        // 退款金额
        List<RefundApply> refundAmountList = refundApplyMapper.selectList(new MPJLambdaWrapper<RefundApply>().eq(RefundApply::getShopId, shopId)
                .between(RefundApply::getAddTime, TigUtils.toTimestampYmd(startDate), TigUtils.toTimestampYmd(endDate))
        );
        List<JSONObject> refundAmountListMap = refundAmountList.stream()
                .map(refundApply -> JSON.parseObject(JSON.toJSONString(refundApply)))
                .toList();
        List<BigDecimal> longitudinalAxisRefundAmount = overviewService.getLongitudinalAxis(horizontalAxis, refundAmountListMap, 0, 6);

        // 商品浏览量
        List<Map<String, Object>> accessData = statisticsLogService.getVisitList(startEndTime, 1, 1, shopId);
        List<BigDecimal> longitudinalAxisProductView = overviewService.getLongitudinalAxis(horizontalAxis, accessData.stream().map(object -> JSON.parseObject(JSON.toJSONString(object))).toList(), 0, 2);

        // 商品访客量
        List<Map<String, Object>> visitData = statisticsLogService.getVisitList(startEndTime, 0, 1, shopId);
        List<BigDecimal> longitudinalAxisProductVisitor = overviewService.getLongitudinalAxis(horizontalAxis, visitData.stream().map(object -> JSON.parseObject(JSON.toJSONString(object))).toList(), 0, 2);
        SalesDetailVO.SalesStatisticsData salesStatisticsData = new SalesDetailVO.SalesStatisticsData();
        salesStatisticsData.setHorizontalAxis(horizontalAxis);
        salesStatisticsData.setLongitudinalAxisPaymentAmount(longitudinalAxisPaymentAmount);
        salesStatisticsData.setLongitudinalAxisRefundAmount(longitudinalAxisRefundAmount);
        salesStatisticsData.setLongitudinalAxisProductView(longitudinalAxisProductView);
        salesStatisticsData.setLongitudinalAxisProductVisitor(longitudinalAxisProductVisitor);
        return salesStatisticsData;
    }

    @SuppressWarnings("AlibabaAvoidCommentBehindStatement")
    @Override
    public PanelVendorIndexVO getPanelVendorIndex() {
        // 获取当前供应商ID（这里需要根据实际的权限系统获取）
        Integer vendorId = HeaderUtils.getVendorId();

        // 获取控制台数据 - 基于真实数据库查询
        PanelVendorIndexVO.ConsoleData consoleData = getVendorConsoleData(vendorId);

        // 获取实时数据 - 基于真实数据库查询
        PanelVendorIndexVO.RealTimeData realTimeData = getVendorRealTimeData(vendorId);

        // 获取统计图表数据 - 基于真实数据库查询
        PanelVendorIndexVO.PanelStatisticalData panelStatisticalData = getVendorPanelStatisticalData(vendorId);

        return PanelVendorIndexVO.builder()
                .consoleData(consoleData)
                .realTimeData(realTimeData)
                .panelStatisticalData(panelStatisticalData)
                .build();
    }

    /**
     * 获取供应商控制台数据
     */
    private PanelVendorIndexVO.ConsoleData getVendorConsoleData(Integer vendorId) {
        // 待发货订单数量
        Long awaitShip = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getVendorId, vendorId)
                .eq(Order::getOrderStatus, OrderStatusEnum.CONFIRMED.getCode())
                .eq(Order::getShippingStatus, 0)
                .eq(Order::getIsDel, 0));

        // 待结算订单数量
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        List<Integer> status = Arrays.asList(OrderStatusEnum.CONFIRMED.getCode(), OrderStatusEnum.PROCESSING.getCode(), OrderStatusEnum.COMPLETED.getCode());
        queryWrapper.in("order_status", status);
        queryWrapper.eq("is_settlement", 0);
        queryWrapper.eq("vendor_id", vendorId);
        List<Order> list = orderMapper.selectList(queryWrapper);
        long awaitSettlement = list.stream().map(Order::getOrderId).toList().size();

        // 待处理售后数量
        Long awaitAfterSale = aftersalesService.count(new LambdaQueryWrapper<Aftersales>()
                .eq(Aftersales::getVendorId, vendorId)
                .in(Aftersales::getStatus,
                        AftersalesStatusEnum.WAIT_FOR_SUPPLIER_AUDIT.getCode(),
                        AftersalesStatusEnum.WAIT_FOR_SUPPLIER_RECEIPT.getCode()));

        // 售罄SKU数量
        Long saleOutProductNum = vendorProductSkuMapper.selectCount(new LambdaQueryWrapper<VendorProductSku>()
                .eq(VendorProductSku::getVendorId, vendorId)
                .eq(VendorProductSku::getSkuStock, 0)
        );

        return PanelVendorIndexVO.ConsoleData.builder()
                .awaitShip(awaitShip)
                .awaitSettlement(awaitSettlement)
                .awaitAfterSale(awaitAfterSale)
                .saleOutProductNum(saleOutProductNum)
                .build();
    }

    /**
     * 获取供应商实时数据
     */
    @SuppressWarnings("AlibabaAvoidCommentBehindStatement")
    private PanelVendorIndexVO.RealTimeData getVendorRealTimeData(Integer vendorId) {
        // 获取今天的时间戳范围
        long todayStart = DateUtil.beginOfDay(new Date()).getTime() / 1000;
        long todayEnd = DateUtil.endOfDay(new Date()).getTime() / 1000;

        // 今日结算总额
        BigDecimal todaySettlementAmount = orderService.getTodayOrderSettlementAmountByVendor(vendorId, todayStart, todayEnd);

        // 今日结算订单数
        Long todaySettlementNum = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getVendorId, vendorId)
                .eq(Order::getIsSettlement, 1)
                .between(Order::getPayTime, todayStart, todayEnd)
                .eq(Order::getIsDel, 0));

        // 在售商品数
        Long saleProductNum = vendorProductMapper.selectCount(new LambdaQueryWrapper<VendorProduct>()
                .eq(VendorProduct::getVendorId, vendorId)
                .eq(VendorProduct::getProductState, 1));

        // 断供商品数（库存为0的商品）
        Long outageProductNum = vendorProductMapper.selectCount(new LambdaQueryWrapper<VendorProduct>()
                .eq(VendorProduct::getVendorId, vendorId)
                .eq(VendorProduct::getProductState, 0));

        // 供应商余额
        BigDecimal accountBalance = vendorMapper.selectById(vendorId).getVendorMoney();

        // 待结算金额
        BigDecimal awaitSettlementAmount = orderService.getOrderUnSettlementAmountByVendor(vendorId);

        // 这里需要根据实际的vendor_shop_bind表来查询
        Long bindShopNum = vendorShopBindMapper.selectCount(new LambdaQueryWrapper<VendorShopBind>()
                .eq(VendorShopBind::getVendorId, vendorId));

        return PanelVendorIndexVO.RealTimeData.builder()
                .todaySettlementAmount(todaySettlementAmount)
                .todaySettlementNum(todaySettlementNum)
                .saleProductNum(saleProductNum)
                .outageProductNum(outageProductNum)
                .accountBalance(accountBalance)
                .awaitSettlementAmount(awaitSettlementAmount)
                .bindShopNum(bindShopNum)
                .build();
    }

    /**
     * 获取供应商统计图表数据
     */
    private PanelVendorIndexVO.PanelStatisticalData getVendorPanelStatisticalData(Integer vendorId) {
        // 获取近30天
        List<String> horizontalAxis = new ArrayList<>();
        List<BigDecimal> longitudinalAxisIncome = new ArrayList<>();
        List<Long> longitudinalAxisOrderNum = new ArrayList<>();

        for (int i = 29; i >= 0; i--) {
            Date date = DateUtil.offsetDay(new Date(), -i);
            String dateStr = DateUtil.format(date, "yyyy-M-d");
            horizontalAxis.add(dateStr);

            long dayStart = DateUtil.beginOfDay(date).getTime() / 1000;
            long dayEnd = DateUtil.endOfDay(date).getTime() / 1000;

            // 查询当天收入
            List<Order> dayIncomeOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .eq(Order::getVendorId, vendorId)
                    .eq(Order::getPayStatus, PaymentStatus.PAID.getCode())
                    .between(Order::getPayTime, dayStart, dayEnd)
                    .eq(Order::getIsDel, 0));
            BigDecimal dayIncome = dayIncomeOrders.stream()
                    .map(Order::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            longitudinalAxisIncome.add(dayIncome);

            // 查询当天订单数
            Long dayOrderNum = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                    .eq(Order::getVendorId, vendorId)
                    .eq(Order::getPayStatus, PaymentStatus.PAID.getCode())
                    .between(Order::getPayTime, dayStart, dayEnd)
                    .eq(Order::getIsDel, 0));
            longitudinalAxisOrderNum.add(dayOrderNum);
        }

        return PanelVendorIndexVO.PanelStatisticalData.builder()
                .horizontalAxis(horizontalAxis.toArray(new String[0]))
                .longitudinalAxisIncome(longitudinalAxisIncome)
                .longitudinalAxisOrderNum(longitudinalAxisOrderNum)
                .build();
    }

    /**
     * 获取控制台数据
     *
     * @return 控制台数据
     */
    public PanelIndexVO.ConsoleData getConsoleData() {
        Integer shopId = HeaderUtils.getShopId();
        boolean shopFlag = shopId != null && shopId > 0;
        // 待付款订单
        Long awaitPayTotal = orderService.lambdaQuery()
                .eq(Order::getOrderStatus, OrderStatusEnum.PENDING.getCode())
                .eq(shopFlag, Order::getShopId, shopId)
                .count();

        // 待发货的订单
        Long awaitShip = orderService.lambdaQuery()
                .eq(Order::getOrderStatus, OrderStatusEnum.CONFIRMED.getCode())
                .eq(shopFlag, Order::getShopId, shopId)
                .count();

        // 待售后的订单
        Long awaitAfterSale = aftersalesService.lambdaQuery()
                .eq(Aftersales::getStatus, AftersalesStatusEnum.IN_REVIEW.getCode())
                .eq(shopFlag, Aftersales::getShopId, shopId)
                .count();

        // 待回复的订单留言
        Long awaitFeedback = feedbackService.lambdaQuery()
                .eq(Feedback::getStatus, 0)
                .eq(Feedback::getParentId, 0)
                .in(Feedback::getType, 5, 6)
                .eq(shopFlag, Feedback::getShopId, shopId)
                .count();

        return PanelIndexVO.ConsoleData.builder()
                .awaitPay(awaitPayTotal)
                .awaitShip(awaitShip)
                .awaitAfterSale(awaitAfterSale)
                .awaitComment(awaitFeedback)
                .build();
    }

    /**
     * 获取实时数据
     *
     * @return 实时数据
     */
    public PanelIndexVO.RealTimeData getRealTimeData() {

        Integer shopId = HeaderUtils.getShopId();
        shopId = shopId == null ? -1 : shopId;
        // 当天时间段
        String today = DateUtil.today();
        String[] startEndTime = {today, today};
        //today时间的时间戳
        long todayTimestamp = DateUtil.parse(today).getTime() / 1000;
        //今天结束时间的时间戳
        long todayEndTimestamp = DateUtil.endOfDay(DateUtil.parse(today)).getTime() / 1000;

        // 获取环比时间区间
        String[] prevDate = StringUtils.getPrevDate(startEndTime, 3);

        Long prevTimestamp = DateUtil.parse(prevDate[0]).getTime() / 1000;
        long prevEndTimestamp = DateUtil.endOfDay(DateUtil.parse(prevDate[1])).getTime() / 1000;

        // 支付金额
        Order todayOrder = orderService.getOne(
                new QueryWrapper<Order>().eq(shopId > 0, "shop_id", shopId).eq("pay_status", PaymentStatus.PAID.getCode()).between("pay_time", todayTimestamp, todayEndTimestamp)
                        .lt("pay_time", todayEndTimestamp).select("sum(total_amount) as total_amount")
        );
        BigDecimal todayOrderAmount = todayOrder != null ? todayOrder.getTotalAmount() : BigDecimal.ZERO;

        Order yesterdayOrder = orderService.getOne(
                new QueryWrapper<Order>().eq(shopId > 0, "shop_id", shopId).eq("pay_status", PaymentStatus.PAID.getCode()).between("pay_time", prevTimestamp, prevEndTimestamp)
                        .lt("pay_time", prevEndTimestamp).select("sum(total_amount) as total_amount")
        );
        BigDecimal yesterdayOrderAmount = yesterdayOrder != null ? yesterdayOrder.getTotalAmount() : BigDecimal.ZERO;

        BigDecimal orderAmountGrowthRate = TigUtils.getGrowthRate(yesterdayOrderAmount, todayOrderAmount);
//        // 访客数
        int todayVisitNum = statisticsBaseService.getVisitNum(startEndTime, 0, 0, shopId);
        int yesterdayVisitNum = statisticsBaseService.getVisitNum(prevDate, 0, 0, shopId);
        BigDecimal visitGrowthRate = TigUtils.getGrowthRate(BigDecimal.valueOf(yesterdayVisitNum), BigDecimal.valueOf(todayVisitNum));

//        // 支付买家数
        long todayBuyerNum = orderService.count(new QueryWrapper<Order>().eq(shopId > 0, "shop_id", shopId).eq("pay_status", PaymentStatus.PAID.getCode()).between("pay_time", todayTimestamp, todayEndTimestamp));
        long yesterdayBuyerNum = orderService.count(new QueryWrapper<Order>().eq(shopId > 0, "shop_id", shopId).eq("pay_status", PaymentStatus.PAID.getCode()).between("pay_time", prevTimestamp, prevEndTimestamp));
        BigDecimal buyerGrowthRate = TigUtils.getGrowthRate(BigDecimal.valueOf(yesterdayBuyerNum), BigDecimal.valueOf(todayBuyerNum));
//        // 浏览量
        int todayViewNum = statisticsBaseService.getVisitNum(startEndTime, 1, 0, shopId);
        int yesterdayViewNum = statisticsBaseService.getVisitNum(prevDate, 1, 0, shopId);
        BigDecimal viewGrowthRate = TigUtils.getGrowthRate(BigDecimal.valueOf(yesterdayViewNum), BigDecimal.valueOf(todayViewNum));
//
//        // 支付订单数
        long todayOrderNum = orderService.count(new QueryWrapper<Order>().eq(shopId > 0, "shop_id", shopId).eq("pay_status", PaymentStatus.PAID.getCode()).between("pay_time", todayTimestamp, todayEndTimestamp));
        long yesterdayOrderNum = orderService.count(new QueryWrapper<Order>().eq(shopId > 0, "shop_id", shopId).eq("pay_status", PaymentStatus.PAID.getCode()).between("pay_time", prevTimestamp, prevEndTimestamp));
        BigDecimal orderGrowthRate = TigUtils.getGrowthRate(BigDecimal.valueOf(yesterdayOrderNum), BigDecimal.valueOf(todayOrderNum));

        return PanelIndexVO.RealTimeData.builder()
                .todayOrderAmount(todayOrderAmount)
                .yesterdayOrderAmount(yesterdayOrderAmount)
                .orderAmountGrowthRate(orderAmountGrowthRate)
                .todayVisitNum(todayVisitNum)
                .yesterdayVisitNum(yesterdayVisitNum)
                .visitGrowthRate(visitGrowthRate)
                .todayBuyerNum(todayBuyerNum)
                .yesterdayBuyerNum(yesterdayBuyerNum)
                .buyerGrowthRate(buyerGrowthRate)
                .todayViewNum(todayViewNum)
                .yesterdayViewNum(yesterdayViewNum)
                .viewGrowthRate(viewGrowthRate)
                .todayOrderNum(todayOrderNum)
                .yesterdayOrderNum(yesterdayOrderNum)
                .orderGrowthRate(orderGrowthRate)
                .build();
    }

    public PanelIndexVO.PanelStatisticalData getPanelStatisticalData() {
        Integer shopId = HeaderUtils.getShopId();

        // 默认为一个月的数据
        String today = DateUtil.today();
        String monthAgo = DateUtil.offsetDay(DateUtil.parse(today), -30).toString("yyyy-MM-dd");
        String[] startEndTime = {monthAgo, today};

        // 访问统计
        List<Map<String, Object>> accessData = statisticsLogService.getVisitList(startEndTime, 1, 0, shopId);

        // 订单统计 -- 订单数量/ 订单金额
        QueryWrapper<Order> orderQueryWrapper = new QueryWrapper<>();
        orderQueryWrapper.between("pay_time", DateUtil.parse(monthAgo).getTime() / 1000, DateUtil.parse(today).getTime() / 1000)
                .eq("pay_status", PaymentStatus.PAID.getCode())
                .eq("shop_id", shopId)
                .select("DATE_FORMAT(FROM_UNIXTIME(pay_time), '%Y-%m-%d') AS period", "COUNT(*) AS orderCount", "SUM(total_amount) AS orderAmount")
                .groupBy("period");
        List<Map<String, Object>> orderData = orderService.listMaps(orderQueryWrapper);

        // 横轴
        String[] horizontalAxis = TigUtils.getRangeDateX(0, monthAgo, today);

        // 访问统计 -- 纵轴
        List<BigDecimal> longitudinalAxisAccess = overviewService.getLongitudinalAxis(Arrays.stream(horizontalAxis).toList(), accessData.stream().map(object -> JSON.parseObject(JSON.toJSONString(object))).toList(), 0, 2);

        // 订单统计 -- 订单数量
        List<BigDecimal> longitudinalAxisOrderNum = overviewService.getLongitudinalAxis(Arrays.stream(horizontalAxis).toList(), orderData.stream().map(object -> JSON.parseObject(JSON.toJSONString(object))).toList(), 0, 3);

        // 订单金额
        List<BigDecimal> longitudinalAxisOrderAmount = overviewService.getLongitudinalAxis(Arrays.stream(horizontalAxis).toList(), orderData.stream().map(object -> JSON.parseObject(JSON.toJSONString(object))).toList(), 0, 7);

        return PanelIndexVO.PanelStatisticalData.builder()
                .horizontalAxis(horizontalAxis)
                .longitudinalAxisAccess(longitudinalAxisAccess)
                .longitudinalAxisOrderNum(longitudinalAxisOrderNum)
                .longitudinalAxisOrderAmount(longitudinalAxisOrderAmount)
                .build();
    }

}
