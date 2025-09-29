package com.tigshop.service.cart.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tigshop.bean.dto.cart.*;
import com.tigshop.bean.dto.product.ProductDetailDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.enums.cart.CartTypeEnum;
import com.tigshop.bean.enums.order.AftersalesStatusEnum;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.order.PaymentStatus;
import com.tigshop.bean.enums.product.ProductStatus;
import com.tigshop.bean.enums.promotion.SendRangeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.cart.Cart;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.product.ECard;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductAttribute;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserCoupon;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.param.product.ProductSaveParam;
import com.tigshop.bean.vo.cart.CartByStoreVO;
import com.tigshop.bean.vo.cart.CartListByStoreVO;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.content.CartCouponDiscountVO;
import com.tigshop.bean.vo.product.ProductAvailabilityVO;
import com.tigshop.bean.vo.promotion.CouponVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.cart.CartMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.product.ECardMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.cart.CartService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.AftersalesItemService;
import com.tigshop.service.order.AftersalesService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.product.ECardService;
import com.tigshop.service.product.ProductAttributesService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.promotion.CouponService;
import com.tigshop.service.promotion.ProductPromotionService;
import com.tigshop.service.promotion.SeckillService;
import com.tigshop.service.promotion.TimeDiscountService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.shop.ShopService;
import com.tigshop.service.user.UserCouponService;
import com.tigshop.service.user.UserRankService;
import com.tigshop.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.product.ProductType.PRODUCT_TYPE_CARD;
import static com.tigshop.bean.enums.product.ProductType.PRODUCT_TYPE_PAID;
import static com.tigshop.bean.enums.setting.SettingsEnum.USE_COUPON;
import static com.tigshop.common.constant.order.OrderConstants.ORDER_NOT_EXIST;

/**
 * 购物车
 *
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
@RequiredArgsConstructor
@Service
public class CartServiceImpl extends BaseServiceImpl<CartMapper, Cart> implements CartService {

    private final ProductService productService;

    private final CouponService couponService;

    private final ProductSkuService productSkuService;

    private final ShopService shopService;

    private final UserRankService userRankService;

    private final UserMapper userMapper;

    private final OrderItemMapper orderItemMapper;

    private final ECardMapper eCardMapper;

    private final ProductAttributesService productAttributesService;

    private final UserCouponService userCouponService;

    private final ProductPromotionService productPromotionService;

    private final TimeDiscountService timeDiscountService;

    private final SeckillService seckillService;

    private final TranslatePackageImpl translatePackage;

    private final OrderService orderService;

    private final ConfigService configService;

    private final UserService userService;

    private final ECardService eCardService;

    private final AftersalesService aftersalesService;

    private final AftersalesItemService aftersalesItemService;

    private final TigshopProperties tigshopProperties;

    @Override
    public CartListByStoreVO getCartListByStore(boolean isChecked, Integer type, Map<String, Object> filter, Integer storeShopId) {
        List<CartVO> cartVOList = getCartList(isChecked, type, filter, storeShopId);
        // 按shop_id来分组
        Map<Integer, List<CartVO>> cartMap = cartVOList.stream().collect(java.util.stream.Collectors.groupingBy(CartVO::getShopId));

        CartListByStoreVO cartListByStoreVO = new CartListByStoreVO();
        // 循环处理每个shop的数据
        CartListByStoreVO.CartTotal total = cartListByStoreVO.getTotal();

        cartMap.forEach((shopId, carts) -> {
            CartByStoreVO cartByStoreVO = new CartByStoreVO();
            cartByStoreVO.setShopId(shopId);
            if (carts.getFirst().getShop() != null) {
                cartByStoreVO.setShopTitle(carts.getFirst().getShop().getShopTitle());
            }
            // 循环carts处理购物车数据
            for (CartVO cart : carts) {
                if (cart.getProductType() == PRODUCT_TYPE_CARD.getCode()) {
                    long count = eCardService.count(new LambdaQueryWrapper<ECard>()
                            .eq(ECard::getGroupId, cart.getCardGroupId()));
                    cart.setProductStock((int) count);
                    cart.setStock((int) count);
                }

                cart.setIsDisabled(false);
                if (cart.getStock() == 0 || cart.getProductStatus() == 0) {
                    cart.setIsDisabled(true);
                    if (cart.getIsChecked()) {
                        this.update(new LambdaUpdateWrapper<Cart>().eq(Cart::getCartId, cart.getCartId()).set(Cart::getIsChecked, 0));
                        cart.setIsChecked(false);
                    }
                }
                total.setTotalCount(cartListByStoreVO.getTotal().getTotalCount() + cart.getQuantity());
                if (cart.getIsChecked()) {
                    total.setCheckedCount(cartListByStoreVO.getTotal().getCheckedCount() + cart.getQuantity());
                    total.setProductAmount(cartListByStoreVO.getTotal().getProductAmount().add(cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()))));
                }
            }

            boolean noShippingFlag = carts.stream().anyMatch(cart -> Constants.YES.equals(cart.getNoShipping()));
            cartByStoreVO.setNoShipping(noShippingFlag ? Constants.YES : Constants.NO);

            total.setDiscountAfter(total.getProductAmount());
            total.setDiscountSeckillAmount(BigDecimal.ZERO);
            total.setDiscountProductPromotionAmount(BigDecimal.ZERO);
            total.setDiscountCouponAmount(BigDecimal.ZERO);
            total.setDiscountTimeDiscountAmount(BigDecimal.ZERO);
            total.setDiscountDiscountAmount(BigDecimal.ZERO);
            cartListByStoreVO.setTotal(total);
            cartByStoreVO.setCarts(carts);
            List<CartByStoreVO> cardList = cartListByStoreVO.getCartList();
            cardList.add(cartByStoreVO);
            cartListByStoreVO.setCartList(cardList);
        });
        return cartListByStoreVO;
    }

    @Override
    public List<CartVO> getCartList(boolean isChecked, Integer type, Map<String, Object> filter, Integer storeShopId) {

        Object productId = filter != null ? filter.get("productId") : null;
        List<Cart> cartList = this.lambdaQuery()
                .eq(storeShopId != null, Cart::getShopId, storeShopId)
                .eq(isChecked, Cart::getIsChecked, 1)
                .eq(productId != null, Cart::getProductId, productId)
                .eq(Cart::getType, type)
                .eq(Cart::getUserId, SecurityUtils.getCurrentUserId()).list();

        // 取出ids对应的product
        Map<Integer, Product> productMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(cartList)) {
            List<Integer> productIds = cartList.stream().map(Cart::getProductId).toList();
            List<Product> products = productService.listByIds(productIds);
            productMap = products.stream().collect(Collectors.toMap(Product::getProductId, Function.identity()));
        }

        // 取出所有 sku_id 不为 0 的 sku_id 数据
        Map<Integer, ProductSku> skuMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(cartList)) {
            List<Integer> skuIds = cartList.stream().map(Cart::getSkuId).filter(skuId -> skuId != 0).toList();
            if (CollUtil.isNotEmpty(skuIds)) {
                List<ProductSku> skuList = productSkuService.listByIds(skuIds);
                skuMap = skuList.stream().collect(Collectors.toMap(ProductSku::getSkuId, Function.identity()));
            }
        }

        // 取出所有shop_id不为0的shop_id数据
        Map<Integer, Shop> shopMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(cartList)) {
            List<Integer> shopIds = cartList.stream().map(Cart::getShopId).filter(shopId -> shopId != 0).toList();
            if (CollUtil.isNotEmpty(shopIds)) {
                List<Shop> shopList = shopService.listByIds(shopIds);
                shopMap = shopList.stream().collect(Collectors.toMap(Shop::getShopId, Function.identity()));
            }
        }

        List<CartVO> cartVOList = new ArrayList<>();
        for (Cart cart : cartList) {
            Product product = productMap.get(cart.getProductId());
            if (product == null) {
                continue;
            }
            ProductSku productSku = skuMap.get(cart.getSkuId());
            BigDecimal memberPrice;
            if (productSku != null) {
                memberPrice = productSku.getSkuPrice();
            } else {
                memberPrice = product.getProductPrice();
            }
            if (SecurityUtils.getCurrentUserId() > 0) {
                List<UserRank> userRankList = userRankService.getUserRank();
                Integer rankId = userMapper.selectById(SecurityUtils.getCurrentUserId()).getRankId();
                for (UserRank userRank : userRankList) {
                    if (userRank.getRankId().equals(rankId) && userRank.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                        memberPrice = memberPrice.multiply(userRank.getDiscount()).divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
                    }
                }
            }

            int skuId = cart.getSkuId() == null ? 0 : cart.getSkuId();
            // 防止商品不存在造成的最终价计算为null
            long count = productService.count(Wrappers.lambdaQuery(Product.class).eq(Product::getProductId, cart.getProductId()));
            if (count < 1) {
                continue;
            }
            ProductAvailabilityVO productSkuDetail = productService.getProductFinalPrice(cart.getProductId(), skuId, 0, null, cart.getShopId());

            // BigDecimal price = cart.getSkuId() != null && cart.getSkuId() != 0 ? productSku.getSkuPrice() : product.getProductPrice();
            BigDecimal price = productSkuDetail.getPrice();
            // BigDecimal productPrice = productService.getProductFinalPrice(cart.getProductId(), price, cart.getSkuId());
            Integer stock = productService.getProductStock(cart.getProductId(), cart.getSkuId(), false);

            Shop shop = shopMap.get(cart.getShopId());

            CartVO cartVO = new CartVO(cart, product, productSku, shop, price, memberPrice, stock);
            cartVOList.add(cartVO);
        }

        return cartVOList;
    }

    @Override
    public Integer getCartCount(Integer shopId) {
        List<Cart> carts = this.lambdaQuery().eq(shopId != null, Cart::getShopId, shopId).eq(Cart::getUserId, SecurityUtils.getCurrentUserId()).list();
        return carts.stream().map(Cart::getQuantity).reduce(0, Integer::sum);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCheck(List<Map<String, Object>> list) {
        //解析list
        List<Cart> carts = new ArrayList<>();
        for (Map<String, Object> map : list) {
            Cart cart = new Cart();
            cart.setCartId((Integer) map.get("cartId"));
            cart.setIsChecked((Integer) map.get("isChecked"));
            carts.add(cart);
        }
        this.updateBatchById(carts);
    }

    @Override
    public void updateCartItem(UpdateItemDTO updateItem) {
        Assert.notNull(updateItem.getData().getQuantity(), "请选择购买数量");

        Cart cart = this.getById(updateItem.getCartId());
        Assert.notNull(cart, () -> new GlobalException(translatePackage.translate("购物车不存在此商品")));

        Integer stock = productService.getProductStock(cart.getProductId(), cart.getSkuId(), false);
        Assert.isFalse(stock < updateItem.getData().getQuantity(), () -> new GlobalException(translatePackage.translate("商品库存不足")));

        int changeQuantity = updateItem.getData().getQuantity() - cart.getQuantity();
        checkProductLimitNumber(cart.getProductId(), cart.getUserId(), changeQuantity, cart.getCartId());

        Cart newCart = new Cart();
        newCart.setCartId(updateItem.getCartId());
        newCart.setQuantity(updateItem.getData().getQuantity());
        this.updateById(newCart);
    }

    @Override
    public CartCouponDiscountVO getCouponDiscount(Integer couponId) {

        CouponVO coupon = couponService.detail(couponId);
        List<CartVO> cartList = getCartList(true, CartTypeEnum.TYPE_NORMAL.getCode(), null, null);
        Integer quantityCount = 0;
        BigDecimal checkedProductPriceSum = BigDecimal.ZERO;
        for (CartVO cartVO : cartList) {
            if (coupon.getSendRange() == SendRangeEnum.CATEGORY.getValue()) {
                if (!coupon.getSendRangeData().contains(cartVO.getCategoryId())) {
                    continue;
                }
            } else if (coupon.getSendRange() == SendRangeEnum.BRAND.getValue()) {
                if (!coupon.getSendRangeData().contains(cartVO.getBrandId())) {
                    continue;
                }
            } else if (coupon.getSendRange() == SendRangeEnum.PRODUCT.getValue()) {
                if (!coupon.getSendRangeData().contains(cartVO.getProductId())) {
                    continue;
                }
            } else if (coupon.getSendRange() == SendRangeEnum.NOT_PRODUCT.getValue()) {
                if (coupon.getSendRangeData().contains(cartVO.getProductId())) {
                    continue;
                }
            }

            quantityCount += cartVO.getQuantity();
            BigDecimal price = cartVO.getSku() != null && cartVO.getSku().getSkuPrice() != null
                    ? new BigDecimal(cartVO.getSku().getSkuPrice())
                    : cartVO.getProductPrice();
            BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(cartVO.getQuantity()));
            checkedProductPriceSum = checkedProductPriceSum.add(totalPrice);
        }

        BigDecimal discountMoney;

        if (coupon.getCouponType() == 1) {
            discountMoney = coupon.getCouponMoney();
        } else {
            BigDecimal discount = coupon.getCouponDiscount().divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP);
            discount = BigDecimal.valueOf(1).subtract(discount);
            BigDecimal discountRate = checkedProductPriceSum.multiply(discount);
            discountMoney = checkedProductPriceSum.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal finalCheckedProductPriceSum = checkedProductPriceSum;
        Integer finalQuantityCount = quantityCount;
        return CartCouponDiscountVO.builder()
                .minOrderAmount(coupon.getReduceType() == 2 ? BigDecimal.ZERO : coupon.getMinOrderAmount())
                .couponMoney(coupon.getCouponMoney())
                .couponUnit(coupon.getCouponUnit())
                .productPrice(finalCheckedProductPriceSum)
                .quantityCount(finalQuantityCount)
                .discountMoney(discountMoney)
                .build();
    }

    @Override
    public CartListByStoreVO getCartList(ListDTO listDTO) {
        CartListByStoreVO getCartListByStoreVO = getCartListByStore(listDTO.getIsChecked() != null ? listDTO.getIsChecked() : false, CartTypeEnum.TYPE_NORMAL.getCode(), null, listDTO.getShopId());
        return buildCartPromotion(getCartListByStoreVO, 1, 1, null);
    }

    @Override
    public CartListByStoreVO buildCartPromotion(CartListByStoreVO cartListByStore, Integer flowType, Integer useDefaultCoupon, List<Integer> useCouponIds) {
        if (flowType != 1) {
            return cartListByStore;
        }

        CartListByStoreVO.CartTotal cartTotal = cartListByStore.getTotal();
        cartTotal.setDiscountSeckillAmount(BigDecimal.ZERO);
        cartTotal.setDiscountProductPromotionAmount(BigDecimal.ZERO);
        cartTotal.setDiscountCouponAmount(BigDecimal.ZERO);
        cartTotal.setDiscountTimeDiscountAmount(BigDecimal.ZERO);
        cartTotal.setDiscountDiscountAmount(BigDecimal.ZERO);

        List<CartByStoreVO> cartListByStoreNew = new ArrayList<>();
        for (CartByStoreVO cartByStore : cartListByStore.getCartList()) {
            if (cartByStore.getCarts() == null) {
                continue;
            }

            List<ProductSaveParam> products = cartByStore.getCarts().stream()
                    .filter(CartVO::getIsChecked)
                    .map(cart -> {
                        ProductSaveParam product = new ProductSaveParam();
                        product.setProductId(cart.getProductId());
                        product.setSkuId(cart.getSkuId());
                        product.setCartId(cart.getCartId());
                        product.setIsChecked(cart.getIsChecked());
                        return product;
                    }).toList();

            // 获取购物车每个活动对应的购物车商品
            Map<Integer, List<CartVO>> promotionIdToCarts = new HashMap<>();
            // 获取购物车每个活动对应的活动信息
            Map<Integer, PromotionVO> promotionIdToPromotion = new HashMap<>();
            // 获取购物车商品对应的活动信息
            List<CartVO> carts1 = cartByStore.getCarts();

            Map<Integer, ProductSaveParam> promotions = productService.getProductsPromotion(products, cartByStore.getShopId(), "cart");

            for (Integer cartId : promotions.keySet()) {
                if (promotions.get(cartId).getActivityInfo() == null || promotions.get(cartId).getActivityInfo().isEmpty()) {
                    continue;
                }
                //循环每个活动
                for (PromotionVO promotionVO : promotions.get(cartId).getActivityInfo()) {
                    promotionIdToPromotion.put(promotionVO.getPromotionId(), promotionVO);
                    promotionIdToCarts.put(promotionVO.getPromotionId(), promotionIdToCarts.getOrDefault(promotionVO.getPromotionId(), new ArrayList<>()));
                    CartVO thiscartVO = carts1.stream().filter(cart -> cart.getCartId().equals(cartId)).findFirst().get();
                    CartVO newcartVO = BeanUtil.copyProperties(thiscartVO, CartVO.class);
                    promotionIdToCarts.get(promotionVO.getPromotionId()).add(newcartVO);
                }
            }

            //如果使用推荐默认优惠券，先计算出最大的优惠券
            Map<Integer, PromotionVO> maxCouponPromotion = new HashMap<>();
            for (Integer promotionId : promotionIdToCarts.keySet()) {
                if (promotionIdToPromotion.get(promotionId).getType() != 2) {
                    continue;
                }
                UserCoupon userCouponByCouponId = userCouponService.getUserCouponByCouponId(SecurityUtils.getCurrentUserId(), promotionIdToPromotion.get(promotionId).getRelationId());
                if (userCouponByCouponId == null) {
                    // 未领取
                    promotionIdToPromotion.get(promotionId).setIsReceive(0);
                } else {
                    // 已领取
                    promotionIdToPromotion.get(promotionId).setIsReceive(1);
                    if (useDefaultCoupon != null && useDefaultCoupon == 1) {
                        CartPromotionParsePriceDTO cartPromotionParsePriceDTO = couponService.parsePrice(promotionIdToCarts.get(promotionId), promotionIdToPromotion.get(promotionId));
                        if (cartPromotionParsePriceDTO != null) {
                            if (maxCouponPromotion.get(0) == null || maxCouponPromotion.get(0).getAmount().compareTo(cartPromotionParsePriceDTO.getPromotion().getAmount()) < 0) {
                                maxCouponPromotion.put(0, cartPromotionParsePriceDTO.getPromotion());
                            }
                        }
                    }
                }

            }

            if (maxCouponPromotion.get(0) != null) {
                if (useCouponIds == null) {
                    useCouponIds = new ArrayList<>();
                }
                useCouponIds.add(maxCouponPromotion.get(0).getRelationId());
            }

            List<CartVO> carts = cartByStore.getCarts();

            // 可使用的营销
            List<PromotionVO> enableUsePromotion = new ArrayList<>();
            // 已使用的营销
            List<PromotionVO> usedPromotion = new ArrayList<>();

            //处理所有购物车列表选择的商品优惠计算
            for (Integer cartId : promotions.keySet()) {
                if (!promotions.get(cartId).getIsChecked()) {
                    promotions.get(cartId).setActivityInfo(List.of());
                    continue;
                }
                //循环每个活动
                if (promotions.get(cartId).getActivityInfo() == null || promotions.get(cartId).getActivityInfo().isEmpty()) {
                    continue;
                }
                //删除这个promotionVO
                promotions.get(cartId).getActivityInfo().removeIf(promotionVO -> promotionVO.getType() == 2 && promotionVO.getIsReceive() == 0 && promotionVO.getIsDelete() == 1);
                List<PromotionVO> activityInfo = promotions.get(cartId).getActivityInfo();
                for (PromotionVO promotionVO : activityInfo) {
                    if (promotionVO == null) {
                        continue;
                    }
                    // php 版本的未循环 usedPromotion 然后 parsePrice 这里处理逻辑和 php 不一样，所以在这里添加重复过滤
                    if (usedPromotion.contains(promotionVO)) {
                        continue;
                    }
                    CartPromotionParsePriceDTO cartPromotionParsePriceDTO = switch (promotionVO.getType()) {
                        case 1 ->
                                seckillService.parsePrice(promotionIdToCarts.get(promotionVO.getPromotionId()), promotionVO);
                        case 2 -> {
                            ConfigPO config = configService.getConfigByCode(USE_COUPON.getBizCode());
                            if (config.getBizVal() != null && "1".equals(config.getBizVal())) {
                                if (tigshopProperties.getIsOverseas() == 1) {
                                    String promotionDesc = translatePackage.translate("满")+"%s"+translatePackage.translate("减")+"%s";
                                    Map<String, Object> data = promotionVO.getData();
                                    BigDecimal minOrderAmount = new BigDecimal(data.get("minOrderAmount").toString());
                                    BigDecimal couponMoney = new BigDecimal(data.get("couponMoney").toString());
                                    // 使用String.format()填充占位符，保留两位小数
                                    String result = String.format(promotionDesc,
                                            minOrderAmount.setScale(2),
                                            couponMoney.setScale(2));
                                    data.put("promotionDesc", result);
                                }
                                yield couponService.parsePrice(promotionIdToCarts.get(promotionVO.getPromotionId()), promotionVO);
                            }
                            yield null;
                        }
                        case 3, 4, 5 ->
                                productPromotionService.parsePrice(promotionIdToCarts.get(promotionVO.getPromotionId()), promotionVO);
                        case 6 ->
                                timeDiscountService.parsePrice(promotionIdToCarts.get(promotionVO.getPromotionId()), promotionVO);
                        default -> null;
                    };


                    if (promotionVO.getType() == 1) {
                        //循环cartByStore.getCarts() 里的cart找到对应cartId 并赋值isSeckill
                        carts.forEach(cart -> {
                            if (cart.getCartId().equals(cartId)) {
                                cart.setIsSeckill(1);
                            }
                        });
                    } else if (promotionVO.getType() == 2 && cartPromotionParsePriceDTO != null) {
                        enableUsePromotion.add(cartPromotionParsePriceDTO.getPromotion());
                        if (useCouponIds == null || useCouponIds.isEmpty() || !useCouponIds.contains(promotionVO.getRelationId())) {
                            continue;
                        }
                    }

                    if (cartPromotionParsePriceDTO == null) {
                        continue;
                    }

                    usedPromotion.add(cartPromotionParsePriceDTO.getPromotion());
                    if (cartPromotionParsePriceDTO.getGift() != null) {
                        //赋值gift
                        if (cartByStore.getGift().isEmpty()) {
                            cartByStore.setGift(new ArrayList<>());
                        }
                        List<CartPromotionParsePriceDTO.Gift> gift = cartByStore.getGift();
                        gift.add(cartPromotionParsePriceDTO.getGift());
                        cartByStore.setGift(gift);
                    }

                    Map<String, Object> hasDiscountCartId = new HashMap<>();
                    for (CartVO cartVO : cartPromotionParsePriceDTO.getCartList()) {
                        //一种优惠只使用一次
                        if (hasDiscountCartId.containsKey(cartVO.getCartId() + "_" + promotionVO.getPromotionId())) {
                            continue;
                        }
                        hasDiscountCartId.put(cartVO.getCartId() + "_" + promotionVO.getPromotionId(), true);
                        //通过cartId赋值并修改购物车的price数据，同时出现计算购物车的subtotal
                        carts.forEach(cart -> {
                            if (cart.getCartId().equals(cartVO.getCartId())) {
                                cart.setPrice(cartVO.getPrice());
                                cart.setSubtotal(cart.getPrice().multiply(new BigDecimal(cartVO.getQuantity())));
                                cart.setActivityInfo(promotions.get(cartId).getActivityInfo());
                            }
                        });
                    }

                }

            }
            cartByStore.setEnableUsePromotion(enableUsePromotion);
            cartByStore.setUsedPromotions(usedPromotion);
            cartByStore.setCarts(carts);
            CartByStoreVO.CartStoreTotal cartByStoreTotal = cartByStore.getTotal();

            cartByStoreTotal.setDiscountCouponAmount(BigDecimal.ZERO);
            cartByStoreTotal.setDiscountSeckillAmount(BigDecimal.ZERO);
            cartByStoreTotal.setDiscountTimeDiscountAmount(BigDecimal.ZERO);
            cartByStoreTotal.setDiscountProductPromotionAmount(BigDecimal.ZERO);
            cartByStoreTotal.setDiscountDiscountAmount(BigDecimal.ZERO);
            cartByStoreTotal.setDiscounts(BigDecimal.ZERO);

            for (PromotionVO promotion : usedPromotion) {
                if (promotion.getAmount() == null) {
                    promotion.setAmount(BigDecimal.ZERO);
                }

                // 计算活动优惠
                if (promotion.getType() == 2) {
                    // 优惠券
                    if (CollUtil.isNotEmpty(useCouponIds) && useCouponIds.contains(promotion.getRelationId())) {
                        cartByStoreTotal.setDiscountCouponAmount(cartByStoreTotal.getDiscountCouponAmount().add(promotion.getAmount()));
                        List<Integer> couponIds = cartByStoreTotal.getCouponIds();
                        couponIds.add(promotion.getRelationId());
                        cartByStoreTotal.setCouponIds(couponIds);
                        cartTotal.setDiscountCouponAmount(cartTotal.getDiscountCouponAmount().add(promotion.getAmount()));
                    } else {
                        promotion.setAmount(BigDecimal.ZERO);
                    }

                } else if (promotion.getType() == 1) {
                    // 秒杀
                    cartByStoreTotal.setDiscountSeckillAmount(cartByStoreTotal.getDiscountSeckillAmount().add(promotion.getAmount()));
                    cartTotal.setDiscountSeckillAmount(cartTotal.getDiscountSeckillAmount().add(promotion.getAmount()));
                } else if (promotion.getType() == 6) {
                    // 限时折扣
                    cartByStoreTotal.setDiscountTimeDiscountAmount(cartByStoreTotal.getDiscountTimeDiscountAmount().add(promotion.getAmount()));
                    cartTotal.setDiscountTimeDiscountAmount(cartTotal.getDiscountTimeDiscountAmount().add(promotion.getAmount()));
                } else {
                    cartByStoreTotal.setDiscountProductPromotionAmount(cartByStoreTotal.getDiscountProductPromotionAmount().add(promotion.getAmount()));
                    cartTotal.setDiscountProductPromotionAmount(cartTotal.getDiscountProductPromotionAmount().add(promotion.getAmount()));
                }

                // 优惠券独立计算
                if (promotion.getType() != 2) {
                    cartByStoreTotal.setDiscountProductPromotionAmount(cartByStoreTotal.getDiscountProductPromotionAmount().add(promotion.getAmount()));
                    cartByStoreTotal.setDiscountDiscountAmount(cartByStoreTotal.getDiscountDiscountAmount().add(promotion.getAmount()));
                    cartTotal.setDiscountDiscountAmount(cartTotal.getDiscountDiscountAmount().add(promotion.getAmount()));
                }
                cartTotal.setDiscounts(cartTotal.getDiscounts().add(promotion.getAmount()));
            }

            cartByStore.setTotal(cartByStoreTotal);
            cartListByStoreNew.add(cartByStore);
        }

        cartTotal.setProductAmount(cartTotal.getProductAmount());
        cartTotal.setDiscountAfter(cartTotal.getProductAmount().subtract(cartTotal.getDiscounts()));
        cartTotal.setDiscountCouponAmount(cartTotal.getDiscountCouponAmount().compareTo(cartTotal.getProductAmount()) > 0 ? cartTotal.getProductAmount() : cartTotal.getDiscountCouponAmount());
        cartTotal.setDiscounts(cartTotal.getDiscounts().compareTo(cartTotal.getProductAmount()) > 0 ? cartTotal.getProductAmount() : cartTotal.getDiscounts());
        // 保留小数点后两位，向下取整
        cartTotal.twoDecimalPlaces();
        cartListByStore.setCartList(cartListByStoreNew);
        cartListByStore.setTotal(cartTotal);

        return getCartPriceByAttrPrice(cartListByStore);
    }

    /**
     * 处理购物车内商品附加属性价格
     *
     * @param cart 购物车数据
     * @return 处理后的购物车数据
     */
    public CartListByStoreVO getCartPriceByAttrPrice(CartListByStoreVO cart) {
        // 设置服务费为0
        cart.getTotal().setServiceFee(BigDecimal.ZERO);

        // 遍历购物车每个商店
        for (CartByStoreVO storeCart : cart.getCartList()) {
            if (storeCart.getCarts() == null || storeCart.getCarts().isEmpty()) {
                continue;
            }

            // 遍历每个商品
            for (int i = 0; i < storeCart.getCarts().size(); i++) {
                CartVO item = storeCart.getCarts().get(i);

                BigDecimal totalAttrPrice = BigDecimal.ZERO;
                BigDecimal serviceFee = BigDecimal.ZERO;

                // 获取附加属性数据
                List<CarExtraSkuDataDTO> extraSkuData = item.getExtraSkuData();
                if (extraSkuData != null && !extraSkuData.isEmpty()) {
                    for (CarExtraSkuDataDTO attrItem : extraSkuData) {
                        BigDecimal attrPrice = attrItem.getAttrPrice();
                        BigDecimal quantity = new BigDecimal(item.getQuantity());
                        BigDecimal totalPrice = attrPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
                        attrItem.setTotalAttrPrice(totalPrice);
                    }

                    // 获取附加属性价格
                    for (CarExtraSkuDataDTO attrItem : extraSkuData) {
                        BigDecimal attrPrice = attrItem.getAttrPrice();
                        totalAttrPrice = totalAttrPrice.add(attrPrice).setScale(2, RoundingMode.HALF_UP);
                    }

                    // 乘以商品数量
                    totalAttrPrice = totalAttrPrice.multiply(new BigDecimal(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);

                    // 如果商品被选中，则更新总价和服务费
                    if (item.getIsChecked()) {
                        cart.getTotal().setDiscountAfter(cart.getTotal().getDiscountAfter().add(totalAttrPrice).setScale(2, RoundingMode.HALF_UP));
                        cart.getTotal().setServiceFee(cart.getTotal().getServiceFee().add(totalAttrPrice).setScale(2, RoundingMode.HALF_UP));
                    }

                    storeCart.getCarts().get(i).setServiceFee(serviceFee.add(totalAttrPrice).setScale(2, RoundingMode.HALF_UP).toString());
                }

                // 获取所有附加属性
                List<ProductAttribute> extraSkuAllData = productAttributesService.list(new LambdaQueryWrapper<ProductAttribute>()
                        .eq(ProductAttribute::getProductId, item.getProductId())
                        .eq(ProductAttribute::getAttrType, 2)
                );
                List<CarExtraSkuDataDTO> extraSkuAllDataList = CollUtil.isNotEmpty(extraSkuAllData) ? JSONUtil.toList(JSONUtil.toJsonStr(extraSkuAllData), CarExtraSkuDataDTO.class) : null;
                storeCart.getCarts().get(i).setExtraSkuAllData(extraSkuAllDataList);
            }
        }
        return cart;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean buyAgain(Integer id, Integer userId) {
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getOrderId, id);
        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);
        if (orderItems.isEmpty()) {
            throw new GlobalException(ORDER_NOT_EXIST);
        }
        //将购物车中所有状态改为不选中的
        LambdaQueryWrapper<Cart> cartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        cartLambdaQueryWrapper.eq(Cart::getUserId, userId);
        // 构建更新条件
        Cart updateCart = new Cart();
        updateCart.setIsChecked(0);
        this.update(updateCart, cartLambdaQueryWrapper);
        //循环orderItems，然后将orderItem中的数据插入到购物车中
        orderItems.forEach(orderItem -> {
            AddToCartDTO addToCartDTO = new AddToCartDTO();
            addToCartDTO.setId(orderItem.getProductId());
            addToCartDTO.setNumber(orderItem.getQuantity());
            addToCartDTO.setSkuId(orderItem.getSkuId());
            addToCartDTO.setType(orderItem.getProductType());
            addToCartDTO.setShopId(orderItem.getShopId());
            //添加分销员id
            //addToCartDTO.setSalesmanId();
            addToCartDTO.setIsQuick(false);
            if (orderItem.getExtraSkuData() != null && !"[]".equals(orderItem.getExtraSkuData())) {
                JSONArray extraSkuData = JsonUtil.fromJson(orderItem.getExtraSkuData(), JSONArray.class);
                List<Integer> extraAttrIds = new ArrayList<>();
                if (extraSkuData != null) {
                    for (Object obj : extraSkuData) {
                        if (obj instanceof JSONObject jsonObject) {
                            Integer attributesId = jsonObject.getInteger("attributes_id");
                            if (attributesId != null) {
                                extraAttrIds.add(attributesId);
                            }
                        }
                    }
                }
                //将放入extraAttrIds中转为字符串
                if (!extraAttrIds.isEmpty()) {
                    String extraAttrIdsStr = extraAttrIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                    addToCartDTO.setExtraAttrIds(extraAttrIdsStr);
                }
                //添加购物车
                this.addProductToCart(userId, addToCartDTO);

            }

        });
        return true;
    }

    @Transactional
    @Override
    public Boolean addProductToCart(Integer userId, AddToCartDTO dto) {
        // 校验实名认证功能
        String isIdentity = configService.getConfigVal(SettingsEnum.IS_IDENTITY);
        if ("1".equals(isIdentity) && tigshopProperties.getIsB2b() == 1) {
            User user = userService.getById(SecurityUtils.getCurrentUserId());
            if (user != null && user.getIsCompanyAuth() == 0) {
                throw new GlobalException("请先完成实名认证");
            }
        }
        // 初始化数据
        dto.initParam();

        List<AddToCartDTO.SkuItem> skuItems = dto.getSkuItem();
        if (CollUtil.isEmpty(skuItems)) {
            skuItems = new ArrayList<>();
            int skuId = dto.getSkuId() != null ? dto.getSkuId() : 0;
            dto.setSkuId(skuId);
            Integer number = dto.getNumber() == null ? 1 : dto.getNumber();
            skuItems.add(new AddToCartDTO.SkuItem(dto.getId(), skuId, number));
        }

        ProductDetailDTO product = productService.detail(dto.getId());

        // 如果为已经购买的付费商品则报错
        if (product.getProductType().equals(PRODUCT_TYPE_PAID.getCode())) {
            Integer buy = productService.isBuy(product.getProductId());
            if (buy == 1) {
                throw new GlobalException("您已购买该付费商品");
            }
        }

        // 检测店铺状态
        boolean shopFlag = product.getShopId() > 0 && !shopService.checkShopStatus(product.getShopId(), product.getProductName());
        Assert.isFalse(shopFlag, () -> new GlobalException(translatePackage.translate("店铺已关闭！")));

        // 检测商品状态
        Assert.isFalse(product.getProductStatus() == ProductStatus.OFF_SALE.getCode(), () -> new GlobalException(translatePackage.translate("该商品已下架！")));

        // 检查商品数量
        boolean skuNumFlag = skuItems.stream().anyMatch(skuItem -> skuItem.getNum() == null || skuItem.getNum() <= 0);
        Assert.isFalse(skuNumFlag, () -> new GlobalException(translatePackage.translate("商品数量错误！")));


        // 卡券商品检查卡券库存
        if (dto.getType().equals(CartTypeEnum.TYPE_CARD.getCode())) {
            Long cardCount = eCardMapper.selectCount(new LambdaQueryWrapper<ECard>()
                    .eq(ECard::getGroupId, product.getCardGroupId()).eq(ECard::getIsUse, 0));
            if (cardCount < dto.getNumber()) {
                throw new GlobalException(translatePackage.translate("该商品卡券库存不足！"));
            }
        }

        // 检测是否超过商品购买限制
        skuItems.forEach(skuItem -> {
            Cart cart;
            if (skuItem.getSkuId() != null && skuItem.getSkuId() > 0) {
                cart = this.lambdaQuery().eq(Cart::getSkuId, skuItem.getSkuId()).eq(Cart::getUserId, userId).one();
            } else {
                Integer shopId = dto.getShopId();
                cart = this.lambdaQuery().eq(Cart::getProductId, skuItem.getProductId())
                        .eq(Cart::getUserId, userId)
                        .eq(Cart::getShopId, shopId)
                        .one();
            }

            if (cart != null) {
                if (dto.getIsQuick()) {
                    this.checkOrderProductLimitNumber(cart.getProductId(), userId, skuItem.getNum(), null);
                } else {
                    int changeQuantity = skuItem.getNum() + cart.getQuantity();
                    this.checkProductLimitNumber(cart.getProductId(), userId, changeQuantity, null);
                }
            } else {
                checkProductLimitNumber(dto.getId(), userId, skuItem.getNum(), null);
            }

        });

        // 获取商品规格信息
        List<ProductAvailabilityVO> productAvailabilities = skuItems.stream()
                .map(skuItem -> productService.getProductFinalPrice(skuItem.getProductId(), skuItem.getSkuId(), dto.getSalesmanId(), dto.getExtraAttrIds(), dto.getShopId()))
                .toList();

        // 商品附加属性
        List<Integer> extraAttrIdList = new ArrayList<>(StringUtils.str2IntList(dto.getExtraAttrIds()));
        List<CarExtraSkuDataDTO> list = new ArrayList<>();
        if (!extraAttrIdList.isEmpty()) {
            List<ProductAttribute> extraSkuData = productAttributesService.getProductAttributesByIds(extraAttrIdList);
            for (ProductAttribute productAttribute : extraSkuData) {
                CarExtraSkuDataDTO carExtraSkuDataDTO = new CarExtraSkuDataDTO();
                carExtraSkuDataDTO.setAttributesId(productAttribute.getAttributesId());
                carExtraSkuDataDTO.setAttrName(productAttribute.getAttrName());
                carExtraSkuDataDTO.setAttrValue(productAttribute.getAttrValue());
                carExtraSkuDataDTO.setAttrPrice(productAttribute.getAttrPrice());
                list.add(carExtraSkuDataDTO);
            }
        }

        // 获取已存在的购物车商品
        List<Integer> skuIds = skuItems.stream().map(AddToCartDTO.SkuItem::getSkuId).toList();
        List<Cart> carts = this.lambdaQuery().eq(Cart::getProductId, dto.getId())
                .eq(Cart::getUserId, userId)
                .eq(Cart::getType, dto.getType())
                .eq(tigshopProperties.getIsO2o() == 1, Cart::getShopId, dto.getShopId() != null ? dto.getShopId() : product.getShopId())
                .in(Cart::getSkuId, skuIds)
                .list();

        List<Cart> addCarts = new ArrayList<>();
        for (ProductAvailabilityVO productAvailability : productAvailabilities) {
            Cart cart = Cart.builder()
                    .userId(userId)
                    .productId(dto.getId())
                    .productSn(product.getProductSn())
                    .picThumb(product.getPicThumb())
                    .marketPrice(product.getMarketPrice())
                    .originalPrice(productAvailability.getPrice())
                    .skuId(dto.getSkuId())
                    .skuData(Optional
                            .ofNullable(productAvailability.getData())
                            .map(JsonUtil::toJson)
                            .orElse("[]"))
                    .productType(product.getProductType())
                    .isChecked(1)
                    .type(dto.getType())
                    .shopId(tigshopProperties.getIsO2o() == 1 && dto.getShopId() != null ? dto.getShopId() : product.getShopId())
                    .updateTime(StringUtils.getCurrentTime())
                    .salesmanId(dto.getSalesmanId())
                    .extraSkuData(CollUtil.isNotEmpty(list) ? JSONUtil.toJsonStr(list) : "")
                    .build();


            ProductSkuDTO sku = productAvailability.getSku();
            if (sku != null) {
                cart.setOriginalPrice(new BigDecimal(sku.getSkuPrice()));
                cart.setSkuId(sku.getSkuId());
                cart.setSkuData(Optional
                        .ofNullable(sku.getSkuData())
                        .map(JsonUtil::toJson)
                        .orElse("[]"));
            }

            Optional<AddToCartDTO.SkuItem> skuItemOptional = skuItems.stream().filter(skuItem -> skuItem.getSkuId().equals(cart.getSkuId())).findFirst();
            skuItemOptional.ifPresent(skuItem -> cart.setQuantity(skuItem.getNum()));

            Optional<Cart> cartOptional = carts.stream().filter(item -> item.getSkuId().equals(cart.getSkuId()) && (tigshopProperties.getIsO2o() != 1 || item.getShopId().equals(cart.getShopId()))).findFirst();
            if (dto.getIsQuick()) {
                cartOptional.ifPresent(item -> cart.setQuantity(cart.getQuantity()));
            } else {
                cartOptional.ifPresent(item -> cart.setQuantity(cart.getQuantity() + item.getQuantity()));
            }

            addCarts.add(cart);
        }

        // 检查库存
        for (Cart cart : addCarts) {
            boolean stockFlag = productSkuService.checkProductStock(cart.getQuantity(), cart.getProductId(), cart.getSkuId());
            Assert.isTrue(stockFlag, () -> new GlobalException(translatePackage.translate(String.format("商品:%s 已加购总数达库存上限", product.getProductName()))));
        }

        if (dto.getIsQuick()) {
            // 先将会员的所有其它购物车商品设置为未选择
            this.lambdaUpdate().set(Cart::getIsChecked, 0).eq(Cart::getUserId, userId).update();
        }

        // 如果购物车中商品已存在
        if (CollUtil.isNotEmpty(carts)) {
            // 删除原购物车数据
            List<Integer> cartIds = carts.stream().map(Cart::getCartId).toList();
            this.removeByIds(cartIds);
        }

        this.saveBatch(addCarts);

        return true;
    }

    @Override
    public Boolean checkProductLimitNumber(Integer productId, Integer userId, Integer quantity, Integer cartId) {
        // 查询产品信息
        Product product = productService.getById(productId);
        if (product == null) {
            return false;
        }

        // 检查店铺状态
        shopService.checkShopStatus(product.getShopId(), product.getProductName());

        Integer limitNumber = product.getLimitNumber();
        if (limitNumber != null && limitNumber > 0) {
            // 查询符合条件的订单ID列表
            QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
            orderWrapper.select("order_id")
                    .eq("user_id", userId)
                    .in("pay_status", Arrays.asList(
                            PaymentStatus.UNPAID.getCode(),
                            PaymentStatus.PROCESSING.getCode(),
                            PaymentStatus.PAID.getCode()))
                    .in("order_status", Arrays.asList(
                            OrderStatusEnum.PENDING.getCode(),
                            OrderStatusEnum.CONFIRMED.getCode(),
                            OrderStatusEnum.PROCESSING.getCode(),
                            OrderStatusEnum.COMPLETED.getCode()));

            List<Integer> orderIds = orderService.list(orderWrapper)
                    .stream()
                    .map(Order::getOrderId)
                    .collect(Collectors.toList());

            // 计算已购买数量
            int quantitySum = 0;
            if (!orderIds.isEmpty()) {
                QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
                itemWrapper
                        .in("order_id", orderIds)
                        .eq("product_id", productId);

                List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);
                quantitySum = Optional.ofNullable(orderItems)
                        .map(list -> list.stream().mapToInt(OrderItem::getQuantity).sum())
                        .orElse(0);

                // 查询售后数量
                List<Aftersales> aftersales = aftersalesService.lambdaQuery()
                        .in(Aftersales::getOrderId, orderIds)
                        .eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode())
                        .list();
                if (CollUtil.isNotEmpty(aftersales) && CollUtil.isNotEmpty(orderItems)) {
                    List<Integer> aftersaleIds = aftersales.stream().map(Aftersales::getAftersaleId).toList();
                    List<Integer> orderItemIds = orderItems.stream().map(OrderItem::getItemId).toList();

                    List<AftersalesItem> aftersalesItems = aftersalesItemService.lambdaQuery()
                            .in(AftersalesItem::getOrderItemId, orderItemIds)
                            .in(AftersalesItem::getAftersaleId, aftersaleIds)
                            .list();

                    for (AftersalesItem aftersalesItem : aftersalesItems) {
                        quantitySum = quantitySum - aftersalesItem.getNumber();
                    }
                }

            }

            // 计算购物车数量
            Integer cartNum = 0;
            if (cartId != null && cartId > 0) {
                List<Cart> carts = this.lambdaQuery()
                        .eq(Cart::getProductId, productId)
                        .eq(Cart::getUserId, userId)
                        .eq(Cart::getType, CartTypeEnum.TYPE_NORMAL.getCode())
                        .list();
                cartNum = carts.stream().map(Cart::getQuantity).reduce(0, Integer::sum);
            }

            // 检查购买限制
            if (quantity > (limitNumber - quantitySum - cartNum)) {

                if (cartId != null && cartId > 0) {
                    // 更新购物车状态
                    Cart updateCart = new Cart();
                    updateCart.setIsChecked(0);
                    this.update(updateCart,
                            new LambdaUpdateWrapper<Cart>().eq(Cart::getCartId, cartId));
                }

                throw new GlobalException(String.format("您购买的%s数量超过该商品购买限制，请减少数量再下单", product.getProductName()));
            }
        }
        return true;
    }

    @Override
    public Boolean checkOrderProductLimitNumber(Integer productId, Integer userId, Integer quantity, Integer cartId) {
        // 查询产品信息
        Product product = productService.getById(productId);
        if (product == null) {
            return false;
        }
        Integer limitNumber = product.getLimitNumber();
        if (!(limitNumber != null && limitNumber > 0)) {
            return true;
        }

        // 检查店铺状态
        shopService.checkShopStatus(product.getShopId(), product.getProductName());

        // 查询符合条件的订单ID列表
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.select("order_id")
                .eq("user_id", userId)
                .in("pay_status", Arrays.asList(
                        PaymentStatus.UNPAID.getCode(),
                        PaymentStatus.PROCESSING.getCode(),
                        PaymentStatus.PAID.getCode()))
                .in("order_status", Arrays.asList(
                        OrderStatusEnum.PENDING.getCode(),
                        OrderStatusEnum.CONFIRMED.getCode(),
                        OrderStatusEnum.PROCESSING.getCode(),
                        OrderStatusEnum.COMPLETED.getCode()));

        List<Integer> orderIds = orderService.list(orderWrapper)
                .stream()
                .map(Order::getOrderId)
                .collect(Collectors.toList());

        // 计算已购买数量
        int quantitySum = 0;
        if (!orderIds.isEmpty()) {
            QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
            itemWrapper
                    .in("order_id", orderIds)
                    .eq("product_id", productId);

            List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);
            quantitySum = Optional.ofNullable(orderItems)
                    .map(list -> list.stream().mapToInt(OrderItem::getQuantity).sum())
                    .orElse(0);

            // 查询售后数量
            List<Aftersales> aftersales = aftersalesService.lambdaQuery()
                    .in(Aftersales::getOrderId, orderIds)
                    .eq(Aftersales::getStatus, AftersalesStatusEnum.COMPLETE.getCode())
                    .list();
            if (CollUtil.isNotEmpty(aftersales) && CollUtil.isNotEmpty(orderItems)) {
                List<Integer> aftersaleIds = aftersales.stream().map(Aftersales::getAftersaleId).toList();
                List<Integer> orderItemIds = orderItems.stream().map(OrderItem::getItemId).toList();

                List<AftersalesItem> aftersalesItems = aftersalesItemService.lambdaQuery()
                        .in(AftersalesItem::getOrderItemId, orderItemIds)
                        .in(AftersalesItem::getAftersaleId, aftersaleIds)
                        .list();

                for (AftersalesItem aftersalesItem : aftersalesItems) {
                    quantitySum = quantitySum - aftersalesItem.getNumber();
                }
            }
        }

        // 检查购买限制
        if (quantity > (limitNumber - quantitySum)) {

            if (cartId != null && cartId > 0) {
                // 更新购物车状态
                Cart updateCart = new Cart();
                updateCart.setIsChecked(0);
                this.update(updateCart,
                        new LambdaUpdateWrapper<Cart>().eq(Cart::getCartId, cartId));
            }

            throw new GlobalException(String.format("您购买的%s数量超过该商品购买限制，请减少数量再下单", product.getProductName()));
        }
        return true;
    }

    @Override
    public void clearCart(Integer userId, Integer type) {
        this.remove(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId).eq(Cart::getType, type));
    }

    @Override
    public Integer getCartTypeByProduct(Integer id, Integer type) {
        Product product = productService.getById(id);
        Assert.notNull(product, () -> new GlobalException("商品不存在"));

        return switch (type) {
            case 2 -> CartTypeEnum.TYPE_PIN.getCode();
            case 3 -> CartTypeEnum.TYPE_EXCHANGE.getCode();
            case 4 -> CartTypeEnum.TYPE_GIFT.getCode();
            case 5 -> CartTypeEnum.TYPE_BARGAIN.getCode();
            default -> switch (product.getProductType()) {
                case 1 -> CartTypeEnum.TYPE_NORMAL.getCode();
                case 2 -> CartTypeEnum.TYPE_VIRTUAL.getCode();
                case 3 -> CartTypeEnum.TYPE_CARD.getCode();
                case 4 -> CartTypeEnum.TYPE_PAID.getCode();
                default -> throw new GlobalException("商品类型错误");
            };
        };
    }
}