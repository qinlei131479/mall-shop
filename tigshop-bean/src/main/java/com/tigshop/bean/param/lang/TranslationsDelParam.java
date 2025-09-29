package com.tigshop.bean.param.lang;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 批量操作入参
 *
 * @author kidd
 * @since 2025/4/19 13:59
 */
@Schema(description = "操作入参")
@Data
public class TranslationsDelParam {


    @Schema(description = "id")
    private Integer id;


}
