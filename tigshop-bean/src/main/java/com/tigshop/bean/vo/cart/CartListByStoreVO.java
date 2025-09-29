// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车数据
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "购物车数据")
public class CartListByStoreVO {
    @Schema(description = "统计数据")
    private CartTotal total;

    @Schema(description = "示例名称")
    private List<CartByStoreVO> cartList;

    @Data
    @Schema(description = "统计数据")
    public static class CartTotal {
        @Schema(description = "商品价格（已选）")
        private BigDecimal productAmount;

        @Schema(description = "商品数（已选）")
        private Integer checkedCount;

        @Schema(description = "优惠 组合商品（已选）")
        private BigDecimal discounts;

        @Schema(description = "优惠后总价")
        private BigDecimal discountAfter;

        @Schema(description = "总商品数")
        private Integer totalCount;

        @Schema(description = "优惠券优惠金额")
        private BigDecimal discountCouponAmount;

        @Schema(description = "秒杀优惠金额")
        private BigDecimal discountSeckillAmount;

        @Schema(description = "满减优惠金额")
        private BigDecimal discountProductPromotionAmount;

        @Schema(description = "限时折扣优惠金额")
        private BigDecimal discountTimeDiscountAmount;

        @Schema(description = "其他优惠优惠金额")
        private BigDecimal discountDiscountAmount;

        @Schema(description = "附加费用")
        private BigDecimal serviceFee;

        //初始化所有值为0
        public CartTotal() {
            this.productAmount = BigDecimal.ZERO;
            this.checkedCount = 0;
            this.discounts = BigDecimal.ZERO;
            this.discountAfter = BigDecimal.ZERO;
            this.totalCount = 0;
            this.discountCouponAmount = BigDecimal.ZERO;
            this.discountSeckillAmount = BigDecimal.ZERO;
            this.discountProductPromotionAmount  = BigDecimal.ZERO;
            this.discountTimeDiscountAmount = BigDecimal.ZERO;
            this.discountDiscountAmount = BigDecimal.ZERO;
            this.serviceFee = BigDecimal.ZERO;
        }
        public void twoDecimalPlaces(){
            this.productAmount = this.productAmount.setScale(2, RoundingMode.HALF_UP);
            this.discounts = this.discounts.setScale(2, RoundingMode.HALF_UP);
            this.discountAfter = this.discountAfter.setScale(2, RoundingMode.HALF_UP);
            this.discountCouponAmount = this.discountCouponAmount.setScale(2, RoundingMode.HALF_UP);
            this.discountSeckillAmount = this.discountSeckillAmount.setScale(2, RoundingMode.HALF_UP);
            this.discountProductPromotionAmount = this.discountProductPromotionAmount.setScale(2, RoundingMode.HALF_UP);
            this.discountTimeDiscountAmount = this.discountTimeDiscountAmount.setScale(2, RoundingMode.HALF_UP);
            this.discountDiscountAmount = this.discountDiscountAmount.setScale(2, RoundingMode.HALF_UP);
            this.serviceFee = this.serviceFee.setScale(2, RoundingMode.HALF_UP);
        }

    }

    public CartListByStoreVO() {
        this.total = new CartTotal();
        this.cartList = new ArrayList<>();
    }

}