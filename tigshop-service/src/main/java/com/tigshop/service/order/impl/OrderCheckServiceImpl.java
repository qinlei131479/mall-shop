package com.tigshop.service.order.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.dto.cart.CartPromotionParsePriceDTO;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.enums.finance.StatementType;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.msg.MessageTypeEnum;
import com.tigshop.bean.enums.order.*;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.vendor.VendorProductSkuStockBizEnum;
import com.tigshop.bean.model.finance.OrderInvoice;
import com.tigshop.bean.model.o2o.OrderPickupItem;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.o2o.UserPickupInfo;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderAmountDetail;
import com.tigshop.bean.model.order.OrderCouponDetail;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.product.ECard;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.promotion.PointsExchange;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.setting.ShippingTplInfo;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserAddress;
import com.tigshop.bean.model.user.UserCoupon;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.param.finance.orderinvoice.OrderInvoiceSaveParam;
import com.tigshop.bean.param.order.OrderCheckParam;
import com.tigshop.bean.param.settings.shippingtpl.RegionDataParam;
import com.tigshop.bean.param.vendor.product.VendorProductSkuStockParam;
import com.tigshop.bean.query.order.StoreShippingTypeQuery;
import com.tigshop.bean.vo.cart.CartByStoreVO;
import com.tigshop.bean.vo.cart.CartListByStoreVO;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.o2o.DeliveryOptionVO;
import com.tigshop.bean.vo.order.OrderCheckVO;
import com.tigshop.bean.vo.order.OrderSubmitVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.o2o.UserPickupInfoMapper;
import com.tigshop.mapper.promotion.PointsExchangeMapper;
import com.tigshop.service.cart.CartService;
import com.tigshop.service.finance.OrderInvoiceService;
import com.tigshop.service.finance.RecordRateService;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.msgTemplate.MessageService;
import com.tigshop.service.o2o.OrderPickupItemService;
import com.tigshop.service.o2o.StoreProductService;
import com.tigshop.service.o2o.StoreSkuService;
import com.tigshop.service.order.*;
import com.tigshop.service.product.ECardGroupService;
import com.tigshop.service.product.ECardService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.promotion.ProductGiftService;
import com.tigshop.service.promotion.SeckillService;
import com.tigshop.service.salesman.SalesmanOrderService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.MessageTemplateService;
import com.tigshop.service.setting.ShippingTplInfoService;
import com.tigshop.service.setting.ShippingTplService;
import com.tigshop.service.shop.ShopService;
import com.tigshop.service.user.*;
import com.tigshop.service.vendor.VendorProductSkuService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.product.ProductType.PRODUCT_TYPE_CARD;
import static com.tigshop.bean.enums.setting.SettingsEnum.USE_COUPON;
import static com.tigshop.common.constant.Constants.YES;

/**
 * 订单结算
 *
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
@Slf4j
@Service
public class OrderCheckServiceImpl
        implements OrderCheckService {

    @Resource
    OrderService orderService;

    @Resource
    AdminMsgService adminMsgService;

    @Resource
    OrderItemService orderItemService;

    @Resource
    OrderInvoiceService orderInvoiceService;

    @Resource
    SalesmanOrderService salesmanOrderService;

    @Resource
    CartService cartService;

    @Resource
    ShippingTplService shippingTplService;

    @Resource
    ShippingTplInfoService shippingTplInfoService;

    @Resource
    SeckillService seckillService;

    @Resource
    ConfigService configService;

    @Resource
    ProductService productService;

    @Resource
    ProductSkuService productSkuService;

    @Resource
    StoreProductService storeProductService;

    @Resource
    StoreSkuService storeSkuService;

    @Resource
    UserAddressService userAddressService;

    @Resource
    UserService userService;

    @Resource
    UserRankService userRankService;

    @Resource
    MessageTemplateService messageTemplateService;

    @Resource
    UserCouponService userCouponService;

    @Resource
    OrderAmountDetailService orderAmountDetailService;

    @Resource
    OrderCouponDetailService orderCouponDetailService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private PointsExchangeMapper pointsExchangeMapper;

    @Resource
    private TranslatePackageImpl translatePackage;

    @Resource
    private ECardGroupService eCardCardService;

    @Resource
    private ECardService eCardService;

    @Resource
    private ShopService shopService;

    @Resource
    private MessageService messageService;

    @Resource
    private ProductGiftService productGiftService;

    @Resource
    private UserAuthorizeService userAuthorizeService;

    @Resource
    private VendorProductSkuService vendorProductSkuService;
    @Resource
    private RecordRateService recordRateService;
    @Autowired
    private OrderPickupItemService orderPickupItemService;
    @Autowired
    private TigshopProperties tigshopProperties;
    @Autowired
    private UserPickupInfoMapper userPickupInfoMapper;

    @Override
    public OrderInvoice getLastInvoice(Integer invoiceType, Integer titleType) {
        if (invoiceType == null) {
            invoiceType = 0;
        }
        if (titleType == null) {
            titleType = 0;
        }
        return orderInvoiceService.lambdaQuery()
                .eq(OrderInvoice::getInvoiceType, invoiceType)
                .eq(OrderInvoice::getTitleType, titleType)
                .eq(OrderInvoice::getUserId, SecurityUtils.getCurrentUserId())
                .orderByDesc(OrderInvoice::getId)
                .last("limit 1")
                .one();
    }

    @Override
    public OrderCheckVO getOrderCheckData(OrderCheckParam param) {
        // 检查用户是否为企业认证用户与B2B类型
        userService.checkUserCompanyAuth();

        CartListByStoreVO cartListByStore = cartService.getCartListByStore(true, param.getFlowType(), null, param.getShopId());
        String childAreaNeedRegion = configService.getConfigByCode("childAreaNeedRegion").getBizVal();
        if ("1".equals(childAreaNeedRegion)) {
            for (CartByStoreVO cartByStoreVO : cartListByStore.getCartList()) {
                for (CartVO cart : cartByStoreVO.getCarts()) {
                    if (cart.getFixedShippingType() == 2) {
                        shippingTplInfoService.verifyUserAddress(Math.toIntExact(cart.getShippingTplId()), param.getAddressId());
                    }
                }
            }
        }

        CartListByStoreVO cartList = cartService.buildCartPromotion(cartListByStore, param.getFlowType(),
                param.getUseDefaultCouponIds(),
                param.getUseCouponIds());

        /*UserAddressListDTO userAddressListDTO = new UserAddressListDTO();
        userAddressListDTO.setSortOrder("desc");
        userAddressListDTO.setSortField("is_selected");
        // 获取用户的地址列表
        ListResVO<UserAddressVO, UserAddressListDTO> userAddressListResVO = userAddressService.list(userAddressListDTO);
        orderCheckVO.setAddressList(userAddressListResVO.getFilterResult());
        // 初始化时候默认选中第一个地址
        if (!userAddressListResVO.getFilterResult().isEmpty() && orderCheckDTO.getSelectFrom() == 1) {
            orderCheckDTO.setAddressId(userAddressListResVO.getFilterResult().getFirst().getAddressId());
        }
        //获得可用支付方式
        List<OrderCheckVO.PaymentType> availablePaymentType = getAvailablePaymentType();
        if (orderCheckDTO.getSelectFrom() == 1 && !availablePaymentType.isEmpty()) {
            orderCheckDTO.setPayTypeId(availablePaymentType.getFirst().getTypeId());
        }
        orderCheckVO.setAvailablePaymentType(availablePaymentType);

        //获得店铺的物流方式
        List<List<OrderCheckVO.ShippingType>> storeShippingType = getStoreShippingType();
        orderCheckVO.setStoreShippingType(storeShippingType);
        if (orderCheckDTO.getSelectFrom() != null && orderCheckDTO.getSelectFrom() == 1 && orderCheckVO.getStoreShippingType() != null && !orderCheckVO.getStoreShippingType().isEmpty()
                && orderCheckVO.getStoreShippingType().getFirst() != null && !orderCheckVO.getStoreShippingType().getFirst().isEmpty() && orderCheckVO.getStoreShippingType().getFirst().getFirst() != null) {
            List<OrderCheckDTO.ShippingType> defaultShippingTypeList = new ArrayList<>();
            //默认选中每个storeShippingType里面的每第一个配送类型然后组装成list然后set到orderCheckDTO.setShippingType
            orderCheckVO.getStoreShippingType().forEach(storeShippingTypeItem -> {
                OrderCheckDTO.ShippingType shippingType = new OrderCheckDTO.ShippingType();
                shippingType.setTypeName(storeShippingTypeItem.getFirst().getShippingTypeName());
                shippingType.setTypeId(storeShippingTypeItem.getFirst().getShippingTypeId());
                shippingType.setShopId(storeShippingTypeItem.getFirst().getShopId());
                defaultShippingTypeList.add(shippingType);
            });
            orderCheckDTO.setShippingType(defaultShippingTypeList);
        }*/
        List<Integer> useCouponIds = new ArrayList<>();
        List<Integer> selectUserCouponIds = CollUtil.isNotEmpty(param.getSelectUserCouponIds()) ? param.getSelectUserCouponIds() : new ArrayList<>();
        if (param.getSelectFrom() == SelectFromEnum.FIRST_LOAD.getValue() || (param.getSelectFrom() == SelectFromEnum.CHECK_UPDATE.getValue() && CollUtil.isEmpty(param.getSelectUserCouponIds()))) {
            for (CartByStoreVO cartByStoreVO : cartList.getCartList()) {
                if (cartByStoreVO.getUsedPromotions() != null) {
                    cartByStoreVO.getUsedPromotions().forEach(promotionVO -> {
                        if (promotionVO.getType() == 2) {
                            useCouponIds.add(promotionVO.getRelationId());
                            UserCoupon userCoupon = userCouponService.getUserCouponByCouponId(SecurityUtils.getCurrentUserId(), promotionVO.getRelationId());
                            selectUserCouponIds.add(userCoupon.getId());
                        }
                    });
                }
            }
            param.setUseCouponIds(useCouponIds);
            param.setSelectUserCouponIds(selectUserCouponIds);
        }

        //获得用户数据
        User user = userService.getById(SecurityUtils.getCurrentUserId());

        /*orderCheckVO.setBalance(getUserBalance(user));

        orderCheckVO.setPoints(getUserPoints(user));*/


        OrderCheckVO.Item item = new OrderCheckVO.Item();
        BeanUtil.copyProperties(param, item);

        OrderCheckVO orderCheckVO = new OrderCheckVO();
        orderCheckVO.setCartList(cartList.getCartList());

        OrderCheckVO.CouponList couponList = getCouponListByPromotion(cartList, param.getUseCouponIds(), param.getSelectUserCouponIds());
        orderCheckVO.setCouponList(couponList);

        orderCheckVO.setSelectUserCouponIds(selectUserCouponIds);

        OrderCheckVO.Total total = getTotalFee(param, cartList, user);
        orderCheckVO.setTotal(total);

        orderCheckVO.setItem(item);

        List<String> miniProgramTemplateIds = messageTemplateService.getMiniProgramTemplateIds();
        orderCheckVO.setTmplIds(miniProgramTemplateIds);

        orderCheckVO.setFlowType(param.getFlowType());
        orderCheckVO.setUseCouponIds(param.getUseCouponIds());

        Integer userPoints = getOrderAvailablePoints(cartListByStore, user);
        orderCheckVO.setAvailablePoints(userPoints);

        DeliveryOptionVO deliveryOption = productService.getDeliveryOption(param.getShopId());
        orderCheckVO.setDeliveryOption(deliveryOption);
        return orderCheckVO;
    }

    private BigDecimal getUserBalance(User user) {
        return user.getBalance().compareTo(BigDecimal.valueOf(0)) > 0 ? user.getBalance() : BigDecimal.valueOf(0);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderSubmitVO submit(OrderCheckParam param) {
        // 获取是否默认优惠券，清空使用的优惠券
        String useCoupon = configService.getConfigVal(USE_COUPON);
        param.submitInitParam(useCoupon);

        if ("wechat".equals(HeaderUtils.getClientType())) {
            if (!userAuthorizeService.checkUserIsAuthorize(SecurityUtils.getCurrentUserId(), 1)) {
                throw new GlobalException(translatePackage.translate("请先授权登录！"), HttpStatus.USER_NOT_BIND_WECHAT);
            }
        }

        String closeOrder = configService.getConfigVal(SettingsEnum.CLOSE_ORDER);
        Assert.isFalse("1".equals(closeOrder), () -> new GlobalException(translatePackage.translate("商场正在维护已停止下单！")));

        CartListByStoreVO cartListByStore = cartService.getCartListByStore(true, param.getFlowType(), null, param.getShopId());
        String childAreaNeedRegion = configService.getConfigByCode("childAreaNeedRegion").getBizVal();
        if ("1".equals(childAreaNeedRegion)) {
            for (CartByStoreVO cartByStoreVO : cartListByStore.getCartList()) {
                for (CartVO cart : cartByStoreVO.getCarts()) {
                    if (cart.getFixedShippingType() == 2) {
                        Assert.isTrue(shippingTplInfoService.verifyUserAddress(Math.toIntExact(cart.getShippingTplId()), param.getAddressId()), () -> new GlobalException("该地区不支持配送"));
                    }
                }
            }
        }

        CartListByStoreVO cartList = cartService.buildCartPromotion(cartListByStore, param.getFlowType(), 0, param.getUseCouponIds());
        Assert.isFalse(ObjectUtil.isEmpty(cartList.getCartList()), () -> new GlobalException("请添加商品"));

        // 获得用户数据
        User user = userService.getById(SecurityUtils.getCurrentUserId());
        OrderCheckVO.Total totalFee = getTotalFee(param, cartList, user);

        // 订单店铺金额明细
        List<OrderAmountDetail> orderAmountList = new ArrayList<>();
        // 订单店铺优惠券明细
        List<OrderCouponDetail> orderCouponDetailList = new ArrayList<>();
        Map<String, Integer> salesmanOrder = new HashMap<>();
        List<Integer> cartIds = new ArrayList<>();
        List<OrderItem> orderItemList = new ArrayList<>();
        List<ECard> eCards = new ArrayList<>();
        Integer defaultVendorId = 0;
        for (CartByStoreVO cartByStore : cartList.getCartList()) {
            OrderAmountDetail orderAmountDetail = cartByStore.creatOrderAmountDetail(totalFee);
            orderAmountList.add(orderAmountDetail);

            List<OrderCouponDetail> orderCouponDetails = cartByStore.createOrderCouponDetails();
            orderCouponDetailList.addAll(orderCouponDetails);

            for (CartVO cart : cartByStore.getCarts()) {
                Shop shop = shopService.getById(cart.getShopId());
                Assert.isFalse(shop != null && shop.getStatus() != 1, () -> new GlobalException("该店铺暂停运营，所属商品无法购买"));

                cartService.checkOrderProductLimitNumber(cart.getProductId(), user.getUserId(), cart.getQuantity(), null);
                Assert.isFalse(cart.getProductStatus() == 0, () -> new GlobalException(translatePackage.translate(cart.getProductName() + " 商品已下架~")));
                Assert.isFalse(cart.getStock() < cart.getQuantity(), () -> new GlobalException(translatePackage.translate(cart.getProductName() + " 商品库存不足~")));

                cartIds.add(cart.getCartId());
                OrderItem orderItem = new OrderItem();
                orderItem.setUserId(SecurityUtils.getCurrentUserId());
                orderItem.setPrice(cart.getPrice());
                orderItem.setQuantity(cart.getQuantity());
                orderItem.setProductId(cart.getProductId());
                orderItem.setProductName(cart.getProductName());
                orderItem.setProductSn(cart.getProductSn());
                orderItem.setPicThumb(cart.getPicThumb());
                orderItem.setSkuId(cart.getSkuId());
                orderItem.setSkuData(cart.getSkuData().toJSONString(1));
                orderItem.setProductType(cart.getProductType());
                orderItem.setShopId(cart.getShopId());
                orderItem.setPromotionData(cart.getActivityInfo() != null ? JSONUtil.toJsonStr(cart.getActivityInfo()) : "");
                orderItem.setOriginPrice(cart.getOriginPrice() != null ? cart.getOriginPrice() : BigDecimal.ZERO);
                orderItem.setIsSeckill(cart.getIsSeckill() != null ? cart.getIsSeckill() : 0);
                orderItem.setExtraSkuData(cart.getExtraSkuData() != null ? JSONUtil.toJsonStr(cart.getExtraSkuData()) : "");
                orderItem.setIsGift(0);
                orderItem.setSuppliersId(cart.getSuppliersId() != null ? cart.getSuppliersId() : 0);
                orderItem.setCardGroupName(cart.getProductType() == 3 ? eCardCardService.getById(cart.getCardGroupId()).getGroupName() : "");
                if (cart.getSalesmanId() != null) {
                    salesmanOrder.put(cart.getProductId() + "_" + cart.getSkuId(), cart.getSalesmanId());
                }
                // 为卡密商品订单时分配电子卡券
                if (cart.getProductType() == PRODUCT_TYPE_CARD.getCode()) {
                    eCards = eCardService.getNewCardByGroupId(cart.getCardGroupId(), cart.getQuantity());
                    Assert.isFalse(eCards.size() < cart.getQuantity(), () -> new GlobalException(translatePackage.translate("卡券库存不足")));
                }
                orderItem.setVendorId(cart.getVendorId());
                orderItem.setVendorProductId(cart.getVendorProductId());
                orderItem.setVendorProductSkuId(cart.getVendorProductSkuId());
                orderItemList.add(orderItem);
                defaultVendorId = cart.getVendorId();
            }

            if (!cartByStore.getGift().isEmpty()) {
                for (CartPromotionParsePriceDTO.Gift gift : cartByStore.getGift()) {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setUserId(SecurityUtils.getCurrentUserId());
                    orderItem.setPrice(BigDecimal.ZERO);
                    orderItem.setQuantity(gift.getNum());
                    orderItem.setProductId(gift.getProductId());
                    orderItem.setProductName(gift.getProductName());
                    orderItem.setProductSn(gift.getProductSn());
                    orderItem.setPicThumb(gift.getPicThumb());
                    orderItem.setSkuId(gift.getSkuId());
                    orderItem.setSkuData(gift.getSkuData().toString());
                    orderItem.setProductType(gift.getProductType());
                    orderItem.setShopId(gift.getShopId());
                    orderItem.setProductType(gift.getProductType());
                    orderItem.setIsGift(1);
                    orderItem.setPromotionData(JSONUtil.toJsonStr(gift));
                    orderItemList.add(orderItem);
                }
            }

        }

        UserAddressVO address = userAddressService.getUserSelectedAddress();
        Assert.notNull(address, () -> new GlobalException("请选择收货地址"));

        Order order = new Order();
        order.setOrderSn(creatNewOrderSn());
        order.setUserId(SecurityUtils.getCurrentUserId());
        order.setOrderStatus(OrderStatusEnum.PENDING.getCode());
        order.setShippingStatus(ShippingStatusEnum.PENDING.getCode());
        order.setPayStatus(PaymentStatus.UNPAID.getCode());
        order.setAddTime(StringUtils.getCurrentTime());

        if (tigshopProperties.getIsO2o() == 1) {
            // 如果是自提的话那就添加自提信息
            OrderCheckParam.ShippingType shippingType = param.getShippingType().get(order.getShopId());
            if (shippingType != null && shippingType.getPickupId() != null) {
                log.info("自提商品订单");
            } else {
                order.setConsignee(address.getConsignee());
                order.setRegionIds(JsonUtil.toJson(address.getRegionIds()));
                order.setRegionNames(JsonUtil.toJson(address.getRegionNames()));
                order.setAddress(address.getAddress());
                order.setAddressData(JSONUtil.toJsonStr(address));
                order.setMobile(address.getMobile());
                order.setEmail(address.getEmail());
            }
        } else {
            order.setConsignee(address.getConsignee());
            order.setRegionIds(JsonUtil.toJson(address.getRegionIds()));
            order.setRegionNames(JsonUtil.toJson(address.getRegionNames()));
            order.setAddress(address.getAddress());
            order.setAddressData(JSONUtil.toJsonStr(address));
            order.setMobile(address.getMobile());
            order.setEmail(address.getEmail());
        }

        order.setBuyerNote(param.getBuyerNote());
        order.setPayTypeId(param.getPayTypeId());
        order.setUsePoints(param.getUsePoint());
        order.setReferrerUserId(userService.getUserReferrerId(SecurityUtils.getCurrentUserId()));
        order.setShopId(0);
        order.setIsStoreSplited(0);
        order.setTotalAmount(totalFee.getTotalAmount());
        order.setPaidAmount(totalFee.getPaidAmount());
        order.setUnpaidAmount(totalFee.getUnpaidAmount());
        order.setProductAmount(totalFee.getProductAmount());
        order.setCouponAmount(totalFee.getCouponAmount());
        order.setPointsAmount(totalFee.getPointsAmount());
        order.setBalance(totalFee.getBalance());
        order.setServiceFee(totalFee.getServiceFee());
        order.setShippingFee(totalFee.getShippingFee());
        order.setInvoiceFee(BigDecimal.ZERO);
        order.setDiscountAmount(totalFee.getDiscountAmount());
        order.setOrderExtension(new JSONObject().toString());
        order.setOnlinePaidAmount(BigDecimal.ZERO);
        order.setBalance(totalFee.getBalance());
        order.setOfflinePaidAmount(BigDecimal.ZERO);
        order.setOrderSource(HeaderUtils.getClientType());
        order.setInvoiceData(param.getInvoiceData() != null && param.getInvoiceData().getInvoiceType() != null ? JSONUtil.toJsonStr(param.getInvoiceData()) : "");
        order.setIsExchangeOrder(totalFee.getExchangePoints() > 0 ? 1 : 0);
        order.setOrderType(param.getFlowType());
        order.setVendorId(defaultVendorId);
        order.setIsStoreSplited(0);
        if (cartList.getCartList().size() == 1) {
            // 所有商品都是来自同一店铺时
//
            order.setShopId(cartList.getCartList().getFirst().getShopId());

            OrderCheckParam.ShippingType shippingType = param.getShippingType().get(order.getShopId());
            if (shippingType != null) {
                order.setShippingTypeId(shippingType.getTypeId());
                order.setShippingTypeName(shippingType.getTypeName());
            }

        }
        while (orderService.count(new LambdaQueryWrapper<Order>().eq(Order::getOrderSn, order.getOrderSn())) > 0) {
            order.setOrderSn(creatNewOrderSn());
        }

        try {
            if (ObjectUtil.isEmpty(order.getAddress())) {
                order.setAddress("");
            }
            if (ObjectUtil.isEmpty(order.getRegionIds())) {
                order.setRegionIds("");
            }
            if (ObjectUtil.isEmpty(order.getRegionNames())) {
                order.setRegionNames("");
            }
            boolean save = orderService.save(order);
            Assert.isTrue(save, () -> new GlobalException(translatePackage.translate("订单创建失败")));
            if (param.getUsePoint() != null && param.getUsePoint() > 0) {
                String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
                userService.decPoints(param.getUsePoint(), SecurityUtils.getCurrentUserId(), "订单支付扣除" + (StrUtil.isNotBlank(configVal) ? configVal : "积分"));
            }
            if (param.getUseBalance() != null && param.getUseBalance().compareTo(BigDecimal.ZERO) > 0) {
                userService.decBalance(order.getBalance(), SecurityUtils.getCurrentUserId(), "订单支付");
            }
            if (!cartIds.isEmpty()) {
                cartService.removeBatchByIds(cartIds, true);
            }
            if (param.getUseCouponIds() != null && !param.getUseCouponIds().isEmpty()) {
                userCouponService.useCoupon(param.getUseCouponIds(), SecurityUtils.getCurrentUserId(), order.getOrderId());
            }

            //循环orderItems
            for (OrderItem orderItem : orderItemList) {
                orderItem.setOrderId(order.getOrderId());
                orderItem.setOrderSn(order.getOrderSn());
                if (orderItem.getVendorId() != null && orderItem.getVendorId() > 0) {
                    //供应商商品减库存
                    vendorProductSkuService.decStockAndSalesVolume(new VendorProductSkuStockParam(Long.valueOf(orderItem.getVendorProductSkuId()), orderItem.getQuantity(), VendorProductSkuStockBizEnum.ORDER_CREATE, "下单减库存"));
                    orderItem.setVendorProductSupplyPrice(vendorProductSkuService.getById(orderItem.getVendorProductSkuId()).getSupplyPrice());
                } else {
                    // 店铺商品减库存
                    if (orderItem.getSkuId() > 0) {
                        handleSkuStock(orderItem);
                    } else {
                        handleProductStock(orderItem);
                    }
                    // O2O 模式下增加门店销量
                    if (tigshopProperties.getIsO2o() == 1) {
                        increaseShopSales(orderItem);
                    }
                }

                //增加秒杀销量
                if (orderItem.getIsSeckill() != null && orderItem.getIsSeckill() == 1) {
                    seckillService.incSales(orderItem.getProductId(), orderItem.getSkuId(), orderItem.getQuantity());
                }
                //减少秒杀库存
                if (orderItem.getIsSeckill() != null && orderItem.getIsSeckill() == 1) {
                    seckillService.decStock(orderItem.getProductId(), orderItem.getSkuId(), orderItem.getQuantity());
                }
                //增加商品销量
                productService.incSales(orderItem.getProductId(), orderItem.getQuantity());
                //减少赠品库存
                if (orderItem.getIsGift() == 1) {
                    CartPromotionParsePriceDTO.Gift gift = JSONUtil.toBean(orderItem.getPromotionData(), CartPromotionParsePriceDTO.Gift.class);
                    productGiftService.decStock(gift.getGiftId(), orderItem.getQuantity());
                }

                //商品库存预警 + 无货 --- 发送后台信息
                int productStock = productSkuService.getProductStock(orderItem.getProductId(), orderItem.getSkuId());
                if (0 < productStock && productStock <= 100) {
                    //记录管理员消息
                    AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
                    adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.STOCK_WARNING.getCatId());
                    adminMsgCreateDTO.setTitle("商品库存预警:" + orderItem.getProductName());
                    adminMsgCreateDTO.setContent("您的商品【" + orderItem.getProductName() + "】库存不足,还剩" + productStock + ",预警库存为100,请及时补充库存!");
                    adminMsgCreateDTO.setShopId(orderItem.getShopId());
                    adminMsgCreateDTO.setProductId(orderItem.getProductId());
                    Map<String, Object> relatedData = new HashMap<>();
                    relatedData.put("productId", orderItem.getProductId());
                    adminMsgCreateDTO.setRelatedData(relatedData);
                    adminMsgCreateDTO.setVendorId(orderItem.getVendorId());
                    adminMsgService.createMessage(adminMsgCreateDTO);
                }
                if (productStock <= 0) {
                    AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
                    adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.OUT_OF_STOCK.getCatId());
                    adminMsgCreateDTO.setTitle("商品无货:" + orderItem.getProductName());
                    adminMsgCreateDTO.setContent("您的商品【" + orderItem.getProductName() + "】库存已经无货，请及时补充库存!");
                    adminMsgCreateDTO.setShopId(orderItem.getShopId());
                    adminMsgCreateDTO.setProductId(orderItem.getProductId());
                    Map<String, Object> relatedData = new HashMap<>();
                    relatedData.put("productId", orderItem.getProductId());
                    adminMsgCreateDTO.setRelatedData(relatedData);
                    adminMsgService.createMessage(adminMsgCreateDTO);
                }

            }
            orderItemService.saveBatch(orderItemList);

            salesmanOrderService.addSalesmanOrder(orderItemList, salesmanOrder);

            for (OrderItem item : orderItemList) {
                //封装卡密信息
                if (!eCards.isEmpty() && item.getProductType() == 3) {
                    for (ECard eCard : eCards) {
                        eCard.setIsUse(1);
                        eCard.setOrderId(order.getOrderId());
                        eCard.setOrderItemId(item.getItemId());
                    }
                }
            }

            orderService.addLog(order.getOrderId(), "会员提交订单");

            if (!orderAmountList.isEmpty()) {
                for (OrderAmountDetail orderAmountDetail : orderAmountList) {
                    orderAmountDetail.setOrderId(order.getOrderId());
                }
                orderAmountDetailService.saveBatch(orderAmountList);
            }
            if (!orderCouponDetailList.isEmpty()) {
                for (OrderCouponDetail orderCouponDetail : orderCouponDetailList) {
                    orderCouponDetail.setOrderId(order.getOrderId());
                }
                orderCouponDetailService.saveBatch(orderCouponDetailList);
            }

            if (param.getInvoiceData() != null && param.getInvoiceData().getInvoiceType() != null) {
                OrderInvoiceSaveParam orderInvoiceSaveParam = new OrderInvoiceSaveParam();
                BeanUtils.copyProperties(param.getInvoiceData(), orderInvoiceSaveParam);
                orderInvoiceSaveParam.setUserId(user.getUserId());
                orderInvoiceSaveParam.setOrderId(order.getOrderId());
                orderInvoiceSaveParam.setStatus(0);
                orderInvoiceSaveParam.setAmount(order.getTotalAmount());
                if (param.getInvoiceData().getInvoiceType() == 2) {
                    orderInvoiceSaveParam.setTitleType(2);
                }
                orderInvoiceSaveParam.setAddTime(StringUtils.getCurrentTime());
                orderInvoiceService.create(orderInvoiceSaveParam);
                OrderInvoice orderInvoice = orderInvoiceService.lambdaQuery().eq(OrderInvoice::getOrderId, order.getOrderId()).one();
                order.setInvoiceData(JSONUtil.toJsonStr(orderInvoice));
                orderService.updateById(order);

                AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
                adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.INVOICE_REQUEST.getCatId());
                adminMsgCreateDTO.setTitle("您有新的发票申请,申请金额：" + order.getTotalAmount() + "发票抬头:" + orderInvoice.getCompanyName());
                adminMsgCreateDTO.setContent("用户【" + user.getUsername() + "】针对订单【" + order.getOrderSn() + "】提交了发票申请");
                adminMsgCreateDTO.setShopId(0);
                adminMsgCreateDTO.setOrderId(order.getOrderId());
                Map<String, Object> relatedData = new HashMap<>();
                relatedData.put("orderId", order.getOrderId());
                relatedData.put("orderInvoiceId", orderInvoice.getId());
                adminMsgCreateDTO.setRelatedData(relatedData);
                adminMsgService.createMessage(adminMsgCreateDTO);
            }
            if (order.getUnpaidAmount().compareTo(BigDecimal.ZERO) <= 0) {
                orderService.updateOrderMoney(order);

                if (!eCards.isEmpty()) {
                    eCardService.updateBatchById(eCards);
                }

                // 订单交易成功获取成长值
                userRankService.getRankGrowth(order.getUserId());
            } else {
                messageService.sendUserMessage(order.getUserId(), order.getOrderId(), MessageTypeEnum.NEW_ORDER);
            }
            //加入自动取消队列
            if (order.getUnpaidAmount().compareTo(BigDecimal.ZERO) > 0 && order.getPayTypeId() != PaymentType.OFFLINE.getCode()) {
                ConfigPO autoCancelOrderMinuteConfig = configService.getConfigByCode(SettingsEnum.AUTO_CANCEL_ORDER_MINUTE.getBizCode());
                int autoCancelOrderMinute = Integer.parseInt(autoCancelOrderMinuteConfig.getBizVal());
                if (autoCancelOrderMinute > 0) {
                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.DELAY_EXCHANGE,
                            RabbitMQConfig.ORDER_CANCEL_ROUTING_KEY,
                            order.getOrderId(),
                            message -> {
                                message.getMessageProperties().setHeader("x-delay", autoCancelOrderMinute * 60000);
                                return message;
                            });
                }
            }
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            throw new GlobalException(translatePackage.translate("订单提交失败，请重试"));
        }
        OrderSubmitVO orderSubmitVO = new OrderSubmitVO();
        orderSubmitVO.setOrderId(order.getOrderId());
        orderSubmitVO.setReturnType(order.getUnpaidAmount().compareTo(BigDecimal.ZERO) > 0 ? 1 : 2);

        // 添加当前订单的费率
        recordRateService.saveRecordRate(order.getOrderId(), StatementType.ORDER.getCode(),
                order.getShopId() != null ? order.getShopId() : 0,
                order.getVendorId() != null ? order.getVendorId() : 0);


        if (tigshopProperties.getIsO2o() == 1) {
            // 如果是自提的话那就添加自提信息
            OrderCheckParam.ShippingType shippingType = param.getShippingType().get(order.getShopId());
            if (shippingType != null && shippingType.getPickupId() != null) {
                // 自提信息
                UserPickupInfo validUserPickupInfo = getValidUserPickupInfo(order.getUserId());

                Integer userPickupId;
                if (validUserPickupInfo != null) {
                    userPickupId = validUserPickupInfo.getUserPickupId();
                } else {
                    throw new GlobalException("请先添加自提信息");
                }

                param.getShippingType().forEach((key, value) -> {
                    if (value.getPickupId() != null) {
                        OrderPickupItem orderPickupItem = OrderPickupItem.builder()
                                .orderId(order.getOrderId())
                                .shopId(value.getShopId())
                                .pickupId(value.getPickupId())
                                .expectPickupTime(value.getExpectPickupTime())
                                .userPickupId(userPickupId)
                                .build();
                        orderPickupItemService.saveOrderPickupItem(orderPickupItem);
                    }
                });
            }
        }

        return orderSubmitVO;
    }

    /**
     * 获取用户自提信息
     *
     * @param userId 用户ID
     * @return 用户自提信息
     */
    public UserPickupInfo getValidUserPickupInfo(Integer userId) {
        // 1. 优先取已选中的
        UserPickupInfo selected = userPickupInfoMapper.selectOne(
                Wrappers.lambdaQuery(UserPickupInfo.class)
                        .eq(UserPickupInfo::getUserId, userId)
                        .eq(UserPickupInfo::getIsSelected, YES)
                        .last("limit 1")
        );
        if (selected != null) {
            return selected;
        }

        // 2. 再取默认的
        UserPickupInfo defaultInfo = userPickupInfoMapper.selectOne(
                Wrappers.lambdaQuery(UserPickupInfo.class)
                        .eq(UserPickupInfo::getUserId, userId)
                        .eq(UserPickupInfo::getIsDefault, YES)
                        .last("limit 1")
        );
        if (defaultInfo != null) {
            return defaultInfo;
        }

        // 3. 再取任意一条
        UserPickupInfo any = userPickupInfoMapper.selectOne(
                Wrappers.lambdaQuery(UserPickupInfo.class)
                        .eq(UserPickupInfo::getUserId, userId)
                        .last("limit 1")
        );
        return any; // 可能为 null
    }

    /**
     * 处理 SKU 商品库存扣减
     */
    private void handleSkuStock(OrderItem orderItem) {
        if (tigshopProperties.getIsO2o() != 1) {
            decStockSkuAndProduct(orderItem);
            return;
        }

        Product product = productService.getById(orderItem.getProductId());
        if (!Objects.equals(product.getShopId(), orderItem.getShopId())) {
            handleCrossStoreSkuStock(orderItem);
        } else {
            decStockSkuAndProduct(orderItem);
        }
    }

    /**
     * 处理普通商品库存扣减
     */
    private void handleProductStock(OrderItem orderItem) {
        // 卡券商品不用减库存
        if (orderItem.getProductType() == PRODUCT_TYPE_CARD.getCode()) {
            return;
        }

        if (tigshopProperties.getIsO2o() != 1) {
            decStockProductOnly(orderItem);
            return;
        }

        Product product = productService.getById(orderItem.getProductId());
        if (!Objects.equals(product.getShopId(), orderItem.getShopId())) {
            handleCrossStoreProductStock(orderItem);
        } else {
            decStockProductOnly(orderItem);
        }
    }

    /**
     * 跨门店 SKU 商品逻辑
     */
    private void handleCrossStoreSkuStock(OrderItem orderItem) {
        ConfigPO soloStock = configService.getConfigByCode(SettingsEnum.STORE_USE_SOLO_PRODUCT_STOCK.getBizCode());
        ConfigPO totalStock = configService.getConfigByCode(SettingsEnum.STORE_USE_TOTAL_PRODUCT_STOCK.getBizCode());

        if ("1".equals(soloStock.getBizVal())) {
            StoreProduct storeProduct = storeProductService.decStock(orderItem.getShopId(), orderItem.getProductId(), orderItem.getQuantity());
            storeSkuService.decStock(storeProduct, orderItem.getShopId(), orderItem.getSkuId(), orderItem.getQuantity());
        }
        if ("1".equals(totalStock.getBizVal())) {
            decStockSkuAndProduct(orderItem);
        }
    }

    /**
     * 跨门店 普通商品逻辑
     */
    private void handleCrossStoreProductStock(OrderItem orderItem) {
        ConfigPO soloStock = configService.getConfigByCode(SettingsEnum.STORE_USE_SOLO_PRODUCT_STOCK.getBizCode());
        ConfigPO totalStock = configService.getConfigByCode(SettingsEnum.STORE_USE_TOTAL_PRODUCT_STOCK.getBizCode());

        if ("1".equals(soloStock.getBizVal())) {
            storeProductService.decStock(orderItem.getShopId(), orderItem.getProductId(), orderItem.getQuantity());
        }
        if ("1".equals(totalStock.getBizVal())) {
            decStockProductOnly(orderItem);
        }
    }

    /**
     * 扣减 SKU + 商品库存
     */
    private void decStockSkuAndProduct(OrderItem orderItem) {
        productSkuService.decStock(orderItem.getSkuId(), orderItem.getQuantity());
        productService.decStock(orderItem.getProductId(), orderItem.getQuantity());
    }

    /**
     * 扣减 商品库存
     */
    private void decStockProductOnly(OrderItem orderItem) {
        productService.decStock(orderItem.getProductId(), orderItem.getQuantity());
    }

    /**
     * 增加门店销量
     */
    private void increaseShopSales(OrderItem orderItem) {
        try {
            Shop shop = shopService.getById(orderItem.getShopId());
            shop.setShopSales(shop.getShopSales() + orderItem.getQuantity());
            shopService.updateById(shop);
        } catch (Exception e) {
            log.error("增加门店销量失败", e);
        }
    }


    private String creatNewOrderSn() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        String formattedDate = LocalDateTime.now().format(formatter);
        Random random = new Random();
        int randomNumber = 10000000 + random.nextInt(90000000);
        return formattedDate + randomNumber;
    }

    //计算可用积分
    private Integer getOrderAvailablePoints(CartListByStoreVO cartList, User user) {
        Integer availablePoints = getAmountValuePoints(cartList.getTotal().getProductAmount());
        Integer amountMaxValuePoints = getAmountMaxValuePoints(cartList.getTotal().getProductAmount());
        availablePoints = Math.min(availablePoints, amountMaxValuePoints);
        return Math.min(availablePoints, getUserPoints(user));
    }

    private OrderCheckVO.CouponList getCouponListByPromotion(CartListByStoreVO cartList, List<Integer> useCouponIds, List<Integer> selectCouponIds) {
        OrderCheckVO.CouponList couponList = new OrderCheckVO.CouponList();
        List<OrderCheckVO.Coupon> enableCoupons = new ArrayList<>();
        List<OrderCheckVO.Coupon> disableCoupons = new ArrayList<>();
        for (CartByStoreVO store : cartList.getCartList()) {
            // 获取当前用户在当前店铺下，符合时间要求且未使用的所有优惠券
            List<OrderCheckVO.Coupon> userCoupons = userCouponService.listUserCouponList(SecurityUtils.getCurrentUserId(), store.getShopId());
            List<OrderCheckVO.Coupon> usedCoupons = new ArrayList<>();

            // 遍历每张优惠券，判断是否能用于当前促销
            for (OrderCheckVO.Coupon coupon : userCoupons) {
                if (CollUtil.isNotEmpty(store.getEnableUsePromotion())) {
                    for (PromotionVO promotionVO : store.getEnableUsePromotion()) {
                        if (ObjectUtil.equals(promotionVO.getRelationId(), coupon.getCouponId())) {
                            boolean selected = CollUtil.isNotEmpty(useCouponIds) && useCouponIds.contains(coupon.getCouponId()) &&
                                    CollUtil.isNotEmpty(selectCouponIds) && selectCouponIds.contains(coupon.getUserCouponId());
                            coupon.setSelected(selected);
                            enableCoupons.add(coupon);
                            usedCoupons.add(coupon);
                            break; // 一张券只匹配一次促销
                        }
                    }
                }
            }

            // 不在 enableCoupons 中的，才是 disableCoupons
            for (OrderCheckVO.Coupon coupon : userCoupons) {
                if (!usedCoupons.contains(coupon)) {
                    disableCoupons.add(coupon);
                }
            }
        }

        couponList.setEnableCoupons(CollUtil.distinct(enableCoupons));
        couponList.setDisableCoupons(CollUtil.distinct(disableCoupons));
        return couponList;
    }


    //计算积分能抵多少金额
    private BigDecimal getPointValueAmount(BigDecimal point) {
        ConfigPO integralScaleConfig = configService.getConfigByCode(SettingsEnum.INTEGRAL_SCALE.getBizCode());
        BigDecimal integralScale = new BigDecimal(integralScaleConfig.getBizVal());
        return integralScale.multiply(point.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    }

    //获取用户积分
    private Integer getUserPoints(User user) {
        return user.getPoints() > 0 ? user.getPoints() : 0;
    }

    //获取金额对应的积分值
    private Integer getAmountValuePoints(BigDecimal amount) {
        String integralScaleVal = configService.getConfigVal(SettingsEnum.INTEGRAL_SCALE);
        BigDecimal integralScale = new BigDecimal(integralScaleVal);
        return integralScale.compareTo(BigDecimal.ZERO) > 0 ?
                amount.divide(integralScale, 0, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).intValue() : 0;
    }

    //获取金额最多使用积分值
    private Integer getAmountMaxValuePoints(BigDecimal amount) {
        String integralScaleVal = configService.getConfigVal(SettingsEnum.INTEGRAL_PERCENT);
        BigDecimal integralScale = new BigDecimal(integralScaleVal);
        return integralScale.compareTo(BigDecimal.ZERO) > 0 ?
                amount.divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN).multiply(integralScale).intValue() : 0;
    }

    public OrderCheckVO.Total getTotalFee(OrderCheckParam param, CartListByStoreVO cartList, User user) {
        OrderCheckVO.Total total = new OrderCheckVO.Total();
        //初始化total
        // 订单总金额
        total.setTotalAmount(BigDecimal.valueOf(0));
        // 已支付金额
        total.setPaidAmount(BigDecimal.valueOf(0));
        // 未付款金额
        total.setUnpaidAmount(BigDecimal.valueOf(0));
//        total.setUnrefundAmount(0); // 未退款金额
        // 使用优惠券金额
        total.setCouponAmount(BigDecimal.valueOf(0));
        // 使用积分金额
        total.setPointsAmount(BigDecimal.valueOf(0));
        // 使用余额金额
        total.setBalance(BigDecimal.valueOf(0));
        // 服务费
        total.setServiceFee(BigDecimal.valueOf(0));
        // 配送费用
        total.setShippingFee(BigDecimal.valueOf(0));
//        total.setInvoiceFee(0); // 发票费用
        // 各种优惠活动金额
        total.setDiscountAmount(BigDecimal.valueOf(0));
        total.setOrderSendPoint(0);
        total.setExchangePoints(0);
        total.setStoreShippingFee(new HashMap<>());
        //如果cartList.getTotal()里有值的则覆盖到total中
        total.setDiscountCouponAmount(cartList.getTotal().getDiscountCouponAmount());
        total.setCouponAmount(cartList.getTotal().getDiscountCouponAmount());
        total.setDiscountAmount(cartList.getTotal().getDiscountDiscountAmount());
        total.setExchangePoints(0);
        total.setPointsAmount(getPointValueAmount(param.getUsePoint() != null ? BigDecimal.valueOf(param.getUsePoint()) : BigDecimal.valueOf(0)));
        BeanUtils.copyProperties(cartList.getTotal(), total);

        //计算各个店铺的优惠
//        Map<String, Map<Integer, BigDecimal>> storeDiscountMap = new HashMap<>();

//        for (CartByStoreVO cartListVO : cartList.getCartList()) {
//            for (PromotionVO promotion : cartListVO.getUsedPromotions()) {
//                if(promotion.getType() == 2){
//
//                }
//            }
//        }
        if (param.getFlowType() == 3) {
            //积分兑换功能
            Integer exchangeProductId = 0;
            Integer exchangeProductNumber = 0;
            Integer exchangeProductSkuId = 0;
            for (CartByStoreVO cartListVO : cartList.getCartList()) {
                for (CartVO cartVO : cartListVO.getCarts()) {
                    exchangeProductId = cartVO.getProductId();
                    exchangeProductSkuId = cartVO.getSkuId();
                    exchangeProductNumber += cartVO.getQuantity();
                }
            }
            if (exchangeProductId > 0) {
                PointsExchange pointsExchangeVO = pointsExchangeMapper.selectOne(new LambdaQueryWrapper<PointsExchange>().eq(PointsExchange::getProductId, exchangeProductId).eq(PointsExchange::getSkuId, exchangeProductSkuId));
                if (pointsExchangeVO != null) {
                    total.setExchangePoints(pointsExchangeVO.getExchangeIntegral() * exchangeProductNumber);
                    total.setPointsAmount(pointsExchangeVO.getPointsDeductedAmount().multiply(BigDecimal.valueOf(exchangeProductNumber)));
                    param.setUsePoint(total.getExchangePoints());
                }
            }
        }
        OrderCheckVO.ShippingFee shippingFee = getShippingFee(cartList, user, param);
        total.setShippingFee(shippingFee.getTotal());
        total.setStoreShippingFee(shippingFee.getStoreShippingFee());
        total.setTotalAmount(
                total.getProductAmount()
                        .add(total.getShippingFee())
                        .add(total.getServiceFee())
                        .subtract(total.getPointsAmount())
                        .subtract(total.getDiscountAmount())
                        .subtract(total.getCouponAmount()));
        if (param.getUseBalance() != null && param.getUseBalance().compareTo(BigDecimal.valueOf(0)) > 0) {
            BigDecimal userBalance = getUserBalance(user);
            if (total.getTotalAmount().compareTo(userBalance) < 0) {
                total.setBalance(total.getTotalAmount());
            } else {
                total.setBalance(userBalance);
            }
        }

        total.setBalance(total.getBalance().compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total.getBalance());
        total.setPaidAmount(BigDecimal.valueOf(0));
        total.setUnpaidAmount(total.getTotalAmount().subtract(total.getBalance()));
        total.setProductAmount(cartList.getTotal().getProductAmount());
        return total;
    }

    private OrderCheckVO.ShippingFee getShippingFee(CartListByStoreVO cartList, User user, OrderCheckParam param) {

        List<UserRank> ranksList = userRankService.list();
        int userRankId = user.getRankId();
        boolean freeShipping = false;

        for (UserRank value : ranksList) {
            if (value.getRankId() == userRankId && value.getFreeShipping() == 1) {
                freeShipping = true;
                break;
            }
        }

        Map<Integer, Integer> selectedTypeIds = new HashMap<>();
        if (CollUtil.isNotEmpty(param.getShippingType())) {

            for (Integer key : param.getShippingType().keySet()) {
                selectedTypeIds.put(param.getShippingType().get(key).getShopId(), param.getShippingType().get(key).getTypeId());
            }

        }

        OrderCheckVO.ShippingFee result = new OrderCheckVO.ShippingFee();
        result.setTotal(BigDecimal.ZERO);
        Map<Integer, BigDecimal> storeShippingFee = new HashMap<>();

        for (CartByStoreVO storeCart : cartList.getCartList()) {
            int shopId = storeCart.getShopId();
            int typeId = 0;
            if (selectedTypeIds.containsKey(shopId)) {
                typeId = selectedTypeIds.get(shopId);
            }
            Long defaultTplId = shippingTplService.getDefaultShippingTplId(shopId);
            BigDecimal thisStoreShippingFee = BigDecimal.ZERO;
            storeShippingFee.put(shopId, thisStoreShippingFee);
            if (freeShipping || storeCart.getNoShipping() == 1) {
                continue;
            }
            Map<Long, Map<String, BigDecimal>> storeShippingTplMap = new HashMap<>();
            //计算重量重新计算运费
            for (CartVO cartVO : storeCart.getCarts()) {
                if (cartVO.getFreeShipping() == 1) {
                    continue;
                }
                //获取运费模板类型
                // 如果为固定模板
                Product product = productService.getById(cartVO.getProductId());
                if (product.getFixedShippingType() == 1) {
                    storeShippingFee.put(shopId, storeShippingFee.get(shopId).add(product.getFixedShippingFee()));
                    result.setTotal(result.getTotal().add(product.getFixedShippingFee()));
                    continue;
                }
                // 如果为非固定模板
                Long shippingTplId = cartVO.getShippingTplId() == null || cartVO.getShippingTplId() == 0 ? defaultTplId : cartVO.getShippingTplId();
                //如果没有值则初始化一个map
                if (!storeShippingTplMap.containsKey(shippingTplId)) {
                    Map<String, BigDecimal> shippingTplMap = new HashMap<>();
                    shippingTplMap.put("count", BigDecimal.valueOf(0));
                    shippingTplMap.put("fee", BigDecimal.valueOf(0));
                    shippingTplMap.put("weight", BigDecimal.valueOf(0));
                    storeShippingTplMap.put(shippingTplId, shippingTplMap);
                }

                Map<String, BigDecimal> oldShippingTplMap = storeShippingTplMap.get(shippingTplId);
                BigDecimal productWeight = cartVO.getProductWeight() != null ? cartVO.getProductWeight() : BigDecimal.ONE;
                BigDecimal quantity = new BigDecimal(cartVO.getQuantity());
                BigDecimal weight = oldShippingTplMap.get("weight").add(productWeight.multiply(quantity));
                BigDecimal count = oldShippingTplMap.get("count").add(quantity);
                oldShippingTplMap.put("weight", weight);
                oldShippingTplMap.put("count", count);
                storeShippingTplMap.put(shippingTplId, oldShippingTplMap);

            }


            //for循环storeShippingTplMap
            for (Map.Entry<Long, Map<String, BigDecimal>> entry : storeShippingTplMap.entrySet()) {
                List<ShippingTplInfo> allTplInfo = shippingTplInfoService.list(new LambdaQueryWrapper<ShippingTplInfo>().eq(ShippingTplInfo::getShippingTplId, entry.getKey()).eq(ShippingTplInfo::getShippingTypeId, typeId));
                ShippingTplInfo tplInfo = null;
                for (ShippingTplInfo v : allTplInfo) {
                    if (v.getIsDefault() == 1) {
                        tplInfo = v;
                    } else {
                        UserAddress userAddress = userAddressService.getById(param.getAddressId());
                        if (userAddress == null) {
                            return result;
                        }
                        String regionIds = userAddress.getRegionIds();
                        List<Integer> objects = JSONUtil.parseArray(regionIds, new JSONConfig() {
                        }).toList(Integer.class);
                        if (fetchRegion(objects, v.getRegionData())) {
                            tplInfo = v;
                        }
                    }
                }
                Map<String, BigDecimal> oldShippingTplMap = entry.getValue();
                if (tplInfo != null) {
                    BigDecimal count = oldShippingTplMap.get("count");
                    BigDecimal weight = oldShippingTplMap.get("weight");
                    entry.getValue().put("fee", count.compareTo(BigDecimal.ZERO) > 0 ? tplInfo.getStartPrice() : BigDecimal.ZERO);

                    if (cartList.getTotal().getProductAmount().compareTo(tplInfo.getFreePrice()) >= 0 && tplInfo.getFreePrice().compareTo(BigDecimal.ZERO) > 0 || tplInfo.getIsFree() == 1) {
                        oldShippingTplMap.put("fee", BigDecimal.ZERO);
                    } else {
                        if (tplInfo.getPricingType() == 1) {
                            if (count.compareTo(tplInfo.getStartNumber()) >= 0) {
                                BigDecimal addCount = (count.subtract(tplInfo.getStartNumber())).divide(tplInfo.getAddNumber(), 0, RoundingMode.UP);
                                oldShippingTplMap.put("fee", oldShippingTplMap.get("fee").add(addCount.multiply(tplInfo.getAddPrice())));
                            }
                        } else if (tplInfo.getPricingType() == 2) {
                            if (weight.compareTo(tplInfo.getStartNumber()) >= 0) {
                                BigDecimal addWeight = (weight.subtract(tplInfo.getStartNumber())).divide(tplInfo.getAddNumber(), 0, RoundingMode.UP);
                                oldShippingTplMap.put("fee", oldShippingTplMap.get("fee").add(addWeight.multiply(tplInfo.getAddPrice())));
                            }
                        }
                    }
                    result.setTotal(result.getTotal().add(oldShippingTplMap.get("fee")));
                    storeShippingFee.put(shopId, storeShippingFee.get(shopId).add(oldShippingTplMap.get("fee")));
                }
            }
        }
        result.setStoreShippingFee(storeShippingFee);
        return result;
    }

    @Override
    public Map<Integer, List<OrderCheckVO.ShippingType>> getStoreShippingType(StoreShippingTypeQuery query) {
        CartListByStoreVO carts;
        if (query.getShopId() != null) {
            carts = cartService.getCartListByStore(true, query.getFlowType(), null, query.getShopId());
        } else {
            carts = cartService.getCartListByStore(true, query.getFlowType(), null, null);
        }

        UserAddressVO selectedAddress = userAddressService.getUserSelectedAddress();

        //region_ids json数组转成list
        List<Integer> regionIds = new ArrayList<>();
        if (selectedAddress != null && selectedAddress.getRegionIds() != null) {
            regionIds = selectedAddress.getRegionIds();
        }
        if (carts.getCartList() == null) {
            return null;
        }

        Map<Integer, List<OrderCheckVO.ShippingType>> availableShippingTypeList = new HashMap<>();
        for (CartByStoreVO storeCart : carts.getCartList()) {
            if (storeCart.getCarts() == null) {
                continue;
            }
            //取出cart里所有的商品id集合
            List<Integer> productIds = storeCart.getCarts().stream().map(CartVO::getProductId).toList();
            List<Long> shippingTplIds = getShippingTplIds(storeCart.getShopId(), productIds);
            List<OrderCheckVO.ShippingType> availableShippingType = getAvailableShippingType(storeCart.getShopId(), shippingTplIds, regionIds);
            //获取可用的配送方式并全部插入到availableShippingTypeList中
            availableShippingTypeList.put(storeCart.getShopId(), availableShippingType);
            CartListByStoreVO cartListByStore = cartService.getCartListByStore(true, query.getFlowType(), null, null);
            String childAreaNeedRegion = configService.getConfigByCode("childAreaNeedRegion").getBizVal();
            if ("1".equals(childAreaNeedRegion)) {
                for (CartByStoreVO cartByStoreVO : cartListByStore.getCartList()) {
                    for (CartVO cart : cartByStoreVO.getCarts()) {
                        if (cart.getFixedShippingType() == 2) {
                            if (!shippingTplInfoService.verifyUserAddress(Math.toIntExact(cart.getShippingTplId()), selectedAddress.getAddressId())) {
                                return Map.of();
                            }
                        }
                    }
                }
            }
        }
        return availableShippingTypeList;
    }

    @Override
    public List<OrderCheckVO.PaymentType> getAvailablePaymentType() {
        ConfigPO useOfflineConfig = configService.getConfigByCode(SettingsEnum.USE_OFFLINE.getBizCode());
        Boolean isOffline = "1".equals(useOfflineConfig.getBizVal());
        List<OrderCheckVO.PaymentType> result = new ArrayList<>();

        OrderCheckVO.PaymentType onlinePayment = new OrderCheckVO.PaymentType();
        onlinePayment.setTypeId(PaymentType.ONLINE.getCode());
        onlinePayment.setTypeName(translatePackage.translate("在线支付"));
        onlinePayment.setDisabled(false);
        onlinePayment.setDisabledDesc("");
        onlinePayment.setIsShow(true);
        result.add(onlinePayment);

        OrderCheckVO.PaymentType offlinePayment = new OrderCheckVO.PaymentType();
        offlinePayment.setTypeId(PaymentType.OFFLINE.getCode());
        offlinePayment.setTypeName(translatePackage.translate("线下支付"));
        offlinePayment.setDisabled(false);
        offlinePayment.setDisabledDesc("");
        offlinePayment.setIsShow(isOffline);
        result.add(offlinePayment);

        return result;
    }

    public List<Long> getShippingTplIds(Integer shopId, List<Integer> productIds) {
        // Long defaultId = shippingTplService.getDefaultShippingTplId(shopId);
        List<Product> productList = productService.list(new LambdaQueryWrapper<Product>().in(Product::getProductId, productIds).select(Product::getShippingTplId));
        List<Long> tplIds = productList.stream().map(Product::getShippingTplId).collect(Collectors.toList());
        if (tplIds.contains(0L)) {
//            tplIds.add(defaultId);
            tplIds = tplIds.stream().filter(item -> item != 0).toList();
        }
        return new ArrayList<>(new HashSet<>(tplIds));
    }

//    public int getAmountValuePoints(double amount) {
//        JSONObject shoppingSetting = configService.getConfigByCode("base_shopping").getData();
//        Integer scale = shoppingSetting.get("points_setting").get("integral_scale");
//        return scale > 0 ? (int) (amount / scale * 100) : 0;
//    }

    public List<OrderCheckVO.ShippingType> getAvailableShippingType(Integer shopId, List<Long> shippingTplIds, List<Integer> regionIds) {
//
//        ConfigPO childAreaNeedRegionConfig = configService.getConfigByCode(SettingsEnum.CHILD_AREA_NEED_REGION.getBizCode());
//        String needRegion = StrUtil.isNotBlank(childAreaNeedRegionConfig.getBizVal()) ? childAreaNeedRegionConfig.getBizVal() : "0";
//        List<Long> availableTplTypeIds = new ArrayList<>();
//        List<Long> allTplTypeIds = new ArrayList<>();
//        List<Long> resultTplIds = new ArrayList<>();
//        int i = 0;
//        for (Long tplId : shippingTplIds) {
//            if ("1".equals(needRegion)) {
//                List<ShippingTplInfo> shippingTplInfos = shippingTplInfoService.list(new LambdaUpdateWrapper<ShippingTplInfo>().eq(ShippingTplInfo::getShippingTypeId, tplId).eq(ShippingTplInfo::getIsDefault, 1));
//                if (shippingTplInfos != null) {
//                    for (ShippingTplInfo shippingTplInfo : shippingTplInfos) {
//                        if (fetchRegion(regionIds, shippingTplInfo.getRegionData())) {
//                            availableTplTypeIds.add(shippingTplInfo.getShippingTypeId());
//                        }
//                    }
//                }
//            }
//            List<ShippingTplInfo> list = shippingTplInfoService.list(new LambdaQueryWrapper<ShippingTplInfo>().eq(ShippingTplInfo::getShippingTplId, tplId));
//            if (list != null) {
//                for (ShippingTplInfo shippingTplInfo : list) {
//                    allTplTypeIds.add(shippingTplInfo.getShippingTypeId());
//                }
//            }
//            allTplTypeIds = allTplTypeIds.stream().distinct().collect(Collectors.toList());
//            resultTplIds = i != 0 ? resultTplIds.stream().filter(availableTplTypeIds::contains).toList() : allTplTypeIds;
//            i++;
//        }
//        if ("1".equals(needRegion)) {
//            resultTplIds = resultTplIds.stream().filter(availableTplTypeIds::contains).toList();
//        }
//        List<OrderCheckVO.ShippingType> shippingTypeList = new ArrayList<>();
//
//        OrderCheckVO.ShippingType shippingType = new OrderCheckVO.ShippingType();
//        shippingType.setShippingTypeId(1);
//        shippingType.setShopId(shopId);
//        shippingType.setShippingTypeName(configService.getConfigVal(SettingsEnum.DEFAULT_LOGISTICS_NAME));
//        shippingTypeList.add(shippingType);
//        if (resultTplIds.isEmpty()) {
//            return shippingTypeList;
//        }
//        List<ShippingType> shippingTypes = shippingTypeService.list(new LambdaQueryWrapper<ShippingType>().in(ShippingType::getShippingTypeId, resultTplIds));
//        for (ShippingType shippingType : shippingTypes) {
//            OrderCheckVO.ShippingType shippingType1 = new OrderCheckVO.ShippingType();
//            BeanUtil.copyProperties(shippingType, shippingType1);
//            shippingTypeList.add(shippingType1);
//        }

        List<OrderCheckVO.ShippingType> shippingTypeList = new ArrayList<>();

        OrderCheckVO.ShippingType shippingType = new OrderCheckVO.ShippingType();
        shippingType.setShippingTypeId(1);
        shippingType.setShopId(shopId);
        shippingType.setShippingTypeName(configService.getConfigVal(SettingsEnum.DEFAULT_LOGISTICS_NAME));
        shippingTypeList.add(shippingType);
        return shippingTypeList;
    }

    boolean fetchRegion(List<Integer> addressRegions, String regionData) {
        if (addressRegions == null || addressRegions.isEmpty()) {
            return false;
        }
        List<Long> regionIds = new ArrayList<>();
        RegionDataParam regionDataBean = JSONUtil.toBean(regionData, RegionDataParam.class);
        if (regionDataBean != null) {
            for (List<Long> region : regionDataBean.getAreaRegions()) {
                regionIds.addAll(region);
            }
        } else {
            return false;
        }

        int size = regionIds.size();
        if (size > 0) {
            Long lastRegionId = regionIds.getLast();
            return lastRegionId != null && addressRegions.contains(lastRegionId.intValue());
        }
        return false;
    }

}