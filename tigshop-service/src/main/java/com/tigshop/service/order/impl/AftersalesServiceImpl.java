// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.order.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.dto.order.*;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.finance.AccountType;
import com.tigshop.bean.enums.finance.EntryType;
import com.tigshop.bean.enums.finance.StatementType;
import com.tigshop.bean.enums.order.*;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.finance.RecordRate;
import com.tigshop.bean.model.finance.RefundApply;
import com.tigshop.bean.model.finance.StatementOutput;
import com.tigshop.bean.model.order.*;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.param.finance.statement.StatementOutputSaveParam;
import com.tigshop.bean.param.finance.statement.StatementSaveParam;
import com.tigshop.bean.param.order.AftersalesApplyParam;
import com.tigshop.bean.param.order.AftersalesEditParam;
import com.tigshop.bean.query.order.AftersalesListPageQuery;
import com.tigshop.bean.vo.order.AfterSalesClientDetailVO;
import com.tigshop.bean.vo.order.AftersalesItemVO;
import com.tigshop.bean.vo.order.AftersalesLogVO;
import com.tigshop.bean.vo.order.AftersalesVO;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.*;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.finance.PaylogMapper;
import com.tigshop.mapper.finance.RefundApplyMapper;
import com.tigshop.mapper.order.AftersalesLogMapper;
import com.tigshop.mapper.order.AftersalesMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.RecordRateService;
import com.tigshop.service.finance.StatementOutputService;
import com.tigshop.service.finance.StatementService;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.order.AftersalesItemService;
import com.tigshop.service.order.AftersalesLogService;
import com.tigshop.service.order.AftersalesService;
import com.tigshop.service.order.OrderItemService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.order.AftersalesStatusEnum.*;
import static com.tigshop.common.constant.Constants.DATE_FORMAT;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@RequiredArgsConstructor
@Service
public class AftersalesServiceImpl extends BaseServiceImpl<AftersalesMapper, Aftersales> implements AftersalesService {

    private final RefundApplyMapper refundApplyMapper;

    private final AdminLogService adminLogService;

    private final AftersalesLogService aftersalesLogService;

    private final OrderMapper orderMapper;

    private final AftersalesItemService aftersalesItemService;

    private final OrderItemMapper orderItemMapper;

    private final UserMapper userMapper;

    private final AftersalesLogMapper aftersalesLogMapper;

    private final AdminMsgService adminMsgService;

    private final TranslatePackageImpl translatePackage;

    private final ConfigService configService;

    private final RabbitTemplate rabbitTemplate;

    private final OrderItemService orderItemService;

    private final TigshopProperties tigshopProperties;
    private final StatementOutputService statementOutputService;
    private final PaylogMapper paylogMapper;
    private final RecordRateService recordRateService;
    private final StatementService statementService;

    @Override
    public Page<AftersalesVO> list(AftersalesListPageQuery pageQuery) {
        // 分页
        Page<Aftersales> page = buildSortOrder(pageQuery);

        Integer aftersaleType = pageQuery.getAftersaleType();
        boolean aftersalesTypeFlag = aftersaleType != null && (
                aftersaleType.equals(AftersalesTypeEnum.RETURN.getCode()) || aftersaleType.equals(AftersalesTypeEnum.PAYRETURN.getCode())
        );

        Integer vendorId = this.getVendorId();
        boolean vendorFlag = vendorId != null && vendorId > 0;

        boolean statusFlag = pageQuery.getStatus() != null && pageQuery.getStatus() > 0;

        Integer shopId = HeaderUtils.getShopId();
        boolean shopFlag = shopId != null && shopId > 0;

        List<Integer> searchOrderIds = new ArrayList<>();
        String keyword = pageQuery.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            List<Order> orderList = orderMapper.selectList(new LambdaQueryWrapper<Order>().like(Order::getOrderSn, keyword));
            searchOrderIds = orderList.stream()
                    .map(Order::getOrderId)
                    .toList();
        }
        Integer status = pageQuery.getStatus();
        List<Integer> vendorStatuses = Arrays.asList(21, 22, 6);
        boolean isVendorAdmin = false;
        if (getAdminType() != null) {
            isVendorAdmin = AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.VENDOR;
        }
        Page<Aftersales> aftersalesPage = this.lambdaQuery()
                .eq(aftersalesTypeFlag, Aftersales::getAftersaleType, aftersaleType)
                .eq(shopFlag, Aftersales::getShopId, shopId)
                .eq(ObjectUtil.isNotEmpty(pageQuery.getUserId()), Aftersales::getUserId, pageQuery.getUserId())
                .eq(vendorFlag, Aftersales::getVendorId, vendorId)
                .in(CollUtil.isNotEmpty(searchOrderIds), Aftersales::getOrderId, searchOrderIds)
                // 普通后台按 status 查询
                .eq(!isVendorAdmin && statusFlag, Aftersales::getStatus, status)
                // 供应商后台没传 status => 默认 21,22
                .in(isVendorAdmin && status == null, Aftersales::getStatus, vendorStatuses)
                // 供应商后台传了 status => 必须在 21,22 内
                .eq(isVendorAdmin && statusFlag && vendorStatuses.contains(status), Aftersales::getStatus, status)
                // 供应商后台传了非法 status => 查不到数据
                .eq(isVendorAdmin && statusFlag && !vendorStatuses.contains(status), Aftersales::getStatus, -999)
                .page(page);
        List<Aftersales> aftersalesPageRecords = aftersalesPage.getRecords();

        //循环records获得售后ID
        List<Integer> aftersaleIds = aftersalesPageRecords.stream()
                .map(Aftersales::getAftersaleId)
                .toList();
        //通过Ids查询aftersalesItem
        List<AftersalesItemVO> itemList = aftersalesItemService.getItemByIds(aftersaleIds);
        Map<Integer, List<AftersalesItemVO>> itemMap = itemList.stream().collect(Collectors.groupingBy(AftersalesItemVO::getAftersaleId));
        //获取订单id
        List<Integer> orderIds = aftersalesPageRecords.stream()
                .map(Aftersales::getOrderId)
                .toList();
        Map<Integer, Order> orderMap;
        if (!orderIds.isEmpty()) {
            List<Order> orderList = orderMapper.selectBatchIds(orderIds);
            orderMap = orderList.stream().collect(Collectors.toMap(Order::getOrderId, Function.identity()));
        } else {
            orderMap = new HashMap<>();
        }

        // 转换为VO
        List<AftersalesVO> aftersalesVOList = aftersalesPageRecords.stream()
                .map(aftersales -> {
                    AftersalesVO aftersalesVO = new AftersalesVO();
                    BeanUtils.copyProperties(aftersales, aftersalesVO);

                    aftersalesVO.setAftersalesTypeName(AftersalesTypeEnum.getTypeName(aftersales.getAftersaleType()));
                    aftersalesVO.setStatusName(AftersalesStatusEnum.getStatusName(aftersales.getStatus()));

                    if (aftersales.getAddTime() > 0) {
                        DateTime date = DateUtil.date(aftersales.getAddTime() * 1000);
                        aftersalesVO.setAddTime(DateUtil.format(date, DATE_FORMAT));
                    } else {
                        aftersalesVO.setAddTime("");
                    }

                    if (orderMap.get(aftersales.getOrderId()) != null) {
                        aftersalesVO.setOrderSn(orderMap.get(aftersales.getOrderId()).getOrderSn());
                    } else {
                        aftersalesVO.setOrderSn("");
                    }

                    aftersalesVO.setAftersalesItems(itemMap.get(aftersales.getAftersaleId()));

                    return aftersalesVO;
                }).toList();
        return PageUtil.convertPage(aftersalesPage, aftersalesVOList);
    }

    @Override
    public AftersalesVO detail(Integer id) {
        Aftersales aftersales = this.getById(id);
        AftersalesVO aftersalesVO = new AftersalesVO();
        BeanUtils.copyProperties(aftersales, aftersalesVO);
        aftersalesVO.setAftersalesTypeName(AftersalesTypeEnum.getTypeName(aftersales.getAftersaleType()));
        aftersalesVO.setStatusName(AftersalesStatusEnum.getStatusName(aftersales.getStatus()));

        DateTime date = DateUtil.date(aftersales.getAddTime() * 1000);
        aftersalesVO.setAddTime(DateUtil.format(date, DATE_FORMAT));

        List<Integer> aftersaleIds = Collections.singletonList(aftersales.getAftersaleId());
        List<AftersalesItemVO> itemList = aftersalesItemService.getItemByIds(aftersaleIds);
        Map<Integer, List<AftersalesItemVO>> itemMap = itemList.stream().collect(Collectors.groupingBy(AftersalesItemVO::getAftersaleId));
        aftersalesVO.setAftersalesItems(itemMap.get(aftersales.getAftersaleId()));
//            aftersalesVO.setAftersalesLog(aftersalesLogService.list(new QueryWrapper<AftersalesLog>().eq("aftersale_id", aftersales.getAftersaleId())).stream().map(aftersalesLog -> aftersalesLog));

        BigDecimal suggestRefundAmount = itemList.stream().map(item -> item.getPrice().multiply(new BigDecimal(item.getNumber()))).reduce(BigDecimal.ZERO, BigDecimal::add);
        aftersalesVO.setSuggestRefundAmount(suggestRefundAmount);

        List<AftersalesLog> aftersalesLogList = aftersalesLogService.lambdaQuery()
                .eq(AftersalesLog::getAftersaleId, aftersales.getAftersaleId())
                .list();
        if(CollUtil.isNotEmpty(aftersalesLogList)){
            List<AftersalesLogVO> aftersalesLogVOList = aftersalesLogList.stream()
                    .map(aftersalesLog -> {
                        AftersalesLogVO aftersalesLogVO = new AftersalesLogVO();
                        BeanUtils.copyProperties(aftersalesLog, aftersalesLogVO);
                        aftersalesLogVO.setAddTime(TigUtils.handelTime(Long.parseLong(aftersalesLog.getAddTime())));
                        aftersalesLogVO.setReturnPic(JsonUtil.jsonToList(aftersalesLog.getReturnPic(), ReturnPicDTO.class));
                        aftersalesLogVO.setShopId(aftersalesLog.getShopId());
                        aftersalesLogVO.setVendorId(aftersalesLog.getVendorId());
                        return aftersalesLogVO;
                    }).toList();
            aftersalesVO.setAftersalesLog(aftersalesLogVOList);
        }

        return aftersalesVO;
    }

    @Override
    public boolean create(AftersalesApplyParam createDTO) {
        if (createDTO != null) {
            Aftersales aftersales = new Aftersales();
            BeanUtils.copyProperties(createDTO, aftersales);
            return this.save(aftersales);
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(AftersalesEditParam param) {
        // 1. 查询售后
        Aftersales aftersales = this.getById(param.getId());
        Assert.notNull(aftersales, () -> new GlobalException("售后信息不存在"));
        Assert.isTrue(IN_REVIEW.getCode() == aftersales.getStatus() || WAIT_FOR_SUPPLIER_AUDIT.getCode() == aftersales.getStatus(), () -> new GlobalException("该售后状态不能操作"));

        // 2. 校验参数
        param.validParam(aftersales);

        int itemFlag;
        // 审核通过
        if (param.getStatus() == APPROVED_FOR_PROCESSING.getCode()) {
            // 售后商品数量校验
            itemFlag = checkAfterSaleProduct(aftersales.getAftersaleId(), aftersales.getOrderId());

            // 3. 更新售后
            Aftersales updateAftersales = param.createUpdateAftersales(aftersales);
            this.updateById(updateAftersales);

            // 全退 => 订单状态已完成
            if (itemFlag == 1) {
                Order order = orderMapper.selectById(aftersales.getOrderId());
                if (order != null) {
                    order.setOrderStatus(OrderStatusEnum.COMPLETED.getCode());
                    order.setShippingStatus(ShippingStatusEnum.SHIPPED.getCode());
                    orderMapper.updateById(order);
                }
            }

            // 如果该售后订单为仅退款
            if (aftersales.getAftersaleType().equals(AftersalesTypeEnum.PAYRETURN.getCode())) {
                // 创建退款申请
                RefundApply applyData = new RefundApply();
                applyData.setRefundType(aftersales.getOrderId() != null ? 1 : 2);
                applyData.setOrderId(aftersales.getOrderId());
                applyData.setUserId(aftersales.getUserId());
                applyData.setAftersaleId(param.getId());
                applyData.setShopId(aftersales.getShopId());
                applyData.setAddTime(StringUtils.getCurrentTime());

                refundApplyMapper.insert(applyData);

                // 下单出账，添加流水
                if (aftersales.getRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
                    saveAftersalesStatementOutput(aftersales);
                    // 对账单
                    Integer realSettlement = statementOutputService.lambdaQuery()
                            .eq(StatementOutput::getRecordId, aftersales.getOrderId())
                            .eq(StatementOutput::getRecordType, StatementType.ORDER.getCode())
                            .last("LIMIT 1")
                            .one().getRealSettlement();
                    if (realSettlement > 0) {
                        this.generateStatement(aftersales);
                    }

                    // 如果为已结算订单，退款时直接扣减账户余额
                    TransactionAfterCommitExecutor.execute(() -> {
                        rabbitTemplate.convertAndSend(
                                RabbitMQConfig.DIRECT_EXCHANGE,
                                RabbitMQConfig.REFUND_SETTLEMENT_ROUTING_KEY,
                                aftersales.getAftersaleId());
                    });
                }
            }
        }

        // 审核未通过
        if (param.getStatus() == REFUSE.getCode()) {
            aftersales.setStatus(param.getStatus());
            aftersales.setReply(param.getReply());
            this.updateById(aftersales);
        }

        adminLogService.createByLogInfo("更新退换货:" + param.getId());
        aftersalesLogService.addAftersaleLog(param.getId(), AftersalesStatusEnum.getStatusName(param.getStatus()) + ":" + param.getReply());
    }

    /**
     * 售后商品数量校验
     *
     * @param aftersalesId 售后订单id
     * @param orderId 订单id
     * @return int
     */
    public int checkAfterSaleProduct(int aftersalesId, int orderId) {
        int itemFlag = 0;

        // Step 1：获取售后商品列表（按 order_item_id 分组，合计数量）
        List<AftersalesItem> aftersalesItemList = aftersalesItemService.lambdaQuery()
                .eq(AftersalesItem::getAftersaleId, aftersalesId)
                .list();

        Map<Integer, Integer> aftersalesItems = aftersalesItemList.stream()
                .collect(Collectors.groupingBy(
                        AftersalesItem::getOrderItemId,
                        Collectors.summingInt(AftersalesItem::getNumber)
                ));

        // Step 2：获取订单商品列表
        List<OrderItem> orderItemList = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, orderId)
                .list();

        Map<Integer, Integer> orderItems = orderItemList.stream()
                .collect(Collectors.toMap(OrderItem::getItemId, OrderItem::getQuantity));

        // Step 3：对比是否完全退
        if (aftersalesItems.equals(orderItems)) {
            itemFlag = 1;
        } else {
            List<Integer> itemFlags = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : orderItems.entrySet()) {
                Integer itemId = entry.getKey();
                Integer quantity = entry.getValue();

                // 查询该 itemId 下所有有效状态的售后申请总和
                int validSum = aftersalesItemService.lambdaQuery()
                        .inSql(AftersalesItem::getAftersaleId,
                                "SELECT aftersale_id FROM aftersales WHERE status IN (" +
                                        AftersalesStatusEnum.validStatus().stream().map(String::valueOf).collect(Collectors.joining(",")) +
                                        ")")
                        .eq(AftersalesItem::getOrderItemId, itemId)
                        .list()
                        .stream()
                        .mapToInt(AftersalesItem::getNumber)
                        .sum();

                if (aftersalesItems.containsKey(itemId) && quantity.equals(validSum)) {
                    itemFlags.add(1);
                } else {
                    itemFlags.add(0);
                }
            }

            if (!itemFlags.contains(0)) {
                itemFlag = 1;
            }
        }

        return itemFlag;
    }


    @Transactional
    @Override
    public boolean receive(Integer id) {
        Aftersales aftersales = this.getById(id);
        if (aftersales == null) {
            throw new GlobalException("售后信息不存在");
        }
        Integer isVendor = tigshopProperties.getIsVendor();
        // 如果是供应商版本，那么只能处理供应商待收货
        if (isVendor == 1 && (aftersales.getVendorId() != null && aftersales.getVendorId() > 0)){
            if (!aftersales.getStatus().equals(WAIT_FOR_SUPPLIER_RECEIPT.getCode())) {
                throw new GlobalException("该售后状态不能操作");
            }
        } else { // 如果是其他版本，那么只能处理商家待收货
            if (!aftersales.getStatus().equals(AftersalesStatusEnum.RETURNED.getCode())) {
                throw new GlobalException("该售后状态不能操作");
            }
        }

        Aftersales updateAftersales = new Aftersales();
        updateAftersales.setAftersaleId(id);
        updateAftersales.setStatus(AftersalesStatusEnum.COMPLETE.getCode());

        try {
            // 记录售后处理完成时间
            updateAftersales.setFinalTime(StringUtils.getCurrentTime());

            // 更新售后状态
            boolean result = this.updateById(updateAftersales);

            // 下单出账，添加流水
            if (aftersales.getRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
                saveAftersalesStatementOutput(aftersales);
                // 如果为已结算订单，退款时直接扣减账户余额
                TransactionAfterCommitExecutor.execute(() -> {
                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.DIRECT_EXCHANGE,
                            RabbitMQConfig.REFUND_SETTLEMENT_ROUTING_KEY,
                            aftersales.getAftersaleId());
                });
            }

            // 创建退款申请
            RefundApply applyData = new RefundApply();
            applyData.setRefundType(aftersales.getOrderId() != null ? 1 : 2);
            applyData.setOrderId(aftersales.getOrderId());
            applyData.setUserId(aftersales.getUserId());
            applyData.setAftersaleId(id);
            applyData.setShopId(aftersales.getShopId());
            applyData.setRefundNote("售后申请通过");
            applyData.setAddTime(StringUtils.getCurrentTime());

            if (refundApplyMapper.insert(applyData) <= 0) {
                throw new Exception("创建退款申请失败");
            }

            // 对账单
            Integer realSettlement = statementOutputService.lambdaQuery()
                    .eq(StatementOutput::getRecordId, aftersales.getOrderId())
                    .eq(StatementOutput::getRecordType, StatementType.ORDER.getCode())
                    .last("LIMIT 1")
                    .one().getRealSettlement();
            if (realSettlement > 0) {
                this.generateStatement(aftersales);
            }
            adminLogService.createByLogInfo("完成退换货:" + id);
            if (result) {
                aftersalesLogService.addAftersaleLog(id, AftersalesStatusEnum.getStatusName(AftersalesStatusEnum.COMPLETE.getCode()) + ": 售后申请通过");
            }

            return result;
        } catch (Exception exception) {
            throw new GlobalException(exception.getMessage());
        }
    }

    /**
     * 保存退款的出账
     *
     * @param aftersales 退款记录
     */
    private void saveAftersalesStatementOutput(Aftersales aftersales) {
        List<StatementOutputSaveParam> statementOutputSaveParamList = new ArrayList<>();
        Integer aftersaleId = aftersales.getAftersaleId();
        RecordRate recordRate = recordRateService.lambdaQuery()
                .eq(RecordRate::getRecordId, aftersales.getOrderId())
                .eq(RecordRate::getRecordType, StatementType.ORDER.getCode())
                .orderByDesc(RecordRate::getRecordRateId)
                .last("limit 1")
                .one();
        // 获取订单的状态
        Integer settlementStatus = statementOutputService.lambdaQuery()
                .eq(StatementOutput::getRecordId, aftersales.getOrderId())
                .eq(StatementOutput::getRecordType, StatementType.ORDER.getCode())
                .last("limit 1")
                .one()
                .getRealSettlement();
        settlementStatus = settlementStatus == null ? Constants.NO : settlementStatus;

        if (aftersales.getVendorId() != null && aftersales.getVendorId() > 0) {
            BigDecimal totalAmount = this.baseMapper.selectTotalAmount(aftersaleId, COMPLETE.getCode());
            totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
            StatementOutputSaveParam vendorStatementOutputSaveParam =
                    new StatementOutputSaveParam(aftersales, StatementType.ORDER_REFUND.getCode(), totalAmount, false, false);
            // 判断订单是否已结算，如果结算则将真实的结算状态改为已结算
            vendorStatementOutputSaveParam.setRealSettlement(settlementStatus);
            statementOutputSaveParamList.add(vendorStatementOutputSaveParam);

            BigDecimal serviceFee = totalAmount.multiply(recordRate.getSupplierServiceFee())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            StatementOutputSaveParam vendorServiceStatementOutputSaveParam =
                    new StatementOutputSaveParam(aftersales, StatementType.SERVICE_FEE.getCode(), serviceFee, false, true);
            vendorServiceStatementOutputSaveParam.setRealSettlement(settlementStatus);
            statementOutputSaveParamList.add(vendorServiceStatementOutputSaveParam);

            if (aftersales.getShopId() > 0) {
                BigDecimal shopAmount = aftersales.getRefundAmount().subtract(totalAmount);
                StatementOutputSaveParam shopStatementOutputSaveParam =
                        new StatementOutputSaveParam(aftersales, StatementType.ORDER_REFUND.getCode(), shopAmount, true, false);
                shopStatementOutputSaveParam.setRealSettlement(settlementStatus);
                statementOutputSaveParamList.add(shopStatementOutputSaveParam);

                BigDecimal shopServiceFee = shopAmount.multiply(recordRate.getShopServiceFee())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                StatementOutputSaveParam shopServiceStatementOutputSaveParam =
                        new StatementOutputSaveParam(aftersales, StatementType.SERVICE_FEE.getCode(), shopServiceFee, true, true);
                shopServiceStatementOutputSaveParam.setRealSettlement(settlementStatus);
                statementOutputSaveParamList.add(shopServiceStatementOutputSaveParam);
            }
        } else {
            if (aftersales.getShopId() > 0) {
                BigDecimal refundAmount = aftersales.getRefundAmount();
                StatementOutputSaveParam shopStatementOutputSaveParam =
                        new StatementOutputSaveParam(aftersales, StatementType.ORDER_REFUND.getCode(), refundAmount, true, false);
                shopStatementOutputSaveParam.setRealSettlement(settlementStatus);
                statementOutputSaveParamList.add(shopStatementOutputSaveParam);

                BigDecimal shopServiceFee = refundAmount.multiply(recordRate.getShopServiceFee())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                StatementOutputSaveParam shopServiceStatementOutputSaveParam =
                        new StatementOutputSaveParam(aftersales, StatementType.SERVICE_FEE.getCode(), shopServiceFee, true, true);
                shopServiceStatementOutputSaveParam.setRealSettlement(settlementStatus);
                statementOutputSaveParamList.add(shopServiceStatementOutputSaveParam);
            }
        }
        statementOutputService.batchSaveStatementOutput(statementOutputSaveParamList);
    }

    // 生成对账单
    private void generateStatement(Aftersales aftersales) {
        StatementSaveParam statementSaveParam = new StatementSaveParam();
        statementSaveParam.setAccountType(AccountType.ACCOUNT_BALANCE.getCode());
        statementSaveParam.setType(StatementType.ORDER_REFUND.getCode());
        statementSaveParam.setEntryType(EntryType.AUTO.getCode());

        // 可以改成订单各种支付类型的值做判断
        Paylog paylog = paylogMapper.selectOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getOrderId, aftersales.getOrderId()));
        if (paylog != null){
            statementSaveParam.setPaymentType(paylog.getPayCode());
        } else {
            statementSaveParam.setPaymentType(PayMethodType.OTHER.getCode());
        }

        statementSaveParam.setShopId(aftersales.getShopId());
        statementSaveParam.setVendorId(aftersales.getVendorId());
        statementSaveParam.setRecordId(aftersales.getAftersaleId());

        statementService.saveStatement(statementSaveParam);
    }

    @Override
    public boolean submitFeedbackRecord(Integer aftersaleId, AftersalesRecordCreateDTO aftersalesRecordCreateDTO, Integer currentAdminId) {
        Aftersales aftersales = this.getById(aftersaleId);
        if (aftersales == null) {
            throw new GlobalException("售后信息不存在");
        }
        AftersalesLog aftersalesLog = new AftersalesLog();
        aftersalesLog.setAftersaleId(aftersaleId);
        aftersalesLog.setLogInfo(aftersalesRecordCreateDTO.getLogInfo());
        aftersalesLog.setAdminName(currentAdminId != null ? SecurityUtils.getCurrentUsername() : "");
        aftersalesLog.setAddTime(StringUtils.getCurrentTime().toString());
        aftersalesLog.setUserName(SecurityUtils.getCurrentUsername());
        aftersalesLog.setReturnPic(JsonUtil.toJson(aftersalesRecordCreateDTO.getReturnPic()));

        if (aftersales.getStatus() == AftersalesStatusEnum.SEND_BACK.getCode() && (currentAdminId != null && currentAdminId != 0)) {
            Aftersales updateAftersales = new Aftersales();
            updateAftersales.setAftersaleId(aftersaleId);
            updateAftersales.setTrackingNo(aftersalesRecordCreateDTO.getTrackingNo());
            updateAftersales.setLogisticsName(aftersalesRecordCreateDTO.getLogisticsName());
            updateAftersales.setDealTime(StringUtils.getCurrentTime());
            this.save(updateAftersales);
        }

        return aftersalesLogService.save(aftersalesLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void afterSalesApply(AftersalesApplyParam param) {
        if (ObjectUtil.isEmpty(param.getItems()) || param.getItems().stream().mapToInt(AftersalesApplyParam.ItemsDTO::getNumber).sum() == 0) {
            throw new GlobalException("请填写申请售后数量");
        }

        // 移除 param.getItems() 中数量为0的项
        param.getItems().removeIf(item -> item.getNumber() == 0);

        // 1. 查询订单
        Order order = orderMapper.selectById(param.getOrderId());
        Assert.notNull(order, () -> new GlobalException(translatePackage.translate("订单不存在")));
        Assert.isTrue(order.getPayStatus() == PaymentStatus.PAID.getCode(), () -> new GlobalException(translatePackage.translate("未支付订单不允许售后")));

        // 买家确认收货 afterSalesLimitDays 天后，不支持买家申请售后
        if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus()) {
            String afterSalesLimitDays = configService.getConfigVal(SettingsEnum.AFTER_SALES_LIMIT_DAYS);
            if (StrUtil.isNotBlank(afterSalesLimitDays)) {
                long receivedTime = order.getReceivedTime() * 1000;
                BigDecimal afterSalesLimitDaysTime = new BigDecimal(afterSalesLimitDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
                LocalDateTime limitDateTime = Instant.ofEpochMilli(receivedTime + afterSalesLimitDaysTime.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                boolean toAftersalses = limitDateTime.isAfter(LocalDateTime.now());
                if (!toAftersalses) {
                    throw new GlobalException("买家确认收货" + afterSalesLimitDays + "天后，不支持买家申请售后");
                }
            }
        }

        // 2. 校验商品可申请数量不足
        validateOrderQuantity(param.getItems(), param.getOrderId(), true, null);

        // 3. 创建售后
        Aftersales aftersales = param.createAfterSales(order.getOrderId(), order.getShopId());
        // 如果该订单有绑定供应商
        if (order.getVendorId() != null && order.getVendorId() > 0) {
            aftersales.setVendorId(order.getVendorId());
        }
        this.save(aftersales);

        // 4. 创建售后项
        List<AftersalesItem> afterSalesItems = param.createAfterSalesItems(aftersales.getAftersaleId());
        aftersalesItemService.saveBatch(afterSalesItems);

        // 5. 创建日志
        AftersalesLog aftersalesLog = param.createAftersalesLog(aftersales.getAftersaleId());
        aftersalesLogMapper.insert(aftersalesLog);

        // 6. 发送后台消息 -- 售后申请
        // 6.1. 获取订单商品
        Integer orderItemId = param.getItems().getFirst().getOrderItemId();
        OrderItem orderItem = orderItemMapper.selectById(orderItemId);
        // 6.2. 创建管理员消息
        AdminMsgCreateDTO adminMsgCreateDTO = param.createAdminMsgCreateDTO(order, orderItem, aftersales);
        adminMsgService.createMessage(adminMsgCreateDTO);

        // 7. 发送自动同意退货申请
        String autoReturnGoods = configService.getConfigVal(SettingsEnum.AUTO_RETURN_GOODS);
        if (StrUtil.isNotBlank(autoReturnGoods) && "1".equals(autoReturnGoods)) {
            // 7.1. 计算延迟时间
            String autoReturnGoodsDays = configService.getConfigVal(SettingsEnum.AUTO_RETURN_GOODS_DAYS);
            // long delayTime = Long.parseLong(autoReturnGoodsDays) * 24 * 60 * 60 * 1000;
            BigDecimal delayTime = new BigDecimal(autoReturnGoodsDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DELAY_EXCHANGE,
                    RabbitMQConfig.AUTO_RETURN_GOODS_QUEUE_ROUTING_KEY,
                    aftersales.getAftersaleId(),
                    message -> {
                        message.getMessageProperties().setHeader("x-delay", delayTime.longValue());
                        return message;
                    }
            );
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void afterSalesApplyUpdate(ClientAfterSalesUpdateDTO updateDTO) {
        Aftersales aftersales = this.getById(updateDTO.getAftersaleId());
        Assert.notNull(aftersales, () -> new GlobalException(translatePackage.translate("售后信息不存在")));
        Assert.isFalse(aftersales.getStatus() != IN_REVIEW.getCode(), () -> new GlobalException(translatePackage.translate("该状态下不能修改")));
        Assert.isFalse(updateDTO.getItems().isEmpty(), () -> new GlobalException(translatePackage.translate("请填写申请售后数量")));

        validateOrderQuantity(updateDTO.getItems(), updateDTO.getOrderId(), false, aftersales.getAftersaleId());

        BeanUtils.copyProperties(updateDTO, aftersales);
        if (updateDTO.getPics() != null) {
            aftersales.setPics(JsonUtil.toJson(updateDTO.getPics()));
        }
        this.updateById(aftersales);

        // 删除
        aftersalesItemService.lambdaUpdate().eq(AftersalesItem::getAftersaleId, aftersales.getAftersaleId()).remove();

        for (AftersalesApplyParam.ItemsDTO item : updateDTO.getItems()) {
            AftersalesItem aftersalesItem = new AftersalesItem();
            aftersalesItem.setAftersaleId(aftersales.getAftersaleId());
            aftersalesItem.setOrderItemId(item.getOrderItemId());
            aftersalesItem.setNumber(item.getNumber());
            aftersalesItemService.save(aftersalesItem);
        }

        //记录日志
        User user = userMapper.selectById(updateDTO.getUserId());
        AftersalesLog aftersalesLog = new AftersalesLog();
        aftersalesLog.setAftersaleId(aftersales.getAftersaleId());
        aftersalesLog.setLogInfo("会员发起了" + AftersalesTypeEnum.getTypeName(updateDTO.getAftersaleType()) +
                ": " + updateDTO.getDescription());
        aftersalesLog.setUserName(user.getUsername());
        aftersalesLog.setAddTime(StringUtils.getCurrentTime().toString());
        if (!updateDTO.getItems().isEmpty()) {
            aftersalesLog.setReturnPic(JsonUtil.toJson(updateDTO.getPics()));
        } else {
            aftersalesLog.setReturnPic("");
        }
        aftersalesLogMapper.insert(aftersalesLog);
    }

    @Override
    public AfterSalesClientDetailVO clientDetail(Integer id) {
        if (id != null) {
            Aftersales aftersales = this.getById(id);
            AfterSalesClientDetailVO afterSalesVO = new AfterSalesClientDetailVO();
            BeanUtils.copyProperties(aftersales, afterSalesVO);
            afterSalesVO.setAftersalesTypeName(AftersalesTypeEnum.getTypeName(aftersales.getAftersaleType()));
            afterSalesVO.setStatusName(AftersalesStatusEnum.getStatusName(aftersales.getStatus()));
            afterSalesVO.setAddTime(TigUtils.handelTime(aftersales.getAddTime()));

            afterSalesVO.setAftersalesItems(aftersalesItemService.getItemByIds(Collections.singletonList(id)));
            //查询afterSalesLog
            LambdaQueryWrapper<AftersalesLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AftersalesLog::getAftersaleId, id);
            //List<AftersalesLog> afterSalesLogs = aftersalesLogMapper.selectList(queryWrapper);
            List<AftersalesLogVO> afterSalesLogVOList = aftersalesLogService.getDetailLog(id);
            afterSalesVO.setAftersalesLog(afterSalesLogVOList);
            afterSalesVO.setCanCancel(aftersales.getStatus() == IN_REVIEW.getCode());
            afterSalesVO.setStepStatus(getStep(aftersales));
            afterSalesVO.setReturnGoodsTip(aftersales.getReturnGoodsTip());
            afterSalesVO.setPics(JSON.parseArray(aftersales.getPics(), ReturnPicDTO.class));
            afterSalesVO.setRefundAmount(aftersales.getRefundAmount().toString());
            return afterSalesVO;

        }
        return null;
    }

    @Override
    public boolean submitFeedbackRecord(AfterSalesFeedbackDTO dto) {

        if (dto != null) {
            Aftersales aftersales = this.getById(dto.getId());
            if (aftersales == null) {
                throw new GlobalException(translatePackage.translate("该售后申请不存在"));
            }
            if (aftersales.getStatus() == SEND_BACK.getCode() && (dto.getUserId() != null && dto.getUserId() > 0)) {
                //要求寄回
                aftersales.setTrackingNo(dto.getTrackingNo());
                aftersales.setLogisticsName(dto.getLogisticsName());
                aftersales.setDealTime(StringUtils.getCurrentTime());
                Integer isVendor = tigshopProperties.getIsVendor();
                // 如果是供应商版本，那么只能处理供应商待收货
                if (isVendor == 1 && (aftersales.getVendorId() != null && aftersales.getVendorId() > 0)){
                    aftersales.setStatus(WAIT_FOR_SUPPLIER_RECEIPT.getCode());
                }else {
                    aftersales.setStatus(RETURNED.getCode());
                }
                this.updateById(aftersales);
            }
            AftersalesLog aftersalesLog = new AftersalesLog();
            aftersalesLog.setAftersaleId(dto.getId());
            aftersalesLog.setLogInfo(dto.getLogInfo());
            User user = userMapper.selectById(dto.getUserId());
            aftersalesLog.setUserName(user != null ? user.getUsername() : "");
            aftersalesLog.setAddTime(StringUtils.getCurrentTime().toString());
            aftersalesLog.setAdminName("");
            if (dto.getReturnPic() != null && !dto.getReturnPic().isEmpty()) {
                aftersalesLog.setReturnPic(JsonUtil.toJson(dto.getReturnPic()));
            } else {
                aftersalesLog.setReturnPic("[]");
            }
            return aftersalesLogService.save(aftersalesLog);

        }
        return false;
    }

    @Override
    public boolean cancel(AfterSalesCancelDTO dto) {
        Aftersales aftersales = this.getById(dto.getAftersaleId());
        if (aftersales == null) {
            throw new GlobalException(translatePackage.translate("记录不存在"));
        }
        if (aftersales.getStatus() != IN_REVIEW.getCode()) {
            throw new GlobalException(translatePackage.translate("该状态下不能取消申请"));
        }
        aftersales.setStatus(CANCEL.getCode());
        boolean res = this.updateById(aftersales);
        if (res) {
            AftersalesLog aftersalesLog = new AftersalesLog();
            aftersalesLog.setLogInfo(CANCEL.getDescription());
            aftersalesLog.setAddTime(StringUtils.getCurrentTime().toString());
            aftersalesLog.setAftersaleId(dto.getAftersaleId());
            aftersalesLog.setReturnPic("");
            User user = userMapper.selectById(dto.getUserId());
            aftersalesLog.setUserName(user != null ? user.getUsername() : "");
            return aftersalesLogService.save(aftersalesLog);
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(AfterSalesCompleteDTO completeDTO) {
        Integer afterSaleId = completeDTO.getId();
        Aftersales aftersales = this.getById(afterSaleId);
        if (aftersales == null) {
            throw new GlobalException("售后信息不存在");
        }
        if (!aftersales.getStatus().equals(REFUSE.getCode())) {
            throw new GlobalException("该售后状态不能操作");
        }
        //售后单状态改为关闭
        aftersales.setStatus(CANCEL.getCode());
        updateById(aftersales);
        //添加售后记录
        aftersalesLogService.addAftersaleLog(afterSaleId, "售后驳回，关闭售后单");
    }

    //获取流程步骤
    private AfterSalesClientDetailVO.StepStatusVO getStep(Aftersales aftersales) {
        // 初始化所有动作为false
        int current = 0;
        String status = "process";
        List<Map<String, Object>> steps = new ArrayList<>();

        Map<String, Object> step1 = new HashMap<>();
        step1.put("title", translatePackage.translate("提交申请"));
        step1.put("active", true);
        step1.put("description", TigUtils.handelTime(aftersales.getAddTime()));
        steps.add(step1);

        Map<String, Object> step2 = new HashMap<>();
        step2.put("title", translatePackage.translate("客服审核"));
        step2.put("active", false);
        step2.put("description", aftersales.getAuditTime() != null && aftersales.getAuditTime() > 0 ? TigUtils.handelTime(aftersales.getAuditTime()) : "");
        steps.add(step2);

        Map<String, Object> step3 = new HashMap<>();
        Map<String, Object> step4 = new HashMap<>();
        if (aftersales.getAftersaleType().equals(AftersalesTypeEnum.PAYRETURN.getCode())) {
            step3.put("title", translatePackage.translate("已完成"));
            step3.put("active", false);
            step3.put("description", aftersales.getFinalTime() != null && aftersales.getFinalTime() > 0 ? TigUtils.handelTime(aftersales.getFinalTime()) : "");
            steps.add(step3);
        } else {
            step3.put("title", translatePackage.translate("售后处理"));
            step3.put("active", false);
            step3.put("description", aftersales.getDealTime() != null && aftersales.getDealTime() > 0 ? TigUtils.handelTime(aftersales.getDealTime()) : "");
            steps.add(step3);

            step4.put("title", translatePackage.translate("已完成"));
            step4.put("active", false);
            step4.put("description", aftersales.getFinalTime() != null && aftersales.getFinalTime() > 0 ? TigUtils.handelTime(aftersales.getFinalTime()) : "");
            steps.add(step4);
        }

        AftersalesStatusEnum afterSaleStatus = AftersalesStatusEnum.getStatus(aftersales.getStatus());

        switch (afterSaleStatus) {
            case IN_REVIEW -> {
                current = 1;
                step2.put("active", true);
                step2.put("description", translatePackage.translate("售后审核中，请耐心等待"));
            }
            case APPROVED_FOR_PROCESSING -> {
                current = 1;
                step2.put("active", true);
                step2.put("description", translatePackage.translate("售后审核通过，等待处理"));
            }

            case REFUSE -> {
                current = 1;
                step2.put("active", true);
                step2.put("description", translatePackage.translate("您的申请未能通过，详情请联系客服"));
            }
            case SEND_BACK -> {
                current = 2;
                step2.put("active", true);
                step2.put("description", translatePackage.translate("待用户回寄"));
                step3.put("active", true);
            }
            case RETURNED -> {
                current = 2;
                step2.put("active", true);
                step2.put("description", translatePackage.translate("待商家收货"));
                step3.put("active", true);
            }
            case WAIT_FOR_SUPPLIER_AUDIT -> {
                current = 2;
                step2.put("active", true);
                step2.put("description", translatePackage.translate("待供应商审核"));
                step3.put("active", true);
            }
            case WAIT_FOR_SUPPLIER_RECEIPT -> {
                current = 2;
                step2.put("active", true);
                step2.put("description", translatePackage.translate("待供应商收货"));
                step3.put("active", true);
            }
            case COMPLETE -> {
                current = aftersales.getAftersaleType().equals(AftersalesTypeEnum.PAYRETURN.getCode()) ? 2 : 3;
                status = "finish";
                step2.put("active", true);
                step3.put("active", true);
                step3.put("description", TigUtils.handelTime(aftersales.getFinalTime()));
                if (aftersales.getAftersaleType().equals(AftersalesTypeEnum.PAYRETURN.getCode())) {
                    if (steps.size() > 3) {
                        steps.remove(3);
                    }
                } else {
                    step4.put("active", true);
                }
            }
            case CANCEL -> {
                current = 1;
                status = "finish";
                step2.put("title", translatePackage.translate("已取消"));
                step2.put("active", true);
                step2.put("description", "");
                steps.remove(2);
            }

        }

        AfterSalesClientDetailVO.StepStatusVO stepStatusVO = new AfterSalesClientDetailVO.StepStatusVO();
        stepStatusVO.setCurrent(current);
        stepStatusVO.setStatus(status);
        stepStatusVO.setSteps(steps);
        return stepStatusVO;
    }

    //校验商品可申请数量不足
    private void validateOrderQuantity(List<AftersalesApplyParam.ItemsDTO> items, Integer orderId, boolean isAdd, Integer aftersaleId) {
        for (AftersalesApplyParam.ItemsDTO item : items) {
            //
            if (item.getNumber() == 0) {
                continue;
            }
            //查询是否有订单在售后
            if (isAdd) {
                long count = getApplyAftersalesCount(orderId, item.getOrderItemId());
                Assert.isTrue(count == 0, () -> new GlobalException(translatePackage.translate("该商品已申请售后,请勿重复申请 ")));
            }

            Integer orderQuantity = getOrderItemNumber(item.getOrderItemId(), orderId);
            Integer sumValidNum = getValidNumber(item.getOrderItemId(), orderId, isAdd, aftersaleId);
            Assert.isTrue(orderQuantity >= item.getNumber() + sumValidNum, () -> new GlobalException(translatePackage.translate("该商品可申请数量不足")));
        }
    }

    // 售后进行中状态
    private List<Integer> getAfterSalseStatusList() {
        List<Integer> status = new ArrayList<>();
        status.add(IN_REVIEW.getCode());
        status.add(APPROVED_FOR_PROCESSING.getCode());
        status.add(AftersalesStatusEnum.SEND_BACK.getCode());
        status.add(AftersalesStatusEnum.RETURNED.getCode());
        Integer isVendor = tigshopProperties.getIsVendor();
        if (isVendor == 1) {
            status.add(WAIT_FOR_SUPPLIER_AUDIT.getCode());
            status.add(WAIT_FOR_SUPPLIER_RECEIPT.getCode());
        }
        return status;
    }

    //获取售后有效状态
    private List<Integer> getAfterSalseValidStatusList() {
        List<Integer> status = new ArrayList<>();
        status.add(IN_REVIEW.getCode());
        status.add(REFUSE.getCode());
        status.add(APPROVED_FOR_PROCESSING.getCode());
        status.add(AftersalesStatusEnum.SEND_BACK.getCode());
        status.add(AftersalesStatusEnum.RETURNED.getCode());
        status.add(AftersalesStatusEnum.COMPLETE.getCode());
        status.add(WAIT_FOR_SUPPLIER_AUDIT.getCode());
        status.add(WAIT_FOR_SUPPLIER_RECEIPT.getCode());
        return status;
    }

    //获取已申请售后的数量
    private long getApplyAftersalesCount(Integer orderId, Integer itemId) {
        LambdaQueryWrapper<Aftersales> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Aftersales::getOrderId, orderId);
        queryWrapper.in(Aftersales::getStatus, getAfterSalseStatusList());
        List<Aftersales> aftersalesList = this.list(queryWrapper);
        if (!aftersalesList.isEmpty()) {
            //获取主键Id
            List<Integer> ids = aftersalesList.stream().
                    map(Aftersales::getAftersaleId).
                    toList();
            LambdaQueryWrapper<AftersalesItem> aftersalesItemQueryWrapper = new LambdaQueryWrapper<>();
            aftersalesItemQueryWrapper.in(AftersalesItem::getAftersaleId, ids);
            aftersalesItemQueryWrapper.eq(AftersalesItem::getOrderItemId, itemId);
            return aftersalesItemService.count(aftersalesItemQueryWrapper);
        }
        return 0;

    }

    //获取订单购买数量
    private Integer getOrderItemNumber(Integer itemId, Integer orderId) {
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getItemId, itemId);
        queryWrapper.eq(OrderItem::getOrderId, orderId);
        OrderItem orderItem = orderItemMapper.selectOne(queryWrapper);
        if (orderItem != null) {
            return orderItem.getQuantity();
        }
        return 0;
    }

    //获取有效数量
    private Integer getValidNumber(Integer itemId, Integer orderId, boolean isAdd, Integer aftersaleId) {
        List<Aftersales> aftersalesList = this.lambdaQuery()
                .eq(Aftersales::getOrderId, orderId)
                .in(Aftersales::getStatus, getAfterSalseValidStatusList())
                .ne(!isAdd, Aftersales::getAftersaleId, aftersaleId)
                .list();

        Integer sumValidNum = 0;
        if (!aftersalesList.isEmpty()) {
            List<Integer> ids = aftersalesList.stream().
                    map(Aftersales::getAftersaleId).
                    toList();
            QueryWrapper<AftersalesItem> aftersalesItemQueryWrapper = new QueryWrapper<>();
            aftersalesItemQueryWrapper.in("aftersale_id", ids);
            aftersalesItemQueryWrapper.eq("order_item_id", itemId);
            aftersalesItemQueryWrapper.select("sum(number) as number");
            AftersalesItem aftersalesItem = aftersalesItemService.getOne(aftersalesItemQueryWrapper);

            if (aftersalesItem != null) {
                sumValidNum = aftersalesItem.getNumber();
            } else {
                sumValidNum = 0;
            }
        }
        return sumValidNum;
    }
}
