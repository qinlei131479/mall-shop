package com.tigshop.bean.dto.product;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author wzh
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PriceInquiryDTO extends BasePage {
    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "回复状态：0 未回复 1 已回复")
    private Integer status;

    @Schema(description = "商品id")
    private Integer productId;

    @Schema(description = "店铺id")
    private Integer shopId;

}
