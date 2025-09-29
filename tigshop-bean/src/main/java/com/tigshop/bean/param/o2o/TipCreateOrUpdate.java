package com.tigshop.bean.param.o2o;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "门店标签创建参数")
public class TipCreateOrUpdate {
    @Schema(description = "门店标签id 编辑时必传")
    private Integer tipId;

    @Schema(description = "门店标签名称")
    @NotBlank(message = "门店标签名称不能为空")
    private String tipName;

    @Schema(description = "0 禁用，1 启用")
    @NotNull(message = "0 禁用，1 启用")
    private Integer status;
}