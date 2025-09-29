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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.dto.order.*;
import com.tigshop.bean.dto.order.deliver.BatchDeliverData;
import com.tigshop.bean.dto.order.deliver.DeliverDataList;
import com.tigshop.bean.dto.order.deliver.DeliverInfo;
import com.tigshop.bean.dto.order.deliver.DeliverOrderParam;
import com.tigshop.bean.dto.print.PrintOrderDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.dto.shop.OrderConfigDataDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.finance.AccountType;
import com.tigshop.bean.enums.finance.EntryType;
import com.tigshop.bean.enums.finance.StatementType;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.msg.MessageTypeEnum;
import com.tigshop.bean.enums.o2o.ShipmentStatusEnum;
import com.tigshop.bean.enums.order.*;
import com.tigshop.bean.enums.print.PrintStatusEnum;
import com.tigshop.bean.enums.salesman.SalesmanConfigTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.bean.enums.vendor.VendorProductSkuStockBizEnum;
import com.tigshop.bean.feign.shipping.ShippingResult;
import com.tigshop.bean.feign.wechat.GetTokenResult;
import com.tigshop.bean.feign.wechat.UploadShippingInfoParam;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.finance.RecordRate;
import com.tigshop.bean.model.finance.StatementOutput;
import com.tigshop.bean.model.o2o.OrderPickupItem;
import com.tigshop.bean.model.o2o.OrderPickupShipment;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.o2o.UserPickupInfo;
import com.tigshop.bean.model.order.*;
import com.tigshop.bean.model.print.Print;
import com.tigshop.bean.model.product.Comment;
import com.tigshop.bean.model.product.ECard;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.model.salesman.SalesmanConfig;
import com.tigshop.bean.model.salesman.SalesmanOrder;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.setting.LogisticsCompany;
import com.tigshop.bean.model.shop.CollectShop;
import com.tigshop.bean.model.shop.OrderConfig;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.*;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.param.finance.statement.StatementOutputSaveParam;
import com.tigshop.bean.param.finance.statement.StatementSaveParam;
import com.tigshop.bean.param.order.ExportItemSaveParam;
import com.tigshop.bean.param.order.OrderAdminNoteParam;
import com.tigshop.bean.param.order.OrderChangeStatusParam;
import com.tigshop.bean.param.vendor.product.VendorProductSkuStockParam;
import com.tigshop.bean.query.order.AftersalesListPageQuery;
import com.tigshop.bean.query.order.OrderListPageQuery;
import com.tigshop.bean.query.panel.SalesProductDetailPageQuery;
import com.tigshop.bean.vo.finance.OrderInvoiceVO;
import com.tigshop.bean.vo.o2o.OrderPickupShipmentVO;
import com.tigshop.bean.vo.o2o.OrderPickupTimeVO;
import com.tigshop.bean.vo.o2o.PickupListVO;
import com.tigshop.bean.vo.order.*;
import com.tigshop.bean.vo.panel.SalesProductOrderItemVO;
import com.tigshop.bean.vo.setting.config.OrderSettingsVO;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.bean.vo.user.UserBaseVO;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.enums.KuaiDiNiaoTypes;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.*;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.feign.ShippingInfoClient;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.finance.PaylogMapper;
import com.tigshop.mapper.finance.StatementOutputMapper;
import com.tigshop.mapper.o2o.UserPickupInfoMapper;
import com.tigshop.mapper.order.AftersalesMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.print.PrintMapper;
import com.tigshop.mapper.product.CommentMapper;
import com.tigshop.mapper.salesman.SalesmanConfigMapper;
import com.tigshop.mapper.salesman.SalesmanMapper;
import com.tigshop.mapper.salesman.SalesmanOrderMapper;
import com.tigshop.mapper.setting.LogisticsCompanyMapper;
import com.tigshop.mapper.shop.CollectShopMapper;
import com.tigshop.mapper.shop.OrderConfigMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.user.CollectProductMapper;
import com.tigshop.mapper.user.UserAuthorizeMapper;
import com.tigshop.mapper.user.UserCouponMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.vendor.VendorMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.RecordRateService;
import com.tigshop.service.finance.StatementOutputService;
import com.tigshop.service.kuaidi.KuaiDiService;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.msgTemplate.MessageService;
import com.tigshop.service.o2o.OrderPickupItemService;
import com.tigshop.service.o2o.OrderPickupShipmentService;
import com.tigshop.service.o2o.StoreProductService;
import com.tigshop.service.o2o.StoreSkuService;
import com.tigshop.service.order.*;
import com.tigshop.service.product.ECardService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.promotion.SeckillService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.LogisticsCompanyService;
import com.tigshop.service.setting.RegionService;
import com.tigshop.service.shop.PickupService;
import com.tigshop.service.user.UserBalanceService;
import com.tigshop.service.user.UserPointsLogService;
import com.tigshop.service.user.UserRankService;
import com.tigshop.service.vendor.VendorProductSkuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tigshop.bean.enums.o2o.ShipmentStatusEnum.NOT_SHIPPED;
import static com.tigshop.bean.enums.o2o.ShipmentStatusEnum.SHIPPED;
import static com.tigshop.bean.enums.order.OrderStatusEnum.PENDING;
import static com.tigshop.bean.enums.setting.SettingsEnum.COLLECTION_TIME_SETTING;
import static com.tigshop.common.constant.Constants.*;
import static com.tigshop.common.constant.order.OrderConstants.*;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 订单管理服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class OrderServiceImpl extends BaseServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderItemService orderItemService;
    private final UserMapper userMapper;
    private final OrderAftersalesService orderAfterSalesService;
    private final OrderLogService orderLogService;
    private final OrderAmountDetailService orderAmountDetailService;
    private final OrderCouponDetailService orderCouponDetailService;
    private final OrderSpitLogService orderSpitLogService;
    private final RegionService regionService;
    private final HttpServletRequest request;
    private final LogisticsApiLogService logisticsApiLogService;
    private final AdminUserMapper adminUserMapper;
    private final ShopMapper shopMapper;
    private final AftersalesItemService aftersalesItemService;
    private final CollectProductMapper collectProductMapper;
    private final CollectShopMapper collectShopMapper;
    private final LogisticsCompanyMapper logisticsCompanyMapper;
    private final ShippingInfoClient shippingInfoClient;
    private final LogisticsCompanyService logisticsCompanyService;
    private final ConfigService configService;
    private final CommentMapper commentMapper;
    private final AftersalesMapper aftersalesMapper;
    private final OrderConfigMapper orderConfigMapper;
    private final PaylogMapper paylogMapper;
    private final OrderItemMapper orderItemMapper;
    private final StoreProductService storeProductService;
    private final StoreSkuService storeSkuService;
    private final UserBalanceService userBalanceService;
    private final UserPointsLogService userPointsLogService;
    private final UserCouponMapper userCouponMapper;
    private final ProductService productService;
    private final ProductSkuService productSkuService;
    private final SeckillService seckillService;
    private final AdminMsgService adminMsgService;
    private final TranslatePackageImpl translatePackage;
    private final RabbitTemplate rabbitTemplate;
    private final UserRankService userRankService;
    private final MessageService messageService;
    private final WechatApiClient wechatApiClient;
    private final UserAuthorizeMapper userAuthorizeMapper;
    private final AftersalesService aftersalesService;
    private final SalesmanOrderMapper salesmanOrderMapper;
    private final SalesmanMapper salesmanMapper;
    private final SalesmanConfigMapper salesmanConfigMapper;
    private final PrintMapper printMapper;
    private final VendorProductSkuService vendorProductSkuService;
    private final TigshopProperties tigshopProperties;
    private final KuaiDiService kdNiaoService;
    private final VendorMapper vendorMapper;
    private final StatementOutputMapper statementOutputMapper;
    private final StatementOutputService statementOutputService;
    private final RecordRateService recordRateService;
    private final OrderPickupItemService orderPickupItemService;
    private final OrderPickupShipmentService orderPickupShipmentService;
    private final ECardService eCardService;
    private final PickupService pickupService;
    private final UserPickupInfoMapper userPickupInfoMapper;

    @Override
    public Page<OrderVO> list(OrderListPageQuery pageQuery) {
        Page<Order> page = buildSortOrder(pageQuery);

        // 构造查询构造器
        // LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        MPJLambdaWrapper<Order> queryWrapper = new MPJLambdaWrapper<>();
        buildQueryWrapper(queryWrapper, pageQuery);

        // 待晒单
        if (pageQuery.getIsShowed() != null && pageQuery.getIsShowed() > -1) {
            List<Comment> comments = commentMapper.selectList(
                    Wrappers.lambdaQuery(Comment.class).eq(Comment::getUserId, pageQuery.getUserId()).eq(Comment::getIsShowed, pageQuery.getIsShowed())
            );
            // 取出评论的orderId
            List<Integer> orderIds = comments.stream().map(Comment::getOrderId).toList();
            if (orderIds.isEmpty()) {
                return new Page<>();
            }
            queryWrapper.in(Order::getOrderId, orderIds);
        }
        // 如果查询参数包含了支付方式，那么联表筛选出已支付的相关支付方式的订单
        if (StrUtil.isNotBlank(pageQuery.getPayCode())) {
            queryWrapper.leftJoin(Paylog.class, Paylog::getOrderId, Order::getOrderId)
                    .eq(Paylog::getPayCode, pageQuery.getPayCode())
                    .eq(Paylog::getPayStatus, PayStatusEnum.PAID.getCode())
                    .groupBy(Order::getOrderId);
        }
        // 执行查询
        Page<Order> orderPage = this.page(page, queryWrapper);

        // 获取查询结果
        List<Order> orderPageRecords = orderPage.getRecords();
        if (orderPageRecords.isEmpty()) {
            return new Page<>();
        }

        List<Integer> userIds = orderPageRecords.stream().map(Order::getUserId).distinct().toList();
        Map<Integer, User> userMap = new HashMap<>();
        if (CollUtil.isNotEmpty(userIds)) {
            List<User> userList = userMapper.selectBatchIds(userIds);
            userMap = userList.stream().collect(Collectors.toMap(User::getUserId, Function.identity()));
        }

        // 调用方法获取订单信息
        List<Integer> orderIds = orderPageRecords.stream().map(Order::getOrderId).toList();
        List<OrderItemVO> itemListNew = getOrderItemList(orderIds);

        // 插入售后数据
        Map<Integer, List<OrderItemVO>> itemMap = itemListNew.stream().collect(Collectors.groupingBy(OrderItemVO::getOrderId));
        // 取出店铺Id
        List<Integer> shopIds = orderPageRecords.stream().map(Order::getShopId).distinct().toList();
        List<Shop> shopList = shopMapper.selectBatchIds(shopIds);
        Map<Integer, Shop> shopMap = shopList.stream().collect(Collectors.toMap(Shop::getShopId, Function.identity()));

        // 查询店铺结算配置
        List<OrderConfig> orderConfigs = orderConfigMapper.selectList(
                new LambdaQueryWrapper<OrderConfig>()
                        .in(OrderConfig::getShopId, shopIds)
        );
        Map<Integer, OrderConfig> orderConfigMap = orderConfigs.stream().collect(Collectors.toMap(OrderConfig::getShopId, Function.identity()));

        // 获取订单配置
        OrderSettingsVO orderSettings = configService.getSettings(OrderSettingsVO.class);

        // 转换为VO
        Map<Integer, User> finalUserMap = userMap;
        String afterSalesLimitDays = configService.getConfigVal(SettingsEnum.AFTER_SALES_LIMIT_DAYS);
        String autoDeliveryDays = configService.getConfigVal(SettingsEnum.AUTO_DELIVERY_DAYS);
        List<OrderVO> orderVOList = orderPageRecords.stream()
                .map(order -> {
                    OrderVO orderVO = new OrderVO();
                    BeanUtils.copyProperties(order, orderVO);
                    orderVO.setRegionIds(JsonUtil.jsonToList(order.getRegionIds(), Integer.class));
                    orderVO.setRegionNames(JsonUtil.jsonToList(order.getRegionNames(), String.class));

                    Integer orderStatus = order.getOrderStatus();
                    orderVO.setOrderStatusName(OrderStatusEnum.getStatusName(orderStatus));

                    orderVO.setPayStatusName(PaymentStatus.getStatusName(order.getPayStatus()));
                    orderVO.setShippingStatusName(ShippingStatusEnum.getStatusName(order.getShippingStatus()));
                    orderVO.setInvoiceData(JSON.parseObject(order.getInvoiceData(), OrderInvoiceVO.class));
                    if (orderVO.getRegionNames() != null) {
                        orderVO.setUserAddress(String.join(" ", orderVO.getRegionNames()) + " " + order.getAddress());
                    }
                    UserAddressVO addressData = JsonUtil.fromJson(order.getAddressData(), UserAddressVO.class);
                    orderVO.setAddressData(addressData);

                    DateTime addDate = DateUtil.date(order.getAddTime() * 1000);
                    orderVO.setAddTime(DateUtil.format(addDate, DATE_FORMAT));

                    if (order.getPayTime() > 0) {
                        DateTime payDate = DateUtil.date(order.getPayTime() * 1000);
                        orderVO.setPayTime(DateUtil.format(payDate, DATE_FORMAT));
                    }

                    if (order.getShippingTime() > 0) {
                        DateTime shippingDate = DateUtil.date(order.getShippingTime() * 1000);
                        orderVO.setShippingTime(DateUtil.format(shippingDate, DATE_FORMAT));
                    }

                    if (order.getReceivedTime() > 0) {
                        DateTime receivedDate = DateUtil.date(order.getReceivedTime() * 1000);
                        orderVO.setReceivedTime(DateUtil.format(receivedDate, DATE_FORMAT));
                    }

                    orderVO.setItems(itemMap.get(order.getOrderId()));
                    UserBaseVO userBaseVO = new UserBaseVO();
                    User user = finalUserMap.get(order.getUserId());
                    if (user != null) {
                        BeanUtils.copyProperties(user, userBaseVO);
                    }
                    orderVO.setUser(userBaseVO);
                    orderVO.setAvailableActions(availableActions(orderVO));

                    if (order.getShopId() != null && order.getShopId() > 0) {
                        OrderVO.ShopVO shopVO = new OrderVO.ShopVO();
                        Shop shop = shopMap.get(order.getShopId());
                        if (shop != null) {
                            BeanUtils.copyProperties(shop, shopVO);
                            shopVO.setStatusText(ShopType.getTypeName(shop.getStatus()));
                            orderVO.setShop(shopVO);
                            orderVO.setShopName(shopVO.getShopTitle());
                            orderVO.setShopId(shopVO.getShopId());
                        }
                    }

                    // 判断是否可以申请售后；1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）2. 全局设置（买家申请售后限制）
                    boolean toAftersales = orderVO.getAvailableActions().getToAftersales();
                    orderVO.setToAftersalses(toAftersales);
                    if (toAftersales) {
                        // 1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）
                        if (order.getShopId() != null && order.getShopId() > 0) {
                            OrderConfig orderConfig = orderConfigMap.get(order.getShopId());
                            if (orderConfig != null) {
                                OrderConfigDTO orderConfigData = JsonUtil.fromJson(orderConfig.getData(), OrderConfigDTO.class);
                                if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus() && 0 == orderConfigData.getDateType()) {
                                    orderVO.setToAftersalses(false);
                                }
                            }
                        }
                        // 2. 全局设置（买家申请售后限制）
                        if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus()) {
                            if (StrUtil.isNotBlank(afterSalesLimitDays)) {
                                long receivedTime = order.getReceivedTime() * 1000;
                                BigDecimal afterSalesLimitDaysTime = new BigDecimal(afterSalesLimitDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
                                LocalDateTime limitDateTime = Instant.ofEpochMilli(receivedTime + afterSalesLimitDaysTime.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                                boolean toAftersalses = limitDateTime.isAfter(LocalDateTime.now());
                                orderVO.setToAftersalses(toAftersalses);
                            }
                        }
                    }
                    orderVO.getAvailableActions().setToAftersales(orderVO.getToAftersalses());

                    // 自动发货天数
                    Long aftersalesCount = aftersalesMapper.selectCount(
                            Wrappers.lambdaQuery(Aftersales.class)
                                    .eq(Aftersales::getOrderId, order.getOrderId())
                                    .eq(Aftersales::getStatus, AftersalesStatusEnum.IN_REVIEW.getCode())
                    );
                    if (aftersalesCount == 0 && OrderStatusEnum.PROCESSING.getCode() == order.getOrderStatus() && StrUtil.isNotBlank(autoDeliveryDays)) {
                        long shippingTimeMillis = order.getShippingTime() * 1000;
                        long elapsedMillis = Instant.now().toEpochMilli() - shippingTimeMillis;

                        long elapsedDays = TimeUnit.MILLISECONDS.toDays(elapsedMillis);

                        BigDecimal result = new BigDecimal(autoDeliveryDays).subtract(new BigDecimal(elapsedDays)).setScale(0, RoundingMode.UP);

                        orderVO.setAutoDeliveryDays(result);
                    }

                    orderVO.assembleOrderSettings(orderSettings);

                    // 支付信息
                    Paylog paylog = paylogMapper.selectOne(Wrappers.lambdaQuery(Paylog.class)
                            .eq(Paylog::getPayStatus, PayStatusEnum.PAID.getCode())
                            .eq(Paylog::getOrderId, order.getOrderId())
                            .select(Paylog::getPaySn, Paylog::getPayCode, Paylog::getTransactionId)
                    );
                    orderVO.setPayLog(paylog);

                    orderVO.setVendorName(Optional.ofNullable(vendorMapper.selectById(order.getVendorId())).map(Vendor::getVendorName).orElse(""));
                    // 是否自提
//                    Long count = orderPickupShipmentMapper.selectCount(Wrappers.lambdaQuery(OrderPickupShipment.class)
//                            .eq(OrderPickupShipment::getOrderId, order.getOrderId()));
                    long count = orderPickupItemService.count(Wrappers.lambdaQuery(OrderPickupItem.class)
                            .eq(OrderPickupItem::getOrderId, order.getOrderId()));
                    orderVO.setIsPickup(count > 0);
                    try {
                        if (AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.VENDOR) {
                            // 供应商只保留去发货按钮
                            orderVO.setIsPickup(false);
                        }
                    } catch (Exception e) {
                    }

                    if (tigshopProperties.getIsO2o() == 1 && count > 0) {
                        orderVO.setAutoDeliveryDays(null);
                    }

                    if (count > 0 && orderStatus == OrderStatusEnum.CONFIRMED.getCode()) {
                        orderVO.setOrderStatusName(NOT_SHIPPED.getDescription());
                    } else if (count > 0 && orderStatus == OrderStatusEnum.PROCESSING.getCode()) {
                        orderVO.setOrderStatusName(SHIPPED.getDescription());
                    }
                    return orderVO;
                }).toList();
        return PageUtil.convertPage(orderPage, orderVOList);
    }

    @Override
    public List<OrderItemVO> getOrderItemList(List<Integer> orderIds) {
        List<OrderItemVO> itemList = orderItemService.getItemByOrderIds(orderIds);
        return itemList.stream().map(item -> {
            OrderItemVO orderItemVO = new OrderItemVO();
            BeanUtils.copyProperties(item, orderItemVO);
            LambdaQueryWrapper<AftersalesItem> queryWrapperSales = new LambdaQueryWrapper<>();
            queryWrapperSales.eq(AftersalesItem::getOrderItemId, item.getItemId());
            queryWrapperSales.orderByDesc(AftersalesItem::getAftersalesItemId);
            queryWrapperSales.last("limit 1");
            AftersalesItem aftersalesItem = aftersalesItemService.getOne(queryWrapperSales);
            if (aftersalesItem != null) {
                orderItemVO.setAftersalesItem(aftersalesItem);
            }
            return orderItemVO;
        }).toList();
    }

    // 构建查询构造器
    public void buildQueryWrapper(MPJLambdaWrapper<Order> queryWrapper, OrderListPageQuery pageQuery) {
        // 关键词检索 收货人 + 订单号 + 订单id
        String keyword = pageQuery.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Order::getOrderSn, keyword)
                    .or()
                    .like(Order::getConsignee, keyword)
                    .or()
                    .eq(Order::getOrderId, keyword));
        }

        //订单状态
        if (pageQuery.getOrderStatus() != null && ObjectUtil.isNotEmpty(pageQuery.getOrderStatus())) {
            String[] split = pageQuery.getOrderStatus().split(",");
            if (split.length == 1 && Integer.parseInt(split[0]) >= 0) {
                queryWrapper.eq(Order::getOrderStatus, split[0]);
            }
            if (split.length > 1) {
                queryWrapper.in(Order::getOrderStatus, List.of(split));
            }
        }
        if (pageQuery.getOrderStatus() != null && ObjectUtil.equals(pageQuery.getOrderStatus(), "-2")) {
            // 查询删除的订单
            queryWrapper.eq(Order::getIsDel, DeleteType.DELETE.getCode());
        } else {
            queryWrapper.eq(Order::getIsDel, DeleteType.NOT_DELETE.getCode());
        }
        if (pageQuery.getOrderIds() != null && !pageQuery.getOrderIds().isEmpty()) {
            queryWrapper.in(Order::getOrderId, pageQuery.getOrderIds());
        }
        // 店铺检索
        if (getShopId() != null && getShopId() > 0) {
            if (getAdminType().equals(AdminTypeEnum.PICKUP.getCode())) {
                queryWrapper.leftJoin(OrderPickupItem.class, OrderPickupItem::getOrderId, Order::getOrderId);
                queryWrapper.eq(OrderPickupItem::getPickupId, getShopId());
            } else {
                queryWrapper.eq(Order::getShopId, getShopId());
            }
        } else {
            if (pageQuery.getShopId() != null && pageQuery.getShopId() > 0) {
                queryWrapper.eq(Order::getShopId, pageQuery.getShopId());
            }
        }
        //供应商筛选
        if (getVendorId() != null && getVendorId() > 0) {
            queryWrapper.eq(Order::getVendorId, getVendorId());
        } else {
            if (pageQuery.getVendorId() != null && pageQuery.getVendorId() > 0) {
                queryWrapper.eq(Order::getVendorId, pageQuery.getVendorId());
            }
            if (pageQuery.getVendorId() != null && pageQuery.getVendorId() == -2) {
                //查询供应商ID大于0的
                queryWrapper.gt(Order::getVendorId, 0);
            }
        }

        // 是否结算检索
        if (pageQuery.getIsSettlement() != null && pageQuery.getIsSettlement() > -1) {
            queryWrapper.eq(Order::getIsSettlement, pageQuery.getIsSettlement());
        }
        // 是否积分订单
        if (pageQuery.getIsExchangeOrder() != null && pageQuery.getIsExchangeOrder() > -1) {
            queryWrapper.eq(Order::getIsExchangeOrder, pageQuery.getIsExchangeOrder());
        }
        // 支付状态
        if (pageQuery.getPayStatus() != null && pageQuery.getPayStatus() != -1) {
            queryWrapper.eq(Order::getPayStatus, pageQuery.getPayStatus());
        }
        // 发货状态
        if (pageQuery.getShippingStatus() != null && pageQuery.getShippingStatus() != -1) {
            queryWrapper.eq(Order::getShippingStatus, pageQuery.getShippingStatus());
        }
        // 评价状态
        if (pageQuery.getCommentStatus() != null && pageQuery.getCommentStatus() != -1) {
            if (pageQuery.getCommentStatus() > 0) {
                queryWrapper.eq(Order::getCommentStatus, pageQuery.getCommentStatus());
            } else {
                queryWrapper.eq(Order::getCommentStatus, 0).eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode());
            }
        }
        // 详情地址
        if (StrUtil.isNotEmpty(pageQuery.getAddress())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Order::getAddress, pageQuery.getAddress())
                    .or()
                    .like(Order::getRegionNames, pageQuery.getAddress()));
        }
        // 收货人的email
        if (StrUtil.isNotEmpty(pageQuery.getEmail())) {
            queryWrapper.like(Order::getEmail, pageQuery.getEmail());
        }
        //手机号
        if (StrUtil.isNotEmpty(pageQuery.getMobile())) {
            queryWrapper.like(Order::getMobile, pageQuery.getMobile());
        }
        //配送物流
        if (pageQuery.getLogisticsId() != null && pageQuery.getLogisticsId() > 0) {
            queryWrapper.eq(Order::getLogisticsId, pageQuery.getLogisticsId());
        }
        //下单时间
        if (StrUtil.isNotEmpty(pageQuery.getAddStartTime())) {
            LocalDateTime startOfDay = LocalDate.parse(pageQuery.getAddStartTime()).atStartOfDay();
            Long addStartTime = startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000;
            queryWrapper.ge(Order::getAddTime, addStartTime);
        }
        if (StrUtil.isNotEmpty(pageQuery.getAddEndTime())) {
            LocalDateTime endOfDay = LocalDate.parse(pageQuery.getAddEndTime()).atTime(LocalTime.MAX);
            Long addEndTime = endOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000;

            queryWrapper.le(Order::getAddTime, addEndTime);
        }
        //支付时间
        if (StrUtil.isNotBlank(pageQuery.getPayTime())) {
            //逗号隔开分割时间
            String[] payTime = pageQuery.getPayTime().split(",");
            Long payTimeStart = DateUtil.parse(payTime[0]).getTime();
            Long payTimeEnd = DateUtil.parse(payTime[1]).getTime() + 86400;
            queryWrapper.between(Order::getPayTime, payTimeStart, payTimeEnd);
        }

        //用户ID
        if (pageQuery.getUserId() != null) {
            queryWrapper.eq(Order::getUserId, pageQuery.getUserId());
        }
        // PC 端 日期检索
        if (pageQuery.getDataType() != null) {
            Long[] startAndEndTime = TigUtils.getStartAndEndTime(pageQuery.getDataType());
            queryWrapper.between(Order::getAddTime, startAndEndTime[0], startAndEndTime[1]);
        }

        if (pageQuery.getMark() != null && pageQuery.getMark() != -1) {
            queryWrapper.eq(Order::getMark, pageQuery.getMark());
        }
    }

    /**
     * 获取订单可用的操作
     */
    @Override
    public OrderActionVO availableActions(OrderVO orderVO) {
        OrderActionVO orderActionVO = new OrderActionVO();
        Integer orderId = orderVO.getOrderId();
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.getStatus(orderVO.getOrderStatus());
        orderVO.setToAftersalses(false);
        switch (orderStatusEnum) {
            case PENDING:
                // 待确认，待付款
                orderActionVO.setSetConfirm(true);
                orderActionVO.setCancelOrder(true);
                orderActionVO.setModifyOrder(true);
                orderActionVO.setModifyShippingInfo(true);
                orderActionVO.setModifyOrderMoney(true);
                orderActionVO.setSetPaid(true);
                orderActionVO.setModifyOrderConsignee(true);
                // 假设 adminUid 是一个可以通过某种方式获取的变量
                if (SecurityUtils.isAdminUser() && orderVO.getPayTypeId() != PayType.OFFLINE.getCode()) {
                    // 后台用户不允许操作非线下支付订单
                    orderActionVO.setSetPaid(false);
                }
                orderActionVO.setToPay(true);
                break;
            case CONFIRMED:
                // 已确认，待发货
                orderActionVO.setSplitOrder(true);
                orderActionVO.setDeliver(true);
                orderActionVO.setModifyOrderConsignee(true);
                if (orderVO.getPayStatus() != PaymentStatus.PAID.getCode()) {
                    orderActionVO.setSetPaid(true);
                    orderActionVO.setCancelOrder(true);
                }
                orderActionVO.setToAftersales(orderAfterSalesService.getOrderAfterSalesToAction(orderId));
                break;
            case PROCESSING:
                // 已发货，处理中
                orderActionVO.setConfirmReceipt(true);
                orderActionVO.setModifyShippingInfo(true);
                orderActionVO.setToAftersales(orderAfterSalesService.getOrderAfterSalesToAction(orderId));
                break;
            case COMPLETED:
                orderActionVO.setRebuy(true);
                orderActionVO.setToAftersales(orderAfterSalesService.getOrderAfterSalesToAction(orderId));
                if (orderVO.getCommentStatus() == PENDING.getCode()) {
                    orderActionVO.setToComment(true);
                }
                break;
            case CANCELLED:
                orderActionVO.setRebuy(true);
                orderActionVO.setDelOrder(true);
                break;
            case INVALID:
                orderActionVO.setDelOrder(true);
                break;
            // 其他状态...
            default:
                break;
        }
        PaymentStatus paymentStatus = PaymentStatus.getStatus(orderVO.getPayStatus());
        switch (paymentStatus) {
            case UNPAID:
                break;
            case PAID:
                orderActionVO.setSetUnpaid(false);
                break;
            default:
                break;
        }
        if (orderVO.getIsStoreSplited() != null && orderVO.getIsStoreSplited() == 1) {
            orderActionVO.setSplitOrder(false);
        } else {
            orderActionVO.setDeliver(false);
        }
        if (orderVO.getIsDel() != null && orderVO.getIsDel() == 1) {
            orderActionVO.setDelOrder(false);
        }
        if (orderVO.getOrderType() != null && orderVO.getOrderType() > 1) {
            orderActionVO.setRebuy(false);
            if (OrderType.getNoAfterSalesType(orderVO.getOrderType())) {
                orderActionVO.setToAftersales(false);
            }
        }
        return orderActionVO;
    }

    @Transactional
    @Override
    public void setAdminNote(OrderAdminNoteParam param) {
        // 批量操作
        if (StrUtil.isNotBlank(param.getIds())) {
            List<String> ids = StrUtil.split(param.getIds(), StrUtil.COMMA);
            List<Order> orders = ids.stream().map(id -> Order.builder()
                    .orderId(Integer.parseInt(id))
                    .adminNote(param.getAdminNote())
                    .mark(param.getMark())
                    .build()).toList();
            this.updateBatchById(orders);

            for (String id : ids) {
                Order orderModel = this.getById(id);
                orderLogService.quickCreate("订单商家备注已更新", orderModel.getOrderId(), orderModel.getOrderSn());
            }
        } else {
            Order order = Order.builder()
                    .orderId(param.getId())
                    .adminNote(param.getAdminNote())
                    .mark(param.getMark())
                    .build();
            this.updateById(order);

            Order orderModel = this.getById(param.getId());
            orderLogService.quickCreate("订单商家备注已更新", orderModel.getOrderId(), orderModel.getOrderSn());
        }


    }

    @Override
    public Long getOrderStatusCount(int userId, String type, int status) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getIsDel, 0);
        queryWrapper.eq(Order::getUserId, userId);
        if (Objects.equals(type, "order_status")) {
            queryWrapper.eq(Order::getOrderStatus, status);
        }

        if (Objects.equals(type, "comment_status")) {
            queryWrapper.eq(Order::getCommentStatus, status);
        }

        if (Objects.equals(type, "order_comment_status")) {
            queryWrapper.eq(Order::getCommentStatus, PENDING.getCode());
            queryWrapper.eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode());
        }

        if (Objects.equals(type, "order_comment_status")) {
            queryWrapper.eq(Order::getCommentStatus, PENDING.getCode());
            queryWrapper.eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode());
        }

        return this.count(queryWrapper);
    }

    /**
     * 确认收货
     */
    @Override
    public boolean setOrderConfirm(Integer id) {
        OrderVO orderVO = this.detail(id);
        if (!orderVO.getAvailableActions().getConfirmReceipt()) {
            return false;
        }
        Order order = new Order();
        order.setOrderId(id);
        order.setOrderStatus(OrderStatusEnum.CONFIRMED.getCode());
        this.save(order);
        orderLogService.quickCreate("订单状态已更新", orderVO.getOrderId(), orderVO.getOrderSn());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean splitStoreOrder(Integer id) {
        OrderVO orderVO = this.detail(id);
        if (orderVO.getIsStoreSplited() == 1) {
            return false;
        }
        Map<Integer, Map<Integer, List<OrderItemVO>>> stores = new HashMap<>();

        if (CollUtil.isNotEmpty(orderVO.getItems())) {
            orderVO.getItems().forEach(item -> {
                Integer shopId = item.getShopId();
                Integer vendorId = item.getVendorId();
                stores.computeIfAbsent(shopId, k -> new HashMap<>())
                        .computeIfAbsent(vendorId, k -> new ArrayList<>())
                        .add(item);
            });
        }

        //计算stores中只有一个店铺且只有一个VID
        boolean isSingleShopAndVendor = stores.size() == 1 && stores.values().iterator().next().size() == 1;
        if (isSingleShopAndVendor) {
            // 所有商品只存在同一个店铺id，直接更新拆分状态
            Order order1 = new Order();
            order1.setIsStoreSplited(1);
            order1.setOrderId(orderVO.getOrderId());
            this.updateById(order1);
            return true;
        }

        for (Map.Entry<Integer, Map<Integer, List<OrderItemVO>>> entry : stores.entrySet()) {
            int shopId = entry.getKey();
            for (Map.Entry<Integer, List<OrderItemVO>> itemEntry : entry.getValue().entrySet()) {
                List<OrderItemVO> items = itemEntry.getValue();
                // 同一个店铺订单，是否是同一个供应商。同一个店铺不同供应商的商品的优惠劵需要分摊
                OrderVO copyOrderVO;
                boolean isSingleShopSingleVendor = entry.getValue().size() == 1;
                if (!isSingleShopSingleVendor) {
                    copyOrderVO = new OrderVO();
                    BeanUtils.copyProperties(orderVO, copyOrderVO);
                    copyOrderVO.setProductAmount(BigDecimal.ZERO);
                    entry.getValue().forEach((key, value) -> value.forEach(orderItemVO -> {
                        copyOrderVO.setProductAmount(copyOrderVO.getProductAmount().add(orderItemVO.getOriginPrice().multiply(BigDecimal.valueOf(orderItemVO.getQuantity()))));
                    }));
                } else {
                    copyOrderVO = orderVO;
                }
                this.createSplitOrder(copyOrderVO, items, true, shopId, isSingleShopSingleVendor);
            }

        }

        // 删除父订单和订单商品
        orderLogService.quickCreate("订单商品来自不同店铺，已拆分", orderVO.getOrderId(), orderVO.getOrderSn());
        this.removeById(orderVO.getOrderId());
        orderItemService.remove(
                Wrappers.lambdaQuery(OrderItem.class)
                        .eq(OrderItem::getOrderId, orderVO.getOrderId())
        );
        return true;
    }

    public String creatNewOrderSn() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String timestamp = dateFormat.format(new Date());
        Random random = new Random();
        int randomNumber = 100 + random.nextInt(900);
        return timestamp + randomNumber;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createSplitOrder(OrderVO order, List<OrderItemVO> newItems, boolean isSpiltStore, Integer shopId, boolean isSingleShopSingleVendor) {
        // 获取原始订单数据并移除不需要的字段 ,直接使用 OrderVO 会导致时间丢失
        Order waitSplitOrder = getById(order.getOrderId());
        Order newOrder = new Order();
        BeanUtils.copyProperties(waitSplitOrder, newOrder);
        //转换地址信息【JSON】
        String regionIds = "";
        if (CollUtil.isNotEmpty(order.getRegionIds())) {
            regionIds = JSONUtil.toJsonStr(order.getRegionIds());
        }
        newOrder.setRegionIds(regionIds);
        String regionNames = "";
        if (CollUtil.isNotEmpty(order.getRegionNames())) {
            regionNames = JSONUtil.toJsonStr(order.getRegionNames());
        }
        newOrder.setRegionNames(regionNames);
        newOrder.setOrderId(null);
        newOrder.setShopId(shopId);
        BigDecimal productAmount = BigDecimal.valueOf(0);
        BigDecimal totalAmount;

        // 计算商品总金额
        for (OrderItemVO item : newItems) {
            item.setItemId(null);
            newOrder.setVendorId(item.getVendorId());
            productAmount = productAmount.add(new BigDecimal(item.getQuantity()).multiply(item.getOriginPrice()));
        }
        newOrder.setProductAmount(productAmount);
        totalAmount = productAmount;

        // 检查订单编号是否已存在
        newOrder.setOrderSn(creatNewOrderSn());

        // 设置父订单ID和父订单编号
        Integer parentOrderId = order.getParentOrderId() > 0 ? order.getParentOrderId() : order.getOrderId();
        String parentOrderSn = StringUtils.isNotEmpty(order.getParentOrderSn()) ? order.getParentOrderSn() : order.getOrderSn();
        newOrder.setParentOrderId(parentOrderId);
        newOrder.setParentOrderSn(parentOrderSn);

        // 获取订单金额详情和优惠券详情
        OrderAmountDetail orderAmountDetail = orderAmountDetailService.getOne(
                Wrappers.lambdaQuery(OrderAmountDetail.class)
                        .eq(OrderAmountDetail::getShopId, shopId)
                        .eq(OrderAmountDetail::getOrderId, parentOrderId)
        );
        OrderCouponDetail orderCouponDetail = orderCouponDetailService.getOne(
                Wrappers.lambdaQuery(OrderCouponDetail.class)
                        .eq(OrderCouponDetail::getShopId, shopId)
                        .eq(OrderCouponDetail::getOrderId, parentOrderId)
        );
        if (isSpiltStore) {
            newOrder.setIsStoreSplited(1);
        }

        if (isSpiltStore && isSingleShopSingleVendor) {
            // 取主单里当前店铺的amount信息
            //Map extensionData = JSONUtil.parse(order.getOrderExtension()).toBean(Map.class);
            Map<String, Object> extensionData = JSONUtil.parseObj(order.getOrderExtension());
            // 优惠券金额（按店铺）
            BigDecimal couponAmount = Optional.ofNullable(orderAmountDetail).map(OrderAmountDetail::getCouponAmount).orElse(BigDecimal.valueOf(0.0));
            newOrder.setCouponAmount(couponAmount);
            totalAmount = totalAmount.subtract(couponAmount);

            // 运费（按店铺）
            BigDecimal shippingFee = Optional.ofNullable(orderAmountDetail).map(OrderAmountDetail::getShippingFee).orElse(BigDecimal.valueOf(0.0));
            newOrder.setShippingFee(shippingFee);
            totalAmount = totalAmount.add(shippingFee);

            // 优惠/折扣（按店铺）
            BigDecimal discountAmount = Optional.ofNullable(orderAmountDetail).map(OrderAmountDetail::getDiscountAmount).orElse(BigDecimal.valueOf(0.0));
            newOrder.setDiscountAmount(discountAmount);
            totalAmount = totalAmount.subtract(discountAmount);

            // 配送类型
            Map<String, Object> shippingType = (Map<String, Object>) extensionData.getOrDefault("shipping_type", Collections.emptyMap());
            newOrder.setShippingTypeId((Integer) Optional.ofNullable(shippingType.get(String.valueOf(shopId))).map(map -> ((Map<String, Object>) map).get("type_id")).orElse(0));
            newOrder.setShippingTypeName((String) Optional.ofNullable(shippingType.get(String.valueOf(shopId))).map(map -> ((Map<String, Object>) map).get("type_name")).orElse(""));
        } else {
            // 优惠券金额（平摊）
            BigDecimal couponAmount = newOrder.getCouponAmount().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getProductAmount(), productAmount, newOrder.getCouponAmount()) : BigDecimal.valueOf(0);
            newOrder.setCouponAmount(couponAmount);
            totalAmount = totalAmount.subtract(couponAmount);

            // 运费（平摊）
            BigDecimal shippingFee = newOrder.getShippingFee().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getProductAmount(), productAmount, newOrder.getShippingFee()) : BigDecimal.valueOf(0);
            newOrder.setShippingFee(shippingFee);
            totalAmount = totalAmount.add(shippingFee);

            // 优惠/折扣（平摊）
            BigDecimal discountAmount = newOrder.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getProductAmount(), productAmount, newOrder.getDiscountAmount()) : BigDecimal.valueOf(0);
            newOrder.setDiscountAmount(discountAmount);
            totalAmount = totalAmount.subtract(discountAmount);
        }

        // 积分抵扣（平摊）
        BigDecimal pointsAmount = newOrder.getPointsAmount().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getProductAmount(), productAmount, newOrder.getPointsAmount()) : BigDecimal.valueOf(0);
        newOrder.setPointsAmount(pointsAmount);

        // 手续费（平摊）
        BigDecimal serviceFee = newOrder.getServiceFee().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getProductAmount(), productAmount, newOrder.getServiceFee()) : BigDecimal.valueOf(0);
        newOrder.setServiceFee(serviceFee);

        // 发票费用（平摊）
        BigDecimal invoiceFee = newOrder.getInvoiceFee().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getProductAmount(), productAmount, newOrder.getInvoiceFee()) : BigDecimal.valueOf(0);
        newOrder.setInvoiceFee(invoiceFee);

        // 使用的余额（平摊）
        BigDecimal balance = newOrder.getBalance().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getTotalAmount(), totalAmount, newOrder.getBalance()) : BigDecimal.valueOf(0);
        newOrder.setBalance(balance);

        // 线上支付金额（平摊）
        BigDecimal onlinePaidAmount = newOrder.getOnlinePaidAmount().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getProductAmount(), productAmount, newOrder.getOnlinePaidAmount()) : BigDecimal.valueOf(0);
        newOrder.setOnlinePaidAmount(onlinePaidAmount);

        // 线下支付金额（平摊）
        BigDecimal offlinePaidAmount = newOrder.getOfflinePaidAmount().compareTo(BigDecimal.ZERO) > 0 ? allocatedAmount(order.getProductAmount(), productAmount, newOrder.getOfflinePaidAmount()) : BigDecimal.valueOf(0);
        newOrder.setOfflinePaidAmount(offlinePaidAmount);

        // 保存新订单
        // 为解决region_ids报错问题，先默认给一个地址
        if (StrUtil.isNotBlank(newOrder.getRegionIds())) {
            newOrder.setRegionIds("[110000,110100,110101]");
        }
        this.save(newOrder);

        // 添加订单商品
        for (OrderItemVO item : newItems) {
            item.setOrderId(newOrder.getOrderId());
        }
        orderItemService.saveBatch(newItems.stream().map(item -> {
            OrderItem orderItem = new OrderItem();
            String skuData = "";
            if (CollUtil.isNotEmpty(item.getSkuData())) {
                skuData = JSONUtil.toJsonStr(item.getSkuData());
            }
            orderItem.setSkuData(skuData);
            BeanUtils.copyProperties(item, orderItem);
            if (orderItem.getVendorProductSkuId() != null && orderItem.getVendorProductSkuId() != 0) {
                orderItem.setVendorProductSupplyPrice(vendorProductSkuService.getById(orderItem.getVendorProductSkuId()).getSupplyPrice());
            }
            return orderItem;
        }).collect(Collectors.toList()));

        // 更新新订单的金额
        this.updateOrderMoney(newOrder);

        if (orderAmountDetail != null) {
            orderAmountDetail.setOrderDiscountDetailId(null);
            orderAmountDetail.setOrderId(newOrder.getOrderId());
            orderAmountDetailService.save(orderAmountDetail);
        }

        if (orderCouponDetail != null) {
            orderCouponDetail.setOrderCouponDetailId(null);
            orderCouponDetail.setOrderId(newOrder.getOrderId());
            orderCouponDetailService.save(orderCouponDetail);
        }

//        if (isSpiltStore) {
//            // 处理店铺订单已拆分后的操作，比如发票申请等
//        }

        // 添加拆份记录
        orderSpitLogService.addSplitLog(order.getOrderId(), newOrder.getOrderId(), order);
        return newOrder;

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateOrderMoney(Order order) {

        // 订单总金额
        BigDecimal totalAmount = order.getProductAmount()
                .add(order.getServiceFee())
                .add(order.getShippingFee())
                .add(order.getInvoiceFee())
                .subtract(order.getPointsAmount())
                .subtract(order.getDiscountAmount())
                .subtract(order.getCouponAmount());
        order.setTotalAmount(totalAmount);

        // 已付款金额
        BigDecimal paidAmount = order.getBalance()
                .add(order.getOnlinePaidAmount())
                .add(order.getOfflinePaidAmount());
        order.setPaidAmount(paidAmount);

        // 未付款金额
        BigDecimal unpaidAmount = totalAmount.subtract(paidAmount).max(BigDecimal.ZERO);
        order.setUnpaidAmount(unpaidAmount);

        // 未退款金额
        BigDecimal unrefundAmount = paidAmount.subtract(totalAmount).max(BigDecimal.ZERO);
        order.setUnrefundAmount(unrefundAmount);

        // 更新金额
        this.updateById(order);

        // 如果未支付，且未付款金额为0，则更新订单状态
        if (unpaidAmount.compareTo(BigDecimal.ZERO) <= 0 &&
                order.getOrderStatus().equals(PENDING.getCode()) &&
                order.getPayStatus().equals(PaymentStatus.UNPAID.getCode())) {
            this.setOrderPaid(order.getOrderId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setOrderPaid(Integer id) {
        //判断能否支付
        Order order = this.getById(id);
        Order updateOrder = new Order();
        updateOrder.setOrderId(id);
        updateOrder.setPayStatus(PaymentStatus.PAID.getCode());
        updateOrder.setPayTime(StringUtils.getCurrentTime());
        if (order.getOrderStatus() == PENDING.getCode()) {
            updateOrder.setOrderStatus(OrderStatusEnum.CONFIRMED.getCode());
        }
        if (this.updateById(updateOrder)) {
            orderLogService.quickCreate("订单支付", id, order.getOrderSn());
            this.orderSuccessPoint(order.getTotalAmount(), order.getUserId(), id);

            this.orderECard(order);
        }
        splitStoreOrder(id);

        TransactionAfterCommitExecutor.execute(() -> {
            try {
                if (this.lambdaQuery().eq(Order::getOrderId, id).count() > 0) {
                    batchSaveStatementOutputParam(order);
                    OrderPickupShipment orderPickupShipment = new OrderPickupShipment(order.getOrderId(), order.getShopId());
                    orderPickupShipmentService.saveOrderPickupShipment(orderPickupShipment);
                }
                List<Order> list = list(new LambdaQueryWrapper<Order>().eq(Order::getOrderId, id).or().eq(Order::getParentOrderId, id));
                for (Order o : list) {
                    // 添加当前订单的费率
                    recordRateService.saveRecordRate(o.getOrderId(), StatementType.ORDER.getCode(),
                            o.getShopId() != null ? o.getShopId() : 0,
                            o.getVendorId() != null ? o.getVendorId() : 0);
                    // 更具当前店铺获取打印机信息
                    Print print = printMapper.selectOne(new LambdaQueryWrapper<Print>()
                            .eq(Print::getShopId, o.getShopId())
                            .eq(Print::getStatus, PrintStatusEnum.ENABLED.getCode())
                    );

                    messageService.sendUserMessage(o.getUserId(), o.getOrderId(), MessageTypeEnum.ORDER_PAY);

                    if (print != null && print.getAutoPrint() == 1) {
                        PrintOrderDTO printOrderDTO = PrintOrderDTO.builder()
                                .print(print)
                                .orderId(o.getOrderId())
                                .build();
                        // 发送到RabbitMQ队列
                        rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE,
                                RabbitMQConfig.PRINT_ORDER_ROUTING_KEY, printOrderDTO);
                    }
                    if (o.getParentOrderId() != null && o.getParentOrderId() > 0) {
                        // 批量保存对账单输出参数
                        batchSaveStatementOutputParam(o);
                        OrderPickupShipment orderPickupShipment = new OrderPickupShipment(o.getOrderId(), o.getShopId());
                        orderPickupShipmentService.saveOrderPickupShipment(orderPickupShipment);
                    }
                }
            } catch (Exception e) {
                log.error("打印小票", e);
            }
        });
    }

    private void orderECard(Order order) {
        if (order.getOrderType() == OrderType.CARD.getCode()) {
            List<OrderItem> orderItems = orderItemService.lambdaQuery().eq(OrderItem::getOrderId, order.getOrderId()).list();
            for (OrderItem orderItem : orderItems) {
                if (StrUtil.isNotBlank(orderItem.getCardGroupName())) {
                    Product product = productService.getById(orderItem.getProductId());

                    List<ECard> eCards = eCardService.getNewCardByGroupId(product.getCardGroupId(), orderItem.getQuantity());
                    Assert.isFalse(eCards.size() < orderItem.getQuantity(), () -> new GlobalException(translatePackage.translate("卡券库存不足")));


                    eCards.forEach(eCard -> {
                        eCard.setIsUse(1);
                        eCard.setOrderId(orderItem.getOrderId());
                        eCard.setOrderItemId(orderItem.getItemId());
                    });

                    eCardService.updateBatchById(eCards);
                }
            }
        }
    }

    /**
     * 批量保存对账单输出参数
     *
     * @param order 订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveStatementOutputParam(Order order) {
        // 下单出账，添加流水单
        List<StatementOutputSaveParam> statementOutputSaveParamList = new ArrayList<>();

        RecordRate recordRate = recordRateService.lambdaQuery()
                .eq(RecordRate::getRecordId, order.getOrderId())
                .eq(RecordRate::getRecordType, StatementType.ORDER.getCode())
                .orderByDesc(RecordRate::getRecordRateId)
                .last("limit 1")
                .one();
        // 供应商获订单金额和店铺减余额
        boolean isVendorOrder = order.getVendorId() != null && order.getVendorId() > 0;
        if (isVendorOrder) {
            List<OrderItem> orderItems = orderItemService.list(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getOrderId()));
            BigDecimal totalSupplyPrice = orderItems.stream()
                    .map(item -> item.getVendorProductSupplyPrice().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (order.getShopId() > 0) {
                // ①店铺订单加入流水
                BigDecimal amount = order.getPaidAmount().subtract(totalSupplyPrice);
                StatementOutputSaveParam shopAddStatementOutput = new StatementOutputSaveParam(order, StatementType.ORDER.getCode(), amount, true, true);
                statementOutputSaveParamList.add(shopAddStatementOutput);
                // ③ 店铺减去服务费
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal serviceFee = amount.multiply(recordRate.getShopServiceFee())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    StatementOutputSaveParam shopSubServiceStatementOutput = new StatementOutputSaveParam(order, StatementType.SERVICE_FEE.getCode(), serviceFee, true, false);
                    statementOutputSaveParamList.add(shopSubServiceStatementOutput);
                }
            }
            // ②供应商加入订单流水
            StatementOutputSaveParam vendorAddStatementOutput = new StatementOutputSaveParam(order, StatementType.ORDER.getCode(), totalSupplyPrice, false, true);
            statementOutputSaveParamList.add(vendorAddStatementOutput);
            // ④ 供应商减去服务费
            BigDecimal serviceFee2 = totalSupplyPrice.multiply(recordRate.getSupplierServiceFee())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            StatementOutputSaveParam vendorSubServiceStatementOutput = new StatementOutputSaveParam(order, StatementType.SERVICE_FEE.getCode(), serviceFee2, false, false);
            statementOutputSaveParamList.add(vendorSubServiceStatementOutput);
        } else {
            // ①店铺订单加入流水
            StatementOutputSaveParam shopAddStatementOutput = new StatementOutputSaveParam(order, StatementType.ORDER.getCode(), order.getPaidAmount(), true, true);
            statementOutputSaveParamList.add(shopAddStatementOutput);
            // ② 店铺减去服务费
            if (order.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal serviceFee = order.getPaidAmount().multiply(recordRate.getShopServiceFee())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                StatementOutputSaveParam shopServiceStatementOutput = new StatementOutputSaveParam(order, StatementType.SERVICE_FEE.getCode(), serviceFee, true, false);
                statementOutputSaveParamList.add(shopServiceStatementOutput);
            }
        }

        // 批量保存出账单
        statementOutputService.batchSaveStatementOutput(statementOutputSaveParamList);
    }

    private void orderSuccessPoint(BigDecimal orderMoney, Integer userId, Integer id) {
        BigDecimal points = this.getOrderSendPoint(orderMoney);
        List<UserRank> ranksList = userRankService.list(Wrappers.emptyWrapper());
        User user = userMapper.selectById(userId);
        Integer userRankId = user.getRankId();

        BigDecimal rankPoint = BigDecimal.ONE;
        for (UserRank userRank : ranksList) {
            if (Objects.equals(userRankId, userRank.getRankId())) {
                rankPoint = new BigDecimal(userRank.getRankPoint());
            }
        }

        points = points.multiply(rankPoint).setScale(0, RoundingMode.UP);

        String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
        if (points.compareTo(BigDecimal.ZERO) > 0) {
            userPointsLogService.incPoints(points.intValue(), userId, "下单送" + configVal, id);
        }

    }

    private BigDecimal getOrderSendPoint(BigDecimal orderMoney) {
        String orderSendPointVal = configService.getConfigVal(SettingsEnum.ORDER_SEND_POINT);

        BigDecimal orderSendPoint = StrUtil.isNotBlank(orderSendPointVal) ? new BigDecimal(orderSendPointVal) : BigDecimal.ZERO;

        BigDecimal points = BigDecimal.ZERO;
        if (orderSendPoint.compareTo(BigDecimal.ZERO) > 0) {
            points = orderMoney.multiply(orderSendPoint).setScale(0, RoundingMode.UP);
        }
        return points;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Integer id) {
        Integer userId = 0;
        return this.cancelOrder(id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Integer id, Integer userId) {
        OrderVO orderVO = this.detail(id, userId);
        if (!orderVO.getAvailableActions().getCancelOrder()) {
            throw new GlobalException(ORDER_STATUS_ERROR);
        }
        orderLogService.quickCreate(ORDER_CANCEL, id, orderVO.getOrderSn());

        if (orderVO.getUsePoints() > 0) {
            String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
            userPointsLogService.incPoints(orderVO.getUsePoints(), orderVO.getUserId(), "订单取消返还" + (StrUtil.isNotBlank(configVal) ? configVal : "积分"), id);
        }
        //使用了余额返余额
        if (orderVO.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            userBalanceService.incBalance(orderVO.getBalance(), orderVO.getUserId(), "订单取消返还余额");
        }
        if (orderVO.getCouponAmount().compareTo(BigDecimal.ZERO) > 0) {
            //通过userId和orderId更新数据
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUsedTime(0L);
            userCoupon.setOrderId(0);
            userCouponMapper.update(userCoupon,
                    Wrappers.lambdaQuery(UserCoupon.class)
                            .eq(UserCoupon::getOrderId, orderVO.getOrderId())
                            .eq(UserCoupon::getUserId, orderVO.getUserId()));
        }
        for (OrderItemVO item : orderVO.getItems()) {

            if (item.getVendorId() != null && item.getVendorId() > 0) {
                //供应商商品减库存
                vendorProductSkuService.incStockAndSalesVolume(new VendorProductSkuStockParam(Long.valueOf(item.getVendorProductSkuId()), item.getQuantity(), VendorProductSkuStockBizEnum.ORDER_CANCEL, "取消订单加库存"));
            } else {
                if (item.getSkuId() > 0) {
                    handleSkuStock(item);
                } else {
                    handleProductStock(item);
                }
                // O2O 模式下增加门店销量
                if (tigshopProperties.getIsO2o() == 1) {
                    decShopSales(item);
                }
            }

            //减少销量
            Product product = productService.getById(item.getProductId());
            if (product.getVirtualSales() != null && product.getVirtualSales() < item.getQuantity()) {
                productService.lambdaUpdate().eq(Product::getProductId, item.getProductId()).setDecrBy(Product::getVirtualSales, product.getVirtualSales()).update();
            } else {
                productService.lambdaUpdate().eq(Product::getProductId, item.getProductId()).setDecrBy(Product::getVirtualSales, item.getQuantity()).update();
            }
            //秒杀品减少销量
            seckillService.decSales(item.getProductId(), item.getSkuId(), item.getQuantity());
            //秒杀返回库存
            seckillService.incStock(item.getProductId(), item.getSkuId(), item.getQuantity());
        }
        Order order = new Order();
        order.setOrderId(id);
        order.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
        this.updateById(order);
        // 发送后台信息 -- 取消订单
        Order byId = this.getById(id);
        AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
        adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.ORDER_CANCELED.getCatId());
        adminMsgCreateDTO.setTitle("订单已被取消：" + byId.getOrderSn() + ",金额：" + byId.getTotalAmount());
        adminMsgCreateDTO.setContent("订单编号：" + byId.getOrderSn() + "，订单金额：" + byId.getTotalAmount() + "元，订单取消。");
        adminMsgCreateDTO.setShopId(byId.getShopId());
        adminMsgCreateDTO.setOrderId(byId.getOrderId());
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("orderId", byId.getOrderId());
        adminMsgCreateDTO.setRelatedData(relatedData);
        adminMsgService.createMessage(adminMsgCreateDTO);
        if (byId.getVendorId() != null && byId.getVendorId() > 0) {
            adminMsgCreateDTO.setVendorId(byId.getVendorId());
            adminMsgService.createMessage(adminMsgCreateDTO);
        }
        return true;
    }


    /**
     * 处理 SKU 商品库存扣减
     */
    private void handleSkuStock(OrderItemVO orderItem) {
        if (tigshopProperties.getIsO2o() != 1) {
            incStockSkuAndProduct(orderItem);
            return;
        }

        Product product = productService.getById(orderItem.getProductId());
        if (!Objects.equals(product.getShopId(), orderItem.getShopId())) {
            handleCrossStoreSkuStock(orderItem);
        } else {
            incStockSkuAndProduct(orderItem);
        }
    }

    /**
     * 处理普通商品库存扣减
     */
    private void handleProductStock(OrderItemVO orderItem) {
        if (tigshopProperties.getIsO2o() != 1) {
            incStockProductOnly(orderItem);
            return;
        }

        Product product = productService.getById(orderItem.getProductId());
        if (!Objects.equals(product.getShopId(), orderItem.getShopId())) {
            handleCrossStoreProductStock(orderItem);
        } else {
            incStockProductOnly(orderItem);
        }
    }

    /**
     * 跨门店 SKU 商品逻辑
     */
    private void handleCrossStoreSkuStock(OrderItemVO orderItem) {
        ConfigPO soloStock = configService.getConfigByCode(SettingsEnum.STORE_USE_SOLO_PRODUCT_STOCK.getBizCode());
        ConfigPO totalStock = configService.getConfigByCode(SettingsEnum.STORE_USE_TOTAL_PRODUCT_STOCK.getBizCode());

        if ("1".equals(soloStock.getBizVal())) {
            StoreProduct storeProduct = storeProductService.decStock(orderItem.getShopId(), orderItem.getProductId(), orderItem.getQuantity());
            storeSkuService.decStock(storeProduct, orderItem.getShopId(), orderItem.getSkuId(), orderItem.getQuantity());
        }
        if ("1".equals(totalStock.getBizVal())) {
            incStockSkuAndProduct(orderItem);
        }
    }

    /**
     * 跨门店 普通商品逻辑
     */
    private void handleCrossStoreProductStock(OrderItemVO orderItem) {
        ConfigPO soloStock = configService.getConfigByCode(SettingsEnum.STORE_USE_SOLO_PRODUCT_STOCK.getBizCode());
        ConfigPO totalStock = configService.getConfigByCode(SettingsEnum.STORE_USE_TOTAL_PRODUCT_STOCK.getBizCode());

        if ("1".equals(soloStock.getBizVal())) {
            storeProductService.incStock(orderItem.getShopId(), orderItem.getProductId(), orderItem.getQuantity());
        }
        if ("1".equals(totalStock.getBizVal())) {
            incStockProductOnly(orderItem);
        }
    }

    /**
     * 增加 SKU + 商品库存
     */
    private void incStockSkuAndProduct(OrderItemVO orderItem) {
        productSkuService.incStock(orderItem.getSkuId(), orderItem.getQuantity());
        productService.incStock(orderItem.getProductId(), orderItem.getQuantity());
    }

    /**
     * 增加 商品库存
     */
    private void incStockProductOnly(OrderItemVO orderItem) {
        productService.incStock(orderItem.getProductId(), orderItem.getQuantity());
    }

    /**
     * 增加门店销量
     */
    private void decShopSales(OrderItemVO item) {
        if (item.getShopId() != null && item.getShopId() != 0) {
            try {
                // 减少门店销量
                Shop shop = shopMapper.selectById(item.getShopId());
                shop.setShopSales(shop.getShopSales() - item.getQuantity());
                shopMapper.updateById(shop);
            } catch (Exception e) {
                throw new GlobalException("减少门店销量失败");
            }
        }
    }


    @Override
    public boolean delOrder(Integer id) {
        Integer userId = 0;
        return this.delOrder(id, userId);
    }

    @Override
    public boolean delOrder(Integer id, Integer userId) {
        OrderVO orderVO = this.detail(id, userId);
        if (!orderVO.getAvailableActions().getDelOrder()) {
            throw new GlobalException("订单状态错误");
        }
        Order order = new Order();
        order.setOrderId(id);
        order.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setIsDel(DeleteType.DELETE.getCode());
        orderLogService.quickCreate("订单删除", id, orderVO.getOrderSn());
        return this.updateById(order);
    }

    @Override
    @Transactional
    public boolean modifyOrderMoney(OrderModifyMoneyDTO feeData) {
        OrderVO orderVO = this.detail(feeData.getId());
        if (!orderVO.getAvailableActions().getModifyOrderMoney()) {
            throw new GlobalException("订单状态错误");
        }
        Order order = new Order();
        order.setOrderId(feeData.getId());
        if (feeData.getShippingFee() != null) {
            order.setShippingFee(feeData.getShippingFee());
        }
        if (feeData.getInvoiceFee() != null) {
            order.setInvoiceFee(feeData.getInvoiceFee());
        }
        if (feeData.getServiceFee() != null) {
            order.setServiceFee(feeData.getServiceFee());
        }
        if (feeData.getDiscountAmount() != null) {
            order.setDiscountAmount(feeData.getDiscountAmount());
        }
        boolean b = this.updateById(order);
        Order orderSelect = this.getById(order.getOrderId());
        updateOrderMoney(orderSelect);
        orderLogService.quickCreate("订单金额修改", feeData.getId(), orderVO.getOrderSn());
        return b;
    }

    @Override
    public boolean modifyOrderConsignee(OrderModifyConsigneeDTO modifyConsigneeDTO) {
        OrderVO orderVO = this.detail(modifyConsigneeDTO.getId());
        if (!orderVO.getAvailableActions().getModifyOrderConsignee()) {
            throw new GlobalException("订单状态错误");
        }
        Order order = new Order();
        order.setOrderId(modifyConsigneeDTO.getId());
        order.setConsignee(modifyConsigneeDTO.getConsignee());
        order.setMobile(modifyConsigneeDTO.getMobile());
        order.setEmail(modifyConsigneeDTO.getEmail());
        order.setAddress(modifyConsigneeDTO.getAddress());
        order.setRegionIds(JSONUtil.toJsonStr(modifyConsigneeDTO.getRegionIds()));
        List<String> regionNames = regionService.getRegionNamesByRegionIds(modifyConsigneeDTO.getRegionIds());
        order.setRegionNames(JSONUtil.toJsonStr(regionNames));
        UserAddressVO userAddress = new UserAddressVO();
        userAddress.setAddress(modifyConsigneeDTO.getAddress());
        userAddress.setRegionIds(modifyConsigneeDTO.getRegionIds());
        userAddress.setPostcode(modifyConsigneeDTO.getPostcode());
        userAddress.setTelephone(modifyConsigneeDTO.getTelephone());
        userAddress.setMobile(modifyConsigneeDTO.getMobile());
        userAddress.setEmail(modifyConsigneeDTO.getEmail());
        userAddress.setRegionNames(regionNames);
        if (orderVO.getAddressData() != null) {
            userAddress.setAddressTag(orderVO.getAddressData().getAddressTag());
            userAddress.setUserId(orderVO.getAddressData().getUserId());
            userAddress.setAddressId(orderVO.getAddressData().getAddressId());
            userAddress.setIsDefault(orderVO.getAddressData().getIsDefault());
            userAddress.setIsSelected(orderVO.getAddressData().getIsSelected());
        }
        order.setAddressData(JSONUtil.toJsonStr(userAddress));
        orderLogService.quickCreate("修改收货人信息", orderVO.getOrderId(), orderVO.getOrderSn());
        return this.updateById(order);
    }

    @Override
    public boolean confirmReceipt(Integer orderId) {
        return this.confirmReceipt(orderId, null);

    }

    @Override
    public boolean confirmReceipt(Integer orderId, Integer userId) {
        OrderVO orderVO = this.detail(orderId, userId);
        Assert.isFalse(!orderVO.getAvailableActions().getConfirmReceipt(), () -> new GlobalException("订单状态错误"));

        // 对账单生成
        // statementOutputService.updateStatementOutput(orderVO.getOrderId(), StatementType.ORDER.getCode());
        String collectionTime = configService.getConfigVal(COLLECTION_TIME_SETTING);
        if (StringUtils.isEmpty(collectionTime)) {
            collectionTime = "10";
        }
        BigDecimal delayTime = new BigDecimal(collectionTime).multiply(new BigDecimal(24 * 60 * 60 * 1000));
        StatementSaveParam statementSaveParam = new StatementSaveParam();
        statementSaveParam.setAccountType(AccountType.ACCOUNT_BALANCE.getCode());
        statementSaveParam.setType(StatementType.ORDER.getCode());
        statementSaveParam.setEntryType(EntryType.AUTO.getCode());
        Paylog paylog = paylogMapper.selectOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getOrderId, orderVO.getOrderId()));
        if (paylog != null) {
            statementSaveParam.setPaymentType(paylog.getPayCode());
        } else {
            statementSaveParam.setPaymentType(PayMethodType.OTHER.getCode());
        }
        statementSaveParam.setShopId(orderVO.getShopId());
        statementSaveParam.setVendorId(orderVO.getVendorId());
        statementSaveParam.setRecordId(orderVO.getOrderId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELAY_EXCHANGE,
                RabbitMQConfig.GENERATE_STATEMENT_ROUTING_KEY,
                statementSaveParam,
                message -> {
                    message.getMessageProperties().setHeader("x-delay", delayTime.longValue());
                    return message;
                }
        );


        orderLogService.quickCreate("确认收货", orderId, orderVO.getOrderSn());
        //分销结算
        autoSettlement(orderVO);

        //店铺结算
        this.storeSettlement(orderVO.getOrderId());

        Order currentOrder = getById(orderId);
        //平台端出售供应商商品结算
        if (tigshopProperties.getIsVendor() == 1 && currentOrder.getVendorId() != null && currentOrder.getVendorId() > 0 && currentOrder.getShopId() == 0) {
            this.vendorSettlement(orderVO.getOrderId(), currentOrder.getVendorId(), 0);
        }

        // 创建管理员日志
        AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
        adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.ORDER_COMPLETED.getCatId());
        adminMsgCreateDTO.setTitle("订单已完成通知：" + orderVO.getOrderSn() + ",金额：" + orderVO.getTotalAmount());
        adminMsgCreateDTO.setContent("您有订单【" + orderVO.getOrderSn() + "】已完成,请注意查看。");
        adminMsgCreateDTO.setShopId(orderVO.getShopId());
        adminMsgCreateDTO.setOrderId(orderVO.getOrderId());

        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("orderId", orderVO.getOrderId());
        adminMsgCreateDTO.setRelatedData(relatedData);


        adminMsgService.createMessage(adminMsgCreateDTO);

        if (adminMsgCreateDTO.getShopId() != null && adminMsgCreateDTO.getShopId() > 0) {
            //如果是店铺订单则给平台订单也发一条
            adminMsgCreateDTO.setShopId(0);
            adminMsgService.createMessage(adminMsgCreateDTO);
        }

        if (orderVO.getVendorId() != null && orderVO.getVendorId() > 0) {
            //如果是供应商订单则给供应商订单也发一条
            adminMsgCreateDTO.setVendorId(orderVO.getVendorId());
            adminMsgService.createMessage(adminMsgCreateDTO);
        }

        // 更新订单状态
        Order order = new Order();
        order.setOrderId(orderId);
        order.setShippingStatus(ShippingStatusEnum.SHIPPED.getCode());
        order.setOrderStatus(OrderStatusEnum.COMPLETED.getCode());
        order.setReceivedTime(StringUtils.getCurrentTime());
        boolean b = this.updateById(order);

        // 订单成交，用于分销员等级升级
        List<SalesmanOrder> salesmanOrders = salesmanOrderMapper.selectList(new LambdaQueryWrapper<SalesmanOrder>().eq(SalesmanOrder::getOrderId, orderId));
        salesmanOrders.forEach(salesmanOrder -> {
            if (salesmanOrder != null) {
                // 分销员分销订单
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.DIRECT_EXCHANGE,
                        RabbitMQConfig.SALESMAN_UPDATE_ROUTING_KEY,
                        salesmanOrder.getSalesmanId());
            }
        });
        Salesman salesman = salesmanMapper.selectOne(new LambdaQueryWrapper<Salesman>().eq(Salesman::getUserId, orderVO.getUserId()));
        if (salesman != null) {
            // 分销员自购订单
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DIRECT_EXCHANGE,
                    RabbitMQConfig.SALESMAN_UPDATE_ROUTING_KEY,
                    salesman.getSalesmanId());
        }
        // 增加客户成长值，升级客户等级
        userRankService.getRankGrowth(orderVO.getUserId());
        return b;
    }


    private void autoSettlement(OrderVO orderVO) {
        // 查询结算设置
        SalesmanConfig salesmanConfig = salesmanConfigMapper.selectOne(new LambdaQueryWrapper<SalesmanConfig>()
                .eq(SalesmanConfig::getCode, SalesmanConfigTypeEnum.SALESMAN_SETTLEMENT.getCode()));
        if (salesmanConfig == null) {
            return;
        }

        JSONObject jsonObject = JSONUtil.parseObj(salesmanConfig.getData());
        // 1.自动结算
        if (jsonObject.containsKey("settlementType") && jsonObject.getInt("settlementType") == 1) {
            if (jsonObject.containsKey("dateType") && jsonObject.getInt("dateType") == 0) {
                // 延时15天结算
                BigDecimal delayTime = new BigDecimal(15).multiply(new BigDecimal(24 * 60 * 60 * 1000));
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.DELAY_EXCHANGE,
                        RabbitMQConfig.SALESMAN_SETTLEMENT_ROUTING_KEY,
                        orderVO.getOrderId(),
                        message -> {
                            message.getMessageProperties().setHeader("x-delay", delayTime.longValue());
                            return message;
                        }
                );
            } else {
                // 收货立即结算
                settlement(orderVO.getOrderId(), 1);
            }
        }
        // 2.手动结算
        if (jsonObject.containsKey("settlementType") && jsonObject.getInt("settlementType") == 2) {
            settlement(orderVO.getOrderId(), 2);
        }
    }

    @Override
    public void settlement(Integer orderId, Integer settlementType) {
        List<SalesmanOrder> salesmanOrders = salesmanOrderMapper.selectList(new LambdaQueryWrapper<SalesmanOrder>().eq(SalesmanOrder::getOrderId, orderId));
        for (SalesmanOrder salesmanOrder : salesmanOrders) {
            OrderVO orderVO = this.detail(orderId);
            if (orderVO == null) {
                return;
            }
            if (salesmanOrder != null) {
                if (salesmanOrder.getStatus() != 0) {
                    return;
                }
                salesmanOrder.setStatus(1);
                salesmanOrder.setSettlementTime(StringUtils.getCurrentTime());
                salesmanOrderMapper.updateById(salesmanOrder);
                Salesman salesman = salesmanMapper.selectById(salesmanOrder.getSalesmanId());
                if (salesman != null) {
                    UpdateWrapper<Salesman> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("salesman_id", salesman.getSalesmanId())
                            .setSql("sale_amount = sale_amount + " + salesmanOrder.getOrderAmount());
                    salesmanMapper.update(null, updateWrapper);
                    if (settlementType == 1) {
                        // 自动结算添加到余额中
                        userBalanceService.incBalance(salesmanOrder.getAmount(), salesman.getUserId(), "订单" + orderVO.getOrderSn() + "结算佣金");
                    }
                }
            }
        }
    }


    private void storeSettlement(Integer orderId) {
        String collectionTimeSetting = configService.getConfigVal(COLLECTION_TIME_SETTING);
        int useDays = StrUtil.isNotBlank(collectionTimeSetting) ? Integer.parseInt(collectionTimeSetting) : 0;
        BigDecimal delayTime = new BigDecimal(useDays).multiply(new BigDecimal(24 * 60 * 60 * 1000)).add(new BigDecimal(3000));
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELAY_EXCHANGE,
                RabbitMQConfig.STORE_SETTLEMENT_ROUTING_KEY,
                orderId,
                message -> {
                    message.getMessageProperties().setHeader("x-delay", delayTime.longValue());
                    return message;
                }
        );

    }

    @Override
    public void vendorSettlement(Integer orderId, Integer vendorId, Integer shopId) {
        int useDays = 0;
        if (shopId == 0) {
            String configVal = configService.getConfigVal(SettingsEnum.COLLECTION_TIME_SETTING);
            useDays = StrUtil.isNotBlank(configVal) ? Integer.parseInt(configVal) : 0;
        }
        BigDecimal delayTime = new BigDecimal(useDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DIRECT_EXCHANGE,
                RabbitMQConfig.VENDOR_SETTLEMENT_ROUTING_KEY,
                orderId,
                message -> {
                    message.getMessageProperties().setHeader("x-delay", delayTime.longValue());
                    return message;
                }
        );
    }

    @Override
    public boolean modifyOrderShipping(OrderModifyShippingDTO modifyShippingDTO) {
        OrderVO orderVO = this.detail(modifyShippingDTO.getId());
        if (!orderVO.getAvailableActions().getModifyShippingInfo()) {
            throw new GlobalException("订单状态错误");
        }
        Order order = new Order();
        order.setOrderId(modifyShippingDTO.getId());
        order.setShippingMethod(modifyShippingDTO.getShippingMethod());

        if (modifyShippingDTO.getShippingMethod() == 1) {
            order.setLogisticsId(modifyShippingDTO.getLogisticsId());
            LogisticsCompany logisticsCompany = logisticsCompanyService.getById(modifyShippingDTO.getLogisticsId());
            if (logisticsCompany != null) {
                order.setLogisticsName(logisticsCompany.getLogisticsName());
            }
            order.setTrackingNo(modifyShippingDTO.getTrackingNo());
        } else if (modifyShippingDTO.getShippingMethod() == 2) {
            order.setLogisticsId(0);
            order.setTrackingNo("");
            order.setLogisticsName("商家配送");
        } else if (modifyShippingDTO.getShippingMethod() == 3) {
            order.setLogisticsId(0);
            order.setTrackingNo("");
            order.setLogisticsName("无需配送");
        } else if (modifyShippingDTO.getShippingMethod() == 4) {
            // 电子面单
            order.setLogisticsId(modifyShippingDTO.getLogisticsId());
            LogisticsCompany logisticsCompany = logisticsCompanyService.getById(modifyShippingDTO.getLogisticsId());
            if (logisticsCompany != null) {
                order.setLogisticsName(logisticsCompany.getLogisticsName());
            }
            order.setTrackingNo("");
        }
        boolean result = this.updateById(order);
        if (result) {
            orderLogService.quickCreate("修改配送信息", modifyShippingDTO.getId(), orderVO.getOrderSn());
        }
        return false;
    }

    @Override
    public ShippingResult getShippingInfo(Integer id) {
        Order order = this.getById(id);
        if (order == null || order.getTrackingNo() == null || StringUtils.isEmpty(order.getTrackingNo())) {
            return null;
        }
        // 获取物流公司
        LogisticsCompany logisticsCompany = logisticsCompanyMapper.selectById(order.getLogisticsId());
        // SF（顺分）、KYSY（跨越速运） 需要填写 CustomerName 手机号后4为
        String customerName = "";
        if (ObjectUtil.equals(logisticsCompany.getLogisticsCode(), "SF") || ObjectUtil.equals(logisticsCompany.getLogisticsCode(), "KYSY") || ObjectUtil.equals(logisticsCompany.getLogisticsCode(), "ZTO")) {
            customerName = StrUtil.subSuf(order.getMobile(), order.getMobile().length() - 4);
        }
        // 组装应用级参数
        String requestData = "{" +
                "'ShipperCode': '" + logisticsCompany.getLogisticsCode() + "'," +
                "'LogisticCode': '" + order.getTrackingNo() + "'," +
                "'CustomerName': '" + customerName + "'" +
                "}";
        // 组装系统级参数
        String apiKey = configService.getConfigByCode(SettingsEnum.KDNIAO_API_KEY.getBizCode()).getBizVal();
        String kdnBusinessId = configService.getConfigByCode(SettingsEnum.KDNIAO_BUSINESS_ID.getBizCode()).getBizVal();
        String dataSign = Base64.encode(SecureUtil.md5(requestData + apiKey), "UTF-8");
        // 调用 http 获取快递信息
        String shippingInfo = shippingInfoClient.getShippingInfo(URLEncoder.encode(requestData, StandardCharsets.UTF_8),
                kdnBusinessId,
                KuaiDiNiaoTypes.KDNIAO_JSCX.getCode(),
                URLEncoder.encode(dataSign, StandardCharsets.UTF_8),
                "2");
        ShippingResult bean = JSONUtil.toBean(shippingInfo, JSONConfig.create().setIgnoreCase(true), ShippingResult.class);
        // 如果返回失败则返回null
        if (ObjectUtil.equals(bean.getSuccess(), "false")) {
            return null;
        }
        return bean;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchOperation(Integer id, String type, BatchDeliverData data) {
        if ("deliver".equals(type)) {
            OrderVO orderVO = this.detail(id);
            if (!orderVO.getAvailableActions().getDeliver()) {
                throw new GlobalException("状态错误");
            }
            if (data == null) {
                throw new GlobalException("请选择发货信息");
            }
            String trackingNo = "";
            List<DeliverInfo> deliverinfo = List.of();
            for (DeliverDataList deliverDataListList : data.getDeliverData()) {
                if (orderVO.getOrderId().equals(deliverDataListList.getId())) {
                    deliverinfo = deliverDataListList.getDeliverInfo();
                    trackingNo = deliverDataListList.getTrackingNo();
                    break;
                }
            }
            DeliverOrderParam param = DeliverOrderParam.builder()
                    .id(id)
                    .deliverData(deliverinfo)
                    .shippingMethod(data.getShippingMethod())
                    .logisticsId(data.getLogisticsId())
                    .trackingNo(trackingNo)
                    .build();
            this.deliverOrder(param);
        }
    }

    @Override
    public List<OrderVO> printSeveral(List<Integer> ids) {
        List<OrderVO> orderPrint = new ArrayList<>();
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        // 构建URL
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        // 如果端口不是默认的80或443，则添加端口号
        if (!(serverPort == 80 && "http".equals(scheme)) && !(serverPort == 443 && "https".equals(scheme))) {
            url.append(":").append(serverPort);
        }
        Date date = DateUtil.date(System.currentTimeMillis());
        for (Integer id : ids) {
            OrderVO orderVO = this.detail(id);
            orderVO.setHost(url.toString());
            orderVO.setPrintTime(String.valueOf(date));
            orderPrint.add(orderVO);
        }
        return orderPrint;
    }

    @Transactional
    @Override
    public void deliverOrder(DeliverOrderParam param) {
        OrderVO orderVO = this.detail(param.getId());
        Assert.notNull(orderVO, () -> new GlobalException("未知的订单"));
        Assert.isTrue(orderVO.getAvailableActions().getDeliver(), () -> new GlobalException("状态错误"));

        // 查询订单信息
        List<Aftersales> aftersalesList = aftersalesService.lambdaQuery().eq(Aftersales::getOrderId, param.getId()).list();

        Set<Integer> aftersalesingStatus = Stream.of(AftersalesStatusEnum.IN_REVIEW, AftersalesStatusEnum.SEND_BACK, AftersalesStatusEnum.RETURNED)
                .map(AftersalesStatusEnum::getCode)
                .collect(Collectors.toSet());
        boolean hasAftersales = aftersalesList.stream().map(Aftersales::getStatus).anyMatch(aftersalesingStatus::contains);
        Assert.isFalse(hasAftersales, () -> new GlobalException("该订单存在售后，无法发货"));

        List<AftersalesItem> aftersalesItems = new ArrayList<>();
        if (CollUtil.isNotEmpty(aftersalesList)) {
            List<Integer> aftersalesIds = aftersalesList.stream().map(Aftersales::getAftersaleId).toList();
            aftersalesItems = aftersalesItemService.lambdaQuery().in(AftersalesItem::getAftersaleId, aftersalesIds).list();
        }

        List<OrderItemVO> orderItems = orderVO.getItems();
        List<OrderItemVO> remainItems = param.getRemainItems(orderItems, aftersalesItems);
        List<OrderItemVO> deliverItems = param.getDeliverItems(orderItems, aftersalesItems);

        // 3. 拆分订单逻辑（如果有部分发货或多仓库）
        Integer deliverOrderId = param.getId();
        if (CollUtil.isNotEmpty(remainItems)) {
            // 创建发货订单
            List<OrderItemVO> newOrderItems = BeanUtil.copyToList(deliverItems, OrderItemVO.class);
            Order deliverOrder = this.createSplitOrder(orderVO, newOrderItems, false, orderVO.getShopId(), false);
            deliverOrderId = deliverOrder.getOrderId();

            // 将原订单的售后记录迁移到发货订单
            if (CollUtil.isNotEmpty(aftersalesList)) {
                Integer finalDeliverOrderId = deliverOrderId;
                aftersalesList.forEach(aftersales -> aftersales.setOrderId(finalDeliverOrderId));
                aftersalesService.updateBatchById(aftersalesList);

                List<OrderItem> items = orderItemService.lambdaQuery().eq(OrderItem::getOrderId, deliverOrderId).list();
                Map<Integer, Integer> newOrderItemMap = items.stream().collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getItemId));
                Map<Integer, Integer> itemIdMap = deliverItems.stream().collect(Collectors.toMap(OrderItemVO::getItemId, item -> newOrderItemMap.get(item.getProductId())));

                List<AftersalesItem> delAftersalesItems = aftersalesItems.stream()
                        .filter(item -> itemIdMap.get(item.getOrderItemId()) == null || item.getNumber() == 0)
                        .toList();

                List<AftersalesItem> updateAftersalesItems = aftersalesItems.stream()
                        .filter(item -> itemIdMap.get(item.getOrderItemId()) != null && item.getNumber() > 0)
                        .peek(item -> {
                            Integer newOrderItemId = itemIdMap.get(item.getOrderItemId());
                            item.setOrderItemId(newOrderItemId);
                        })
                        .collect(Collectors.toList());

                aftersalesItemService.updateBatchById(updateAftersalesItems);
                aftersalesItemService.removeByIds(delAftersalesItems);
            }

            this.createSplitOrder(orderVO, remainItems, false, orderVO.getShopId(), false);

            // 记录拆单日志
            orderLogService.quickCreate("订单商品来自不同店铺，已拆分", orderVO.getOrderId(), orderVO.getOrderSn());
            // 删除原订单
            this.removeById(orderVO.getOrderId());
            orderItemService.remove(new QueryWrapper<OrderItem>().eq("order_id", orderVO.getOrderId()));
        }

        // 4. 更新订单状态
        Order order = new Order();
        order.setOrderId(deliverOrderId);
        order.setShippingStatus(ShippingStatusEnum.SENT.getCode());
        order.setShippingMethod(param.getShippingMethod());
        order.setShippingTime(StringUtils.getCurrentTime());
        order.setOrderStatus(OrderStatusEnum.PROCESSING.getCode());
        order.setOrderSource(orderVO.getOrderSource());
        if (param.getShippingMethod() == 1) {
            order.setLogisticsId(param.getLogisticsId());
            Assert.isTrue((param.getLogisticsId() != null && param.getLogisticsId() > 0), () -> new GlobalException("物流公司不能为空"));
            LogisticsCompany logisticsCompany = logisticsCompanyService.getById(param.getLogisticsId());
            if (logisticsCompany != null) {
                order.setLogisticsName(logisticsCompany.getLogisticsName());
            }
            order.setTrackingNo(param.getTrackingNo());
        } else if (param.getShippingMethod() == 2) {
            order.setLogisticsId(0);
            order.setTrackingNo("");
            order.setLogisticsName("商家配送");
        } else if (param.getShippingMethod() == 3) {
            order.setLogisticsId(0);
            order.setTrackingNo("");
            order.setLogisticsName("无需配送");
        } else if (param.getShippingMethod() == 4) {
            order.setLogisticsId(param.getLogisticsId());
            LogisticsCompany logisticsCompany = logisticsCompanyService.getById(param.getLogisticsId());
            if (logisticsCompany != null) {
                order.setLogisticsName(logisticsCompany.getLogisticsName());
            }
            order.setTrackingNo("");
        }
        this.updateById(order);

        // 5. 发送自动确认收货消息
        long existCount = orderPickupItemService.count(Wrappers.lambdaQuery(OrderPickupItem.class)
                .eq(OrderPickupItem::getOrderId, order.getOrderId()));
        if (existCount < 1) {
            TransactionAfterCommitExecutor.execute(() -> {
                try {
                    // 计算延迟时间
                    String autoDeliveryDays = configService.getConfigVal(SettingsEnum.AUTO_DELIVERY_DAYS);
                    if (StrUtil.isNotBlank(autoDeliveryDays)) {
                        // long delayTime = Long.parseLong(autoDeliveryDays) * 24 * 60 * 60 * 1000;
                        BigDecimal delayTime = new BigDecimal(autoDeliveryDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
                        rabbitTemplate.convertAndSend(
                                RabbitMQConfig.DELAY_EXCHANGE,
                                RabbitMQConfig.ORDER_CONFIRM_RECEIPT_ROUTING_KEY,
                                order.getOrderId(),
                                message -> {
                                    message.getMessageProperties().setHeader("x-delay", delayTime.longValue());
                                    return message;
                                }
                        );
                    }
                } catch (Exception e) {
                    log.error("确认收货", e);
                }
            });
        }

        // 发送消息通知
        if (StrUtil.isNotBlank(order.getLogisticsName()) && StrUtil.isNotBlank(order.getTrackingNo())) {
            messageService.sendUserMessage(orderVO.getUserId(), order.getOrderId(), MessageTypeEnum.ORDER_SHIPPING);
        }

        // 微信小程序发货信息录入接口
        if ("miniProgram".equals(order.getOrderSource())) {
            try {
                this.uploadShippingInfo(order, deliverItems);
            } catch (Exception e) {
                log.error("微信小程序发货信息录入接口异常", e);
            }
        }

        if (param.getShippingMethod() == 4) {
            //如果是电子面单调用电子面单服务
            Map<String, Object> item = kdNiaoService.getElectronicWaybill(this.detail(param.getId()), param.getBillRemark());
            Map<String, Object> orderInfo = (Map<String, Object>) item.get("Order");
            String logisticCode = (String) orderInfo.get("LogisticCode");
            order.setTrackingNo(logisticCode);
            this.updateById(order);
        }
    }

    private void uploadShippingInfo(Order order, List<OrderItemVO> deliverItems) {
        // 获取微信配置
        String appId = configService.getConfigVal(SettingsEnum.WECHAT_APP_ID);
        String appSecret = configService.getConfigVal(SettingsEnum.WECHAT_APP_SECRET);
        // 获取token
        GetTokenResult tokenResult = wechatApiClient.getToken("client_credential", appId, appSecret);
        String accessToken = tokenResult.getAccessToken();

        // 查询订单支付记录
        Paylog paylog = paylogMapper.selectOne(
                Wrappers.lambdaQuery(Paylog.class).eq(Paylog::getOrderId, order.getOrderId()).eq(Paylog::getPayStatus, PaymentStatus.PAID)
        );

        String wechatPayMchId = configService.getConfigVal(SettingsEnum.WECHAT_PAY_MCHID);
        UploadShippingInfoParam.OrderKey orderKey = UploadShippingInfoParam.OrderKey.builder()
                .orderNumberType(1)
                .mchid(wechatPayMchId)
                .outTradeNo(paylog.getPaySn())
                .build();

        int logisticsType = order.getLogisticsId() != null && order.getLogisticsId() != 0 ? 3 : 1;
        // 无拆单
        boolean isAllDelivered = order.getParentOrderId() == null;
        // 拆单情况
        if (!isAllDelivered) {
            Long pendingCount = this.lambdaQuery()
                    .eq(Order::getParentOrderId, order.getParentOrderId())
                    .eq(Order::getShippingStatus, ShippingStatusEnum.PENDING.getCode())
                    .ne(Order::getOrderId, order.getOrderId())
                    .count();
            isAllDelivered = pendingCount == 0;
        }

        // 查询物流公司编码
        LogisticsCompany logisticsCompany = logisticsCompanyService.lambdaQuery().eq(LogisticsCompany::getLogisticsId, order.getLogisticsId()).one();
        String expressCompany = logisticsCompany.getLogisticsCode();

        UploadShippingInfoParam.Shipping shipping = UploadShippingInfoParam.Shipping.builder()
                .trackingNo(order.getTrackingNo())
                .expressCompany(expressCompany)
                .itemDesc(deliverItems.getFirst() != null ? deliverItems.getFirst().getProductName() : "")
                .build();
        shipping.assembleContact(null, order.getMobile());
        List<UploadShippingInfoParam.Shipping> shippingList = Collections.singletonList(shipping);

        ZonedDateTime zdt = Instant.ofEpochMilli(order.getShippingTime()).atZone(ZoneId.of("Asia/Shanghai"));
        String uploadTime = zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        // 获取用户 openid
        UserAuthorize userAuthorize = userAuthorizeMapper.selectOne(
                Wrappers.lambdaQuery(UserAuthorize.class).eq(UserAuthorize::getUserId, order.getUserId())
        );
        String openid = userAuthorize.getOpenId();

        UploadShippingInfoParam uploadShippingInfoParam = UploadShippingInfoParam.builder()
                .orderKey(orderKey)
                .logisticsType(logisticsType)
                .isAllDelivered(isAllDelivered)
                .shippingList(shippingList)
                .uploadTime(uploadTime)
                .payer(new UploadShippingInfoParam.Payer(openid))
                .build();

        wechatApiClient.uploadShippingInfo(accessToken, uploadShippingInfoParam);
    }

    @Override
    public OrderVO getOrderPrintInfo(Integer id) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        // 构建URL
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        // 如果端口不是默认的80或443，则添加端口号
        if (!(serverPort == 80 && "http".equals(scheme)) && !(serverPort == 443 && "https".equals(scheme))) {
            url.append(":").append(serverPort);
        }
        Date date = DateUtil.date(System.currentTimeMillis());
        OrderVO orderVO = this.detail(id);
        orderVO.setHost(url.toString());
        orderVO.setPrintTime(String.valueOf(date));
        return orderVO;
    }

    @Override
    public LogisticsApiLog getOrderPrintWaybillInfo(Integer id) {
        return logisticsApiLogService.getOrderLogisticsApiLog(id);
    }

    @Override
    public List<OrderVO> getSeveralDetail(List<Integer> ids, Integer suppliersId) {
        if (CollUtil.isEmpty(ids)) {
            throw new GlobalException("请选择有效订单");
        }

        List<OrderVO> orders = new ArrayList<>();
        for (Integer id : ids) {
            orders.add(detail(id));
        }

        if (orders.isEmpty()) {
            throw new GlobalException("请选择有效订单");
        }

        // 如果传入了 suppliersId，过滤订单项
//        if (suppliersId != null && suppliersId > 0) {
//            for (OrderVO order : orders) {
//                List<OrderItem> filteredItems = order.getItems().stream()
//                        .filter(item -> Objects.equals(item.gets(), suppliersId))
//                        .collect(Collectors.toList());
//                order.setItems(filteredItems); // 重设过滤后的 items
//            }
//        }

        return orders;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean modifyOrderProduct(OrderModifyProductDTO modifyProductDTO) {
        OrderVO orderVO = this.detail(modifyProductDTO.getId());
        if (!orderVO.getAvailableActions().getModifyOrderProduct()) {
            throw new GlobalException("订单状态错误");
        }
        orderItemService.modifyOrderItem(orderVO, modifyProductDTO);
        Order order = this.getById(modifyProductDTO.getId());
        this.updateOrderMoney(order);
        orderLogService.quickCreate("订单商品信息修改", modifyProductDTO.getId(), orderVO.getOrderSn());
        return false;
    }

    @Override
    public BigDecimal getOrderUnSettlementAmount(Integer shopId) {
        LambdaQueryWrapper<StatementOutput> eq = new LambdaQueryWrapper<StatementOutput>()
                .in(StatementOutput::getSettlementStatus, StatementType.SERVICE_FEE.getCode(), StatementType.ORDER.getCode(), StatementType.ORDER_REFUND.getCode())
                .eq(StatementOutput::getRealSettlement, NO);
        if (AdminTypeEnum.fromCode(HeaderUtils.getAdminType()) == AdminTypeEnum.ADMIN) {
            if (shopId > 0) {
                eq.eq(StatementOutput::getShopId, shopId);
            } else {
                eq.gt(StatementOutput::getShopId, 0);
            }
        } else {
            eq.eq(StatementOutput::getShopId, shopId);
        }
        List<StatementOutput> statementOutputs = statementOutputMapper.selectList(eq);
        if (ObjectUtil.isEmpty(statementOutputs)) {
            return BigDecimal.ZERO;
        }
        BigDecimal in = statementOutputs.stream().map(StatementOutput::getIncome).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        BigDecimal out = statementOutputs.stream().map(StatementOutput::getExpenditure).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        return in.subtract(out);
    }

    @Override
    public BigDecimal getOrderUnSettlementAmountByVendor(Integer vendorId) {
        LambdaQueryWrapper<StatementOutput> eq = new LambdaQueryWrapper<StatementOutput>()
                .in(StatementOutput::getSettlementStatus, StatementType.SERVICE_FEE.getCode(), StatementType.ORDER.getCode(), StatementType.ORDER_REFUND.getCode())
                .eq(StatementOutput::getRealSettlement, NO);
        if (AdminTypeEnum.fromCode(HeaderUtils.getAdminType()) == AdminTypeEnum.ADMIN) {
            if (vendorId > 0) {
                eq.eq(StatementOutput::getVendorId, vendorId);
            } else {
                eq.gt(StatementOutput::getVendorId, 0);
            }
        } else {
            eq.eq(StatementOutput::getVendorId, vendorId);
        }
        List<StatementOutput> statementOutputs = statementOutputMapper.selectList(eq);
        if (ObjectUtil.isEmpty(statementOutputs)) {
            return BigDecimal.ZERO;
        }
        BigDecimal in = statementOutputs.stream().map(StatementOutput::getIncome).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        BigDecimal out = statementOutputs.stream().map(StatementOutput::getExpenditure).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        return in.subtract(out);
    }

    @Override
    public BigDecimal getTodayOrderSettlementAmountByVendor(Integer vendorId, Long todayStart, Long todayEnd) {
        List<StatementOutput> statementOutputs = statementOutputMapper.selectList(new LambdaQueryWrapper<StatementOutput>()
                .eq(StatementOutput::getVendorId, vendorId)
                .in(StatementOutput::getSettlementStatus, StatementType.SERVICE_FEE.getCode(), StatementType.ORDER.getCode(), StatementType.ORDER_REFUND.getCode())
                .eq(StatementOutput::getRealSettlement, YES)
                .between(StatementOutput::getRealSettlementTime, todayStart, todayEnd));
        if (ObjectUtil.isEmpty(statementOutputs)) {
            return BigDecimal.ZERO;
        }
        BigDecimal in = statementOutputs.stream().map(StatementOutput::getIncome).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        BigDecimal out = statementOutputs.stream().map(StatementOutput::getExpenditure).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        return in.subtract(out);
    }

    @Override
    public Map<String, String> getExportItemList() {
        return Map.ofEntries(
                Map.entry("orderSn", "订单编号"),
                Map.entry("addTime", "下单时间"),
                Map.entry("orderStatusName", "订单状态"),
                Map.entry("payStatusName", "支付状态"),
                Map.entry("shopName", "店铺名称"),
                Map.entry("username", "会员名称"),
                Map.entry("consignee", "收件人姓名"),
                Map.entry("mobile", "收件人电话"),
                Map.entry("address", "收件人地址"),
                Map.entry("totalAmount", "订单总价"),
                Map.entry("balance", "使用余额"),
                Map.entry("discountAmount", "折扣"),
                Map.entry("pointsAmount", "积分抵金额"),
                Map.entry("couponAmount", "优惠券金额"),
                Map.entry("payTime", "支付时间"),
                Map.entry("payTypeId", "支付类型"),
                Map.entry("paidAmount", "支付金额"),
                Map.entry("shippingTime", "发货时间"),
                Map.entry("shippingFee", "运费"),
                Map.entry("logisticsName", "物流名称"),
                Map.entry("trackingNo", "发货单号"),
                Map.entry("buyerNote", "订单备注"),
                Map.entry("adminNote", "商家备注"),
                Map.entry("productInfo", "商品信息"),
                Map.entry("shippingStatusName", "发货状态"),
                Map.entry("productWeight", "总重量(KG)"),
                Map.entry("usePoints", "使用积分")
        );
    }

    @Override
    public void saveExportItem(ExportItemSaveParam param) {
        adminUserMapper.update(
                Wrappers.lambdaUpdate(AdminUser.class)
                        .eq(AdminUser::getAdminId, SecurityUtils.getCurrentAdminId())
                        .set(AdminUser::getOrderExport, JSONUtil.toJsonStr(param.getExportItem().split(",")))
        );
    }

    @Override
    public Map<String, String> exportItemInfo() {
        AdminUser adminUser = adminUserMapper.selectById(SecurityUtils.getCurrentAdminId());
        if (adminUser.getOrderExport() != null) {
            List<String> orderExport = JSONUtil.toList(adminUser.getOrderExport(), String.class);
            return this.getExportItemList().entrySet().stream().filter(entry -> orderExport.contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return Map.of();
    }

    @Override
    public void orderExport(OrderListPageQuery pageQuery, HttpServletResponse response) {
        pageQuery.setSize(99999);
        List<OrderVO> list = this.list(pageQuery).getRecords();

        List<OrderExportVO> orderExportList = new ArrayList<>();
        for (OrderVO order : list) {
            OrderExportVO orderExportVO = new OrderExportVO();
            BeanUtils.copyProperties(order, orderExportVO);
            orderExportVO.setAddress(order.getUserAddress());
            orderExportVO.setPayTypeName(order.getPayTypeId() == 1 ? "在线支付" : order.getPayTypeId() == 2 ? "货到付款" : "线下支付");

            //orderList.item下面的重量相加
            if (order.getItems() != null) {
                orderExportVO.setProductWeight(
                        order.getItems().stream()
                                .map(item -> Optional.ofNullable(item.getProductWeight()).orElse(BigDecimal.ZERO))
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                );
                //取list.item下面的商品循环商品名称，商品属性，商品编号，购买数量，购买价格，小计用竖线隔开，商品之间用换行符隔开
                for (OrderItemVO item : order.getItems()) {
                    OrderExportVO newOrderExportVO = BeanUtil.copyProperties(orderExportVO, OrderExportVO.class);

                    newOrderExportVO.setProductName(item.getProductName());
                    if (CollUtil.isNotEmpty(item.getSkuData())) {
                        String skuData = item.getSkuData().stream()
                                .map(skudata -> StrUtil.format("{}:{}", skudata.getName(), skudata.getValue()))
                                .collect(Collectors.joining("|"));
                        newOrderExportVO.setSkuData(skuData);
                    }
                    newOrderExportVO.setProductSn(item.getProductSn());
                    newOrderExportVO.setQuantity(item.getQuantity());
                    newOrderExportVO.setPrice(item.getPrice());
                    newOrderExportVO.setTotalPrice(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                    newOrderExportVO.setUsername(order.getUser().getUsername());
                    orderExportList.add(newOrderExportVO);
                }

            }
        }

        ExcelUtils<OrderExportVO> excelUtils = new ExcelUtils<>(orderExportList, OrderExportVO.class);
        Set<String> exportItemList = Arrays.stream(pageQuery.getExportItem().split(",")).collect(Collectors.toSet());
        //如果选择了商品信息，则添加商品对应字段信息
        if (exportItemList.contains("productInfo")) {
            exportItemList.remove("productInfo");
            exportItemList.add("productName");
            exportItemList.add("skuData");
            exportItemList.add("productSn");
            exportItemList.add("quantity");
            exportItemList.add("price");
            exportItemList.add("totalPrice");
        }

        if (exportItemList.contains("payTypeId")) {
            exportItemList.add("payTypeName");
        }
        String fileName = URLEncoder.encode("订单列表信息" + StringUtils.getCurrentTime() + XLSX, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        excelUtils.exportExcelWebByTitle(response, exportItemList, fileName, "订单列表信息");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addLog(Integer orderId, String desc) {

        if (desc != null) {
            orderLogService.quickCreate(desc, orderId, "");
        }
    }

    @Override
    public Page<AfterSalesClientVO> afterSalesList(AftersalesListPageQuery listDTO) {
        // 分页
        Page<Order> page = buildSortOrder(listDTO);

        Page<Order> orderPage = this.lambdaQuery()
                .eq(Order::getIsDel, 0)
                .eq(Order::getUserId, listDTO.getUserId())
                .notIn(Order::getOrderType, OrderType.VIRTUAL.getCode(), OrderType.PAID.getCode(), OrderType.CARD.getCode())
                .in(Order::getOrderStatus, OrderStatusEnum.CONFIRMED.getCode(), OrderStatusEnum.PROCESSING.getCode(), OrderStatusEnum.COMPLETED.getCode())
                .page(page);

        // 获取查询结果
        List<Order> orderPageRecords = orderPage.getRecords();
        if (CollUtil.isEmpty(orderPageRecords)) {
            return new Page<>();
        }

        // 取出订单ID
        List<Integer> orderIds = orderPageRecords.stream().map(Order::getOrderId).toList();
        List<OrderItemVO> itemListNew = getOrderItemList(orderIds);

        // 获取产品数据
        List<Integer> productIds = itemListNew.stream().map(OrderItemVO::getProductId).toList();
        Map<Integer, Product> productMap = Map.of();
        if (CollUtil.isNotEmpty(productIds)) {
            List<Product> productList = productService.listByIds(productIds);
            productMap = productList.stream().collect(Collectors.toMap(Product::getProductId, Function.identity()));
        }

        // 查询店铺结算配置
        List<Integer> shopIds = itemListNew.stream().map(OrderItemVO::getShopId).distinct().toList();
        if (CollUtil.isEmpty(shopIds)) {
            return new Page<>();
        }
        List<OrderConfig> orderConfigs = orderConfigMapper.selectList(
                new LambdaQueryWrapper<OrderConfig>()
                        .in(OrderConfig::getShopId, shopIds)
        );
        Map<Integer, OrderConfig> orderConfigMap = orderConfigs.stream().collect(Collectors.toMap(OrderConfig::getShopId, Function.identity()));

        // 插入售后数据
        Map<Integer, List<OrderItemVO>> itemMap = itemListNew.stream().collect(Collectors.groupingBy(OrderItemVO::getOrderId));
        Map<Integer, Product> finalProductMap = productMap;
        String afterSalesLimitDays = configService.getConfigVal(SettingsEnum.AFTER_SALES_LIMIT_DAYS);
        List<AfterSalesClientVO> orderVOList = orderPageRecords.stream()
                .map(order -> {
                    AfterSalesClientVO afterSalesClientVO = new AfterSalesClientVO();
                    BeanUtils.copyProperties(order, afterSalesClientVO);
                    afterSalesClientVO.setShippingTime(TigUtils.handelTime(order.getShippingTime()));

                    List<OrderItemVO> orderItems = itemMap.get(order.getOrderId());
                    //循环orderItems ，将每个值复制给AfterSalesClientVO.ITEM
                    List<AfterSalesClientVO.Item> items = new ArrayList<>();
                    orderItems.forEach(orderItem -> {
                        AfterSalesClientVO.Item item = new AfterSalesClientVO.Item();
                        BeanUtils.copyProperties(orderItem, item);

                        Product product = finalProductMap.get(orderItem.getProductId());
                        if (product != null) {
                            item.setProductPicThumb(product.getPicThumb());
                            item.setProductWeight(product.getProductWeight());
                            item.setProductStock(product.getProductStock());
                            item.setVirtualSample(product.getVirtualSample());
                            item.setPaidContent(JsonUtil.checkJsonType(product.getPaidContent()));
                            item.setCardGroupId(product.getCardGroupId());
                        }

                        item = getAfterSalesItem(order.getOrderId(), orderItem, item);

                        // 判断是否可以申请售后；1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）2. 全局设置（买家申请售后限制）
                        /*Long count = aftersalesMapper.selectCount(new LambdaQueryWrapper<Aftersales>()
                                .eq(Aftersales::getOrderId, item.getOrderId())
                                .in(Aftersales::getStatus, AftersalesStatusEnum.validStatus())
                        );*/
                        // 一个订单多个商品，或者多个数量时，这里先默认为可申请
                        item.setToAftersalses(true);
                        // 1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）
                        if (orderItem.getShopId() != null && orderItem.getShopId() > 0) {
                            OrderConfig orderConfig = orderConfigMap.get(orderItem.getShopId());
                            if (orderConfig != null) {
                                OrderConfigDTO orderConfigData = JsonUtil.fromJson(orderConfig.getData(), OrderConfigDTO.class);
                                if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus() && 0 == orderConfigData.getDateType()) {
                                    item.setToAftersalses(false);
                                }
                            }
                        }
                        // 2. 全局设置（买家申请售后限制）
                        if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus()) {
                            if (StrUtil.isNotBlank(afterSalesLimitDays)) {
                                long receivedTime = order.getReceivedTime() * 1000;
                                BigDecimal afterSalesLimitDaysTime = new BigDecimal(afterSalesLimitDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
                                LocalDateTime limitDateTime = Instant.ofEpochMilli(receivedTime + afterSalesLimitDaysTime.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                                boolean toAftersalses = limitDateTime.isAfter(LocalDateTime.now());
                                item.setToAftersalses(toAftersalses);
                            }
                        }

                        items.add(item);
                    });
                    afterSalesClientVO.setItems(items);
                    return afterSalesClientVO;
                }).toList();
        return PageUtil.convertPage(orderPage, orderVOList);
    }

    @Override
    public List<AfterSalesDetailVO> applyData(Integer itemId, Integer orderId) {
        // 判断是否支持售后
        Order order = this.getById(orderId);

        // 获取是否有店铺订单设置
        OrderConfig orderConfig = orderConfigMapper.selectOne(
                Wrappers.lambdaQuery(OrderConfig.class)
                        .eq(OrderConfig::getShopId, order.getShopId())
                        .eq(OrderConfig::getCode, "order_config")
        );

        // 确认收货订单多少天之后不可在申请售后
        if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus() && orderConfig != null) {
            OrderConfigDataDTO orderConfigDataDTO = JsonUtil.fromJson(orderConfig.getData(), OrderConfigDataDTO.class);
            if (orderConfigDataDTO.getDataType() == 1) {
                // 当前时间 - order.getReceivedTime()
                long time = StringUtils.getCurrentTime() / 1000 - order.getReceivedTime();
                if (time > orderConfigDataDTO.getUseDay() * 24 * 60 * 60) {
                    throw new GlobalException(translatePackage.translate("已超过该订单支持的售后时间"));
                }
            }
        }

        List<OrderItem> orderItems = orderItemMapper.selectList(
                Wrappers.lambdaQuery(OrderItem.class)
                        .eq(OrderItem::getOrderId, orderId)
                        .eq(itemId != null, OrderItem::getItemId, itemId)
        );
        Assert.notEmpty(orderItems, () -> new GlobalException(translatePackage.translate("该商品订单不存在")));

        Paylog paylog = paylogMapper.selectOne(
                Wrappers.lambdaQuery(Paylog.class)
                        .eq(Paylog::getOrderId, orderId)
                        .eq(Paylog::getPayStatus, 1)
        );
        List<String> payCodes = List.of("yunpay_wechat", "yunpay_alipay", "yunpay_yunshanfu");
        Assert.isFalse(paylog != null && payCodes.contains(paylog.getPayCode()) && itemId != null, () -> new GlobalException(translatePackage.translate("该商品只支持整单退款")));

        List<Integer> status = getAfterSalseStatusList();
        return orderItems.stream()
                .map(orderItem -> {
                    AfterSalesDetailVO afterSalesDetailVO = new AfterSalesDetailVO();
                    BeanUtils.copyProperties(orderItem, afterSalesDetailVO);
                    afterSalesDetailVO.setSubtotal(orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity())));
                    afterSalesDetailVO.setSkuData(JsonUtil.jsonToList(orderItem.getSkuData(), ProductSkuDTO.SkuData.class));
                    List<Aftersales> aftersalesList = aftersalesMapper.selectList(
                            Wrappers.lambdaQuery(Aftersales.class)
                                    .in(Aftersales::getStatus, status)
                                    .eq(Aftersales::getOrderId, orderId)
                    );
                    Integer canApplyQuantity = orderItem.getQuantity();
                    if (CollUtil.isNotEmpty(aftersalesList)) {
                        canApplyQuantity = handleSumValidNum(aftersalesList, orderItem.getItemId(), orderItem.getQuantity());
                    }
                    afterSalesDetailVO.setCanApplyQuantity(canApplyQuantity);
                    return afterSalesDetailVO;
                }).toList();
    }

    public AfterSalesClientVO.Item getAfterSalesItem(Integer orderId, OrderItemVO orderItem, AfterSalesClientVO.Item item) {
        List<Integer> status = getAfterSalseStatusList();
        LambdaQueryWrapper<Aftersales> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Aftersales::getStatus, status);
        queryWrapper.eq(Aftersales::getOrderId, orderId);
        List<Aftersales> aftersalesList = aftersalesMapper.selectList(queryWrapper);
        if (!aftersalesList.isEmpty()) {
            int canApplyQuantity = handleSumValidNum(aftersalesList, orderItem.getItemId(), orderItem.getQuantity());
            if (item.getAftersalesItem() != null) {
                LambdaQueryWrapper<Aftersales> queryWrapperAfter = new LambdaQueryWrapper<>();
                queryWrapperAfter.eq(Aftersales::getAftersaleId, item.getAftersalesItem().getAftersaleId());
                Aftersales aftersales = aftersalesMapper.selectOne(queryWrapperAfter);
                if (aftersales != null) {
                    if (aftersales.getStatus() == AftersalesStatusEnum.COMPLETE.getCode()) {
                        if (canApplyQuantity == 0) {
                            // 不可申请--跳转详情
                            item.setAftersaleFlag(0);
                        } else {
                            // 可申请
                            item.setAftersaleFlag(1);
                        }
                    } else if (aftersales.getStatus() == AftersalesStatusEnum.CANCEL.getCode()) {
                        // 售后取消 -- 可申请
                        item.setAftersaleFlag(1);
                    } else {
                        item.setAftersaleFlag(0);
                    }
                } else {
                    item.setAftersaleFlag(1);
                }
            } else {
                item.setAftersaleFlag(1);
            }
        } else {
            item.setAftersaleFlag(1);
        }
        return item;
    }

    private Integer handleSumValidNum(List<Aftersales> aftersalesList, Integer itemId, Integer quantity) {
        List<Integer> aftersalesIds = aftersalesList.stream().map(Aftersales::getAftersaleId).toList();

        List<AftersalesItem> aftersalesItems = aftersalesItemService.lambdaQuery()
                .in(AftersalesItem::getAftersaleId, aftersalesIds)
                .eq(AftersalesItem::getOrderItemId, itemId)
                .list();

        if (CollUtil.isNotEmpty(aftersalesItems)) {
            Integer afterSalesNum = aftersalesItems.stream().map(AftersalesItem::getNumber).reduce(0, Integer::sum);
            return quantity - afterSalesNum;
        }

        return quantity;
    }

    /**
     * 获取售后状态
     *
     * @return List
     */
    private List<Integer> getAfterSalseStatusList() {
        List<Integer> status = new ArrayList<>();
        status.add(AftersalesStatusEnum.IN_REVIEW.getCode());
        status.add(AftersalesStatusEnum.APPROVED_FOR_PROCESSING.getCode());
        status.add(AftersalesStatusEnum.REFUSE.getCode());
        status.add(AftersalesStatusEnum.SEND_BACK.getCode());
        status.add(AftersalesStatusEnum.RETURNED.getCode());
        status.add(AftersalesStatusEnum.COMPLETE.getCode());
        Integer isVendor = tigshopProperties.getIsVendor();
        if (isVendor == 1) {
            status.add(AftersalesStatusEnum.WAIT_FOR_SUPPLIER_AUDIT.getCode());
            status.add(AftersalesStatusEnum.WAIT_FOR_SUPPLIER_RECEIPT.getCode());
        }
        return status;
    }

    private BigDecimal allocatedAmount(BigDecimal originalAmount, BigDecimal newAmount, BigDecimal amount) {
        // 实现金额平摊逻辑
        BigDecimal ratio = amount.divide(originalAmount, 10, RoundingMode.HALF_UP);
        return newAmount.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 订单进度条
     */
    @Override
    public OrderStepVO stepStatus(OrderVO orderVO) {
        OrderStepVO orderStepVO = new OrderStepVO();
        orderStepVO.setCurrent(0);
        orderStepVO.setStatus("process");
        List<OrderStepVO.OrderStepDetailVO> steps = new ArrayList<>();

        // 第一步：提交订单
        OrderStepVO.OrderStepDetailVO submitOrderStep = new OrderStepVO.OrderStepDetailVO();
        submitOrderStep.setTitle("提交订单");
        submitOrderStep.setDescription(orderVO.getAddTime());
        steps.add(submitOrderStep);

        // 第二步：支付状态
        OrderStepVO.OrderStepDetailVO paymentStep = new OrderStepVO.OrderStepDetailVO();
        paymentStep.setTitle(PaymentStatus.getStatusName(orderVO.getPayStatus()));
        paymentStep.setDescription("");
        steps.add(paymentStep);

        // 支付状态判断
        switch (PaymentStatus.getStatus(orderVO.getPayStatus())) {
            case UNPAID:
                orderStepVO.setCurrent(1);
                break;
            case PAID:
                orderStepVO.setCurrent(2);
                paymentStep.setDescription(orderVO.getPayTime());
                break;
            default:
                break;
        }

        // 第三步：物流状态
        OrderStepVO.OrderStepDetailVO shippingStep = new OrderStepVO.OrderStepDetailVO();
        shippingStep.setTitle(ShippingStatusEnum.getStatusName(orderVO.getShippingStatus()));
        if (Boolean.TRUE.equals(orderVO.getIsPickup())) {
            shippingStep.setTitle(ShipmentStatusEnum.getStatusName(orderVO.getShippingStatus() + 1));
        }
        shippingStep.setDescription("");
        steps.add(shippingStep);

        // 物流状态判断
        switch (ShippingStatusEnum.getStatus(orderVO.getShippingStatus())) {
            case PENDING:
                log.info("PENDING");
                break;
            case SENT:
                orderStepVO.setCurrent(2);
                shippingStep.setDescription(orderVO.getShippingTime());
                break;
            case SHIPPED:
                orderStepVO.setCurrent(2);
                shippingStep.setDescription(orderVO.getReceivedTime());
                orderStepVO.setStatus("finish");
                break;
            default:
                break;
        }

        // 订单状态判断
        switch (OrderStatusEnum.getStatus(orderVO.getOrderStatus())) {
            case CANCELLED:
                orderStepVO.setCurrent(1);
                steps.get(1).setTitle(OrderStatusEnum.getStatusName(OrderStatusEnum.CANCELLED.getCode()));
                steps.remove(2);
                orderStepVO.setStatus("error");
                break;
            case INVALID:
                orderStepVO.setCurrent(1);
                steps.get(1).setDescription("");
                orderStepVO.setStatus("error");
                break;
            case PENDING, CONFIRMED, PROCESSING, COMPLETED:
            default:
                break;
        }

        orderStepVO.setSteps(steps);
        return orderStepVO;
    }

    @Override
    public OrderVO detail(Integer id) {
        Integer userId = getCurrentUserId();

        OrderVO detail = this.detail(id, userId);
        OrderSettingsVO orderSettings = configService.getSettings(OrderSettingsVO.class);

        detail.assembleOrderSettings(orderSettings);

        return detail;
    }

    @Override
    public OrderVO detail(Integer id, Integer userId) {
        long count = this.lambdaQuery().eq(Order::getParentOrderId, id).count();
        Assert.isTrue(count == 0, () -> new GlobalException(ORDER_SPLIT_ERROR));

        Order order = this.lambdaQuery().eq(Order::getOrderId, id).eq(userId != null && userId != 0, Order::getUserId, userId).one();
        Assert.notNull(order, () -> new GlobalException(ORDER_NOT_EXIST));

        OrderVO orderVO = new OrderVO(order);

        // 调用方法获取会员信息
        User user = userMapper.selectById(order.getUserId());
        UserBaseVO userBaseVO = new UserBaseVO(user);
        orderVO.setUser(userBaseVO);

        List<OrderItemVO> orderItems = orderItemService.getItemByOrderIds(Collections.singletonList(order.getOrderId()));

        Map<Integer, List<AftersalesItem>> aftersalesItemMap = new HashMap<>();
        if (CollUtil.isNotEmpty(orderItems)) {
            List<Integer> orderItemIds = orderItems.stream().map(OrderItemVO::getItemId).toList();
            List<AftersalesItem> aftersalesItems = aftersalesItemService.validAftersalesItems(orderItemIds);
            aftersalesItemMap = aftersalesItems.stream().collect(Collectors.groupingBy(AftersalesItem::getOrderItemId));
        }

        Map<Integer, List<AftersalesItem>> finalAftersalesItemMap = aftersalesItemMap;
        orderItems = orderItems.stream()
                .peek(item -> {
                    List<AftersalesItem> currAftersalesItems = finalAftersalesItemMap.get(item.getItemId());
                    item.setAllowDeliverNum(item.getQuantity());
                    if (CollUtil.isNotEmpty(currAftersalesItems)) {
                        for (AftersalesItem currAftersalesItem : currAftersalesItems) {
                            Aftersales aftersales = aftersalesService.getById(currAftersalesItem.getAftersaleId());
                            if (aftersales != null && aftersales.getStatus() == AftersalesStatusEnum.COMPLETE.getCode()) {
                                item.setAllowDeliverNum(item.getQuantity() - currAftersalesItem.getNumber());
                            }
                        }
                        currAftersalesItems = currAftersalesItems.stream().sorted(Comparator.comparing(AftersalesItem::getAftersaleId)).toList();
                        item.setAftersalesItem(currAftersalesItems.getLast());
                    }
                }).toList();
        orderVO.setItems(orderItems);

        orderVO.setAvailableActions(availableActions(orderVO));

        if (order.getShopId() > 0) {
            Shop shop = shopMapper.selectById(order.getShopId());
            orderVO.setShop(new OrderVO.ShopVO(shop));
        }

        // 判断是否可以申请售后；1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）2. 全局设置（买家申请售后限制）
        boolean toAftersales = orderVO.getAvailableActions().getToAftersales();
        orderVO.setToAftersalses(toAftersales);
        Integer orderStatus = order.getOrderStatus();
        if (toAftersales) {
            // 1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）
            if (order.getShopId() != null && order.getShopId() > 0) {
                // 查询店铺结算配置
                OrderConfig orderConfig = orderConfigMapper.selectOne(
                        new LambdaQueryWrapper<OrderConfig>()
                                .eq(OrderConfig::getShopId, order.getShopId())
                );
                if (orderConfig != null) {
                    OrderConfigDTO orderConfigData = JsonUtil.fromJson(orderConfig.getData(), OrderConfigDTO.class);
                    if (OrderStatusEnum.COMPLETED.getCode() == orderStatus && 0 == orderConfigData.getDateType()) {
                        orderVO.setToAftersalses(false);
                    }
                }
            }
            // 2. 全局设置（买家申请售后限制）
            if (OrderStatusEnum.COMPLETED.getCode() == orderStatus) {
                String afterSalesLimitDays = configService.getConfigVal(SettingsEnum.AFTER_SALES_LIMIT_DAYS);
                if (StrUtil.isNotBlank(afterSalesLimitDays)) {
                    long receivedTime = order.getReceivedTime() * 1000;
                    BigDecimal afterSalesLimitDaysTime = new BigDecimal(afterSalesLimitDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
                    LocalDateTime limitDateTime = Instant.ofEpochMilli(receivedTime + afterSalesLimitDaysTime.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                    boolean toAftersalses = limitDateTime.isAfter(LocalDateTime.now());
                    orderVO.setToAftersalses(toAftersalses);
                }
            }
        }
        orderVO.getAvailableActions().setToAftersales(orderVO.getToAftersalses());

        // 自动发货天数
        Long aftersalesCount = aftersalesMapper.selectCount(
                Wrappers.lambdaQuery(Aftersales.class)
                        .eq(Aftersales::getOrderId, order.getOrderId())
                        .eq(Aftersales::getStatus, AftersalesStatusEnum.IN_REVIEW.getCode())
        );
        String autoDeliveryDays = configService.getConfigVal(SettingsEnum.AUTO_DELIVERY_DAYS);
        if (aftersalesCount == 0 && OrderStatusEnum.PROCESSING.getCode() == orderStatus && StrUtil.isNotBlank(autoDeliveryDays)) {
            long shippingTimeMillis = order.getShippingTime() * 1000;
            long elapsedMillis = Instant.now().toEpochMilli() - shippingTimeMillis;

            long elapsedDays = TimeUnit.MILLISECONDS.toDays(elapsedMillis);

            BigDecimal result = new BigDecimal(autoDeliveryDays).subtract(new BigDecimal(elapsedDays)).setScale(0, RoundingMode.UP);

            orderVO.setAutoDeliveryDays(result);
        }

        orderVO.getItems().forEach(item -> {
            List<ProductSkuDTO.SkuData> skuData = JSONUtil.toList(JSONUtil.parseArray(item.getSkuData()), ProductSkuDTO.SkuData.class);
            item.setSkuValue(skuData
                    .stream()
                    .map(sku -> sku.getName() + ":" + sku.getValue())
                    .collect(Collectors.joining("|")));
        });

        String configVal = configService.getConfigVal(SettingsEnum.KDNIAO_BUSINESS_ID);
        orderVO.setWayBill(ObjectUtil.isNotEmpty(configVal));
        if (orderVO.getOrderType() == OrderType.VIRTUAL.getCode()) {
            if (orderStatus == OrderStatusEnum.CANCELLED.getCode() || orderStatus == PENDING.getCode()) {
                orderVO.getItems().forEach(item -> item.setVirtualSample(null));
            }
        }
        if (tigshopProperties.getIsO2o() == 1) {
            long existCount = orderPickupItemService.count(Wrappers.lambdaQuery(OrderPickupItem.class)
                    .eq(OrderPickupItem::getOrderId, order.getOrderId()));
            boolean isPickup = existCount > 0;
            orderVO.setIsPickup(isPickup);
            if (isPickup) {
                OrderPickupShipmentVO orderPickupShipment = orderPickupShipmentService.getOrderPickupShipment(order.getOrderId());
                orderVO.setPickupCode(orderPickupShipment.getPickupSn());
                List<OrderPickupItem> orderPickupItems = orderPickupItemService.lambdaQuery().eq(OrderPickupItem::getOrderId, order.getOrderId())
                        .list();
                if (CollUtil.isNotEmpty(orderPickupItems)) {
                    PickupListVO pickupListVO = pickupService.pickupDetail(orderPickupItems.getFirst().getPickupId());
                    orderVO.setPickupDetail(pickupListVO);
                }
                String expectPickupTime = orderPickupItems.getFirst().getExpectPickupTime();
                orderVO.setExpectPickupTime(expectPickupTime);
                // 提货时间提示
                if (order.getOrderStatus() == OrderStatusEnum.PROCESSING.getCode()) {
                    List<OrderPickupTimeVO> shopPickupTpl = orderPickupShipmentService.getShopPickupTpl(order.getShopId());
                    if (CollUtil.isNotEmpty(shopPickupTpl)) {
                        OrderPickupTimeVO opt = shopPickupTpl.getFirst();
                        if (opt.getPickupTimeStatus() == 0) {
                            // todo 还有问题，待完善
                            orderVO.setPickupTip(expectPickupTime);
                        }
                        if (opt.getPickupTimeStatus() == 1) {
                            orderVO.setPickupTip(expectPickupTime);
                        }
                    }
                }
                // 设置提货信息
                UserPickupInfo userPickupInfo = userPickupInfoMapper.selectById(orderPickupItems.getFirst().getUserPickupId());
                orderVO.setUserPickupInfo(userPickupInfo);
            }
            if (isPickup && orderStatus == OrderStatusEnum.CONFIRMED.getCode()) {
                orderVO.setOrderStatusName(NOT_SHIPPED.getDescription());
            } else if (isPickup && orderStatus == OrderStatusEnum.PROCESSING.getCode()) {
                orderVO.setOrderStatusName(SHIPPED.getDescription());
            }
        }
        orderVO.setStepStatus(stepStatus(orderVO));
        return orderVO;
    }

    @Override
    public OrderNumVO orderNum() {
        OrderNumVO orderNumVO = new OrderNumVO();
        int userId = getCurrentUserId();
        orderNumVO.setAwaitPay(this.getOrderStatusCount(userId, "order_status", PENDING.getCode()));
        orderNumVO.setAwaitShipping(this.getOrderStatusCount(userId, "order_status",
                OrderStatusEnum.CONFIRMED.getCode()));
        orderNumVO.setAwaitReceived(this.getOrderStatusCount(userId, "order_status",
                OrderStatusEnum.PROCESSING.getCode()));
        orderNumVO.setAwaitComment(this.getOrderStatusCount(userId, "order_comment_status",
                CommentStatus.PENDING.getCode()));
        orderNumVO.setOrderCompleted(this.getOrderStatusCount(userId, "order_status",
                OrderStatusEnum.COMPLETED.getCode()));
        Long aftersalesCount = aftersalesService.count(new LambdaQueryWrapper<Aftersales>()
                .eq(Aftersales::getUserId, userId)
                .in(Aftersales::getStatus, AftersalesStatusEnum.noContainCompleteAndCancelStatus()));
        orderNumVO.setAwaitAftersalesCollect(aftersalesCount);

        Long collectProductCount = collectProductMapper.selectCount(new LambdaQueryWrapper<CollectProduct>().eq(CollectProduct::getUserId, userId));
        orderNumVO.setProductCollect(collectProductCount);
        Long collectShopCount = collectShopMapper.selectCount(new LambdaQueryWrapper<CollectShop>().eq(CollectShop::getUserId, userId));
        orderNumVO.setShopCollect(collectShopCount);
        return orderNumVO;
    }

    @Override
    public boolean create(OrderCreateDTO createDTO) {
        if (createDTO != null) {
            Order order = new Order();
            BeanUtils.copyProperties(createDTO, order);
            return this.save(order);
        }
        return false;
    }

    @Override
    public boolean update(OrderUpdateDTO updateDTO) {
        if (updateDTO != null) {
            Order order = new Order();
            BeanUtils.copyProperties(updateDTO, order);
            return this.updateById(order);
        }
        return false;
    }

    @Override
    public Long getNewOrderCount(Long startTime) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        if (AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.VENDOR) {
            queryWrapper.eq(Order::getVendorId, getVendorId());
        } else {
            queryWrapper.eq(Order::getShopId, getShopId());
        }
        queryWrapper.ge(Order::getAddTime, startTime);
        return this.count(queryWrapper);

    }

    @Override
    public BigDecimal getOrderBalance(String startTime, String endTime, String type) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            Date startTimeDate = DateUtil.parse(startTime);
            Date endTimeDate = DateUtil.parse(endTime);
            // 将时间设置为当天的结束时间
            endTimeDate = DateUtil.endOfDay(endTimeDate);
            // 添加时间范围的条件，假设 changeTime 是 Unix 时间戳
            queryWrapper.ge(Order::getAddTime, startTimeDate.getTime() / 1000)
                    .le(Order::getAddTime, endTimeDate.getTime() / 1000);
        }
        if (Objects.equals(type, "balance")) {
            // 查询所有符合条件的余额变动
            queryWrapper.select(Order::getBalance);
            List<Order> orderList = this.list(queryWrapper);
            return orderList.stream()
                    .map(Order::getBalance)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
        } else if (Objects.equals(type, "use_points")) {
            queryWrapper.select(Order::getUsePoints);
            List<Order> orderList = this.list(queryWrapper);
            return orderList.stream()
                    .map(order -> BigDecimal.valueOf(order.getUsePoints()))
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public Page<SalesProductOrderItemVO> getSalesProductDetail(SalesProductDetailPageQuery pageQuery) {
        Page<SalesProductOrderItemVO> page = buildSortOrder(pageQuery);

        return this.baseMapper.getSalesProductDetail(page, pageQuery);
    }

    @Override
    public OrderPageConfigVO getOrderPageConfig() {
        return configService.getSettings(OrderPageConfigVO.class);
    }

    @Transactional
    @Override
    public void changeOrderStatus(OrderChangeStatusParam param) {
        Order order = this.getById(param.getId());
        Integer oldOrderStatus = order.getOrderStatus();
        Assert.notNull(order, () -> new GlobalException("订单不存在"));

        Integer shopId = HeaderUtils.getShopId();

        OrderSettingsVO orderSettings = configService.getSettings(OrderSettingsVO.class);
        Integer isChangeOrderStatus = Constants.NO;
        Integer changeOrderStatus = order.getOrderStatus();

        if (shopId != null && shopId == 0) {
            if (Constants.YES.equals(orderSettings.getIsPlatformCancelPaidOrder()) && OrderStatusEnum.CONFIRMED.getCode() == order.getOrderStatus()) {
                isChangeOrderStatus = Constants.YES;
                OrderStatusEnum preStatus = OrderStatusEnum.getPreStatus(order.getOrderStatus());
                if (preStatus != null) {
                    changeOrderStatus = preStatus.getCode();
                }
            }
            if (Constants.YES.equals(orderSettings.getIsPlatformCancelDeliverOrder()) && OrderStatusEnum.PROCESSING.getCode() == order.getOrderStatus()) {
                isChangeOrderStatus = Constants.YES;
                OrderStatusEnum preStatus = OrderStatusEnum.getPreStatus(order.getOrderStatus());
                if (preStatus != null) {
                    changeOrderStatus = preStatus.getCode();
                }
            }
        }

        if (shopId != null && shopId > 0) {
            if (Constants.YES.equals(orderSettings.getIsShopCancelDeliverOrder()) && OrderStatusEnum.PROCESSING.getCode() == order.getOrderStatus()) {
                isChangeOrderStatus = Constants.YES;
                OrderStatusEnum preStatus = OrderStatusEnum.getPreStatus(order.getOrderStatus());
                if (preStatus != null) {
                    changeOrderStatus = preStatus.getCode();
                }
            }
        }

        if (Constants.NO.equals(isChangeOrderStatus)) {
            return;
        }

        // 取消支付
        if (OrderStatusEnum.CANCELLED.getCode() == changeOrderStatus) {
            order.setAdminNote(param.getAdminNote());
            this.updateById(order);

            // 走仅退款的逻辑
            this.changeCancelOrder(order.getOrderId(), order.getUserId());
        }
        // 取消发货
        if (OrderStatusEnum.CONFIRMED.getCode() == changeOrderStatus) {
            // 修改订单状态、发货状态
            order.setOrderStatus(changeOrderStatus);
            order.setShippingStatus(ShippingStatusEnum.PENDING.getCode());
            order.setShippingMethod(0);
            order.setShippingTime(0L);
            order.setOrderSource("");
            order.setLogisticsId(0);
            order.setTrackingNo("");
            order.setLogisticsName("");
            order.setAdminNote(param.getAdminNote());
            this.updateById(order);
        }

        // 3. 添加订单日志
        OrderLog orderLog = OrderLog.builder()
                .orderId(order.getOrderId())
                .orderSn(order.getOrderSn())
                .adminId(SecurityUtils.getCurrentAdminId())
                .description("订单状态由" + OrderStatusEnum.getStatusName(oldOrderStatus) + "改为" + OrderStatusEnum.getStatusName(param.getVal()))
                .logTime(StringUtils.getCurrentTime())
                .shopId(HeaderUtils.getShopId())
                .build();
        orderLogService.save(orderLog);
    }

    private boolean changeCancelOrder(Integer id, Integer userId) {
        OrderVO orderVO = this.detail(id, userId);
        orderLogService.quickCreate(ORDER_CANCEL, id, orderVO.getOrderSn());

        if (orderVO.getUsePoints() > 0) {
            String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
            userPointsLogService.incPoints(orderVO.getUsePoints(), orderVO.getUserId(), "订单取消返还" + (StrUtil.isNotBlank(configVal) ? configVal : "积分"), id);
        }
        //使用了余额返余额
        if (orderVO.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            userBalanceService.incBalance(orderVO.getBalance(), orderVO.getUserId(), "订单取消返还余额");
        }
        if (orderVO.getCouponAmount().compareTo(BigDecimal.ZERO) > 0) {
            //通过userId和orderId更新数据
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUsedTime(0L);
            userCoupon.setOrderId(0);
            userCouponMapper.update(userCoupon,
                    Wrappers.lambdaQuery(UserCoupon.class)
                            .eq(UserCoupon::getOrderId, orderVO.getOrderId())
                            .eq(UserCoupon::getUserId, orderVO.getUserId()));
        }
        for (OrderItemVO item : orderVO.getItems()) {
            if (item.getSkuId() > 0) {
                productSkuService.incStock(item.getSkuId(), item.getQuantity());
            } else {
                productService.incStock(item.getProductId(), item.getQuantity());
            }
            //减少销量
            Product product = productService.getById(item.getProductId());
            if (product.getVirtualSales() != null && product.getVirtualSales() < item.getQuantity()) {
                productService.lambdaUpdate().eq(Product::getProductId, item.getProductId()).setDecrBy(Product::getVirtualSales, product.getVirtualSales()).update();
            } else {
                productService.lambdaUpdate().eq(Product::getProductId, item.getProductId()).setDecrBy(Product::getVirtualSales, item.getQuantity()).update();
            }
            //秒杀品减少销量
            seckillService.decSales(item.getProductId(), item.getSkuId(), item.getQuantity());
            //秒杀返回库存
            seckillService.incStock(item.getProductId(), item.getSkuId(), item.getQuantity());
        }
        Order order = new Order();
        order.setOrderId(id);
        order.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
        this.updateById(order);
        // 发送后台信息 -- 取消订单
        Order byId = this.getById(id);
        AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
        adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.ORDER_CANCELED.getCatId());
        adminMsgCreateDTO.setTitle("订单已被取消：" + byId.getOrderSn() + ",金额：" + byId.getTotalAmount());
        adminMsgCreateDTO.setContent("订单编号：" + byId.getOrderSn() + "，订单金额：" + byId.getTotalAmount() + "元，订单取消。");
        adminMsgCreateDTO.setShopId(byId.getShopId());
        adminMsgCreateDTO.setOrderId(byId.getOrderId());
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("orderId", byId.getOrderId());
        adminMsgCreateDTO.setRelatedData(relatedData);
        adminMsgService.createMessage(adminMsgCreateDTO);
        return true;
    }

    @Override
    public void remindDeliver(Integer id) {
        OrderVO orderVO = this.detail(id);
        if (orderVO.getVendorId() == null || orderVO.getVendorId() == 0) {
            throw new GlobalException("非供应商订单不允许操作");
        }
        if (orderVO.getOrderStatus() != OrderStatusEnum.CONFIRMED.getCode()) {
            throw new GlobalException("订单状态错误");
        }

        AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
        adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.QUICK_DELIVERY.getCatId());
        adminMsgCreateDTO.setTitle("发货提醒：" + orderVO.getOrderSn() + ",金额：" + orderVO.getTotalAmount());
        adminMsgCreateDTO.setVendorId(orderVO.getVendorId());
        adminMsgCreateDTO.setContent("收到订单号：" + orderVO.getOrderSn() + "，金额：" + orderVO.getTotalAmount() + "，的催发货提醒，请尽快进行发货");
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("orderId", orderVO.getOrderId());
        adminMsgCreateDTO.setRelatedData(relatedData);
        adminMsgService.createMessage(adminMsgCreateDTO);
    }
}
