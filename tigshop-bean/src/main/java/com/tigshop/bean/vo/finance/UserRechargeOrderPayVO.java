package com.tigshop.bean.vo.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户充值金额快捷表VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "用户充值金额快捷表参数")
public class UserRechargeOrderPayVO {
    @Schema(description = "状态类型")
    private String statusType;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "充值金额")
    private BigDecimal amount;

    @Schema(description = "折扣金额")
    private BigDecimal discountMoney;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "支付时间")
    private String paidTime;

    @Schema(description = "备注")
    private String postscript;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "用户信息")
    private UserVO user;

    @Data
    @Schema(description = "用户信息")
    public static class UserVO {
        @Schema(description = "用户名")
        private String username;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "用户ID")
        private Integer userId;

        @Schema(description = "手机号")
        private String mobile;
    }
}
