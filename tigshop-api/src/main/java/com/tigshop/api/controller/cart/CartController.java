package com.tigshop.api.controller.cart;

import com.tigshop.bean.dto.cart.*;
import com.tigshop.bean.enums.cart.CartTypeEnum;
import com.tigshop.bean.model.cart.Cart;
import com.tigshop.bean.vo.cart.CartListByStoreVO;
import com.tigshop.bean.vo.content.CartCouponDiscountVO;
import com.tigshop.bean.vo.product.ExchangeProductToCartVO;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.cart.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.ResultTextConstants.ID_CANNOT_IS_NULL;

/**
 * 订单管理控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/api/cart/cart")
@Validated
@Tag(name = "购物车管理", description = "购物车管理")
public class CartController {

    @Resource
    CartService cartService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public CartListByStoreVO list(ListDTO listDTO) {
        return cartService.getCartList(listDTO);
    }

    @GetMapping("/getCount")
    @Operation(summary = "获取数量")
    public Integer getCount(@RequestParam(value = "shopId", required = false) Integer shopId) {
        return cartService.getCartCount(shopId);
    }

    @PostMapping("/updateCheck")
    @Operation(summary = "更新选中状态")
    public void updateCheck(@RequestBody UpdateCheckDTO updateCheckDTO) {
        cartService.updateCheck(updateCheckDTO.getData());
    }

    @PostMapping("/updateItem")
    @Operation(summary = "更新购物车商品")
    public void updateItem(@Valid @RequestBody UpdateItemDTO updateItem) {
        cartService.updateCartItem(updateItem);
    }

    @PostMapping("/removeItem")
    @Operation(summary = "删除购物车商品")
    public void removeItem(@RequestBody RemoveItemDTO removeItem) {
        cartService.removeBatchByIds(removeItem.getCartIds());
    }

    @PostMapping("/clear")
    @Operation(summary = "清空购物车")
    public void clear(@RequestParam(value = "shopId", required = false) Integer shopId) {
        cartService.lambdaUpdate()
                .eq(Cart::getUserId, SecurityUtils.getCurrentUserId())
                .eq(shopId != null, Cart::getShopId, shopId)
                .remove();
    }

    @GetMapping("/getCouponDiscount")
    @Operation(summary = "获取优惠券折扣")
    public CartCouponDiscountVO getCouponDiscount(@RequestParam(value = "couponId") @NotNull(message = ID_CANNOT_IS_NULL) Integer couponId) {
        return cartService.getCouponDiscount(couponId);
    }

    @PostMapping("/addToCart")
    @Operation(summary = "添加购物车")
    public ExchangeProductToCartVO addProductToCart(@RequestBody AddToCartDTO addToCartDTO) {
        if (addToCartDTO.getType() == null) {
            addToCartDTO.setType(CartTypeEnum.TYPE_NORMAL.getCode());
        }
        Integer cartType = cartService.getCartTypeByProduct(addToCartDTO.getId(), addToCartDTO.getType());
        addToCartDTO.setType(cartType);
        cartService.addProductToCart(SecurityUtils.getCurrentUserId(), addToCartDTO);
        return ExchangeProductToCartVO.builder()
                .item(true)
                .flowType(cartType)
                .build();
    }
}
