package com.tigshop.service.vendor.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.OrderConfigDTO;
import com.tigshop.bean.dto.shop.ShopListDTO;
import com.tigshop.bean.enums.order.AftersalesStatusEnum;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.order.PaymentStatus;
import com.tigshop.bean.enums.order.ShippingStatusEnum;
import com.tigshop.bean.enums.product.DeleteType;
import com.tigshop.bean.enums.product.ProductStatus;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.product.Brand;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.shop.OrderConfig;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.vendor.VendorSettlementOrder;
import com.tigshop.bean.model.vendor.VendorShopBind;
import com.tigshop.bean.model.vendor.product.VendorProductSku;
import com.tigshop.bean.param.vendor.VendorShopBindPageParam;
import com.tigshop.bean.param.vendor.shopbind.VendorOrderBindPageParam;
import com.tigshop.bean.param.vendor.shopbind.VendorProductBindPageParam;
import com.tigshop.bean.query.merchant.MerchantListPageQuery;
import com.tigshop.bean.vo.finance.OrderInvoiceVO;
import com.tigshop.bean.vo.merchant.MerchantVO;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.shop.ShopVO;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.bean.vo.user.UserBaseVO;
import com.tigshop.bean.vo.vendor.shopbind.VendorShopBindVO;
import com.tigshop.bean.vo.vendor.shopbind.VendorShopOrderBindVO;
import com.tigshop.bean.vo.vendor.shopbind.VendorShopProductBindVO;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.order.AftersalesMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.product.BrandMapper;
import com.tigshop.mapper.product.CategoryMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.shop.OrderConfigMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.vendor.VendorSettlementOrderMapper;
import com.tigshop.mapper.vendor.VendorShopBindMapper;
import com.tigshop.mapper.vendor.product.VendorProductSkuMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.im.ImServantService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.vendor.VendorShopBindService;
import com.tigshop.service.vendor.VendorWithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.Constants.DATE_FORMAT;

/**
 * @author Admin
 * @description 针对表【vendor_shop_bind(供应商店铺绑定表)】的数据库操作Service实现
 * @createDate 2025-07-09 10:53:49
 */
@Service
@RequiredArgsConstructor
public class VendorShopBindServiceImpl extends BaseServiceImpl<VendorShopBindMapper, VendorShopBind> implements VendorShopBindService {

    private final ProductMapper productMapper;
    private final OrderService orderService;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final VendorProductSkuMapper vendorProductSkuMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final ShopMapper shopMapper;
    private final OrderConfigMapper orderConfigMapper;
    private final ConfigService configService;
    private final AftersalesMapper aftersalesMapper;
    private final VendorWithdrawService vendorWithdrawService;
    private final ImServantService imServantService;
    private final VendorSettlementOrderMapper vendorSettlementOrderMapper;

    @Override
    public Page<VendorShopBindVO> list(VendorShopBindPageParam param) {
        Page<Object> page = Page.of(param.getPage(), param.getSize());
        Page<VendorShopBindVO> vendorAssociatedShopVOIPage = this.getBaseMapper().pageVendorShopBind(
                page,
                getVendorId(),
                param
        );
        vendorAssociatedShopVOIPage.getRecords().forEach(vendorShopBindVO -> {
            // 自营店铺处理
            if (vendorShopBindVO.getShopId() == 0) {
                vendorShopBindVO.setShopTitle("自营店铺");
                vendorShopBindVO.setMerchantName("自营商户");
                vendorShopBindVO.setShopLogo(configService.getConfigVal(SettingsEnum.SHOP_LOGO));
            }

            // 填充字段
            // 上架商品数量
            vendorShopBindVO.setUpProductCount(productMapper.selectCount(new LambdaQueryWrapper<Product>()
                    .eq(Product::getVendorId, getVendorId())
                    .eq(Product::getShopId, vendorShopBindVO.getShopId())
                    .eq(Product::getProductStatus, ProductStatus.ON_SALE.getCode())
                    .eq(Product::getIsDelete, DeleteType.NOT_DELETE.getCode())
            ));
            // 下架商品数量
            vendorShopBindVO.setDownProductCount(productMapper.selectCount(new LambdaQueryWrapper<Product>()
                    .eq(Product::getVendorId, getVendorId())
                    .eq(Product::getShopId, vendorShopBindVO.getShopId())
                    .eq(Product::getProductStatus, ProductStatus.OFF_SALE.getCode())
                    .eq(Product::getIsDelete, DeleteType.NOT_DELETE.getCode())
            ));
            // 订单量
            vendorShopBindVO.setOrderCount(orderService.count(new LambdaQueryWrapper<Order>()
                    .eq(Order::getVendorId, getVendorId())
                    .eq(Order::getShopId, vendorShopBindVO.getShopId())
                    .eq(Order::getPayStatus, PaymentStatus.PAID.getCode())
            ));
            // 结算金额 (供应商提现金额)
            List<VendorSettlementOrder> list = vendorSettlementOrderMapper.selectList(new LambdaQueryWrapper<VendorSettlementOrder>()
                    .eq(VendorSettlementOrder::getVendorId, getVendorId())
                    .eq(VendorSettlementOrder::getShopId, vendorShopBindVO.getShopId()));
            vendorShopBindVO.setOrderAmount(list.stream()
                    .map(VendorSettlementOrder::getAmount)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO));
        });
        return vendorAssociatedShopVOIPage;
    }

    @Override
    public Page<VendorShopProductBindVO> productList(VendorProductBindPageParam param) {
        Page<Object> page = Page.of(param.getPage(), param.getSize());
        Page<VendorShopProductBindVO> vendorShopProductBindVOPage = this.getBaseMapper().pageProductList(
                page,
                getVendorId(),
                param
        );
        vendorShopProductBindVOPage.getRecords().forEach(vendorShopBindVO -> {
            // 填充字段
            Category category = categoryMapper.selectById(vendorShopBindVO.getProductCategoryId());
            vendorShopBindVO.setProductCategoryName(category != null ? category.getCategoryName() : "");
            Brand brand = brandMapper.selectById(vendorShopBindVO.getProductBrandId());
            vendorShopBindVO.setProductBrandName(brand != null ? brand.getBrandName() : "");

            List<VendorProductSku> vendorProductSkus = vendorProductSkuMapper.selectList(new LambdaQueryWrapper<VendorProductSku>()
                    .eq(VendorProductSku::getVendorProductId, vendorShopBindVO.getVendorProductId())
            );
            // 获取 vendorProductSkus 价格最小的
            vendorShopBindVO.setProductSupplyPrice(vendorProductSkus.stream()
                    .map(VendorProductSku::getSupplyPrice)
                    .min(Comparator.comparing(BigDecimal::doubleValue)).orElse(null));
            vendorShopBindVO.setProductStock(vendorProductSkus.stream()
                    .map(VendorProductSku::getSkuStock)
                    .reduce(0, Integer::sum));
            // 查询未取消订单的 orderItem 相关的数量
            vendorShopBindVO.setSalesVolume(Optional
                    .ofNullable(orderItemMapper.getProductSalesVolumeByProductIdAndShopId(vendorShopBindVO.getId(), param.getShopId()))
                    .orElse(0));

            Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                    .eq(Product::getVendorProductId, vendorShopBindVO.getVendorProductId())
                    .eq(Product::getShopId, param.getShopId())
            );
            vendorShopBindVO.setProductStatus(product.getProductStatus());
        });
        return vendorShopProductBindVOPage;
    }

    @Override
    public Page<OrderVO> orderList(VendorOrderBindPageParam param) {
        Page<Object> page = Page.of(param.getPage(), param.getSize());
        Page<VendorShopOrderBindVO> vendorShopOrderBindVOPage = this.getBaseMapper().pageOrderList(
                page,
                getVendorId(),
                param
        );
        if (CollUtil.isEmpty(vendorShopOrderBindVOPage.getRecords())) {
            return PageUtil.convertPage(page, List.of());
        }

        vendorShopOrderBindVOPage.getRecords().forEach(shopOrderBindVO -> {
            Order order = orderService.getById(shopOrderBindVO.getOrderId());
            BeanUtils.copyProperties(order, shopOrderBindVO);
        });

        List<Integer> userIds = vendorShopOrderBindVOPage.getRecords().stream().map(VendorShopOrderBindVO::getUserId).distinct().toList();
        Map<Integer, User> userMap = new HashMap<>();
        if (CollUtil.isNotEmpty(userIds)) {
            List<User> userList = userMapper.selectBatchIds(userIds);
            userMap = userList.stream().collect(Collectors.toMap(User::getUserId, Function.identity()));
        }

        // 调用方法获取会员信息
        List<Integer> orderIds = vendorShopOrderBindVOPage.getRecords().stream().map(VendorShopOrderBindVO::getOrderId).toList();
        List<OrderItemVO> itemListNew = orderService.getOrderItemList(orderIds);

        // 插入售后数据
        Map<Integer, List<OrderItemVO>> itemMap = itemListNew.stream().collect(Collectors.groupingBy(OrderItemVO::getOrderId));
        // 取出店铺Id
        List<Integer> shopIds = vendorShopOrderBindVOPage.getRecords().stream().map(VendorShopOrderBindVO::getShopId).distinct().toList();
        List<Shop> shopList = shopMapper.selectBatchIds(shopIds);
        Map<Integer, Shop> shopMap = shopList.stream().collect(Collectors.toMap(Shop::getShopId, Function.identity()));

        // 查询店铺结算配置
        List<OrderConfig> orderConfigs = orderConfigMapper.selectList(
                new LambdaQueryWrapper<OrderConfig>()
                        .in(OrderConfig::getShopId, shopIds)
        );
        Map<Integer, OrderConfig> orderConfigMap = orderConfigs.stream().collect(Collectors.toMap(OrderConfig::getShopId, Function.identity()));

        // 转换为VO
        Map<Integer, User> finalUserMap = userMap;
        String afterSalesLimitDays = configService.getConfigVal(SettingsEnum.AFTER_SALES_LIMIT_DAYS);
        String autoDeliveryDays = configService.getConfigVal(SettingsEnum.AUTO_DELIVERY_DAYS);
        List<OrderVO> orderVOList = vendorShopOrderBindVOPage.getRecords().stream()
                .map(order -> {
                    OrderVO orderVO = new OrderVO();
                    BeanUtils.copyProperties(order, orderVO);
                    orderVO.setRegionIds(JsonUtil.jsonToList(order.getRegionIds(), Integer.class));
                    orderVO.setRegionNames(JsonUtil.jsonToList(order.getRegionNames(), String.class));
                    orderVO.setOrderStatusName(OrderStatusEnum.getStatusName(order.getOrderStatus()));
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
                    orderVO.setAvailableActions(orderService.availableActions(orderVO));

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

                    return orderVO;
                }).toList();
        return PageUtil.convertPage(page, orderVOList);
    }

    @Override
    public Page<MerchantVO> merchantList(MerchantListPageQuery param) {
        Page<Object> page = Page.of(param.getPage(), param.getSize());
        Page<MerchantVO> merchantVOPage = this.getBaseMapper().merchantList(
                page,
                getVendorId(),
                param
        );
        return merchantVOPage;
    }

    @Override
    public Page<ShopVO> shopList(ShopListDTO param) {
        Page<Object> page = Page.of(param.getPage(), param.getSize());
        Page<ShopVO> shopVOPage = this.getBaseMapper().shopList(
                page,
                getVendorId(),
                param
        );
        return shopVOPage;
    }
}




