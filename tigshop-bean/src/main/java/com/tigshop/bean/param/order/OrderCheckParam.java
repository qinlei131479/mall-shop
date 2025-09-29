// **---------------------------------------------------------------------+
// ** 文件 -- ${DESCRIPTION}
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.param.order;

import com.tigshop.bean.enums.cart.CartTypeEnum;
import com.tigshop.bean.enums.order.SelectFromEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 结算操作
 *
 * @author Jayce
 * @create 2024年10月22日 13:50
 */
@Data
@Schema(description = "结算操作")
public class OrderCheckParam {

    @Schema(description = "地址ID")
    private Integer addressId;

    @Schema(description = "配送方式")
    private Map<Integer, ShippingType> shippingType;

    @Schema(description = "支付方式ID")
    private Integer payTypeId;

    @Schema(description = "是否使用积分")
    private Integer usePoint;

    @Schema(description = "是否使用余额")
    private BigDecimal useBalance;

    @Schema(description = "使用的优惠券ID列表")
    private List<Integer> useCouponIds;

    @Schema(description = "备注")
    private String buyerNote;

    @Schema(description = "发票")
    private InvoiceData invoiceData;

    @Schema(description = "流程类型")
    private Integer flowType;

    // *** UNKNOWN ***

    @Schema(description = "是否使用默认优惠劵")
    private Integer useDefaultCouponIds;

    @Schema(description = "选择的用户优惠券ID列表")
    private List<Integer> selectUserCouponIds;

    @Schema(description = "附加属性")
    private Map<String, String> productExtra;

    @Schema(description = "区域")
    private List<Integer> regionIds;

    @Schema(description = "从哪个页面跳转 1.首次加载 2.更新结算数据（配送类型，使用积分等） 3.更新优惠券 4.提交")
    private Integer selectFrom;

    @Schema(description = "门店id")
    private Integer shopId;

    @Data
    @Schema(description = "发票")
    public static class InvoiceData {
        @Schema(description = "发票类型")
        private Integer invoiceType;

        @Schema(description = "抬头类型")
        private Integer titleType;

        @Schema(description = "抬头")
        private String title;

        @Schema(description = "税号")
        private String taxNo;

        @Schema(description = "内容")
        private String content;

        @Schema(description = "电话")
        private String taxPhone;

        @Schema(description = "个人/单位名称")
        private String companyName;

        @Schema(description = "邮箱")
        private String email;

        @Schema(description = "手机号")
        private String mobile;

    }

    @Data
    @Schema(description = "配送方式")
    public static class ShippingType {

        @Schema(description = "配送方式ID")
        private Integer typeId;

        @Schema(description = "店铺ID")
        private Integer shopId;

        @Schema(description = "配送方式名称")
        private String typeName;

        @Schema(description = "自提点id")
        private Integer pickupId;

        @Schema(description = "自提时间")
        private String expectPickupTime;
    }

    public void indexInitParam() {
        this.selectFrom = SelectFromEnum.FIRST_LOAD.getValue();
        this.useBalance = BigDecimal.ZERO;
        this.usePoint = 0;
    }

    public void submitInitParam(String useCoupon) {
        this.selectFrom = SelectFromEnum.CHECK_SUBMIT.getValue();
        this.useCouponIds =  "0".equals(useCoupon) ? Collections.emptyList() : this.useCouponIds;
        this.flowType = this.flowType == null ? CartTypeEnum.TYPE_NORMAL.getCode() : this.flowType;
    }

}
