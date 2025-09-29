package com.tigshop.bean.param.finance.refundapply;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 退款申请审核参数
 *
 * @author kidd
 * @since 2025/4/10 11:26
 */
@Data
public class RefundApplyAuditParam {

    @NotNull(message = "退款申请ID不能为空")
    @Schema(description = "退款申请ID")
    private Long refundId;

    @Schema(description = "退款状态")
    private Integer refundStatus;

    @Schema(description = "退款备注")
    private String refundNote;

    @Schema(description = "线上退款金额")
    private String onlineBalance;

    @Schema(description = "线下退款金额")
    private String offlineBalance;

    @Schema(description = "打款凭证")
    private String paymentVoucher;

    @Schema(description = "退款金额")
    private String refundBalance;

    public void initParam() {
        if (this.refundStatus == null) {
            this.refundStatus = 1;
        }
    }
}
