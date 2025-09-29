// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.controller.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.cart.AddToCartDTO;
import com.tigshop.bean.dto.product.ProductDetailDTO;
import com.tigshop.bean.dto.promotion.PointsExchangeListDTO;
import com.tigshop.bean.enums.cart.CartTypeEnum;
import com.tigshop.bean.vo.product.ExchangeProductToCartVO;
import com.tigshop.bean.vo.product.ProductDetailVO;
import com.tigshop.bean.vo.promotion.PointsExchangeVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.cart.CartService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.promotion.PointsExchangeService;
import com.tigshop.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 积分兑换
 *
 * @author Tigshop团队
 * @create 2025年02月12日 14:59
 */
@RestController
@RequestMapping("/api/product/exchange")
@Tag(name = "积分兑换")
@Validated
public class ExchangeController {
    @Resource
    private ProductService productService;

    @Resource
    private CartService cartService;

    @Resource
    private PointsExchangeService pointsExchangeService;

    @Resource
    private ProductSkuService productSkuService;

    @Resource
    private UserService userService;

    @GetMapping("/detail")
    @Operation(summary = "积分兑换详情")
    public ProductDetailVO detail(@RequestParam("id") Integer productId,
                                  @RequestParam(value = "shopId", required = false) Integer shopId,
                                  @RequestParam(value = "nearestShopId", required = false) Integer nearestShopId) {

        PointsExchangeVO pointsExchange = pointsExchangeService.detail(productId);
        ProductDetailVO productDetailVO = productService.clientDetail(pointsExchange.getProductSn(), shopId, nearestShopId);
        productDetailVO.setExchangeInfo(pointsExchangeService.detail(productId));
        return productDetailVO;
    }

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<PointsExchangeVO> list(PointsExchangeListDTO listDTO) {
        return pointsExchangeService.list(listDTO);
    }

    @PostMapping("/addToCart")
    @Operation(summary = "添加积分商品购物车")
    public ExchangeProductToCartVO addExchangeProductToCart(@RequestBody AddToCartDTO addToCartDTO) {
        Integer userId = getCurrentUserId();
        PointsExchangeVO pointsExchange = pointsExchangeService.detail(addToCartDTO.getId());
        if (pointsExchange == null) {
            throw new GlobalException("活动不存在");
        }
        if (pointsExchange.getIsEnabled() == 0) {
            throw new GlobalException("活动已下架");
        }

        Integer productId = pointsExchange.getProductId();

        ProductDetailDTO product = productService.detail(productId);
        if (product == null) {
            throw new GlobalException("未找到兑换商品");
        }

        if (product.getProductStatus() == 0 || product.getIsDelete() == 1) {
            throw new GlobalException("商品不存在");
        }

        //检测库存
        int skuId = addToCartDTO.getSkuId() != null ? addToCartDTO.getSkuId() : 0;
        int stock = productSkuService.getProductStock(productId, skuId);
        if (stock < addToCartDTO.getNumber()) {
            throw new GlobalException("库存不足");
        }
        //检测积分是否充足
        int points = userService.detail(userId).getPoints();
        if (points < pointsExchange.getExchangeIntegral() * addToCartDTO.getNumber()) {
            throw new GlobalException("您的积分不足");
        }
        Integer cartType = CartTypeEnum.TYPE_EXCHANGE.getCode();
        //清空购物车
        cartService.clearCart(userId, cartType);
        //加入购物车
        addToCartDTO.setType(cartType);
        addToCartDTO.setId(productId);
        cartService.addProductToCart(userId, addToCartDTO);

        return ExchangeProductToCartVO.builder().item(true).flowType(cartType).build();
    }

}