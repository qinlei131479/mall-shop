package com.tigshop.bean.dto.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 充值支付数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "充值支付参数")
public class UserRechargeOrderPayDTO {
    @Schema(description = "订单ID")
    private Integer id;

    @Schema(description = "支付类型")
    private String type;

    @Schema(description = "code")
    private String code;
}
