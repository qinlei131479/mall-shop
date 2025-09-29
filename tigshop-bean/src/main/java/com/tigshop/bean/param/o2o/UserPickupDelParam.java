package com.tigshop.bean.param.o2o;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Tigshop项目组
 * @create 2025年09月10日 13:15
 */
@Data
@Schema(name = "UserPickupParam", description = "用户自提信息参数")
public class UserPickupDelParam {
    @Schema(description = "自提信息ID")
    private Integer userPickupId;

}