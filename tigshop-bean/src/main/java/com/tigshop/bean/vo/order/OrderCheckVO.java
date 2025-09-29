// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.order;

import com.tigshop.bean.param.order.OrderCheckParam;
import com.tigshop.bean.vo.cart.CartByStoreVO;
import com.tigshop.bean.vo.o2o.DeliveryOptionVO;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Tigshop
 */
@Data
@Schema(description = "订单检查信息")
public class OrderCheckVO {

//    @Schema(description = "地址列表")
//    private List<UserAddressVO> addressList;
//
//    @Schema(description = "可用支付方式")
//    private List<PaymentType> availablePaymentType;

//    @Schema(description = "店铺配送方式")
//    private List<List<ShippingType>> storeShippingType;

    @Schema(description = "购物车列表")
    private List<CartByStoreVO> cartList;

    @Schema(description = "总计信息")
    private Total total;

//    @Schema(description = "余额")
//    private BigDecimal balance;
//
//    @Schema(description = "积分")
//    private Integer points;

   @Schema(description = "可用积分")
   private Integer availablePoints;

    @Schema(description = "优惠券列表")
    private CouponList couponList;

    @Schema(description = "订单项信息")
    private Item item;

    @Schema(description = "使用的优惠券ID")
    private List<Integer> useCouponIds;

    @Schema(description = "选择的用户优惠券ID")
    private List<Integer> selectUserCouponIds;

    @Schema(description = "模板ID")
    private List<String> tmplIds;

    @Schema(description = "流程类型")
    private Integer flowType;

    @Schema(description = "消息")
    private String message;

    @Schema(description = "商品配送方式")
    private DeliveryOptionVO deliveryOption;

    @Data
    @Schema(description = "支付方式")
    public static class PaymentType {
        @Schema(description = "支付类型ID")
        private Integer typeId;
        @Schema(description = "支付类型名称")
        private String typeName;
        @Schema(description = "是否禁用")
        private Boolean disabled;
        @Schema(description = "禁用描述")
        private String disabledDesc;
        @Schema(description = "是否显示")
        private Boolean isShow;
    }

    @Data
    @Schema(description = "配送方式")
    public static class ShippingType {
        @Schema(description = "配送方式ID")
        private Integer shippingTypeId;
        @Schema(description = "配送方式名称")
        @JsonTranslate
        private String shippingTypeName;
        @Schema(description = "默认配送方式ID")
        private Integer shippingDefaultId;
        @Schema(description = "配送方式描述")
        private String shippingTypeDesc;
        @Schema(description = "配送时间描述")
        private String shippingTimeDesc;
        @Schema(description = "是否支持货到付款")
        private Integer isSupportCod;
        @Schema(description = "排序号")
        private Integer sortOrder;
        @Schema(description = "店铺ID")
        private Integer shopId;
    }

    @Data
    @Schema(description = "总计信息")
    public static class Total {
        @Schema(description = "产品金额")
        private BigDecimal productAmount;
        @Schema(description = "选中数量")
        private Integer checkedCount;
        @Schema(description = "折扣")
        private BigDecimal discounts;
        @Schema(description = "折扣后金额")
        private String discountAfter;
        @Schema(description = "总数量")
        private Integer totalCount;
        @Schema(description = "优惠券折扣金额")
        private BigDecimal discountCouponAmount;
        @Schema(description = "折扣金额")
        private BigDecimal discountDiscountAmount;
        @Schema(description = "秒杀折扣金额")
        private BigDecimal discountSeckillAmount;
        @Schema(description = "产品促销折扣金额")
        private BigDecimal discountProductPromotionAmount;
        @Schema(description = "限时折扣金额")
        private BigDecimal discountTimeDiscountAmount;
        @Schema(description = "服务费")
        private BigDecimal serviceFee;
        @Schema(description = "已付金额")
        private BigDecimal paidAmount;
        @Schema(description = "优惠券金额")
        private BigDecimal couponAmount;
        @Schema(description = "折扣金额")
        private BigDecimal discountAmount;
        @Schema(description = "兑换积分")
        private Integer exchangePoints;
        @Schema(description = "积分金额")
        private BigDecimal pointsAmount;
        @Schema(description = "运费")
        private BigDecimal shippingFee;
        @Schema(description = "店铺运费")
        private Map<Integer, BigDecimal> storeShippingFee;
        @Schema(description = "总金额")
        private BigDecimal totalAmount;
        @Schema(description = "订单发送积分")
        private Integer orderSendPoint;
        @Schema(description = "余额")
        private BigDecimal balance;
        @Schema(description = "未付金额")
        private BigDecimal unpaidAmount;

        public BigDecimal getShippingFeeByShopId(Integer shopId) {
            if (Objects.isNull(this.storeShippingFee)){
                return BigDecimal.ZERO;
            }
            return this.storeShippingFee.get(shopId);
        }
    }

    @Data
    @Schema(description = "运费")
    public static class ShippingFee {
        @Schema(description = "总数")
        private BigDecimal total;
        @Schema(description = "禁用的优惠券")
        private Map<Integer, BigDecimal> storeShippingFee;
    }

    @Data
    @Schema(description = "优惠券列表")
    public static class CouponList {
        @Schema(description = "启用的优惠券")
        private List<OrderCheckVO.Coupon> enableCoupons;
        @Schema(description = "禁用的优惠券")
        private List<OrderCheckVO.Coupon> disableCoupons;
    }

    @Data
    @Schema(description = "订单项信息")
    public static class Item {
        @Schema(description = "地址ID")
        private Integer addressId;
        @Schema(description = "配送方式")
        private Map<Integer, OrderCheckParam.ShippingType> shippingType;
        @Schema(description = "支付类型ID")
        private Integer payTypeId;
        @Schema(description = "使用积分")
        private Integer usePoint;
        @Schema(description = "使用余额")
        private Integer useBalance;
        @Schema(description = "使用的优惠券ID")
        private List<Integer> useCouponIds;
        @Schema(description = "选择的用户优惠券ID")
        private List<Integer> selectUserCouponIds;
        @Schema(description = "流程类型")
        private Integer flowType;
    }

    @Data
    @Schema(description = "订单结算优惠券信息")
    public static class Coupon {

        @Schema(description = "优惠券ID")
        private Integer id;

        @Schema(description = "优惠券ID")
        private Integer couponId;

        @Schema(description = "优惠券名称")
        private String couponName;

        @Schema(description = "优惠券类型")
        private Integer couponType;

        @Schema(description = "优惠券面额")
        private BigDecimal couponMoney;

        @Schema(description = "优惠券最小订单金额")
        private BigDecimal minOrderAmount;

        @Schema(description = "优惠券描述")
        private String couponDesc;

        @Schema(description = "是否全局优惠券")
        private Boolean isGlobal;

        @Schema(description = "优惠券折扣")
        private BigDecimal couponDiscount;

        @Schema(description = "店铺ID")
        private Integer shopId;

        @Schema(description = "优惠券结束日期")
        private String endDate;

        @Schema(description = "用户优惠券ID")
        private Integer userCouponId;

        @Schema(description = "是否选择")
        private Boolean selected;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Coupon coupon = (Coupon) o;
            return Objects.equals(id, coupon.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }

}
