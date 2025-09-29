package com.tigshop.bean.param.print;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 打印机更新DTO
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "打印机更新DTO")
public class PrintUpdateParam {

    @Schema(description = "打印机ID")
    @NotNull(message = "打印机ID不能为空")
    private Integer printId;

    @Schema(description = "打印机名称")
    @NotBlank(message = "打印机名称不能为空")
    private String printName;

    @Schema(description = "第三方平台对接账号")
    @NotBlank(message = "第三方平台对接账号不能为空")
    private String thirdAccount;

    @Schema(description = "第三方平台key")
    @NotBlank(message = "第三方平台key不能为空")
    private String thirdKey;

    @Schema(description = "打印机SN")
    @NotBlank(message = "打印机SN不能为空")
    private String printSn;

    @Schema(description = "打印机KEY")
    @NotBlank(message = "打印机KEY不能为空")
    private String printKey;

    @Schema(description = "打印机状态 1开启 2关闭")
    @NotNull(message = "打印机状态")
    private Integer status;

    @Schema(description = "订单支付自动打印 1开启 2关闭")
    @NotNull(message = "订单支付自动打印不能为空")
    private Integer autoPrint;
}