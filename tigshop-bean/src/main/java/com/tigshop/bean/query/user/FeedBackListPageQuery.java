package com.tigshop.bean.query.user;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Tigshop项目组
 */
@Getter
@Setter
@Schema(description = "会员留言")

public class FeedBackListPageQuery extends BasePage {

    @Schema(description = "留言类型")
    private String type;

    @Schema(description = "前端留言类型")
    private Integer isOrder;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "状态，0：待回复，1：已回复，2：无效")
    private Integer status;

    @Schema(description = "会员ID")
    private Integer userId;
}
