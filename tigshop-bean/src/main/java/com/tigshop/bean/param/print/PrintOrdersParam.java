package com.tigshop.bean.param.print;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 打印小票
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "打印小票")
public class PrintOrdersParam {
    @Schema(description = "订单ids")
    @NotNull(message = "订单id不能为空")
    private List<Integer> ids;
}